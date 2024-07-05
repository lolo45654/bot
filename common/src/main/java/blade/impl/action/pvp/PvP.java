package blade.impl.action.pvp;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.ConfigKeys;
import blade.impl.action.pvp.crystal.Crystal;
import blade.impl.action.pvp.shield.Shield;
import blade.impl.action.pvp.sword.Sword;
import blade.impl.action.pvp.totem.Totem;
import blade.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface PvP {
    static void register(BladeMachine blade) {
        Totem.register(blade);
        Crystal.register(blade);
        Sword.register(blade);
        Shield.register(blade);
        // blade.addAction(new UseBow());
    }

    default void lookAtEnemy(Bot bot, int tick) {
        float time = ConfigKeys.getDifficultyReversedCubic(bot) * 1.2f;
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        Vec3 direction = closestPoint.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
    }

    default boolean isPvPSatisfied(Bot bot) {
        return bot.getBlade().get(ConfigKeys.TARGET) != null;
    }
}
