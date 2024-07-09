package blade.impl.action.attack.sword;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.action.attack.Attack;
import blade.impl.action.attack.ConsumeHealing;
import blade.impl.action.attack.HitEnemy;
import blade.impl.action.attack.MoveTowardsEnemy;
import blade.inventory.BotInventory;
import net.minecraft.world.item.Items;

public interface Sword extends Attack {
    static void register(BladeMachine blade) {
        blade.addAction(new HitEnemy());
        blade.addAction(new STap());
        blade.addAction(new Jump());
        blade.addAction(new MoveTowardsEnemy());
        blade.addAction(new ConsumeHealing());
        blade.addAction(new StrafeRight());
        blade.addAction(new StrafeLeft());
    }

    static double getSwordScore(Bot bot) {
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
}
