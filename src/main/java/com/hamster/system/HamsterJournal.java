package com.hamster.system;
import com.hamster.model.Hamster;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HamsterJournal {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.desktophamster/";
    private static final String JOURNAL_FILE = SAVE_DIR + "journal.properties";

    public static class JournalEntry {
        public final String name;
        public final String color;
        public final String personality;
        public final int generation;
        public final int lifespanDays;
        public final String causeOfDeath;
        public final long timestamp;

        public JournalEntry(String name, String color, String personality,
                            int generation, int lifespanDays, String causeOfDeath, long timestamp) {
            this.name = name;
            this.color = color;
            this.personality = personality;
            this.generation = generation;
            this.lifespanDays = lifespanDays;
            this.causeOfDeath = causeOfDeath;
            this.timestamp = timestamp;
        }
    }

    private final List<JournalEntry> entries = new ArrayList<>();

    public void addEntry(Hamster hamster, String causeOfDeath) {
        String personalityName = hamster.getPersonality() != null
                ? hamster.getPersonality().getDisplayName() : "\uBBF8\uC0C1";
        JournalEntry entry = new JournalEntry(
                hamster.getName(),
                hamster.getColor().getDisplayName(),
                personalityName,
                hamster.getGeneration(),
                hamster.getAgeDays(),
                causeOfDeath,
                System.currentTimeMillis()
        );
        entries.add(0, entry); // newest first
    }

    public List<JournalEntry> getEntries() {
        return entries;
    }

    public int getEntryCount() {
        return entries.size();
    }

    public void save() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        props.setProperty("count", String.valueOf(entries.size()));
        for (int i = 0; i < entries.size(); i++) {
            JournalEntry e = entries.get(i);
            String prefix = "entry." + i + ".";
            props.setProperty(prefix + "name", e.name);
            props.setProperty(prefix + "color", e.color);
            props.setProperty(prefix + "personality", e.personality);
            props.setProperty(prefix + "generation", String.valueOf(e.generation));
            props.setProperty(prefix + "lifespanDays", String.valueOf(e.lifespanDays));
            props.setProperty(prefix + "causeOfDeath", e.causeOfDeath);
            props.setProperty(prefix + "timestamp", String.valueOf(e.timestamp));
        }

        try (FileOutputStream fos = new FileOutputStream(JOURNAL_FILE)) {
            props.store(fos, "DesktopHamster Journal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HamsterJournal load() {
        HamsterJournal journal = new HamsterJournal();
        File file = new File(JOURNAL_FILE);
        if (!file.exists()) return journal;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            return journal;
        }

        int count = Integer.parseInt(props.getProperty("count", "0"));
        for (int i = 0; i < count; i++) {
            String prefix = "entry." + i + ".";
            JournalEntry entry = new JournalEntry(
                    props.getProperty(prefix + "name", "\uD584\uC2A4\uD130"),
                    props.getProperty(prefix + "color", "\uAC08\uC0C9"),
                    props.getProperty(prefix + "personality", "\uBBF8\uC0C1"),
                    Integer.parseInt(props.getProperty(prefix + "generation", "1")),
                    Integer.parseInt(props.getProperty(prefix + "lifespanDays", "0")),
                    props.getProperty(prefix + "causeOfDeath", "\uBBF8\uC0C1"),
                    Long.parseLong(props.getProperty(prefix + "timestamp", "0"))
            );
            journal.entries.add(entry);
        }

        return journal;
    }
}
