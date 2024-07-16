package blade.impl.action.attack;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static blade.impl.action.attack.Attack.isPvPSatisfied;
import static blade.impl.action.attack.Attack.lookAtEnemy;

public class ConsumeHealingPotion extends BladeAction implements Attack {
    private final Holder<MobEffect> effect;
    private final double baseScore;

    public ConsumeHealingPotion(Holder<MobEffect> effect, double baseScore) {
        this.effect = effect;
        this.baseScore = baseScore;
    }

    public Slot getPotionSlot() {
        return bot.getInventory().findFirst(stack -> {
            if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION)) return false;
            PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null) return false;
            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                if (effect.is(this.effect)) return true;
            }
            return false;
        }, SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
    }

    public boolean hasThrownPotion() {
        List<ThrownPotion> thrownPotions = bot.getVanillaPlayer().level().getEntitiesOfClass(ThrownPotion.class, bot.getVanillaPlayer().getBoundingBox().inflate(2));
        for (ThrownPotion thrownPotion : thrownPotions) {
            ItemStack potionItem = thrownPotion.getItem();
            PotionContents potionContents = potionItem.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null) continue;
            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                if (effect.is(this.effect)) return true;
            }
        }
        return false;
    }

    @Override
    public void onTick() {
        BotInventory inventory = bot.getInventory();
        Slot potionSlot = getPotionSlot();
        if (potionSlot == null) return;
        if (!potionSlot.isHotbar()) {
            inventory.openInventory();
            inventory.move(potionSlot, Slot.ofHotbar(2));
            return;
        }

        inventory.closeInventory();
        ItemStack potionStack = inventory.getItem(potionSlot);
        inventory.setSelectedSlot(potionSlot.getHotbarIndex());
        
        if (potionStack.is(Items.SPLASH_POTION)) {
            float time = ConfigKeys.getDifficultyReversedCubic(bot) * 1.2f;
            bot.lookRealistic(0.0f, 90.0f, tick / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
            if (bot.getVanillaPlayer().getXRot() < 80.0f) return;

            bot.setSneak(true);
            bot.setMoveLeft(false);
            bot.setMoveRight(false);
            bot.setMoveForward(false);
            bot.setMoveBackward(false);
            bot.interact();
        } else if (potionStack.is(Items.POTION)) {
            lookAtEnemy(bot, tick);
            bot.interact(true);
        }
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
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

        return 1 +
                ((targetHealthRatio - ourHealthRatio) * 3 - bot.getBlade().get(ConfigKeys.DIFFICULTY)) +
                (getPotionSlot() == null ? -12 : 0);
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.getInventory().closeInventory();
        bot.setSneak(false);
        bot.interact(false);
    }

    @Override
    public String toString() {
        String effectStr = effect.unwrapKey().isPresent() ? effect.unwrapKey().get().location().getPath() : "empty";
        return String.format("consume_potion[effect=%s]", effectStr);
    }
}
