package com.turqmelon.PopulaceMarket.shops;


import com.turqmelon.Populace.Resident.Resident;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopManager {

    private static List<Shop> shops = new ArrayList<>();

    public static List<Shop> getShops(Resident resident){
        return getShops().stream().filter(shop -> shop.getCreator().getUuid().equals(resident.getUuid())).collect(Collectors.toList());
    }

    public static Shop getShop(UUID uuid){
        for(Shop shop : getShops()){
            if (shop.getUuid().equals(uuid)){
                return shop;
            }
        }
        return null;
    }

    public static List<Shop> getShops() {
        return shops;
    }
}
