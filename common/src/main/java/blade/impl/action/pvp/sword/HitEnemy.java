package blade.impl.action.pvp.sword;

import blade.impl.ConfigKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import blade.util.BotMath;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class HitEnemy extends ScoreAction implements Sword {
    public Slot getSwordSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(ItemTags.SWORDS), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot swordSlot = getSwordSlot();
        if (swordSlot != null) {
            bot.getInventory().setSelectedSlot(swordSlot.getHotBarIndex());
        }
        lookAtEnemy(bot, tick);
        bot.setMoveForward(true);
        bot.setMoveBackward(false);
        bot.setSprint(!AttackUtil.canCritIgnoreSprint(bot.getVanillaPlayer()));
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        if (bot.getCrossHairTarget() instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == target) {
            bot.attack();
        }
    }

    @Override
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Player player = bot.getVanillaPlayer();
        Vec3 eyePos = player.getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        float attackStrength = player.getAttackStrengthScale(0.5f);
        double reach = getReach(bot);

        return getSwordScore(bot) +
                (distSq > reach * reach ? -8 : (Math.min(distSq, 6))) +
                (attackStrength < 0.4f ? -8 : attackStrength > 0.9f ? 5.0f : attackStrength * 6 - 4) +
                (getSwordSlot() == null ? -4 : 0);
    }
}
