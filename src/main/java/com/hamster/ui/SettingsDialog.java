package com.hamster.ui;
import com.hamster.model.Settings;
import com.hamster.render.HamsterIcon;
import com.hamster.system.GlobalHotkeyManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingsDialog {

    private static final String FONT_NAME = "Noto Sans KR";

    public interface OnSave {
        void onSettingsSaved(Settings settings);
    }

    public static void show(Component parent, Settings settings, GlobalHotkeyManager hotkeyManager, OnSave onSave) {
        // Suspend global hotkeys so they don't intercept key capture
        if (hotkeyManager != null) hotkeyManager.suspend();

        JDialog dialog = new JDialog((Frame) null, "\uC124\uC815", true);
        dialog.setResizable(false);
        dialog.setIconImages(HamsterIcon.createIcons());
        dialog.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        panel.setBackground(new Color(255, 250, 240));
        GridBagConstraints gbc = new GridBagConstraints();

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel title = new JLabel(ControlPanel.wrapEmoji("\u2699 \uC124\uC815"));
        title.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        title.setForeground(new Color(80, 50, 20));
        panel.add(title, gbc);

        // Section title
        gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.WEST;
        JLabel sectionTitle = new JLabel("\uB2E8\uCD95\uD0A4 \uC124\uC815");
        sectionTitle.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        sectionTitle.setForeground(new Color(100, 70, 30));
        panel.add(sectionTitle, gbc);

        // --- Row 1: Toggle (hide/show) ---
        int[] toggleMod = {settings.toggleModifier};
        int[] toggleKey = {settings.toggleKeyCode};
        JButton toggleBtn = createHotkeyButton(settings.getToggleHotkeyText());
        setupKeyCapture(dialog, toggleBtn, toggleMod, toggleKey);
        addHotkeyRow(panel, gbc, 2, "\uC228\uAE30\uAE30/\uBCF4\uC774\uAE30:", toggleBtn);

        // --- Row 2: Send-back ---
        int[] sendBackMod = {settings.sendBackModifier};
        int[] sendBackKey = {settings.sendBackKeyCode};
        JButton sendBackBtn = createHotkeyButton(settings.getSendBackHotkeyText());
        setupKeyCapture(dialog, sendBackBtn, sendBackMod, sendBackKey);
        addHotkeyRow(panel, gbc, 3, "\uB4A4\uB85C \uBCF4\uB0B4\uAE30:", sendBackBtn);

        // --- Row 3: Panel toggle ---
        int[] panelMod = {settings.panelToggleModifier};
        int[] panelKey = {settings.panelToggleKeyCode};
        JButton panelBtn = createHotkeyButton(settings.getPanelToggleHotkeyText());
        setupKeyCapture(dialog, panelBtn, panelMod, panelKey);
        addHotkeyRow(panel, gbc, 4, "\uD328\uB110 \uC5F4\uAE30/\uB2EB\uAE30:", panelBtn);

        // Help text
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 0, 12, 0);
        gbc.anchor = GridBagConstraints.WEST;
        JLabel helpLabel = new JLabel("\uBC84\uD2BC\uC744 \uD074\uB9AD\uD55C \uD6C4 \uC6D0\uD558\uB294 \uD0A4 \uC870\uD569\uC744 \uB204\uB974\uC138\uC694");
        helpLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
        helpLabel.setForeground(new Color(150, 130, 100));
        panel.add(helpLabel, gbc);

        // Save / Cancel buttons
        gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        JButton saveBtn = new JButton("\uC800\uC7A5");
        saveBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        saveBtn.setBackground(new Color(200, 230, 180));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            settings.toggleModifier = toggleMod[0];
            settings.toggleKeyCode = toggleKey[0];
            settings.sendBackModifier = sendBackMod[0];
            settings.sendBackKeyCode = sendBackKey[0];
            settings.panelToggleModifier = panelMod[0];
            settings.panelToggleKeyCode = panelKey[0];
            settings.save();
            // Resume hotkeys with new settings
            if (hotkeyManager != null) hotkeyManager.resume(settings);
            if (onSave != null) onSave.onSettingsSaved(settings);
            dialog.dispose();
        });
        btnPanel.add(saveBtn);

        JButton cancelBtn = new JButton("\uCDE8\uC18C");
        cancelBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> {
            // Resume hotkeys with original settings
            if (hotkeyManager != null) hotkeyManager.resume(settings);
            dialog.dispose();
        });
        btnPanel.add(cancelBtn);

        panel.add(btnPanel, gbc);

        // Resume hotkeys if dialog is closed by X button
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (hotkeyManager != null) hotkeyManager.resume(settings);
            }
        });

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void addHotkeyRow(JPanel panel, GridBagConstraints gbc, int row, String label, JButton btn) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 6, 10);
        gbc.anchor = GridBagConstraints.EAST;

        gbc.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(btn, gbc);
    }

    private static JButton createHotkeyButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(160, 30));
        btn.setMinimumSize(new Dimension(160, 30));
        btn.setBackground(new Color(240, 235, 220));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 160, 130), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusable(true);
        return btn;
    }

    private static void setupKeyCapture(JDialog dialog, JButton btn, int[] modHolder, int[] keyHolder) {
        btn.addActionListener(e -> {
            btn.setText("\uB300\uAE30 \uC911...");
            btn.setBackground(new Color(255, 255, 200));

            KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent ke) {
                    // Consume all ALT/CTRL/SHIFT events to prevent Swing menu activation
                    if (ke.getID() == KeyEvent.KEY_RELEASED || ke.getID() == KeyEvent.KEY_TYPED) {
                        ke.consume();
                        return true;
                    }
                    if (ke.getID() != KeyEvent.KEY_PRESSED) return false;

                    int code = ke.getKeyCode();
                    if (code == KeyEvent.VK_ALT || code == KeyEvent.VK_CONTROL ||
                            code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_META) {
                        ke.consume();
                        return true;
                    }

                    int javaMod = ke.getModifiersEx();
                    int winMod = Settings.javaModToWinMod(javaMod);

                    if (winMod == 0) {
                        btn.setText("\uC218\uC815\uD0A4 \uD544\uC694!");
                        btn.setBackground(new Color(255, 200, 200));
                        Timer t = new Timer(1000, ev -> {
                            btn.setText(Settings.modifierToString(modHolder[0]) + Settings.keyCodeToString(keyHolder[0]));
                            btn.setBackground(new Color(240, 235, 220));
                        });
                        t.setRepeats(false);
                        t.start();
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                        return true;
                    }

                    int winVK = Settings.javaKeyToWinVK(code);
                    modHolder[0] = winMod;
                    keyHolder[0] = winVK;

                    btn.setText(Settings.modifierToString(winMod) + Settings.keyCodeToString(winVK));
                    btn.setBackground(new Color(200, 255, 200));
                    Timer t = new Timer(500, ev -> btn.setBackground(new Color(240, 235, 220)));
                    t.setRepeats(false);
                    t.start();

                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                    return true;
                }
            };
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        });
    }
}
