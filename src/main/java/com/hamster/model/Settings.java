package com.hamster.model;

import com.hamster.system.GameLogger;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Properties;

public class Settings {

    private static final String SAVE_DIR = GameConstants.SAVE_DIR;
    private static final String SETTINGS_FILE = SAVE_DIR + "settings.properties";

    // Toggle hotkey (hide/show) - default ALT+Q
    public int toggleModifier = MOD_ALT;
    public int toggleKeyCode = 0x51; // VK_Q

    // Send-back hotkey - default ALT+W
    public int sendBackModifier = MOD_ALT;
    public int sendBackKeyCode = 0x57; // VK_W

    // Control panel toggle hotkey - default ALT+E
    public int panelToggleModifier = MOD_ALT;
    public int panelToggleKeyCode = 0x45; // VK_E

    // Opacity (20~100)
    public int opacity = 100;

    // Windows API modifier constants
    public static final int MOD_ALT = 0x0001;
    public static final int MOD_CTRL = 0x0002;
    public static final int MOD_SHIFT = 0x0004;

    public void save() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        props.setProperty("toggleModifier", String.valueOf(toggleModifier));
        props.setProperty("toggleKeyCode", String.valueOf(toggleKeyCode));
        props.setProperty("sendBackModifier", String.valueOf(sendBackModifier));
        props.setProperty("sendBackKeyCode", String.valueOf(sendBackKeyCode));
        props.setProperty("panelToggleModifier", String.valueOf(panelToggleModifier));
        props.setProperty("panelToggleKeyCode", String.valueOf(panelToggleKeyCode));
        props.setProperty("opacity", String.valueOf(opacity));

        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            props.store(fos, "DesktopHamster Settings");
        } catch (IOException e) {
            GameLogger.error("Failed to save settings", e);
        }
    }

    public static Settings load() {
        Settings s = new Settings();
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) return s;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return s;
        }

        s.toggleModifier = Integer.parseInt(props.getProperty("toggleModifier", String.valueOf(MOD_ALT)));
        s.toggleKeyCode = Integer.parseInt(props.getProperty("toggleKeyCode", "81"));
        s.sendBackModifier = Integer.parseInt(props.getProperty("sendBackModifier", String.valueOf(MOD_ALT)));
        s.sendBackKeyCode = Integer.parseInt(props.getProperty("sendBackKeyCode", "87"));
        s.panelToggleModifier = Integer.parseInt(props.getProperty("panelToggleModifier", String.valueOf(MOD_ALT)));
        s.panelToggleKeyCode = Integer.parseInt(props.getProperty("panelToggleKeyCode", "69"));
        s.opacity = Integer.parseInt(props.getProperty("opacity", "100"));
        return s;
    }

    /** Converts Windows API modifier flags to a display string */
    public static String modifierToString(int mod) {
        StringBuilder sb = new StringBuilder();
        if ((mod & MOD_CTRL) != 0) sb.append("CTRL+");
        if ((mod & MOD_ALT) != 0) sb.append("ALT+");
        if ((mod & MOD_SHIFT) != 0) sb.append("SHIFT+");
        return sb.toString();
    }

    /** Converts a Windows VK code to display string */
    public static String keyCodeToString(int vk) {
        if (vk >= 0x41 && vk <= 0x5A) return String.valueOf((char) vk);
        if (vk >= 0x30 && vk <= 0x39) return String.valueOf((char) vk);
        if (vk >= 0x70 && vk <= 0x7B) return "F" + (vk - 0x70 + 1);
        switch (vk) {
            case 0x20: return "SPACE";
            case 0x0D: return "ENTER";
            case 0x1B: return "ESC";
            case 0x09: return "TAB";
            case 0xBE: return ".";
            case 0xBC: return ",";
            default: return "0x" + Integer.toHexString(vk).toUpperCase();
        }
    }

    public String getToggleHotkeyText() {
        return modifierToString(toggleModifier) + keyCodeToString(toggleKeyCode);
    }

    public String getSendBackHotkeyText() {
        return modifierToString(sendBackModifier) + keyCodeToString(sendBackKeyCode);
    }

    public String getPanelToggleHotkeyText() {
        return modifierToString(panelToggleModifier) + keyCodeToString(panelToggleKeyCode);
    }

    /** Convert Java KeyEvent modifiers to Windows API modifiers */
    public static int javaModToWinMod(int javaModifiers) {
        int winMod = 0;
        if ((javaModifiers & KeyEvent.ALT_DOWN_MASK) != 0) winMod |= MOD_ALT;
        if ((javaModifiers & KeyEvent.CTRL_DOWN_MASK) != 0) winMod |= MOD_CTRL;
        if ((javaModifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) winMod |= MOD_SHIFT;
        return winMod;
    }

    /** Convert Java KeyEvent keyCode to Windows VK code */
    public static int javaKeyToWinVK(int javaKeyCode) {
        return javaKeyCode;
    }
}
