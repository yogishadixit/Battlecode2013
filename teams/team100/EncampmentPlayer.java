package team100;
//new
import battlecode.common.*;

public class EncampmentPlayer extends BasePlayer {
	int encampment_index = -1;
	public EncampmentPlayer(RobotController rc) {
		super(rc);
	}
	public void run() throws GameActionException {
		int round_number = Clock.getRoundNum();
		MapLocation location = rc.getLocation();
		if(encampment_index == -1){
			for(int i=0; i<number_of_encampments; i++){
				if(location.equals(all_encampments[i])){
					encampment_index = i;
					break;
				}
			}
			rc.broadcast((22356+round_number*10+encampment_index)%GameConstants.BROADCAST_MAX_CHANNELS,1111);
		}
		if(rc.isActive()){
			
			
		
		    if (rc.getType() == RobotType.ARTILLERY) {
		    	rc.setIndicatorString(0, String.format("focal 1(%d,%d), focal 2(%d,%d)",focal_point_1.x,focal_point_1.y,focal_point_2.x,focal_point_2.y));
		    	rc.setIndicatorString(1, String.format("focal distance %d, distance_to_enemy %d",(int)(Math.sqrt(location.distanceSquaredTo(focal_point_1))+Math.sqrt(location.distanceSquaredTo(focal_point_2))),root_distance_to_enemy));
		    	rc.setIndicatorString(2,"AAAAA");
		    	
		    	Robot [] enemy_robots_robot = rc.senseNearbyGameObjects(Robot.class, location, 63, enemy);
		    	int size=enemy_robots_robot.length;
		    	
		    	MapLocation [] enemy_robots_location= new MapLocation[size];
		    	
		    	for (int i=0;i<size; i++){
		    		enemy_robots_location[i]=rc.senseLocationOf(enemy_robots_robot[i]);
		    	}
		    	
		    	int minimun_distance=100000;
		    	MapLocation closet_enemy_location=enemy_location;
		    	for (int i=0;i<size; i++) {
		    			MapLocation enemy_robot_location=enemy_robots_location[i];
		    			int enemy_distance=location.distanceSquaredTo(enemy_robot_location);
		    			if(enemy_distance<64&&enemy_distance<minimun_distance){
		    				if(is_safe_to_attack(enemy_robot_location))
		    					minimun_distance=enemy_distance;
		    					closet_enemy_location=enemy_robot_location;
		    			}
		    	}
		    	if(minimun_distance!=100000){
		    		rc.attackSquare(closet_enemy_location);
		    	}
		    	else{
		    		int max_attack_damage=0;
			    	MapLocation enemy_max_damage=enemy_location;
		    		for (int i=0;i<size; i++) {
		    			MapLocation enemy_robot_location=enemy_robots_location[i];
		    			int enemy_damage=splash_damage(enemy_robot_location);
		    			if(enemy_damage>max_attack_damage){
		    				if(is_safe_to_attack(enemy_robot_location))
		    					max_attack_damage=enemy_damage;
		    					enemy_max_damage=enemy_robot_location;
		    			}
		    		}
		    		if(max_attack_damage!=0){
		    			rc.attackSquare(enemy_max_damage);
		    		}
		    		
		    	}
		    	
		    	
		    }
		
		}
		if(round_number%5==0){
			rc.broadcast((22356+round_number*10+encampment_index)%GameConstants.BROADCAST_MAX_CHANNELS,1111);
		}
	}
	
	boolean is_safe_to_attack(MapLocation attack_square){
		if(rc.senseNearbyGameObjects(Robot.class, attack_square, 2, us).length!=0){
			return true;
		}else{
			return false;
		}
	}
	
	int splash_damage(MapLocation attack_square){
		return rc.senseNearbyGameObjects(Robot.class, attack_square, 2, enemy).length;
	}
	
}





