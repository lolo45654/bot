package blade.paper;

import blade.bot.IServerBot;
import blade.bot.ServerBot;
import blade.bot.ServerBotSettings;
import blade.util.fake.FakePlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
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

public class BotSettingGui {
    public static void show(ServerPlayer player, PaperPlatform platform, ServerBotSettings settings, @Nullable IServerBot bot) {
        Runnable show = () -> show(player, platform, settings, bot);
        Gui gui = Gui.empty(9, 6);
        gui.setBackground(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(MenuUtils.empty()));

        ItemStack baseItem = new ItemBuilder(Material.OAK_BUTTON)
                .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Armor"))
                .get();
        setCycleItem(gui, show, 2, 2,
                settings.armor, new ServerBotSettings.Armor[] { ServerBotSettings.Armor.DIAMOND, ServerBotSettings.Armor.NETHERITE },
                new ItemStack[] {
                        new ItemBuilder(withType(baseItem, Material.DIAMOND_CHESTPLATE))
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <aqua><bold>DIAMOND"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(withType(baseItem, Material.NETHERITE_CHESTPLATE))
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <light_purple><bold>NETHERITE"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get()
                }, val -> settings.armor = val);

        baseItem = new ItemBuilder(Material.OAK_BUTTON)
                .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Blast Protection"))
                .addLoreLines(MenuUtils.empty())
                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Are <aqua>Blast Protection</aqua> Leggings really needed?"))
                .get();
        setBoolItem(gui, show, 2, 3,
                settings.blastProtection,
                new ItemBuilder(withType(baseItem, Material.LIME_DYE))
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <green><bold>ON"))
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to toggle!"))
                        .get(),
                new ItemBuilder(withType(baseItem, Material.GRAY_DYE))
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <red><bold>OFF"))
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to toggle!"))
                        .get(), val -> settings.blastProtection = val);

        baseItem = new ItemBuilder(Material.OAK_BUTTON)
                .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Reach"))
                .addLoreLines(MenuUtils.empty())
                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>This only affects auto hit."))
                .get();
        setCycleItem(gui, show, 4, 2,
                settings.reach, new Float[] { 1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f },
                new ItemStack[] {
                        new ItemBuilder(withType(baseItem, Material.LIGHT_BLUE_DYE))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <aqua><bold>1"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(withType(baseItem, Material.LIME_DYE))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <light_purple><bold>1.5"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(withType(baseItem, Material.YELLOW_DYE))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <light_purple><bold>2"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(withType(baseItem, Material.ORANGE_DYE))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <light_purple><bold>2.5"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(withType(baseItem, Material.RED_DYE))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <light_purple><bold>3"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(withType(baseItem, Material.PURPLE_DYE))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <light_purple><bold>3.5"))
                                .addLoreLines(MenuUtils.empty())
                                .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to cycle!"))
                                .get()
                }, val -> settings.reach = val);

        if (settings.shield) {
            gui.setItem(6, 2, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Shield"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Status: <green><bold>ENABLED"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to disable!")), click -> {
                settings.shield = false;
                show.run();
            }));
            gui.setItem(6, 3, new SimpleItem(new ItemBuilder(Material.REDSTONE)
                    .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Shield Settings"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<gray>That's more settings."))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to edit! ")), click -> {
                showShieldGroup(player, platform, settings, bot, show);
            }));
        } else {
            gui.setItem(6, 2, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Shield"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Status: <red><bold>DISABLED"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to enable!")), click -> {
                settings.shield = true;
                show.run();
            }));
        }

        if (bot == null) {
            gui.setItem(5, 5, new SimpleItem(new ItemBuilder(Material.DIAMOND)
                    .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Spawn Bot"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to spawn!")), click -> {
                Location pos = click.getPlayer().getLocation();
                FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "SimpleBot"));
                ServerBot spawningBot = new ServerBot(fakePlayer, platform, player, settings);
                platform.addBot(spawningBot);
                click.getPlayer().closeInventory();
            }));
        } else {
            gui.setItem(3, 5, new SimpleItem(new ItemBuilder(Material.ENDER_PEARL)
                    .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Teleport to you"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<gray>We lost contact, reconnecting..."))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to teleport!")), click -> {
                bot.getVanillaPlayer().teleportTo(player.serverLevel(), player.position());
                click.getPlayer().closeInventory();
            }));
            gui.setItem(5, 5, new SimpleItem(new ItemBuilder(Material.TNT)
                    .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Despawn"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<gray>No longer want it to exist?"))
                    .addLoreLines(MenuUtils.empty())
                    .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to despawn!")), click -> {
                bot.destroy();
                click.getPlayer().closeInventory();
            }));
        }
        gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.BARRIER)
                .setDisplayName(MenuUtils.wrapMiniMessage("<red>Close")), click -> {
            click.getPlayer().closeInventory();
        }));

        Window.single()
                .setGui(gui)
                .setTitle(MenuUtils.wrapMiniMessage("<aqua>Bot Settings"))
                .build(player.getBukkitEntity()).open();
    }

    public static void showShieldGroup(ServerPlayer player, PaperPlatform platform, ServerBotSettings settings, @Nullable IServerBot bot, Runnable back) {
        Runnable show = () -> showShieldGroup(player, platform, settings, bot, back);
        Gui gui = Gui.empty(9, 6);
        gui.setBackground(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(MenuUtils.empty()));

        ItemStack baseItem = new ItemBuilder(Material.OAK_BUTTON)
                .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Auto Hit"))
                .get();
        setBoolItem(gui, show, 2, 2,
                settings.autoHit,
                new ItemBuilder(withType(baseItem, Material.DIAMOND_SWORD))
                        .addEnchantment(Enchantment.MENDING, 1, true)
                        .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <green><bold>ON"))
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to toggle!"))
                        .get(),
                new ItemBuilder(withType(baseItem, Material.WOODEN_SWORD))
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<gray>Selected: <red><bold>OFF"))
                        .addLoreLines(MenuUtils.empty())
                        .addLoreLines(MenuUtils.wrapMiniMessage("<yellow>Click to toggle!"))
                        .get(), val -> settings.autoHit = val);


        gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.OAK_DOOR)
                .setDisplayName(MenuUtils.wrapMiniMessage("<aqua>Go back")), click -> {
            back.run();
        }));

        Window.single()
                .setGui(gui)
                .setTitle(MenuUtils.wrapMiniMessage("<aqua>Shield"))
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
    
    private static ItemStack withType(ItemStack stack, Material type) {
        ItemStack itemStack = new ItemStack(type, stack.getAmount());
        if (stack.hasItemMeta()) {
            itemStack.setItemMeta(stack.getItemMeta());
        }

        return itemStack;
    }
}
