package me.loloed.bot.api.blade.impl.action.pvp.crystal;

import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

public class PearlTowardsEnemy extends ScoreAction implements Crystal {
    public Slot getPearlSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.ENDER_PEARL), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot pearlSlot = getPearlSlot();
        if (pearlSlot == null) return;
        bot.getInventory().setSelectedSlot(pearlSlot.getHotBarIndex());
        double time = ConfigKeys.getDifficultyReversed(bot) * 3;
        float[] bow = BotMath.getRotationForBow(bot.getVanillaPlayer().position(), bot.getBlade().get(ConfigKeys.TARGET).position(), 24);
        if (tick < time) {
            bot.lookRealistic(bow[0], bow[1], tick / (float) time, 1f);
        }
        if (tick >= time) {
            bot.interact();
        }
    }

    @Override
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double distSq = target.distanceToSqr(bot.getVanillaPlayer());
        return getCrystalScore(bot) +
                Math.min(distSq / 96, 4) +
                (getPearlSlot() == null ? -8 : 0) +
                (bot.getVanillaPlayer().getCooldowns().isOnCooldown(Items.ENDER_PEARL) ? -4 : 0);
    }
}
