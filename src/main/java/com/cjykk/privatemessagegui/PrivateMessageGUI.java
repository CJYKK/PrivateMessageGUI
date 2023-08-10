package com.cjykk.privatemessagegui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PrivateMessageGUI extends JavaPlugin implements Listener {

    private final Map<Player, Player> playerConversations = new HashMap<>();

    @Override
    public void onEnable() {

        getLogger().info(" ____  __  __  ____ _   _ ___ ");
        getLogger().info("|  _ \\|  \\/  |/ ___| | | |_ _|");
        getLogger().info("| |_) | |\\/| | |  _| | | || | ");
        getLogger().info("|  __/| |  | | |_| | |_| || | ");
        getLogger().info("|_|   |_|  |_|\\____|\\___/|___|");
        getLogger().info("------------------------------");
        getLogger().info("PrivateMessageGUI | v" + getDescription().getVersion() + " | By CJYKK");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("已完成加载！");
    }

    @Override
    public void onDisable() {
        getLogger().info("插件已关闭。Have a nice day!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pmgui")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
                return true;
            }

            Player player = (Player) sender;
            openPlayerListGUI(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("pmcancel")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (playerConversations.containsKey(player)) {
                    playerConversations.remove(player);
                    player.sendMessage(ChatColor.GREEN + "已取消私信。");
                } else {
                    player.sendMessage(ChatColor.RED + "您当前不在私信会话中。");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令。");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("请选择私信对象")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
                Player targetPlayer = Bukkit.getPlayerExact(skullMeta.getDisplayName());

                if (targetPlayer != null) {
                    Player player = (Player) event.getWhoClicked();
                    playerConversations.put(player, targetPlayer);
                    player.closeInventory();
                    player.sendMessage("请输入发给 " + targetPlayer.getName() + " 的私信内容（或使用 /pmcancel 取消）：");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerConversations.containsKey(player)) {
            Player targetPlayer = playerConversations.get(player);
            playerConversations.remove(player);
            event.setCancelled(true);
            String message = event.getMessage();

            if (message.equalsIgnoreCase("/pmcancel")) {
                player.sendMessage(ChatColor.GREEN + "已取消私信。");
            } else {
                Bukkit.getScheduler().runTask(this, () -> Bukkit.dispatchCommand(player, "msg " + targetPlayer.getName() + " " + message));
            }
        }
    }

    private void openPlayerListGUI(Player player) {
        Inventory playerListGUI = Bukkit.createInventory(player, 54, "请选择私信对象");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ItemStack playerHead = createPlayerHeadItem(onlinePlayer.getName());
            playerListGUI.addItem(playerHead);
        }

        player.openInventory(playerListGUI);
    }

    private ItemStack createPlayerHeadItem(String playerName) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        skullMeta.setDisplayName(ChatColor.RESET + playerName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.AQUA + "点击发送私信！");
        skullMeta.setLore(lore);

        playerHead.setItemMeta(skullMeta);
        return playerHead;
    }
}