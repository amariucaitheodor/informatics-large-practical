package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

class Game {
    static final double RANGEDIST = 0.00025;
    static final double MOVEPOWERCOST = 1.25;
    static final double POWERWEIGHT = 0.35;
    static final double COINSWEIGHT = 1 - POWERWEIGHT;
    private static final double MOVEDIST = 0.0003;
    private final List<Feature> mapStationsAsFeatures;
    private PrintWriter moveChoiceWriter;
    private PrintWriter mapPathWriter;
    private List<Point> path;
    private Drone drone;
    private Map map;

    Game(long seed, FeatureCollection mapStationsAsCollection, Position initialDronePos,
         String droneType, String year, String month, String day, boolean submissionGeneration)
            throws FileNotFoundException, UnsupportedEncodingException {
        if (!droneType.equals("stateless") && !droneType.equals("stateful"))
            throw new java.lang.IllegalArgumentException(String.format("Unrecognized drone type %s", droneType));

        if (!initialDronePos.inPlayArea())
            throw new java.lang.IllegalArgumentException("Initial drone position not in play area");

        precomputeMovementShift();
        moveChoiceWriter = new PrintWriter(String.format("%s-%s-%s-%s.txt", droneType, day, month, year), "UTF-8");
        mapPathWriter = new PrintWriter(String.format("%s-%s-%s-%s.geojson", droneType, day, month, year), "UTF-8");

        // keep track of drone path
        path = new ArrayList<>();
        path.add(Point.fromLngLat(initialDronePos.longitude, initialDronePos.latitude));

        this.mapStationsAsFeatures = mapStationsAsCollection.features();
        assert mapStationsAsFeatures != null;
        map = new Map(mapStationsAsFeatures);

        drone = droneType.equals("stateful") ?
                Stateful.createInstance(initialDronePos, seed, submissionGeneration, map) :
                Stateless.createInstance(initialDronePos, seed, submissionGeneration);
    }

    // this will optimize the runtime of the application by storing precalculated trigonometric shits in position
    static void precomputeMovementShift() {
        // We first consider right-angled triangles with hypotenuse Game.moveDist, width latMove, and height longMove when travelling NNE, NE or ENE (the first quadrant).
        Position.moveShift.put(Direction.N, new double[]{Game.MOVEDIST, 0});
        Position.moveShift.put(Direction.NNE, new double[]{Game.MOVEDIST * Math.sin(Math.toRadians(67.5)),
                Game.MOVEDIST * Math.cos(Math.toRadians(67.5))});
        Position.moveShift.put(Direction.NE, new double[]{Game.MOVEDIST * Math.sin(Math.toRadians(45)),
                Game.MOVEDIST * Math.cos(Math.toRadians(45))});
        Position.moveShift.put(Direction.ENE, new double[]{Game.MOVEDIST * Math.sin(Math.toRadians(22.5)),
                Game.MOVEDIST * Math.cos(Math.toRadians(22.5))});
        Position.moveShift.put(Direction.E, new double[]{0, Game.MOVEDIST});
        // If the drone was instead heading South or West, the latitude or longitude of the drone’s position would be decreasing, so we would instead subtract the heights and widths of similar triangles.
        Position.moveShift.put(Direction.ESE, new double[]{-Game.MOVEDIST * Math.sin(Math.toRadians(22.5)),
                Game.MOVEDIST * Math.cos(Math.toRadians(22.5))});
        Position.moveShift.put(Direction.SE, new double[]{-Game.MOVEDIST * Math.sin(Math.toRadians(45)),
                Game.MOVEDIST * Math.cos(Math.toRadians(45))});
        Position.moveShift.put(Direction.SSE, new double[]{-Game.MOVEDIST * Math.sin(Math.toRadians(67.5)),
                Game.MOVEDIST * Math.cos(Math.toRadians(67.5))});
        Position.moveShift.put(Direction.S, new double[]{-Game.MOVEDIST, 0});
        Position.moveShift.put(Direction.SSW, new double[]{-Game.MOVEDIST * Math.sin(Math.toRadians(67.5)),
                -Game.MOVEDIST * Math.cos(Math.toRadians(67.5))});
        Position.moveShift.put(Direction.SW, new double[]{-Game.MOVEDIST * Math.sin(Math.toRadians(45)),
                -Game.MOVEDIST * Math.cos(Math.toRadians(45))});
        Position.moveShift.put(Direction.WSW, new double[]{-Game.MOVEDIST * Math.sin(Math.toRadians(22.5)),
                -Game.MOVEDIST * Math.cos(Math.toRadians(22.5))});
        Position.moveShift.put(Direction.W, new double[]{0, -Game.MOVEDIST});
        Position.moveShift.put(Direction.WNW, new double[]{Game.MOVEDIST * Math.sin(Math.toRadians(22.5)),
                -Game.MOVEDIST * Math.cos(Math.toRadians(22.5))});
        Position.moveShift.put(Direction.NW, new double[]{Game.MOVEDIST * Math.sin(Math.toRadians(45)),
                -Game.MOVEDIST * Math.cos(Math.toRadians(45))});
        Position.moveShift.put(Direction.NNW, new double[]{Game.MOVEDIST * Math.sin(Math.toRadians(67.5)),
                -Game.MOVEDIST * Math.cos(Math.toRadians(67.5))});
    }

    void play() {
        while (drone.hasMovesLeft() && drone.hasPower()) {
            Position formerPos = drone.getPosition();
            Direction choice = drone.chooseMoveDirection(formerPos, map);
            drone.move(choice);

            moveChoiceWriter.println(String.format("%s,%s,%s,%s,%s,%s,%s", formerPos.latitude, formerPos.longitude,
                    choice.toString(), drone.getPosition().latitude, drone.getPosition().longitude, drone.getCoins(),
                    drone.getPower()));

            path.add(Point.fromLngLat(drone.getPosition().longitude, drone.getPosition().latitude));
        }

        mapPathWriter.println(createOutputGeoJson().toJson());

        moveChoiceWriter.close();
        mapPathWriter.close();
    }

    private FeatureCollection createOutputGeoJson() {
        List<Feature> finalFeatures = mapStationsAsFeatures;
        finalFeatures.add(Feature.fromGeometry(LineString.fromLngLats(path)));
        return FeatureCollection.fromFeatures(finalFeatures);
    }

    double getGameScore() {
        if (drone.hasMovesLeft())
            throw new java.lang.IllegalArgumentException("Game score requested before the game has finished!");

        return drone.getCoins();
    }
}