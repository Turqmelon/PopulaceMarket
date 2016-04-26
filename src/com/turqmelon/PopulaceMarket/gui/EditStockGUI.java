package com.turqmelon.PopulaceMarket.gui;


import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import com.turqmelon.PopulaceMarket.shops.Shop;
import com.turqmelon.PopulaceMarket.shops.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class EditStockGUI extends ShopGUI {

    public static final String INFINITY_PRMISSION = "populace.market.infinite";

    public EditStockGUI(Resident resident, Shop shop, ShopItem item) {
        super(resident, shop, item);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 0){
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            getShop().getGUI(getResident()).open(player);
        }
        else if (raw == 22){

            if (event.isShiftClick() && player.hasPermission(INFINITY_PRMISSION)){
                if (getItem().isInfinite()){
                    getItem().setStock(0);
                    player.sendMessage(Msg.OK + "Item is no longer infinite.");
                }
                else{
                    getItem().setStock(-1);
                    player.sendMessage(Msg.OK + "Item is now infinite.");
                }
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                repopulate();
            }
            else{

                if (getItem().isInfinite()){
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
                    player.sendMessage(Msg.ERR + "You can't pull items from an infinite pool!");
                    return;
                }

                int pullout = 1;
                if (event.isRightClick()){
                    int max = getItem().getItem().getType().getMaxStackSize();
                    if (max > getItem().getStock()){
                        max = getItem().getStock();
                    }
                    if (max == 0){
                        max = 1;
                    }
                    pullout = max;
                }

                if (getItem().getStock() >= pullout){

                    boolean theresSpace = player.getInventory().firstEmpty()!=-1;
                    ItemStack item = getItem().getItem();
                    item.setAmount(pullout);

                    getItem().setStock(getItem().getStock()-pullout);

                    if (theresSpace){
                        player.getInventory().addItem(item);
                    }
                    else{
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                        player.sendMessage(Msg.WARN + "Item was dropped on the ground due to a full inventory.");
                    }
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    repopulate();

                }
                else{
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
                    player.sendMessage(Msg.ERR + "You can't pull out " + pullout + " when the stock is " + getItem().getStock() + "!");
                }

            }

        }
        else{

            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR){

                if (getItem().matches(cursor)){

                    getItem().setStock(getItem().getStock()+cursor.getAmount());
                    event.setCursor(null);
                    player.updateInventory();
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    repopulate();
                    player.sendMessage(Msg.OK + "Added item to the stock!");

                }
                else{
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
                    player.sendMessage(Msg.ERR + "Held item doesn't match what's in the shop.");
                }

            }

        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        inv.setItem(0, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());

        ItemStack item = getItem().getItem();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§b§l" + ItemUtil.getItemName(item));
        List<String> lore = new ArrayList<>();

        lore.add(getItem().isInStock()?"§fStock §e"+(getItem().isInfinite()?"Infinite": NumberFormat.getInstance().format(getItem().getStock())):"§c§oOut of Stock");

        lore.add("§a");
        lore.add("§6§lStocking Items");
        lore.add("§fTo stock this item, just click it onto");
        lore.add("§fany §eempty space§f in this window from");
        lore.add("§fyour inventory.");
        lore.add("§a");
        lore.add("§6§lRetrieving Stock");
        lore.add("§fStock can be retrieved by clicking");
        lore.add("§fhere with an empty cursor.");
        lore.add("§a");
        lore.add("§aLeft Click§f to pull out §e1f.");
        int max = item.getType().getMaxStackSize();
        if (max > getItem().getStock()){
            max = getItem().getStock();
        }
        if (max > 1){
            lore.add("§aRight Click§f to pull out §e" + max + "f.");
        }
        Player player = Bukkit.getPlayer(getResident().getUuid());
        if (player != null && player.hasPermission(INFINITY_PRMISSION)){
            lore.add("§6Shift Click§f to toggle infinite stock. §c(Admins)");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        inv.setItem(22, item);

    }
}
