package actuator.piston;

import util.UnitConverter;

public class LinearSingleActionPiston extends LinearPiston {
    private final double SPRING_CONSTANT; //lbforce/in
    private final double SPRING_REST_FORCE; //lbforce

    /**
     *
     * @param boreDiameter bore diameter [in]
     * @param rodDiameter rod diameter [in]
     * @param minPosition min retraction position [in]
     * @param maxPosition max extension position [in]
     * @param boreMass mass of bore [lbmass]
     * @param length length of cylinder chamber [in]
     * @param startingPosition starting position [in]
     * @param flowCoefficient Cv
     * @param springConstant Retracting spring constant [N/m]
     * @param springRestForce Spring force when fully retracted [N]
     */
    public LinearSingleActionPiston(double boreDiameter, double rodDiameter, double minPosition, double maxPosition, double boreMass,
                                  double length, double startingPosition, double flowCoefficient, double springConstant,
                                    double springRestForce) {
        super(boreDiameter, rodDiameter, minPosition, maxPosition, boreMass, length, startingPosition, flowCoefficient);
        SPRING_CONSTANT = springConstant;
        SPRING_REST_FORCE = springRestForce;
    }

    /**
     * Updates the piston
     * @param dt time interval [s]
     * @param loadForce force opposing piston extension [lbforce or N]
     * @param loadMass mass attached to piston [lbmass or kg]
     * @param extPortPressure pressure going into extension port [PSI or N/m^2]
     * @param retPortPressure pressure going into retraction port [PSI or N/m^2]
     * @param SI whether input is in SI units or not
     */
    public void update(double dt, double loadForce, double loadMass, double extPortPressure, double retPortPressure, boolean SI) {
        if (!SI) {
            loadForce = UnitConverter.lbforcetoNewton(loadForce);
            loadMass = UnitConverter.lbmassToKg(loadMass);
            extPortPressure = UnitConverter.psiToNPSM(extPortPressure);
            retPortPressure = UnitConverter.psiToNPSM(retPortPressure);
        }

        calcFlows(dt, extPortPressure, retPortPressure);

        super.update(dt, loadForce + SPRING_REST_FORCE + SPRING_CONSTANT * (position - MIN_POSITION), loadMass);
    }

    public void calcFlows(double dt, double extPortPressure, double retPortPressure) {
        extChamberPressure = calcFlow(dt, extChamberPressure, extChamberVolume, extPortPressure);
    }
}
