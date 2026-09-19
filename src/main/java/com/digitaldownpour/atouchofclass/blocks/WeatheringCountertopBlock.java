package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/** A countertop that advances through the vanilla copper oxidation lifecycle. */
public class WeatheringCountertopBlock extends CountertopBlock implements WeatheringCopper {
    public static final MapCodec<WeatheringCountertopBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCountertopBlock::getAge),
                    propertiesCodec()
            ).apply(instance, WeatheringCountertopBlock::new));

    private final WeatherState age;

    public WeatheringCountertopBlock(WeatherState age, Properties properties) {
        super(properties);
        this.age = age;
    }

    @Override
    protected MapCodec<WeatheringCountertopBlock> codec() {
        return CODEC;
    }

    @Override
    public WeatherState getAge() {
        return age;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(this).isPresent();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        changeOverTime(state, level, pos, random);
    }
}
