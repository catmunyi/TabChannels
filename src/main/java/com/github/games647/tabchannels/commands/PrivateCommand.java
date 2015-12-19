package com.github.games647.tabchannels.commands;

import com.github.games647.tabchannels.Channel;
import com.github.games647.tabchannels.Subscriber;
import com.github.games647.tabchannels.TabChannels;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrivateCommand implements CommandExecutor {

    private final TabChannels plugin;

    public PrivateCommand(TabChannels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "You cannot start a private message channel");
            return true;
        }

        if (args.length > 0) {
            String target = args[0];

            Player self = (Player) sender;
            Player targetPlayer = Bukkit.getPlayerExact(target);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.DARK_RED + "This player isn't online");
            } else if (self.equals(targetPlayer)) {
                sender.sendMessage(ChatColor.DARK_RED + "You cannot message with yourself");
            } else {
                //user who started the chat + the target user
                String channelId = self.getUniqueId().toString() + targetPlayer.getUniqueId().toString();
                String testId = targetPlayer.getUniqueId().toString() + self.getUniqueId().toString();
                if (plugin.getChannels().containsKey(channelId) || plugin.getChannels().containsKey(testId)) {
                    sender.sendMessage(ChatColor.DARK_RED + "This chat already exists");
                } else {
                    startPrivateChat(self, targetPlayer, channelId);
                }
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Missing receiver name");
        }

        return true;
    }

    private void startPrivateChat(Player self, Player targetPlayer, String channelId) {
        //start a private chat
        Subscriber selfSubscriber = plugin.getSubscribers().get(self.getUniqueId());

        UUID targetUUID = targetPlayer.getUniqueId();
        Subscriber targetSubscriber = plugin.getSubscribers().get(targetUUID);

        Channel privateChannel = new Channel(channelId, "Private", true);
        selfSubscriber.subscribe(privateChannel);
        targetSubscriber.subscribe(privateChannel);

        privateChannel.addRecipient(targetUUID);
        privateChannel.addRecipient(self.getUniqueId());
        plugin.getChannels().put(channelId, privateChannel);

        sendNewChat(selfSubscriber, self);
        sendNewChat(targetSubscriber, targetPlayer);
    }

    private void sendNewChat(Subscriber subscriber, Player player) {
        Channel currentChannel = subscriber.getCurrentChannel();
        player.spigot().sendMessage(currentChannel.getFormattedHeader(currentChannel.getName()));
        player.spigot().sendMessage(currentChannel.getContent());
        player.spigot().sendMessage(subscriber.getChannelSelection());
    }
}
