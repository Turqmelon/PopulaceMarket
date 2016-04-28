package com.turqmelon.PopulaceMarket.shops;


import com.turqmelon.Populace.Populace;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Utils.ItemUtil;
import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;

import java.text.NumberFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class ShopItem {

    private UUID uuid;
    private ItemStack item;
    private int stock = 0;
    private double buyPrice = 0;
    private double sellPrice = 0;

    public ShopItem(UUID uuid, ItemStack item) {
        this.uuid = uuid;
        this.item = item;
    }

    public ShopItem(JSONObject object) throws MojangsonParseException {
        this.uuid = UUID.fromString((String) object.get("uuid"));
        this.stock = (int)((long) object.get("stock"));
        this.item = ItemUtil.JSONtoItemStack((String) object.get("item"));
        JSONObject prices = (JSONObject) object.get("prices");
        this.buyPrice = (double) prices.get("buy");
        this.sellPrice = (double) prices.get("sell");
    }

    public static Map<ShopCalculationType, Double> calculatePrice(Town town, double price) {
        Map<ShopCalculationType, Double> data = new HashMap<>();
        if (town.getSalesTax() == 0 || price == 0) {
            data.put(ShopCalculationType.TOWN_PROFIT, 0.0);
            data.put(ShopCalculationType.NEW_PRICE, price);
        } else {
            double perc = town.getSalesTax() / 100.0;
            double newPrice = price - (price * perc);
            double townProfit = price - newPrice;
            if (!Populace.getCurrency().isDecimalSupported()) {
                newPrice = Math.round(newPrice);
                townProfit = Math.round(townProfit);
            }
            data.put(ShopCalculationType.TOWN_PROFIT, townProfit);
            data.put(ShopCalculationType.NEW_PRICE, newPrice);
        }
        return data;
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public JSONObject toJSON(){
        JSONObject object = new JSONObject();
        object.put("uuid", getUuid().toString());
        object.put("stock", getStock());

        JSONObject prices = new JSONObject();
        prices.put("buy", getBuyPrice());
        prices.put("sell", getSellPrice());

        object.put("prices", prices);

        object.put("item", ItemUtil.itemToJSON(item));
        return object;
    }

    public ItemStack getDisplayItem(Shop shop){
        return getDisplayItem(null, shop);
    }

    public boolean isVisible() {
        return getBuyPrice() > 0 && getSellPrice() > 0;
    }


    public ItemStack getDisplayItem(Resident viewer, Shop shop){
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§l" + ItemUtil.getItemName(item));
        List<String> lore = new ArrayList<>();
        lore.add(isInStock()?"§fStock §e"+(isInfinite()?"Infinite": NumberFormat.getInstance().format(getStock())):"§c§oOut of Stock");
        lore.add("§7");

        double sellPrice = getSellPrice();
        double buyPrice = getBuyPrice();
        int maxstack = item.getType().getMaxStackSize();

        if (!isInfinite())

        if (sellPrice > 0){
            lore.add("§fBuy §e1§f for §e" + Populace.getCurrency().format(sellPrice));
            if (maxstack > 1){
                lore.add("§fBuy §e" + maxstack + "§f for §e" + Populace.getCurrency().format(sellPrice*maxstack));
            }
            lore.add("§7");
        }

        if (buyPrice > 0){
            lore.add("§fSell §e1§f for §e" + Populace.getCurrency().format(buyPrice));
            if (maxstack > 1){
                lore.add("§fSell §e" + maxstack + "§f for §e" + Populace.getCurrency().format(buyPrice*maxstack));
            }
            lore.add("§7");
        }

        if (viewer != null) {
            if (viewer.getUuid().equals(shop.getCreator().getUuid())) {
                lore.add("§aLeft Click§f to edit stock.");
                lore.add("§aRight Click§f to edit pricing.");
                lore.add("§cShift Right Click§f to remove item.");
            }
            else{
                if (buyPrice > 0 && sellPrice > 0) {
                    lore.add("§aLeft Click§f to buy from shop.");
                    lore.add("§aRight Click§f to sell to shop.");
                } else if (buyPrice > 0 && sellPrice == 0) {
                    lore.add("§aLeft Click§f to sell to shop.");
                } else {
                    lore.add("§aLeft Click§f to buy from shop.");
                }
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return ItemUtil.addTag(item, "shopitemid", new NBTTagString(getUuid().toString()));
    }

    public boolean remove(Shop shop, boolean force){
        if (!force){
            if (getStock() > 0){
                return false;
            }
        }
        shop.getItems().remove(this);
        return true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean matches(ItemStack item) {
        return item.isSimilar(this.item);
    }

    public boolean isInStock(){
        return stock > 0 || isInfinite();
    }

    public boolean isInfinite(){
        return stock == -1;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setBuyPrice(double buyPrice) {
        if (buyPrice < 0)buyPrice = 0;
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(double sellPrice) {
        if (sellPrice < 0)sellPrice  = 0;
        this.sellPrice = sellPrice;
    }

    public ItemStack getItem() {
        return ItemUtil.deepCopy(item);
    }

    public int getStock() {
        return stock;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public static enum ShopCalculationType {
        NEW_PRICE, TOWN_PROFIT
    }

}
