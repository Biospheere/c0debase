package de.c0debase.bot.listener.other;

import de.c0debase.bot.CodebaseBot;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.reflections.Reflections;

import java.util.Set;

/**
 * @author Biosphere
 * @date 23.01.18
 */

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);

        event.getJDA().getGuilds().get(0).getMembers().forEach(member -> {
            if (!member.getUser().isBot()) {
                CodebaseBot.getInstance().getLevelManager().load(member.getUser().getId());
                if (CodebaseBot.getInstance().getLevelManager().getLevelUser(member.getUser().getId()).getLevel() >= 3 && !member.getGuild().getRolesByName("Projekt", true).isEmpty()) {
                    Role role = member.getGuild().getRolesByName("Projekt", true).get(0);
                    if(PermissionUtil.canInteract(member.getGuild().getSelfMember(), role)){
                        member.getGuild().getController().addRolesToMember(member, role).queue();
                    }
                }
            }
        });

        Set<Class<? extends ListenerAdapter>> classes = new Reflections("de.c0debase.bot.listener").getSubTypesOf(ListenerAdapter.class);
        classes.forEach(listenerClass -> {
            if (!listenerClass.getName().equals(this.getClass().getName())) {
                try {
                    event.getJDA().addEventListener(listenerClass.newInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        CodebaseBot.getInstance().getLevelManager().startInviteChecker();
    }

}