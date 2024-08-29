package blade.impl.action.attack.crystal;

import blade.debug.visual.VisualBox;
import blade.debug.visual.VisualText;
import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static blade.impl.action.attack.Attack.isPvPSatisfied;

public class DestroyCrystal extends BladeAction implements Crystal {
    private CrystalPosition crystalPos = null;
    private List<EndCrystal> endCrystals = null;

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
        Vec3 lookAt = endCrystals.getFirst().position().add(0, 0.2, 0);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 direction = lookAt.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
        if (tick >= time) {
            bot.attack();
        }

        bot.getBlade().addVisualDebug(new VisualBox(crystalPos.crystalAABB(), 0.9f, 0.1f, 0.3f, 0.5f));
        bot.getBlade().addVisualDebug(new VisualText(Vec3.atCenterOf(crystalPos.obsidian().above()), String.format("C: %.3f", crystalPos.confidence())));
    }

    @Override
    public boolean isSatisfied() {
        crystalPos = CrystalPosition.get(bot, crystalPos);
        if (crystalPos == null) return false;
        Level world = bot.getVanillaPlayer().level();
        endCrystals = world.getEntitiesOfClass(EndCrystal.class, crystalPos.crystalAABB());
        return isPvPSatisfied(bot) && !endCrystals.isEmpty();
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        return state.getValue(StateKeys.CRYSTAL_MODE) +
                (Math.max(Math.min(crystalPos.confidence() / 3, 3), 0)) +
                (hasWeakness() && !hasStrength() ? -1 : 0) +
                (getSwordSlot() == null ? 0 : 1);
    }

    @Override
    public String toString() {
        return String.format("destroy_crystal[pos=%s]", crystalPos);
    }
}
