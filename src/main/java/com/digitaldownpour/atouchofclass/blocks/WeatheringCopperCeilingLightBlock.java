package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperCeilingLightBlock extends CopperCeilingLightBlock implements WeatheringCopper {

    private final WeatherState age;

    public WeatheringCopperCeilingLightBlock(WeatherState age, boolean coffered, Properties properties) {
        super(coffered, properties);
        this.age = age;
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
