package com.hamster.system;
import com.hamster.model.Accessory;
import com.hamster.model.FoodItem;
import com.hamster.model.Hamster;
import com.hamster.model.HamsterColor;
import com.hamster.model.Personality;

import java.io.*;
import java.util.*;

public class AchievementManager {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.desktophamster/";
    private static final String ACH_FILE = SAVE_DIR + "achievements.properties";

    private final Set<String> unlocked = new HashSet<>();

    // Cumulative counters
    public int totalPoopsCleaned = 0;
    public int totalEventsTriggered = 0;
    public int totalBreeds = 0;
    public int totalPlays = 0;
    public int totalInteractions = 0;
    public boolean maxStatReached = false;

    // Collection tracking
    public final Set<String> colorsSeen = new HashSet<>();
    public final Set<String> foodsTried = new HashSet<>();
    public final Set<String> personalitiesSeen = new HashSet<>();
    public final Set<String> accessoriesBought = new HashSet<>();

    public boolean isUnlocked(Achievement ach) {
        return unlocked.contains(ach.name());
    }

    public List<Achievement> checkAndUnlock(int hamstersRaised, int maxGeneration,
                                             long totalCoinsEarned, int longestLifespanDays,
                                             List<Hamster> hamsters) {
        List<Achievement> newlyUnlocked = new ArrayList<>();

        check(newlyUnlocked, Achievement.FIRST_HAMSTER, hamstersRaised >= 1);
        check(newlyUnlocked, Achievement.FIVE_HAMSTERS, hamstersRaised >= 5);
        check(newlyUnlocked, Achievement.TEN_HAMSTERS, hamstersRaised >= 10);
        check(newlyUnlocked, Achievement.THREE_GENERATIONS, maxGeneration >= 3);
        check(newlyUnlocked, Achievement.TEN_GENERATIONS, maxGeneration >= 10);

        check(newlyUnlocked, Achievement.FIVE_HUNDRED_COINS, totalCoinsEarned >= 500);
        check(newlyUnlocked, Achievement.FIVE_THOUSAND_COINS, totalCoinsEarned >= 5000);

        // Check max stat (uses persistent flag)
        check(newlyUnlocked, Achievement.MAX_STAT, maxStatReached);

        check(newlyUnlocked, Achievement.SURVIVE_30, longestLifespanDays >= 30);
        check(newlyUnlocked, Achievement.SURVIVE_50, longestLifespanDays >= 50);

        check(newlyUnlocked, Achievement.POOP_50, totalPoopsCleaned >= 50);
        check(newlyUnlocked, Achievement.POOP_200, totalPoopsCleaned >= 200);
        check(newlyUnlocked, Achievement.EVENT_20, totalEventsTriggered >= 20);
        check(newlyUnlocked, Achievement.EVENT_100, totalEventsTriggered >= 100);
        check(newlyUnlocked, Achievement.BREED_5, totalBreeds >= 5);
        check(newlyUnlocked, Achievement.BREED_20, totalBreeds >= 20);
        check(newlyUnlocked, Achievement.PLAY_100, totalPlays >= 100);

        check(newlyUnlocked, Achievement.ALL_COLORS, colorsSeen.size() >= HamsterColor.values().length);
        check(newlyUnlocked, Achievement.ALL_FOODS, foodsTried.size() >= FoodItem.values().length);
        check(newlyUnlocked, Achievement.ALL_PERSONALITIES, personalitiesSeen.size() >= Personality.values().length);
        check(newlyUnlocked, Achievement.FIRST_ACCESSORY, !accessoriesBought.isEmpty());
        check(newlyUnlocked, Achievement.ALL_ACCESSORIES, accessoriesBought.size() >= Accessory.values().length);

        // Time checks
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        check(newlyUnlocked, Achievement.MIDNIGHT, hour == 0);
        check(newlyUnlocked, Achievement.EARLY_BIRD, hour == 6);

        check(newlyUnlocked, Achievement.INTERACTION_50, totalInteractions >= 50);

        return newlyUnlocked;
    }

    private void check(List<Achievement> list, Achievement ach, boolean condition) {
        if (condition && !unlocked.contains(ach.name())) {
            unlocked.add(ach.name());
            list.add(ach);
        }
    }

    public int getUnlockedCount() {
        return unlocked.size();
    }

    public void save() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        props.setProperty("unlocked", join(unlocked));
        props.setProperty("totalPoopsCleaned", String.valueOf(totalPoopsCleaned));
        props.setProperty("totalEventsTriggered", String.valueOf(totalEventsTriggered));
        props.setProperty("totalBreeds", String.valueOf(totalBreeds));
        props.setProperty("totalPlays", String.valueOf(totalPlays));
        props.setProperty("totalInteractions", String.valueOf(totalInteractions));
        props.setProperty("maxStatReached", String.valueOf(maxStatReached));
        props.setProperty("colorsSeen", join(colorsSeen));
        props.setProperty("foodsTried", join(foodsTried));
        props.setProperty("personalitiesSeen", join(personalitiesSeen));
        props.setProperty("accessoriesBought", join(accessoriesBought));

        try (FileOutputStream fos = new FileOutputStream(ACH_FILE)) {
            props.store(fos, "DesktopHamster Achievements");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AchievementManager load() {
        AchievementManager mgr = new AchievementManager();
        File file = new File(ACH_FILE);
        if (!file.exists()) return mgr;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            return mgr;
        }

        split(props.getProperty("unlocked", ""), mgr.unlocked);
        mgr.totalPoopsCleaned = Integer.parseInt(props.getProperty("totalPoopsCleaned", "0"));
        mgr.totalEventsTriggered = Integer.parseInt(props.getProperty("totalEventsTriggered", "0"));
        mgr.totalBreeds = Integer.parseInt(props.getProperty("totalBreeds", "0"));
        mgr.totalPlays = Integer.parseInt(props.getProperty("totalPlays", "0"));
        mgr.totalInteractions = Integer.parseInt(props.getProperty("totalInteractions", "0"));
        mgr.maxStatReached = Boolean.parseBoolean(props.getProperty("maxStatReached", "false"));
        split(props.getProperty("colorsSeen", ""), mgr.colorsSeen);
        split(props.getProperty("foodsTried", ""), mgr.foodsTried);
        split(props.getProperty("personalitiesSeen", ""), mgr.personalitiesSeen);
        split(props.getProperty("accessoriesBought", ""), mgr.accessoriesBought);

        return mgr;
    }

    private static String join(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            if (sb.length() > 0) sb.append(",");
            sb.append(s);
        }
        return sb.toString();
    }

    private static void split(String str, Set<String> set) {
        if (str == null || str.isEmpty()) return;
        String[] parts = str.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) set.add(trimmed);
        }
    }
}
