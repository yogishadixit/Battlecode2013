package team100;
//watch the hobbit
import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public class SoldierPlayer extends BasePlayer {
	MapLocation towards = enemy_location;
	int encampment_index = 0;
	int type_of_soldier = rc.getRobot().getID()%(int)(11-attack_time/40.0*1.5);//make type of soldier a function of attack time it will go from 10 to 5
	
	RobotType construction_type = RobotType.SOLDIER;
	
	boolean attack = false;
	boolean go_to_attack_place = false;
	boolean waiting = false;//checks is the player is waiting to build an encampment and doesn't have enough energy
	
	public SoldierPlayer(RobotController rc) {
		super(rc);
		//code to execute one time
	}
	
	public void run() throws GameActionException{
		int round_number = Clock.getRoundNum();
		int offset = round_number % 5;
		boolean should_broadcast = offset == 0;
		MapLocation location = rc.getLocation();
	 
		rc.setIndicatorString(0,String.format("type_of_soldier %d",type_of_soldier));
		rc.setIndicatorString(2,String.format("attack_time %d root_gather_distance %d T", attack_time, root_gather_distance));
        
		//y is this here??
	    if(should_broadcast&&(type_of_soldier == 0 || type_of_soldier == 2 || type_of_soldier == 123) && !waiting){
			
			int last_broadcast_channel = 0;
			if (should_broadcast){
				last_broadcast_channel = round_number - 5;
			}
			else{
				last_broadcast_channel = round_number - round_number%5;
			}	
			int is_waiting = rc.readBroadcast((21356+(round_number-1)*10)%GameConstants.BROADCAST_MAX_CHANNELS);//reads signal for encampment being built
			if(is_waiting == 1111){//not enough power and another soldier is trying to build and encampment
				type_of_soldier = 1;
			}
		}
		
		if (!rc.isActive() && rc.senseEncampmentSquare(location) && (type_of_soldier == 0 || type_of_soldier == 2))  {// added signal to tell how many generators are being built
			if(should_broadcast){
				rc.broadcast((22356+round_number*10+encampment_index)%GameConstants.BROADCAST_MAX_CHANNELS, 1111); //broadcasts that it found an encampment
				if(construction_type == RobotType.GENERATOR){ //tells how many generators are being built
					int generators_under_construction = rc.readBroadcast((20356+round_number*10)%GameConstants.BROADCAST_MAX_CHANNELS); //reads the number of generators
					if(generators_under_construction%100 == 42){// makes sure the signal has not been altered, 42 in digits is a marker
						rc.broadcast((20356+round_number*10)%GameConstants.BROADCAST_MAX_CHANNELS, generators_under_construction+100); //adds one to the counter
						
					}
					else{//channel disrupted; assume it is the first generator being built
						rc.broadcast((20356+round_number*10)%GameConstants.BROADCAST_MAX_CHANNELS, 142); 
					}
				}
			}
		}
		
	
		/*if(type_of_soldier == 2){//born artillery searcher
			int number_of_artillery = 0;
			Robot [] robots = rc.senseNearbyGameObjects(Robot.class, location, 10000, us);
			int number_of_robots = robots.length;
			for(int i=0; i<number_of_robots; i++){
				if(rc.senseRobotInfo(robots[i]).type==RobotType.ARTILLERY) {
					number_of_artillery++; 
				}	
			}
			if(number_of_artillery < (1 + root_distance_to_enemy/30)){
				type_of_soldier = 123; //
				int last_broadcast_channel = 0;
				if (should_broadcast){
					last_broadcast_channel = round_number - 5;
				}
				else{
					last_broadcast_channel = round_number - round_number%5;
				}
						
				int minimum_index = 0;
				int minimum_distance = 10000;
				rc.setIndicatorString(0, "AAAAA");
				for (int i=0; i<number_of_encampments; i++) { //finding closest artillery
					int distance = location.distanceSquaredTo(all_encampments[i]);
					if (distance < minimum_distance) {
						if(all_encampments[i].distanceSquaredTo(our_location) > 16 && all_encampments[i].distanceSquaredTo(enemy_location) <= 0.6 * distance_to_enemy){//make sure doesn't build encampment in front of HQ
							if((int)(Math.sqrt(location.distanceSquaredTo(focal_point_1))+Math.sqrt(location.distanceSquaredTo(focal_point_2)))<root_distance_to_enemy+5){
								int broadcast = rc.readBroadcast((22356+(last_broadcast_channel)*10+i)%GameConstants.BROADCAST_MAX_CHANNELS);
								if (broadcast != 1111) { //if encampment is free
									minimum_distance = distance;
									minimum_index=i;
								}	
							}
						}
					}
				}
							
				if (minimum_distance != 10000){
					encampment_index = minimum_index;
					towards = all_encampments[encampment_index];
					last_broadcast_channel = round_number - round_number%5;
					rc.broadcast((22356+last_broadcast_channel*10+encampment_index)%GameConstants.BROADCAST_MAX_CHANNELS,1111);//going to an encampment
				}
				
				else {
					type_of_soldier = 1;
				}	
				
				if(should_broadcast){
					last_broadcast_channel = round_number - round_number%5;
					rc.broadcast((22356+last_broadcast_channel*10+encampment_index)%GameConstants.BROADCAST_MAX_CHANNELS,1111); //make sure message doesn't get erased
				}
			}	
			
			else {
				type_of_soldier = 1;	
			}	
		}*/
		
		else {
			if(waiting && should_broadcast){
				rc.broadcast((21356+round_number*10)%GameConstants.BROADCAST_MAX_CHANNELS,1111);//broadcasts signal if waiting to build an encampment
			}
			
			if (round_number < attack_time || type_of_soldier == 0 || type_of_soldier == 2) {
				rc.setIndicatorString(1,String.format("encampment_index %d",encampment_index));
				if(location.equals(towards)){ //arrived at encampment
					if(rc.senseCaptureCost() > rc.getTeamPower()){
						if(!waiting){
							int last_broadcast_channel = 0;
							if (should_broadcast){
								last_broadcast_channel = round_number - 5;
							}
							else{
								last_broadcast_channel = round_number - offset;
							}
							rc.broadcast((21356+last_broadcast_channel*10)%GameConstants.BROADCAST_MAX_CHANNELS, 1111);//broadcast it is waiting
							waiting = true;
						}
					}
					
					else{
						//checks for the the number of generators
						int number_of_generators = 0;
						int number_of_suppliers = 0;
						int number_of_artillery = 0;
						Robot [] robots=rc.senseNearbyGameObjects(Robot.class, location, 10000, us);
						int number_of_robots = robots.length;
						for(int i=0; i<number_of_robots; i++){
							RobotType type = rc.senseRobotInfo(robots[i]).type;
							if(type == RobotType.GENERATOR) {
								number_of_generators++;
							}
							else if(type == RobotType.SUPPLIER) {
								number_of_suppliers++;
							}
							else if(type == RobotType.ARTILLERY) {
								number_of_artillery++;
							}
						}
			
						int last_broadcast_channel = round_number - offset;
						int generators_under_construction = rc.readBroadcast((20356+last_broadcast_channel*10)%GameConstants.BROADCAST_MAX_CHANNELS);//reads the number of generators being built
						if(generators_under_construction%100 == 42){
							number_of_generators += generators_under_construction/100;//adds the number generators being built to total number of generators
						}
					
						rc.setIndicatorString(1,String.format("encampment_index %d, generators_under_construction %d, broadcastchannel %d",encampment_index,generators_under_construction,20356+last_broadcast_channel*10));
						
					    
					    
					    //checks to see if we have enough energy
						if (number_of_robots*1.3 >= number_of_generators*10+40){//to be changed
							construction_type = RobotType.GENERATOR;
							if(generators_under_construction%100 == 42){
								rc.broadcast((20356+last_broadcast_channel*10)%GameConstants.BROADCAST_MAX_CHANNELS,generators_under_construction+100); //adds one to the counter	
							}
							else{//channel disrupted; assume it is the first generator being built
								rc.broadcast((20356+last_broadcast_channel*10)%GameConstants.BROADCAST_MAX_CHANNELS,142);
							}
							rc.captureEncampment(RobotType.GENERATOR);
							
						}
						else {
							if(type_of_soldier == 2 && round_number >= attack_time * 0.3){ // 
								if(number_of_artillery < (1 + root_distance_to_enemy/30)){
									rc.captureEncampment(RobotType.ARTILLERY);
									return;
								}
							}
							else {
								rc.captureEncampment(RobotType.SUPPLIER);
							}
						}
					}
				}
				else {
					int last_broadcast_channel = 0;
					if (should_broadcast){
						last_broadcast_channel = round_number - 5;
					}
					else{
						last_broadcast_channel = round_number - offset;
					}
					
					if (towards == enemy_location){
						int minimum_index = 0;
						int minimum_distance = 10000;
						
						int minimum_artillery_index = 0;
						int minimum_artillery_distance = 10000;
		
						for (int i=0; i<number_of_encampments; i++) {
							int distance = location.distanceSquaredTo(all_encampments[i]);
							
							if (type_of_soldier == 2) {//find artillery
								if (distance < minimum_artillery_distance) {
							    	if(all_encampments[i].distanceSquaredTo(our_location) > 16 && all_encampments[i].distanceSquaredTo(enemy_location) <= 0.6 * distance_to_enemy){//make sure doesn't build encampment in front of HQ
							    		if((int)(Math.sqrt(location.distanceSquaredTo(focal_point_1))+Math.sqrt(location.distanceSquaredTo(focal_point_2)))<root_distance_to_enemy+5){
							    			int broadcast = rc.readBroadcast((22356+(last_broadcast_channel)*10+i)%GameConstants.BROADCAST_MAX_CHANNELS);
							    			if (broadcast != 1111) { //if encampment is free
							    				minimum_artillery_distance = distance;
							    				minimum_artillery_index=i;
							    			}	
							    		}	
							    	}
							    }
							}
							else {
								if (distance < minimum_distance) {
							    	if(!(all_encampments[i].distanceSquaredTo(our_location)<=16&&our_location.directionTo(all_encampments[i])==direction_to_enemy)){//makes sure encampment doesn't block HQ
										int broadcast = rc.readBroadcast((22356+(last_broadcast_channel)*10+i)%GameConstants.BROADCAST_MAX_CHANNELS);
										if (broadcast != 1111) { //if encampment is free
											minimum_distance = distance;
											minimum_index=i;
										}	
									}
								}	
							}
						}
						if (minimum_distance != 10000 || minimum_artillery_index != 10000) {
							if (type_of_soldier == 2) {
								encampment_index = minimum_artillery_index;
							}
							else {
								encampment_index = minimum_index;
							}
							towards = all_encampments[encampment_index];
							last_broadcast_channel = round_number - offset;
							rc.broadcast((22356+last_broadcast_channel*10+encampment_index)%GameConstants.BROADCAST_MAX_CHANNELS,1111);
						}
						
						else {
							type_of_soldier = 1;
							moveTowards(gather, rc);
							return;
						}
					}
					
					if(should_broadcast){
						last_broadcast_channel = round_number - offset;
						rc.broadcast((22356+last_broadcast_channel*10+encampment_index)%GameConstants.BROADCAST_MAX_CHANNELS,1111); //make sure message doesn't get erased
					}
					
					if(!(location.isAdjacentTo(towards) && !rc.canMove(location.directionTo(towards)))){//checks if encampment is owned by enemy
						moveTowards(towards, rc);
					}	   
					
				}
			}
			else {
				int current_distance_to_HQ = location.distanceSquaredTo(our_location);
				if (current_distance_to_HQ <= (root_gather_distance+6)*(root_gather_distance+6) && !go_to_attack_place) {//
					int near_enemy_strength = rc.senseNearbyGameObjects(Robot.class, our_location, 7, enemy).length;
					if (near_enemy_strength != 0) {
						moveTowards(our_location, rc);
						return;
					}
					
					if (rc.canSenseSquare(attackpoint) || rc.canSenseSquare(enemy_location)) {
						moveTowards(attackpoint, rc);
					}
					
					else {
						int gather_strength = rc.senseNearbyGameObjects(Robot.class, gather, 7, us).length;
						if (rc.senseMine(location) == null) {
							boolean has_upgrade = rc.hasUpgrade(Upgrade.PICKAXE);
							
							
							//int gather_enemy_strength = rc.senseNearbyGameObjects(Robot.class, location, 3, enemy).length;
							//int gather_near_strength = rc.senseNearbyGameObjects(Robot.class, location, 3, us).length;
							//rc.setIndicatorString(1,String.format("gather_strength %d gather_enemy_strenght %d gather_near_strength %d gather_number %d",gather_strength,gather_enemy_strength, gather_near_strength, gather_number));
							rc.setIndicatorString(1, String.format("go_to_attack", go_to_attack_place));
							if (gather_strength <= gather_number/3) {
								if (goodPlace(location, has_upgrade)) {//checks to see if it has upgrade
									rc.layMine();
									return;
								}
							}
						}
						
						if (gather_strength >= gather_number) {
							moveTowards(attackpoint, rc);
							go_to_attack_place=true;
							return;
						}
						else {
							moveTowards(gather, rc);
							return;
						}
					}
				}
				else {
					int our_strength = rc.senseNearbyGameObjects(Robot.class, attackpoint, 7, us).length;
					int enemy_strength = rc.senseNearbyGameObjects(Robot.class, location, 3, enemy).length;
					int near_strength = rc.senseNearbyGameObjects(Robot.class, location, 3, us).length;
					rc.setIndicatorString(1,String.format("enemy_strength %d near_strength %d our_strength %d gather_number %d",enemy_strength,near_strength, our_strength, gather_number));
						
					
						int last_broadcast_channel; //a little buggy?
						if (should_broadcast){
							last_broadcast_channel = round_number - 5;
						}
						else{
							last_broadcast_channel = round_number - offset;							
						}
						
						int should_attack = rc.readBroadcast((19356+last_broadcast_channel*10)%GameConstants.BROADCAST_MAX_CHANNELS);//erases the past signal for encampments being built
						if (should_attack == 1111){
							attack = true;
						}
						
						if(!attack){
							
							if (our_strength >= gather_number) {
								rc.broadcast((19356+last_broadcast_channel*10)%GameConstants.BROADCAST_MAX_CHANNELS, 1111);//erases the past signal for encampments being built????
								attack = true;
								
							}
							else {
								if (near_strength > enemy_strength || enemy_strength == 0) {
									moveTowards(attackpoint, rc);
								}
								else {
									moveTowards(gather, rc);
								}
							}
						}
				
							
						if (attack) {//&& near_strength > enemy_strength
							if (near_strength > enemy_strength) {
								moveTowards(enemy_location, rc);
							}
							else {
								moveTowards(attackpoint, rc);
							}
						}
						
	    			//}
				}	
			}
		}
	}
	
	
    

	boolean goodPlace(MapLocation location, boolean has_research_pickaxe) {//makes robot lay mine on chekcerboard form
		if(has_research_pickaxe)
			return ((3*location.x+location.y)%8==0);//pickaxe with gaps		
		else
			return ((location.x+location.y)%2==0);//checkerboard
	}
	
	boolean yesMove(Direction direction, MapLocation location, RobotController rc) throws GameActionException {
    	return rc.canMove(direction) && (rc.senseMine(location)!=enemy && rc.senseMine(location)!=Team.NEUTRAL);
    }
	
    boolean yesDefuse(MapLocation location, RobotController rc) throws GameActionException {
    	if (concentration_of_mine > 0.4 && Clock.getRoundNum() < attack_time*5 && root_distance_to_enemy > 20) {
    		return rc.senseMine(location)==enemy;
    	}
    	else {
    		return rc.senseMine(location)==enemy || rc.senseMine(location)==Team.NEUTRAL;
    	}
    }
    
    void moveTowards(MapLocation whereToGo, RobotController rc) throws GameActionException{
		 Direction center = rc.getLocation().directionTo(whereToGo);
		 MapLocation myLocation=rc.getLocation();
		 MapLocation center_location = myLocation.add(center);
		 
		 if (yesMove(center, center_location, rc)) {
			 rc.move(center);
			 return;
		 }
		 else { //can't move to center
			 Direction left = center.rotateLeft();
			 MapLocation left_location = myLocation.add(left);
			 int left_distance = left_location.distanceSquaredTo(enemy_location);
			 
			 Direction right = center.rotateRight();
			 MapLocation right_location = myLocation.add(right);
			 int right_distance = right_location.distanceSquaredTo(enemy_location);
			 
			 if (left_distance < right_distance){ 
				 if (yesMove(left, left_location, rc)){
					 rc.move(left);
					 return;
				 }
				 else if (yesMove(right, right_location, rc)){
					 rc.move(right);
					 return;
				 }
			 }
			 else {
				 if (yesMove(right, right_location, rc)){
					 rc.move(right);
					 return;
				 }
				 else if (yesMove(left, left_location, rc)) {
					 rc.move(left);
					 return;				 
				 }
			 }
			 //can't move to center/left/right
			 if (yesDefuse(center_location, rc)){		
				 rc.defuseMine(center_location);
				 return;
			 }
			   
			 else if ((left_distance < right_distance)){	 
				 if (yesDefuse(left_location, rc)){		
					 rc.defuseMine(left_location);
					 return;
				 }
				 else if (yesDefuse(right_location, rc)){		
					 rc.defuseMine(right_location);
					 return;
				 }
			 }
			 
			 else{
				 if (yesDefuse(right_location, rc)){		
					 rc.defuseMine(right_location);
					 	return;
				 }
				 else if (yesDefuse(left_location, rc)) {
					 rc.defuseMine(left_location);
					 return;					 
				 }
			 }
			 //can't defuse center/left/right
			 Direction leftleft = center.rotateLeft().rotateLeft();
			 MapLocation leftleft_location = myLocation.add(leftleft);
			 int leftleft_distance = leftleft_location.distanceSquaredTo(enemy_location);
					 
			 Direction rightright = center.rotateRight().rotateRight();
			 MapLocation rightright_location = myLocation.add(rightright);
			 int rightright_distance = rightright_location.distanceSquaredTo(enemy_location);
				 
			 if (leftleft_distance < rightright_distance){
				 if (yesMove(leftleft, leftleft_location, rc)) {
					 rc.move(leftleft);
					 return;
				 }
				 else if (yesMove(rightright, rightright_location, rc)) {
					 rc.move(rightright);
					 return;						 
				 }
			 }
			 else {
				 if (yesMove(rightright, rightright_location, rc)) {
					 rc.move(rightright);
					 return;						 
				 }
				 else if (yesMove(leftleft, leftleft_location, rc)) {
					 rc.move(leftleft);
					 return;						 
				 }
			 }
			 //can't move leftleft/rightright
			 if (leftleft_distance < rightright_distance){
				 if (yesDefuse(leftleft_location, rc)) {
					 rc.defuseMine(leftleft_location);
					 return;
				 }
				 else if (yesDefuse(rightright_location, rc)) {
					 rc.defuseMine(rightright_location);
					 return;							
				 }		 
			 }
			 else {
				 if (yesDefuse(rightright_location, rc)) {
					 rc.defuseMine(rightright_location);
					 return;
				 }
				 else if (yesDefuse(leftleft_location, rc)) {
					 rc.defuseMine(leftleft_location);
					 return;
				 }
			 }
			 //can't defuse leftleft/rightright
			 if (rc.senseMine(center_location)==Team.NEUTRAL) {
				 rc.defuseMine(center_location);
				 return;
			 }
			 else if (rc.senseMine(leftleft_location)==Team.NEUTRAL) {
				 rc.defuseMine(leftleft_location);
				 return;
			 }
			 else if (rc.senseMine(rightright_location)==Team.NEUTRAL) {
				 rc.defuseMine(rightright_location);
				 return;
			 }
		}
    }
}
	


