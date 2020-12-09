import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.util.Hashtable;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class App {

    private JFrame mainFrame = new JFrame();
    private JFrame animationFrame = new JFrame();
    private JPanel mainPanel = new JPanel();
    private JPanel controlsPanel = new JPanel();
    private LogsPanel logsPanel = new LogsPanel();

    private World world = new World((msg) -> logsPanel.insert(msg, 0));

    private AnimationPanel animationPanel;

    private JLabel trafficLimitLabel = new JLabel("Traffic limit:");

    private JComboBox<World.TrafficLimit> trafficLimitComboBox = new JComboBox<World.TrafficLimit>(
            World.TrafficLimit.values());

    private JLabel trafficVolumeLabel = new JLabel("Traffic volume:");
    private static final int MIN_BUS_SPAWN_DELAY = 0;
    private static final int MAX_BUS_SPAWN_DELAY = 4500;
    private static final int INITIAL_BUS_SPAWN_DELAY = 2000;
    private JSlider trafficVolumeSlider = new JSlider(MIN_BUS_SPAWN_DELAY, MAX_BUS_SPAWN_DELAY,
            INITIAL_BUS_SPAWN_DELAY);

    private JLabel trafficDirectionLabel = new JLabel("Traffic direction:");
    private JSlider trafficDirectionSlider = new JSlider(0, 100, 50);

    private JLabel busesOnBridgeLabel = new JLabel("On bridge:");
    private JTextField busesOnBridgeTextField = new JTextField();

    private JLabel busesOnGatesLabel = new JLabel("On gates:");
    private JTextField busesOnGatesTextField = new JTextField();

    private JLabel activeBusesLabel = new JLabel("Current state:");
    private JTextField activeBusesTextField = new JTextField();

    public static void main(String[] args) {
        new App();
    }

    private App() {

        constructControlsPanel();
        constructMenuBar();

        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(controlsPanel, BorderLayout.PAGE_START);
        mainPanel.add(logsPanel, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainFrame.add(mainPanel);

        mainFrame.setSize(480, 720);
        mainFrame.setTitle("2 buses 1 bridge");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                world.setRunning(false);
            }
        });
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLocation(mainFrame.getX() - mainFrame.getWidth() / 2 - 20, mainFrame.getY());
        world.setRunning(true);
        startRefresher();
        createAnimationWindow();
        mainFrame.setVisible(true);
    }

    private void constructMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener((e) -> JOptionPane.showMessageDialog(mainFrame,
                "Author: Michał Sieroń\nDate: 2020 December", "About", JOptionPane.INFORMATION_MESSAGE));

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener((e) -> world.setRunning(false));
        menu.add(about);
        menu.add(exit);
        menuBar.add(menu);
        mainFrame.setJMenuBar(menuBar);
    }

    private void startRefresher() {
        (new Thread(() -> {
            while (world.isRunning()) {
                long start = System.currentTimeMillis();
                busesOnBridgeTextField.setText(String.join(" ", world.getIdsOnBridge()));
                busesOnGatesTextField.setText(String.join(" ", world.getIdsOnGates()));
                String currentState = world.getCurrentState();
                activeBusesTextField.setText(currentState);
                long end = System.currentTimeMillis();
                long sleepTime = 20 - (end - start);
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            mainFrame.dispose();
            animationFrame.dispose();
        })).start();
    }

    private void constructControlsPanel() {
        controlsPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 0.75;
        c.gridy = 0;
        c.gridx = 0;
        controlsPanel.add(trafficLimitLabel, c);
        c.gridx = 1;
        controlsPanel.add(trafficLimitComboBox, c);
        trafficLimitComboBox.setSelectedItem(World.TrafficLimit.ThreeBuses);
        trafficLimitComboBox.addActionListener((e) -> {
            world.setTrafficLimit((World.TrafficLimit) trafficLimitComboBox.getSelectedItem());
        });
        c.gridy = 1;
        c.gridx = 0;
        controlsPanel.add(trafficVolumeLabel, c);
        c.gridx = 1;
        controlsPanel.add(trafficVolumeSlider, c);
        Hashtable<Integer, JLabel> volumeSliderLabelTable = new Hashtable<Integer, JLabel>();
        volumeSliderLabelTable.put(0, new JLabel("Small"));
        volumeSliderLabelTable.put(4500, new JLabel("Big"));
        trafficVolumeSlider.setLabelTable(volumeSliderLabelTable);
        trafficVolumeSlider.setPaintLabels(true);
        trafficVolumeSlider.addChangeListener((e) -> {
            world.setBusSpawnDelay(5000 - trafficVolumeSlider.getValue());
        });
        c.gridy = 2;
        c.gridx = 0;
        controlsPanel.add(trafficDirectionLabel, c);
        c.gridx = 1;
        controlsPanel.add(trafficDirectionSlider, c);
        trafficDirectionSlider.addChangeListener((e) -> {
            world.setDirectionRatio(trafficDirectionSlider.getValue());
        });
        Hashtable<Integer, JLabel> directionSliderLabelTable = new Hashtable<Integer, JLabel>();
        directionSliderLabelTable.put(0, new JLabel("West"));
        directionSliderLabelTable.put(100, new JLabel("East"));
        trafficDirectionSlider.setLabelTable(directionSliderLabelTable);
        trafficDirectionSlider.setPaintLabels(true);
        c.gridy = 3;
        c.gridx = 0;
        controlsPanel.add(busesOnBridgeLabel, c);
        c.gridx = 1;
        controlsPanel.add(busesOnBridgeTextField, c);
        busesOnBridgeTextField.setEditable(false);
        c.gridy = 4;
        c.gridx = 0;
        controlsPanel.add(busesOnGatesLabel, c);
        c.gridx = 1;
        controlsPanel.add(busesOnGatesTextField, c);
        busesOnGatesTextField.setEditable(false);
        c.gridy = 5;
        c.gridx = 0;
        controlsPanel.add(activeBusesLabel, c);
        c.gridx = 1;
        controlsPanel.add(activeBusesTextField, c);
        activeBusesTextField.setEditable(false);

        controlsPanel
                .setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Controls"));
        logsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Logs"));
    }

    private void createAnimationWindow() {
        animationPanel = new AnimationPanel(world);
        animationFrame.add(animationPanel);
        animationFrame.setSize(480, 720);
        animationFrame.setMinimumSize(new Dimension(480, 720));
        animationFrame.setTitle("2 buses 1 bridge animation");
        animationFrame.setLocationRelativeTo(null);
        animationFrame.setLocation(animationFrame.getX() + animationFrame.getWidth() / 2 + 20, animationFrame.getY());
        animationFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        animationFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                world.setRunning(false);
            }
        });
        animationFrame.setVisible(true);
    }
}
