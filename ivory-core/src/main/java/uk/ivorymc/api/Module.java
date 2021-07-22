package uk.ivorymc.api;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import uk.ivorymc.api.storage.SQLController;
import uk.ivorymc.api.storage.YamlFile;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class Module extends JavaPlugin implements Listener, BukkitTaskSource, PluginMessageListener
{
    protected Registry registry;
    protected ModuleConfig config;
    protected SQLController sqlController;

    // CCF
    private BukkitCommandManager<CommandSender> commandManager;
    private BukkitAudiences bukkitAudiences;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private AnnotationParser<CommandSender> annotationParser;


    @Override
    public void onEnable()
    {
        // plugin messaging setup
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "ivory:messaging");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "ivory:messaging", this);
        // plugin setup
        this.registry = new Registry(this);

        // command manager setup

        //
        // This is a function that will provide a command execution coordinator that parses and executes commands
        // asynchronously
        //
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();
        //
        // However, in many cases it is fine for to run everything synchronously:
        //
        // final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
        //        CommandExecutionCoordinator.simpleCoordinator();
        //
        // This function maps the command sender type of our choice to the bukkit command sender.
        // However, in this example we use the Bukkit command sender, and so we just need to map it
        // to itself
        //
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try
        {
            this.commandManager = new PaperCommandManager<>(
                /* Owning plugin */ this,
                /* Coordinator function */ executionCoordinatorFunction,
                /* Command Sender -> C */ mapperFunction,
                /* C -> Command Sender */ mapperFunction
            );
        }
        catch (final Exception e)
        {
            this.getLogger().severe("Failed to initialize the command this.manager");
            /* Disable the plugin */
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //
        // Create a BukkitAudiences instance (adventure) in order to use the minecraft-extras
        // help system
        //
        this.bukkitAudiences = BukkitAudiences.create(this);
        //
        // Create the Minecraft help menu system
        //
        this.minecraftHelp = new MinecraftHelp<>(
            /* Help Prefix */ "/example help",
            /* Audience mapper */ this.bukkitAudiences::sender,
            /* Manager */ this.commandManager
        );
        //
        // Register Brigadier mappings
        //
        if (this.commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.commandManager.registerBrigadier();
        }
        //
        // Register asynchronous completions
        //
        if (this.commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.commandManager).registerAsynchronousCompletions();
        }
        //
        // Create the confirmation this.manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        //
        this.confirmationManager = new CommandConfirmationManager<>(
            /* Timeout */ 30L,
            /* Timeout unit */ TimeUnit.SECONDS,
            /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
            ChatColor.RED + "Confirmation required. Confirm using /example confirm."),
            /* Action when no confirmation is pending */ sender -> sender.sendMessage(
            ChatColor.RED + "You don't have any pending commands.")
        );
        //
        // Register the confirmation processor. This will enable confirmations for commands that require it
        //
        this.confirmationManager.registerConfirmationProcessor(this.commandManager);
        //
        // Create the annotation parser. This allows you to define commands using methods annotated with
        // @CommandMethod
        //
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
            CommandMeta.simple()
                // This will allow you to decorate commands with descriptions
                .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                .build();
        this.annotationParser = new AnnotationParser<>(
            /* Manager */ this.commandManager,
            /* Command sender type */ CommandSender.class,
            /* Mapper for command meta instances */ commandMetaFunction
        );


        // get config file
        this.config = new ModuleConfig(Path.of(getDataFolder().getAbsolutePath()), "config.yml");
        // get sql connection
        this.sqlController = new SQLController(
            config.getConfig().getString("mysql.database"),
            config.getConfig().getString("mysql.host"),
            config.getConfig().getInt("mysql.port"),
            config.getConfig().getString("mysql.username"),
            config.getConfig().getString("mysql.password")
        );
        // register commands and events
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable()
    {
        //make sure to unregister the registered channels in case of a reload
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    public @NotNull Plugin plugin()
    {
        return this;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message){}

    protected abstract void registerCommands();
    protected abstract void registerEvents();

    public BukkitCommandManager<CommandSender> getCommandManager() { return commandManager; }
    public AnnotationParser<CommandSender> getAnnotationParser() { return annotationParser; }
    public SQLController getSqlController() { return sqlController; }

    public Audience audience(Player player)
    {
        return bukkitAudiences.player(player);
    }

    public Audience audience(CommandSender sender)
    {
        return bukkitAudiences.sender(sender);
    }

    private class ModuleConfig extends YamlFile
    {
        ModuleConfig(Path path, String name)
        {
            super(path, name);
        }

        public void saveAsync()
        {
            async().run(this::save);
        }

        public void saveDefaults()
        {
            setDefault("mysql.database", "ivory");
            setDefault("mysql.host", "127.0.0.1");
            setDefault("mysql.port", 3306);
            setDefault("mysql.username", "");
            setDefault("mysql.password", "");
            saveAsync();
        }

        public void setDefault(String key, Object value)
        {
            if (!getConfig().contains(key))
            {
                getConfig().set(key, value);
            }
        }
    }
}
