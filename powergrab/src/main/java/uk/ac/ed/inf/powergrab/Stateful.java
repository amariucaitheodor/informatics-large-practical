package uk.ac.ed.inf.powergrab;

import java.util.*;

class Stateful extends Drone {
    // 'trace' variable keeps track of the drone's last 5 positions
    private Queue<Position> trace;
    // 'target' variable keeps track of the drone's desired station to be reached next
    private Station target;

    private Stateful(Position position, long seed, Map map) {
        movesLeft = 250;
        trace = new LinkedList<>();
        trace.add(position);
        this.power = 250;
        this.coins = 0;
        this.position = position;
        this.dirGenerator = new Random(seed);
        this.target = closestPositiveUncollectedStation(position, map);
    }

    static Drone createInstance(Position position, long seed, boolean submissionGeneration, Map map) {
        if (instance == null || submissionGeneration)
            instance = new Stateful(position, seed, map);
        return instance;
    }

    @Override
    void move(Direction dir) {
        super.move(dir);
        if (trace.size() == 5)
            trace.poll();
        trace.add(position);
    }

    private Station randomStation(Map map) {
        int stationsNo = map.getAllStations().size();
        return map.getAllStations().get(dirGenerator.nextInt(stationsNo));
    }

    // if drone has been to the same position 3 times in its last 5 moves, then it's stuck and needs a new target
    private boolean isStuck() {
        HashMap<String, Integer> occurrences = new HashMap<>();
        trace.forEach(pos ->
                occurrences.put(pos.toString(), occurrences.getOrDefault(pos.toString(), 0) + 1));

        for (int occurrence : occurrences.values())
            if (occurrence == 3)
                // 3 occurrences of the same position have been found in the last 5 moves
                return true;

        return false;
    }

    private Station closestPositiveUncollectedStation(Position currentPos, Map map) {
        return Collections.min(map.getPositiveUncollectedStations(),
                Comparator.comparingDouble(a -> map.distanceBetweenPoints(a.getPosition(), currentPos)));
    }

    private Position newTargetPosition(Position currentPos, Map map) {
        // with no positive uncollected stations left, game is over and target does not matter
        if (map.getPositiveUncollectedStations().isEmpty())
            return map.getAllStations().get(0).getPosition();

        if (isStuck())
            target = randomStation(map);

        if (map.getCollectedStations().contains(target))
            // target has just been collected, select a new one
            return closestPositiveUncollectedStation(currentPos, map).getPosition();
        else
            return target.getPosition();
    }

    private Direction closestSafeDirection(EnumMap<Direction, DirectionOption> safeDirectionsStateful,
                                           boolean idealDirections) {
        double distance = Integer.MAX_VALUE;
        Direction closestDirection = null;
        // go through all safe directions and choose the closest one based on distance to target (stored in HashMap)
        // if 'idealDirections' flag is true, then only consider such directions
        for (java.util.Map.Entry<Direction, DirectionOption> safeDir : safeDirectionsStateful.entrySet())
            if (safeDir.getValue().distance < distance && (!idealDirections || safeDir.getValue().isIdeal)) {
                distance = safeDir.getValue().distance;
                closestDirection = safeDir.getKey();
            }
        return closestDirection;
    }

    Direction chooseMoveDirection(Position currentPos, Map map) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Set<Station> chosenStations = new HashSet<>();

        EnumMap<Direction, DirectionOption> safeDirectionsStateful = new EnumMap<>(Direction.class);
        Position targetPos = newTargetPosition(currentPos, map);

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                Position nextPosition = currentPos.nextPosition(dir);
                List<Station> nextStations = new ArrayList<>();

                // calculate utility for this direction, and take note if any negative stations are charged from
                double directionUtilityGain = 0;
                boolean idealDirection = true;
                List<Station> uncollectedStations = map.getUncollectedStations();
                for (Station station : uncollectedStations)
                    if (map.arePointsInRange(nextPosition, station.getPosition())) {
                        directionUtilityGain += map.stationUtility(station);
                        if (map.stationUtility(station) < 0)
                            idealDirection = false;
                        nextStations.add(station);
                    }

                // any direction where we gain coins is a safe direction
                if (directionUtilityGain >= 0)
                    safeDirectionsStateful.put(dir,
                            new DirectionOption(map.distanceBetweenPoints(nextPosition, targetPos), idealDirection));

                // keep track of maximum coin profit
                if (directionUtilityGain > maxGain) {
                    maxGain = directionUtilityGain;
                    choice = dir;
                    chosenStations = new HashSet<>(nextStations);
                }
            }

        // all directions are neutral
        if (maxGain == 0)
            return closestSafeDirection(safeDirectionsStateful, false);

        // no safe directions available, pick the best direction among the worst
        if (safeDirectionsStateful.isEmpty()) {
            charge(chosenStations, map);
            return choice;
        }

        // direction bringing maximum profit also does not charge from any negative stations, perfect!
        if (safeDirectionsStateful.get(choice).isIdeal) {
            charge(chosenStations, map);
            return choice;
        }

        // check to see if we can avoid charging from negative stations at all while getting closer to target
        Direction closestSafeIdealDir = closestSafeDirection(safeDirectionsStateful, true);
        if (closestSafeIdealDir != null &&
                map.distanceBetweenPoints(currentPos.nextPosition(closestSafeIdealDir), targetPos) <
                        map.distanceBetweenPoints(currentPos, targetPos))
            return closestSafeIdealDir;

        // choice not ideal (charges from negative stations), but we have not found a better option
        charge(chosenStations, map);
        return choice;
    }

    // wraps together, for the HashMap, the distance to target and whether direction is ideal (no negative stations)
    static class DirectionOption {
        private double distance;
        private boolean isIdeal;

        DirectionOption(double distance, boolean ideal) {
            this.distance = distance;
            this.isIdeal = ideal;
        }
    }
}
