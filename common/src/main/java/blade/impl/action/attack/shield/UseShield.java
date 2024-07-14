package blade.impl.action.attack.shield;

import blade.impl.ConfigKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import static blade.impl.action.attack.Attack.isPvPSatisfied;
import static blade.impl.action.attack.Attack.lookAtEnemy;
import static blade.impl.action.attack.shield.Shield.getShieldSlot;

public class UseShield extends BladeAction implements Shield {
    public Slot getTotemSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot shieldSlot = getShieldSlot(bot);
        if (shieldSlot == null) return;
        Slot totemSlot = getTotemSlot();
        if (shieldSlot.isOffHand() && totemSlot != null) {
            bot.getInventory().setSelectedSlot(totemSlot.getHotbarIndex());
        } else if (shieldSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(shieldSlot.getHotbarIndex());
        } else {
            bot.getInventory().openInventory();
            bot.getInventory().move(shieldSlot, Slot.ofOffhand());
            return;
        }
        bot.getInventory().closeInventory();
        lookAtEnemy(bot, tick);
        bot.interact(true);
        bot.setSprint(false);
        bot.setMoveBackward(false);
        bot.setMoveForward(false);
        bot.setMoveRight(false);
        bot.setMoveLeft(false);
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

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Player player = bot.getVanillaPlayer();
        return (getShieldSlot(bot) == null ? -12 : 0) +
                (player.isUsingItem() && player.getUseItem().is(Items.SHIELD) ? Math.max(Math.max(tick - 12, 0) * -0.3, -1) : 0) +
                (player.getCooldowns().isOnCooldown(Items.SHIELD) ? -12 : 0) +
                AttackUtil.isAttacking(target, player) * 2.0;
    }
}
