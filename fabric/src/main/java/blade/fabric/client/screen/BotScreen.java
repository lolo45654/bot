package blade.fabric.client.screen;

import blade.BladeMachine;
import blade.Bot;
import blade.debug.DebugFrame;
import blade.debug.planner.ScorePlannerDebug;
import blade.planner.score.ScorePlanner;
import blade.util.blade.BladeAction;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
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
    private BotTab[] tabs;

    public BotScreen(Bot bot) {
        super(GameNarrator.NO_TITLE);
        this.bot = bot;
    }

    @Override
    protected void init() {
        tabs = new BotTab[] { new ActionsTab(bot) };
        tabNavigationBar = addRenderableWidget(TabNavigationBar.builder(tabManager, width).addTabs(tabs).build());
        tabNavigationBar.selectTab(0, false);
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
        return false;
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
            ScorePlannerDebug planner = frame.getPlanner();
            int color = -1;
            if (planner.getActionTaken() == action) color = YELLOW;
            if (!planner.getScores().containsKey(action)) color = RED;
            List<FormattedCharSequence> label = font.split(Component.literal(action.toString()), 175);
            if (!label.isEmpty()) {
                guiGraphics.drawString(font, label.getFirst(), k, j, color, false);
            }
            ScorePlanner.Score score = frame.getPlanner().getScores().get(action);
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
}