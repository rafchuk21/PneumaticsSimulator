package actuator.piston;

import util.UnitConverter;

import java.security.InvalidParameterException;

public abstract class LinearPiston {
    protected final double BORE_DIAMETER; //m
    protected final double ROD_DIAMETER; //m
    protected final double LENGTH; //m
    protected final double MIN_POSITION; //m
    protected final double MAX_POSITION; //m
    protected final double BORE_MASS; //kg

    protected final double EXTENSION_AREA; //m^2
    protected final double RETRACTION_AREA; //m^2

    protected final double ATMOSPHERIC_PRESSURE = 101325.0; //N/m^2
    protected double FLOW_COEFFICIENT = 0.01;

    protected double position; //m
    protected double velocity; //m/s
    protected double extChamberPressure; //N/m^2
    protected double retChamberPressure; //N/m^2
    protected double extChamberVolume; //m^3
    protected double retChamberVolume; //m^3

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
     */
    public LinearPiston(double boreDiameter, double rodDiameter, double minPosition, double maxPosition, double boreMass,
                  double length, double startingPosition, double flowCoefficient) {
        BORE_DIAMETER = UnitConverter.inchesToMeters(boreDiameter);
        ROD_DIAMETER = UnitConverter.inchesToMeters(rodDiameter);
        LENGTH = UnitConverter.inchesToMeters(length);
        MIN_POSITION = UnitConverter.inchesToMeters(minPosition);
        MAX_POSITION = UnitConverter.inchesToMeters(maxPosition);
        BORE_MASS = UnitConverter.lbmassToKg(boreMass);

        EXTENSION_AREA = Math.PI * (BORE_DIAMETER * BORE_DIAMETER) / 4.0;
        RETRACTION_AREA = Math.PI * (BORE_DIAMETER * BORE_DIAMETER - ROD_DIAMETER * ROD_DIAMETER) / 4.0;

        position = UnitConverter.inchesToMeters(startingPosition);
        velocity = 0;
        extChamberPressure = ATMOSPHERIC_PRESSURE;
        retChamberPressure = ATMOSPHERIC_PRESSURE;
        extChamberVolume = position * EXTENSION_AREA;
        retChamberVolume = (LENGTH - position) * RETRACTION_AREA;

        FLOW_COEFFICIENT = flowCoefficient;

        if (MIN_POSITION <= 0 || MAX_POSITION >= LENGTH) {
            throw new InvalidParameterException("Min or Max position out of bounds");
        }

        if (position < MIN_POSITION || position > MAX_POSITION) {
            throw new InvalidParameterException("Starting position out of bounds: " + position +
                    ", [" + minPosition + ", " + maxPosition + "]");
        }
    }

    /**
     * Calculates the new pressure in the chamber.
     * Assumes: No change in temperature, no change in volume
     * @param dt time interval in seconds [s]
     * @param chamberPressure pressure in the chamber [N/m^2]
     * @param chamberVolume Volume of chamber [m^3]
     * @param inPressure pressure of the input [N/m^2]
     * @return new pressure in chamber [N/m^2]
     */
    public double calcFlow(double dt, double chamberPressure, double chamberVolume, double inPressure) {
        double SCFM = FLOW_COEFFICIENT/(0.718*Math.pow(Math.abs(UnitConverter.npsmToPSI(inPressure-chamberPressure)),-.804)); //standard cubic feet per min
        //P1*V1 + P2*V2 = P3*V3
        //P3 = (P1*V1 + P2*V2) / V3
        double cubicMetersPerMinute = SCFM * .0283; //.0283 cubic meters in a cubic foot
        double cubicMetersPerSecond = cubicMetersPerMinute / 60.0; //60 seconds in a minute
        double newPressure;
        if (inPressure > chamberPressure)
            newPressure = (chamberPressure * chamberVolume + inPressure * cubicMetersPerSecond * dt) / chamberVolume;
        else if (inPressure < chamberPressure)
            newPressure = (chamberPressure * chamberVolume - inPressure * cubicMetersPerSecond * dt) / chamberVolume;
        else
            newPressure = chamberPressure;
        //System.out.println(chamberPressure + ", " + chamberVolume + ", " + inPressure + " -> " + newPressure);
        return newPressure;
    }

    public double getPosition() {
        return UnitConverter.metersToInches(position);
    }

    public double getPosition(boolean metric) {
        if (metric)
            return position;
        else
            return getPosition();
    }

    public double[] getPressures() {
        return new double[] { UnitConverter.npsmToPSI(extChamberPressure), UnitConverter.npsmToPSI(retChamberPressure)};
    }

    public double[] getPressures(boolean metric) {
        if (metric)
            return new double[] {extChamberPressure, retChamberPressure};
        else
            return getPressures();
    }

    public void update(double dt, double loadForce, double loadMass, double extPortPressure, double retPortPressure) {
        update(dt, loadForce, loadMass, extPortPressure, retPortPressure, false);
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
    public abstract void update(double dt, double loadForce, double loadMass, double extPortPressure, double retPortPressure, boolean SI);

    /**
     * Update the piston, should only be called by update method of child classes
     * @param dt time interval [s]
     * @param loadForce force opposing piston extension [N]
     * @param loadMass mass attached to piston [kg]
     */
    public void update(double dt, double loadForce, double loadMass) {
        double netForce = getNetPressureForce() - loadForce;
        double acceleration = netForce / (BORE_MASS + loadMass);
        accelerate(dt, acceleration);
        move(dt);
    }

    public double getNetPressureForce() {
        return extChamberPressure * EXTENSION_AREA - retChamberPressure * RETRACTION_AREA;
    }

    public double accelerate(double dt, double acceleration) {
        return accelerate(dt, acceleration, false);
    }

    /**
     * Accelerates the system according to an acceleration and time interval
     * @param dt time interval [s]
     * @param acceleration acceleration of system [m/s^2]
     * @param metric whether return value should be in [in/s^2] or [m/s^2]
     * @return new velocity, in either [in/s^2] or [m/s^2]
     */
    public double accelerate(double dt, double acceleration, boolean metric) {
        velocity += acceleration * dt;
        if (metric)
            return velocity;
        else
            return UnitConverter.metersToInches(velocity);
    }

    public double move(double dt) {
        return move(dt, false);
    }

    public double move(double dt, boolean metric) {
        position += velocity * dt;

        //check limits of travel
        if (position >= MAX_POSITION) {
            position = MAX_POSITION;
            velocity = 0;
        } else if (position <= MIN_POSITION) {
            position = MIN_POSITION;
            velocity = 0;
        }

        //update chamber pressures according to P1*V1 = P2*V2
        extChamberPressure = extChamberPressure * extChamberVolume / (position * EXTENSION_AREA);
        retChamberPressure = retChamberPressure * retChamberVolume / ((LENGTH - position) * RETRACTION_AREA);

        //update chamber volumes
        extChamberVolume = position * EXTENSION_AREA;
        retChamberVolume = (LENGTH - position) * RETRACTION_AREA;

        if (metric)
            return position;
        else
            return UnitConverter.metersToInches(position);
    }

    public abstract void calcFlows(double dt, double extPortPressure, double retPortPressure);

    /**
     * Finds the acceleration of the piston shaft
     * @param netForce Net force on the piston (including pressures) [lbforce or N]
     * @param loadMass Mass piston is moving (excluding piston shaft) [lbmass or kg]
     * @param SI Boolean whether inputs are in SI or not
     * @return acceleration of piston shaft [m/s^2]
     */
    public double getNetAcceleration(double netForce, double loadMass, boolean SI) {
        if (!SI) {
            netForce = UnitConverter.lbforcetoNewton(netForce);
            loadMass = UnitConverter.lbmassToKg(loadMass);
        }

        return netForce / (loadMass + BORE_MASS);
    }
}
