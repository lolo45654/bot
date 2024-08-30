package blade.bot;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerBotSettings implements Cloneable {
    private static final Map<EquipmentSlot, ArmorPiece> NETHERITE_ARMOR = new HashMap<>() {{
        put(EquipmentSlot.HEAD, new ArmorPiece(Items.NETHERITE_HELMET, false));
        put(EquipmentSlot.CHEST, new ArmorPiece(Items.NETHERITE_CHESTPLATE, false));
        put(EquipmentSlot.LEGS, new ArmorPiece(Items.NETHERITE_LEGGINGS, true));
        put(EquipmentSlot.FEET, new ArmorPiece(Items.NETHERITE_BOOTS, false));
    }};

    public static final ServerBotSettings TOTEM = new ServerBotSettings();

    public static final ServerBotSettings SHIELD = new ServerBotSettings() {{
        shield = true;
        // autoHit = true;
    }};

    public boolean autoHealing = true;
    public boolean moveTowardsSpawner = false;

    public Map<EquipmentSlot, ArmorPiece> armor = new HashMap<>(NETHERITE_ARMOR);

    public boolean shield = false;
    public boolean autoHit = false;

    public float reach = 3.0f;

    public Set<Holder<MobEffect>> effects = new HashSet<>();

    @Override
    public ServerBotSettings clone() {
        try {
            return (ServerBotSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public record ArmorPiece(Item base, boolean blastProtection) {
        public ArmorPiece withBlastProtection(boolean v) {
            return new ArmorPiece(base, v);
        }

        public ArmorPiece withBase(Item v) {
            return new ArmorPiece(v, blastProtection);
        }
    }
}
