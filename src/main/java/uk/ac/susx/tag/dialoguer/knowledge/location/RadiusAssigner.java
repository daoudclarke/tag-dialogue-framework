package uk.ac.susx.tag.dialoguer.knowledge.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The responsibility of this class is to assign radii to nodes returned from an overpass query.
 *
 * Individual nodes already have a single point long and lat, and no other suggestion of size, so a default or specified
 * radius in metres is used.
 *
 * However "ways" are defined as a path of nodes, and therefore have no single point long and lat. But because the path
 * defines the building, we have information that can inform a vaguely suitable radius for it.
 *
 * This following is the procedure taken by the "assignRadii" method:
 *
 *  - Assign to each node a default radius
 *  - For each "way":
 *      - use the average lon and lat of all nodes in the way path as the center point of the way.
 *      - Create a point from the max lon and max lat
 *      - Create a point from the min lon and min lat
 *      - Calculate the 2 haversine distances:
 *         1. between the center and the max point
 *         2. between the center and the min point
 *      - Take the greatest distance as the radius of the "way"
 *
 * The haversine distance functions are also exposed for public use.
 *
 * Created by Andrew D. Robertson on 06/08/2014.
 */
public class RadiusAssigner {

    public static final double R = 6372.8; // Earth radius in kilometers

    public static List<ResultsElement> assignRadii(List<ResultsElement> results){
        return assignRadii(results, 20);
    }

    public static List<ResultsElement> assignRadii(List<ResultsElement> results, double defaultRadiusMetres){

        Map<Long, ResultsElement> id2Node = new HashMap<>();
        List<ResultsElement> ways = new ArrayList<>();

        for (ResultsElement r : results) {
            if (r.type.equals("node")) {
                r.radius = defaultRadiusMetres;
                id2Node.put(r.id, r);
            } else if (r.type.equals("way")) {
                ways.add(r);
            }
        }

        for (ResultsElement r : ways) {
            int n = r.nodes.size();
            double cumulLat = 0;
            double maxLat = id2Node.get(r.nodes.get(0)).lat;
            double minLat = id2Node.get(r.nodes.get(0)).lat;
            double cumulLon = 0;
            double maxLon = id2Node.get(r.nodes.get(0)).lon;
            double minLon = id2Node.get(r.nodes.get(0)).lon;
            for (Long id : r.nodes){
                double lat = id2Node.get(id).lat;
                double lon = id2Node.get(id).lon;
                cumulLat += lat;
                cumulLon += lon;
                if (lat > maxLat) maxLat = lat;
                if (lat < minLat) minLat = lat;
                if (lon > maxLon) maxLon = lon;
                if (lon < minLon) minLon = lon;
            }
            r.lat = cumulLat / n;
            r.lon = cumulLon / n;

            r.radius = Math.max(haversineM(r.lat, r.lon,maxLat,maxLon), haversineM(r.lat,r.lon,minLat,minLon));
        }
        return results;
    }


    public static double haversineM(double lat1, double lon1, double lat2, double lon2){
        return haversineKM(lat1,lon1,lat2,lon2) * 1000;
    }

    public static double haversineKM(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    /**
     * Calculate direction from (lat1, lon1) to (lat2, lon2) as one of 8 possibilities (N, NE, E, SE, S, SW, W, NW)
     * see https://software.intel.com/en-us/blogs/2012/11/30/calculating-a-bearing-between-points-in-location-aware-apps
     *
     * @param lat1 originating lattitude
     * @param lon1 originating longitude
     * @param lat2 destination lattitude
     * @param lon2 destrination longitude
     * @return String description
     */
    public static String getDirection(double lat1, double lon1, double lat2, double lon2) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        double bearing = bearingFromSource(lat1, lon1, lat2, lon2);
        int index = ((int) ((bearing + 22.5) / 45.0)) % 8;
        return directions[index];
    }

    static public double bearingFromSource (double lat1, double long1, double lat2, double long2)
    {
        return (subBearing(lat1, long1, lat2, long2) + 360.0) % 360;
    }

    static public double bearingFromDestination (double lat1, double long1, double lat2, double long2)
    {
        return (subBearing(lat2, long2, lat1, long1) + 180.0) % 360;
    }

    static private double subBearing(double lat1, double long1, double lat2, double long2)
    {
        double degToRad = Math.PI / 180.0;
        double phi1 = lat1 * degToRad;
        double phi2 = lat2 * degToRad;
        double lam1 = long1 * degToRad;
        double lam2 = long2 * degToRad;

        return Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)
        ) * 180/Math.PI;
    }

}
