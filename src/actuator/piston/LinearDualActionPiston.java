package actuator.piston;

public class LinearDualActionPiston extends LinearPiston {

    public LinearDualActionPiston(double boreDiameter, double rodDiameter, double minPosition, double maxPosition, double boreMass,
                                  double length, double startingPosition, double flowCoefficient) {
        super(boreDiameter, rodDiameter, minPosition, maxPosition, boreMass, length, startingPosition, flowCoefficient);
    }

    public void update(double dt, double loadForce, double loadMass, double extPortPressure, double retPortPressure) {
        extChamberPressure = calcFlow(dt, extChamberPressure, extChamberVolume, extPortPressure);
        retChamberPressure = calcFlow(dt, retChamberPressure, retChamberVolume, retPortPressure);

        double netForce = extChamberPressure * EXTENSION_AREA - retChamberPressure * RETRACTION_AREA - loadForce;
        double acceleration = netForce / (BORE_MASS + loadMass);
        velocity += acceleration * dt;
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
    }
}
