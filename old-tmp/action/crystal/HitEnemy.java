package me.loloed.bot.api.blade.impl.action.crystal;

import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.entity.Entity;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.material.MaterialFlag;
import me.loloed.bot.api.util.MathUtil;
import me.loloed.bot.api.util.Vector;
import me.loloed.bot.api.blade.impl.util.HitCrystalPosition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HitEnemy extends ScoreAction {
    @Override
    public void onTick() {
        BotInventory inventory = bot.getInventory();
        Slot slot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.KNOCKBACK_ONE), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
        if (slot == null && tick == 0) {
            bot.setSprint(true);
            bot.setMoveForward(true);
            return;
        }
        if (slot != null && slot.isHotBar()) {
            inventory.setSelectedSlot(slot.getHotBarIndex());
        }
        StateEntity stateEntity = EntityTarget.TARGET.getValue(state);
        Optional<Entity> target = stateEntity.toRealEntity(bot, state);
        if (target.isEmpty()) return;
        bot.setSprint(false);
        bot.setMoveForward(false);
        Vector closestPosition = target.get().getBoundingBox().getClosestPosition(bot.getHeadPosition());
        bot.lookAt(closestPosition);
        if (target.get().getHurtTime() == 0) bot.attack();
    }

    @Override
    public double getScore() {
        StateKey<StateEntity> target = EntityTarget.TARGET;
        Entity realTarget = target.getDefaultValue().toRealEntity(bot, state).get();
        HitCrystalPosition hitCrystal = CrystalFacts.FIXED_HIT_CRYSTAL.getValue(bot, state);
        double confidence = hitCrystal.confidence();
        Vector obsidian = hitCrystal.position();

        double score = 0.0;
        score += confidence * confidence / 2;
        score += getScoredHeight(realTarget.getPosition(), obsidian);
        return score;
    }

    public double getScoredHeight(Vector target, Vector obsidian) {
        Map<Double, Double> gradient = new HashMap<>();
        gradient.put(-2.0, -4.0);
        gradient.put(-1.0, 2.0);
        gradient.put(-0.0, -1.0);
        return MathUtil.linearGradient(target.getY() - obsidian.up().getY(), gradient);
    }
}
