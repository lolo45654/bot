package blade.bot;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;

public class ServerBotSettings implements Cloneable {
    public static final ServerBotSettings TOTEM = new ServerBotSettings();

    public static final ServerBotSettings SHIELD = new ServerBotSettings() {{
        shield = true;
        // autoHit = true;
    }};

    public Armor armor = Armor.NETHERITE;
    public boolean blastProtection = true;
    public boolean autoHealing = true;

    public boolean shield = false;
    public boolean autoHit = false;

    public float reach = 3.0f;

    @Override
    public ServerBotSettings clone() {
        try {
            return (ServerBotSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static enum Armor {
        DIAMOND(Map.of(EquipmentSlot.HEAD, Items.DIAMOND_HELMET, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS, EquipmentSlot.FEET, Items.DIAMOND_BOOTS)),
        NETHERITE(Map.of(EquipmentSlot.HEAD, Items.NETHERITE_HELMET, EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE, EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS, EquipmentSlot.FEET, Items.NETHERITE_BOOTS))

        ;

        public final Map<EquipmentSlot, Item> itemTypes;

        Armor(Map<EquipmentSlot, Item> itemTypes) {
            this.itemTypes = itemTypes;
        }
    }
}
