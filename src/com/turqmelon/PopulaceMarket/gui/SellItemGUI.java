package com.turqmelon.PopulaceMarket.gui;

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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class SellItemGUI extends ShopGUI {

    private int selling = 0;

    public SellItemGUI(Resident resident, Shop shop, ShopItem item) {
        super(resident, shop, item);
    }

    @Override
    protected void onPlayerCloseInv(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (getSelling() > 0) {
            for (int i = 0; i < getSelling(); i++) {
                player.getWorld().dropItemNaturally(player.getLocation(), getItem().getItem());
            }
            setSelling(0);
        }
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 0) {
            if (getSelling() > 0) {
                for (int i = 0; i < getSelling(); i++) {
                    player.getWorld().dropItemNaturally(player.getLocation(), getItem().getItem());
                }
                setSelling(0);
            }
            getShop().getGUI(getResident()).open(player);
            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        } else if (raw == 8 && getSelling() > 0) {

            double profit = getSelling() * getItem().getBuyPrice();
            Account account = AccountManager.getAccount(getResident().getUuid());

            if (getShop().getFundSource().withdraw(Populace.getCurrency(), profit)) {

                Map<ShopItem.ShopCalculationType, Double> revenue = ShopItem.calculatePrice(getShop().getTown(), profit);
                double town = revenue.get(ShopItem.ShopCalculationType.TOWN_PROFIT);
                profit = revenue.get(ShopItem.ShopCalculationType.NEW_PRICE);
                if (account != null && account.deposit(Populace.getCurrency(), profit)) {
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    getItem().setStock(getItem().getStock() + getSelling());
                    if (town > 0) {
                        getShop().getTown().setBank(getShop().getTown().getBank() + town);
                    }

                    player.sendMessage(Msg.OK + "You sold " + getSelling() + "x " + ItemUtil.getItemName(getItem().getItem()) + " for " + Populace.getCurrency().format(profit) + ".");
                    getShop().getCreator().sendMessage(Msg.INFO + player.getName() + " sold " + getSelling() + "x " + ItemUtil.getItemName(getItem().getItem()) + " for " + Populace.getCurrency().format(profit) + ".");

                    setSelling(0);
                    player.closeInventory();
                } else {
                    player.sendMessage(Msg.ERR + "Deposit failed. You were refunded.");
                    player.closeInventory();
                    getShop().getFundSource().deposit(Populace.getCurrency(), profit);
                }

            } else {
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
                player.sendMessage(Msg.ERR + "Merchant doesn't have enough " + Populace.getCurrency().getPlural() + ".");
            }
        } else if (raw >= 18 && raw <= 53) {

            ItemStack clicked = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (clicked != null && clicked.getType() != Material.AIR) {
                int amt = clicked.getAmount();
                setSelling(getSelling() - amt);
                for (int i = 0; i < amt; i++) {
                    player.getWorld().dropItemNaturally(player.getLocation(), getItem().getItem());
                }
                repopulate();
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            } else if (cursor != null && cursor.getType() != Material.AIR) {
                int amt = cursor.getAmount();
                if (getItem().matches(event.getCursor())) {
                    setSelling(getSelling() + amt);
                    event.setCursor(null);
                    repopulate();
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                } else {
                    player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, 0);
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
        double merchantBalance = getShop().getFundSource().getBalance(Populace.getCurrency());
        double profit = getItem().getBuyPrice() * getSelling();
        Map<ShopItem.ShopCalculationType, Double> revenue = ShopItem.calculatePrice(getShop().getTown(), profit);
        profit = revenue.get(ShopItem.ShopCalculationType.NEW_PRICE);

        if (getSelling() > 0) {
            if (merchantBalance >= profit) {
                inv.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE).withData((byte) 5)
                        .withCustomName("§a§lSELLING " + getSelling())
                        .withLore(
                                Arrays.asList(
                                        "§a",
                                        "§fProfit §e" + Populace.getCurrency().format(profit) + (getShop().getTown().getSalesTax() > 0 ? " §7§o(-" + getShop().getTown().getSalesTax() + "% Sales Tax)" : ""),
                                        "§fYou have §e" + Populace.getCurrency().format(balance),
                                        "§fMerchant has §e" + Populace.getCurrency().format(merchantBalance),
                                        "§fTheir " + Populace.getCurrency().getPlural() + " After §e" + Populace.getCurrency().format(merchantBalance - profit),
                                        "§fYour " + Populace.getCurrency().getPlural() + " After §e" + Populace.getCurrency().format(balance + profit),
                                        "§a",
                                        "§aLeft Click§f to sell to merchant.")).build());
            } else {
                inv.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE).withData((byte) 15)
                        .withCustomName("§c§lSELLING " + getSelling())
                        .withLore(
                                Arrays.asList(
                                        "§a",
                                        "§fProfit §e" + Populace.getCurrency().format(profit) + (getShop().getTown().getSalesTax() > 0 ? " §7§o(-" + getShop().getTown().getSalesTax() + "% Sales Tax)" : ""),
                                        "§fYou have §e" + Populace.getCurrency().format(balance),
                                        "§fMerchant has §c" + Populace.getCurrency().format(merchantBalance),
                                        "§fTheir " + Populace.getCurrency().getPlural() + " After §c" + Populace.getCurrency().format(merchantBalance - profit),
                                        "§fYour " + Populace.getCurrency().getPlural() + " After §e" + Populace.getCurrency().format(balance + profit))).build());
            }

        } else {
            inv.setItem(8, new ItemBuilder(Material.SIGN)
                    .withCustomName("§c§lNOTHING TO SELL")
                    .withLore(Arrays.asList("§7Place items to sell",
                            "§7in the inventory below.")).build());
        }


        for (int i = 0; i < getSelling(); i++) {
            inv.addItem(getItem().getItem());
        }


    }

    public int getSelling() {
        return selling;
    }

    public void setSelling(int selling) {
        this.selling = selling;
    }
}
