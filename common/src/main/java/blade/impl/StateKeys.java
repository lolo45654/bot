package blade.impl;

import blade.debug.DebugFrame;
import blade.debug.planner.ScorePlannerDebug;
import blade.impl.action.attack.Attack;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.planner.score.StateKey;
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
        if (target == null) return 0;
        return target.getHealth() / target.getMaxHealth();
    });

    public static final StateKey HAS_TARGET = StateKey.key("has_target", bot -> bot.getBlade().get(ConfigKeys.TARGET) == null ? 0 : 1);

    public static final StateKey TARGET_DISTANCE_SQRT = StateKey.key("target_distance_squared", bot -> bot.getBlade().get(ConfigKeys.TARGET).distanceToSqr(bot.getVanillaPlayer()));

    public static final StateKey HAS_OBSIDIAN = StateKey.key("has_obsidian", bot -> bot.getInventory().findFirst(stack -> stack.is(Items.OBSIDIAN)) != null ? 1 : 0);

    public static final StateKey DOUBLE_HAND_TOTEM = StateKey.key("double_hand", bot -> {
        BotInventory inv = bot.getInventory();
        return inv.getItem(Slot.ofHotbar(inv.getSelectedSlot())).is(Items.TOTEM_OF_UNDYING) && inv.getOffHand().is(Items.TOTEM_OF_UNDYING) ? 1 : 0;
    });

    public static final StateKey OFF_HAND_TOTEM = StateKey.key("off_hand_totem", bot -> bot.getInventory().getOffHand().is(Items.TOTEM_OF_UNDYING) ? 1 : 0);

    public static final StateKey DOING_PVP = StateKey.key("doing_pvp", bot -> {
        DebugFrame frame = bot.getBlade().getLastFrame();
        if (frame == null) return 0;
        ScorePlannerDebug planner = frame.planner();
        if (planner == null) return 0;
        return planner.action() instanceof Attack ? 1.0 : 0;
    });

    public static final StateKey IS_HEALING = StateKey.key("is_healing", bot -> 0);

    public static final StateKey RECENTLY_HIT_ENEMY = StateKey.key("recently_hit_enemy", bot -> 0);

    public static final StateKey CRYSTAL_MODE = StateKey.key("crystal_mode", bot -> {
        BotInventory inv = bot.getInventory();
        double score = 0.0;
        score += inv.findFirst(stack -> stack.is(Items.END_CRYSTAL)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.OBSIDIAN)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.RESPAWN_ANCHOR)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.GLOWSTONE)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.ENDER_PEARL)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING)) != null ? 1 : 0;
        return score / 3;
    });

    public static final StateKey SWORD_MODE = StateKey.key("sword_mode", bot -> {
        BotInventory inv = bot.getInventory();
        double score = 0.0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_HELMET)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_CHESTPLATE)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_LEGGINGS)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_BOOTS)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_SWORD)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.GOLDEN_APPLE)) != null ? 1 : 0;
        return score / 4;
    });
}
