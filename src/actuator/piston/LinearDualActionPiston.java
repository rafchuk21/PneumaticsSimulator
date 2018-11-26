package actuator.piston;

import util.UnitConverter;

public class LinearDualActionPiston extends LinearPiston {

    /**
     * @param boreDiameter bore diameter [in]
     * @param rodDiameter rod diameter [in]
     * @param minPosition min retraction position [in]
     * @param maxPosition max extension position [in]
     * @param boreMass mass of bore [lbmass]
     * @param length length of cylinder chamber [in]
     * @param startingPosition starting position [in]
     * @param flowCoefficient Cv
     */
    public LinearDualActionPiston(double boreDiameter, double rodDiameter, double minPosition, double maxPosition, double boreMass,
                                  double length, double startingPosition, double flowCoefficient) {
        super(boreDiameter, rodDiameter, minPosition, maxPosition, boreMass, length, startingPosition, flowCoefficient);
    }

    /**
     * Update the piston
     * @param dt time interval [s]
     * @param loadForce force opposing piston extension [lbforce or N]
     * @param loadMass mass attached to piston [lbmass or kg]
     * @param extPortPressure pressure going into extension port [PSI or N/m^2]
     * @param retPortPressure pressure going into retraction port [PSI or N/m^2]
     * @param SI whether input is in SI units or not
     */
    public void update(double dt, double loadForce, double loadMass, double extPortPressure, double retPortPressure, boolean SI) {
        if (!SI) { //if the inputs weren't in SI, converts them to SI
            loadForce = UnitConverter.lbforcetoNewton(loadForce);
            loadMass = UnitConverter.lbmassToKg(loadMass);
            extPortPressure = UnitConverter.psiToNPSM(extPortPressure);
            retPortPressure = UnitConverter.psiToNPSM(retPortPressure);
        }

        calcFlows(dt, extPortPressure, retPortPressure); //calculate the pressure change from airflow into piston chambers

        super.update(dt, loadForce, loadMass);
    }
    
    /**
     * Update the flows in the chambers, assuming no change in volume or temperature
     * @param dt time interval [s]
     * @param extPortPressure Pressure going into the extension port [N/m^2]
     * @param retPortPressure Pressure going into the retraction port [N/m^2]
     */
    public void calcFlows(double dt, double extPortPressure, double retPortPressure) {
        extChamberPressure = calcFlow(dt, extChamberPressure, extChamberVolume, extPortPressure);
        retChamberPressure = calcFlow(dt, retChamberPressure, retChamberVolume, retPortPressure);
    }
}
