package com.turqmelon.PopulaceMarket.gui;

import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.PopulaceMarket.shops.Shop;
import com.turqmelon.PopulaceMarket.shops.ShopItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/******************************************************************************
 * *
 * CONFIDENTIAL                                                               *
 * __________________                                                         *
 * *
 * [2012 - 2016] Devon "Turqmelon" Thome                                      *
 * All Rights Reserved.                                                      *
 * *
 * NOTICE:  All information contained herein is, and remains                  *
 * the property of Turqmelon and its suppliers,                               *
 * if any.  The intellectual and technical concepts contained                 *
 * herein are proprietary to Turqmelon and its suppliers and                  *
 * may be covered by U.S. and Foreign Patents,                                *
 * patents in process, and are protected by trade secret or copyright law.    *
 * Dissemination of this information or reproduction of this material         *
 * is strictly forbidden unless prior written permission is obtained          *
 * from Turqmelon.                                                            *
 * *
 ******************************************************************************/
public class NewItemGUI extends ShopGUI {

    private ItemStack itemToBeSold = null;
    private double price = 0;

    public NewItemGUI(Resident resident, Shop shop) {
        super(resident, shop, 1);
    }

    @Override
    protected void onPlayerCloseInv(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (getItemToBeSold() != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), getItemToBeSold());
            setItemToBeSold(null);
        }
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 0) {
            if (getItemToBeSold() != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), getItemToBeSold());
                setItemToBeSold(null);
            }
            getShop().getGUI(getResident()).open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        } else if (raw == 13) {
            if (getItemToBeSold() != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), getItemToBeSold());
                setItemToBeSold(null);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                repopulate();
            } else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                ItemStack cursor = event.getCursor();
                setItemToBeSold(cursor);
                event.setCursor(null);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                repopulate();
            }
        } else if (raw == 31) {
            double newAmount = getPrice();
            if (event.isRightClick()) {
                newAmount--;
            } else {
                newAmount++;
            }
            if (newAmount < 0) {
                newAmount = 0;
            }
            if (newAmount != getPrice()) {
                setPrice(newAmount);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                repopulate();
            }
        } else if (raw == 32) {
            double newAmount = getPrice();
            if (event.isRightClick()) {
                newAmount -= 10;
            } else {
                newAmount += 10;
            }
            if (newAmount < 0) {
                newAmount = 0;
            }
            if (newAmount != getPrice()) {
                setPrice(newAmount);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                repopulate();
            }
        } else if (raw == 33) {
            double newAmount = getPrice();
            if (event.isRightClick()) {
                newAmount -= 100;
            } else {
                newAmount += 100;
            }
            if (newAmount < 0) {
                newAmount = 0;
            }
            if (newAmount != getPrice()) {
                setPrice(newAmount);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                repopulate();
            }
        } else if (raw == 34) {
            double newAmount = getPrice();
            if (event.isRightClick()) {
                newAmount -= 1000;
            } else {
                newAmount += 1000;
            }
            if (newAmount < 0) {
                newAmount = 0;
            }
            if (newAmount != getPrice()) {
                setPrice(newAmount);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                repopulate();
            }
        } else if ((raw == 48 || raw == 49 || raw == 50) && getItemToBeSold() != null && getPrice() > 0) {

            int stock = getItemToBeSold().getAmount();
            getItemToBeSold().setAmount(1);

            ShopItem item = new ShopItem(UUID.randomUUID(), getItemToBeSold());
            item.setStock(stock);
            item.setSellPrice(getPrice());

            getShop().getItems().add(item);
            setItemToBeSold(null);
            getShop().getGUI(getResident()).open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();
        ItemStack spacer = new ItemBuilder(Material.STAINED_GLASS_PANE).withData((byte) 7).withCustomName("§a").build();

        for (int i = 0; i < getProxyInventory().getSize(); i++) {
            inv.setItem(i, spacer);
        }

        inv.setItem(0, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());

        if (getItemToBeSold() != null) {
            inv.setItem(13, getItemToBeSold());
        } else {
            inv.setItem(13, new ItemBuilder(Material.SIGN).withCustomName("§e§lPlace Item Here")
                    .withLore(Arrays.asList("§7Place the item you want to sell here.")).build());
        }

        List<String> lore = new ArrayList<>();
        String townName = getShop().getTown().getName() + getShop().getTown().getLevel().getSuffix();
        if (getShop().getTown().getSalesTax() > 0) {
            Map<ShopItem.ShopCalculationType, Double> pricing = ShopItem.calculatePrice(getShop().getTown(), getPrice());
            lore.add("§f" + townName + " Sales Tax §e" + getShop().getTown().getSalesTax() + "% §7§o(They'll get " + Populace.getCurrency().format(pricing.get(ShopItem.ShopCalculationType.TOWN_PROFIT)) + ")");
            lore.add("§fYou'll Receive §e" + Populace.getCurrency().format(pricing.get(ShopItem.ShopCalculationType.NEW_PRICE)));
        } else {
            lore.add("§c" + townName + " has no sales tax.");
        }

        inv.setItem(29, new ItemBuilder(Material.DIAMOND).withCustomName("§fPrice §e" + Populace.getCurrency().format(getPrice())).withLore(lore).build());

        inv.setItem(31, new ItemBuilder(Material.GOLD_NUGGET).withCustomName("§f" + Populace.getCurrency().format(1) + " §7(" + Populace.getCurrency().format(getPrice()) + ")")
                .withLore(Arrays.asList("§aLeft Click§f to increase price.", "§aRight Click§f to decrease price.")).build());
        inv.setItem(32, new ItemBuilder(Material.GOLD_INGOT).withCustomName("§f" + Populace.getCurrency().format(10) + " §7(" + Populace.getCurrency().format(getPrice()) + ")")
                .withLore(Arrays.asList("§aLeft Click§f to increase price.", "§aRight Click§f to decrease price.")).build());
        inv.setItem(33, new ItemBuilder(Material.GOLD_BLOCK).withCustomName("§f" + Populace.getCurrency().format(100) + " §7(" + Populace.getCurrency().format(getPrice()) + ")")
                .withLore(Arrays.asList("§aLeft Click§f to increase price.", "§aRight Click§f to decrease price.")).build());
        inv.setItem(34, new ItemBuilder(Material.GOLDEN_APPLE).withCustomName("§f" + Populace.getCurrency().format(1000) + " §7(" + Populace.getCurrency().format(getPrice()) + ")")
                .withLore(Arrays.asList("§aLeft Click§f to increase price.", "§aRight Click§f to decrease price.")).build());

        if (getPrice() > 0 && getItemToBeSold() != null) {
            int[] slots = {48, 49, 50};
            ItemStack confirm = new ItemBuilder(Material.STAINED_GLASS_PANE).withCustomName("§a§lCreate Item").withData((byte) 5).build();
            for (int i : slots) {
                inv.setItem(i, confirm);
            }
        }

    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ItemStack getItemToBeSold() {
        return itemToBeSold;
    }

    public void setItemToBeSold(ItemStack itemToBeSold) {
        this.itemToBeSold = itemToBeSold;
    }
}
