package blade.impl.action.attack;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.isPvPSatisfied;
import static blade.impl.action.attack.Attack.lookAtEnemy;

public class ConsumeHealing extends BladeAction implements Attack {
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
            bot.getInventory().setSelectedSlot(swordSlot.getHotbarIndex());
        } else if (healingSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(healingSlot.getHotbarIndex());
        }
        lookAtEnemy(bot, tick);
        bot.setMoveForward(false);
        bot.setMoveBackward(false);
        bot.setSprint(false);
        bot.interact(true);
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.interact(false);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.IS_HEALING, 1.0);
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        float ourHealthRatio = bot.getVanillaPlayer().getHealth() / bot.getVanillaPlayer().getMaxHealth();
        float targetHealthRatio = target.getHealth() / target.getMaxHealth();

        return 0 +
                Math.min((distSq - 3 * 3) / (8 * 8), 1) +
                Math.min(tick / 16, 1) +
                ((targetHealthRatio - ourHealthRatio) * 3 - bot.getBlade().get(ConfigKeys.DIFFICULTY)) +
                (getHealingSlot() == null ? -12 : 0) +
                Math.max(Math.min(20 - bot.getVanillaPlayer().getFoodData().getFoodLevel(), 2) * 2, 0) +
                Math.max(Math.min(20.0f - bot.getVanillaPlayer().getFoodData().getSaturationLevel(), 2.0f), 0.0f) / 3.0f;
    }
}
