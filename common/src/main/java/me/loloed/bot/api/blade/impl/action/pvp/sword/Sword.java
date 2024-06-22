package me.loloed.bot.api.blade.impl.action.pvp.sword;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public interface Sword {
    static void register(BladeMachine blade) {
        blade.addAction(new HitEnemy());
        blade.addAction(new STap());
        blade.addAction(new Jump());
        blade.addAction(new MoveClose());
        blade.addAction(new UseHealing());
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

    default void lookAtEnemy(Bot bot, int tick) {
        float time = (float) (ConfigKeys.getDifficultyReversed(bot) * 0.6);
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        Vec3 direction = closestPoint.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
    }
}
