package com.hamster.system;
import com.hamster.model.Settings;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GlobalHotkeyManager {

    private static final int HOTKEY_TOGGLE = 1;
    private static final int HOTKEY_SENDBACK = 2;
    private static final int HOTKEY_PANEL_TOGGLE = 3;

    private volatile boolean running = false;
    private volatile Runnable toggleCallback;
    private volatile Runnable sendBackCallback;
    private volatile Runnable panelToggleCallback;
    private Thread hotkeyThread;

    // Current registered keys
    private volatile int toggleMod, toggleVK;
    private volatile int sendBackMod, sendBackVK;
    private volatile int panelToggleMod, panelToggleVK;

    // Pending re-registration
    private volatile int pendingToggleMod = -1, pendingToggleVK = -1;
    private volatile int pendingSendBackMod = -1, pendingSendBackVK = -1;
    private volatile int pendingPanelToggleMod = -1, pendingPanelToggleVK = -1;
    private volatile boolean reregisterRequested = false;

    public interface HotkeyCallback {
        void onHotkeyPressed();
    }

    public interface FailureCallback {
        void onRegistrationFailed(String hotkeyName, String hotkeyText);
    }

    private volatile FailureCallback failureCallback;

    public void setCallback(Runnable callback) { this.toggleCallback = callback; }
    public void setSendBackCallback(Runnable callback) { this.sendBackCallback = callback; }
    public void setPanelToggleCallback(Runnable callback) { this.panelToggleCallback = callback; }
    public void setFailureCallback(FailureCallback callback) { this.failureCallback = callback; }

    public void start(Settings settings) {
        if (running) return;
        running = true;

        toggleMod = settings.toggleModifier;
        toggleVK = settings.toggleKeyCode;
        sendBackMod = settings.sendBackModifier;
        sendBackVK = settings.sendBackKeyCode;
        panelToggleMod = settings.panelToggleModifier;
        panelToggleVK = settings.panelToggleKeyCode;

        hotkeyThread = new Thread(() -> {
            registerHotkeys();

            WinUser.MSG msg = new WinUser.MSG();
            while (running) {
                // Suspend: unregister all hotkeys temporarily
                if (suspendRequested) {
                    suspendRequested = false;
                    User32.INSTANCE.UnregisterHotKey(null, HOTKEY_TOGGLE);
                    User32.INSTANCE.UnregisterHotKey(null, HOTKEY_SENDBACK);
                    User32.INSTANCE.UnregisterHotKey(null, HOTKEY_PANEL_TOGGLE);
                    System.out.println("[GlobalHotkey] \uC77C\uC2DC \uC911\uC9C0");
                }

                // Resume: re-register with (possibly new) keys
                if (resumeRequested) {
                    resumeRequested = false;
                    if (pendingToggleMod >= 0) { toggleMod = pendingToggleMod; toggleVK = pendingToggleVK; }
                    if (pendingSendBackMod >= 0) { sendBackMod = pendingSendBackMod; sendBackVK = pendingSendBackVK; }
                    if (pendingPanelToggleMod >= 0) { panelToggleMod = pendingPanelToggleMod; panelToggleVK = pendingPanelToggleVK; }
                    pendingToggleMod = pendingToggleVK = -1;
                    pendingSendBackMod = pendingSendBackVK = -1;
                    pendingPanelToggleMod = pendingPanelToggleVK = -1;
                    registerHotkeys();
                }

                if (reregisterRequested) {
                    reregisterRequested = false;
                    User32.INSTANCE.UnregisterHotKey(null, HOTKEY_TOGGLE);
                    User32.INSTANCE.UnregisterHotKey(null, HOTKEY_SENDBACK);
                    User32.INSTANCE.UnregisterHotKey(null, HOTKEY_PANEL_TOGGLE);

                    if (pendingToggleMod >= 0) { toggleMod = pendingToggleMod; toggleVK = pendingToggleVK; }
                    if (pendingSendBackMod >= 0) { sendBackMod = pendingSendBackMod; sendBackVK = pendingSendBackVK; }
                    if (pendingPanelToggleMod >= 0) { panelToggleMod = pendingPanelToggleMod; panelToggleVK = pendingPanelToggleVK; }
                    pendingToggleMod = pendingToggleVK = -1;
                    pendingSendBackMod = pendingSendBackVK = -1;
                    pendingPanelToggleMod = pendingPanelToggleVK = -1;

                    registerHotkeys();
                }

                boolean hasMessage = User32.INSTANCE.PeekMessage(msg, null, 0, 0, 1);
                if (hasMessage && msg.message == WinUser.WM_HOTKEY) {
                    int id = msg.wParam.intValue();
                    if (id == HOTKEY_TOGGLE) {
                        Runnable cb = toggleCallback;
                        if (cb != null) SwingUtilities.invokeLater(cb);
                    } else if (id == HOTKEY_SENDBACK) {
                        Runnable cb = sendBackCallback;
                        if (cb != null) SwingUtilities.invokeLater(cb);
                    } else if (id == HOTKEY_PANEL_TOGGLE) {
                        Runnable cb = panelToggleCallback;
                        if (cb != null) SwingUtilities.invokeLater(cb);
                    }
                }
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }

            User32.INSTANCE.UnregisterHotKey(null, HOTKEY_TOGGLE);
            User32.INSTANCE.UnregisterHotKey(null, HOTKEY_SENDBACK);
            User32.INSTANCE.UnregisterHotKey(null, HOTKEY_PANEL_TOGGLE);
        });
        hotkeyThread.setDaemon(true);
        hotkeyThread.setName("GlobalHotkey-Thread");
        hotkeyThread.start();
    }

    private void registerHotkeys() {
        List<String> failures = new ArrayList<>();

        registerOne(HOTKEY_TOGGLE, toggleMod, toggleVK, failures);
        registerOne(HOTKEY_SENDBACK, sendBackMod, sendBackVK, failures);
        registerOne(HOTKEY_PANEL_TOGGLE, panelToggleMod, panelToggleVK, failures);

        if (!failures.isEmpty()) {
            FailureCallback fc = failureCallback;
            if (fc != null) {
                String joined = String.join(", ", failures);
                SwingUtilities.invokeLater(() -> fc.onRegistrationFailed("\uB2E8\uCD95\uD0A4", joined));
            }
        }
    }

    private void registerOne(int id, int mod, int vk, List<String> failures) {
        String text = Settings.modifierToString(mod) + Settings.keyCodeToString(vk);
        boolean ok = User32.INSTANCE.RegisterHotKey(null, id, mod, vk);
        if (ok) {
            System.out.println("[GlobalHotkey] " + text + " \uB4F1\uB85D \uC644\uB8CC");
        } else {
            System.err.println("[GlobalHotkey] " + text + " \uB4F1\uB85D \uC2E4\uD328");
            failures.add(text);
        }
    }

    public void updateHotkeys(Settings settings) {
        pendingToggleMod = settings.toggleModifier;
        pendingToggleVK = settings.toggleKeyCode;
        pendingSendBackMod = settings.sendBackModifier;
        pendingSendBackVK = settings.sendBackKeyCode;
        pendingPanelToggleMod = settings.panelToggleModifier;
        pendingPanelToggleVK = settings.panelToggleKeyCode;
        reregisterRequested = true;
    }

    /** Temporarily unregister all hotkeys (e.g. while settings dialog is open) */
    private volatile boolean suspendRequested = false;
    private volatile boolean resumeRequested = false;

    public void suspend() {
        suspendRequested = true;
    }

    public void resume(Settings settings) {
        pendingToggleMod = settings.toggleModifier;
        pendingToggleVK = settings.toggleKeyCode;
        pendingSendBackMod = settings.sendBackModifier;
        pendingSendBackVK = settings.sendBackKeyCode;
        pendingPanelToggleMod = settings.panelToggleModifier;
        pendingPanelToggleVK = settings.panelToggleKeyCode;
        resumeRequested = true;
    }

    public void stop() {
        running = false;
        if (hotkeyThread != null) hotkeyThread.interrupt();
    }
}
