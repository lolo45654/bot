package blade.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.ThreadLocalRandom;

public interface IServerBot {
    Property[] SKINS = new Property[] {
            new Property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTcxOTA1NTY2OTA5OSwKICAicHJvZmlsZUlkIiA6ICI0NTM1Y2RjNjk3NGU0Nzk4YjljYzY4ODlkZWY1MDk2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICIzZXlyZW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGI2YTVmYmIyNjQ1MjkzMjkyYTg0Mjk4ZDdlYTcwMTVkMjc2YzVlZDM3OWY2NjFmYWEyNzcyMGYxNWFkMTlhOCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                    "NBpbJIYZPHY7+z1tJ3PtxtgqL+usbgRyujq8iZ0sjOwbDgRmP20oJpqnSnv3cbMrCIc6IkSYqprmkwdXAEQWrYNZT+BLJvRK0rqqay5xjuGryjqNYb3MnKJbtTamXBWqUN5tQ4KvMoCW0cJNdJ/R2AQAK9nmL3sb1uJwNJBiCq+wsl0WZ2n1ca27oqq7SAHgq3kQVUBVuhw2XYIMPswQGD0JmLaK9PdQqJ8Evmrg+2ZzLVni3dvd8DBPQcChiG0RD0EvNMfF7BNEnMdGWsWFNbJ42pCahliEUG+jd2eymEmsityYyYiGiUrHc/lDjmeJ7A/CbG0hKGRhtnbQYrC3cxA+uZYokcLsOAddh3JrkSW7LAXlMLspJJXEDfEcN5yOpFVQpEkNsHISpi6mLBzHHKIlJP1pDk7EDgqR7sUghqaHycEZ3Znzv0SIY1GVqjmpHZDy3v/vSr+BulnezjR9kUvGE/oiUPbFCwgPEQN5KK+MLzWsUZch2gKJX073P17ldIc4awzQX/peDOFh4HnNVFf11PhT2sPDfoGw+D1nWL1bpYu5vT2gtuh79Rr7LTL2IQ2a63ralGx13Q8Ao4149z15odX9IOZBjUNgjFEts9y8nhc4IkoluXN7F6Kj3SbvPQJIZpYbJeqzqGr/h8Ghe9yYY/jafXWyCmJ9TMJ2S5M=")
    };

    ServerPlayer getSpawner();

    ServerBotSettings getSettings();

    ServerPlayer getVanillaPlayer();

    void destroy();

    static GameProfile applySkin(GameProfile profile) {
        PropertyMap properties = profile.getProperties();
        properties.put("textures", SKINS[ThreadLocalRandom.current().nextInt(SKINS.length)]);
        return profile;
    }
}
