package blade.impl.util;

import blade.util.ClientSimulator;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AttackUtil {
    public static double isAttacking(LivingEntity attacker, LivingEntity victim) {
        double score = 0.0;
        score += canCrit(attacker) ? 1.0 : 0.0;
        score += canAttack(attacker, victim) ? 2.0 : 0.0;
        score += attacker instanceof Player player ? player.getAttackStrengthScale(0.5f) : 0;
        return score / 3.5;
    }

    public static boolean canCritIgnoreSprint(LivingEntity entity) {
        return (!(entity instanceof Player player) || player.getAttackStrengthScale(0.5f) > 0.9) && !entity.onGround() && !entity.onClimbable() && !entity.isInWater() && !entity.hasEffect(MobEffects.BLINDNESS) && !entity.isPassenger();
    }

    public static boolean canCrit(LivingEntity entity) {
        return canCritIgnoreSprint(entity) && !entity.isSprinting();
    }

    public static boolean canAttack(LivingEntity attacker, LivingEntity victim) {
        HitResult target = ClientSimulator.findCrosshairTarget(attacker, getEntityInteractionRange(attacker), getBlockInteractionRange(attacker), 1.0f);
        return target.getType() == HitResult.Type.ENTITY && target instanceof EntityHitResult clip && clip.getEntity() == victim;
    }

    public static double getEntityInteractionRange(LivingEntity entity) {
        return entity instanceof Player player ? player.entityInteractionRange() : 3.0;
    }

    public static double getBlockInteractionRange(LivingEntity entity) {
        return entity instanceof Player player ? player.blockInteractionRange() : 4.5;
    }
}
