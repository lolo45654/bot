package me.loloed.bot.api.blade.impl.action.totem;

import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.impl.StateInventory;
import me.loloed.bot.api.blade.state.value.CustomComparingValue;
import me.loloed.bot.api.blade.state.value.MoreThanValue;
import me.loloed.bot.api.blade.state.value.SuppliedValue;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.material.MaterialFlag;

import java.util.Objects;

public abstract class HoldTotem extends AStarAction {
    protected HoldTotem() {
    }

    protected abstract void prepareExpectation();

    @Override
    public void prepare() {
        StateInventory inventory = expectations.getInventory();
        ProducingKey<Integer> selectedSlot = inventory.getSelectedSlot();
        ProducingKey<Boolean> hasTotemHotBar = inventory.hasItem(Slot.fromHotBar(InvLayout.TOTEM_SLOT.getValue(state)), MaterialFlag.RESURRECTING);
        expectations.set(hasTotemHotBar, true);
        expectations.set(selectedSlot, new CustomComparingValue<>(1, that -> {
            Integer totemSlot = InvLayout.TOTEM_SLOT.getValue(state);
            return !Objects.equals(that.getValue(), totemSlot);
        }));
        prepareExpectation();

        results.set(selectedSlot, new SuppliedValue<>(() -> InvLayout.TOTEM_SLOT.getValue(state)));
    }

    @Override
    public void onTick() {
        bot.getInventory().setSelectedSlot(InvLayout.TOTEM_SLOT.getValue(state));
    }

    @Override
    public double getCost() {
        return 2;
    }

    public static class GetHit extends HoldTotem {
        @Override
        protected void prepareExpectation() {
            StateEntity target = expectations.getBot();
            ProducingKey<Double> velocityY = target.getVelocity().getY();
            expectations.set(velocityY, new MoreThanValue<>(0.04));
        }
    }
}
