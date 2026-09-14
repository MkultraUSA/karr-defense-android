package com.codex.karrdefense;

import java.util.Locale;

/**
 * Passive heuristic classifier for discovered devices.
 *
 * The audit covers cars, trucks and delivery/autonomous robots. Nothing here
 * contacts a device — it only labels the advertisement/beacon text that the
 * passive BLE and Wi-Fi scans already collected. Results are always treated as
 * "candidate" labels for a human operator to confirm.
 */
public final class VehicleClassifier {

    public static final String CAR = "car";
    public static final String TRUCK = "truck";
    public static final String ROBOT = "robot";
    public static final String UNKNOWN = "unknown";

    private static final String[] ROBOT_WORDS = {
            "robot", "delivery bot", "kiwibot", "starship", "serve robotics",
            "nuro", "cartken", "refraction", "sidewalk", "ugv", "autonomous"
    };

    private static final String[] TRUCK_WORDS = {
            "truck", " ram ", "semi", "freight", "trailer", "cargo", "f-150", "f150",
            "silverado", "sierra", "tundra", "ridgeline", "titan", "transit",
            "sprinter", "promaster", "box truck"
    };

    private static final String[] CAR_WORDS = {
            "car", "sedan", "coupe", "suv", "ford", "chevrolet", "chevy", "gmc",
            "cadillac", "buick", "onstar", "toyota", "lexus", "honda", "acura",
            "nissan", "infiniti", "hyundai", "kia", "genesis", "mazda", "subaru",
            "volkswagen", "audi", "bmw", "mercedes", "mini", "tesla", "rivian",
            "jeep", "dodge", "chrysler", "uconnect", "volvo", "polestar", "porsche",
            "jaguar", "land rover", "range rover", "carplay", "android auto"
    };

    private VehicleClassifier() {
    }

    /**
     * @param searchableText lower-cased advertisement/beacon text (see Observation.searchableText)
     * @param vendor         OUI vendor string, may be null
     * @return one of CAR / TRUCK / ROBOT / UNKNOWN
     */
    public static String classify(String searchableText, String vendor) {
        String text = " " + (searchableText == null ? "" : searchableText.toLowerCase(Locale.US)) + " ";
        if (matches(text, ROBOT_WORDS)) return ROBOT;
        if (matches(text, TRUCK_WORDS)) return TRUCK;
        if (matches(text, CAR_WORDS)) return CAR;
        return UNKNOWN;
    }

    public static String describe(String category) {
        if (ROBOT.equals(category)) return "Delivery robot / autonomous vehicle";
        if (TRUCK.equals(category)) return "Truck / commercial vehicle";
        if (CAR.equals(category)) return "Passenger car";
        return "Unclassified device";
    }

    /** Label shown in the scan-results list, e.g. "CAR?". */
    public static String badge(String category) {
        return UNKNOWN.equals(category) ? "--" : category.toUpperCase(Locale.US) + "?";
    }

    private static boolean matches(String text, String[] words) {
        for (String word : words) {
            if (text.contains(word)) return true;
        }
        return false;
    }
}