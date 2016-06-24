package com.turqmelon.PopulaceMarket;

import com.turqmelon.Populace.Utils.ItemBuilder;
import com.turqmelon.PopulaceMarket.Listeners.BlockListener;
import com.turqmelon.PopulaceMarket.shops.Shop;
import com.turqmelon.PopulaceMarket.shops.ShopManager;
import net.minecraft.server.v1_9_R2.MojangsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@SuppressWarnings("unchecked")
public class PopulaceMarket extends JavaPlugin {

    private static PopulaceMarket instance;

    public static void loadData() throws IOException, ParseException, MojangsonParseException {

        File dir = getInstance().getDataFolder();
        if (dir.exists()) {
            File file = new File(dir, "data.json");
            if (file.exists()) {

                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(new FileReader(file));

                JSONArray shopArray = (JSONArray) object.get("shops");
                for (Object obj : shopArray) {
                    JSONObject shopObject = (JSONObject) obj;
                    Shop shop = new Shop(shopObject);
                    ShopManager.getShops().add(shop);
                }

                getInstance().getLogger().log(Level.INFO, "Loaded " + ShopManager.getShops().size() + " shops.");

            }
        }

    }

    public static void saveData() throws IOException {

        getInstance().getLogger().log(Level.INFO, "Saving...");

        File dir = getInstance().getDataFolder();
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, "data.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        JSONObject data = new JSONObject();

        JSONArray shops = new JSONArray();
        for (Shop shop : ShopManager.getShops()) {
            shops.add(shop.toJSON());
        }

        data.put("shops", shops);

        FileWriter writer = new FileWriter(file);
        writer.write(data.toJSONString());
        writer.flush();
        writer.close();

        getInstance().getLogger().log(Level.INFO, "Saved.");

    }

    public static PopulaceMarket getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        try {
            saveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {

        instance = this;

        ShapelessRecipe recipe = new ShapelessRecipe(new ItemBuilder(Material.ENDER_CHEST)
                .withCustomName("§e§lShop Chest").withLore(Arrays.asList("§7Place in a §bMerchant Plot§7.")).build());
        recipe.addIngredient(Material.ENDER_CHEST);
        recipe.addIngredient(Material.DIAMOND);
        Bukkit.addRecipe(recipe);

        getServer().getPluginManager().registerEvents(new BlockListener(), this);

        try {
            loadData();
        } catch (IOException | ParseException | MojangsonParseException e) {
            e.printStackTrace();
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    saveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this, TimeUnit.MINUTES.toMillis(10), TimeUnit.MINUTES.toMillis(10));

    }
}
