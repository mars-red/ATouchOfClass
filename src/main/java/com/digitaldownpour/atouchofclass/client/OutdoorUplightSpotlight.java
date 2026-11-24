package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.ModClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.context.ContextKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class OutdoorUplightSpotlight {
    // Keep these limits in sync with outdoor_uplight_spotlight.fsh.
    private static final int MAX_LIGHTS = 8;
    private static final int MAX_OCCLUDERS = 56;
    private static final ContextKey<List<Light>> LIGHTS = new ContextKey<>(
            Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "outdoor_uplight_spotlights"));
    private static final int VEC4_BYTES = 4 * Float.BYTES;
    private static final int OCCLUDER_OFFSET = 9 + 2 * MAX_LIGHTS;
    private static final int WIDTH_SCALE_OFFSET = (OCCLUDER_OFFSET + 2 * MAX_LIGHTS * MAX_OCCLUDERS) * VEC4_BYTES;
    private static final int UNIFORM_BYTES = WIDTH_SCALE_OFFSET + MAX_LIGHTS * Float.BYTES;
    private static final float TILT = (float) Math.toRadians(15);
    private static final float HORIZONTAL_AIM = (float) Math.sin(TILT);
    private static final float VERTICAL_AIM = (float) Math.cos(TILT);
    private static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "pipeline/outdoor_uplight_spotlight"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "core/outdoor_uplight_spotlight"))
            .withBindGroupLayout(BindGroupLayout.builder()
                    .withUniform("SceneDepth", UniformType.COMBINED_IMAGE_SAMPLER)
                    .withUniform("Spotlights", UniformType.UNIFORM_BUFFER)
                    .build())
            .withColorTargetState(new ColorTargetState(
                    Optional.of(new BlendFunction(BlendFactor.DST_COLOR, BlendFactor.ONE)),
                    GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_COLOR))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build();

    private static @Nullable String lastReportedMode;

    private OutdoorUplightSpotlight() {}

    private record IrisShaderState(Object api, Method isShaderPackInUse, Method isReverseZDuringShaders) {
        private static final @Nullable IrisShaderState INSTANCE = find();

        private static @Nullable IrisShaderState find() {
            try {
                Class<?> apiType = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                return new IrisShaderState(apiType.getMethod("getInstance").invoke(null),
                        apiType.getMethod("isShaderPackInUse"), apiType.getMethod("isReverseZDuringShaders"));
            } catch (ReflectiveOperationException | LinkageError exception) {
                LogUtils.getLogger().debug("Outdoor uplight could not access the Iris API", exception);
                return null;
            }
        }

        static IrisMode mode() {
            if (INSTANCE == null) return IrisMode.UNSUPPORTED;
            try {
                if (Boolean.FALSE.equals(INSTANCE.isShaderPackInUse.invoke(INSTANCE.api))) {
                    return IrisMode.NO_SHADER_PACK;
                }
                return Boolean.TRUE.equals(INSTANCE.isReverseZDuringShaders.invoke(INSTANCE.api))
                        ? IrisMode.REVERSE_DEPTH : IrisMode.UNSUPPORTED;
            } catch (ReflectiveOperationException | LinkageError exception) {
                return IrisMode.UNSUPPORTED;
            }
        }
    }

    private enum IrisMode {
        NO_SHADER_PACK, REVERSE_DEPTH, UNSUPPORTED
    }

    @SubscribeEvent
    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerOptionalPipeline(PIPELINE);
    }

    static boolean isActive() {
        return activePipeline() != null;
    }

    static boolean isRequested() {
        return !ModClientConfig.FORCE_DISABLE_OUTDOOR_UPLIGHT_SPOTLIGHT.get() && irisMode() == IrisMode.REVERSE_DEPTH;
    }

    private static IrisMode irisMode() {
        return ModList.get().isLoaded("iris") ? IrisShaderState.mode() : IrisMode.NO_SHADER_PACK;
    }

    private static @Nullable CompiledRenderPipeline activePipeline() {
        return isRequested() ? spotlightPipeline() : null;
    }

    private static @Nullable CompiledRenderPipeline spotlightPipeline() {
        var target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        if (target.getColorTextureView() == null || target.getDepthTextureView() == null) return null;
        return RenderSystem.getCompiledPipelineNullable(PIPELINE);
    }

    @SubscribeEvent
    public static void extract(ExtractLevelRenderStateEvent event) {
        if (!isRequested()) return;
        var frame = event.getRenderState();
        Vec3 camera = frame.cameraRenderState.pos;
        // Vanilla consumes and clears the block entity render states before drawing the world.
        var lights = frame.blockEntityRenderStates.stream()
                .filter(OutdoorUplightRenderer.State.class::isInstance)
                .map(OutdoorUplightRenderer.State.class::cast)
                .filter(state -> state.spotlightAim() != null)
                .sorted(Comparator.comparingDouble(state -> camera.distanceToSqr(Vec3.atCenterOf(state.blockPos))))
                .limit(MAX_LIGHTS)
                .map(state -> {
                    Direction aim = state.spotlightAim();
                    Vec3 lens = Vec3.atBottomCenterOf(state.blockPos)
                            .add(aim.getStepX() * 0.15, 0.59, aim.getStepZ() * 0.15);
                    return new Light(lens, aim, state.spotlightStrength(), state.widthScale,
                            findOccluders(event.getLevel(), state.blockPos, lens, aim, state.widthScale));
                })
                .toList();
        frame.setRenderData(LIGHTS, lights);
    }

    static int horizontalRadius(float widthScale) {
        return (int) Math.ceil(7 + 2 * (widthScale - 1));
    }

    private static List<AABB> findOccluders(Level level, BlockPos fixture, Vec3 lens, Direction aim, float widthScale) {
        Vec3 axis = new Vec3(aim.getStepX() * HORIZONTAL_AIM, VERTICAL_AIM, aim.getStepZ() * HORIZONTAL_AIM);
        var boxes = new ArrayList<AABB>();
        // Bound the widened beam using its largest half-angle and the fixture tilt.
        int horizontalRadius = horizontalRadius(widthScale);
        double lateralSlope = 0.525 * widthScale;
        double radiusAllowance = Math.sqrt(1.0 + lateralSlope * lateralSlope);
        for (BlockPos pos : BlockPos.betweenClosed(fixture.offset(-horizontalRadius, 0, -horizontalRadius),
                fixture.offset(horizontalRadius, 9, horizontalRadius))) {
            if (pos.equals(fixture) || !level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()),
                    SectionPos.blockToSectionCoord(pos.getZ()))) continue;
            var state = level.getBlockState(pos);
            if (state.isAir()) continue;
            for (AABB shape : state.getCollisionShape(level, pos).toAabbs()) {
                AABB box = shape.move(pos.getX() - lens.x, pos.getY() - lens.y, pos.getZ() - lens.z);
                Vec3 center = box.getCenter();
                double radius = center.distanceTo(new Vec3(box.maxX, box.maxY, box.maxZ));
                double axial = center.dot(axis);
                double lateral = center.subtract(axis.scale(axial)).length();
                // Conservatively retain boxes that intersect the beam, including thin trim.
                if (axial + radius > 0 && center.length() - radius < 9.2
                        && lateral < Math.max(axial, 0) * lateralSlope + radius * radiusAllowance) {
                    boxes.add(box);
                }
            }
        }
        boxes.sort(Comparator.comparingDouble(box -> box.distanceToSqr(Vec3.ZERO)));
        return List.copyOf(boxes.subList(0, Math.min(MAX_OCCLUDERS, boxes.size())));
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterLevel event) {
        // Iris finishes compositing before AfterLevel and leaves its world depth on the main target.
        boolean forceWash = ModClientConfig.FORCE_DISABLE_OUTDOOR_UPLIGHT_SPOTLIGHT.get();
        IrisMode iris = forceWash ? IrisMode.NO_SHADER_PACK : irisMode();
        var pipeline = iris == IrisMode.REVERSE_DEPTH ? spotlightPipeline() : null;
        String mode = forceWash ? "wall wash (client override)" : switch (iris) {
            case NO_SHADER_PACK -> "wall wash (no active Iris shader pack)";
            case UNSUPPORTED -> "wall wash (unsupported Iris depth mode)";
            case REVERSE_DEPTH -> pipeline == null
                    ? "wall wash (spotlight pipeline unavailable)"
                    : "shader spotlight (after Iris composite)";
        };
        if (!mode.equals(lastReportedMode)) {
            lastReportedMode = mode;
            LogUtils.getLogger().info("Outdoor uplight renderer: {}", mode);
        }
        if (pipeline == null) return;

        var frame = event.getLevelRenderState();
        var lights = frame.getRenderData(LIGHTS);
        if (lights == null || lights.isEmpty()) return;

        CameraRenderState camera = frame.cameraRenderState;
        var device = RenderSystem.getDevice();
        var target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var data = stack.calloc(UNIFORM_BYTES);
            new Matrix4f(camera.viewRotationMatrix).invert().get(0, data);
            new Matrix4f(camera.projectionMatrix).invert().get(4 * VEC4_BYTES, data);
            data.putInt(8 * VEC4_BYTES, lights.size());
            data.putInt(8 * VEC4_BYTES + Integer.BYTES, device.getDeviceInfo().isZZeroToOne() ? 1 : 0);
            for (int i = 0; i < lights.size(); i++) {
                Light light = lights.get(i);
                Direction aim = light.aim();
                Vec3 lens = light.lens().subtract(camera.pos);
                var position = camera.viewRotationMatrix.transformPosition(lens.toVector3f());
                var direction = camera.viewRotationMatrix.transformDirection(
                        new Vector3f(aim.getStepX() * HORIZONTAL_AIM, VERTICAL_AIM, aim.getStepZ() * HORIZONTAL_AIM));
                int positionOffset = (9 + i) * VEC4_BYTES;
                position.get(positionOffset, data);
                data.putFloat(positionOffset + 3 * Float.BYTES, light.strength());
                int directionOffset = (9 + MAX_LIGHTS + i) * VEC4_BYTES;
                direction.get(directionOffset, data);
                data.putFloat(directionOffset + 3 * Float.BYTES, light.occluders().size());
                data.putFloat(WIDTH_SCALE_OFFSET + i * Float.BYTES, light.widthScale());
                for (int j = 0; j < light.occluders().size(); j++) {
                    AABB box = light.occluders().get(j);
                    int offset = (OCCLUDER_OFFSET + 2 * (i * MAX_OCCLUDERS + j)) * VEC4_BYTES;
                    new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ).get(offset, data);
                    new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ).get(offset + VEC4_BYTES, data);
                }
            }

            try (var uniforms = device.createBuffer(() -> "Outdoor uplight uniforms", GpuBuffer.USAGE_UNIFORM, data);
                 var pass = device.createCommandEncoder().createRenderPass(() -> "Outdoor uplight spotlights",
                         target.getColorTextureView(), Optional.empty(), null, OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
                pass.setUniform("SceneDepth", target.getDepthTextureView(),
                        RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                pass.setUniform("Spotlights", uniforms);
                pass.draw(3, 1, 0, 0);
            }
        }
    }

    private record Light(Vec3 lens, Direction aim, float strength, float widthScale, List<AABB> occluders) {}
}
