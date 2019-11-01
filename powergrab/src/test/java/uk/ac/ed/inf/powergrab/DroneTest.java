package uk.ac.ed.inf.powergrab;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import static org.junit.Assert.assertFalse;

/**
 * Unit test for PowerGrab App, Drone class.  [JUnit 4 version]
 */
public class DroneTest {
	
    @Before
    public void init() {
    	Game.precomputeMovementShift();
    }
	
    @Test
	public void testDroneConstructor() {
		assertTrue( new Drone(250, 0, new Position(55.944425, -3.188396)) != null );
	}
    
    @Test
	public void testRunOutOfPower() {
		Random dirGenerator = new Random();
    	// Initialize drone with 50 power because this is a rare event that a drone runs out of power
    	Drone testDrone = new Drone(50, 0, new Position(55.944425, -3.188396));        
    	while(testDrone.hasMoves() && testDrone.hasPower())
        {
            Direction dir = Direction.randomDirection(dirGenerator);
            while(!testDrone.getPosition().nextPosition(dir).inPlayArea()) {
                dir = Direction.randomDirection(dirGenerator);
            }
            testDrone.move(dir);
        }
		assertFalse(testDrone.hasPower());
		assertTrue(testDrone.hasMoves());
	}
}
