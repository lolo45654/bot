package me.loloed.bot.blade.impl.utils;

import io.papermc.paper.adventure.AdventureComponent;
import me.loloed.bot.api.blade.BladePlannedAction;
import me.loloed.bot.api.blade.debug.*;
import me.loloed.bot.api.blade.debug.planner.ScoreDebug;
import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.blade.planner.astar.node.AStarNode;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.blade.state.value.FixedValue;
import me.loloed.bot.api.blade.state.value.StateValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.advancements.*;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.builder.SkullBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.util.Point2D;
import xyz.xenondevs.invui.util.SlotUtils;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DebugGui {
    public static final ItemProvider BACKGROUND = new ItemWrapper(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));

    private static Gui prepareGui(Player player, Runnable back) {
        Gui gui = Gui.empty(9, 6);
        gui.setBackground(BACKGROUND);

        if (back == null) {
            gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.BARRIER)
                    .setDisplayName(wrapMiniMessage("<red>Close")), click -> player.closeInventory()));
        } else {
            gui.setItem(4, 5, new SimpleItem(new ItemBuilder(Material.OAK_DOOR)
                    .setDisplayName(wrapMiniMessage("<red>Go Back")), click -> back.run()
            ));
        }
        return gui;
    }

    private static void window(Gui gui, Player player, String... path) {
        Component title = Component.join(JoinConfiguration.separator(Component.text(" 🢂 ", NamedTextColor.DARK_GRAY)), Arrays.stream(path)
                .map(text -> MiniMessage.miniMessage().deserialize(text).color(NamedTextColor.DARK_GRAY))
                .collect(Collectors.toList()));
        Window.single()
                .setGui(gui)
                .setTitle(new AdventureComponentWrapper(title))
                .build(player).open();
    }

    private static PagedGui<Item> pagedGui(Gui gui) {
        PagedGui<Item> pagedGui = PagedGui.ofItems(new Structure(
                "# # # # # # # # ↑",
                "# # # # # # # # .",
                "# # # # # # # # .",
                "# # # # # # # # .",
                "# # # # # # # # ↓"
        )
                .addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('.', BACKGROUND)
                .addIngredient('↑', new BackItem())
                .addIngredient('↓', new ForwardItem()), List.of());

        pagedGui.setBackground(ItemProvider.EMPTY);
        for (int i = 0; i < pagedGui.getSize(); i++) {
            Point2D slot = SlotUtils.convertFromIndex(i, gui.getWidth());
            gui.setSlotElement(slot.getX(), slot.getY(), new SlotElement.LinkedSlotElement(pagedGui, i));
        }
        return pagedGui;
    }

    private static ComponentWrapper wrapMiniMessage(String miniMessage) {
        return new AdventureComponentWrapper(MiniMessage.miniMessage().deserialize(miniMessage));
    }

    public static void showMain(Player player, BladeDebug report) {
        Gui gui = prepareGui(player, null);

        gui.setItem(4, 2, new SimpleItem(new ItemBuilder(Material.CLOCK)
                .setDisplayName(wrapMiniMessage("<#9d8aff>Frames"))
                .addLoreLines(wrapMiniMessage(""))
                .addLoreLines(wrapMiniMessage("<yellow>Click to view!"))
                , click -> showAllFrames(player, report, null, 0, () -> showMain(player, report))));
        window(gui, player);
    }

    public static void showAllFrames(Player player, BladeDebug report, Range range, int page, Runnable back) {
        Gui gui = prepareGui(player, back);

        PagedGui<Item> pagedGui = pagedGui(gui);
        List<Item> content = new ArrayList<>();
        List<DebugFrame> frames = report.getFrames();
        for (int i = 0; i < frames.size(); i++) {
            if (range != null && (i < range.from || i > range.to)) continue;
            DebugFrame frame = frames.get(i);
            List<ComponentWrapper> errorLore = new ArrayList<>();
            List<ReportError> errors = frame.getErrors();
            if (!errors.isEmpty()) {
                errorLore.add(wrapMiniMessage("<red>Found " + errors.size() + " errors:"));
                for (ReportError error : errors) {
                    errorLore.add(wrapMiniMessage("<red>⏺ " + error.getMessage()));
                }
                errorLore.addFirst(wrapMiniMessage(""));
            }
            List<ComponentWrapper> actionLore = new ArrayList<>();
            boolean actionTaken = false;
            DebugPlanner planner = frame.getPlanner();
            StringBuilder steps = new StringBuilder();
            while (planner != null) {
                BladePlannedAction<?> plan = planner.getAction();
                if (plan != null && plan.action() != null) {
                    actionTaken = true;
                    String actionName = plan.action().getClass().getSimpleName();
                    actionLore.add(wrapMiniMessage("<#9d8aff>⮞ " + steps + actionName));
                    steps.append("⤷ ");
                }
                planner = planner.getChildren();
            }
            if (actionLore.isEmpty()) {
                actionLore.add(wrapMiniMessage("<red>No action taken!"));
            } else {
                actionLore.addFirst(wrapMiniMessage("<#9d8aff>Action taken:"));
            }
            actionLore.addFirst(wrapMiniMessage(""));

            int finalI = i;
            content.add(new SimpleItem(new ItemBuilder(
                    errors.isEmpty() && !actionTaken ? Material.BOOK : errors.isEmpty() ? Material.WRITTEN_BOOK : Material.KNOWLEDGE_BOOK)
                    .setDisplayName(wrapMiniMessage((errors.isEmpty() ? "" : "<red><bold>ERROR! <reset>") + "<#9d8aff>Frame " + i))
                    .addLoreLines(errorLore)
                    .addLoreLines(actionLore)
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<yellow>Click to inspect!"))
                    , click -> showFrame(player, frame, finalI, () -> showAllFrames(player, report, range, pagedGui.getCurrentPage(), back))));
        }
        pagedGui.setContent(content);
        pagedGui.setPage(page);

        window(gui, player, "Frames");
    }

    public static void showFrame(Player player, DebugFrame frame, int index, Runnable back) {
        Gui gui = prepareGui(player, back);

        gui.setItem(2, 2, new SimpleItem(new ItemBuilder(Material.PAINTING)
                .setDisplayName(wrapMiniMessage("<#9d8aff>Errors"))
                .addLoreLines(wrapMiniMessage("<bold><red>COMING SOON"))
                .addLoreLines(wrapMiniMessage(""))
                .addLoreLines(wrapMiniMessage(""))
                .addLoreLines(frame.getErrors().isEmpty() ? wrapMiniMessage("<red>No errors occurred!") : wrapMiniMessage("<yellow>Click to inspect!"))
                , click -> {
            if (frame.getErrors().isEmpty()) return;
            // showErrors(player, frame.getState(), "Frame " + index, () -> showFrame(player, frame, index, back))
        }));

        gui.setItem(4, 2, new SimpleItem(new ItemBuilder(Material.OAK_SAPLING)
                .setDisplayName(wrapMiniMessage("<#9d8aff>Planning"))
                .addLoreLines(wrapMiniMessage(""))
                .addLoreLines(wrapMiniMessage("<yellow>Click to inspect!"))
                , click -> showPlanning(player, frame.getPlanner(), List.of("Frame " + index), () -> showFrame(player, frame, index, back))));

        gui.setItem(6, 2, new SimpleItem(new ItemBuilder(Material.PAINTING)
                .setDisplayName(wrapMiniMessage("<#9d8aff>State"))
                .addLoreLines(wrapMiniMessage(""))
                .addLoreLines(wrapMiniMessage("<yellow>Click to inspect!"))
                , click -> showState(player, frame.getState(), "Frame " + index, () -> showFrame(player, frame, index, back))));

        window(gui, player, "Frame " + index);
    }

    private static void showPlanning(Player player, DebugPlanner planner, List<String> path, Runnable back) {
        Gui gui = prepareGui(player, back);

        if (planner == null) {
            gui.setItem(4, 2, new SimpleItem(new ItemBuilder(Material.BARRIER)
                    .setDisplayName(wrapMiniMessage("<bold><red>ERROR!"))));
            window(gui, player, "ERROR");
            return;
        }

        if (planner instanceof ScoreDebug score) {
            gui.setItem(3, 2, new SimpleItem(new ItemBuilder(Material.OAK_SAPLING)
                    .setDisplayName(wrapMiniMessage("<#9d8aff>Visualize Weights"))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<gray>You want to see the weighted"))
                    .addLoreLines(wrapMiniMessage("<gray>randomness with temperature?"))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<red>Not available yet!"))
                    , click -> {

            }));
            gui.setItem(5, 2, new SimpleItem(new ItemBuilder(Material.OAK_SAPLING)
                    .setDisplayName(wrapMiniMessage("<#9d8aff>Visualize Score"))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<gray>You see, you feel, you score."))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<red>Not available yet!"))
                    , click -> {

            }));
        } else if (planner instanceof AStarDebug aStar) {
            gui.setItem(4, 2, new SimpleItem(new ItemBuilder(Material.OAK_SAPLING)
                    .setDisplayName(wrapMiniMessage("<#9d8aff>Visualize Thoughts"))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<gray>This will create a new tab in"))
                    .addLoreLines(wrapMiniMessage("<gray>the advancements menu."))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<red>Press <key:key.advancements> or ESC after."))
                    .addLoreLines(wrapMiniMessage("<yellow>Click to see!"))
                    , click -> {
                Supplier<ResourceLocation> nextResource = () -> new ResourceLocation("blade", UUID.randomUUID().toString());
                Map<AStarNode, NodeConclusion> conclusions = aStar.getConclusions();
                Map<AStarNode, AdvancementHolder> createdNodes = new HashMap<>();
                Function<AStarNode, Advancement.Builder> nodeCreator = node -> {
                    Advancement.Builder builder = new Advancement.Builder();
                    NodeConclusion conclusion = conclusions.get(node);
                    List<String> lore = new ArrayList<>();
                    lore.add("<#9d8aff>Node Type 🠚 <#ff61a0>" + node.getClass().getSimpleName());
                    if (node.action != null) {
                        AStarAction action = node.action;
                        lore.add("<#9d8aff>Action Class 🠚 <#ff61a0>" + action.getClass().getName());
                        lore.add("<#9d8aff>Action as String 🠚 <#ff61a0>" + action);
                        lore.add("<#9d8aff>Action Cost 🠚 <#ff61a0>" + node.costG);
                    }
                    lore.add("<#9d8aff>Cost 🠚 <#ff61a0>" + node.cost);
                    lore.add("<#9d8aff>Conclusion 🠚 <#ff61a0>" + conclusion);
                    lore.add("<#9d8aff>Expected State:");
                    for (Map.Entry<StateKey<?>, StateValue<?>> entry : node.entryState.getStates().entrySet()) {
                        lore.add("<#9d8aff>  " + entry.getKey().getName() + " 🠚 <#ff61a0>" + entry.getValue().toString());
                    }
                    lore.add("");
                    lore.add("<#9d8aff>Result State:");
                    Map<StateKey<?>, StateValue<?>> resultState = node.resultState.getStates();
                    if (node.action != null) resultState = node.action.getResults().getStates();
                    for (Map.Entry<StateKey<?>, StateValue<?>> entry : resultState.entrySet()) {
                        lore.add("<#9d8aff>  " + entry.getKey().getName() + " 🠚 <#ff61a0>" + entry.getValue().toString());
                    }

                    Material itemDisplay = Material.LIME_WOOL;
                    if (conclusion == NodeConclusion.START) itemDisplay = Material.GREEN_WOOL;
                    if (conclusion == NodeConclusion.GOAL) itemDisplay = Material.BLUE_WOOL;
                    if (conclusion == NodeConclusion.DEAD_END) itemDisplay = Material.BLACK_WOOL;
                    if (conclusion == NodeConclusion.REACHED_GOAL) itemDisplay = Material.LIGHT_BLUE_WOOL;
                    if (conclusion == NodeConclusion.UNKNOWN) itemDisplay = Material.WHITE_WOOL;
                    builder.display(new DisplayInfo(
                            new net.minecraft.world.item.ItemStack(CraftItemType.bukkitToMinecraft(itemDisplay)),
                            net.minecraft.network.chat.Component.literal((conclusion == NodeConclusion.START ? "Start" :
                                    conclusion == NodeConclusion.GOAL ? "Goal" : node.action != null ? ("Action: " + node.action.getClass().getSimpleName()) : "Unknown") + " ".repeat(50)),
                            new AdventureComponent(MiniMessage.miniMessage().deserialize(String.join("<newline>", lore))),
                            Optional.empty(),
                            AdvancementType.TASK, false, false, false
                    ));
                    return builder;
                };

                AdvancementHolder root = new Advancement.Builder()
                        .display(
                                CraftItemStack.asNMSCopy(new ItemBuilder(Material.PLAYER_HEAD).get()),
                                net.minecraft.network.chat.Component.literal("Blade Bot"),
                                net.minecraft.network.chat.Component.empty(),
                                new ResourceLocation("minecraft", "block/sand"), AdvancementType.TASK,
                                false, false, false
                        )
                        .build(nextResource.get());

                List<AStarNode> openNodes = new ArrayList<>(conclusions.keySet());
                List<AdvancementHolder> nodes = new ArrayList<>();
                AdvancementNode rootAdvNode = new AdvancementNode(root, null);
                Map<AdvancementHolder, AdvancementNode> holderToNodes = new HashMap<>();
                holderToNodes.put(root, rootAdvNode);
                nodes.add(root);
                while (!openNodes.isEmpty()) {
                    AStarNode node = openNodes.removeFirst();
                    AdvancementHolder parent = node.parent == null || conclusions.get(node) == NodeConclusion.START ? root : createdNodes.get(node.parent);
                    if (node.parent != null && parent == null && openNodes.isEmpty()) parent = root;
                    if (node.parent != null && parent == null) {
                        openNodes.addLast(node);
                        continue;
                    }
                    ResourceLocation id = nextResource.get();
                    Advancement.Builder builder = nodeCreator.apply(node);
                    if (parent != null) {
                        builder.parent(parent);
                    }
                    AdvancementHolder advancement = builder.build(id);
                    createdNodes.put(node, advancement);
                    nodes.add(advancement);
                    AdvancementNode advParent = holderToNodes.get(parent);
                    AdvancementNode advNode = new AdvancementNode(advancement, advParent);
                    advParent.addChild(advNode);
                    holderToNodes.put(advancement, advNode);
                }

                TreeNodePosition.run(rootAdvNode);
                ((CraftPlayer) player).getHandle().connection.send(new ClientboundUpdateAdvancementsPacket(false, nodes, Set.of(), Map.of()));
                player.closeInventory();
            }));
        }

        List<String> path0 = new ArrayList<>(path);
        path0.add(planner.getClass().getSimpleName());

        DebugPlanner children = planner.getChildren();
        if (children != null) {
            gui.setItem(2, 0, new SimpleItem(new ItemBuilder(Material.FIREWORK_ROCKET)
                    .setDisplayName(wrapMiniMessage("<#9d8aff>Children"))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<gray>Go to the children of this"))
                    .addLoreLines(wrapMiniMessage("<gray>awesome planner."))
                    .addLoreLines(wrapMiniMessage(""))
                    .addLoreLines(wrapMiniMessage("<yellow>Click to traverse!"))
                    , click -> showPlanning(player, children, path0, () -> showPlanning(player, planner, path, back))));
        }

        window(gui, player, path0.toArray(String[]::new));
    }

    public static void showState(Player player, BladeState state, String frameName, Runnable back) {
        Gui gui = prepareGui(player, back);

        if (state == null) {
            gui.setItem(4, 2, new SimpleItem(new ItemBuilder(Material.BARRIER)
                    .setDisplayName(wrapMiniMessage("<bold><red>ERROR!"))));
            window(gui, player, "ERROR");
            return;
        }

        PagedGui<Item> pagedGui = pagedGui(gui);

        List<Item> content = new ArrayList<>();
        Map<StateKey<?>, StateValue<?>> states = state.getStates();
        for (Map.Entry<StateKey<?>, StateValue<?>> entry : states.entrySet()) {
            StateKey<?> key = entry.getKey();
            StateValue<?> value = entry.getValue();
            Class<?> typeClass = key.getTypeClass();

            List<ComponentWrapper> lore = new ArrayList<>();
            lore.add(wrapMiniMessage(""));
            lore.add(wrapMiniMessage("<#6ed1ff>Key:"));
            lore.add(wrapMiniMessage("<#6ed1ff>  Name 🠚 <#ff61a0>" + key.getName()));
            lore.add(wrapMiniMessage("<#6ed1ff>  Type 🠚 " + typeClass.getSimpleName()));
            lore.add(wrapMiniMessage("<#6ed1ff>  Holding Type 🠚 " + key.getClass().getSimpleName()));
            if (key instanceof ProducingKey<?> producingKey) {
                lore.add(wrapMiniMessage("<#6ed1ff>  Producer 🠚 " + producingKey.getProducer().getClass().getSimpleName()));
            }
            lore.add(wrapMiniMessage(""));
            lore.add(wrapMiniMessage("<#6ed1ff>Value:"));
            lore.add(wrapMiniMessage("<#6ed1ff>  Stored 🠚 <#ff61a0>" + value.toString()));
            Material type = Material.PAPER;
            if (typeClass == Boolean.class && value instanceof FixedValue<?> fixedValue) type = ((boolean) fixedValue.getValue()) ? Material.SOUL_TORCH : Material.LEVER;
            if (Number.class.isAssignableFrom(typeClass)) type = Material.POTION;
            content.add(new SimpleItem(new ItemBuilder(type).setDisplayName(wrapMiniMessage("<#9d8aff>State")).addLoreLines(lore)));
        }
        pagedGui.setContent(content);

        window(gui, player, frameName, "State");
    }

    public static class BackItem extends PageItem {

        public BackItem() {
            super(false);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> pagedGui) {
            if (pagedGui.getCurrentPage() == 0) return BACKGROUND;
            return new SkullBuilder(new SkullBuilder.HeadTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY1ZDVmMmViMGFkYTYyNDNlNDZjZDEwYmY2MDE2YTg1YmMxYjFmYzBhMzRlZTdhOWY4YjViMTc0YTQ0OWQifX19"))
                    .setDisplayName(wrapMiniMessage("<yellow>Previous"));
        }
    }

    public static class ForwardItem extends PageItem {

        public ForwardItem() {
            super(true);
        }

        @Override
        public ItemProvider getItemProvider(PagedGui<?> pagedGui) {
            if (pagedGui.getCurrentPage() == pagedGui.getPageAmount() - 1) return BACKGROUND;
            return new SkullBuilder(new SkullBuilder.HeadTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNhOWRjNjZhNTQ5NjJmNGVkMDRiMjQ1NWFhOTk0YjQ4NDg4OTZlYTZjZTlkYzVlNDUwY2NkY2Y5NWY2YjUyYyJ9fX0="))
                    .setDisplayName(wrapMiniMessage("<yellow>Next"));
        }
    }

    public static record Range(int from, int to) {
    }
}
