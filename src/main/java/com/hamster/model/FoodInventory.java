package com.hamster.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class FoodInventory {

    private final EnumMap<FoodItem, Integer> items = new EnumMap<>(FoodItem.class);

    public FoodInventory() {
        // Start with 5 seeds
        items.put(FoodItem.SEED, 5);
    }

    public void add(FoodItem food, int count) {
        int current = items.containsKey(food) ? items.get(food) : 0;
        items.put(food, current + count);
    }

    public boolean remove(FoodItem food) {
        int current = items.containsKey(food) ? items.get(food) : 0;
        if (current <= 0) return false;
        if (current == 1) {
            items.remove(food);
        } else {
            items.put(food, current - 1);
        }
        return true;
    }

    public int getCount(FoodItem food) {
        return items.containsKey(food) ? items.get(food) : 0;
    }

    public boolean isEmpty() {
        for (Integer count : items.values()) {
            if (count > 0) return false;
        }
        return true;
    }

    public boolean hasAny() {
        return !isEmpty();
    }

    public void saveToProperties(Properties props, String prefix) {
        for (FoodItem food : FoodItem.values()) {
            int count = getCount(food);
            if (count > 0) {
                props.setProperty(prefix + "food." + food.name(), String.valueOf(count));
            }
        }
    }

    public static FoodInventory loadFromProperties(Properties props, String prefix) {
        FoodInventory inv = new FoodInventory();
        inv.items.clear(); // Clear default seeds
        for (FoodItem food : FoodItem.values()) {
            String val = props.getProperty(prefix + "food." + food.name());
            if (val != null) {
                try {
                    int count = Integer.parseInt(val);
                    if (count > 0) inv.items.put(food, count);
                } catch (NumberFormatException ignored) {}
            }
        }
        return inv;
    }

    public Map<FoodItem, Integer> getAllItems() {
        return new EnumMap<>(items);
    }
}
