package com.hamster.model;

import java.io.*;
import java.util.Properties;

public class GameStatistics {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.desktophamster/";
    private static final String STATS_FILE = SAVE_DIR + "statistics.properties";

    public long totalPlayTimeFrames = 0;
    public int totalHamstersRaised = 0;
    public int maxGenerationReached = 0;
    public int longestLifespanDays = 0;
    public long totalCoinsEarned = 0;
    public long totalCoinsSpent = 0;
    public int totalPoopsCleaned = 0;
    public int totalEventsTriggered = 0;
    public int totalBreeds = 0;
    public int totalFeedActions = 0;
    public int totalPlayActions = 0;
    public int totalSleepActions = 0;
    public int totalWheelActions = 0;
    public int totalInteractions = 0;
    public int totalDeaths = 0;
    public int totalGamesPlayed = 0;

    public void save() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        props.setProperty("totalPlayTimeFrames", String.valueOf(totalPlayTimeFrames));
        props.setProperty("totalHamstersRaised", String.valueOf(totalHamstersRaised));
        props.setProperty("maxGenerationReached", String.valueOf(maxGenerationReached));
        props.setProperty("longestLifespanDays", String.valueOf(longestLifespanDays));
        props.setProperty("totalCoinsEarned", String.valueOf(totalCoinsEarned));
        props.setProperty("totalCoinsSpent", String.valueOf(totalCoinsSpent));
        props.setProperty("totalPoopsCleaned", String.valueOf(totalPoopsCleaned));
        props.setProperty("totalEventsTriggered", String.valueOf(totalEventsTriggered));
        props.setProperty("totalBreeds", String.valueOf(totalBreeds));
        props.setProperty("totalFeedActions", String.valueOf(totalFeedActions));
        props.setProperty("totalPlayActions", String.valueOf(totalPlayActions));
        props.setProperty("totalSleepActions", String.valueOf(totalSleepActions));
        props.setProperty("totalWheelActions", String.valueOf(totalWheelActions));
        props.setProperty("totalInteractions", String.valueOf(totalInteractions));
        props.setProperty("totalDeaths", String.valueOf(totalDeaths));
        props.setProperty("totalGamesPlayed", String.valueOf(totalGamesPlayed));

        try (FileOutputStream fos = new FileOutputStream(STATS_FILE)) {
            props.store(fos, "DesktopHamster Statistics");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameStatistics load() {
        GameStatistics stats = new GameStatistics();
        File file = new File(STATS_FILE);
        if (!file.exists()) return stats;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            return stats;
        }

        stats.totalPlayTimeFrames = Long.parseLong(props.getProperty("totalPlayTimeFrames", "0"));
        stats.totalHamstersRaised = Integer.parseInt(props.getProperty("totalHamstersRaised", "0"));
        stats.maxGenerationReached = Integer.parseInt(props.getProperty("maxGenerationReached", "0"));
        stats.longestLifespanDays = Integer.parseInt(props.getProperty("longestLifespanDays", "0"));
        stats.totalCoinsEarned = Long.parseLong(props.getProperty("totalCoinsEarned", "0"));
        stats.totalCoinsSpent = Long.parseLong(props.getProperty("totalCoinsSpent", "0"));
        stats.totalPoopsCleaned = Integer.parseInt(props.getProperty("totalPoopsCleaned", "0"));
        stats.totalEventsTriggered = Integer.parseInt(props.getProperty("totalEventsTriggered", "0"));
        stats.totalBreeds = Integer.parseInt(props.getProperty("totalBreeds", "0"));
        stats.totalFeedActions = Integer.parseInt(props.getProperty("totalFeedActions", "0"));
        stats.totalPlayActions = Integer.parseInt(props.getProperty("totalPlayActions", "0"));
        stats.totalSleepActions = Integer.parseInt(props.getProperty("totalSleepActions", "0"));
        stats.totalWheelActions = Integer.parseInt(props.getProperty("totalWheelActions", "0"));
        stats.totalInteractions = Integer.parseInt(props.getProperty("totalInteractions", "0"));
        stats.totalDeaths = Integer.parseInt(props.getProperty("totalDeaths", "0"));
        stats.totalGamesPlayed = Integer.parseInt(props.getProperty("totalGamesPlayed", "0"));

        return stats;
    }
}
