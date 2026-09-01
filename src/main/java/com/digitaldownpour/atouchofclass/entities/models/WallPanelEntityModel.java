package com.digitaldownpour.atouchofclass.entities.models;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class WallPanelEntityModel extends EntityModel<EntityRenderState> {

        public WallPanelEntityModel(ModelPart root) {
            super(root);
        }

    @Override
    public void setupAnim(EntityRenderState renderState) {
        super.setupAnim(renderState);

        //root.visible = !renderState.isInvisible;
        //renderState.
    }

    public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();

            // Converts from Blockbench units (0-16) to Minecraft model units (1.0F = 1 block)
            // Hence, divide all dimensions and positions by 16.0F

            root.addOrReplaceChild("main_box",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(15 / 16f - 1.0f, 0f, 0f, 1f / 16f, 1f, 1f),
                    PartPose.ZERO);

            root.addOrReplaceChild("frame_left",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(13 / 16f - 1.0f, 0f, 0f, 2f / 16f, 1f, 1f / 16f),
                    PartPose.ZERO);

            root.addOrReplaceChild("frame_right",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(13 / 16f - 1.0f, 0f, 15 / 16f, 2f / 16f, 1f, 1f / 16f),
                    PartPose.ZERO);

            root.addOrReplaceChild("frame_bottom",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(13 / 16f - 1.0f, 0f, 1 / 16f, 2f / 16f, 1f / 16f, 14f / 16f),
                    PartPose.ZERO);

            root.addOrReplaceChild("frame_top",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(13 / 16f - 1.0f, 15 / 16f, 1 / 16f, 2f / 16f, 1f / 16f, 14f / 16f),
                    PartPose.ZERO);

            root.addOrReplaceChild("inner_left",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(14 / 16f - 1.0f, 1 / 16f, 1 / 16f, 1f / 16f, 14f / 16f, 1f / 16f),
                    PartPose.ZERO);

            root.addOrReplaceChild("inner_right",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(14 / 16f - 1.0f, 1 / 16f, 14 / 16f, 1f / 16f, 14f / 16f, 1f / 16f),
                    PartPose.ZERO);

            root.addOrReplaceChild("inner_bottom",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(14 / 16f - 1.0f, 1 / 16f, 2 / 16f, 1f / 16f, 1f / 16f, 12f / 16f),
                    PartPose.ZERO);

            root.addOrReplaceChild("inner_top",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(14 / 16f - 1.0f, 14 / 16f, 2 / 16f, 1f / 16f, 1f / 16f, 12f / 16f),
                    PartPose.ZERO);

            return LayerDefinition.create(mesh, 64, 64);
        }
}
