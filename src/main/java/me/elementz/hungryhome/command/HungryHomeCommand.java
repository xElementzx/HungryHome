package me.elementz.hungryhome.command;

import me.elementz.hungryhome.HungryHome;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class HungryHomeCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String argument = args.<String>getOne(Text.of("arguments")).orElse(null);

        if (StringUtils.isNotBlank(argument)) {
            if (StringUtils.equalsIgnoreCase(argument, "reload") && src.hasPermission("hungryhome.reload.base")) {
                HungryHome.getInstance().configuration.loadConfiguration();
                src.sendMessage(Text.of(TextColors.GREEN, "Configuration reloaded"));
                return CommandResult.success();
            }

            src.sendMessage(Text.of(TextColors.DARK_RED, "Invalid arguments"));
            return CommandResult.empty();
        }

        src.sendMessage(Text.of(TextColors.GREEN, "Created by ", TextColors.AQUA, "xElementzx"));
        return CommandResult.success();
    }
}