package blade.impl.action.attack.totem;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.action.attack.Attack;
import blade.impl.util.AttackUtil;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class EquipTotem extends BladeAction implements Totem {
    @Override
    public void onTick() {
        BotInventory inv = bot.getInventory();
        Slot totemSlot = Totem.getInventoryTotemSlot(bot);
        if (totemSlot == null) return;
        if (tick == 0) {
            inv.openInventory();
        }
        if (tick >= ConfigKeys.getDifficultyReversedCubic(bot) * 0.8) {
            inv.move(totemSlot, Slot.fromOffHand(), true);
        }
    }

    @Override
    public boolean isSatisfied() {
        return Attack.isPvPSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.OFF_HAND_TOTEM, 1);
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        BotInventory inv = bot.getInventory();
        Player player = bot.getVanillaPlayer();
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double deltaY = player.getDeltaMovement().y;
        double health = 1 - player.getHealth() / player.getMaxHealth();
        Slot hotBarSlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);

        return Totem.getTotemScore(bot) +
                (inv.getOffHand().is(Items.TOTEM_OF_UNDYING) ? -12 : 0) +
                (hotBarSlot != null && inv.getSelectedSlot() == hotBarSlot.getHotBarIndex() ? 0.3 : 0) +
                Math.min(Math.max(deltaY * 2, 0), 1.0) +
                Math.min(health * health, 1) +
                AttackUtil.isAttacking(target, bot.getVanillaPlayer()) +
                tick > 0 ? 0.5 : 0;
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.getInventory().closeInventory();
    }
}
