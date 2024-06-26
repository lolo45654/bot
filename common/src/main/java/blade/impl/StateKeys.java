package blade.impl;

import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.state.StateKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class StateKeys {
    public static final StateKey BOT_HEALTH = StateKey.key("bot_health", bot -> {
        Player player = bot.getVanillaPlayer();
        return player.getHealth() / player.getMaxHealth();
    });

    public static final StateKey TARGET_HEALTH = StateKey.key("target_health", bot -> {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        return target.getHealth() / target.getMaxHealth();
    });

    public static final StateKey HAS_TARGET = StateKey.key("has_target", bot -> bot.getBlade().get(ConfigKeys.TARGET) == null ? 0 : 1);

    public static final StateKey TARGET_DISTANCE_SQRT = StateKey.key("target_distance_squared", bot -> bot.getBlade().get(ConfigKeys.TARGET).distanceToSqr(bot.getVanillaPlayer()));

    public static final StateKey HAS_OBSIDIAN = StateKey.key("has_obsidian", bot -> bot.getInventory().findFirst(stack -> stack.is(Items.OBSIDIAN)) != null ? 1 : 0);

    public static final StateKey DOUBLE_HAND_TOTEM = StateKey.key("double_hand", bot -> {
        BotInventory inv = bot.getInventory();
        return inv.getItem(Slot.fromHotBar(inv.getSelectedSlot())).is(Items.TOTEM_OF_UNDYING) && inv.getOffHand().is(Items.TOTEM_OF_UNDYING) ? 1 : 0;
    });

    public static final StateKey OFF_HAND_TOTEM = StateKey.key("off_hand_totem", bot -> bot.getInventory().getOffHand().is(Items.TOTEM_OF_UNDYING) ? 1 : 0);
}
