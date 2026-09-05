package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperChainBlock extends CopperChainBlock implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperChainBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperChainBlock::getAge),
                    propertiesCodec()).apply(instance, WeatheringCopperChainBlock::new));
    private final WeatherState age;
    public WeatheringCopperChainBlock(WeatherState age, Properties properties) { super(properties); this.age = age; }
    @Override public MapCodec<WeatheringCopperChainBlock> codec() { return CODEC; }
    @Override public WeatherState getAge() { return age; }
    @Override protected boolean isRandomlyTicking(BlockState state) { return WeatheringCopper.getNext(this).isPresent(); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { changeOverTime(state, level, pos, random); }
}
