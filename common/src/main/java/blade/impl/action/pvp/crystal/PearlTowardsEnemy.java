package blade.impl.action.pvp.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

public class PearlTowardsEnemy extends BladeAction implements Crystal {
    public Slot getPearlSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.ENDER_PEARL), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot pearlSlot = getPearlSlot();
        if (pearlSlot == null) return;
        bot.getInventory().setSelectedSlot(pearlSlot.getHotBarIndex());
        float time = ConfigKeys.getDifficultyReversed(bot) * 0.3f;
        float[] bow = BotMath.getRotationForBow(bot.getVanillaPlayer().getEyePosition(), bot.getBlade().get(ConfigKeys.TARGET).position(), 24);
        if (tick < time) {
            bot.lookRealistic(bow[0], bow[1], tick / time, 0.3f);
        }
        if (tick >= time) {
            bot.interact();
        }
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double distSq = target.distanceToSqr(bot.getVanillaPlayer());
        double distScore = Math.min(Math.max(distSq - 14 * 14, 0) / (14 * 14), 3);
        return distScore +
                (getPearlSlot() == null ? -8 : 0) +
                (bot.getVanillaPlayer().getCooldowns().isOnCooldown(Items.ENDER_PEARL) ? -4 : 0);
    }
}
