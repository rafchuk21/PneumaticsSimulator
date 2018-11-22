package actuator.piston;

import java.security.InvalidParameterException;

public class LinearPiston {
    protected final double BORE_DIAMETER; //in^2
    protected final double ROD_DIAMETER; //in^2
    protected final double LENGTH; //in
    protected final double MIN_POSITION; //in
    protected final double MAX_POSITION; //in
    protected final double BORE_MASS; //lbmass

    protected final double EXTENSION_AREA; //in^2
    protected final double RETRACTION_AREA; //in^2

    protected final double ATMOSPHERIC_PRESSURE = 14.7; //PSI
    protected double FLOW_COEFFICIENT = 0.01;

    protected double position; //in
    protected double velocity; //in/s
    protected double extChamberPressure; //PSI
    protected double retChamberPressure; //PSI
    protected double extChamberVolume; //in^3
    protected double retChamberVolume; //in^3


    public LinearPiston(double boreDiameter, double rodDiameter, double minPosition, double maxPosition, double boreMass,
                  double length, double startingPosition, double flowCoefficient) {
        BORE_DIAMETER = boreDiameter;
        ROD_DIAMETER = rodDiameter;
        LENGTH = length;
        MIN_POSITION = minPosition;
        MAX_POSITION = maxPosition;
        BORE_MASS = boreMass;

        EXTENSION_AREA = Math.PI * (BORE_DIAMETER * BORE_DIAMETER);
        RETRACTION_AREA = Math.PI * (BORE_DIAMETER * BORE_DIAMETER - ROD_DIAMETER * ROD_DIAMETER);

        position = startingPosition;
        velocity = 0;
        extChamberPressure = ATMOSPHERIC_PRESSURE;
        retChamberPressure = ATMOSPHERIC_PRESSURE;
        extChamberVolume = position * EXTENSION_AREA;
        retChamberVolume = (LENGTH - position) * RETRACTION_AREA;

        FLOW_COEFFICIENT = flowCoefficient;

        if (MIN_POSITION <= 0 || MAX_POSITION >= LENGTH) {
            throw new InvalidParameterException("Min or Max position out of bounds");
        }

        if (startingPosition < MIN_POSITION || startingPosition > MAX_POSITION) {
            throw new InvalidParameterException("Starting position out of bounds");
        }
    }

    /**
     * Calculates the new pressure in the chamber.
     * Assumes: No change in temperature, no change in volume
     * @param dt time interval in seconds
     * @param chamberPressure pressure in the chamber
     * @param inPressure pressure of the input
     * @return new pressure in chamber
     */
    public double calcFlow(double dt, double chamberPressure, double chamberVolume, double inPressure) {
        double SCFM = FLOW_COEFFICIENT/(0.718*Math.pow(Math.abs(inPressure-chamberPressure),-.804)); //standard cubic feet per min
        //P1*V1 + P2*V2 = P3*V3
        //P3 = (P1*V1 + P2*V2) / V3
        double cubicInchesPerMinute = SCFM * 1728.0; //1728 cubic inches in a cubic foot
        double cubicInchesPerSecond = cubicInchesPerMinute / 60.0; //60 seconds in a minute
        double newPressure;
        if (inPressure > chamberPressure)
            newPressure = (chamberPressure * chamberVolume + inPressure * cubicInchesPerSecond * dt) / chamberVolume;
        else if (inPressure < chamberPressure)
            newPressure = (chamberPressure * chamberVolume - inPressure * cubicInchesPerSecond * dt) / chamberVolume;
        else
            newPressure = chamberPressure;
        //System.out.println(chamberPressure + ", " + chamberVolume + ", " + inPressure + " -> " + newPressure);
        return newPressure;
    }

    public double getPosition() {
        return position;
    }

    public double[] getPressures() {
        return new double[] {extChamberPressure, retChamberPressure};
    }

}
