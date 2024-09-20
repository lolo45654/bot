package blade.fabric.mixin;

import blade.Bot;
import blade.fabric.BotMod;
import blade.utils.fake.FakePlayer;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    public void sendFakePlayers(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        for (Bot bot : BotMod.PLATFORM.getBots()) {
            if (bot.getVanillaPlayer() instanceof FakePlayer fakePlayer) {
                fakePlayer.update(serverPlayer);
            }
        }
    }

    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    public void sendFakePlayers(ServerPlayer serverPlayer, boolean bl, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        for (Bot bot : BotMod.PLATFORM.getBots()) {
            if (bot.getVanillaPlayer() instanceof FakePlayer fakePlayer) {
                fakePlayer.update(serverPlayer);
            }
        }
    }
}
