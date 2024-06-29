package blade.impl.action.pvp.sword;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.ConfigKeys;
import blade.impl.action.pvp.PvP;
import blade.inventory.BotInventory;
import net.minecraft.world.item.Items;

public interface Sword extends PvP {
    static void register(BladeMachine blade) {
        blade.addAction(new HitEnemy());
        blade.addAction(new STap());
        blade.addAction(new Jump());
        blade.addAction(new MoveClose());
        blade.addAction(new UseHealing());
        blade.addAction(new StrafeRight());
        blade.addAction(new StrafeLeft());
    }

    default double getSwordScore(Bot bot) {
        BotInventory inv = bot.getInventory();
        double score = 0.0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_HELMET)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_CHESTPLATE)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_LEGGINGS)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_BOOTS)) != null ? 0.5 : 0;
        score += inv.findFirst(stack -> stack.is(Items.DIAMOND_SWORD)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.GOLDEN_APPLE)) != null ? 1 : 0;
        return score / 2;
    }

    default double getReach(Bot bot) {
        return bot.getVanillaPlayer().entityInteractionRange() - ConfigKeys.getDifficultyReversedCubic(bot) / 2;
    }
}
