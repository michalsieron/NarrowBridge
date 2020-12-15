import java.util.concurrent.ThreadLocalRandom;

/*
 *     Program: NarrowBridgeSimulation
 *        Plik: Bus.java
 *       Autor: Michał Sieroń
 *        Data: 2020 December
 */

public class Bus implements Runnable {

    static private long currentId = 0;

    private final long id = currentId++;

    public enum State {
        BOARDING, GOING_TO_BRIDGE, GETTIING_ON_BRIDGE, ON_BRIDGE, GETTING_OFF_BRIDGE, GOING_TO_PARKING, UNLOADING;

        private State() {
        }
    }

    private boolean goingRight = true;
    private State state = State.BOARDING;
    private App app;

    private long time;

    public Bus(App app, boolean goingRight) {
        this.goingRight = goingRight;
        this.app = app;
    }

    public static void sleep(int millis) {
        try {
           Thread.sleep((long) millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
  
     }
  
     public static void sleep(int min_millis, int max_milis) {
        sleep(ThreadLocalRandom.current().nextInt(min_millis, max_milis));
     }

    public long getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public State getState() {
            return state;
    }
    
    public void setState(State s) {
        state = s;
    }
    
    public void setTime(long t) {
        time = t;
    }

    public boolean isGoingRight() {
        return goingRight;
    }

    private void boarding() {
        time = System.currentTimeMillis();
        state = State.BOARDING;

        app.log(this + " is boarding");

        sleep(1000, 10000);
    }

    private void goToTheBridge() {
        time = System.currentTimeMillis();
        state = State.GOING_TO_BRIDGE;

        app.log(this + " is going to the bridge");

        sleep(1000);
    }

    private void rideTheBridge() {
        time = System.currentTimeMillis();
        state = State.ON_BRIDGE;

        app.log(this + " is on the bridge");

        sleep(3000);
    }

    private void goToTheParking() {
        time = System.currentTimeMillis();
        state = State.GOING_TO_PARKING;

        app.log(this + " is going to parking lot");

        sleep(1000);
    }

    private void unloading() {
        time = System.currentTimeMillis();
        state = State.UNLOADING;

        app.log(this + " is unloading");

        sleep(1000);
    }

    @Override
    public void run() {
        app.addBus(this);
        boarding();
        goToTheBridge();
        app.getOnTheBridge(this);
        rideTheBridge();
        app.getOffTheBridge(this);
        goToTheParking();
        unloading();
        app.removeBus(this);
    }

    @Override
    public String toString() {
        return "Bus[id=" + id + ",goingRight=" + goingRight + "]";
    }
}
