package uk.ac.ed.inf.powergrab;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.mapbox.geojson.*;

class Game {
    private PrintWriter moveChoiceWriter;
    private PrintWriter mapPathWriter;
    
    private final Random dirGenerator;
    private Drone drone;
    private GameMap gameMap;
    private static final double MOVEDIST = 0.0003;
    static final double RANGEDIST = 0.00025;
    static final double MOVEPOWERCOST = 1.25;
    // TODO: increase power weight as the drone gets closer to no power?
    static final double POWERWEIGHT = 0.35;
    static final double COINSWEIGHT = 1 - POWERWEIGHT;
    private DroneType droneType;

    private Feature statefulTarget;

    Game(long seed, FeatureCollection mapStations, Position initialDronePos, String droneType, String year, String month, String day) throws FileNotFoundException, UnsupportedEncodingException
    {
		if(!droneType.equals("stateless") && !droneType.equals("stateful"))
		    throw new java.lang.IllegalArgumentException(String.format("Unrecognized drone type %s", droneType));
			
		if(!initialDronePos.inPlayArea())
		    throw new java.lang.IllegalArgumentException("Initial drone position not in play area");

        precomputeMovementShift();
		dirGenerator = new Random(seed);
		moveChoiceWriter = new PrintWriter(String.format("%s-%s-%s-%s.txt", droneType, day, month, year), "UTF-8");
		mapPathWriter = new PrintWriter(String.format("%s-%s-%s-%s.geojson", droneType, day, month, year), "UTF-8");
		gameMap = new GameMap(initialDronePos, mapStations.features());
		drone = new Drone(250, 0, initialDronePos);
        statefulTarget = findClosestTarget(initialDronePos);
        this.droneType = droneType.equals("stateless") ? DroneType.Stateless : DroneType.Stateful;
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
    		Direction choice = chooseMoveDirection(formerPos);
    		drone.move(choice);

    		moveChoiceWriter.println(String.format("%s,%s,%s,%s,%s,%s,%s", formerPos.latitude, formerPos.longitude, choice.toString(), drone.getPosition().latitude, drone.getPosition().longitude, drone.getCoins(), drone.getPower()));
    	    gameMap.addPositionToPath(drone.getPosition());

    	    if(drone.isStuck())
    	        statefulTarget = findRandomTarget();
    	}

		mapPathWriter.println(gameMap.createOutputMap().toJson());

        moveChoiceWriter.close();
        mapPathWriter.close();
    }

    private Feature findRandomTarget() {
        int stationsNo = gameMap.getAllStations().size();
        return gameMap.getAllStations().get(dirGenerator.nextInt(stationsNo));
    }

    private Feature findClosestTarget(Position currentPos) {
        if(gameMap.getPositiveUncollectedStations().isEmpty())
            return findRandomTarget(); // with no positive uncollected stations left, game is over and target does not matter

        return Collections.max(gameMap.getPositiveUncollectedStations(), Comparator.comparingDouble(a -> gameMap.proximityBetweenPoints(gameMap.getStationPosition(a), currentPos)));
    }

    private Direction safeDirection(EnumMap<Direction, Double> safeDirectionsStateful) {
        double proximity = Integer.MIN_VALUE;
        Direction closestDirection = null;
        for(Map.Entry<Direction, Double> safeDir : safeDirectionsStateful.entrySet())
            if(safeDir.getValue()>proximity) {
                proximity = safeDir.getValue();
                closestDirection = safeDir.getKey();
            }
        return closestDirection;
    }

    private Direction safeDirection(Set<Direction> safeDirectionsStateless) {
        Direction choice = Direction.randomDirection(dirGenerator);
        while(!drone.getPosition().nextPosition(choice).inPlayArea() || !safeDirectionsStateless.contains(choice)) {
            choice = Direction.randomDirection(dirGenerator);
        }
        return choice;
    }

    double getGameScore() {
    	return drone.getCoins();
    }

    private Direction chooseMoveDirection(Position currentPos) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Set<Feature> chosenStations = new HashSet<>();

        // TODO: maybe handle this better
        Set<Direction> safeDirectionsStateless = new HashSet<>();

        EnumMap<Direction, Double> safeDirectionsStateful = new EnumMap<>(Direction.class);
        Position targetPos = null;
        if(droneType == DroneType.Stateful) {
            if(gameMap.getCollectedStations().contains(statefulTarget))
                targetPos = gameMap.getStationPosition(findClosestTarget(currentPos));
            else
                targetPos = gameMap.getStationPosition(statefulTarget);
        }

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                Position nextPosition = currentPos.nextPosition(dir);
                double nextGain = 0;
                List<Feature> nextStations = new ArrayList<>();

                for (Feature station : gameMap.getUncollectedStations())
                    if (gameMap.arePointsInRange(nextPosition, gameMap.getStationPosition(station))) {
                        nextGain += gameMap.stationUtility(station);
                        nextStations.add(station);
                    }

                if (nextGain >= 0) {
                    if(droneType == DroneType.Stateful)
                        safeDirectionsStateful.put(dir, gameMap.proximityBetweenPoints(nextPosition, targetPos));
                    else
                        safeDirectionsStateless.add(dir);
                }

                if (nextGain > maxGain) {
                    maxGain = nextGain;
                    choice = dir;
                    chosenStations = new HashSet<>(nextStations);
                }
            }

        drone.charge(chosenStations, gameMap);

        if(maxGain!=0)
            return choice;
        else
            return droneType == DroneType.Stateful? safeDirection(safeDirectionsStateful) : safeDirection(safeDirectionsStateless);
    }

    private enum DroneType {
        Stateful,
        Stateless
    }
}