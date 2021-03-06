package com.github.games647.tabchannels;

import com.github.games647.tabchannels.commands.ChannelCommand;
import com.github.games647.tabchannels.commands.CreateCommand;
import com.github.games647.tabchannels.commands.PrivateCommand;
import com.github.games647.tabchannels.commands.SwitchCommand;
import com.github.games647.tabchannels.listener.ChatListener;
import com.github.games647.tabchannels.listener.SubscriptionListener;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TabChannels extends JavaPlugin {

    private final Map<UUID, Subscriber> subscribers = Maps.newHashMapWithExpectedSize(50);
    private final Map<String, Channel> channels = Maps.newHashMap();

    private final Channel globalChannel = new Channel("global", false);

    @Override
    public void onEnable() {
        //register commands
        getCommand(this.getName().toLowerCase()).setExecutor(new ChannelCommand(this));
        getCommand("switchchannel").setExecutor(new SwitchCommand(this));
        getCommand("private").setExecutor(new PrivateCommand(this));
        getCommand("createchannel").setExecutor(new CreateCommand(this));

        //register listeners
        getServer().getPluginManager().registerEvents(new SubscriptionListener(this), this);
//        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
//            //we cannot register it here because Java wouldn't know the type specification
//            PacketChatListener.createInstance(this);
//        } else {
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
//        }

        channels.put(globalChannel.getId(), globalChannel);

        //load all players if the server is already started like in a reload
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            loadPlayer(onlinePlayer);
        }
    }

    public Map<UUID, Subscriber> getSubscribers() {
        return subscribers;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public Channel getGlobalChannel() {
        return globalChannel;
    }

    public void loadPlayer(Player player) {
        //automatically subscribe to the global channel
        Subscriber subscriber = new Subscriber(player.getUniqueId(), globalChannel);
        subscribers.put(player.getUniqueId(), subscriber);

        globalChannel.addRecipient(player.getUniqueId());
    }
}
