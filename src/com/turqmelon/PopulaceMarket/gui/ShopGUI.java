package com.turqmelon.PopulaceMarket.gui;

import com.turqmelon.Populace.GUI.GUI;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.Populace.Utils.ItemUtil;
import com.turqmelon.Populace.Utils.Msg;
import com.turqmelon.PopulaceMarket.shops.Shop;
import com.turqmelon.PopulaceMarket.shops.ShopItem;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopGUI extends GUI {

    private Resident resident;
    private Shop shop;
    private int page;
    private ShopItem item = null;

    public ShopGUI(Resident resident, Shop shop, int page) {
        super(shop.getTitle(), 54);
        this.resident = resident;
        this.shop = shop;
        this.page = page;
    }

    public ShopGUI(Resident resident, Shop shop, ShopItem item) {
        super(shop.getTitle(), 54);
        this.resident = resident;
        this.shop = shop;
        this.item = item;
    }

    public Resident getResident() {
        return resident;
    }

    public Shop getShop() {
        return shop;
    }

    public ShopItem getItem() {
        return item;
    }

    public int getPage() {
        return page;
    }

    @Override
    protected void onGUIInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();
        Resident resident = ResidentManager.getResident(player);
        int raw = event.getRawSlot();

        boolean owner = resident.getUuid().equals(getShop().getCreator().getUuid());

        if (raw == 45 && getPage() > 1){
            this.page = page-1;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();
        }
        else if (raw == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR){
            this.page = page+1;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            repopulate();
        }
        else if (raw == 3 && owner){
            new NewItemGUI(getResident(), getShop()).open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
        else if (raw == 5 && owner){
            if (getShop().destroy(false)){
                player.closeInventory();
                player.sendMessage(Msg.OK + "Shop destroyed!");
            }
            else{
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                player.closeInventory();
                player.sendMessage(Msg.ERR + "To destroy your shop, you must first remove all the items from it.");
            }
        }
        else{
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR){
                NBTTagString nbt = (NBTTagString) ItemUtil.getTag(clicked, "shopitemid");
                if (nbt != null){
                    UUID itemid = UUID.fromString(nbt.toString().replace("\"", ""));
                    ShopItem item = getShop().getItem(itemid);
                    if (item != null){

                        if (owner){
                            if (event.isShiftClick()){
                                if (item.remove(getShop(), false)){
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    repopulate();
                                }
                                else{
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                                    player.sendMessage(Msg.ERR + "You must empty the stock before removing an item.");
                                }
                            }
                            else if (event.isRightClick()){
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                new EditPricingGUI(getResident(), getShop(), item).open(player);
                            }
                            else{
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                new EditStockGUI(getResident(), getShop(), item).open(player);
                            }
                        } else {
                            if (event.isRightClick() && item.getBuyPrice() > 0) {
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                new SellItemGUI(getResident(), getShop(), item).open(player);
                            } else if (event.isLeftClick() && item.getSellPrice() > 0) {
                                if (item.isInStock()) {
                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                    new BuyItemGUI(getResident(), getShop(), item).open(player);
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 0);
                                }
                            } else if (event.isLeftClick() && item.getBuyPrice() > 0) {
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                                new SellItemGUI(getResident(), getShop(), item);
                            }

                        }

                    }
                }
            }
        }

    }

    @Override
    protected void populate() {

        Inventory inv = getProxyInventory();

        if (getPage() > 1){
            inv.setItem(45, new ItemBuilder(Material.ARROW).withCustomName("§e§l< BACK").build());
        }

        int itemsPerPage = 28;
        int startIndex = 0;
        int endIndex = itemsPerPage-1;

        for(int i = 0; i < (getPage()-1); i++){
            startIndex+=itemsPerPage;
            endIndex+=itemsPerPage;
        }

        List<ItemStack> display = new ArrayList<>();

        boolean nextPage = true;

        for(int i = startIndex; i <= endIndex; i++){
            if (i < shop.getItems().size()){
                display.add(shop.getItems().get(i).getDisplayItem(getResident(), getShop()));
            }
            else{
                nextPage = false;
                break;
            }
        }

        if (nextPage){
            inv.setItem(53, new ItemBuilder(Material.ARROW).withCustomName("§e§lNEXT >").build());
        }

        int index = 10;

        itemloop:
        for(ItemStack itemStack : display){
            inv.setItem(index, itemStack);
            index++;
            switch(index){
                case 17:
                    index = 19;
                    break;
                case 26:
                    index = 28;
                    break;
                case 35:
                    index = 37;
                    break;
                case 44:
                    break itemloop;

            }
        }

        if (getResident() != null && getResident().getUuid().equals(shop.getCreator().getUuid())){
            inv.setItem(3, new ItemBuilder(Material.NAME_TAG).withCustomName("§a§l+ §aNew Shop Item").build());
            inv.setItem(5, new ItemBuilder(Material.TNT).withCustomName("§c§lX §cDestroy Shop").build());
        }

    }
}
