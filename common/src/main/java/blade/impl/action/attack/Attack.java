package blade.impl.action.attack;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.ConfigKeys;
import blade.impl.action.attack.crystal.Crystal;
import blade.impl.action.attack.shield.Shield;
import blade.impl.action.attack.sword.Sword;
import blade.impl.action.attack.totem.Totem;
import blade.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface Attack {
    static void register(BladeMachine blade) {
        Totem.register(blade);
        Crystal.register(blade);
        Sword.register(blade);
        Shield.register(blade);
    }

    static void lookAtEnemy(Bot bot, int tick) {
        float time = ConfigKeys.getDifficultyReversedCubic(bot) * 1.2f;
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        Vec3 direction = closestPoint.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
    }

    static boolean isPvPSatisfied(Bot bot) {
        return bot.getBlade().get(ConfigKeys.TARGET) != null;
    }

    static double getReach(Bot bot) {
        return bot.getVanillaPlayer().entityInteractionRange() - ConfigKeys.getDifficultyReversedCubic(bot) / 2;
    }
}
