import javax.swing.*;
import java.awt.*;

public class Task1 {
    static final int WIDTH = 600, HEIGHT = 360, SIZE = 24, DELAY = 20;
    static final double V = 120; 
    static final double N = 2;   

    static class Rabbit {
        double x, y, dx = 1, dy = 0;
        final boolean albino;

        Rabbit(int x, int y, boolean albino) {
            this.x = x;
            this.y = y;
            this.albino = albino;
        }

        void move(double step) {
            x += dx * step;
            y += dy * step;
            if (x < 0 || x > WIDTH - SIZE) {
                dx = -dx;
                x = Math.max(0, Math.min(x, WIDTH - SIZE));
            }
            if (y < 0 || y > HEIGHT - SIZE) {
                dy = -dy;
                y = Math.max(0, Math.min(y, HEIGHT - SIZE));
            }
        }
    }

    static void startThread(String name, JPanel field, Runnable action) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(DELAY);
                    action.run();
                    field.repaint();
                }
            } catch (InterruptedException ignored) {
            }
        }, name);
        t.setDaemon(true);
        t.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Rabbit[] rabbits = {
                new Rabbit(110, 80, false), new Rabbit(250, 150, false),
                new Rabbit(390, 240, false), new Rabbit(0, 45, true),
                new Rabbit(0, 165, true), new Rabbit(0, 285, true)
            };

            JPanel field = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    for (Rabbit r : rabbits) {
                        int x = (int) r.x, y = (int) r.y;
                        g.setColor(r.albino ? Color.WHITE : new Color(133, 91, 64));
                        g.fillOval(x + 5, y, 6, 13);   // вуха
                        g.fillOval(x + 13, y, 6, 13);
                        g.fillOval(x, y + 8, SIZE, 16); // тіло
                        g.setColor(r.albino ? Color.RED : Color.BLACK);
                        g.fillOval(x + 6, y + 14, 3, 3); // очі
                        g.fillOval(x + 15, y + 14, 3, 3);
                    }
                }
            };
            field.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            field.setBackground(new Color(209, 231, 190));

            JFrame window = new JFrame("Лабораторна 7 · Рух кроликів");
            window.add(field);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            final double step = V * DELAY / 1000.0;

            double[] time = {N}; 
            startThread("Звичайні кролики", field, () -> {
                time[0] += DELAY / 1000.0;
                boolean turn = time[0] >= N;
                if (turn) time[0] = 0;
                for (Rabbit r : rabbits) {
                    if (r.albino) continue;
                    if (turn) {
                        double angle = Math.random() * 2 * Math.PI;
                        r.dx = Math.cos(angle);
                        r.dy = Math.sin(angle);
                    }
                    r.move(step);
                }
            });

            startThread("Кролики-альбіноси", field, () -> {
                for (Rabbit r : rabbits) {
                    if (r.albino) r.move(step);
                }
            });
        });
    }
}