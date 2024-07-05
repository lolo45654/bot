package blade.fabric.client;

import blade.BladeMachine;
import blade.Bot;
import blade.debug.DebugFrame;
import blade.debug.planner.ScorePlannerDebug;
import blade.fabric.adapter.MinecraftAdapter;
import blade.planner.score.ScoreAction;
import blade.planner.score.ScorePlanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BotDebugOverlay {
    public static List<String> getLeft() {
        List<String> lines = new ArrayList<>();
        Bot bot = BotClientMod.PLATFORM.getBot();
        if (bot == null) return lines;
        Minecraft client = Minecraft.getInstance();
        BladeMachine blade = bot.getBlade();
        lines.add("");
        lines.add("[BOT]");
        lines.add(String.join(" ", computeKeys(client)));
        DebugFrame frame = blade.getLastFrame();
        if (frame != null) {
            ScorePlannerDebug planner = frame.getPlanner();
            ScoreAction actionTaken = planner.getActionTaken();
            Map<ScoreAction, ScorePlanner.Score> scores = planner.getScores();
            ScorePlanner.Score defScore = new ScorePlanner.Score(-1, -1, -1, true);
            ScorePlanner.Score score = scores.getOrDefault(actionTaken, defScore);
            lines.add("GOAL: " + blade.getGoal());
            lines.add("E: " + frame.getErrors().size() + " T: " + planner.getTemperature());
            lines.add("A: " + actionTaken + " " + score.toString());

            int additionalActions = 4;
            List<ScoreAction> sortedActions = new ArrayList<>(scores.keySet());
            sortedActions.sort(Comparator.<ScoreAction>comparingDouble(action -> scores.getOrDefault(action, defScore).weight()).reversed());
            for (int i = 0; i < sortedActions.size(); i++) {
                if (i > additionalActions) break;
                ScoreAction action = sortedActions.get(i);
                lines.add("A" + (i + 1) + ": " + action + " " + scores.getOrDefault(action, defScore).toString());
            }
        }
        return lines;
    }

    private static @NotNull List<String> computeKeys(Minecraft minecraft) {
        Options options = minecraft.options;
        MinecraftAdapter minecraftAdapter = (MinecraftAdapter) minecraft;
        List<String> keys = new ArrayList<>();
        if (options.keyUp.isDown()) keys.add("W");
        if (options.keyLeft.isDown()) keys.add("A");
        if (options.keyDown.isDown()) keys.add("S");
        if (options.keyRight.isDown()) keys.add("D");
        if (options.keyShift.isDown()) keys.add("SHFT");
        if (options.keyJump.isDown()) keys.add("JUMP");
        if (options.keySprint.isDown() || minecraft.player.isSprinting()) keys.add("SPRNT");
        if (options.keyAttack.isDown() || minecraftAdapter.bot$getLastAttackClicks() > 0) keys.add("ATCK");
        if (options.keyUse.isDown() || minecraftAdapter.bot$getLastInteractClicks() > 0) keys.add("USE");
        return keys;
    }

    public static List<String> getRight() {
        List<String> lines = new ArrayList<>();
        return lines;
    }
}
