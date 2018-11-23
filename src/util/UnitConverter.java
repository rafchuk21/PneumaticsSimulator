package util;

public class UnitConverter {
    public static double lbmassToKg(double lbmass) {
        return lbmass * 0.453592;
    }

    public static double kgToLbmass(double kg) {
        return kg / 0.453592;
    }

    public static double inchesToMeters(double inches) {
        return inches * .0254;
    }

    public static double metersToInches(double meters) {
        return meters / .0254;
    }

    public static double lbforcetoNewton(double lbforce) {
        return lbforce * 4.44822;
    }

    public static double newtonsToLbforce(double newtons) {
        return newtons / 4.44822;
    }

    public static double psiToNPSM(double psi) {
        return psi * 6894.76;
    }

    public static double npsmToPSI(double npsm) {
        return npsm / 6894.76;
    }
}
