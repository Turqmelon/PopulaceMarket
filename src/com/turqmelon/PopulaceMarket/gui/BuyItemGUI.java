package com.turqmelon.PopulaceMarket.gui;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import com.turqmelon.PopulaceMarket.shops.Shop;
import com.turqmelon.PopulaceMarket.shops.ShopItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

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
public class BuyItemGUI extends ShopGUI {

    private int buying = 1;

    public BuyItemGUI(Resident resident, Shop shop, ShopItem item) {
        super(resident, shop, item);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 0) {
            getShop().getGUI(getResident()).open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        } else if (raw == 8) {
            if (event.isShiftClick()) {
                int newBuying = getBuying();
                if (event.isRightClick()) {
                    newBuying--;
                } else {
                    newBuying++;
                }
                if (newBuying > getItem().getStock()) {
                    newBuying = getItem().getStock();
                }
                if (newBuying < 1) {
                    newBuying = 1;
                }
                if (newBuying != getBuying()) {
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    setBuying(newBuying);
                    repopulate();
                }
            } else {

                if (getItem().isInStock()) {
                    double price = getBuying() * getItem().getSellPrice();
                    Account account = AccountManager.getAccount(getResident().getUuid());

                    if (account != null && account.withdraw(Populace.getCurrency(), price)) {

                        player.closeInventory();
                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);

                        if (!getItem().isInfinite()) {
                            getItem().setStock(getItem().getStock() - getBuying());
                        }

                        for (int i = 0; i < getBuying(); i++) {
                            if (player.getInventory().firstEmpty() != -1) {
                                player.getInventory().addItem(getItem().getItem());
                            } else {
                                player.getWorld().dropItemNaturally(player.getLocation(), getItem().getItem());
                            }
                        }

                        Map<ShopItem.ShopCalculationType, Double> revenue = ShopItem.calculatePrice(getShop().getTown(), price);
                        double town = revenue.get(ShopItem.ShopCalculationType.TOWN_PROFIT);
                        double merchant = revenue.get(ShopItem.ShopCalculationType.NEW_PRICE);
                        getShop().getFundSource().deposit(Populace.getCurrency(), merchant);

                        if (town > 0) {
                            getShop().getTown().setBank(getShop().getTown().getBank() + town);
                        }

                        getShop().getCreator().sendMessage(Msg.INFO + "You sold " + getBuying() + "x " + ItemUtil.getItemName(getItem().getItem()) + " for " + Populace.getCurrency().format(price) + "!");
                        player.sendMessage(Msg.OK + "You bought " + getBuying() + "x " + ItemUtil.getItemName(getItem().getItem()) + " for " + Populace.getCurrency().format(price) + ".");
                    } else {
                        player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
                        player.sendMessage(Msg.ERR + "You don't have enough " + Populace.getCurrency().getPlural() + ".");
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
                    player.sendMessage(Msg.ERR + "Out of stock.");
                }


            }
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        ItemStack spacer = new ItemBuilder(Material.STAINED_GLASS_PANE).withData((byte) 7).withCustomName("§a").build();

        for (int i = 0; i < 18; i++) {
            inv.setItem(i, spacer);
        }

        inv.setItem(0, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());

        inv.setItem(4, getItem().getDisplayItem(getShop()));

        double balance = 0;
        Account account = AccountManager.getAccount(getResident().getUuid());
        if (account != null) {
            balance = account.getBalance(Populace.getCurrency());
        }
        double price = getItem().getSellPrice() * getBuying();

        if (balance >= price) {
            inv.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE).withData((byte) 5)
                    .withCustomName("§a§lBUYING " + getBuying())
                    .withLore(
                            Arrays.asList(
                                    "§a",
                                    "§fPrice §e" + Populace.getCurrency().format(price),
                                    "§fYou have §e" + Populace.getCurrency().format(balance),
                                    "§f" + Populace.getCurrency().getPlural() + " After §e" + Populace.getCurrency().format(balance - price),
                                    "§a",
                                    "§aLeft Click§f to purchase.",
                                    "§aShift Left Click§f to increase quantity.",
                                    "§aShift Right Click§f to decrease quantity.")).build());
        } else {
            inv.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE).withData((byte) 15)
                    .withCustomName("§c§lBUYING " + getBuying())
                    .withLore(
                            Arrays.asList(
                                    "§a",
                                    "§fPrice §e" + Populace.getCurrency().format(price),
                                    "§fYou have §c" + Populace.getCurrency().format(balance),
                                    "§f" + Populace.getCurrency().getPlural() + " After §c" + Populace.getCurrency().format(balance - price),
                                    "§a",
                                    "§aShift Left Click§f to increase quantity.",
                                    "§aShift Right Click§f to decrease quantity.")).build());
        }


        int placed = 0;
        for (int i = 18; i < 54; i++) {
            if (placed >= getBuying()) {
                break;
            }
            inv.setItem(i, getItem().getItem());
            placed++;
        }


    }

    public int getBuying() {
        return buying;
    }

    public void setBuying(int buying) {
        this.buying = buying;
    }
}
