package edu.duke.pratt.hal.triangletraffic.utility;

/**
 * Created by tedzhu on 6/2/15.
 */
public class Distance {

    private double meters;

    public Distance(double meters) {
        this.meters = meters;
    }

    public String getDisplayString() {
        // Assume US Imperial for now. Will link to settings later.

        String unitsSystem = "US Imperial";

        double toMiles = 0.000621371;
        double toYards = 1.09361;
        double toFeet = 3.28084;
        double toInches = 39.3701;

        if (unitsSystem.equals("US Imperial")) {

            double distanceMiles = this.meters * toMiles;

            if (distanceMiles > 1.0) {
                // Return miles.
                return String.format("%.2f", distanceMiles) + " mi";
            } else if (distanceMiles > 0.094697) {
                // Return yards. (0.094697 mi == 500 ft)
                double distanceYards = this.meters * toYards;
                return String.format("%.0f", distanceYards) + " yds";
            } else if (this.meters * toFeet > 1.0) {
                // Return feet.
                double distanceFeet = this.meters * toFeet;
                return String.format("%.0f", distanceFeet) + " ft";
            } else if (this.meters * toInches > 1.0) {
                // Return whole inches.
                double distanceInches = this.meters * toInches;
                return String.format("%.0f", distanceInches) + " in";
            } else {
                // Return decimal inches.
                double distanceInches = this.meters * toInches;
                return String.format("%.1f", distanceInches) + " in";
            }

        }

        return "Not Imperial.";

    }

    public double getMeters() {
        return this.meters;
    }

}
