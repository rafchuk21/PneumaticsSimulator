import actuator.piston.LinearDualActionPiston;
import mechanism.linearTranslator.SimpleLinearTranslator;

import java.io.*;

public class Main {
    private final static double ATMOSPHERIC_PRESSURE = 14.7;
    private final static double ATM = ATMOSPHERIC_PRESSURE;
    private final static double dt = 0.0001;
    private final static double Cv = .5;
    private final static double endTime = 3;
    private static LinearDualActionPiston P1;
    private static SimpleLinearTranslator mech;
    private static double currentTime = 0;
    private static String linearDualActionPistonFilePath = "io/out/simulation/SimpleLinearDualActionPiston" + System.nanoTime() + ".txt";
    private static String linearTranslatorFilePath = "io/out/simulation/SimpleLinearTranslator" + System.nanoTime() + ".txt";
    private static PrintWriter pw;


    public static void main(String[] args) {
        P1 = new LinearDualActionPiston(1.25, 0.25, 0.5,
                7.5, .1, 8, 0.5, Cv);
        mech = new SimpleLinearTranslator(P1, 50, Math.PI/3);
        try {
            File outputFile = new File(linearTranslatorFilePath);
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
        pw.println(currentTime + ", " + mech.getPosition() + ", " + mech.getPiston().getPressures()[0] + "," + mech.getPiston().getPressures()[1]);
        if (Math.floor(currentTime * 2.0) % 2 == 0)
            mech.update(dt, 134.7, ATM, false);
        else
            mech.update(dt, ATM, ATM, false);
    }
}
