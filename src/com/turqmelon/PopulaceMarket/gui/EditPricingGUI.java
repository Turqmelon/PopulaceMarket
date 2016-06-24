package com.turqmelon.PopulaceMarket.gui;


import com.turqmelon.MelonEco.utils.Currency;
import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.PopulaceMarket.shops.Shop;
import com.turqmelon.PopulaceMarket.shops.ShopItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;


public class EditPricingGUI extends ShopGUI {

    public EditPricingGUI(Resident resident, Shop shop, ShopItem item) {
        super(resident, shop, item);
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        int raw = event.getRawSlot();

        if (raw == 0){
            getShop().getGUI(getResident()).open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        double amt = 0;
        ClickMode mode = null;

        if (raw == 19 ||
                raw == 28 ||
                raw == 37 ||
                raw == 46){
            mode = ClickMode.SELL;
            switch(raw){
                case 19:
                    amt = 1;
                    break;
                case 28:
                    amt = 10;
                    break;
                case 37:
                    amt = 100;
                    break;
                case 46:
                    amt = 1000;
                    break;
            }
        }
        else if (raw == 21 ||
                raw == 30 ||
                raw == 39 ||
                raw == 48){
            mode = ClickMode.SELL;
            switch(raw){
                case 21:
                    amt = -1;
                    break;
                case 30:
                    amt = -10;
                    break;
                case 39:
                    amt = -100;
                    break;
                case 48:
                    amt = 1000;
                    break;
            }
        }
        else if (raw == 23 ||
                raw == 32 ||
                raw == 41 ||
                raw == 50){
            mode = ClickMode.BUY;
            switch(raw){
                case 23:
                    amt = 1;
                    break;
                case 32:
                    amt = 10;
                    break;
                case 41:
                    amt = 100;
                    break;
                case 50:
                    amt = 1000;
                    break;
            }
        }
        else if (raw == 25 ||
                raw == 34 ||
                raw == 43 ||
                raw == 52){
            mode = ClickMode.BUY;
            switch(raw){
                case 25:
                    amt = -1;
                    break;
                case 34:
                    amt = -10;
                    break;
                case 43:
                    amt = -100;
                    break;
                case 52:
                    amt = -1000;
                    break;
            }
        }

        if (event.isShiftClick()){
            amt = -Integer.MAX_VALUE;
        }

        if (mode != null){
            switch(mode){
                case SELL:
                    getItem().setSellPrice(getItem().getSellPrice()+amt);
                    break;
                case BUY:
                    getItem().setBuyPrice(getItem().getBuyPrice()+amt);
                    break;
            }
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        repopulate();
    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();
        Currency c = Populace.getCurrency();


        inv.setItem(0, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());

        if (getItem().getSellPrice() > 0){
            inv.setItem(11, new ItemBuilder(Material.DIAMOND).withCustomName("§a§lSelling for " + c.format(getItem().getSellPrice()) + "/ea").build());
        }
        else{
            inv.setItem(11, new ItemBuilder(Material.DIAMOND).withCustomName("§c§lYou're not selling this.").build());
        }

        inv.setItem(19, new ItemBuilder(Material.GOLD_NUGGET).withCustomName("§a§l+" + c.format(1) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());
        inv.setItem(28, new ItemBuilder(Material.GOLD_INGOT).withCustomName("§a§l+" + c.format(10) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());
        inv.setItem(37, new ItemBuilder(Material.GOLD_BLOCK).withCustomName("§a§l+" + c.format(100) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());
        inv.setItem(46, new ItemBuilder(Material.GOLDEN_APPLE).withCustomName("§a§l+" + c.format(1000) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());

        inv.setItem(21, new ItemBuilder(Material.GOLD_NUGGET).withCustomName("§c§l-" + c.format(1) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());
        inv.setItem(30, new ItemBuilder(Material.GOLD_INGOT).withCustomName("§c§l-" + c.format(10) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());
        inv.setItem(39, new ItemBuilder(Material.GOLD_BLOCK).withCustomName("§c§l-" + c.format(100) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());
        inv.setItem(48, new ItemBuilder(Material.GOLDEN_APPLE).withCustomName("§c§l-" + c.format(1000) + " §7§l(" + Populace.getCurrency().format(getItem().getSellPrice()) + ")").build());

        if (getItem().getBuyPrice() > 0){
            inv.setItem(15, new ItemBuilder(Material.CHEST).withCustomName("§a§lBuying for " + c.format(getItem().getBuyPrice()) + "/ea").build());
        }
        else{
            inv.setItem(15, new ItemBuilder(Material.CHEST).withCustomName("§c§lYou're not buying this.").build());
        }

        inv.setItem(23, new ItemBuilder(Material.GOLD_NUGGET).withCustomName("§a§l+" + c.format(1) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());
        inv.setItem(32, new ItemBuilder(Material.GOLD_INGOT).withCustomName("§a§l+" + c.format(10) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());
        inv.setItem(41, new ItemBuilder(Material.GOLD_BLOCK).withCustomName("§a§l+" + c.format(100) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());
        inv.setItem(50, new ItemBuilder(Material.GOLDEN_APPLE).withCustomName("§a§l+" + c.format(1000) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());

        inv.setItem(25, new ItemBuilder(Material.GOLD_NUGGET).withCustomName("§c§l-" + c.format(1) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());
        inv.setItem(34, new ItemBuilder(Material.GOLD_INGOT).withCustomName("§c§l-" + c.format(10) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());
        inv.setItem(43, new ItemBuilder(Material.GOLD_BLOCK).withCustomName("§c§l-" + c.format(100) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());
        inv.setItem(52, new ItemBuilder(Material.GOLDEN_APPLE).withCustomName("§c§l-" + c.format(1000) + " §7§l(" + Populace.getCurrency().format(getItem().getBuyPrice()) + ")").build());


    }

    enum ClickMode{
        BUY, SELL
    }
}
