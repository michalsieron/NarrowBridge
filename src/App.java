import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ThreadLocalRandom;
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

/*
 *     Program: NarrowBridgeSimulation
 *        Plik: App.java
 *       Autor: Michał Sieroń
 *        Data: 2020 December
 */

public class App {

    private static final long MAX_BUSES = 100;

    private JFrame mainFrame = new JFrame();
    private JFrame animationFrame = new JFrame();
    private JPanel mainPanel = new JPanel();
    private JPanel controlsPanel = new JPanel();
    private LogsPanel logsPanel = new LogsPanel();

    private AnimationPanel animationPanel;

    private JLabel trafficLimitLabel = new JLabel("Traffic limit:");

    private JComboBox<TrafficLimit> trafficLimitComboBox = new JComboBox<TrafficLimit>(TrafficLimit.values());

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

    public enum TrafficLimit {
        OneBus, ThreeBuses, NoLimit, OneWayThreeBuses;

        private TrafficLimit() {
        }
    }

    private boolean running = false;
    private TrafficLimit trafficLimit = TrafficLimit.ThreeBuses;
    private int busSpawnDelay = INITIAL_BUS_SPAWN_DELAY;
    private int direction = 50;
    private ArrayList<Bus> buses = new ArrayList<Bus>();
    private ArrayList<String> busesOnBridge = new ArrayList<String>();
    private boolean bridgeGoingRight = false;
    private ArrayList<String> busesWaitingRight = new ArrayList<String>();
    private ArrayList<String> busesWaitingLeft = new ArrayList<String>();

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
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLocation(mainFrame.getX() - mainFrame.getWidth() / 2 - 20, mainFrame.getY());
        createAnimationWindow();
        setRunning(true);
        startRefresher();
        mainFrame.setVisible(true);
        runMainLoop();
    }

    public void log(String msg) {
        logsPanel.insert(msg + "\n", 0);
    }

    private void runMainLoop() {
        while (isRunning()) {
            if (buses.size() < MAX_BUSES) {
                boolean goingRight = ThreadLocalRandom.current().nextInt(0, 101) < direction;
                (new Thread(new Bus(this, goingRight))).start();
            }

            try {
                Thread.sleep(busSpawnDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void getOnTheBridge(Bus b) {
        String busId = Long.toString(b.getId());
        b.setState(Bus.State.GETTIING_ON_BRIDGE);
        while (!canEnterBridge(b)) {
            try {
                if (b.isGoingRight()) {
                    if (!busesWaitingLeft.contains(busId))
                        busesWaitingLeft.add(busId);
                }
                else {
                    if (!busesWaitingRight.contains(busId))
                        busesWaitingRight.add(busId);
                }

                log(b + " is waiting on gates");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (b.isGoingRight())
            busesWaitingLeft.remove(busId);
        else
            busesWaitingRight.remove(busId);

        busesOnBridge.add(busId);
        bridgeGoingRight = b.isGoingRight();

        b.setTime(System.currentTimeMillis());
        b.setState(Bus.State.ON_BRIDGE);
    }

    private synchronized boolean canEnterBridge(Bus b) {
        boolean can = false;
        switch (trafficLimit) {
            case OneBus: {
                can = busesOnBridge.size() == 0;
                break;
            }
            case ThreeBuses: {
                can = busesOnBridge.size() < 3;
                break;
            }
            case NoLimit: {
                can = true;
                break;
            }
            case OneWayThreeBuses: {
                if (b.isGoingRight()) {
                    if (busesWaitingLeft.size() >= busesWaitingRight.size() - 3)
                        can = (busesOnBridge.size() == 0) || (busesOnBridge.size() < 3 && bridgeGoingRight);
                }
                else {
                    if (busesWaitingLeft.size() - 3 < busesWaitingRight.size())
                        can = (busesOnBridge.size() == 0) || (busesOnBridge.size() < 3 && !bridgeGoingRight);
                }
                break;
            }
            default:
                break;
        }
        return can;
    }

    public synchronized void getOffTheBridge(Bus b) {
        b.setTime(System.currentTimeMillis());
        b.setState(Bus.State.GOING_TO_PARKING);

        log(b + " got off the bridge");

        synchronized (busesOnBridge) {
            busesOnBridge.remove(Long.toString(b.getId()));
        }
        notifyAll();
    }

    public synchronized void addBus(Bus b) {
        buses.add(b);
    }

    public synchronized void removeBus(Bus b) {
        buses.remove(b);
    }

    private void constructMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener((e) -> JOptionPane.showMessageDialog(mainFrame,
                "Author: Michał Sieroń\nDate: 2020 December", "About", JOptionPane.INFORMATION_MESSAGE));

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener((e) -> setRunning(false));
        menu.add(about);
        menu.add(exit);
        menuBar.add(menu);
        mainFrame.setJMenuBar(menuBar);
    }

    private synchronized void updateTextFields() {
        ArrayList<String> allBusesWaiting = new ArrayList<String>();
        allBusesWaiting.addAll(busesWaitingLeft);
        allBusesWaiting.addAll(busesWaitingRight);
        busesOnGatesTextField.setText(String.join(" ", allBusesWaiting));
        busesOnBridgeTextField.setText(String.join(" ", busesOnBridge));
    }

    private void startRefresher() {
        (new Thread(() -> {
            while (isRunning()) {
                updateTextFields();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
        trafficLimitComboBox.setSelectedItem(TrafficLimit.ThreeBuses);
        trafficLimitComboBox.addActionListener((e) -> {
            trafficLimit = (TrafficLimit) trafficLimitComboBox.getSelectedItem();
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
            busSpawnDelay = 5000 - trafficVolumeSlider.getValue();
        });
        c.gridy = 2;
        c.gridx = 0;
        controlsPanel.add(trafficDirectionLabel, c);
        c.gridx = 1;
        controlsPanel.add(trafficDirectionSlider, c);
        trafficDirectionSlider.addChangeListener((e) -> {
            direction = trafficDirectionSlider.getValue();
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
        animationPanel = new AnimationPanel(this);
        animationFrame.add(animationPanel);
        animationFrame.setSize(480, 720);
        animationFrame.setMinimumSize(new Dimension(480, 720));
        animationFrame.setTitle("2 buses 1 bridge animation");
        animationFrame.setLocationRelativeTo(null);
        animationFrame.setLocation(animationFrame.getX() + animationFrame.getWidth() / 2 + 20, animationFrame.getY());
        animationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        animationFrame.setVisible(true);
    }

    private boolean isRunning() {
        return running;
    }

    private boolean setRunning(boolean r) {
        animationPanel.setRunning(r);
        return running = r;
    }

    public synchronized Bus[] getBuses() {
        Bus[] array = new Bus[buses.size()];
        buses.toArray(array);
        return array;
    }

    public synchronized int getBusesSize() {
        return buses.size();
    }
}
