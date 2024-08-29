package blade.impl.action.attack;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.*;

public class HitEnemy extends BladeAction implements Attack {
    public Slot getSwordSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(ItemTags.SWORDS), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot swordSlot = getSwordSlot();
        if (swordSlot != null) {
            bot.getInventory().setSelectedSlot(swordSlot.hotbarIndex());
        }
        lookAtEnemy(bot, tick);
        bot.setMoveForward(true);
        bot.setMoveBackward(false);
        bot.setSprint(!AttackUtil.canCritIgnoreSprint(bot.getVanillaPlayer()));
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        if (bot.getCrossHairTarget().getType() == HitResult.Type.ENTITY) {
            bot.attack();
            state.setValue(StateKeys.RECENTLY_HIT_ENEMY, 1);
        }
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
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

        return 0 +
                (distSq > reach * reach ? -8 : 1 - (distSq / (reach * reach)) * 2) +
                (attackStrength < 0.4f ? -8 : attackStrength > 0.9f ? 2.0f : attackStrength * 4 - 3) +
                AttackUtil.isAttacking(player, target);
    }
}
