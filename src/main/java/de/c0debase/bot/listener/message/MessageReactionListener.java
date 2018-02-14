package de.c0debase.bot.listener.message;

import com.vdurmont.emoji.EmojiManager;
import de.c0debase.bot.CodebaseBot;
import de.c0debase.bot.commands.Command;
import de.c0debase.bot.commands.Command.Categorie;
import de.c0debase.bot.level.LevelUser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.react.GenericPrivateMessageReactionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.awt.*;

/**
 * @author Biosphere
 * @date 23.01.18
 */
public class MessageReactionListener extends ListenerAdapter {

    @Override
    public void onGenericPrivateMessageReaction(GenericPrivateMessageReactionEvent event) {
        super.onGenericPrivateMessageReaction(event);
        if (event.getUser().isBot() || EmojiManager.getByUnicode(event.getReactionEmote().getName()) == null) {
            return;
        }
        event.getChannel().getMessageById(event.getMessageId()).queue((Message success) -> {
            String emote = EmojiManager.getByUnicode(event.getReactionEmote().getName()).getAliases().get(0);
            if (!success.getEmbeds().isEmpty() && success.getAuthor().isBot()) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.GREEN);
                if (emote.equalsIgnoreCase("wastebasket")) {
                    success.delete().queue();
                    return;
                }

                for (Categorie categorie : Categorie.values()) {
                    if (categorie.getEmote().equalsIgnoreCase(emote)) {
                        embedBuilder.setTitle(":question: " + categorie.getName() + " Commands Help");
                        for (Command command : CodebaseBot.getInstance().getCommandManager().getAvailableCommands()) {
                            if (command.getCategorie() == categorie) {
                                embedBuilder.appendDescription("**!" + command.getCommand() + "**\n" + command.getDescription() + "\n\n");
                            }
                        }
                        success.editMessage(embedBuilder.build()).queue();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onGenericGuildMessageReaction(GenericGuildMessageReactionEvent event) {
        super.onGenericGuildMessageReaction(event);
        if (event.getUser().isBot()) {
            return;
        }
        event.getChannel().getMessageById(event.getMessageId()).queue(success -> {
            if (event.getReactionEmote().getName().equalsIgnoreCase("wastebasket") && success.getAuthor().isBot()) {
                success.delete().queue();
                return;
            }
            if (!success.getEmbeds().isEmpty() && success.getAuthor().isBot()) {
                String emote = EmojiManager.getByUnicode(event.getReactionEmote().getName()).getAliases().get(0);
                MessageEmbed messageEmbed = success.getEmbeds().get(0);
                if (messageEmbed.getFooter().getText().contains("Seite")) {
                    CodebaseBot.getInstance().getLeaderboardPagination().updateList(CodebaseBot.getInstance().getLevelManager().getLevelUsersSorted());

                    String[] strings = messageEmbed.getFooter().getText().replace("Seite: (", "").replace(")", "").split("/");

                    int max = Integer.valueOf(strings[1]);
                    int current = Integer.valueOf(strings[0]);


                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setColor(success.getGuild().getSelfMember().getColor());
                    embedBuilder.setTitle("Leaderboard: " + event.getGuild().getName());

                    if (max != current) {
                        if (emote.equalsIgnoreCase("arrow_right")) {
                            current++;
                        } else if (emote.equalsIgnoreCase("arrow_left") && current > 1) {
                            current--;
                        }
                    } else if (emote.equalsIgnoreCase("arrow_left") && current >= 1) {
                        current--;
                    }
                    embedBuilder.setFooter("Seite: (" + current + "/" + max + ")", success.getGuild().getIconUrl());

                    int count = 1;

                    for (LevelUser levelUser : CodebaseBot.getInstance().getLeaderboardPagination().getPage(current)) {
                        Member member = success.getGuild().getMemberById(Long.valueOf(levelUser.getId()));
                        if (member != null) {
                            embedBuilder.appendDescription("`" + (current == 1 ? count : +((current - 1) * 10 + count)) + ")` " + member.getEffectiveName() + "#" + member.getUser().getDiscriminator() + " (Lvl." + levelUser.getLevel() + ")\n");
                            count++;
                        }
                    }
                    success.editMessage(embedBuilder.build()).queue();
                }
            }
        });
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        super.onGuildMessageReactionRemove(event);
        if (event.getMember().getUser().isBot()) {
            return;
        }
        event.getChannel().getMessageById(event.getMessageId()).queue(success -> {
            if (success.getTextChannel().getName().equalsIgnoreCase("rollen-freischalten")) {
                String emote = event.getReactionEmote().getName();
                if (!success.getGuild().getRolesByName(emote, true).isEmpty()) {
                    Role role = success.getGuild().getRolesByName(emote, true).get(0);
                    if (success.getGuild().getMembersWithRoles(role).contains(event.getMember())) {
                        success.getGuild().getController().removeRolesFromMember(event.getMember(), role).queue();
                    }
                }
            }
        });
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        super.onGuildMessageReactionAdd(event);
        event.getChannel().getMessageById(event.getMessageId()).queue(success -> {
            if (success.getTextChannel().getName().equalsIgnoreCase("rollen-freischalten")) {
                String emote = event.getReactionEmote().getName();
                if (!success.getGuild().getRolesByName(emote, true).isEmpty()) {
                    Role role = success.getGuild().getRolesByName(emote, true).get(0);
                    if (!success.getGuild().getMembersWithRoles(role).contains(event.getMember())) {
                        success.getGuild().getController().addRolesToMember(event.getMember(), role).queue();
                    }
                }
            }
        });
    }
}
