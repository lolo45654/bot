package me.loloed.bot.blade;

import com.mojang.authlib.GameProfile;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.BuildConstants;
import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.blade.debug.BladeDebug;
import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.impl.goal.KillTargetGoal;
import me.loloed.bot.api.impl.ShieldBot;
import me.loloed.bot.api.impl.TotemBot;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftLocation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

import java.util.*;
import java.util.function.Function;

public class BladePlugin extends JavaPlugin {
    public static final TextColor PRIMARY = TextColor.color(0xFFCBD5);
    public static PaperPlatform platform;
    private static final Map<String, BladeDebug> reports = new HashMap<>();

    @Override
    public void onLoad() {
        super.onLoad();
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        platform = new PaperPlatform();
        platform.register(this);

        new CommandTree("bot")
                .withPermission("bot.use")
                .then(new LiteralArgument("totem")
                        .withPermission("bot.totem")
                        .executesPlayer((sender, args) -> {
                            Location pos = sender.getLocation();
                            List<Bot> bots = new ArrayList<>(PaperPlatform.BOTS);
                            for (Bot bot : bots) {
                                if (bot instanceof TotemBot totemBot && totemBot.getSpawner().getUUID().equals(sender.getUniqueId())) {
                                    bot.destroy();
                                    sender.sendMessage(Component.text("Removed your totem bot!", PRIMARY));
                                    return;
                                }
                            }

                            FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "TotemBot"));
                            TotemBot bot = new TotemBot(fakePlayer, platform, ((CraftPlayer) sender).getHandle());
                            platform.addBot(bot);
                            sender.sendMessage(Component.text("Spawned a totem bot!", PRIMARY));
                        }))
                .then(new LiteralArgument("shield")
                        .withPermission("bot.shield")
                        .executesPlayer((sender, args) -> {
                            Location pos = sender.getLocation();
                            List<Bot> bots = new ArrayList<>(PaperPlatform.BOTS);
                            for (Bot bot : bots) {
                                if (bot instanceof ShieldBot totemBot && totemBot.getSpawner().getUUID().equals(sender.getUniqueId())) {
                                    bot.destroy();
                                    sender.sendMessage(Component.text("Removed your shield bot!", PRIMARY));
                                    return;
                                }
                            }

                            FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), "ShieldBot"));
                            ShieldBot bot = new ShieldBot(fakePlayer, platform, ((CraftPlayer) sender).getHandle());
                            platform.addBot(bot);
                            sender.sendMessage(Component.text("Spawned a shield bot!", PRIMARY));
                        }))
                .register();

        new CommandTree("blade")
                .withPermission("bot.blade.use")
                .then(new LiteralArgument("control")
                        .withPermission("blade.control")
                        .then(createPossibleBots(new PlayerArgument("who"), args -> ((CraftPlayer) args.get("who")).getHandle())))
                .then(new LiteralArgument("spawn")
                        .withPermission("blade.spawn")
                        .then(createPossibleBots(new LocationArgument("where"), args -> {
                            Location pos = (Location) args.get("where");
                            return new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), BuildConstants.CENSOR_NAMES ? "BOT" : "Blade"));
                        })))
                .then(new LiteralArgument("removeall")
                        .executes((sender, args) -> {
                            platform.removeAll();
                        }))
                .then(new LiteralArgument("debug")
                        .then(new LiteralArgument("save")
                                .then(new GreedyStringArgument("name")
                                        .executes((sender, args) -> {
                                            Bot first = PaperPlatform.BOTS.getFirst();
                                            if (first == null) return;
                                            reports.put((String) args.get("name"), first.getBlade().getReport());
                                        })))
                        .then(new LiteralArgument("view")
                                .then(new GreedyStringArgument("name")
                                        .replaceSuggestions(ArgumentSuggestions.strings(info -> reports.keySet().toArray(new String[0])))
                                        .executesPlayer((sender, args) -> {
                                            String name = (String) args.get("name");
                                            if (name == null || !reports.containsKey(name)) return;
                                            // DebugGui.showMain(sender, reports.get(name));
                                        }))))
                .then(new LiteralArgument("count")
                        .executes((sender, args) -> {
                            sender.sendMessage(Component.text("There are currently " + PaperPlatform.BOTS.size() + " blade bot(s)."));
                        }))
                .register();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        platform.removeAll();
    }

    public Argument<?> createPossibleBots(Argument<?> tree, Function<CommandArguments, ServerPlayer> playerGetter) {
        Function<CommandArguments, Bot> botGetter = args -> {
            Bot bot = new Bot(playerGetter.apply(args), platform);
            platform.addBot(bot);
            return bot;
        };

        return tree.then(new LiteralArgument("kill")
                .then(new EntitySelectorArgument.OneEntity("target")
                        .executesPlayer((sender, args) -> {
                            Entity target = (Entity) args.get("target");
                            if (!(target instanceof LivingEntity)) return;
                            Bot bot = botGetter.apply(args);
                            BladeMachine blade = bot.getBlade();
                            blade.set(ConfigKeys.TARGET, ((CraftLivingEntity) target).getHandle());
                            blade.setGoal(new KillTargetGoal());
                            sender.sendMessage(Component.text("Spawned a killer bot."));
                        })))
                .then(new LiteralArgument("cart")
                        .then(new EntitySelectorArgument.OneEntity("target")
                                .executesPlayer((sender, args) -> {
                                    Entity target = (Entity) args.get("target");
                                    if (!(target instanceof LivingEntity)) return;
                                    Bot bot = botGetter.apply(args);
                                    BladeMachine blade = bot.getBlade();
                                    blade.set(ConfigKeys.TARGET, ((CraftLivingEntity) target).getHandle());
                                    blade.setGoal(new KillTargetGoal());

                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemBuilder(Material.BOW)
                                            .addEnchantment(Enchantment.ARROW_FIRE, 1, true).get());
                                    for (int i = 0; i < 5; i++) {
                                        inv.addItem(new ItemStack(Material.TNT_MINECART));
                                    }
                                    inv.addItem(new ItemStack(Material.RAIL, 64));
                                    inv.addItem(new ItemStack(Material.ARROW, 64));
                                    sender.sendMessage(Component.text("Spawned a cart bot."));
                                })))
                .then(new LiteralArgument("crystal")
                        .then(new EntitySelectorArgument.OneEntity("target")
                                .executesPlayer((sender, args) -> {
                                    Entity target = (Entity) args.get("target");
                                    if (!(target instanceof LivingEntity)) return;
                                    Bot bot = botGetter.apply(args);
                                    BladeMachine blade = bot.getBlade();
                                    blade.set(ConfigKeys.TARGET, ((CraftLivingEntity) target).getHandle());
                                    blade.setGoal(new KillTargetGoal());

                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemBuilder(Material.NETHERITE_SWORD)
                                            .addEnchantment(Enchantment.KNOCKBACK, 1, true).get());
                                    inv.addItem(new ItemStack(Material.END_CRYSTAL, 64));
                                    inv.addItem(new ItemStack(Material.OBSIDIAN, 64));
                                    inv.addItem(new ItemStack(Material.ENDER_PEARL, 16));
                                    inv.setHelmet(new ItemBuilder(Material.NETHERITE_HELMET)
                                            .addEnchantment(Enchantment.DAMAGE_ALL, 4, true).get());
                                    inv.setChestplate(new ItemBuilder(Material.NETHERITE_CHESTPLATE)
                                            .addEnchantment(Enchantment.DAMAGE_ALL, 4, true).get());
                                    inv.setLeggings(new ItemBuilder(Material.NETHERITE_LEGGINGS)
                                            .addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4, true).get());
                                    inv.setBoots(new ItemBuilder(Material.NETHERITE_BOOTS)
                                            .addEnchantment(Enchantment.DAMAGE_ALL, 4, true)
                                            .addEnchantment(Enchantment.PROTECTION_FALL, 4, true).get());
                                    for (int i = 0; i < 12; i++) {
                                        inv.addItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                                    }
                                    inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
                                    sender.sendMessage(Component.text("OK!"));
                                })))
                .then(new LiteralArgument("sword")
                        .then(new EntitySelectorArgument.OneEntity("target")
                                .executesPlayer((sender, args) -> {
                                    Entity target = (Entity) args.get("target");
                                    if (!(target instanceof LivingEntity)) return;
                                    Bot bot = botGetter.apply(args);
                                    BladeMachine blade = bot.getBlade();
                                    blade.set(ConfigKeys.TARGET, ((CraftLivingEntity) target).getHandle());
                                    blade.setGoal(new KillTargetGoal());

                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemBuilder(Material.DIAMOND_SWORD).get());
                                    inv.setHelmet(new ItemBuilder(Material.DIAMOND_HELMET)
                                            .addEnchantment(Enchantment.DAMAGE_ALL, 4, true).get());
                                    inv.setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                                            .addEnchantment(Enchantment.DAMAGE_ALL, 4, true).get());
                                    inv.setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS)
                                            .addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4, true).get());
                                    inv.setBoots(new ItemBuilder(Material.DIAMOND_BOOTS)
                                            .addEnchantment(Enchantment.DAMAGE_ALL, 4, true)
                                            .addEnchantment(Enchantment.PROTECTION_FALL, 4, true).get());
                                    inv.setItemInOffHand(new ItemStack(Material.GOLDEN_APPLE, 64));
                                    sender.sendMessage(Component.text("OK!"));
                                })))
                .then(new LiteralArgument("test")
                        .then(new LiteralArgument("move_forward")
                                .executesPlayer((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    bot.setMoveForward(true);
                                }))
                        .then(new LiteralArgument("move_right")
                                .executesPlayer((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    bot.setMoveRight(true);
                                }))
                        .then(new LiteralArgument("attack")
                                .executesPlayer((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    bot.attack();
                                }))
                        .then(new LiteralArgument("bow")
                                .executesPlayer((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemStack(Material.BOW));
                                    inv.addItem(new ItemStack(Material.ARROW, 64));
                                    platform.addBot(bot);
                                    bot.interact(true);
                                    bot.getVanillaPlayer().getBukkitEntity().getScheduler().runDelayed(BladePlugin.this, task -> {
                                        bot.interact(false);
                                    }, null, 40L);
                                })));
    }
}
