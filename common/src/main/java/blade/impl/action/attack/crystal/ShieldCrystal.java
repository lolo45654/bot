package blade.impl.action.attack.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.action.attack.shield.Shield;
import blade.impl.util.AnchorPosition;
import blade.impl.util.AttackUtil;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.utils.BotMath;
import blade.utils.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.isAttackSatisfied;
import static blade.impl.action.attack.shield.Shield.getShieldSlot;

public class ShieldCrystal extends BladeAction implements Shield {
    private CrystalPosition crystalPos = null;
    private AnchorPosition anchorPos = null;

    public Slot getTotemSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.SHIELD), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot shieldSlot = getShieldSlot(bot);
        if (shieldSlot == null) return;
        Slot totemSlot = getTotemSlot();
        if (shieldSlot.isOffHand() && totemSlot != null) {
            bot.getInventory().setSelectedSlot(totemSlot.hotbarIndex());
        } else if (shieldSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(shieldSlot.hotbarIndex());
        } else {
            bot.getInventory().openInventory();
            bot.getInventory().move(shieldSlot, Slot.ofHotbar(2));
            return;
        }
        bot.getInventory().closeInventory();

        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        double crystalConfidence = crystalPos == null ? 0 : crystalPos.confidence();
        double anchorConfidence = anchorPos == null ? 0 : anchorPos.confidence();
        Vec3 pos = crystalConfidence > anchorConfidence && crystalPos != null ? Vec3.atCenterOf(crystalPos.obsidian().above()) : anchorPos == null ? null : Vec3.atCenterOf(anchorPos.anchorPos());
        if (pos == null) throw new IllegalStateException("impossible");
        Vec3 direction = pos.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.setRotationTarget(yaw, pitch, ConfigKeys.getDifficultyReversedCubic(bot) * 400);

        bot.interact(true);
        bot.setSprint(false);
        bot.setMoveBackward(false);
        bot.setMoveForward(false);
        bot.setMoveRight(false);
        bot.setMoveLeft(false);
    }

    @Override
    public boolean isSatisfied() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);

        return isAttackSatisfied(bot) && getShieldSlot(bot) != null &&
                !bot.getVanillaPlayer().getCooldowns().isOnCooldown(Items.SHIELD) &&
                ((crystalPos = CrystalPosition.produce(target, crystalPos, bot.getVanillaPlayer())) != null ||
                (anchorPos = AnchorPosition.produce(target, anchorPos, bot.getVanillaPlayer())) != null);
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
        float ourHealthRatio = bot.getVanillaPlayer().getHealth() / bot.getVanillaPlayer().getMaxHealth();
        float targetHealthRatio = target.getHealth() / target.getMaxHealth();
        double crystalConfidence = crystalPos == null ? 0 : crystalPos.confidence();
        double anchorConfidence = anchorPos == null ? 0 : anchorPos.confidence();

        return (player.isUsingItem() && player.getUseItem().is(Items.SHIELD) ? Math.max(Math.max(tick - 12, 0) * -0.3, -1) : 0) +
                AttackUtil.isAttacking(target, player) * 2.0 +
                ((targetHealthRatio - ourHealthRatio) * 2 - bot.getBlade().get(ConfigKeys.DIFFICULTY)) +
                Math.max(crystalConfidence, anchorConfidence) / 3 +
                state.getValue(StateKeys.CRYSTAL_MODE);
    }
}
