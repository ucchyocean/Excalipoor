/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.excalipoor;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * アイテム設定クラス
 * @author ucchy
 */
public class ItemSetting {

    private int amount;
    private ItemStack item;
    private short durability;
    
    public static ItemSetting load(String name, ConfigurationSection section) {
        
        ItemSetting setting = new ItemSetting();
        
        setting.amount = section.getInt("amount", 5);
        
        // アイテムの種類
        String item_str = section.getString("item", "DIAMOND_SWORD");
        if ( Material.getMaterial(item_str) == null ) {
            return null;
        }
        setting.item = new ItemStack(Material.getMaterial(item_str), setting.amount);
        
        // エンチャント
        if ( section.contains("enchants") ) {
            ConfigurationSection enchants_sec = section.getConfigurationSection("enchants");
            for ( String type_str : enchants_sec.getKeys(false) ) {
                Enchantment enchant = Enchantment.getByName(type_str);
                
                if ( enchant != null ) {
                    int level = enchants_sec.getInt(type_str, 1);
                    setting.item.addUnsafeEnchantment(enchant, level);
                }
            }
        }
        
        // 残消耗度
        short remain = (short)section.getInt("remain", 1);
        setting.durability = (short)(setting.item.getType().getMaxDurability() - remain + 1);
        if ( setting.durability < 0 ) {
            setting.durability = 0;
        }
        setting.item.setDurability(setting.durability);
        
        // 表示名設定
        ItemMeta meta = setting.item.getItemMeta();
        meta.setDisplayName(name);
        setting.item.setItemMeta(meta);
        
        return setting;
    }
    
    public short getdurability() {
        return durability;
    }
    
    public ItemStack getItem() {
        return item.clone();
    }
}
