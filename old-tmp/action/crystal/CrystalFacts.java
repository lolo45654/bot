package me.loloed.bot.api.blade.impl.action.crystal;

import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.blade.state.value.AtomicValue;
import me.loloed.bot.api.blade.impl.util.HitCrystalPosition;

public class CrystalFacts {
    public static final ProducingKey<HitCrystalPosition> FIXED_HIT_CRYSTAL = StateKey.producingKey("CrystalsFacts#FIXED_HIT_CRYSTAL", HitCrystalPosition.class)
            .setProducer((bot, state) -> new AtomicValue<>(EntityTarget.TARGET.getValue(state).getPosition().get(HitCrystalPosition.HIT_CRYSTAL_POSITION).getValue(bot, state)));
}
