package me.loloed.bot.blade;

import com.mojang.authlib.GameProfile;
import me.loloed.bot.api.impl.ServerBot;
import me.loloed.bot.api.impl.ServerBotSettings;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftLocation;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CycleItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

import static me.loloed.bot.blade.MenuUtils.empty;
import static me.loloed.bot.blade.MenuUtils.wrapMiniMessage;

public class BotSettingGui {
    public static void show(ServerPlayer player, PaperPlatform platform, ServerBotSettings settings, @Nullable ServerBot bot) {
        Runnable show = () -> show(player, platform, settings, bot);
        Gui gui = Gui.empty(9, 6);
        gui.setBackground(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(empty()));

        ItemStack baseItem = new ItemBuilder(Material.OAK_BUTTON)
                .setDisplayName(wrapMiniMessage("<aqua>Armor"))
                .get();
        setCycleItem(gui, show, 1, 2,
                settings.armor, new ServerBotSettings.Armor[] { ServerBotSettings.Armor.DIAMOND, ServerBotSettings.Armor.NETHERITE },
                new ItemStack[] {
                        new ItemBuilder(baseItem.clone())
                                .setMaterial(Material.DIAMOND_CHESTPLATE)
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <aqua><bold>DIAMOND"))
                                .get(),
                        new ItemBuilder(baseItem.clone())
                                .setMaterial(Material.NETHERITE_CHESTPLATE)
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <light_purple><bold>NETHERITE"))
                                .get()
                }, val -> settings.armor = val);

        if (bot == null) {
            gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.DIAMOND)
                    .setDisplayName(wrapMiniMessage("<aqua>Spawn Bot"))
                    .addLoreLines(empty())
                    .addLoreLines(wrapMiniMessage("<yellow>Click to spawn!")), click -> {
                Location pos = click.getPlayer().getLocation();
                FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "ShieldBot"));
                ServerBot spawningBot = new ServerBot(fakePlayer, platform, player, settings);
                platform.addBot(spawningBot);
                click.getPlayer().closeInventory();
            }));
        } else {
            gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.BARRIER)
                    .setDisplayName(wrapMiniMessage("<red>Close")), click -> {
                click.getPlayer().closeInventory();
            }));
        }

        gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.BARRIER)
                .setDisplayName(new AdventureComponentWrapper(Component.text("Close", NamedTextColor.RED))),
                click -> click.getPlayer().closeInventory()));

        Window.single()
                .setGui(gui)
                .setTitle(new AdventureComponentWrapper(Component.text("Bot Settings", NamedTextColor.GREEN)))
                .build(player.getBukkitEntity()).open();
    }

    private static <O> void setCycleItem(Gui gui, Runnable show, int x, int y, O currently, O[] options, ItemStack[] optionItems, Consumer<O> setter) {
        ItemProvider[] items = new ItemProvider[options.length];
        for (int i = 0; i < options.length; i++) {
            items[i] = new ItemWrapper(optionItems[i]);
        }

        int pos = Arrays.binarySearch(options, currently);
        gui.setItem(x, y, CycleItem.withStateChangeHandler((player, index) -> {
            setter.accept(options[index]);
            show.run();
        }, pos == -1 ? 0 : pos, items));
    }

    private static void setBoolItem(Gui gui, Runnable show, int x, int y, boolean currently, ItemStack trueItem, ItemStack falseItem, Consumer<Boolean> setter) {
        gui.setItem(x, y, new SimpleItem(currently ? trueItem : falseItem, click -> {
            setter.accept(!currently);
            show.run();
        }));
    }
}
