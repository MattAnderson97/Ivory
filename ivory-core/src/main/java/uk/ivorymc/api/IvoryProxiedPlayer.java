package uk.ivorymc.api;

import community.leaf.textchain.adventure.TextChain;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class IvoryProxiedPlayer
{
    private final IvoryBungee proxy;
    private final ProxiedPlayer player;
    private Optional<ProxiedPlayer> lastSender;
    private MessageType lastMessageType;

    public IvoryProxiedPlayer(IvoryBungee proxy, ProxiedPlayer player)
    {
        this.proxy = proxy;
        this.player = player;
        lastSender = Optional.empty();
        lastMessageType = MessageType.NONE;
    }

    public void setSender(ProxiedPlayer sender) { lastSender = Optional.ofNullable(sender); }
    public Optional<ProxiedPlayer> getLastSender(){ return lastSender; }
    public MessageType getLastMessageType() { return lastMessageType; }
    public ProxiedPlayer getPlayer() { return player; }

    public void sendMessage(TextChain message, ProxiedPlayer sender, MessageType type)
    {
        setSender(sender);
        lastMessageType = type;
        message.send(proxy.adventure().sender(player));
    }

    public void sendMessage(String message, ProxiedPlayer sender, MessageType type)
    {
        sendMessage(TextChain.chain().then(message), sender, type);
    }

    public void sendMessage(TextChain message, MessageType type)
    {
        setSender(null);
        lastMessageType = type;
        message.send(proxy.adventure().sender(player));
    }

    public void sendMessage(String message, MessageType type)
    {
        sendMessage(TextChain.chain().then(message), type);
    }

    public void sendMessage(TextChain message)
    {
        setSender(null);
        lastMessageType = MessageType.NONE;
        message.send(proxy.adventure().sender(player));
    }

    public void sendMessage(String message)
    {
        sendMessage(TextChain.chain().then(message));
    }
}
