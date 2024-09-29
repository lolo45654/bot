package blade.impl.action.attack.totem;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.utils.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import static blade.impl.action.attack.Attack.isAttackSatisfied;
import static blade.impl.action.attack.totem.Totem.getInventoryTotemSlot;
import static blade.impl.action.attack.totem.Totem.isTotemSatisfied;

public class EquipOffhandTotem extends BladeAction implements Totem {
    @Override
    public void onTick() {
        BotInventory inv = bot.getInventory();
        Slot totemSlot = getInventoryTotemSlot(bot);
        if (totemSlot == null) return;
        if (tick == 0) {
            inv.openInventory();
        }
        if (tick >= ConfigKeys.getDifficultyReversedCubic(bot) * 6) {
            inv.move(totemSlot, Slot.ofOffhand());
        }
    }

    @Override
    public boolean isSatisfied() {
        return isAttackSatisfied(bot) && isTotemSatisfied(bot) &&
                getInventoryTotemSlot(bot) != null &&
                !bot.getInventory().getOffHand().is(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.OFF_HAND_TOTEM, 1.0);
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        BotInventory inv = bot.getInventory();
        Player player = bot.getVanillaPlayer();
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double deltaY = player.getDeltaMovement().y;
        double health = 2 - player.getHealth() / player.getMaxHealth();
        Slot hotBarSlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);

        return state.getValue(StateKeys.CRYSTAL_MODE) +
                (hotBarSlot != null && inv.getSelectedSlot() == hotBarSlot.hotbarIndex() ? 0.3 : 0) +
                Math.min(Math.max(deltaY * 4, 0), 3.0) +
                Math.min(Math.max(health * health, 0), 1) +
                AttackUtil.isAttacking(target, bot.getVanillaPlayer()) +
                (tick > 0 ? 0.5 : 0);
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.getInventory().closeInventory();
    }
}
