package com.turqmelon.PopulaceMarket.shops;

import com.turqmelon.MelonEco.utils.Account;
import com.turqmelon.MelonEco.utils.AccountManager;
import com.turqmelon.Populace.Plot.Plot;
import com.turqmelon.Populace.Plot.PlotChunk;
import com.turqmelon.Populace.Plot.PlotManager;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.PopulaceMarket.gui.ShopGUI;
import net.minecraft.server.v1_8_R3.MojangsonParseException;
import org.bukkit.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class Shop {

    private UUID uuid;
    private String title;
    private Account fundSource;
    private Resident creator;
    private Location location;

    private List<ShopItem> items = new ArrayList<>();

    public Shop(UUID uuid, String title, Account fundSource, Resident creator, Location location) {
        this.uuid = uuid;
        this.title = title;
        this.fundSource = fundSource;
        this.creator = creator;
        this.location = location;
    }

    public Shop(JSONObject object) throws MojangsonParseException {
        this.uuid = UUID.fromString((String) object.get("uuid"));
        this.title = (String) object.get("title");
        this.fundSource = AccountManager.getAccount(UUID.fromString((String) object.get("fundsource")));
        this.creator = ResidentManager.getResident(UUID.fromString((String) object.get("creator")));
        JSONArray shopItems = (JSONArray) object.get("items");
        for(Object o : shopItems){
            JSONObject obj = (JSONObject)o;
            getItems().add(new ShopItem(obj));
        }
        String[] loc = ((String) object.get("location")).split(",");
        World world = Bukkit.getWorld(loc[0]);
        if (world == null){
            world = Bukkit.getWorlds().get(0);
        }
        int x = Integer.parseInt(loc[1]);
        int y = Integer.parseInt(loc[2]);
        int z = Integer.parseInt(loc[3]);
        this.location = new Location(world, x, y, z);
    }


    public Town getTown(){
        Plot plot = PlotManager.getPlot(getChunk());
        return plot != null ? plot.getTown() : null;
    }

    public PlotChunk getChunk(){
        Chunk ch = getLocation().getChunk();
        return new PlotChunk(getLocation().getWorld(), ch.getX(), ch.getZ());
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public JSONObject toJSON(){
        JSONObject object = new JSONObject();
        object.put("uuid", getUuid().toString());
        object.put("title", getTitle());
        object.put("fundsource", getFundSource().getUuid().toString());
        object.put("creator", getCreator().getUuid().toString());
        JSONArray shopItems = new JSONArray();
        for(ShopItem item : items){
            shopItems.add(item.toJSON());
        }
        object.put("items", shopItems);
        object.put("location", getLocation().getWorld().getName() + "," + getLocation().getBlockX() + "," + getLocation().getBlockY() + "," + getLocation().getBlockZ());
        return object;
    }

    public boolean destroy(boolean force){
        if (!force){
            if (getItems().size() > 0){
                return false;
            }
        }

        getLocation().getBlock().setType(Material.AIR);
        getLocation().getWorld().playEffect(getLocation(), Effect.EXPLOSION_HUGE, 1, 1);
        getLocation().getWorld().playSound(getLocation(), Sound.EXPLODE, 1, 0);
        getLocation().getWorld().dropItemNaturally(getLocation(), new ItemBuilder(Material.ENDER_CHEST)
                .withCustomName("§e§lShop Chest").withLore(Arrays.asList("§7Place in a §bMerchant Plot§7.")).build());

        ShopManager.getShops().remove(this);
        return true;
    }

    public ShopGUI getGUI(Resident resident, ShopItem item){
        return new ShopGUI(resident, this, item);
    }

    public ShopGUI getGUI(Resident resident){
        return new ShopGUI(resident, this, 1);
    }

    public Location getLocation() {
        return location;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public Account getFundSource() {
        return fundSource;
    }



    public Resident getCreator() {
        return creator;
    }

    public ShopItem getItem(UUID uuid){
        for(ShopItem item : getItems()) {
            if (item.getUuid().equals(uuid)) {
                return item;
            }
        }
        return null;
    }

    public List<ShopItem> getItems() {
        return items;
    }
}
