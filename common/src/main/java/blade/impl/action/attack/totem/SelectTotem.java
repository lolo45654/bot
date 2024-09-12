package blade.impl.action.attack.totem;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.Slot;
import blade.planner.score.ScoreState;
import blade.utils.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static blade.impl.action.attack.Attack.isAttackSatisfied;
import static blade.impl.action.attack.Attack.lookAtEnemy;
import static blade.impl.action.attack.totem.Totem.getHotBarTotemSlot;

public class SelectTotem extends BladeAction implements Totem {
    @Override
    public void onTick() {
        Slot totemSlot = getHotBarTotemSlot(bot);
        if (totemSlot == null) return;
        lookAtEnemy(bot, tick);
        bot.getInventory().setSelectedSlot(totemSlot.hotbarIndex());
    }

    @Override
    public boolean isSatisfied() {
        return isAttackSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOUBLE_HAND_TOTEM, 1.0);
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        Player player = bot.getVanillaPlayer();
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double deltaY = player.getDeltaMovement().y;

        return state.getValue(StateKeys.CRYSTAL_MODE) / 3 +
                Math.min(Math.max(deltaY, 0), 1) +
                AttackUtil.isAttacking(target, bot.getVanillaPlayer());
    }
}
