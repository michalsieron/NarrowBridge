import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

/*
 *     Program: NarrowBridgeSimulation
 *        Plik: AnimationPanel.java
 *       Autor: Michał Sieroń
 *        Data: 2020 December
 */

public class AnimationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private App app;

    private boolean running = true;

    private static final float PARKING_LOT_WIDTH = 1 / 8F;
    private static final float ROAD_WIDTH = 1 / 6F;
    private static final float GATES_WIDTH = 1 / 8F;
    private static final float BRIDGE_WIDTH = 1 / 6F;

    public AnimationPanel(App app) {
        this.app = app;
        startRepainter();
    }

    public boolean setRunning(boolean r) {
        return running = r;
    }

    private void startRepainter() {
        (new Thread(() -> {
            while (running) {
                repaint();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        drawBuses(g);
    }

    private void drawBackground(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g.setColor(Color.GREEN);
        g.fillRect(0, 0, (int) (PARKING_LOT_WIDTH * getWidth()), getHeight());
        g.drawRect(0, 0, (int) (PARKING_LOT_WIDTH * getWidth()), getHeight());
        g.fillRect((int) ((1 - PARKING_LOT_WIDTH) * getWidth()), 0, (int) (PARKING_LOT_WIDTH * getWidth()),
                getHeight());
        g.drawRect((int) ((1 - PARKING_LOT_WIDTH) * getWidth()), 0, (int) (PARKING_LOT_WIDTH * getWidth()),
                getHeight());

        g.setColor(Color.GRAY);
        g.fillRect((int) (PARKING_LOT_WIDTH * getWidth()), 0, (int) (ROAD_WIDTH * getWidth()), getHeight());
        g.drawRect((int) (PARKING_LOT_WIDTH * getWidth()), 0, (int) (ROAD_WIDTH * getWidth()), getHeight());
        g.fillRect((int) ((1 - PARKING_LOT_WIDTH - ROAD_WIDTH) * getWidth()), 0, (int) (ROAD_WIDTH * getWidth()),
                getHeight());
        g.drawRect((int) ((1 - PARKING_LOT_WIDTH - ROAD_WIDTH) * getWidth()), 0, (int) (ROAD_WIDTH * getWidth()),
                getHeight());

        g.setColor(Color.PINK);
        g.fillRect((int) ((PARKING_LOT_WIDTH + ROAD_WIDTH) * getWidth()), 0, (int) (GATES_WIDTH * getWidth()),
                getHeight());
        g.drawRect((int) ((PARKING_LOT_WIDTH + ROAD_WIDTH) * getWidth()), 0, (int) (GATES_WIDTH * getWidth()),
                getHeight());
        g.fillRect((int) ((1 - PARKING_LOT_WIDTH - ROAD_WIDTH - GATES_WIDTH) * getWidth()), 0,
                (int) (GATES_WIDTH * getWidth()), getHeight());
        g.drawRect((int) ((1 - PARKING_LOT_WIDTH - ROAD_WIDTH - GATES_WIDTH) * getWidth()), 0,
                (int) (GATES_WIDTH * getWidth()), getHeight());

        g.setColor(Color.ORANGE);
        g.fillRect((int) ((PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH) * getWidth()), 0,
                (int) (BRIDGE_WIDTH * getWidth()), getHeight());
        g.drawRect((int) ((PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH) * getWidth()), 0,
                (int) (BRIDGE_WIDTH * getWidth()), getHeight());

        Font font = new Font(Font.MONOSPACED, 0, 32);
        g.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        g.setColor(Color.DARK_GRAY);
        g2.rotate(Math.PI / 2.0);
        Rectangle2D stringBounds = font.getStringBounds("PARKING LOT", frc);
        g.drawString("PARKING LOT", (int) (getHeight() / 2.0 - stringBounds.getWidth() / 2.0),
                -(int) (getWidth() * (PARKING_LOT_WIDTH / 2.0) - stringBounds.getHeight() / 2.0));
        g.drawString("PARKING LOT", (int) (getHeight() / 2.0 - stringBounds.getWidth() / 2.0),
                -(int) (getWidth() * (1 - PARKING_LOT_WIDTH / 2.0) - stringBounds.getHeight() / 2.0));

        stringBounds = font.getStringBounds("ROAD", frc);
        g.drawString("ROAD", (int) (getHeight() / 2.0 - stringBounds.getWidth() / 2.0),
                -(int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH / 2.0) - stringBounds.getHeight() / 2.0));
        g.drawString("ROAD", (int) (getHeight() / 2.0 - stringBounds.getWidth() / 2.0),
                -(int) (getWidth() * (1 - PARKING_LOT_WIDTH - ROAD_WIDTH / 2.0) - stringBounds.getHeight() / 2.0));

        stringBounds = font.getStringBounds("GATES", frc);
        g.drawString("GATES", (int) (getHeight() / 2.0 - stringBounds.getWidth() / 2.0),
                -(int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH / 2.0)
                        - stringBounds.getHeight() / 2.0));
        g.drawString("GATES", (int) (getHeight() / 2.0 - stringBounds.getWidth() / 2.0),
                -(int) (getWidth() * (1 - PARKING_LOT_WIDTH - ROAD_WIDTH - GATES_WIDTH / 2.0)
                        - stringBounds.getHeight() / 2.0));

        stringBounds = font.getStringBounds("BRIDGE", frc);
        g.drawString("BRIDGE", (int) (getHeight() / 2.0 - stringBounds.getWidth() / 2.0),
                -(int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH / 2.0)
                        - stringBounds.getHeight() / 2.0));
        g2.rotate(-Math.PI / 2.0);
    }

    private void drawBuses(Graphics g) {
        int x = 0, y = 0, i = 0;
        Bus[] buses = app.getBuses();
        int t = app.getBusesSize() + 1;
        for (Bus b : buses) {
            x = computeBusX(b);
            y = getHeight() / (t) * ++i;
            drawBus(g, x, y, b);
        }
    }

    private int computeBusX(Bus b) {
        float rtn = 0;
        float pos_start;
        float pos_stop;
        long currTime = System.currentTimeMillis();
        switch(b.getState()) {
            case BOARDING: {
                rtn = PARKING_LOT_WIDTH / 2;
                break;
            }
            case GOING_TO_BRIDGE: {
                pos_start = PARKING_LOT_WIDTH / 2;
                pos_stop = PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH;
                rtn =  (pos_start + (pos_stop - pos_start) * (currTime - b.getTime()) / 1000);
                break;
            }
            case GETTIING_ON_BRIDGE: {
                rtn = PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH;
                break;
            }
            case ON_BRIDGE: {
                pos_start = PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH;
                pos_stop = PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH;
                rtn =  (pos_start + (pos_stop - pos_start) * (currTime - b.getTime()) / 3000);
                break;
            }
            case GETTING_OFF_BRIDGE: {
                rtn = PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH;
                break;
            }
            case GOING_TO_PARKING: {
                pos_start = PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH;
                pos_stop = 1.5f * PARKING_LOT_WIDTH + 2 * ROAD_WIDTH + 2 * GATES_WIDTH + BRIDGE_WIDTH;
                rtn =  (pos_start + (pos_stop - pos_start) * (currTime - b.getTime()) / 1000);
                break;
            }
            case UNLOADING: {
                rtn = 1.5f * PARKING_LOT_WIDTH + 2 * ROAD_WIDTH + 2 * GATES_WIDTH + BRIDGE_WIDTH;
                break;
            }
            default:
                rtn = 0;
        }
        if (!b.isGoingRight())
            rtn = 1 - rtn;
        return (int) (rtn * getWidth());
    }

    private void drawBus(Graphics g, int x, int y, Bus b) {
        Graphics2D g2 = (Graphics2D) g;
        int[] px;
        int[] py;
        Color color;
        if (b.isGoingRight()) {
            px = new int[] { -15 + x, 5 + x, 15 + x, 15 + x, -15 + x, -15 + x, -15 + x, x, -13 + x };
            py = new int[] { -10 + y, -10 + y, 0 + y, 10 + y, 10 + y, -10 + y };
            color = Color.CYAN;
        } else {
            px = new int[] { 15 + x, -5 + x, -15 + x, -15 + x, 15 + x, 15 + x, -10 + x, 5 + x, -10 + x };
            py = new int[] { -10 + y, -10 + y, 0 + y, 10 + y, 10 + y, -10 + y };
            color = Color.MAGENTA;
        }

        g2.setColor(Color.BLACK);
        g2.fillOval(px[6], 7 + y, 10, 10);
        g2.fillOval(px[7], 7 + y, 10, 10);
        BasicStroke gr = new BasicStroke(3);
        g2.setStroke(gr);
        if (b.getState() != Bus.State.GETTIING_ON_BRIDGE) {
            g2.setColor(color);
        } else {
            g2.setColor(Color.YELLOW);
        }

        g2.fillPolygon(px, py, 5);
        g2.setColor(color);
        g2.drawPolygon(px, py, 5);
        g2.setColor(Color.BLACK);
        g.setFont(new Font(Font.MONOSPACED, 0, 14));
        g2.drawString("" + b.getId(), px[8], 7 + y);
    }

}
