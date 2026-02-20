package com.hamster;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.*;

public class GlobalHotkeyManager {

    private static final int HOTKEY_ID = 1;
    private static final int MOD_ALT = 0x0001;
    private static final int VK_Q = 0x51;

    private volatile boolean running = false;
    private Thread hotkeyThread;

    public interface HotkeyCallback {
        void onHotkeyPressed();
    }

    public void start(HotkeyCallback callback) {
        if (running) return;
        running = true;

        hotkeyThread = new Thread(() -> {
            boolean registered = User32.INSTANCE.RegisterHotKey(null, HOTKEY_ID, MOD_ALT, VK_Q);
            if (!registered) {
                System.err.println("[GlobalHotkey] ALT+Q 등록 실패 - 다른 앱이 사용 중일 수 있습니다.");
                running = false;
                return;
            }
            System.out.println("[GlobalHotkey] ALT+Q 핫키 등록 완료");

            WinUser.MSG msg = new WinUser.MSG();
            while (running) {
                boolean hasMessage = User32.INSTANCE.PeekMessage(msg, null, 0, 0, 1);
                if (hasMessage) {
                    if (msg.message == WinUser.WM_HOTKEY && msg.wParam.intValue() == HOTKEY_ID) {
                        SwingUtilities.invokeLater(callback::onHotkeyPressed);
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }

            User32.INSTANCE.UnregisterHotKey(null, HOTKEY_ID);
            System.out.println("[GlobalHotkey] ALT+Q 핫키 해제 완료");
        });
        hotkeyThread.setDaemon(true);
        hotkeyThread.setName("GlobalHotkey-Thread");
        hotkeyThread.start();
    }

    public void stop() {
        running = false;
        if (hotkeyThread != null) {
            hotkeyThread.interrupt();
        }
    }
}
