package team100;
//rofl
import battlecode.common.*;

public class HQPlayer extends BasePlayer {

	//declare local variables
	
	boolean encampment_energy_deficit = false;
	int check_encampment_energy_deficit = 0;
	
	public HQPlayer(RobotController rc) {
		super(rc);
		//code to execute one time
	}
	
	public void spawnSoldier(RobotController rc) throws GameActionException {
		
		Direction direction = Direction.values()[(int)(Math.random()*8)];
		for (int i = 1; i <= 8; i++)
		{
			// no need for research nuke here because the decision whether to research nuke
			// or not shouldn't be made here. it should be made in run method
			if (rc.canMove(direction))
				rc.spawn(direction);
			direction = direction.rotateLeft();
		}
	}
	
	public void run() throws GameActionException {
		//code to execute for the whole match        
		
		int round_number = Clock.getRoundNum();
		
		if (round_number > 200 && rc.checkResearchProgress(Upgrade.NUKE) < 100 && rc.senseEnemyNukeHalfDone()) {
			spawnSoldier(rc);
		}
		
		Robot [] robots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());
		int our_robots = robots.length;
		int enemy_robots = rc.senseNearbyGameObjects(Robot.class, our_location, distance_to_enemy/3, rc.getTeam().opponent()).length;			

		int number_of_generators = 0; //y dont read broadcast??
		for(int i=0; i<our_robots; i++){
		if(rc.senseRobotInfo(robots[i]).type==RobotType.GENERATOR) {
			number_of_generators++;
			}	
		}
		
		if(round_number >= (attack_time + gather_number*10)+400 && enemy_robots == 0 && !rc.canSenseSquare(enemy_location)){//to be improve
			if (round_number % (root_distance_to_enemy/5) != 0) {
				rc.researchUpgrade(Upgrade.NUKE);
			}
		}

		if(round_number%5 == 0){
		    check_encampment_energy_deficit = rc.readBroadcast((21356+(round_number-5)*10)%GameConstants.BROADCAST_MAX_CHANNELS);//checks if an encampment is been build but there is not enough energy
			if(check_encampment_energy_deficit == 1111){
				encampment_energy_deficit = true;
			}
			else{
				encampment_energy_deficit = false;
			}
		}
		
		rc.setIndicatorString(1,String.format("root_distance_to_enemy %d",check_encampment_energy_deficit));
		//rc.setIndicatorString(1,String.format("concetration %d encampment_number %d attack_time %d", concentration_of_mine, number_of_encampments,	attack_time));
		
		if (enemy_robots <= 0 && (!rc.canSenseSquare(enemy_location)) && (attack_time> 60 &&(round_number%5!=0)&&round_number<40)||our_robots*1.3 > number_of_generators*10+40||encampment_energy_deficit) { //if no enemy around and we're not close to the enemy and board is big, also if every 5 turns a soldier is spawn
			if(! rc.hasUpgrade(Upgrade.PICKAXE) && root_distance_to_enemy > 30){//research pickaxe on a big map
				rc.researchUpgrade(Upgrade.PICKAXE);
			}
			else{
				if (our_robots*1.3 > number_of_generators*10+40||encampment_energy_deficit) { //to be improved
					rc.researchUpgrade(Upgrade.NUKE);
				}
				else {
					spawnSoldier(rc);
				}
			}
		}
		else {
			if (our_robots*1.3 > number_of_generators*10+40||encampment_energy_deficit) {
				rc.researchUpgrade(Upgrade.NUKE);
			}
			else {
				spawnSoldier(rc);
			}
		}
		if(round_number%5==0 || round_number==1){ 
			for(int i=0; i<all_encampments.length; i++){
				rc.broadcast((22356+round_number*10+i)%GameConstants.BROADCAST_MAX_CHANNELS,2222); 
			}
			rc.broadcast((20356+round_number*10)%GameConstants.BROADCAST_MAX_CHANNELS, 42);//erases the past signal for generators being built
			rc.broadcast((21356+round_number*10)%GameConstants.BROADCAST_MAX_CHANNELS, 2222);//erases the past signal for encampments being built
			rc.broadcast((19356+round_number*10)%GameConstants.BROADCAST_MAX_CHANNELS, 2222);//erases the past signal for attack time

		}	
	}
}



