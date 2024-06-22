package me.loloed.bot.api.blade.impl.action.pvp.totem;

import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.impl.StateKeys;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import net.minecraft.world.item.Items;

public class EquipHotBarTotem extends ScoreAction implements Totem {
    @Override
    public void onTick() {
        BotInventory inv = bot.getInventory();
        Slot inventorySlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.MAIN, SlotFlag.ARMOR);
        if (inventorySlot == null) return;
        if (tick == 0) {
            inv.startAction();
        }
        if (tick >= ConfigKeys.getDifficultyReversed(bot) * 3) {
            inv.move(inventorySlot, Slot.fromHotBar(1), true);
        }
    }

    @Override
    public void getResult(BladeState result) {
        result.setValue(StateKeys.DOUBLE_HAND_TOTEM, 1);
    }

    @Override
    public double getScore() {
        BotInventory inv = bot.getInventory();
        Slot hotBarSlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
        return getTotemScore(bot) +
                (hotBarSlot == null ? 1.2 : -10);
    }

    @Override
    public void onRelease(ScoreAction next) {
        super.onRelease(next);
        bot.getInventory().endAction();
    }
}
