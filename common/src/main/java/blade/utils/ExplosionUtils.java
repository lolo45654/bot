package blade.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

public class ExplosionUtils {
    public static float getCrystalDamage(Vec3 center, Entity entity, float seenPercent) {
        float radius2 = 12f;
        double e = (1 - (Math.sqrt(entity.distanceToSqr(center)) / 2)) * seenPercent;
        float calculated = (float) ((e * e + e) / 2.0 * 7.0 * radius2 + 1.0);
        if (entity instanceof LivingEntity livingEntity) {
            calculated -= 2.0f * EnchantmentHelper.getEnchantmentLevel(ItemUtils.getEnchantment(Enchantments.BLAST_PROTECTION, entity.level()), livingEntity);
        }
        return calculated;
    }
}
