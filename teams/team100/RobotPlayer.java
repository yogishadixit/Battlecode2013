package team100;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) {
		BasePlayer br;
		switch(rc.getType()) {
		case HQ:
			br = new HQPlayer(rc);
			break;
		case SOLDIER:
			br = new SoldierPlayer(rc);
			break;
		default:
			br = new EncampmentPlayer(rc);
			break;
		}
		
		br.loop();
	}
}


