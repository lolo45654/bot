package blade.impl.action.attack;

import blade.debug.visual.VisualBox;
import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.utils.BotMath;
import blade.utils.ClipUtils;
import blade.utils.blade.BladeAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.*;

public class HitEnemy extends BladeAction implements Attack {
    public Slot getWeaponSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Slot swordSlot = getWeaponSlot();
        if (swordSlot != null) {
            bot.getInventory().setSelectedSlot(swordSlot.hotbarIndex());
        }
        lookAtEnemy(bot, tick);
        bot.setMoveForward(true);
        bot.setMoveBackward(false);
        bot.setSprint(!AttackUtil.canCritIgnoreSprint(bot.getVanillaPlayer()));
        if (bot.getCrossHairTarget() instanceof EntityHitResult entityHitResult) {
            bot.attack();
            state.setValue(StateKeys.RECENTLY_HIT_ENEMY, 1);
            bot.getBlade().addVisualDebug(new VisualBox(entityHitResult.getEntity().getBoundingBox().deflate(0.2), 0.6f, 0.2f, 0.2f));
        } else {
            bot.getBlade().addVisualDebug(new VisualBox(target.getBoundingBox().deflate(0.2), 0.2f, 0.2f, 0.6f));
        }
    }

    @Override
    public boolean isSatisfied() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Player player = bot.getVanillaPlayer();
        Vec3 eyePos = player.getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        double reach = getReach(bot);

        return isAttackSatisfied(bot) && distSq < reach * reach && ClipUtils.hasLineOfSight(player.level(), eyePos, closestPoint);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
        double recentlyHitEnemy = result.getValue(StateKeys.RECENTLY_HIT_ENEMY);
        if (recentlyHitEnemy > 0) {
            result.setValue(StateKeys.RECENTLY_HIT_ENEMY, recentlyHitEnemy < 0.03 ? 0.0 : recentlyHitEnemy * 0.9);
        }
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
        Slot swordSlot = getWeaponSlot();

        return (1 - (distSq / (reach * reach)) * 2) +
                (attackStrength < 0.4f ? -2 : attackStrength > 0.9f ? 2.0f : attackStrength * 4 - 3) /
                        (swordSlot == null || (swordSlot.isHotbar() && bot.getInventory().getSelectedSlot() == swordSlot.hotbarIndex()) ? 1 : 3) +
                AttackUtil.isAttacking(player, target);
    }
}
