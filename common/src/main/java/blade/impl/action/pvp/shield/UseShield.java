package blade.impl.action.pvp.shield;

import blade.impl.ConfigKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class UseShield extends ScoreAction implements Shield {
    public Slot getTotemSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot shieldSlot = getShieldSlot(bot);
        if (shieldSlot == null) return;
        Slot totemSlot = getTotemSlot();
        if (shieldSlot.isOffHand() && totemSlot != null) {
            bot.getInventory().setSelectedSlot(totemSlot.getHotBarIndex());
        } else if (shieldSlot.isHotBar()) {
            bot.getInventory().setSelectedSlot(shieldSlot.getHotBarIndex());
        }
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
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Player player = bot.getVanillaPlayer();
        return (getShieldSlot(bot) == null ? -24 : 0) +
                (player.isUsingItem() ? (-player.getUseItemRemainingTicks() + 16) / 2.0 : 0) +
                AttackUtil.isAttacking(target, player);
    }

    @Override
    public void onRelease(ScoreAction next) {
        bot.interact(false);
    }
}
