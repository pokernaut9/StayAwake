package io.n3rdware;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.*;

public class StayAwake extends JFrame {
    private final JTextField intervalField;
    private final JButton toggleButton;
    private final JLabel statusLabel;

    private ScheduledExecutorService scheduler;
    private volatile boolean running = false;
    private Robot robot;
    private boolean moveRight = true;

    public StayAwake() {
        super("Stay Awake");

        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(
                    StayAwake.class.getResource("/icon.png")
            );
            setIconImage(icon);
        } catch (Exception ignored) {
        }

        try {
            robot = new Robot();
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to initialize java.awt.Robot: " + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }

        Color background = Color.decode("#212121");
        Color textColor = Color.decode("#dedede");

        JLabel intervalLabel = new JLabel("Interval (ms):");
        intervalLabel.setForeground(textColor);

        intervalField = new JTextField("5000", 10);
        intervalField.setBackground(background);
        intervalField.setForeground(textColor);
        intervalField.setCaretColor(textColor);

        toggleButton = new JButton("Start");
        toggleButton.setBackground(background);
        toggleButton.setForeground(textColor);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setOpaque(true);
        toggleButton.setContentAreaFilled(true);

        statusLabel = new JLabel("Stopped");
        statusLabel.setForeground(textColor);

        toggleButton.addActionListener(e -> {
            if (running) {
                stopJiggler();
            } else {
                startJiggler();
            }
        });

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBackground(background);
        inputPanel.add(intervalLabel);
        inputPanel.add(intervalField);
        inputPanel.add(toggleButton);

        JPanel root = new JPanel();
        root.setBackground(background);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        root.add(inputPanel);
        root.add(Box.createVerticalStrut(8));
        root.add(statusLabel);

        setContentPane(root);
        getContentPane().setBackground(background);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopJiggler();
                dispose();
                System.exit(0);
            }
        });

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void startJiggler() {
        long intervalMs;
        try {
            intervalMs = Long.parseLong(intervalField.getText().trim());
            if (intervalMs <= 0) {
                throw new NumberFormatException("Interval must be greater than 0.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid positive number of milliseconds.",
                    "Invalid Interval",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::jiggleMouse, 0, intervalMs, TimeUnit.MILLISECONDS);

        running = true;
        intervalField.setEnabled(false);
        toggleButton.setText("Stop");
        statusLabel.setText("Running every " + intervalMs + " ms");
    }

    private void stopJiggler() {
        running = false;

        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        intervalField.setEnabled(true);
        toggleButton.setText("Start");
        statusLabel.setText("Stopped");
    }

    private void jiggleMouse() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return;
        }

        Point p = pointerInfo.getLocation();
        int dx = moveRight ? 3 : -3;
        moveRight = !moveRight;

        robot.mouseMove(p.x + dx, p.y);
    }
}