package de.c0debase.bot.listener.guild;

import de.c0debase.bot.core.Codebase;
import de.c0debase.bot.database.data.CodebaseUser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Biosphere
 * @date 23.01.18
 */
public class GuildMemberJoinListener extends ListenerAdapter {

    private final Codebase bot;

    public GuildMemberJoinListener(final Codebase bot) {
        this.bot = bot;
        bot.getJDA().addEventListener(this);
    }

    @Override
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setFooter("@" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator(), event.getMember().getUser().getEffectiveAvatarUrl());
        embedBuilder.setColor(event.getGuild().getSelfMember().getColor());
        embedBuilder.setThumbnail(event.getMember().getUser().getEffectiveAvatarUrl());
        embedBuilder.appendDescription("Willkommen auf c0debase " + event.getMember().getAsMention() + "\n");
        embedBuilder.appendDescription("— Weise dir eine Rolle mit !role zu\n");
        embedBuilder.appendDescription("— Schaue dir die Regeln in #rules an");


        event.getGuild().getTextChannelById(System.getenv("BOTCHANNEL")).sendMessage(embedBuilder.build()).queue();

        event.getGuild().getTextChannelsByName("log", true).forEach(channel -> {
            final EmbedBuilder logBuilder = new EmbedBuilder();
            logBuilder.setFooter("@" + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator(), event.getMember().getUser().getEffectiveAvatarUrl());
            logBuilder.setThumbnail(event.getMember().getUser().getEffectiveAvatarUrl());
            logBuilder.appendDescription("Erstelldatum: " + event.getUser().getCreationTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + "\n");
            logBuilder.appendDescription("Standart Avatar: " + (event.getMember().getUser().getAvatarUrl() == null) + "\n");
            channel.sendMessage(logBuilder.build()).queue();
        });

        final CodebaseUser codebaseUser = bot.getDataManager().getUserData(event.getGuild().getId(), event.getUser().getId());
        final List<Role> roles = new ArrayList<>();
        codebaseUser.getRoles().forEach(roleName -> {
            final Role role = event.getGuild().getRoleById(roleName);
            if (role != null && PermissionUtil.canInteract(event.getGuild().getSelfMember(), role)) {
                roles.add(role);
            }
        });
        event.getGuild().getController().addRolesToMember(event.getMember(), roles).reason("Autorole").queue();
    }
}
