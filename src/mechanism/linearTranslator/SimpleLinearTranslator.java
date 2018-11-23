package mechanism.linearTranslator;

import actuator.piston.LinearDualActionPiston;
import actuator.piston.LinearPiston;
import actuator.piston.LinearSingleActionPiston;
import util.UnitConverter;

public class SimpleLinearTranslator {
    private LinearPiston mechanismPiston;
    private final double HORIZONTAL_ANGLE; //radians
    private final double LOAD_MASS; //kg
    private final double LOAD_FORCE; //N

    /**
     *
     * @param p Piston to use
     * @param loadMass Mass of load on translator [lbmass]
     * @param angle Angle of piston to horizontal [radians]
     */
    public SimpleLinearTranslator(LinearPiston p, double loadMass, double angle, boolean SI) {
        mechanismPiston = p;
        HORIZONTAL_ANGLE = angle;
        if (!SI)
            LOAD_MASS = UnitConverter.lbmassToKg(loadMass);
        else
            LOAD_MASS = loadMass;
        LOAD_FORCE = LOAD_MASS * Math.sin(HORIZONTAL_ANGLE) * 9.8;
        System.out.println(LOAD_MASS + ", " + LOAD_FORCE);
    }

    public SimpleLinearTranslator(LinearPiston p, double loadMass, double angle) {
        this(p, loadMass, angle, false);
    }

    /**
     *
     * @param dt time interval [s]
     * @param extPortPressure pressure into extension port [PSI or N/m^2]
     * @param retPortPressure pressure into retraction port [PSI or N/m^2]
     * @param SI whether input is in SI units or not
     */
    public void update(double dt, double extPortPressure, double retPortPressure, boolean SI) {
        if (!SI) {
            extPortPressure = UnitConverter.psiToNPSM(extPortPressure);
            retPortPressure = UnitConverter.psiToNPSM(retPortPressure);
        }
        //mechanismPiston.update(dt, LOAD_FORCE, LOAD_MASS, extPortPressure, retPortPressure, true);

        mechanismPiston.calcFlows(dt, extPortPressure, retPortPressure);
        mechanismPiston.accelerate(dt, mechanismPiston.getNetAcceleration(mechanismPiston.getNetPressureForce()
                - LOAD_FORCE, LOAD_MASS, true), true);
        mechanismPiston.move(dt);
    }

    public double getPosition() {
        return mechanismPiston.getPosition();
    }

    public double getPosition(boolean metric) {
        return mechanismPiston.getPosition(metric);
    }

    public LinearPiston getPiston() {
        return mechanismPiston;
    }
}
