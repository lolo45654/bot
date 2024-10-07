package blade.fabric.client.screen;

import blade.BladeMachine;
import blade.Bot;
import blade.debug.DebugFrame;
import blade.debug.planner.ScorePlannerDebug;
import blade.impl.ConfigKeys;
import blade.planner.score.ScorePlanner;
import blade.utils.blade.BladeAction;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BotScreen extends Screen {
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    @Nullable
    private TabNavigationBar tabNavigationBar;
    private final Bot bot;
    private boolean pause = true;
    private BotTab[] tabs;

    public BotScreen(Bot bot) {
        super(GameNarrator.NO_TITLE);
        this.bot = bot;
    }

    @Override
    protected void init() {
        tabs = new BotTab[] { new SettingsTab(bot), new ActionsTab(bot) };
        tabNavigationBar = addRenderableWidget(TabNavigationBar.builder(tabManager, width).addTabs(tabs).build());
        tabNavigationBar.selectTab(0, false);

        Checkbox pause = addRenderableWidget(Checkbox.builder(Component.literal("Pause"), font)
                .pos(5, 5)
                .selected(true)
                .onValueChange((checkbox, v) -> this.pause = v)
                .maxWidth(60)
                .build());
        pause.setPosition(5, (24 - pause.getHeight()) / 2);

        Button destroy = addRenderableWidget(Button.builder(Component.literal("Destroy"), btn -> {
            bot.destroy();
            Minecraft.getInstance().setScreen(null);
        }).size(70, 20).build());
        destroy.setPosition(width - destroy.getWidth() - 5, (24 - destroy.getHeight()) / 2);

        repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (tabNavigationBar != null) {
            tabNavigationBar.setWidth(width);
            tabNavigationBar.arrangeElements();
        }
        if (tabs != null) {
            for (BotTab tab : tabs) {
                tab.repositionElements();
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return pause;
    }

    public abstract static class BotTab extends GridLayoutTab {
        public BotTab(Component component) {
            super(component);
        }

        public abstract void repositionElements();
    }

    public class ActionsTab extends BotTab {
        private static final Component TITLE = Component.literal("Actions");
        private final ActionsList actionsList;

        public ActionsTab(Bot bot) {
            super(TITLE);
            actionsList = new ActionsList(bot, BotScreen.this.width, BotScreen.this.height - 24, 0, 24);
            layout.addChild(actionsList, BotScreen.this.width, layout.getHeight());
        }

        @Override
        public void repositionElements() {
            layout.arrangeElements();
            actionsList.setSize(BotScreen.this.width, BotScreen.this.height - 24);
            actionsList.setPosition(0, 24);
        }
    }

    public class ActionsList extends ContainerObjectSelectionList<ActionEntry> {
        public ActionsList(Bot bot, int width, int height,  int x, int y) {
            super(Minecraft.getInstance(), width, height, x, y);
            BladeMachine blade = bot.getBlade();
            for (BladeAction action : blade.getActions()) {
                addEntry(new ActionEntry(blade, action));
            }
        }
    }

    public class ActionEntry extends ContainerObjectSelectionList.Entry<ActionEntry> {
        private static final int YELLOW = 0xfffc54;
        private static final int RED = 0xff5465;

        private final BladeMachine blade;
        private final BladeAction action;
        private final Font font = BotScreen.this.font;

        public ActionEntry(BladeMachine blade, BladeAction action) {
            this.blade = blade;
            this.action = action;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            DebugFrame frame = blade.getLastFrame();
            ScorePlannerDebug planner = frame.planner();
            if (planner == null) return;
            int color = -1;
            if (planner.action() == action) color = YELLOW;
            ScorePlanner.Score score = planner.scores().get(action);
            if (score == null || !score.satisfied()) color = RED;
            List<FormattedCharSequence> label = font.split(Component.literal(action.toString()), 175);
            if (!label.isEmpty()) {
                guiGraphics.drawString(font, label.getFirst(), k, j, color, false);
            }
            if (score != null) {
                guiGraphics.drawString(font, score.toString(), k, j + 10, color);
            }
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public @NotNull NarrationPriority narrationPriority() {
                    return NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, action.getClass().getSimpleName());
                }
            });
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }
    }

    public class SettingsTab extends BotTab {
        private static final Component TITLE = Component.literal("Settings");

        public SettingsTab(Bot bot) {
            super(TITLE);
            GridLayout.RowHelper rowHelper = layout.createRowHelper(2);
            layout.defaultCellSetting().padding(4, 4, 4, 0);
            rowHelper.addChild(new AbstractSliderButton(0, 0, 90, 20, Component.literal(String.format("Difficulty: %.1f", bot.getBlade().get(ConfigKeys.DIFFICULTY))), bot.getBlade().get(ConfigKeys.DIFFICULTY)) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(String.format("Difficulty: %.2f", value)));
                }

                @Override
                protected void applyValue() {
                    bot.getBlade().set(ConfigKeys.DIFFICULTY, (float) value);
                }
            });
            rowHelper.addChild(new AbstractSliderButton(0, 0, 90, 20, Component.literal(String.format("Temperature: %.1f", bot.getBlade().getPlanner().getTemperature())), bot.getBlade().getPlanner().getTemperature()) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(String.format("Temperature: %.2f", value)));
                }

                @Override
                protected void applyValue() {
                    bot.getBlade().getPlanner().setTemperature(value);
                }
            });
            rowHelper.addChild(CycleButton.booleanBuilder(Component.literal("On"), Component.literal("Off"))
                    .withInitialValue(bot.isDebug())
                    .create(0, 0, 90, 20, Component.literal("Debug"), (btn, v) -> bot.setDebug(v)));
            rowHelper.addChild(Button.builder(Component.literal("AI: " + (bot.getBlade().getAIManager().getState())), btn -> {
                bot.getBlade().getAIManager().setState(bot.getBlade().getAIManager().getState().next());
                btn.setMessage(Component.literal("AI: " + (bot.getBlade().getAIManager().getState())));
            }).size(90, 20).build());
            layout.arrangeElements();
            FrameLayout.alignInRectangle(layout, 0, 0, BotScreen.this.width, BotScreen.this.height, 0.5F, 0.25F);
        }

        @Override
        public void repositionElements() {
            layout.arrangeElements();
        }
    }
}
