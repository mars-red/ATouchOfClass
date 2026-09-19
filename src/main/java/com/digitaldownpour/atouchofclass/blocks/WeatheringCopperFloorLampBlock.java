package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperFloorLampBlock extends CopperFloorLampBlock implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperFloorLampBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(WeatherState.CODEC.fieldOf("weathering_state")
                            .forGetter(WeatheringCopperFloorLampBlock::getAge),
                    propertiesCodec()).apply(instance, WeatheringCopperFloorLampBlock::new));

    private final WeatherState age;

    public WeatheringCopperFloorLampBlock(WeatherState age, Properties properties) {
        super(properties);
        this.age = age;
    }

    @Override
    protected MapCodec<WeatheringCopperFloorLampBlock> codec() {
        return CODEC;
    }

    @Override
    public WeatherState getAge() {
        return age;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(PART) == 0 && WeatheringCopper.getNext(this).isPresent();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        changeOverTime(state, level, pos, random);
    }
}
