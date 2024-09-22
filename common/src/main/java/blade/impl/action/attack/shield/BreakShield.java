package blade.impl.action.attack.shield;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.utils.BotMath;
import blade.utils.blade.BladeAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.*;

public class BreakShield extends BladeAction implements Shield {
    public Slot getAxeSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(ItemTags.AXES), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot axeSlot = getAxeSlot();
        int previousSelected = bot.getInventory().getSelectedSlot();
        if (axeSlot != null) {
            bot.getInventory().setSelectedSlot(axeSlot.hotbarIndex());
        }
        lookAtEnemy(bot, tick);
        bot.setMoveForward(true);
        bot.setMoveBackward(false);
        bot.setSprint(!AttackUtil.canCritIgnoreSprint(bot.getVanillaPlayer()));
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        if (bot.getCrossHairTarget() instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == target) {
            bot.attack();
            bot.getInventory().setSelectedSlot(previousSelected);
        }
    }

    @Override
    public boolean isSatisfied() {
        return isAttackSatisfied(bot) && getAxeSlot() != null;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Player player = bot.getVanillaPlayer();
        Vec3 eyePos = player.getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        double reach = getReach(bot);

        return distSq < reach * reach && target.isBlocking()  ? 4 : -12;
    }
}
