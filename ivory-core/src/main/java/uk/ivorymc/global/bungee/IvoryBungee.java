package uk.ivorymc.global.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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
import uk.ivorymc.global.bungee.commands.ListCommand;
import uk.ivorymc.global.bungee.commands.LogCommand;
import uk.ivorymc.global.bungee.commands.chat.MailCommand;
import uk.ivorymc.global.bungee.commands.chat.MsgCommand;
import uk.ivorymc.global.bungee.commands.chat.ReplyCommand;
import uk.ivorymc.global.bungee.listeners.ChatListener;
import uk.ivorymc.global.bungee.listeners.CommandListener;
import uk.ivorymc.global.bungee.listeners.JoinListener;
import uk.ivorymc.global.bungee.listeners.QuitListener;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("SpellCheckingInspection")
public class IvoryBungee extends Plugin implements BungeeTextChainSource
{
    private @NullOr BungeeAudiences audiences;
    private ConfigFile configFile;
    private SQLController sqlController;

    private Map<String, BungeePlayerData> playerDataMap;
    private Map<String, String> replyMap;

    @Override
    public void onEnable()
    {
        // prepare plugin message channel
        getProxy().registerChannel( "ivory:messaging" );

        // prepare adventure audience
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
        // create player table
        sqlController.createTable(
                "player",
                "uuid BINARY(16) NOT NULL UNIQUE",
                "name TEXT NOT NULL",
                "nickname TEXT DEFAULT NULL",
                "join_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
                "play_time LONG NOT NULL DEFAULT 0",
                "donor BOOLEAN DEFAULT FALSE",
                "PRIMARY KEY (uuid)"
        );
        // create chat log table
        sqlController.createTable(
            "chat_logs",
            "id INT NOT NULL AUTO_INCREMENT",
            "date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "message TEXT NOT NULL",
            "player_uuid BINARY(16) NOT NULL REFERENCES player(uuid)",
            "PRIMARY KEY (id)"
        );
        // create command log table
        sqlController.createTable(
            "command_logs",
            "id INT NOT NULL AUTO_INCREMENT",
            "date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "message TEXT NOT NULL",
            "player_uuid BINARY(16) NOT NULL REFERENCES player(uuid)",
            "PRIMARY KEY (id)"
        );
        // create mail table
        sqlController.createTable(
            "mail",
            "id INT NOT NULL AUTO_INCREMENT",
            "message TEXT NOT NULL",
            "date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "sender_uuid BINARY(16) NOT NULL REFERENCES player(uuid)",
            "recipient_uuid BINARY(16) REFERENCES player(uuid) DEFAULT NULL",
            "PRIMARY KEY (id)"
        );
    }

    private void register()
    {
        // register events
        register(new ChatListener(this));
        register(new CommandListener(this));
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

    public void sendCustomData(ProxiedPlayer player, ByteArrayDataOutput out)
    {
        player.getServer().getInfo().sendData( "ivory:messaging", out.toByteArray() );
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendCustomData(ProxiedPlayer player, String subchannel)
    {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( subchannel ); // the channel could be whatever you want
        sendCustomData(player, out);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendCustomData(ProxiedPlayer player, String subchannel, String data)
    {
        Collection<ProxiedPlayer> networkPlayers = this.getProxy().getPlayers();
        // perform a check to see if globally are no players
        if ( networkPlayers == null || networkPlayers.isEmpty() )
        {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( subchannel ); // the channel could be whatever you want
        out.writeUTF( data ); // this data could be whatever you want

        // we send the data to the server
        // using ServerInfo the packet is being queued if there are no players in the server
        // using only the server to send data the packet will be lost if no players are in it
        sendCustomData(player, out);
    }
}
