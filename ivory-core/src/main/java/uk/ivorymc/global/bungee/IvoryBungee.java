package uk.ivorymc.global.bungee;

import community.leaf.textchain.bungeecord.BungeeTextChainSource;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import pl.tlinkowski.annotation.basic.NullOr;
import uk.ivorymc.api.playerdata.BungeePlayerData;
import uk.ivorymc.api.storage.SQLController;
import uk.ivorymc.api.storage.YamlFile;
import uk.ivorymc.global.bungee.commands.ListCommand;
import uk.ivorymc.global.bungee.commands.LogCommand;
import uk.ivorymc.global.bungee.commands.chat.MailCommand;
import uk.ivorymc.global.bungee.commands.chat.MsgCommand;
import uk.ivorymc.global.bungee.commands.chat.ReplyCommand;
import uk.ivorymc.global.bungee.listeners.ChatListener;
import uk.ivorymc.global.bungee.listeners.JoinListener;
import uk.ivorymc.global.bungee.listeners.QuitListener;
import uk.ivorymc.global.bungee.logging.LogFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IvoryBungee extends Plugin implements BungeeTextChainSource
{
    private @NullOr BungeeAudiences audiences;
    private ConfigFile configFile;
    private SQLController sqlController;

    private LogFile globalChatLog;
    private Map<String, LogFile> playerChatLogs;
    private Map<String, BungeePlayerData> playerDataMap;
    private Map<String, String> replyMap;

    @Override
    public void onEnable()
    {
        this.audiences = BungeeAudiences.create(this);

        // get config
        configFile = new ConfigFile(this, Path.of(this.getDataFolder().getAbsolutePath()), "config.yml");
        // ensure config values exist
        configFile.saveDefaults();

        // get db config values
        String db_name = configFile.getConfig().getString("mysql.database");
        String host = configFile.getConfig().getString("mysql.host");
        int port = configFile.getConfig().getInt("mysql.port");
        String username = configFile.getConfig().getString("mysql.username");
        String password = configFile.getConfig().getString("mysql.password");
        // get db connection
        this.sqlController = new SQLController(db_name, host, port, username, password);
        // prepare tables
        createTables();

        // prepare other vars

        // local log files
        // globalChatLog = new LogFile(
        //     Path.of(getDataFolder().getAbsolutePath(), "logs", "chat"), "global_chat.log", this
        // );
        // playerChatLogs = new HashMap<>();

        // player data
        playerDataMap = new HashMap<>();
        // replies
        replyMap = new HashMap<>();

        register();
    }

    @Override
    public void onDisable()
    {
        if (this.audiences != null)
        {
            this.audiences.close();
            this.audiences = null;
        }
    }

    @Override
    public @NotNull BungeeAudiences adventure()
    {
        if (this.audiences != null) { return this.audiences; }
        throw new IllegalStateException("Audiences not initialized (plugin is disabled).");
    }

    private void createTables()
    {
        // create chat log table
        sqlController.createTable(
            "chat_logs",
            "id INT NOT NULL AUTO_INCREMENT",
            "player_uuid BINARY(16) NOT NULL",
            "date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "message TEXT NOT NULL",
            "PRIMARY KEY (id)"
        );
        // create command log table
        sqlController.createTable(
            "command_logs",
            "id INT NOT NULL AUTO_INCREMENT",
            "player_uuid BINARY(16) NOT NULL",
            "date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "message TEXT NOT NULL",
            "PRIMARY KEY (id)"
        );
    }

    private void register()
    {
        // register events
        register(new ChatListener(this));
        register(new JoinListener(this));
        register(new QuitListener(this));
        // register commands
        register(new ListCommand(this));
        register(new LogCommand(this));
        register(new MailCommand(this));
        register(new MsgCommand(this));
        register(new ReplyCommand(this));
    }

    private void register(Object object)
    {
        if (object instanceof Listener listener)
        {
            getProxy().getPluginManager().registerListener(this, listener);
        }
        if (object instanceof Command command)
        {
            getProxy().getPluginManager().registerCommand(this, command);
        }
    }

    public void async(Runnable runnable)
    {
        getProxy().getScheduler().runAsync(this, runnable);
    }

    public SQLController getSqlController() { return sqlController; }

    public LogFile getGlobalChatLog()
    {
        return globalChatLog;
    }

    public LogFile getPlayerChatLog(ProxiedPlayer player)
    {
        String UUID = player.getUniqueId().toString();

        if (!playerChatLogs.containsKey(UUID))
        {
            playerChatLogs.put(UUID, new LogFile(
                Path.of(getDataFolder().getAbsolutePath(), "logs", "chat", "players"),
                UUID + ".log",
                this
            ));
        }
        return playerChatLogs.get(UUID);
    }

    public BungeePlayerData getPlayerData(ProxiedPlayer player)
    {
        String UUID = player.getUniqueId().toString();
        if (!playerDataMap.containsKey(UUID))
        {
            playerDataMap.put(UUID, new BungeePlayerData(Path.of(getDataFolder().getAbsolutePath(), "playerdata"), player, this));
        }
        return playerDataMap.get(UUID);
    }

    public void removeReply(ProxiedPlayer player)
    {
        replyMap.remove(player.getUniqueId().toString());
    }

    public void addReply(ProxiedPlayer p1, ProxiedPlayer p2)
    {
        replyMap.put(p1.getUniqueId().toString(), p2.getUniqueId().toString());
    }

    public Optional<String> getReply(ProxiedPlayer player)
    {
        Optional<String> reply = Optional.empty();
        String UUID = player.getUniqueId().toString();
        if (replyMap.containsKey(UUID))
        {
            reply = Optional.of(replyMap.get(UUID));
        }
        return reply;
    }
}
