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
import org.bukkit.enchantments.Enchantment;
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
        setCycleItem(gui, show, 2, 2,
                settings.armor, new ServerBotSettings.Armor[] { ServerBotSettings.Armor.DIAMOND, ServerBotSettings.Armor.NETHERITE },
                new ItemStack[] {
                        new ItemBuilder(baseItem.clone().withType(Material.DIAMOND_CHESTPLATE))
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <aqua><bold>DIAMOND"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(baseItem.clone().withType(Material.NETHERITE_CHESTPLATE))
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <light_purple><bold>NETHERITE"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get()
                }, val -> settings.armor = val);

        baseItem = new ItemBuilder(Material.OAK_BUTTON)
                .setDisplayName(wrapMiniMessage("<aqua>Reach"))
                .get();
        setCycleItem(gui, show, 4, 2,
                settings.reach, new Float[] { 1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f },
                new ItemStack[] {
                        new ItemBuilder(baseItem.clone().withType(Material.LIGHT_BLUE_DYE))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <aqua><bold>1"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(baseItem.clone().withType(Material.LIME_DYE))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <light_purple><bold>1.5"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(baseItem.clone().withType(Material.YELLOW_DYE))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <light_purple><bold>2"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(baseItem.clone().withType(Material.ORANGE_DYE))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <light_purple><bold>2.5"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(baseItem.clone().withType(Material.RED_DYE))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <light_purple><bold>3"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(baseItem.clone().withType(Material.PURPLE_DYE))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<gray>Selected: <light_purple><bold>3.5"))
                                .addLoreLines(empty())
                                .addLoreLines(wrapMiniMessage("<yellow>Click to cycle!"))
                                .get()
                }, val -> settings.reach = val);

        if (settings.shield) {
            gui.setItem(6, 2, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .setDisplayName(wrapMiniMessage("Shield"))
                    .addLoreLines(empty())
                    .addLoreLines(wrapMiniMessage("<gray>Status: <red><bold>ENABLED"))
                    .addLoreLines(empty())
                    .addLoreLines(wrapMiniMessage("<yellow>Click to disable!"))
                    .addLoreLines(wrapMiniMessage("<yellow>Right click to edit! ")), click -> {
                if (click.getClickType().isRightClick()) {
                } else {
                    settings.shield = false;
                }
            }));
        } else {
            gui.setItem(6, 2, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .setDisplayName(wrapMiniMessage("Shield"))
                    .addLoreLines(empty())
                    .addLoreLines(wrapMiniMessage("<gray>Status: <red><bold>DISABLED"))
                    .addLoreLines(empty())
                    .addLoreLines(wrapMiniMessage("<yellow>Click to enable!")), click -> settings.shield = true));
        }

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
            gui.setItem(5, 5, new SimpleItem(new ItemBuilder(Material.TNT)
                    .setDisplayName(wrapMiniMessage("<aqua>Despawn"))
                    .addLoreLines(empty())
                    .addLoreLines(wrapMiniMessage("<gray>No longer want it to exist?"))
                    .addLoreLines(empty())
                    .addLoreLines(wrapMiniMessage("<yellow>Click to despawn!")), click -> {
                bot.destroy();
                click.getPlayer().closeInventory();
            }));
        }

        Window.single()
                .setGui(gui)
                .setTitle(wrapMiniMessage("<green>Bot Settings"))
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
