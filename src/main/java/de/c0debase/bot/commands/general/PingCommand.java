package de.c0debase.bot.commands.general;

import de.c0debase.bot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Zeigt dir den Ping des Bots zu Discord", Category.GENERAL);
    }

    @Override
    public void execute(final String[] args, final Message message) {
        final EmbedBuilder embedBuilder = getEmbed(message.getMember());
        embedBuilder.appendDescription(":stopwatch: " + message.getJDA().getGatewayPing() + " (Websocket)\n\n");
        embedBuilder.appendDescription(":stopwatch: " + message.getJDA().getRestPing().complete() + " (Rest)");
        message.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }
}
