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
import me.loloed.bot.api.util.Rotation;

public class Pearl extends ScoreAction {
    @Override
    public void onTick() {
        BotInventory inventory = bot.getInventory();
        Slot slot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.ENDER_PEARL), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
        if (!slot.isHotBar()) {
            bot.chain("PearlTowards#preparePearls")
                    .startInventoryAction()
                    .delay(4)
                    .move(slot, Slot.fromHotBar(6))
                    .endInventoryAction()
                    .finish();
            return;
        }

        inventory.setSelectedSlot(slot.getHotBarIndex());
        Entity target = EntityTarget.TARGET.getValue(state).toRealEntity(bot, state).get();
        Rotation look = MathUtil.getRotationForBow(bot.getHeadPosition(), target.getPosition(), 24);
        bot.setYaw(look.yaw());
        bot.setPitch(look.pitch());
        bot.interact();
    }

    @Override
    public double getScore() {
        StateKey<StateEntity> target = EntityTarget.TARGET;
        Entity realTarget = target.getDefaultValue().toRealEntity(bot, state).get();
        double distanceSqrt = realTarget.getPosition().distanceSquared(bot.getPosition());

        double score = 0.0;
        score += distanceSqrt / 32;
        return score;
    }
}
