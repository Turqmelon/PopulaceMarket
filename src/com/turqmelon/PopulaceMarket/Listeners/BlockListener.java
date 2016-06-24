package com.turqmelon.PopulaceMarket.Listeners;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.Events.Town.TownUnclaimLandEvent;
import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Plot.PlotType;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.PermissionSet;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import com.turqmelon.PopulaceMarket.shops.Shop;
import com.turqmelon.PopulaceMarket.shops.ShopManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

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
public class BlockListener implements Listener {

    @EventHandler
    public void onUnclaim(TownUnclaimLandEvent event) {
        Plot plot = event.getPlot();
        for (int i = 0; i < ShopManager.getShops().size(); i++) {
            Shop shop = ShopManager.getShops().get(i);
            Plot shopPlot = PlotManager.getPlot(shop.getChunk());
            if (shopPlot != null && shopPlot.getUuid().equals(plot.getUuid())) {
                shop.destroy(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Shop shop = ShopManager.getShop(block.getLocation());
        if (shop != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Msg.ERR + "You can't break shop chests.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getResident(player);
        if (resident == null) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            Block block = event.getClickedBlock();
            Shop shop = ShopManager.getShop(block.getLocation());
            if (shop != null) {
                event.setCancelled(true);

                Plot plot = PlotManager.getPlot(block.getChunk());
                if (plot == null || plot.getType() != PlotType.MERCHANT) {
                    shop.destroy(true);
                } else {
                    if (plot.can(resident, PermissionSet.SHOP)) {
                        shop.getGUI(resident).open(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 0);
                    } else {
                        player.sendMessage(Msg.ERR + "You can't open shops here.");
                    }

                }

            } else {

                ItemStack hand = player.getItemInHand();
                if (hand != null && hand.getType() == Material.ENDER_CHEST && ItemUtil.getItemName(hand).equalsIgnoreCase("§e§lShop Chest")) {

                    event.setCancelled(true);
                    block = event.getClickedBlock().getRelative(event.getBlockFace());
                    if (block.getType() == Material.AIR) {

                        Plot plot = PlotManager.getPlot(block.getChunk());
                        if (plot != null && plot.getType() == PlotType.MERCHANT) {

                            UUID requiredUUID;
                            if (plot.getOwner() != null) {
                                requiredUUID = plot.getOwner().getUuid();
                            } else {
                                requiredUUID = plot.getTown().getMayor().getUuid();
                            }

                            if (requiredUUID.equals(resident.getUuid())) {

                                Account account = AccountManager.getAccount(player.getUniqueId());
                                if (account != null) {

                                    event.setCancelled(false);
                                    ShopManager.getShops().add(new Shop(UUID.randomUUID(), player.getName() + "'s Shop", account, resident, block.getLocation()));
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    player.sendMessage(Msg.OK + "Shop created!");

                                } else {
                                    player.sendMessage(Msg.ERR + "A MelonEco player account is required.");
                                }

                            } else {
                                player.sendMessage(Msg.ERR + "Only the plot owner can place shops.");
                            }

                        } else {
                            player.sendMessage(Msg.ERR + "Shops can only be created inside Merchant plots.");
                        }

                    }

                }
            }

        }
    }

}
