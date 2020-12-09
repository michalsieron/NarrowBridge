import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

public class AnimationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private World world;

    private float PARKING_LOT_WIDTH = 1 / 8F;
    private float ROAD_WIDTH = 1 / 6F;
    private float GATES_WIDTH = 1 / 8F;
    private float BRIDGE_WIDTH = 1 / 6F;

    public AnimationPanel(World world) {
        this.world = world;
        startRepainterSystem();
    }

    private void startRepainterSystem() {
        (new Thread(() -> {
            while (world.isRunning()) {
                long start = System.currentTimeMillis();
                repaint();
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
        Bus[] buses = world.getBuses();
        int x = 0, y = 0, i = 0;
        for (Bus b : buses) {
            x = 0;
            if (b.isGoingRight()) {
                switch (b.getLocation()) {
                    case LEFT_PARKING_LOT: {
                        x = (int) ((getWidth() * PARKING_LOT_WIDTH) / 2.0);
                        break;
                    }
                    case LEFT_ROAD: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH / 2.0 + ROAD_WIDTH * b.getTravelTime() / 500.0));
                        break;
                    }
                    case LEFT_GATES: {
                        x = (int) (getWidth()
                                * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH * b.getTravelTime() / 500.0));
                        break;
                    }
                    case WAITING_ON_LEFT_GATES: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH));
                        break;
                    }
                    case BRIDGE: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH
                                + BRIDGE_WIDTH * b.getTravelTime() / 3000.0));
                        break;
                    }
                    case RIGHT_GATES: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH
                                + GATES_WIDTH * b.getTravelTime() / 500.0));
                        break;
                    }
                    case RIGHT_ROAD: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + GATES_WIDTH + ROAD_WIDTH + BRIDGE_WIDTH
                                + GATES_WIDTH + (ROAD_WIDTH + PARKING_LOT_WIDTH / 2.0) * b.getTravelTime() / 500.0));
                        break;
                    }
                    case RIGHT_PARKING_LOT: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH
                                + GATES_WIDTH + ROAD_WIDTH + PARKING_LOT_WIDTH / 2.0));
                        break;
                    }
                    default:
                        break;
                }
            } else {
                switch (b.getLocation()) {
                    case RIGHT_PARKING_LOT: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH
                                + GATES_WIDTH + ROAD_WIDTH + PARKING_LOT_WIDTH / 2.0));
                        break;
                    }
                    case RIGHT_ROAD: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + GATES_WIDTH + ROAD_WIDTH + BRIDGE_WIDTH
                                + GATES_WIDTH + ROAD_WIDTH * (1 - b.getTravelTime() / 500.0)));
                        break;
                    }
                    case RIGHT_GATES: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH
                                + GATES_WIDTH * (1 - b.getTravelTime() / 500.0)));
                        break;
                    }
                    case WAITING_ON_RIGHT_GATES: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH + BRIDGE_WIDTH));
                        break;
                    }
                    case BRIDGE: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH
                                + BRIDGE_WIDTH * (1 - b.getTravelTime() / 3000.0)));
                        break;
                    }
                    case LEFT_GATES: {
                        x = (int) (getWidth()
                                * (PARKING_LOT_WIDTH + ROAD_WIDTH + GATES_WIDTH * (1 - b.getTravelTime() / 500.0)));
                        break;
                    }
                    case LEFT_ROAD: {
                        x = (int) (getWidth() * (PARKING_LOT_WIDTH + ROAD_WIDTH
                                - (PARKING_LOT_WIDTH / 2.0 + ROAD_WIDTH) * b.getTravelTime() / 500.0));
                        break;
                    }
                    case LEFT_PARKING_LOT: {
                        x = (int) ((getWidth() * PARKING_LOT_WIDTH / 2.0));
                        break;
                    }
                    default:
                        break;
                }
            }
            // dy = getHeight() - 2 * BUS_SIZE >= buses.length * BUS_SIZE ? BUS_SIZE
            //         : (getHeight() - 2 * BUS_SIZE) / buses.length;
            y = getHeight() / (buses.length + 1) * ++i;
            drawBus(g, x, y, b);
        }
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
        if (b.getLocation() != Bus.Location.WAITING_ON_LEFT_GATES && b.getLocation() != Bus.Location.WAITING_ON_RIGHT_GATES) {
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
