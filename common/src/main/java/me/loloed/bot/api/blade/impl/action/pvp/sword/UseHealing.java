package me.loloed.bot.api.blade.impl.action.pvp.sword;

import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.util.BotMath;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class UseHealing extends ScoreAction implements Sword {
    public Slot getSwordSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(ItemTags.SWORDS), SlotFlag.HOT_BAR);
    }

    public Slot getHealingSlot() {
        return bot.getInventory().getBestFood(FoodProperties::canAlwaysEat, SlotFlag.OFF_HAND, SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot healingSlot = getHealingSlot();
        if (healingSlot == null) return;
        Slot swordSlot = getSwordSlot();
        if (healingSlot.isOffHand() && swordSlot != null) {
            bot.getInventory().setSelectedSlot(swordSlot.getHotBarIndex());
        } else if (healingSlot.isHotBar()) {
            bot.getInventory().setSelectedSlot(healingSlot.getHotBarIndex());
        }
        lookAtEnemy(bot, tick);
        bot.setMoveForward(false);
        bot.setMoveBackward(false);
        bot.setSprint(false);
        bot.interact(true);
        if (bot.getVanillaPlayer().getUseItemRemainingTicks() <= 0) {
            bot.interact(false);
        }
    }

    @Override
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        float attackStrength = bot.getVanillaPlayer().getAttackStrengthScale(0.5f);
        float ourHealthRatio = bot.getVanillaPlayer().getHealth() / bot.getVanillaPlayer().getMaxHealth();
        float targetHealthRatio = target.getHealth() / target.getMaxHealth();
        return getSwordScore(bot) +
                Math.min(distSq / 6, 6) +
                ((-bot.getVanillaPlayer().getUseItemRemainingTicks() + 16) / 8.0) +
                ((targetHealthRatio - ourHealthRatio) * 4 + 1.0f) +
                (getHealingSlot() == null ? -12 : 0);
    }
}
