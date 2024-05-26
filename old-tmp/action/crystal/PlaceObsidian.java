package me.loloed.bot.api.blade.impl.action.crystal;

import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.entity.Entity;
import me.loloed.bot.api.material.MaterialFlag;
import me.loloed.bot.api.util.Vector;
import me.loloed.bot.api.blade.impl.util.HitCrystalPosition;

public class PlaceObsidian extends ScoreAction {
    @Override
    public void onTick() {

    }

    @Override
    public double getScore() {
        StateKey<StateEntity> target = EntityTarget.TARGET;
        Entity realTarget = target.getDefaultValue().toRealEntity(bot, state).get();
        HitCrystalPosition hitCrystal = CrystalFacts.FIXED_HIT_CRYSTAL.getValue(bot, state);
        double confidence = hitCrystal.confidence();
        Vector obsidian = hitCrystal.position();
        double velY = realTarget.getVelocity().getY();
        boolean isObsidian = bot.getWorld().getBlock(obsidian).hasFlag(MaterialFlag.CRYSTAL_PLACEABLE);

        double score = 0.0;
        score += confidence * confidence / 2;
        score += velY * velY * 400;
        score += isObsidian ? -8.0 : 8.0;
        return score;
    }
}
