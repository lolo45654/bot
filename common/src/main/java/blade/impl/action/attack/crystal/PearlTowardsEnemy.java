package blade.impl.action.attack.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.utils.BotMath;
import blade.utils.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.isAttackSatisfied;

public class PearlTowardsEnemy extends BladeAction implements Crystal {
    public Slot getPearlSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.ENDER_PEARL), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot pearlSlot = getPearlSlot();
        if (pearlSlot == null) return;
        bot.getInventory().setSelectedSlot(pearlSlot.hotbarIndex());
        float time = ConfigKeys.getDifficultyReversed(bot) * 0.3f;
        Vec3 direction = bot.getBlade().get(ConfigKeys.TARGET).position().subtract(bot.getVanillaPlayer().getEyePosition());
        if (tick < time) {
            bot.setRotationTarget(BotMath.getYaw(direction), BotMath.getPitch(direction), bot.getBlade().get(ConfigKeys.DIFFICULTY) * 700);
        }
        if (tick >= time) {
            bot.interact();
        }
    }

    @Override
    public boolean isSatisfied() {
        return isAttackSatisfied(bot) &&
                !bot.getVanillaPlayer().getCooldowns().isOnCooldown(Items.ENDER_PEARL) &&
                getPearlSlot() != null;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double distSq = target.distanceToSqr(bot.getVanillaPlayer());

        return Math.min(Math.max(distSq - 8 * 8, 0) / (6 * 6), 5);
    }
}
