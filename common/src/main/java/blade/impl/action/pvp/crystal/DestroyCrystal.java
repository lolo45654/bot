package blade.impl.action.pvp.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DestroyCrystal extends BladeAction implements Crystal {
    private CrystalPosition crystalPos = null;

    public Slot getSwordSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(ItemTags.SWORDS), SlotFlag.HOT_BAR);
    }

    public boolean hasWeakness() {
        return bot.getVanillaPlayer().hasEffect(MobEffects.WEAKNESS);
    }

    public boolean hasStrength() {
        return bot.getVanillaPlayer().hasEffect(MobEffects.DAMAGE_BOOST);
    }

    @Override
    public void onTick() {
        float time = ConfigKeys.getDifficultyReversedCubic(bot) * 0.3f;
        BlockPos obsidian = crystalPos.obsidian();
        Vec3 lookAt = Vec3.atCenterOf(obsidian.above());
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 direction = lookAt.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
        if (tick >= time) {
            bot.attack();
        }
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot) && (crystalPos = CrystalPosition.get(bot)) != null;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        Level world = bot.getVanillaPlayer().level();
        List<EndCrystal> endCrystals = world.getEntitiesOfClass(EndCrystal.class, crystalPos.crystalAABB());
        return getCrystalScore(bot) +
                (Math.max(Math.min(crystalPos.confidence() * 4, 6), 0)) +
                (endCrystals.isEmpty() ? -12 : 4) +
                (hasWeakness() && !hasStrength() ? -1 : 0) +
                (getSwordSlot() == null ? 0 : 1);
    }

    @Override
    public String toString() {
        return String.format("destroy_crystal[pos=%s]", crystalPos);
    }
}
