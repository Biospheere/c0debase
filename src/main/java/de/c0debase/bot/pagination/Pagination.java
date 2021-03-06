package de.c0debase.bot.pagination;

import de.c0debase.bot.Codebase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.vdurmont.emoji.EmojiManager;

public abstract class Pagination {

    private Codebase bot = null;

    private String title;
    private int pageSize;

    public Pagination(String title, int pageSize) {
        this.title = title;
        this.pageSize = pageSize;
    }

    public Pagination(String title) {
        this(title, 10);
    }

    public void setInstance(final Codebase instance) {
        if (bot != null) {
            throw new IllegalStateException("Can only initialize once!");
        }
        bot = instance;
    }

    public void update(Message success, MessageEmbed messageEmbed, String emote) {
        int current = getCurrentPage(messageEmbed);
        if (emote.equalsIgnoreCase("arrow_left") && current == 1) {
            return;
        }
        final int max = getMaxPages(messageEmbed);
        final boolean descending = isDescending(messageEmbed);

        if (max != current) {
            if (emote.equalsIgnoreCase("arrow_right")) {
                current++;
            } else if (emote.equalsIgnoreCase("arrow_left") && current > 1) {
                current--;
            }
        } else if (emote.equalsIgnoreCase("arrow_left") && current > 1) {
            current--;
        }

        if (current > 0) {
            final EmbedBuilder embedBuilder = getEmbed(success.getGuild(), current, max, descending);
            buildList(embedBuilder, current, descending, success.getGuild());
            success.editMessage(embedBuilder.build()).queue();
        }
    }

    public void createFirst(boolean descending, TextChannel textChannel) {
        final EmbedBuilder embedBuilder = getEmbed(textChannel.getGuild(), descending);

        buildList(embedBuilder, 1, descending, textChannel.getGuild());

        textChannel.sendMessage(embedBuilder.build()).queue((Message success) -> {
            success.addReaction(EmojiManager.getForAlias("arrow_left").getUnicode()).queue();
            success.addReaction(EmojiManager.getForAlias("arrow_right").getUnicode()).queue();
        });
    }

    public abstract void buildList(EmbedBuilder embedBuilder, int page, boolean descending, Guild guild);

    public Codebase getBot() {
        return bot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage(final MessageEmbed messageEmbed) {
        return Integer.parseInt(splitFooter(messageEmbed.getFooter().getText())[0]);
    }

    public Integer getMaxPages(final MessageEmbed messageEmbed) {
        return Integer.parseInt(splitFooter(messageEmbed.getFooter().getText())[1]);
    }

    public boolean isDescending(final MessageEmbed messageEmbed) {
        return splitFooter(messageEmbed.getFooter().getText())[2].equalsIgnoreCase("absteigend");
    }

    public static boolean isDescending(final String... args) {
        boolean descending = false;
        if (args.length > 0 && (args[0].equalsIgnoreCase("desc") || args[0].equalsIgnoreCase("descending")
                || args[0].equalsIgnoreCase("absteigend"))) {
            descending = true;
        }
        return descending;
    }

    public EmbedBuilder getEmbed(final Guild guild) {
        return new EmbedBuilder().setTitle(getTitle()).setColor(guild.getSelfMember().getColor());
    }

    public EmbedBuilder getEmbed(final Guild guild, final Integer currentPage, final Integer maxPages,
            final boolean descending) {
        final EmbedBuilder embedBuilder = getEmbed(guild);
        embedBuilder.setFooter("Seite: (" + currentPage + "/" + maxPages + ") Sortierung: "
                + (descending ? "absteigend" : "aufsteigend"), guild.getIconUrl());
        return embedBuilder;
    }

    public EmbedBuilder getEmbed(final Guild guild, final boolean descending) {
        final EmbedBuilder embedBuilder = getEmbed(guild);
        embedBuilder.setFooter("Seite: (1/" + ((guild.getMembers().size() / getPageSize()) + 1) + ") Sortierung: "
                + (descending ? "absteigend" : "aufsteigend"), guild.getIconUrl());
        return embedBuilder;
    }

    public <T> Map<Integer, T> getPage(int page, List<T> list, boolean descending) {
        if (pageSize <= 0 || page <= 0) {
            throw new IllegalArgumentException("Invalid page size: " + pageSize);
        }

        final int fromIndex = (page - 1) * pageSize;
        if (list == null || list.size() < fromIndex) {
            return Collections.emptyMap();
        }

        HashMap<Integer, T> map = new LinkedHashMap<>();
        AtomicInteger count = new AtomicInteger(1);
        int listSize = list.size();
        list.subList(fromIndex, Math.min(fromIndex + pageSize, list.size())).forEach(entry -> {
            int number = fromIndex + count.get();
            if (!descending) {
                number = listSize - (fromIndex + count.get());
            }
            count.getAndIncrement();
            map.put(number, entry);
        });
        return map;
    }

    public <T> Map<Integer, T> getPage(int page, List<T> list) {
        return getPage(page, list, true);
    }

    private String[] splitFooter(final String footer) {
        return footer.replace("Seite: (", "").replace(")", "").replace(" Sortierung: ", "/").split("/");
    }
}