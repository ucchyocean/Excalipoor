/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.excalipoor;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * エクスカリパー（念のために書いておくけど、エクスカリバーじゃない。）
 * @author ucchy
 */
public class Excalipoor extends JavaPlugin implements Listener {

    private static final String DISPLAY_NAME = "Excalipoor";
    
    private int amount;
    private ItemStack item;
    private ArrayList<Enchantment> enchants;
    private ArrayList<Integer> enchantLevels;
    private short durability;
    
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
        
        // スタック量
        amount = getConfig().getInt("amount", 5);
        
        // アイテムの種類
        String item_str = getConfig().getString("item", "DIAMOND_SWORD");
        if ( Material.getMaterial(item_str) != null ) {
            item = new ItemStack(Material.getMaterial(item_str), amount);
        } else {
            item = new ItemStack(Material.DIAMOND_SWORD, amount);
        }
        
        // エンチャント
        enchants = new ArrayList<Enchantment>();
        enchantLevels = new ArrayList<Integer>();
        if ( getConfig().contains("enchants") ) {
            ConfigurationSection enchants_sec = getConfig().getConfigurationSection("enchants");
            for ( String type_str : enchants_sec.getKeys(false) ) {
                Enchantment enchant = Enchantment.getByName(type_str);
                
                if ( enchant != null ) {
                    int level = enchants_sec.getInt(type_str, 1);
                    item.addUnsafeEnchantment(enchant, level);
                    enchants.add(enchant);
                    enchantLevels.add(level);
                }
            }
        }
        
        // 残消耗度
        short remain = (short)getConfig().getInt("remain", 1);
        durability = (short)(item.getType().getMaxDurability() - remain + 1);
        if ( durability < 0 ) {
            durability = 0;
        }
        item.setDurability(durability);
        
        // 表示名設定
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(DISPLAY_NAME);
        item.setItemMeta(meta);
    }

    /**
     * プラグインのコマンドが実行されたときに呼び出されるイベント
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        
        if ( args.length >= 1 && args[0].equalsIgnoreCase("get") ) {
            
            if ( !(sender instanceof Player) ) {
                sender.sendMessage("This command can be run only in game.");
                return true;
            }
            
            ItemStack excalipoor = item.clone();
            if ( args.length >= 2 && args[1].matches("-?[0-9]+") ) {
                int amount = Integer.parseInt(args[1]);
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
            
            Player target = getServer().getPlayerExact(args[1]);
            if ( target == null ) {
                sender.sendMessage("Player " + args[1] + " was not found.");
                return true;
            }
            
            ItemStack excalipoor = item.clone();
            if ( args.length >= 3 && args[2].matches("-?[0-9]+") ) {
                int amount = Integer.parseInt(args[2]);
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
        
        // 左クリックでなければ無視する。
        if ( event.getAction() != Action.LEFT_CLICK_AIR && 
                event.getAction() != Action.LEFT_CLICK_BLOCK ) {
            return;
        }
        
        // 手に持っているアイテムがExcalipoorでないなら無視する
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        
        if ( item == null || item.getItemMeta() == null ||
                !item.getItemMeta().hasDisplayName() ||
                !item.getItemMeta().getDisplayName().equals(DISPLAY_NAME) ) {
            return;
        }
        
        // Durabilityが0なら、消耗値を再設定する
        if ( item.getDurability() < durability ) {
            item.setDurability(durability);
        }
    }
}
