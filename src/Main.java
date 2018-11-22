import actuator.piston.LinearDualActionPiston;

import java.io.*;
import java.util.Arrays;

public class Main {
    private final static double ATMOSPHERIC_PRESSURE = 14.7;
    private final static double ATM = ATMOSPHERIC_PRESSURE;
    private final static double dt = 0.001;
    private final static double Cv = .5;
    private final static double endTime = 1;
    private static LinearDualActionPiston P1;
    private static double currentTime = 0;
    private static String filePath = "io/out/simulation/SimpleLinearDualActionPiston" + System.nanoTime() + ".txt";
    private static PrintWriter pw;


    public static void main(String[] args) {
        P1 = new LinearDualActionPiston(1.25, 0.25, 0.5,
                7.5, .1, 8, 0.5, Cv);
        try {
            File outputFile = new File(filePath);
            pw = new PrintWriter(new FileOutputStream(outputFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        pw.println("dt = " + dt + "; " + "Cv = " + Cv);

        for (; currentTime < endTime; currentTime += dt) {
            loop();
        }

        pw.close();
    }

    public static void loop() {
        System.out.println(currentTime + ": " + P1.getPosition() + ";\t" + Arrays.toString(P1.getPressures()));
        pw.println(currentTime + ", " + P1.getPosition() + ", " + P1.getPressures()[0] + "," + P1.getPressures()[1]);
        P1.update(dt, .1, 3, 120, ATM);
    }
}
