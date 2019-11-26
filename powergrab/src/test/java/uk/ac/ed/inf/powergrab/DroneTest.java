package uk.ac.ed.inf.powergrab;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Unit test for PowerGrab App, Drone class.  [JUnit 4 version]
 */
public class DroneTest {
	
    @Before
    public void init() {
    	Game.precomputeMovementShift();
    }

    @Test
    public void testOnlyOneDroneInstance() {
        Drone drone1 = new Stateless(new Position(55.944425, -3.188396), 5678);
        Drone drone2 = new Stateless(new Position(55.944425, -3.188396), 5678);
        assertEquals(drone1, drone2);
        assertEquals(drone2, new Stateless(new Position(55.944425, -3.188396), 5678));
    }
    
    @Test
	public void testRunOutOfPower() {
		Random dirGenerator = new Random();
    	// Initialize drone with 50 power because this is a rare event that a drone runs out of power
    	Drone testDrone =new Stateless(new Position(55.944425, -3.188396), 5678);
    	while(testDrone.hasMovesLeft() && testDrone.hasPower())
        {
            Direction dir = Direction.randomDirection(dirGenerator);
            while(!testDrone.getPosition().nextPosition(dir).inPlayArea()) {
                dir = Direction.randomDirection(dirGenerator);
            }
            testDrone.move(dir);
        }
		assertFalse(testDrone.hasPower());
		assertTrue(testDrone.hasMovesLeft());
	}
}
