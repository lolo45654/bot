package blade.paper;

import blade.BuildConstants;
import blade.bot.IServerBot;
import blade.bot.KitBot;
import blade.bot.ServerBot;
import blade.bot.ServerBotSettings;
import blade.utils.ItemUtils;
import blade.utils.fake.FakePlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
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
import java.util.function.Consumer;

import static blade.paper.MenuUtils.empty;
import static blade.paper.MenuUtils.wrap;

public class BotSettingGui {
    public static void show(ServerPlayer player, PaperPlatform platform, ServerBotSettings settings, @Nullable IServerBot bot) {
        Runnable show = () -> show(player, platform, settings, bot);
        Gui gui = Gui.empty(9, 6);
        gui.setBackground(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(empty()));

        if (bot instanceof KitBot) {
            gui.setItem(4, 2, new SimpleItem(new ItemBuilder(Material.RED_DYE)
                    .setDisplayName(wrap("<red>You can't edit a Kit Bot."))));

            gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.BARRIER)
                    .setDisplayName(wrap("<red>Close")), click -> {
                click.getPlayer().closeInventory();
            }));
            Window.single()
                    .setGui(gui)
                    .setTitle(wrap("<aqua>Bot Settings"))
                    .build(player.getBukkitEntity()).open();
            return;
        }

        addEquipmentItems(gui, show, 1, 1, EquipmentSlot.HEAD, settings);
        addEquipmentItems(gui, show, 1, 2, EquipmentSlot.CHEST, settings);
        addEquipmentItems(gui, show, 1, 3, EquipmentSlot.LEGS, settings);
        addEquipmentItems(gui, show, 1, 4, EquipmentSlot.FEET, settings);

        if (settings.shield) {
            gui.setItem(6, 2, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    .setDisplayName(wrap("<aqua>Shield"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<gray>Selected: <green><bold>ON"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<yellow>Click to disable!")), click -> {
                settings.shield = false;
                show.run();
            }));
            ItemStack baseItem = new ItemBuilder(Material.OAK_BUTTON)
                    .setDisplayName(wrap("<aqua>Auto Hit"))
                    .get();
            setBoolItem(gui, show, 6, 3,
                    settings.autoHit,
                    new ItemBuilder(withType(baseItem, Material.DIAMOND_SWORD))
                            .addEnchantment(Enchantment.MENDING, 1, true)
                            .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            .addLoreLines(empty())
                            .addLoreLines(wrap("<gray>Selected: <green><bold>ON"))
                            .addLoreLines(empty())
                            .addLoreLines(wrap("<yellow>Click to toggle!"))
                            .get(),
                    new ItemBuilder(withType(baseItem, Material.WOODEN_SWORD))
                            .addLoreLines(empty())
                            .addLoreLines(wrap("<gray>Selected: <red><bold>OFF"))
                            .addLoreLines(empty())
                            .addLoreLines(wrap("<yellow>Click to toggle!"))
                            .get(), val -> settings.autoHit = val);
        } else {
            gui.setItem(6, 2, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    .setDisplayName(wrap("<aqua>Shield"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<gray>Selected: <red><bold>OFF"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<yellow>Click to enable!")), click -> {
                settings.shield = true;
                show.run();
            }));
        }

        setBoolItem(gui, show, 4, 2, settings.effects.contains(MobEffects.SLOW_FALLING),
                new ItemBuilder(Material.FEATHER)
                        .addEnchantment(Enchantment.MENDING, 1, true)
                        .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        .setDisplayName(wrap("<aqua>Feather Falling"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Selected: <green><bold>ON"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<yellow>Click to toggle!"))
                        .get(),
                new ItemBuilder(Material.FEATHER)
                        .setDisplayName(wrap("<aqua>Feather Falling"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Selected: <red><bold>OFF"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<yellow>Click to toggle!"))
                        .get(),
                v -> {
                    if (v) settings.effects.add(MobEffects.SLOW_FALLING);
                    else settings.effects.remove(MobEffects.SLOW_FALLING);
                });

        setBoolItem(gui, show, 4, 3, settings.moveTowardsSpawner,
                new ItemBuilder(Material.CHEST_MINECART)
                        .setDisplayName(wrap("<aqua>Run at you"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Causes the Bot to move towards you,"))
                        .addLoreLines(wrap("<gray>while keeping its distance at 2 blocks."))
                        .addLoreLines(wrap("<dark_gray>Doesn't move right after popping!"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Selected: <green><bold>ON"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<yellow>Click to toggle!"))
                        .get(),
                new ItemBuilder(Material.MINECART)
                        .setDisplayName(wrap("<aqua>Run at you"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Causes the Bot to move towards you,"))
                        .addLoreLines(wrap("<gray>while keeping its distance at 2 blocks."))
                        .addLoreLines(wrap("<dark_gray>Doesn't move right after popping!"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Selected: <red><bold>OFF"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<yellow>Click to toggle!"))
                        .get(),
                v -> settings.moveTowardsSpawner = v);

        if (bot == null) {
            gui.setItem(5, 5, new SimpleItem(new ItemBuilder(Material.DIAMOND)
                    .setDisplayName(wrap("<aqua>Spawn Bot"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<yellow>Click to spawn!")), click -> {
                Location pos = click.getPlayer().getLocation();
                FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), IServerBot.getProfile());
                ServerBot spawningBot = new ServerBot(fakePlayer, platform, player, settings);
                platform.addBot(spawningBot);
                click.getPlayer().closeInventory();
            }));
        } else {
            gui.setItem(3, 5, new SimpleItem(new ItemBuilder(Material.ENDER_PEARL)
                    .setDisplayName(wrap("<aqua>Teleport to You"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<gray>We lost contact, attempting automatic"))
                    .addLoreLines(wrap("<gray>reconnecting ... failed, requires"))
                    .addLoreLines(wrap("<gray>Human validation."))
                    .addLoreLines(empty())
                    .addLoreLines(wrap(player.getScoreboardName().equals("Dolphinzo") && BuildConstants.EASTER_EGGS ? "<red>You are not Human." : "<yellow>Click to reconnect!")), click -> {
                Vec3 pos = player.position();
                bot.getVanillaPlayer().teleportTo(player.serverLevel(), pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
                click.getPlayer().closeInventory();
            }));
            gui.setItem(5, 5, new SimpleItem(new ItemBuilder(Material.TNT)
                    .setDisplayName(wrap("<aqua>Despawn"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<gray>No longer want it to exist? You're"))
                    .addLoreLines(wrap("<gray>just a click away from destroying this .. bot?"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<yellow>Click to despawn!")), click -> {
                bot.destroy();
                click.getPlayer().closeInventory();
            }));
        }

        gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.BARRIER)
                .setDisplayName(wrap("<red>Close")), click -> {
            click.getPlayer().closeInventory();
        }));
        Window.single()
                .setGui(gui)
                .setTitle(wrap("<aqua>Bot Settings"))
                .build(player.getBukkitEntity()).open();
    }

    private static void addEquipmentItems(Gui gui, Runnable show, int x, int y, EquipmentSlot slot, ServerBotSettings settings) {
        ServerBotSettings.ArmorPiece armor = settings.armor.get(slot);
        setBoolItem(gui, show, x, y, armor.blastProtection(),
                new ItemBuilder(Material.ENCHANTED_BOOK)
                        .setDisplayName(wrap("<aqua>Blast Protection"))
                        .addLoreLines(wrap(String.format("<dark_gray>for the %s", ItemUtils.getSlotName(slot))))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Selected: <green><bold>ON"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<yellow>Click to disable!"))
                        .get(),
                new ItemBuilder(Material.BOOK)
                        .setDisplayName(wrap("<aqua>Blast Protection"))
                        .addLoreLines(wrap(String.format("<dark_gray>for the %s", ItemUtils.getSlotName(slot))))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Selected: <red><bold>OFF"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<yellow>Click to enable!"))
                        .get(),
                v -> settings.armor.put(slot, armor.withBlastProtection(v)));
        setCycleItem(gui, show, x + 1, y, armor.type(), new ServerBotSettings.ArmorType[] { ServerBotSettings.ArmorType.NETHERITE, ServerBotSettings.ArmorType.DIAMOND },
                new ItemStack[] {
                        new ItemBuilder(CraftItemStack.asNewCraftStack(armor.type().slotToItem.get(slot)))
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .setDisplayName(wrap("<aqua>Armor Type"))
                                .addLoreLines(wrap(String.format("<dark_gray>for the %s", ItemUtils.getSlotName(slot))))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<gray>Selected: <light_purple><bold>NETHERITE"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<yellow>Click to cycle!"))
                                .get(),
                        new ItemBuilder(CraftItemStack.asNewCraftStack(armor.type().slotToItem.get(slot)))
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .setDisplayName(wrap("<aqua>Armor Type"))
                                .addLoreLines(wrap(String.format("<dark_gray>for the %s", ItemUtils.getSlotName(slot))))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<gray>Selected: <aqua><bold>DIAMOND"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<yellow>Click to cycle!"))
                                .get(),
                }, v -> settings.armor.put(slot, armor.withType(v)));
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
