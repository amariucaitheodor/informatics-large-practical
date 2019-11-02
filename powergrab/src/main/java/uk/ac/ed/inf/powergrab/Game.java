package uk.ac.ed.inf.powergrab;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.mapbox.geojson.*;

class Game {
    private PrintWriter moveChoiceWriter;
    private PrintWriter mapPathWriter;

    private Drone drone;
    private Map map;
    private static final double MOVEDIST = 0.0003;
    static final double RANGEDIST = 0.00025;
    static final double MOVEPOWERCOST = 1.25;
    static final double POWERWEIGHT = 0.35;
    static final double COINSWEIGHT = 1 - POWERWEIGHT;

    Game(long seed, FeatureCollection mapStations, Position initialDronePos, String droneType, String year, String month, String day, boolean submissionGeneration) throws FileNotFoundException, UnsupportedEncodingException
    {
		if(!droneType.equals("stateless") && !droneType.equals("stateful"))
		    throw new java.lang.IllegalArgumentException(String.format("Unrecognized drone type %s", droneType));

		if(!initialDronePos.inPlayArea())
		    throw new java.lang.IllegalArgumentException("Initial drone position not in play area");

        precomputeMovementShift();
		moveChoiceWriter = new PrintWriter(String.format("%s-%s-%s-%s.txt", droneType, day, month, year), "UTF-8");
		mapPathWriter = new PrintWriter(String.format("%s-%s-%s-%s.geojson", droneType, day, month, year), "UTF-8");
		map = new Map(initialDronePos, mapStations.features());
        drone = droneType.equals("stateful") ? Stateful.createInstance(initialDronePos, seed, submissionGeneration) : Stateless.createInstance(initialDronePos, seed, submissionGeneration);
    }
    
    static void precomputeMovementShift() {
    	// We first consider right-angled triangles with hypotenuse Game.moveDist, width latMove, and height longMove when travelling NNE, NE or ENE (the first quadrant).
        Position.moveShift.put(Direction.N, new Position.Shift(Game.MOVEDIST, 0));
    	Position.moveShift.put(Direction.NNE, new Position.Shift(Game.MOVEDIST * Math.sin(Math.toRadians(67.5)), Game.MOVEDIST * Math.cos(Math.toRadians(67.5))));
    	Position.moveShift.put(Direction.NE, new Position.Shift(Game.MOVEDIST * Math.sin(Math.toRadians(45)), Game.MOVEDIST * Math.cos(Math.toRadians(45))));
    	Position.moveShift.put(Direction.ENE, new Position.Shift(Game.MOVEDIST * Math.sin(Math.toRadians(22.5)), Game.MOVEDIST * Math.cos(Math.toRadians(22.5))));
    	Position.moveShift.put(Direction.E, new Position.Shift(0, Game.MOVEDIST));
        // If the drone was instead heading South or West, the latitude or longitude of the drone’s position would be decreasing, so we would instead subtract the heights and widths of similar triangles.
        Position.moveShift.put(Direction.ESE, new Position.Shift(- Game.MOVEDIST * Math.sin(Math.toRadians(22.5)), Game.MOVEDIST * Math.cos(Math.toRadians(22.5))));
        Position.moveShift.put(Direction.SE, new Position.Shift(- Game.MOVEDIST * Math.sin(Math.toRadians(45)), Game.MOVEDIST * Math.cos(Math.toRadians(45))));
        Position.moveShift.put(Direction.SSE, new Position.Shift(- Game.MOVEDIST * Math.sin(Math.toRadians(67.5)), Game.MOVEDIST * Math.cos(Math.toRadians(67.5))));
        Position.moveShift.put(Direction.S, new Position.Shift(- Game.MOVEDIST, 0));
        Position.moveShift.put(Direction.SSW, new Position.Shift(- Game.MOVEDIST * Math.sin(Math.toRadians(67.5)), - Game.MOVEDIST * Math.cos(Math.toRadians(67.5))));
        Position.moveShift.put(Direction.SW, new Position.Shift(- Game.MOVEDIST * Math.sin(Math.toRadians(45)), - Game.MOVEDIST * Math.cos(Math.toRadians(45))));
        Position.moveShift.put(Direction.WSW, new Position.Shift(- Game.MOVEDIST * Math.sin(Math.toRadians(22.5)), - Game.MOVEDIST * Math.cos(Math.toRadians(22.5))));
        Position.moveShift.put(Direction.W, new Position.Shift(0, - Game.MOVEDIST));
        Position.moveShift.put(Direction.WNW, new Position.Shift(Game.MOVEDIST * Math.sin(Math.toRadians(22.5)), - Game.MOVEDIST * Math.cos(Math.toRadians(22.5))));
        Position.moveShift.put(Direction.NW, new Position.Shift(Game.MOVEDIST * Math.sin(Math.toRadians(45)), - Game.MOVEDIST * Math.cos(Math.toRadians(45))));
        Position.moveShift.put(Direction.NNW, new Position.Shift(Game.MOVEDIST * Math.sin(Math.toRadians(67.5)), - Game.MOVEDIST * Math.cos(Math.toRadians(67.5))));
    }
	
    void play() {
        while(drone.hasMoves() && drone.hasPower()) 
        {
    		Position formerPos = drone.getPosition();
    		Direction choice = drone.chooseMoveDirection(formerPos, map);
    		drone.move(choice);

    		moveChoiceWriter.println(String.format("%s,%s,%s,%s,%s,%s,%s", formerPos.latitude, formerPos.longitude, choice.toString(), drone.getPosition().latitude, drone.getPosition().longitude, drone.getCoins(), drone.getPower()));
    	    map.addPositionToPath(drone.getPosition());
    	}

		mapPathWriter.println(map.createOutputMap().toJson());

        moveChoiceWriter.close();
        mapPathWriter.close();
    }

    double getGameScore() {
    	return drone.getCoins();
    }
}