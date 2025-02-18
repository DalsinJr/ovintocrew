package com.example.ovintocrew.Util;

import java.text.DecimalFormat;

public class DistanceUtil {
    private static final double EARTH_RADIUS = 6371.0;
    static DecimalFormat distanceFormatter = new DecimalFormat("#.0");

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;
        return Double.parseDouble(distanceFormatter.format(distance));
    }
}
