package de.c0debase.bot;

import de.c0debase.bot.commands.CommandManager;
import de.c0debase.bot.database.Database;
import de.c0debase.bot.database.mongodb.MongoDatabase;
import de.c0debase.bot.listener.guild.GuildMemberJoinListener;
import de.c0debase.bot.listener.guild.GuildMemberLeaveListener;
import de.c0debase.bot.listener.guild.GuildMemberNickChangeListener;
import de.c0debase.bot.listener.guild.GuildMemberRoleListener;
import de.c0debase.bot.listener.message.MessageReactionListener;
import de.c0debase.bot.listener.message.MessageReceiveListener;
import de.c0debase.bot.listener.message.TableFlipListener;
import de.c0debase.bot.listener.other.GuildReadyListener;
import de.c0debase.bot.listener.voice.GuildVoiceListener;
import de.c0debase.bot.pagination.PaginationManager;
import de.c0debase.bot.tempchannel.Tempchannel;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Codebase {

    private static final Logger logger = LoggerFactory.getLogger(Codebase.class);

    private final JDA jda;
    private final Database database;
    private final CommandManager commandManager;
    private final PaginationManager paginationManager;
    private final Map<String, Tempchannel> tempchannels;

    public Codebase() throws Exception {
        final long startTime = System.currentTimeMillis();

        logger.info("Starting c0debase");
        tempchannels = new HashMap<>();

        database = initializeDataManager();
        logger.info("Database-Connection set up!");

        jda = initializeJDA();
        logger.info("JDA set up!");

        paginationManager = new PaginationManager(this);
        logger.info("Pagination-Manager set up!");

        commandManager = new CommandManager(this);
        logger.info("Command-Manager set up!");

        new GuildVoiceListener(this);

        new MessageReactionListener(this);
        new MessageReceiveListener(this);
        new TableFlipListener(this);

        new GuildMemberJoinListener(this);
        new GuildMemberLeaveListener(this);
        new GuildMemberNickChangeListener(this);
        new GuildMemberRoleListener(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                database.close();
            } catch (final Exception exception) {
                exception.printStackTrace();
            }
            jda.shutdown();
        }));
        logger.info(String.format("Startup finished in %dms!", System.currentTimeMillis() - startTime));
    }

    /***
     * Connect to the database
     * 
     * @return The {@link Database} instance
     * @throws Exception
     */
    private Database initializeDataManager() throws Exception {
        try {
            return new MongoDatabase(System.getenv("MONGO_HOST") == null ? "localhost" : System.getenv("MONGO_HOST"),
                    System.getenv("MONGO_PORT") == null ? 27017 : Integer.valueOf(System.getenv("MONGO_PORT")));
        } catch (final Exception exception) {
            logger.error("Encountered exception while initializing Database-Connection!");
            throw exception;
        }
    }

    /**
     *
     * @return The {@link JDA} instance fot the current session
     * @throws Exception
     */
    private JDA initializeJDA() throws Exception {
        try {
            final JDABuilder jdaBuilder = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN"));
            jdaBuilder.setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
            jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
            jdaBuilder.setActivity(Activity.playing("auf c0debase"));
            jdaBuilder.addEventListeners(new GuildReadyListener(this));
            return jdaBuilder.build().awaitReady();
        } catch (Exception exception) {
            logger.error("Encountered exception while initializing JDA!");
            throw exception;
        }
    }

    public Database getDatabase() {
        return database;
    }

    public JDA getJDA() {
        return jda;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public PaginationManager getPaginationManager() {
        return paginationManager;
    }

    public Map<String, Tempchannel> getTempchannels() {
        return tempchannels;
    }

    public static void main(String... args) throws Exception {
        if (System.getenv("SENTRY_DSN") != null || System.getProperty("sentry.properties") != null) {
            Sentry.init();
        }
        try {
            new Codebase();
        } catch (Exception exception) {
            logger.error("Encountered exception while initializing the bot!", exception);
        }
    }
}
