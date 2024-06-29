package blade.fabric.client;

import blade.BladeMachine;
import blade.Bot;
import blade.debug.DebugFrame;
import blade.debug.planner.ScoreDebug;
import blade.planner.score.ScoreAction;
import blade.planner.score.ScorePlanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BotDebugOverlay {
    public static List<String> getLeft() {
        List<String> lines = new ArrayList<>();
        Bot bot = BotClientMod.PLATFORM.getBot();
        if (bot == null) return lines;
        Minecraft client = Minecraft.getInstance();
        BladeMachine blade = bot.getBlade();
        lines.add("");
        lines.add("[BOT]");
        lines.add(String.join(" ", computeKeys(client.options)));
        DebugFrame frame = blade.getLastFrame();
        if (frame != null) {

            ScoreDebug planner = frame.getPlanner();
            ScoreAction actionTaken = planner.getActionTaken();
            Map<ScoreAction, ScorePlanner.Score> scores = planner.getScores();
            ScorePlanner.Score defScore = new ScorePlanner.Score(-1, -1, -1, true);
            ScorePlanner.Score score = scores.getOrDefault(actionTaken, defScore);
            lines.add("A: " + actionTaken + " " + scoreToString(score) + " E: " + frame.getErrors().size() + " T: " + planner.getTemperature());

            int additionalActions = 4;
            List<ScoreAction> sortedActions = new ArrayList<>(scores.keySet());
            sortedActions.sort(Comparator.<ScoreAction>comparingDouble(action -> scores.getOrDefault(action, defScore).weight()).reversed());
            for (int i = 0; i < sortedActions.size(); i++) {
                if (i > additionalActions) break;
                ScoreAction action = sortedActions.get(i);
                lines.add("A" + (i + 1) + ": " + action + " " + scoreToString(scores.getOrDefault(action, defScore)));
            }
        }
        return lines;
    }

    private static @NotNull List<String> computeKeys(Options options) {
        List<String> keys = new ArrayList<>();
        if (options.keyUp.isDown()) keys.add("W");
        if (options.keyLeft.isDown()) keys.add("A");
        if (options.keyDown.isDown()) keys.add("S");
        if (options.keyRight.isDown()) keys.add("D");
        if (options.keyShift.isDown()) keys.add("SHFT");
        if (options.keySprint.isDown() || Minecraft.getInstance().player.isSprinting()) keys.add("SPRNT");
        if (options.keyAttack.isDown()) keys.add("ATCK");
        if (options.keyUse.isDown()) keys.add("INRCT");
        return keys;
    }

    public static List<String> getRight() {
        List<String> lines = new ArrayList<>();
        return lines;
    }

    public static String scoreToString(ScorePlanner.Score score) {
        return String.format(Locale.ROOT, "S: %.3f SG: %.3f W: %.3f C: %s", score.score(), score.scoreWithGoal(), score.weight(), score.satisfied());
    }
}
