import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class World {

    private static final long BUS_COUNT_LIMIT = 1000;

    private boolean running = false;
    private Object isRunningLock = new Object();
    private int busSpawnDelay = 2000;
    private Object busSpawnDelayLock = new Object();
    private int directionRatio = 50;
    private Object directionRatioLock = new Object();
    private int busesOnBridgeGoingLeft = 0;
    private Object busesOnBridgeGoingLeftLock = new Object();
    private int busesOnBridgeGoingRight = 0;
    private Object busesOnBridgeGoingRightLock = new Object();
    private int busesWaitingOnLeftGates = 0;
    private Object busesWaitingOnLeftGatesLock = new Object();
    private int busesWaitingOnRightGates = 0;
    private Object busesWaitingOnRightGatesLock = new Object();

    private Consumer<String> logger;

    public enum TrafficLimit {
        NoPassage, OneBus, ThreeBuses, NoLimit, OneWayThreeBuses;

        private TrafficLimit() {
        }
    }

    private TrafficLimit trafficLimit = TrafficLimit.ThreeBuses;

    private ArrayList<Bus> buses = new ArrayList<Bus>();

    public World(Consumer<String> logger) {
        this.logger = logger;
    }

    private void log(String str) {
        logger.accept(str + "\n");
    }

    public int getBusSpawnDelay() {
        synchronized (busSpawnDelayLock) {
            return busSpawnDelay;
        }
    }

    public void setBusSpawnDelay(int busSpawnDelay) {
        synchronized (busSpawnDelayLock) {
            this.busSpawnDelay = busSpawnDelay;
        }
    }

    public int getDirectionRatio() {
        synchronized (directionRatioLock) {
            return directionRatio;
        }
    }

    public void setDirectionRatio(int directionRatio) {
        synchronized (directionRatioLock) {
            this.directionRatio = directionRatio;
        }
    }

    public boolean isRunning() {
        synchronized (isRunningLock) {
            return running;
        }
    }

    public void setRunning(boolean running) {
        synchronized (isRunningLock) {
            this.running = running;
            if (running)
                run();
        }
    }

    public TrafficLimit getTrafficLimit() {
        synchronized (trafficLimit) {
            return trafficLimit;
        }
    }

    public void setTrafficLimit(TrafficLimit trafficLimit) {
        synchronized (trafficLimit) {
            this.trafficLimit = trafficLimit;
        }
    }

    private int getBusesOnBridgeGoingLeft() {
        synchronized (busesOnBridgeGoingLeftLock) {
            return busesOnBridgeGoingLeft;
        }
    }

    private int getBusesOnBridgeGoingRight() {
        synchronized (busesOnBridgeGoingRightLock) {
            return busesOnBridgeGoingRight;
        }
    }

    private int getBusesWaitingOnLeftGates() {
        synchronized (busesWaitingOnLeftGatesLock) {
            return busesWaitingOnLeftGates;
        }
    }

    private int getBusesWaitingOnRightGates() {
        synchronized (busesWaitingOnRightGatesLock) {
            return busesWaitingOnRightGates;
        }
    }

    public String[] getIdsOnBridge() {
        ArrayList<String> ids = new ArrayList<String>();
        for (Bus b : getBuses())
            if (b.getLocation() == Bus.Location.BRIDGE)
                ids.add(Long.toString(b.getId()));

        String[] array = new String[0];
        return ids.toArray(array);
    }

    public String[] getIdsOnGates() {
        ArrayList<String> ids = new ArrayList<String>();
        for (Bus b : getBuses())
            if (b.getLocation() == Bus.Location.WAITING_ON_LEFT_GATES
                    || b.getLocation() == Bus.Location.WAITING_ON_RIGHT_GATES)
                ids.add(Long.toString(b.getId()));

        String[] array = new String[0];
        return ids.toArray(array);
    }

    private void run() {
        startSpawnSystem();
        startMoveSystem();
    }

    private void startSpawnSystem() {
        // spawns buses on parking lots
        (new Thread(() -> {
            while (isRunning()) {
                if (countBuses() < BUS_COUNT_LIMIT) {
                    // leftParkingLot.spawnBus();
                    boolean goingRight = ThreadLocalRandom.current().nextFloat() < (directionRatio / 100.0);
                    Bus b = new Bus(System.currentTimeMillis(), goingRight);
                    if (goingRight) {
                        b.setLocation(Bus.Location.LEFT_PARKING_LOT);
                        log(System.currentTimeMillis() + ": Bus " + b.getId() + " was spawned on left parking lot");
                    } else {
                        b.setLocation(Bus.Location.RIGHT_PARKING_LOT);
                        log(System.currentTimeMillis() + ": Bus " + b.getId() + " was spawned on right parking lot");
                    }
                    addBus(b);
                }
                try {
                    Thread.sleep(busSpawnDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }

    private void startMoveSystem() {
        // takes buses from left parking lot to left road
        (new Thread(() -> {
            while (isRunning()) {
                long start = System.currentTimeMillis();
                for (Bus b : getBuses()) {
                    if (b.isGoingRight()) {
                        switch (b.getLocation()) {
                            case LEFT_PARKING_LOT: {
                                // <2500, 7500) ms on left parking lot
                                if (b.checkTime(start) > (ThreadLocalRandom.current().nextFloat() * 5000 + 2500)) {
                                    b.setLocation(Bus.Location.LEFT_ROAD);
                                    log(start + ": Bus " + b.getId() + " entered left road");
                                }
                                break;
                            }
                            case LEFT_ROAD: {
                                // <500, 600) ms on left road
                                if (b.checkTime(start) > 500) {
                                    b.setLocation(Bus.Location.LEFT_GATES);
                                    log(start + ": Bus " + b.getId() + " entered left gates");
                                }
                                break;
                            }
                            case LEFT_GATES: {
                                // <500, 1000) ms on left gates
                                if (b.checkTime(start) > 500) {
                                    if (canEnterBridge(b)) {
                                        b.setLocation(Bus.Location.BRIDGE);
                                        synchronized (busesOnBridgeGoingRightLock) {
                                            busesOnBridgeGoingRight++;
                                        }
                                        log(start + ": Bus " + b.getId() + " entered bridge");
                                    } else {
                                        b.setLocation(Bus.Location.WAITING_ON_LEFT_GATES);
                                        synchronized (busesWaitingOnLeftGatesLock) {
                                            busesWaitingOnLeftGates++;
                                        }
                                        log(start + ": Bus " + b.getId() + " waiting on left gates");
                                    }
                                }
                                break;
                            }
                            case WAITING_ON_LEFT_GATES: {
                                if (canEnterBridge(b)) {
                                    b.checkTime(start);
                                    b.setLocation(Bus.Location.BRIDGE);
                                    synchronized (busesOnBridgeGoingRightLock) {
                                        busesOnBridgeGoingRight++;
                                    }
                                    synchronized (busesWaitingOnLeftGatesLock) {
                                        busesWaitingOnLeftGates--;
                                    }
                                    log(start + ": Bus " + b.getId() + " entered bridge");
                                }
                                break;
                            }
                            case BRIDGE: {
                                // <2500, 3000) ms on bridge
                                if (b.checkTime(start) > 2500) {
                                    b.setLocation(Bus.Location.RIGHT_GATES);
                                    synchronized (busesOnBridgeGoingRightLock) {
                                        busesOnBridgeGoingRight--;
                                    }
                                    log(start + ": Bus " + b.getId() + " entered right gates");
                                }
                                break;
                            }
                            case RIGHT_GATES: {
                                // <500, 750) ms on right gates
                                if (b.checkTime(start) > 500) {
                                    b.setLocation(Bus.Location.RIGHT_ROAD);
                                    log(start + ": Bus " + b.getId() + " entered right road");
                                }
                                break;
                            }
                            case RIGHT_ROAD: {
                                // <500, 600) ms on right road
                                if (b.checkTime(start) > 500) {
                                    b.setLocation(Bus.Location.RIGHT_PARKING_LOT);
                                    log(start + ": Bus " + b.getId() + " entered right parking lot");
                                }
                                break;
                            }
                            case RIGHT_PARKING_LOT: {
                                // <2500, 3000) ms on right parking lot
                                if (b.checkTime(start) > (ThreadLocalRandom.current().nextFloat() * 500 + 1500)) {
                                    synchronized (buses) {
                                        buses.remove(b);
                                    }
                                    log(start + ": Bus " + b.getId() + " was destroyed");
                                }
                                break;
                            }
                            default:
                                break;
                        }
                    } else {
                        switch (b.getLocation()) {
                            case RIGHT_PARKING_LOT: {
                                // <2500, 7500) ms on right parking lot
                                if (b.checkTime(start) > (ThreadLocalRandom.current().nextFloat() * 5000 + 2500)) {
                                    b.setLocation(Bus.Location.RIGHT_ROAD);
                                    log(start + ": Bus " + b.getId() + " entered right road");
                                }
                                break;
                            }
                            case RIGHT_ROAD: {
                                // <500, 600) ms on right road
                                if (b.checkTime(start) > 500) {
                                    b.setLocation(Bus.Location.RIGHT_GATES);
                                    log(start + ": Bus " + b.getId() + " entered right gates");
                                }
                                break;
                            }
                            case RIGHT_GATES: {
                                // <500, 1000) ms on right gates
                                if (b.checkTime(start) > 500) {
                                    if (canEnterBridge(b)) {
                                        b.setLocation(Bus.Location.BRIDGE);
                                        synchronized (busesOnBridgeGoingLeftLock) {
                                            busesOnBridgeGoingLeft++;
                                        }
                                        log(start + ": Bus " + b.getId() + " entered bridge");
                                    } else {
                                        b.setLocation(Bus.Location.WAITING_ON_RIGHT_GATES);
                                        synchronized (busesWaitingOnRightGatesLock) {
                                            busesWaitingOnRightGates++;
                                        }
                                        log(start + ": Bus " + b.getId() + " waiting on right gates");
                                    }
                                }
                                break;
                            }
                            case WAITING_ON_RIGHT_GATES: {
                                if (canEnterBridge(b)) {
                                    b.checkTime(start);
                                    b.setLocation(Bus.Location.BRIDGE);
                                    synchronized (busesOnBridgeGoingLeftLock) {
                                        busesOnBridgeGoingLeft++;
                                    }
                                    synchronized (busesWaitingOnRightGatesLock) {
                                        busesWaitingOnRightGates--;
                                    }
                                    log(start + ": Bus " + b.getId() + " entered bridge");
                                }

                                break;
                            }
                            case BRIDGE: {
                                // <2500, 3000) ms on bridge
                                if (b.checkTime(start) > 3000) {
                                    b.setLocation(Bus.Location.LEFT_GATES);
                                    synchronized (busesOnBridgeGoingLeftLock) {
                                        busesOnBridgeGoingLeft--;
                                    }
                                    log(start + ": Bus " + b.getId() + " entered left gates");
                                }
                                break;
                            }
                            case LEFT_GATES: {
                                // <500, 750) ms on left gates
                                if (b.checkTime(start) > 500) {
                                    b.setLocation(Bus.Location.LEFT_ROAD);
                                    log(start + ": Bus " + b.getId() + " entered left road");
                                }
                                break;
                            }
                            case LEFT_ROAD: {
                                // <500, 600) ms on left road
                                if (b.checkTime(start) > 500) {
                                    b.setLocation(Bus.Location.LEFT_PARKING_LOT);
                                    log(start + ": Bus " + b.getId() + " entered left parking lot");
                                }
                                break;
                            }
                            case LEFT_PARKING_LOT: {
                                // <2500, 3000) ms on left parking lot
                                if (b.checkTime(start) > (ThreadLocalRandom.current().nextFloat() * 500 + 1500)) {
                                    synchronized (buses) {
                                        buses.remove(b);
                                    }
                                    log(start + ": Bus " + b.getId() + " was destroyed");
                                }
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
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
        })).start();
    }

    private boolean canEnterBridge(Bus b) {
        boolean can = false;
        switch (trafficLimit) {
            case NoPassage: {
                can = false;
                break;
            }
            case OneBus: {
                if (getBusesOnBridgeGoingLeft() + getBusesOnBridgeGoingRight() == 0)
                    can = true;
                break;
            }
            case ThreeBuses: {
                if (getBusesOnBridgeGoingLeft() + getBusesOnBridgeGoingRight() < 3)
                    can = true;
                break;
            }
            case NoLimit: {
                can = true;
                break;
            }
            case OneWayThreeBuses: {
                int leftGates = getBusesWaitingOnLeftGates();
                int rightGates = getBusesWaitingOnRightGates();
                int bridgeRight = getBusesOnBridgeGoingLeft();
                int bridgeLeft = getBusesOnBridgeGoingRight();
                if (((leftGates >= rightGates && b.isGoingRight() && bridgeRight == 0)
                        || (leftGates < rightGates && !b.isGoingRight() && bridgeLeft == 0))
                                && (bridgeLeft + bridgeRight) < 3)
                    can = true;
                else
                    can = false;
                break;
            }
        }
        return can;
    }

    public String getCurrentState() {
        Bus[] array;
        array = getBuses();
        int lpl = 0, lr = 0, lg = 0, bd = 0, rg = 0, rr = 0, rpl = 0;
        for (Bus b : array) {
            switch (b.getLocation()) {
                case LEFT_PARKING_LOT:
                    lpl++;
                    break;
                case LEFT_ROAD:
                    lr++;
                    break;
                case LEFT_GATES:
                case WAITING_ON_LEFT_GATES:
                    lg++;
                    break;
                case BRIDGE:
                    bd++;
                    break;
                case RIGHT_GATES:
                case WAITING_ON_RIGHT_GATES:
                    rg++;
                    break;
                case RIGHT_ROAD:
                    rr++;
                    break;
                case RIGHT_PARKING_LOT:
                    rpl++;
                    break;
                default:
                    break;
            }
        }
        return "LPL: " + lpl + " LR: " + lr + " LG: " + lg + " B: " + bd + " RG: " + rg + " RR: " + rr + " RPL: " + rpl;
    }

    public int countBuses() {
        int counted = 0;
        synchronized (buses) {
            counted = buses.size();
        }
        return counted;
    }

    private void addBus(Bus b) {
        synchronized (buses) {
            buses.add(b);
        }
    }

    public Bus[] getBuses() {
        Bus[] array;
        synchronized (buses) {
            array = new Bus[buses.size()];
            buses.toArray(array);
        }
        return array;
    }
}
