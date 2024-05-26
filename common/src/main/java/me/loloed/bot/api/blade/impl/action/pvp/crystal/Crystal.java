package me.loloed.bot.api.blade.impl.action.pvp.crystal;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.inventory.BotInventory;
import net.minecraft.world.item.Items;

public interface Crystal {
    static void register(BladeMachine blade) {
        blade.addAction(new PearlTowardsEnemy());
    }

    default double getCrystalScore(Bot bot) {
        BotInventory inv = bot.getInventory();
        double score = 0.0;
        score += inv.findFirst(stack -> stack.is(Items.END_CRYSTAL)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.OBSIDIAN)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.RESPAWN_ANCHOR)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.GLOWSTONE)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.ENDER_PEARL)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING)) != null ? 1 : 0;
        return Math.min(score / 5, 1);
    }
}
