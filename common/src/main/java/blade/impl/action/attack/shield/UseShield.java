package blade.impl.action.attack.shield;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.ItemUtil;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import static blade.impl.action.attack.Attack.isPvPSatisfied;
import static blade.impl.action.attack.Attack.lookAtEnemy;
import static blade.impl.action.attack.shield.Shield.getShieldSlot;

public class UseShield extends BladeAction implements Shield {
    public Slot getNonUsingSlot() {
        BotInventory inv = bot.getInventory();
        Slot selectedSlot = Slot.ofHotbar(inv.getSelectedSlot());
        if (!ItemUtil.isUsingItem(inv.getItem(selectedSlot))) return selectedSlot;
        return inv.findFirst(stack -> !ItemUtil.isUsingItem(stack), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot shieldSlot = getShieldSlot(bot);
        if (shieldSlot == null) return;
        Slot nonUsingSlot = getNonUsingSlot();
        if (shieldSlot.isOffHand() && nonUsingSlot != null) {
            bot.getInventory().setSelectedSlot(nonUsingSlot.hotbarIndex());
        } else if (shieldSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(shieldSlot.hotbarIndex());
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
        return isPvPSatisfied(bot) && getShieldSlot(bot) != null && !bot.getVanillaPlayer().getCooldowns().isOnCooldown(Items.SHIELD);
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.interact(false);
        bot.getInventory().closeInventory();
    }

    @Override
    public void getResult(ScoreState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Player player = bot.getVanillaPlayer();
        return (player.isUsingItem() && player.getUseItem().is(Items.SHIELD) ? Math.max(Math.max(tick - 12, 0) * -0.3, -1) : 0) +
                AttackUtil.isAttacking(target, player) * 2.0
                - state.getValue(StateKeys.CRYSTAL_MODE);
    }
}
