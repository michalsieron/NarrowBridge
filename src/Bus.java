
public class Bus {

    static private long currentId = 0;

    private long lastTimeCheck;
    private Object lastTimeCheckLock = new Object();
    private long travelTime = 0;
    private Object travelTimeLock = new Object();
    private boolean goingRight;
    private long id = currentId++;

    public enum Location {
        LEFT_PARKING_LOT, LEFT_ROAD, LEFT_GATES, WAITING_ON_LEFT_GATES, BRIDGE, WAITING_ON_RIGHT_GATES, RIGHT_GATES,
        RIGHT_ROAD, RIGHT_PARKING_LOT;

        private Location() {
        }
    }

    private Location location;

    public Bus(long time, boolean goingRight) {
        lastTimeCheck = time;
        this.goingRight = goingRight;
    }

    public long getId() {
        return id;
    }

    public Location getLocation() {
        synchronized (location) {
            return location;
        }
    }

    public void setLocation(Location location) {
        synchronized (location) {
            this.location = location;
            synchronized (travelTimeLock) {
                travelTime = 0;
            }
        }
    }

    public boolean isGoingRight() {
        return goingRight;
    }

    public long checkTime(long time) {
        synchronized (travelTimeLock) {
            synchronized (lastTimeCheckLock) {
                travelTime += time - lastTimeCheck;
            }
        }
        lastTimeCheck = time;
        return travelTime;
    }

    public long getTravelTime() {
        synchronized (travelTimeLock) {
            return travelTime;
        }
    }
}
