package blade.platform;

import blade.utils.fake.FakePlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

import java.util.EnumSet;

public interface ServerPlatform extends Platform {
    void declareFakePlayer(FakePlayer player);

    ClientboundPlayerInfoUpdatePacket buildPlayerInfoPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, ClientboundPlayerInfoUpdatePacket.Entry entry);

    @Override
    default boolean isClient() {
        return false;
    }
}
