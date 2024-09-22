package blade.utils.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.ExecutionCommandSource;

@FunctionalInterface
public interface CommandFunction<S extends ExecutionCommandSource<S>, R> {
    R get(CommandContext<S> ctx) throws CommandSyntaxException;
}
