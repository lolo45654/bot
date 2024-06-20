package me.loloed.bot.api.blade.impl.action.pvp.sword;

import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.util.BotMath;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HitEnemy extends ScoreAction implements Sword {
    public Slot getSwordSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(ItemTags.SWORDS), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        float time = ConfigKeys.getDifficultyReversed(bot) * 0.6f;
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 closestPoint = BotMath.getClosestPoint(bot.getVanillaPlayer().getEyePosition(), target.getBoundingBox());
        Vec3 direction = closestPoint.subtract(bot.getVanillaPlayer().position());
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        if (tick < time) {
            bot.lookRealistic(yaw, pitch, tick / (float) time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
        }
        if (tick >= time) {
            bot.attack();
        }
    }

    @Override
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 closestPoint = BotMath.getClosestPoint(bot.getVanillaPlayer().getEyePosition(), target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(bot.getVanillaPlayer().getEyePosition());
        return getSwordScore(bot) +
                (distSq > 3 * 3 ? -8 : (Math.min(distSq / 2, 4))) +
                (getSwordSlot() == null ? -4 : 0);
    }
}
