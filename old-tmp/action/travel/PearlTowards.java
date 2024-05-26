package me.loloed.bot.api.blade.impl.action.travel;

import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.value.AdaptingValue;
import me.loloed.bot.api.blade.state.value.LessThanValue;
import me.loloed.bot.api.entity.Entity;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.material.MaterialFlag;
import me.loloed.bot.api.util.MathUtil;
import me.loloed.bot.api.util.Rotation;

public class PearlTowards extends AStarAction {
    // TODO: is weird

    @Override
    public void prepare() {
        ProducingKey<Boolean> hasPearl = expectations.getInventory().hasItem(MaterialFlag.ENDER_PEARL);
        expectations.set(hasPearl, true);
        expectProduced(hasPearl);

        ProducingKey<Double> distanceSquared = EntityTarget.TARGET.getDefaultValue().getPosition().distanceSquared(results.getBot().getPosition());
        results.set(distanceSquared, new AdaptingValue<>(0.0, that -> {
            if (!(that instanceof LessThanValue<Double> lessThan)) return 0.0;
            Double val = lessThan.getValue();
            return distanceSquared.getOptionalValue(state).orElse(0.0) > val ? val - 1.0 : val + 1.0;
        }));
        expectProduced(distanceSquared);
    }

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
        StateEntity target = EntityTarget.TARGET.get(state).getValue();
        Entity entity = target.toRealEntity(bot, state).get();
        Rotation look = MathUtil.getRotationForBow(bot.getHeadPosition(), entity.getPosition(), 22);
        bot.setYaw(look.yaw());
        bot.setPitch(look.pitch());
        bot.interact();
    }

    @Override
    public double getCost() {
        return 5.0;
    }
}
