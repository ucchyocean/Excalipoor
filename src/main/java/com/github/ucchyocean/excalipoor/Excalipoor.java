/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.excalipoor;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * エクスカリパー（念のために書いておくけど、エクスカリバーじゃない。）
 * @author ucchy
 */
public class Excalipoor extends JavaPlugin implements Listener {

    private HashMap<String, ItemSetting> items;
    
    /**
     * プラグインが有効化されたときに呼び出されるイベント
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // リスナーとして登録
        getServer().getPluginManager().registerEvents(this, this);
        
        // コンフィグが無い場合に作成する
        saveDefaultConfig();
        
        // コンフィグからアイテムをロード
        items = new HashMap<String, ItemSetting>();
        FileConfiguration config = getConfig();
        for ( String name : config.getKeys(false) ) {
            ConfigurationSection section = config.getConfigurationSection(name);
            ItemSetting is = ItemSetting.load(name, section);
            if ( is != null ) {
                items.put(name, is);
            }
        }
    }

    /**
     * プラグインのコマンドが実行されたときに呼び出されるイベント
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        
        if ( args.length >= 2 && args[0].equalsIgnoreCase("get") ) {
            
            if ( !(sender instanceof Player) ) {
                sender.sendMessage("This command can be run only in game.");
                return true;
            }
            
            if ( !sender.hasPermission("excalipoor.get") ) {
                sender.sendMessage("You don't have permission \"excalipoor.get\".");
                return true;
            }
            
            String name = args[1];
            if ( !items.containsKey(name) ) {
                sender.sendMessage("Item " + name + " is not exist.");
                return true;
            }
            
            ItemStack excalipoor = items.get(name).getItem();
            if ( args.length >= 3 && args[2].matches("-?[0-9]+") ) {
                int amount = Integer.parseInt(args[2]);
                excalipoor.setAmount(amount);
            }
            Player player = (Player)sender;
            ItemStack temp = player.getItemInHand();
            player.setItemInHand(excalipoor);
            if ( temp != null && temp.getType() != Material.AIR ) {
                player.getInventory().addItem(temp);
            }
            
            return true;
            
        } else if ( args.length >= 2 && args[0].equalsIgnoreCase("give") ) {
            
            Player target = getServer().getPlayerExact(args[2]);
            if ( target == null ) {
                sender.sendMessage("Player " + args[2] + " was not found.");
                return true;
            }
            
            if ( !sender.hasPermission("excalipoor.give") ) {
                sender.sendMessage("You don't have permission \"excalipoor.give\".");
                return true;
            }
            
            String name = args[1];
            if ( !items.containsKey(name) ) {
                sender.sendMessage("Item " + name + " is not exist.");
                return true;
            }
            
            ItemStack excalipoor = items.get(name).getItem();
            if ( args.length >= 4 && args[3].matches("-?[0-9]+") ) {
                int amount = Integer.parseInt(args[3]);
                excalipoor.setAmount(amount);
            }

            ItemStack temp = target.getItemInHand();
            target.setItemInHand(excalipoor);
            if ( temp != null && temp.getType() != Material.AIR ) {
                target.getInventory().addItem(temp);
            }
            
            return true;
            
        }
        
        return false;
    }
    
    /**
     * プレイヤーのクリックなどが実行されたときに呼び出されるイベント
     * @param event 
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        
        // クリックでなければ無視する。
        if ( event.getAction() == Action.PHYSICAL ) {
            return;
        }
        
        // 手に持っているアイテムが関連アイテムでないなら無視する
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        
        if ( item == null || item.getItemMeta() == null ||
                !item.getItemMeta().hasDisplayName() ||
                !items.containsKey(item.getItemMeta().getDisplayName()) ) {
            return;
        }
        
        // Durabilityが設定値より小さいなら、消耗値を再設定する
        short durability = items.get(item.getItemMeta().getDisplayName()).getdurability();
        if ( item.getDurability() < durability ) {
            item.setDurability(durability);
        }
    }
}
