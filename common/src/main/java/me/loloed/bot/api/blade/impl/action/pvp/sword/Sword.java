package me.loloed.bot.api.blade.impl.action.pvp.sword;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.inventory.BotInventory;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public interface Sword {
    static void register(BladeMachine blade) {
        blade.addAction(new HitEnemy());
    }

    default double getSwordScore(Bot bot) {
        BotInventory inv = bot.getInventory();
        double score = 0.0;
        score += inv.findFirst(stack -> stack.is(ItemTags.TRIMMABLE_ARMOR)) != null ? 2 : 0;
        score += inv.findFirst(stack -> stack.is(ItemTags.SWORDS)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.GOLDEN_APPLE)) != null ? 1 : 0;
        return Math.min(score / 4, 1);
    }
}
