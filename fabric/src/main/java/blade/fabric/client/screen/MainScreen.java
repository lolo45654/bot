package blade.fabric.client.screen;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class MainScreen extends Screen {
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    @Nullable
    private TabNavigationBar tabNavigationBar;

    public MainScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    protected void init() {
        tabNavigationBar = addRenderableWidget(TabNavigationBar.builder(tabManager, width).addTabs(new ActionsTab()).build());
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (tabNavigationBar != null) {
            tabNavigationBar.setWidth(width);
            tabNavigationBar.arrangeElements();
        }
    }

    static class ActionsTab extends GridLayoutTab {
        private static final Component TITLE = Component.literal("Actions");

        public ActionsTab() {
            super(TITLE);
            GridLayout.RowHelper rowHelper = this.layout.rowSpacing(8).createRowHelper(1);
        }
    }
}
