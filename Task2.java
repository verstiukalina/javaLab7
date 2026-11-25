import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class Vehicle {
    private String name;
    private int speed;

    public double x, y;
    public double angle = Math.random() * 2 * Math.PI;
    public int dir = 1;                                
    public long nextChange = 0;                      

    public Vehicle(String name, int speed) {
        this.name = name;
        this.speed = speed;
        this.x = Math.random() * 700;
        this.y = Math.random() * 450;
    }

    public void Show() {
        System.out.println("Назва: " + name + ", швидкість: " + speed + " км/год");
    }
}

class Car extends Vehicle {
    private int seats;

    public Car(String name, int speed, int seats) {
        super(name, speed);
        this.seats = seats;
    }

    @Override
    public void Show() {
        System.out.println("Автомобіль:");
        super.Show();
        System.out.println("Кількість місць: " + seats);
    }
}

class Train extends Vehicle {
    private int wagons;

    public Train(String name, int speed, int wagons) {
        super(name, speed);
        this.wagons = wagons;
    }

    @Override
    public void Show() {
        System.out.println("Поїзд:");
        super.Show();
        System.out.println("Кількість вагонів: " + wagons);
    }
}

class Express extends Train {
    private String route;

    public Express(String name, int speed, int wagons, String route) {
        super(name, speed, wagons);
        this.route = route;
    }

    @Override
    public void Show() {
        System.out.println("Експрес:");
        super.Show();
        System.out.println("Маршрут: " + route);
    }
}

abstract class BaseAI extends Thread {
    protected static final int W = 800, H = 500, SIZE = 20;
    protected static final double V = 100;   
    protected static final double DT = 0.02; 

    protected final List<Vehicle> vehicles;  
    protected final JPanel panel;
    private boolean paused = false;          

    BaseAI(List<Vehicle> vehicles, JPanel panel) {
        this.vehicles = vehicles;
        this.panel = panel;
        setDaemon(true);
    }

    public synchronized void pauseAI() {
        paused = true;
    }

    public synchronized void resumeAI() {
        paused = false;
        notifyAll();
    }

    protected abstract boolean mine(Vehicle v);
    protected abstract void move(Vehicle v); 

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    while (paused) {
                        wait();
                    }
                }
                synchronized (vehicles) {
                    for (Vehicle v : vehicles) {
                        if (mine(v)) {
                            move(v);
                        }
                    }
                }
                panel.repaint();
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
        }
    }
}

class CarAI extends BaseAI {
    private static final int N = 2; 

    CarAI(List<Vehicle> vehicles, JPanel panel) {
        super(vehicles, panel);
    }

    @Override
    protected boolean mine(Vehicle v) {
        return v instanceof Car;
    }

    @Override
    protected void move(Vehicle v) {
        long now = System.currentTimeMillis();
        if (now >= v.nextChange) {
            v.angle = Math.random() * 2 * Math.PI;
            v.nextChange = now + N * 1000L;
        }
        v.x += Math.cos(v.angle) * V * DT;
        v.y += Math.sin(v.angle) * V * DT;

        if (v.x < 0) {
            v.x = 0;
            v.angle = Math.PI - v.angle;
        }
        if (v.x > W - SIZE) {
            v.x = W - SIZE;
            v.angle = Math.PI - v.angle;
        }
        if (v.y < 0) {
            v.y = 0;
            v.angle = -v.angle;
        }
        if (v.y > H - SIZE) {
            v.y = H - SIZE;
            v.angle = -v.angle;
        }
    }
}
class TrainAI extends BaseAI {
    TrainAI(List<Vehicle> vehicles, JPanel panel) {
        super(vehicles, panel);
    }

    @Override
    protected boolean mine(Vehicle v) {
        return v instanceof Train;
    }

    @Override
    protected void move(Vehicle v) {
        v.x += v.dir * V * DT;
        if (v.x >= W - SIZE) {
            v.x = W - SIZE;
            v.dir = -1;
        }
        if (v.x <= 0) {
            v.x = 0;
            v.dir = 1;
        }
    }
}

class DrawPanel extends JPanel {
    private final List<Vehicle> vehicles;

    DrawPanel(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
        setPreferredSize(new Dimension(BaseAI.W, BaseAI.H));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (vehicles) {
            for (Vehicle v : vehicles) {
                if (v instanceof Express) g.setColor(Color.GREEN);
                else if (v instanceof Train) g.setColor(Color.RED);
                else g.setColor(Color.BLUE);
                g.fillOval((int) v.x, (int) v.y, BaseAI.SIZE, BaseAI.SIZE);
            }
        }
    }
}

public class Task2 {
    static int toPriority(String s) {
        if (s.equals("MIN")) return Thread.MIN_PRIORITY;
        if (s.equals("MAX")) return Thread.MAX_PRIORITY;
        return Thread.NORM_PRIORITY;
    }

    static JPanel makeControls(String title, BaseAI ai) {
        JPanel p = new JPanel();
        p.add(new JLabel(title));

        JButton pause = new JButton("Пауза");
        JButton resume = new JButton("Продовжити");
        JComboBox<String> prio = new JComboBox<>(new String[]{"MIN", "NORM", "MAX"});
        prio.setSelectedItem("NORM");

        pause.addActionListener(e -> ai.pauseAI());
        resume.addActionListener(e -> ai.resumeAI());
        prio.addActionListener(e -> ai.setPriority(toPriority((String) prio.getSelectedItem())));

        p.add(pause);
        p.add(resume);
        p.add(new JLabel("Пріоритет:"));
        p.add(prio);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<Vehicle> vehicles = new ArrayList<>();
            vehicles.add(new Car("Audi", 120, 5));
            vehicles.add(new Car("BMW", 140, 4));
            vehicles.add(new Car("Tesla", 150, 5));
            vehicles.add(new Train("Інтерсіті", 100, 10));
            vehicles.add(new Train("Регіональний", 80, 6));
            vehicles.add(new Express("Hyundai", 160, 8, "Київ-Львів"));

            for (Vehicle v : vehicles) {
                v.Show();
                System.out.println();
            }

            DrawPanel panel = new DrawPanel(vehicles);

            BaseAI carAI = new CarAI(vehicles, panel);
            BaseAI trainAI = new TrainAI(vehicles, panel);

            JPanel controls = new JPanel(new GridLayout(2, 1));
            controls.add(makeControls("Автомобілі:", carAI));
            controls.add(makeControls("Поїзди/Експреси:", trainAI));

            JFrame frame = new JFrame("Варіант 4");
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.add(controls, BorderLayout.SOUTH);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            carAI.start();
            trainAI.start();
        });
    }
}