package team100;
//adjsaokjfsaedfsd

import battlecode.common.*;
import battlecode.common.RobotController;

public abstract class BasePlayer {
	RobotController rc;
	Team enemy;
    Team us;
	MapLocation enemy_location;
	MapLocation our_location;
	MapLocation midpoint;
	MapLocation quaterpoint;
	MapLocation gather;
	//MapLocation thirdpoint;
	MapLocation enemyquaterpoint;
	MapLocation attackpoint;
	
	MapLocation focal_point_1;
	MapLocation focal_point_2;
	
	int focal_distance;
	int midpoint_distance;

	
	Direction direction_to_enemy;
	
	static MapLocation [] all_encampments;

	
	// list of the areas that radius below 10 have, the index of the list is the radius
	int []areas = {1,5,9,9,13,21,21,21,25,29,37};
	
	int distance_to_enemy;
	int root_gather_distance;
	int root_distance_to_enemy;
	
	int middle_mines;
	int side_mines;
	int mines;
	double concentration_of_mine;
	
	int number_of_encampments;
	
	int gather_number;
	int attack_time; 
	
	public BasePlayer(RobotController rc) {
		this.rc = rc;
		enemy_location = rc.senseEnemyHQLocation();
		all_encampments = rc.senseAllEncampmentSquares();
		number_of_encampments = all_encampments.length;
		our_location = rc.senseHQLocation();
		enemy = rc.getTeam().opponent();
		us = rc.getTeam();
		distance_to_enemy = our_location.distanceSquaredTo(enemy_location);
		root_distance_to_enemy = (int)Math.sqrt(distance_to_enemy);
		direction_to_enemy = our_location.directionTo(enemy_location);
		
		midpoint = new MapLocation((our_location.x+enemy_location.x)/2, (our_location.y+enemy_location.y)/2);
		quaterpoint = new MapLocation((our_location.x+midpoint.x)/2, (our_location.y+midpoint.y)/2);
		enemyquaterpoint = new MapLocation((enemy_location.x+midpoint.x)/2, (enemy_location.y+midpoint.y)/2);
		
		midpoint_distance=(int)Math.sqrt(our_location.distanceSquaredTo(midpoint));//distance to midpoint
		focal_distance=(int)(midpoint_distance*0.5);
		
		
		focal_point_1=new MapLocation(midpoint.x+(midpoint.x-our_location.x)/midpoint_distance*focal_distance,midpoint.y+(midpoint.y-our_location.y)/midpoint_distance*focal_distance);
		
		focal_point_2=new MapLocation(midpoint.x+(midpoint.x-enemy_location.x)/midpoint_distance*focal_distance,midpoint.y+(midpoint.y-enemy_location.y)/midpoint_distance*focal_distance);

		
		
		
		
		//thirdpoint = new MapLocation(our_location.x+(enemy_location.x-our_location.x)/3, our_location.y+(enemy_location.y-our_location.y)/3);
		middle_mines = (rc.senseMineLocations(midpoint, distance_to_enemy/64, Team.NEUTRAL).length);
		side_mines = (rc.senseMineLocations(quaterpoint, distance_to_enemy/64, Team.NEUTRAL).length);
		mines = middle_mines + 2*side_mines;  
		concentration_of_mine = mines/(3.0*radius_to_area(distance_to_enemy/64));
		
		
        
        gather_number = 5 + (int)((Math.sqrt(root_distance_to_enemy))*(concentration_of_mine+0.4)*1.5); // preferrably between 5 and 15 root_distance_to_enemy/4 + side_mines/3;//
        attack_time = attack_time_fuction(); 
        gather = new MapLocation((our_location.x+quaterpoint.x)/2, (our_location.y+quaterpoint.y)/2);
        attackpoint = attack_place(rc);//midpoint;
        root_gather_distance = (int) Math.sqrt(our_location.distanceSquaredTo(gather));
	}
	public abstract void run() throws GameActionException;
	
	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// End turn
			rc.yield();
		}
	}

	MapLocation attack_place(RobotController rc){
		Direction middle_direction = enemy_location.directionTo(our_location);
		MapLocation top_middle = enemyquaterpoint.add(middle_direction, root_distance_to_enemy/6);
		MapLocation top_left = enemyquaterpoint.add(middle_direction.rotateLeft(), root_distance_to_enemy/6);
		MapLocation top_right = enemyquaterpoint.add(middle_direction.rotateRight(), root_distance_to_enemy/6);
		MapLocation middle_left = enemyquaterpoint.add(middle_direction.rotateLeft().rotateLeft(), root_distance_to_enemy/6);
		MapLocation middle_right = enemyquaterpoint.add(middle_direction.rotateRight().rotateRight(), root_distance_to_enemy/6);
		MapLocation[] locations= {enemyquaterpoint, top_middle, top_left, top_right, middle_left, middle_right};
		int minimum_index = 0;
		int minimum_mines = 10000;
		for (int i=0; i<6; i++) {
			int mines = rc.senseMineLocations(locations[i], distance_to_enemy/64, Team.NEUTRAL).length;
			if (mines < minimum_mines) {
				minimum_mines = mines;
				minimum_index = i;
			}
		}
		/*if (attack_time < 50 || concentration_of_mine < 0.2) {
			return midpoint;
		}*/
		return locations[minimum_index];
	}
	int radius_to_area(int radius_squared){
		//transforms radius to an area
		if (radius_squared < 11){
			return areas[radius_squared]; // below 10 the circle formula is not very exact
		}
		else{
			return (int)(radius_squared*3.14);// after 1 the circle formula becomes more exact
		}
	}
	
	int attack_time_fuction(){
		/*double mines_variable = Math.pow((concentration_of_mine+0.3), 0.7); //to be improved
		if(mines_variable > 1) {
			mines_variable = 1;
		}
		
		double variable_time = ((root_distance_to_enemy-5)/95.0) * mines_variable * Math.sqrt(Math.log10(number_of_encampments)/Math.log10(5)); //to be improved
		if (variable_time > 1) {
			variable_time = 1;
		}
	*/
		int is_enemy_super_close = 0;
		for(int i=0; i<number_of_encampments; i++){
			if(our_location.distanceSquaredTo(all_encampments[i]) < distance_to_enemy){
				is_enemy_super_close++;
			}
		}
		
		if(is_enemy_super_close < 3) {
			return 0;
		}
		
		return root_distance_to_enemy/2;
	}
}



