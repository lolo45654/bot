package blade.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ItemUtil {
    public static Holder<Enchantment> getEnchantment(ResourceKey<Enchantment> key, Level world) {
        Optional<Registry<Enchantment>> enchantmentRegistry = world.registryAccess().registry(Registries.ENCHANTMENT);
        return enchantmentRegistry.map(enchantments -> enchantments.getHolderOrThrow(key)).orElse(null);
    }

    /**
     * incomplete
     */
    public static boolean isUsingItem(ItemStack stack) {
        if (stack.get(DataComponents.FOOD) != null) return true;
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.ENDER_PEARL);
    }
  
    public static String getSlotName(EquipmentSlot slot) {
        return switch (slot) {
            case BODY -> "Armadillo Armor";
            case HEAD -> "Helmet";
            case CHEST -> "Chestplate";
            case LEGS -> "Leggings";
            case FEET -> "Boots";
            case MAINHAND -> "Mainhand";
            case OFFHAND -> "Offhand";
        };
    }
}
