
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;


/**
 * Overload 상태를 관리하고 정찰 컨트롤하는 class
 * 오버워치, 서킷 브레이크는 맵이 넓어서 오버로드가 정찰 가다가,
 * 이미 만들어진 포토캐논이나, 벙커 및 공중 공격 가능 유닛에 저격을 먼저 당한다.
 * 정찰 도중  적을 발견하지 못했을때는 계속 적으로 오버로드는 보내려는 버그가 있음(수정 요망)
 * 
 * @author sc76.choi
 *
 */
public class OverloadManager {
	public static Game Broodwar;
	private int currentOverloadScoutStatus;
	
	private Unit firstScoutOverload; // 게임 시작시에, 첫번째 정찰에 투입될 오버로드
	private Unit myMainLocationOverload;
	private Unit myExpansionLocationOverload;
	private Unit myFirstChokeOverload;
	private Unit myFirstChokePatrolOverload;	
	private Unit myMainBasePatrolOverload;
	private Unit mySecondChokeOverload;
	private Unit centerChokeOverload;
	private Unit enemyFirstChokeOverload;
	private Unit enemySecondChokeOverload;
	private Unit enemyBasePatrolOverload;
//	private Unit dropOverload;
	private static ArrayList<Unit> dropOverloadList = new ArrayList<Unit>(); // Drop Overload 배열관리
	
    private BaseLocation firstScoutTargetBaseLocation = null;
    private BaseLocation secondScoutTargetBaseLocation = null;
	
	private boolean isFinishedInitialScout = false;
	
	public enum ScoutStatus {
		NoScout,						///< 정찰 유닛을 미지정한 상태
		MovingToAnotherBaseLocation,	///< 적군의 BaseLocation 이 미발견된 상태에서 정찰 유닛을 이동시키고 있는 상태
		MoveAroundEnemyBaseLocation   	///< 적군의 BaseLocation 이 발견된 상태에서 정찰 유닛을 이동시키고 있는 상태
	};
	
	// sc76.choi 본진의 patrol 오버로드를 위한 변수
	private Vector<Position> selfMainBaseRegionVertices = new Vector<Position>();
	private int currentMainBaseScoutFreeToVertexIndex = -1;
	private Position currentMainBaseScoutTargetPosition = Position.None;
	
	// sc76.choi 앞마당의 patrol 오버로드를 위한 변수	
	private Vector<Position> selfExpansionBaseRegionVertices = new Vector<Position>();
	private int currentExpansionBaseScoutFreeToVertexIndex = -1;
	private Position currentExpansionBaseScoutTargetPosition = Position.None;
	
	/// 오버로드 목록
	//private List<Unit> overloads = new ArrayList<Unit>();
	private BaseLocation currentScoutTargetBaseLocation = null; // 현재 정찰할 Target base
	/// 각 Overload 에 대한 OverloadJob 상황을 저장하는 자료구조 객체
	private OverloadData overloadData = new OverloadData();
	private CommandUtil commandUtil = new CommandUtil();

	BaseLocation selfMainBaseLocation = null;
	BaseLocation selfFirstExpansionLocation = null;
	Chokepoint selfFirstChokePoint = null;
	Chokepoint selfSecondChokePoint = null;	
	BaseLocation enemyMainBaseLocation = null;
	BaseLocation enemyFirstExpansionBaseLocation = null;
	Chokepoint enemyFirstChokePoint = null;
	Chokepoint enemySecondChokePoint = null;
	
	// static singleton 객체를 리턴합니다
	private static OverloadManager instance = new OverloadManager();
	public static OverloadManager Instance() {
		return instance;
	}
	
    public void onStart() {
        // assign a scout to go scout it
        firstScoutTargetBaseLocation = getClosestBaseLocation();

    }
	
	/// 경기가 시작되면 오버로드를 정찰합니다.
	public void update() {
		
		// 1초에 4번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) return;
				
		selfMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		selfFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
		selfFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
		selfSecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
		enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		enemyFirstExpansionBaseLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
		
		// firstScoutOverload를 지정한다.
		assignFirstScoutOverload(); 
		
		// 지정된 첫번째 오버로드를 정찰 시킨다.
		initialFirstScoutOverload(); 
		
        // 두번째 scoutUnit 을 지정하고, scoutUnit 의 이동을 컨트롤함.
        moveScoutUnit();
        
        // 본진 patrol
		setMainBasePatrolOverload(); 
		
		// 앞마당 patrol
		setMainExpansionBasePatrolOverload();
		
		// 정찰 중인 오버로드가 공격을 받으면 본진으로 귀환 시킨다.
		handleScoutOverloads(); 
        
	}
	
	/// 게임 시작시에 정찰 오버로드을 필요하면 새로 지정합니다
	public void assignFirstScoutOverload(){
		if(isFinishedInitialScout) return;
		
		// 적의 위치가 없고
		//if (enemyBaseLocation == null){
			// 정찰 유닛이 지정되어 있지 않으면
			if (firstScoutOverload == null || firstScoutOverload.exists() == false || firstScoutOverload.getHitPoints() <= 0){
				firstScoutOverload = null;
				currentOverloadScoutStatus = ScoutStatus.NoScout.ordinal();
				
				// TODO overload는 Spawning Pool 건설 필요없이 바로 정찰한다. 
				// first building (Pylon / Supply Depot / Spawning Pool) 을 건설 시작한 후, 가장 가까이에 있는 Worker 를 정찰유닛으로 지정한다
				for (Unit unit : MyBotModule.Broodwar.self().getUnits())
				{
					// 오버로드이고 정찰임무가 아닌 
					if (unit.getType() == UnitType.Zerg_Overlord && 
							overloadData.getOverloadJob(unit) != OverloadData.OverloadJob.Scout) 
					{
						firstScoutOverload = unit;
						break;
					}
				}

				if (firstScoutOverload != null){
					// set unit as scout unit
					//if (MyBotModule.Broodwar.getFrameCount() % Config.showDelayDisplayTime == 0) {
					//	if(Config.DEBUG) System.out.println("- firstOverload Type      : " + firstScoutOverload.getType());
					//	if(Config.DEBUG) System.out.println("- firstOverload ID        : " + firstScoutOverload.getID());
					//}
					// set unit as scout unit
					setScoutOverload(firstScoutOverload);
				}
			}
		//}
	}
	
	/**
	 * initialScoutOverload
	 * 경기시작하자 말자, overload 정찰을 보낸다.
	 */
	public void initialFirstScoutOverload(){
		
		// 정찰 유닛이 valid 하지 않으면 return
		if (!commandUtil.IsValidUnit(firstScoutOverload)){
			firstScoutOverload = null;
			currentOverloadScoutStatus = ScoutStatus.NoScout.ordinal();
			return;
		}
		
		// 적이 없으면 맵을 투어를 계속 한다.
		if (enemyMainBaseLocation == null){
			BaseLocation closestBaseLocation = null;
			double closestDistance = 1000000000;
			double tempDistance = 0;
			
			// sc76.choi 정찰할 Target base가 없다면 나의 메인 base를 지정한다.
			if(currentScoutTargetBaseLocation == null){
				currentScoutTargetBaseLocation = selfMainBaseLocation;
			}
			
			boolean isExploredTemp = false;
			Position tempPosition = null;
			
			// starting location별로 loop를 돌며 가장 가까운 공중 거리(getAirDistance)부터 정찰 수행한다.
			for (BaseLocation startLocation : BWTA.getStartLocations()){
				isExploredTemp = MyBotModule.Broodwar.isExplored(startLocation.getTilePosition());
				
				// if we haven't explored it yet (방문했었던 곳은 다시 가볼 필요 없음)
				if (isExploredTemp == false)
				{
					// GroundDistance 를 기준으로 가장 가까운 곳으로 선정
					tempDistance = currentScoutTargetBaseLocation.getAirDistance(startLocation) + 0.5;

					if (tempDistance > 0 && tempDistance < closestDistance) {
						closestBaseLocation = startLocation;
						closestDistance = tempDistance;
					}
				}
			}
			
			// 현재 정찰할 가장 가까운 Target base 지정
			currentScoutTargetBaseLocation = closestBaseLocation;
			
			if (currentScoutTargetBaseLocation != null && firstScoutOverload != null) {
				currentOverloadScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();
				//System.out.println("move scout overload : " + currentScoutTargetBaseLocation.getPosition());
				firstScoutTargetBaseLocation = closestBaseLocation;
				commandUtil.move(firstScoutOverload, closestBaseLocation.getPosition());
			}
			
//			System.out.println("currentScoutTargetBaseLocation 1 : " + firstScoutOverload.getID() + " " + currentScoutTargetBaseLocation.getPosition());
		}
		// 적진이 발견되었다면,
		else{
			
			if(commandUtil.IsValidUnit(firstScoutOverload) == false) return;
				
			// sc76.choi 안전한지 체크
			boolean isSafeAround = true;
			for(Unit enemyUnit : firstScoutOverload.getUnitsInRadius(Config.TILE_SIZE*7)){
				if(enemyUnit.getPlayer() == InformationManager.Instance().enemyPlayer){
					if(enemyUnit.getType() == UnitType.Terran_Marine
						|| enemyUnit.getType() == UnitType.Terran_Bunker
						|| enemyUnit.getType() == UnitType.Protoss_Dragoon
						|| enemyUnit.getType() == UnitType.Protoss_Cybernetics_Core
						|| enemyUnit.getType() == UnitType.Protoss_Photon_Cannon
						|| enemyUnit.getType() == UnitType.Protoss_Corsair
						|| enemyUnit.getType() == UnitType.Protoss_Scout
						|| enemyUnit.getType() == UnitType.Zerg_Hydralisk
						|| enemyUnit.getType() == UnitType.Zerg_Spore_Colony
						|| enemyUnit.getType() == UnitType.Zerg_Mutalisk){
						
						isSafeAround = false;
					}
				}
			}
			
			//System.out.println("initialFirstScoutOverload() isSafeAround : " + firstScoutOverload.getID() + " " + isSafeAround);
			
			// sc76.choi 발견된 적진의 거리를 구해, 앞마당까지 patrol 할수 있도록 한다.
			//double distanceFromEnemyMainBaseLocation = enemyMainBaseLocation.getDistance(firstScoutOverload.getPosition());
			double distanceFromEnemyMainBaseLocation = enemyFirstExpansionBaseLocation.getDistance(firstScoutOverload.getPosition());
			if(distanceFromEnemyMainBaseLocation <= (double)TilePosition.SIZE_IN_PIXELS*3){
				
				// sc76.choi 공격을 당하면, 본진귀환
	            if (firstScoutOverload.isUnderAttack() || isSafeAround == false) {
	            	if(commandUtil.IsValidUnit(firstScoutOverload)){
		            	overloadData.setOverloadJob(firstScoutOverload, OverloadData.OverloadJob.Idle , (Unit)null);
		                moveScoutUnitToMyBaseLocation(firstScoutOverload);
	            	}
	            }else{
	            	//commandUtil.patrol(firstScoutOverload, enemySecondChokePoint.getCenter());
	            	commandUtil.patrol(firstScoutOverload, enemyMainBaseLocation.getPosition());
	            }
	            
			}else{
				
				
				if (firstScoutOverload.isUnderAttack() || isSafeAround == false) {
	            	if(commandUtil.IsValidUnit(firstScoutOverload)){
		            	overloadData.setOverloadJob(firstScoutOverload, OverloadData.OverloadJob.Idle , (Unit)null);
		                moveScoutUnitToMyBaseLocation(firstScoutOverload);
	            	}
				}else{
					if(isFinishedInitialScout) return; // 정찰이 끝났으면 수행하지 않음,
					
					//commandUtil.move(firstScoutOverload, enemyMainBaseLocation.getPosition());
					commandUtil.move(firstScoutOverload, enemyFirstExpansionBaseLocation.getPosition());
					currentOverloadScoutStatus = ScoutStatus.NoScout.ordinal();
					isFinishedInitialScout = true; // 초반 정찰 끝
					overloadData.setOverloadJob(firstScoutOverload, OverloadData.OverloadJob.EnemyBase, (Unit)null);
				}
			}
		}
	}

	// sc76.choi 정찰 중인 오버로드가 공격 중이면, 본진으로 귀환
	public void handleScoutOverloads(){
		for (Unit overload : overloadData.getOverloads()){
			if (!commandUtil.IsValidSelfUnit(overload)) continue;

			// sc76.choi 안전한지 체크
			boolean isSafeAround = true;
			for(Unit enemyUnit : overload.getUnitsInRadius(Config.TILE_SIZE*7)){
				if(enemyUnit.getPlayer() == InformationManager.Instance().enemyPlayer){
					if(enemyUnit.getType() == UnitType.Zerg_Spire
						|| enemyUnit.getType() == UnitType.Zerg_Mutalisk){
							StrategyManager.Instance().bTimeToAirDefence = true;
						}
					
					if(enemyUnit.getType() == UnitType.Terran_Marine
						|| enemyUnit.getType() == UnitType.Terran_Bunker
						|| enemyUnit.getType() == UnitType.Protoss_Dragoon
						|| enemyUnit.getType() == UnitType.Protoss_Photon_Cannon
						|| enemyUnit.getType() == UnitType.Protoss_Corsair
						|| enemyUnit.getType() == UnitType.Protoss_Scout
						|| enemyUnit.getType() == UnitType.Zerg_Hydralisk
						|| enemyUnit.getType() == UnitType.Zerg_Spore_Colony
						|| enemyUnit.getType() == UnitType.Zerg_Mutalisk){
						isSafeAround = false;
					}
				}
			}
			
			// sc76.choi 공격 받거나, 안전하지 않으면 본진으로 귀환
			if (overloadData.getJobCode(overload) == 'S' || overloadData.getJobCode(overload) == 'I')	{
				if(overload.isUnderAttack() || isSafeAround == false){
					overloadData.setOverloadJob(overload, OverloadData.OverloadJob.Idle, (Unit)null);
					moveScoutUnitToMyBaseLocation(overload);
				}
			}
		}
	}
	
	public void handleMoveOverloads(){
		// for each of our workers
		for (Unit overload : overloadData.getOverloads())
		{
			if (!commandUtil.IsValidSelfUnit(overload)) continue;

			// if it is a move worker
			if (overloadData.getOverloadJob(overload) == OverloadData.OverloadJob.Move)	{
				OverloadMoveData data = overloadData.getOverloadMoveData(overload);

				// 목적지에 도착한 경우 이동 명령을 해제한다
				if (overload.getPosition().getDistance(data.getPosition()) < 4) {
					setIdleOverload(overload);
				}
				else {
					commandUtil.move(overload, data.getPosition());
				}
			}
		}
	}
	
	/// 해당 일꾼 정찰 unit 의 OverloadJob 값를 Idle 로 변경합니다
	public void setIdleOverload(Unit unit){
		if (unit == null) return;
		overloadData.setOverloadJob(unit, OverloadData.OverloadJob.Idle, (Unit)null);
	}
	
	// sets a worker as a scout
	public void setScoutOverload(Unit overload){
		if (overload == null) return;
		overloadData.setOverloadJob(overload, OverloadData.OverloadJob.Scout, (Unit)null);
	}
	
	// sc76.choi 사용안함.
	// sets a worker as a scout
	//public void setSDetectorOverload(Unit overload){
	//	if (overload == null) return;
	//	overloadData.setOverloadJob(overload, OverloadData.OverloadJob.Detector, (Unit)null);
	//}	

	/// target 으로부터 가장 가까운 Detector를 리턴합니다
	public Unit getClosestOverload(Position target)
	{
		Unit closestUnit = null;
		double closestDist = 1000000000;

		for (Unit unit : overloadData.getOverloads()){
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist){
					closestUnit = unit;
					closestDist = dist;
				}
				return closestUnit;
		}

		return closestUnit;
	}

	public void onUnitShow(Unit unit){
		if(unit.getType() == UnitType.Zerg_Overlord &&
				unit.getPlayer() == MyBotModule.Broodwar.self() &&
				unit.getHitPoints() >= 0){
			overloadData.addOverload(unit);
		}
	}
	
	/**
	 * onUnitComplete
	 * @param unit
	 * 
	 * overload 정보를 갱신한다.
	 * setOverloadsBasicPosition이 onUnitComplete에서만 실행되는데, 확인 해봐야 합니다.
	 */
	public void onUnitComplete(Unit unit) {
		if (unit.getType() == UnitType.Zerg_Overlord 
			&& commandUtil.IsValidSelfUnit(unit)){
			overloadData.addOverload(unit);
			if(secondScoutOverload == null){
				assignScoutIfNeeded(unit);
			}else{
				setOverloadsBasicPosition(unit);
			}
		}
	}
	
	/**
	 * onUnitCreate
	 * @param unit
	 * 
	 * overload 정보를 갱신한다.
	 */	
	public void onUnitCreate(Unit unit) { 
	}

	/**
	 * onUnitCreate
	 * @param unit
	 * 
	 * overload 정보를 갱신한다.
	 */
	public void onUnitDestroy(Unit unit){
		overloadData.overloasDestroyed(unit);
	}
	
	public void getOverloadJobMapCount()
	{
		overloadData.getOverloadJobMapCount();
		
	}

	public void printOverloadJobMap(){
		overloadData.printOverloadJobMap();
	}
	
	// sc76.choi 오버로드가 본진을 patrol 한다.
	public void setMainBasePatrolOverload(){
		if(secondScoutOverload != null && myMainBasePatrolOverload != null){
			
			if(!commandUtil.IsValidUnit(myMainBasePatrolOverload)) return;
			
			overloadData.setOverloadJob(myMainBasePatrolOverload, OverloadData.OverloadJob.MyMainBasePatrol, (Unit)null);
			currentMainBaseScoutTargetPosition = getScoutFleePositionFromSelfMainBaseRegionVertices(selfMainBaseLocation, myMainBasePatrolOverload);
			commandUtil.move(myMainBasePatrolOverload, currentMainBaseScoutTargetPosition);
		}
	}

	// sc76.choi 오버로드가 앞마당을 patrol 한다.
	public void setMainExpansionBasePatrolOverload(){
		if(myFirstChokePatrolOverload != null){
			
			if(!commandUtil.IsValidUnit(myFirstChokePatrolOverload)) return;
			
			overloadData.setOverloadJob(myFirstChokePatrolOverload, OverloadData.OverloadJob.MyFirstChokePotrol, (Unit)null);
			currentExpansionBaseScoutTargetPosition = getScoutFleePositionFromSelfExpansionBaseRegionVertices(selfFirstExpansionLocation, myFirstChokePatrolOverload);
			commandUtil.move(myFirstChokePatrolOverload, currentExpansionBaseScoutTargetPosition);
		}
	}
	
	/**
	 * setOverloadsBasicPosition
	 * 오버로드가 태어나는 순서대로 위치를 지정하여 보낸다.

	private Unit myMainLocationOverload;
	private Unit myExpansionLocationOverload;
	private Unit myFirstChokeOverload;
	private Unit mySecondChokeOverload;
	private Unit centerChokeOverload;
	private Unit enemyFirstChokeOverload;
	private Unit enemySecondChokeOverload;
 
		Idle,			///< 하는 일 없음. 대기 상태. 
		Overload,		///< 수리. Terran_SCV 만 가능
		Move,			///< 이동
		Scout, 			///< 정찰. Move와 다름. 정찰지에 도착하면 이동이 없음
		Detector,       ///<Detector 역활
		MyMainBase,
		MyExpansionBase,
		MyFirstChoke,
		MySecondChoke,
		EnemyFirstChoke,
		EnemySecondChoke,
		Center,
		Default 		///< 기본. 미설정 상태.  
	 */
	
    private Unit secondScoutOverload;
    
    
	public void setOverloadsBasicPosition(Unit unit){
		
		if(secondScoutOverload == null) return;

		Position selfMainBaseLocationPosition = selfMainBaseLocation.getPosition();
		Position selfExpansionBaseLocationPosition = selfFirstExpansionLocation.getPosition();
		Position selfFirstChokePosition = selfFirstChokePoint.getCenter();
		Position selfSecondChokePosition = selfSecondChokePoint.getCenter();
		Position centerLocationPosition = new Position(2000, 2000);
		Position enemyMainbaseLocationPosition = null;
		Position enemySecondChokePosition = null;
		
		if(enemyMainBaseLocation != null){
			enemyMainbaseLocationPosition = enemyMainBaseLocation.getPosition();
			enemySecondChokePosition = enemySecondChokePoint.getCenter();
		}
		
//		if(secondScoutOverload != null){
//			//secondScoutOverload = unit;
//		}
		// 나의 본진 patrol position
		if(myMainBasePatrolOverload == null){
			myMainBasePatrolOverload = unit;
		}
		// 나의 첫번째 choke position
		else if(myFirstChokePatrolOverload == null){
			myFirstChokePatrolOverload = unit;
			//overloadData.setOverloadJob(myFirstChokeOverload, OverloadData.OverloadJob.MyFirstChoke, (Unit)null);
			//commandUtil.move(myFirstChokeOverload, selfFirstChokePosition);
			//if(Config.DEBUG) System.out.println("** myFirstChokeOverload : " + myFirstChokeOverload.getID());
		}
		// 센터 position
		else if(centerChokeOverload == null){
			centerChokeOverload = unit;
			
			if(centerChokeOverload.isUnderAttack() 
				|| StrategyManager.Instance().buildState == StrategyManager.BuildState.blockDefence2Dragon8_P
				|| StrategyManager.Instance().buildState == StrategyManager.BuildState.hardCoreMarine_T){
				overloadData.setOverloadJob(centerChokeOverload, OverloadData.OverloadJob.Idle , (Unit)null);
				commandUtil.move(centerChokeOverload, StrategyManager.Instance().DEFENCE_POSITION);
				
			}else{
				overloadData.setOverloadJob(centerChokeOverload, OverloadData.OverloadJob.Center , (Unit)null);
				commandUtil.move(centerChokeOverload, centerLocationPosition);
			}
			//if(Config.DEBUG) System.out.println("** mySecondChokeOverload : " + mySecondChokeOverload.getID());
		} 
		// KTH. Drop 오버로드 Position
//		else if(dropOverload == null){
//			dropOverload = unit;
//			overloadData.setOverloadJob(dropOverload, OverloadData.OverloadJob.Drop, (Unit)null);
//			commandUtil.move(dropOverload, selfFirstChokePosition);
//		}		
		// 나의 두번째 choke position
		/*
		else if(mySecondChokeOverload == null){
			mySecondChokeOverload = unit;
			overloadData.setOverloadJob(mySecondChokeOverload, OverloadData.OverloadJob.MySecondChoke, (Unit)null);
			commandUtil.move(mySecondChokeOverload, selfSecondChokePosition);
			//if(Config.DEBUG) System.out.println("** mySecondChokeOverload : " + mySecondChokeOverload.getID());
		}
		*/
		// 적의 두번째 choke position
		/*
		else if(enemySecondChokeOverload == null && enemyMainBaseLocation != null){
			enemySecondChokeOverload = unit;
					overloadData.setOverloadJob(enemySecondChokeOverload, OverloadData.OverloadJob.EnemySecondChoke, (Unit)null);
					commandUtil.move(enemySecondChokeOverload, enemySecondChokePosition);
					//if(Config.DEBUG) System.out.println("** mySecondChokeOverload : " + mySecondChokeOverload.getID());
		}
		*/
	}
	
    // [참고] sc76.choi 정찰 유닛을 필요하면 새로 지정합니다
    // [참고] sc76.choi AD_fastMutalBot의 두번째 정찰 오버로드를 수정하였습니다.	
    public void assignScoutIfNeeded(Unit unit) {
        BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
        if (firstScoutOverload != null && enemyBaseLocation == null && secondScoutOverload == null) {
            secondScoutOverload = unit;
            overloadData.setOverloadJob(unit, OverloadData.OverloadJob.SecondScout, (Unit)null);
            // System.out.println("setOverloadsBasicPosition secondScoutUnit : " + secondScoutOverload.getID());
        }
    }

    // [참고] sc76.choi 두번째 정찰 유닛을 이동시킵니다
    // [참고] sc76.choi AD_fastMutalBot의 두번째 정찰 오버로드를 수정하였습니다.
    private void moveScoutUnit() {
        BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
        if (enemyBaseLocation == null) {
            // assign a scout to go scout it
            if (secondScoutOverload != null) {
                if (secondScoutTargetBaseLocation == null) {
                    secondScoutTargetBaseLocation = getClosestBaseLocation();
                    
                    
                } else {
                    if (MyBotModule.Broodwar.isExplored(secondScoutTargetBaseLocation.getTilePosition())) {
                        secondScoutTargetBaseLocation = getClosestBaseLocation();
                        //System.out.println("moveScoutUnit secondScoutTargetBaseLocation : " + secondScoutTargetBaseLocation.getTilePosition());
                    } else {
                        commandUtil.move(secondScoutOverload, secondScoutTargetBaseLocation.getPosition());
                    }
                }
            }

//            if (MyBotModule.Broodwar.isExplored(firstScoutTargetBaseLocation.getTilePosition())) {
//                // assign a scout to go scout it
//                firstScoutTargetBaseLocation = getClosestBaseLocation();
//                commandUtil.move(firstScoutOverload, firstScoutTargetBaseLocation.getPosition());
//            }
            
//            if(secondScoutTargetBaseLocation != null){
//            	System.out.println("secondScoutTargetBaseLocation 2 : " + secondScoutOverload.getID() + " " + secondScoutTargetBaseLocation.getPosition());
//            }
            
        } else {
        	
        	centerChokeOverload = secondScoutOverload;
			
			
//            boolean isHydraliskDen = false;
//            for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
//                // morphing hydralisk_den
//                if (unit.getBuildType().equals(UnitType.Zerg_Hydralisk_Den)) {
//                    isHydraliskDen = true;
//                    break;
//                }
//
//                // complete hydralisk_den
//                if (unit.getType().equals(UnitType.Zerg_Hydralisk_Den)) {
//                    isHydraliskDen = true;
//                    break;
//                }
//            }
//
//            if (MyBotModule.Broodwar.enemy().getRace().equals(Race.Terran) || isHydraliskDen) {
//                moveScoutUnitToMyBaseLocation();
//            }
//
			// sc76.choi 공격을 당하면, 본진귀환
			if(commandUtil.IsValidUnit(centerChokeOverload)){
	            if (centerChokeOverload.isUnderAttack()
	            	|| StrategyManager.Instance().buildState == StrategyManager.BuildState.blockDefence2Dragon8_P) {
	            	if(commandUtil.IsValidUnit(centerChokeOverload)){
		            	overloadData.setOverloadJob(centerChokeOverload, OverloadData.OverloadJob.Idle , (Unit)null);
		                moveScoutUnitToMyBaseLocation(centerChokeOverload);
	            	}
	            }else{
	            	overloadData.setOverloadJob(centerChokeOverload, OverloadData.OverloadJob.Center , (Unit)null);
	    			commandUtil.move(secondScoutOverload, new Position(2000, 2000));
	            }
			}
//
//            if (secondScoutOverload != null && secondScoutOverload.isUnderAttack()) {
//                BaseLocation myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
//                commandUtil.move(secondScoutOverload, myMainBaseLocation.getPosition());
//            }
        }
    }

	private BaseLocation getClosestBaseLocation() {
        double closestDistance = 1000000000;
        double tempDistance;
        BaseLocation closestBaseLocation = null;
        // 아군 MainBaseLocation 으로부터 가장 가까운 미정찰 BaseLocation 을 새로운 정찰 대상 currentScoutTargetBaseLocation 으로 잡아서 이동
        for (BaseLocation startLocation : BWTA.getStartLocations()) {
            if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition())) {
                continue;
            }

            if (startLocation == firstScoutTargetBaseLocation) {
                continue;
            }

            if (startLocation == secondScoutTargetBaseLocation) {
                continue;
            }

            tempDistance = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getGroundDistance(startLocation) + 0.5;
            if (tempDistance > 0 && tempDistance < closestDistance) {
                closestBaseLocation = startLocation;
                closestDistance = tempDistance;
            }
        }

        return closestBaseLocation;
    }
    
    private void moveScoutUnitToMyBaseLocation(Unit overload) {
        BaseLocation myFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
        commandUtil.move(overload, myFirstExpansionLocation.getPoint());
//        if (secondScoutOverload != null) {
//            BaseLocation myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
//            commandUtil.move(secondScoutOverload, myMainBaseLocation.getPosition());
//        }
    }

    
	// sc76.choi 추가하려면 별개로 함수를 둔다.
	// 메인 patrol을 위한 매소드
	public Position getScoutFleePositionFromSelfMainBaseRegionVertices(BaseLocation baseLocationParam, Unit overloadParam)
	{
		// calculate enemy region vertices if we haven't yet
		if (selfMainBaseRegionVertices.isEmpty()) {
			calculateSelfMainBaseRegionVertices(baseLocationParam);
		}

		if (selfMainBaseRegionVertices.isEmpty()) {
			return MyBotModule.Broodwar.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		if (currentMainBaseScoutFreeToVertexIndex == -1)
		{
			// so return the closest position in the polygon
			int closestPolygonIndex = getClosestVertexIndex(overloadParam, selfMainBaseRegionVertices);

			if (closestPolygonIndex == -1)
			{
				return MyBotModule.Broodwar.self().getStartLocation().toPosition();
			}
			else
			{
				// set the current index so we know how to iterate if we are still fleeing later
				currentMainBaseScoutFreeToVertexIndex = closestPolygonIndex;
				return selfMainBaseRegionVertices.get(closestPolygonIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else
		{
			double distanceFromCurrentVertex = selfMainBaseRegionVertices.get(currentMainBaseScoutFreeToVertexIndex).getDistance(overloadParam.getPosition());

			// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
			while (distanceFromCurrentVertex < 128)
			{
				currentMainBaseScoutFreeToVertexIndex = (currentMainBaseScoutFreeToVertexIndex + 1) % selfMainBaseRegionVertices.size();
				distanceFromCurrentVertex = selfMainBaseRegionVertices.get(currentMainBaseScoutFreeToVertexIndex).getDistance(overloadParam.getPosition());
			}

			return selfMainBaseRegionVertices.get(currentMainBaseScoutFreeToVertexIndex);
		}
	}
	
	// 나의 MainBaseLocation 이 있는 Region 의 가장자리를  selfBaseRegionVertices 에 저장한다
	// Region 내 모든 건물을 Eliminate 시키기 위한 지도 탐색 로직 작성시 참고할 수 있다
	public void calculateSelfMainBaseRegionVertices(BaseLocation baseLocation)
	{
		if (baseLocation == null) {
			return;
		}
		
		Region selfRegion = baseLocation.getRegion();
		if (selfRegion == null) {
			return;
		}
		final Position basePosition = MyBotModule.Broodwar.self().getStartLocation().toPosition();
		final Vector<TilePosition> closestTobase = MapTools.Instance().getClosestTilesTo(basePosition);
		Set<Position> unsortedVertices = new HashSet<Position>();

		// check each tile position
		for (int i = 0; i < closestTobase.size(); ++i)
		{
			final TilePosition tp = closestTobase.get(i);

			if (BWTA.getRegion(tp) != selfRegion)
			{
				continue;
			}

			// a tile is 'surrounded' if
			// 1) in all 4 directions there's a tile position in the current region
			// 2) in all 4 directions there's a buildable tile
			boolean surrounded = true;
			if (BWTA.getRegion(new TilePosition(tp.getX() + 1, tp.getY())) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() + 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() + 1)) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() + 1))
					|| BWTA.getRegion(new TilePosition(tp.getX() - 1, tp.getY())) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() - 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() - 1)) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() - 1)))
			{
				surrounded = false;
			}

			// push the tiles that aren't surrounded 
			// Region의 가장자리 타일들만 추가한다
			if (!surrounded && MyBotModule.Broodwar.isBuildable(tp))
			{
				if (Config.DrawScoutInfo)
				{
					int x1 = tp.getX() * 32 + 2;
					int y1 = tp.getY() * 32 + 2;
					int x2 = (tp.getX() + 1) * 32 - 2;
					int y2 = (tp.getY() + 1) * 32 - 2;
					MyBotModule.Broodwar.drawTextMap(x1 + 3, y1 + 2, "" + BWTA.getGroundDistance(tp, basePosition.toTilePosition()));
					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Green, false);
				}

				unsortedVertices.add(new Position(tp.toPosition().getX() + 16, tp.toPosition().getY() + 16));
			}
		}

		Vector<Position> sortedVertices = new Vector<Position>();
		Position current = unsortedVertices.iterator().next();
		selfMainBaseRegionVertices.add(current);
		unsortedVertices.remove(current);

		// while we still have unsorted vertices left, find the closest one remaining to current
		while (!unsortedVertices.isEmpty())
		{
			double bestDist = 1000000;
			Position bestPos = null;

			for (final Position pos : unsortedVertices)
			{
				double dist = pos.getDistance(current);

				if (dist < bestDist)
				{
					bestDist = dist;
					bestPos = pos;
				}
			}

			current = bestPos;
			sortedVertices.add(bestPos);
			unsortedVertices.remove(bestPos);
		}

		// let's close loops on a threshold, eliminating death grooves
		int distanceThreshold = 100;

		while (true)
		{
			// find the largest index difference whose distance is less than the threshold
			int maxFarthest = 0;
			int maxFarthestStart = 0;
			int maxFarthestEnd = 0;

			// for each starting vertex
			for (int i = 0; i < (int)sortedVertices.size(); ++i)
			{
				int farthest = 0;
				int farthestIndex = 0;

				// only test half way around because we'll find the other one on the way back
				for (int j= 1; j < sortedVertices.size() / 2; ++j)
				{
					int jindex = (i + j) % sortedVertices.size();

					if (sortedVertices.get(i).getDistance(sortedVertices.get(jindex)) < distanceThreshold)
					{
						farthest = j;
						farthestIndex = jindex;
					}
				}

				if (farthest > maxFarthest)
				{
					maxFarthest = farthest;
					maxFarthestStart = i;
					maxFarthestEnd = farthestIndex;
				}
			}

			// stop when we have no long chains within the threshold
			if (maxFarthest < 4)
			{
				break;
			}

			double dist = sortedVertices.get(maxFarthestStart).getDistance(sortedVertices.get(maxFarthestEnd));

			Vector<Position> temp = new Vector<Position>();

			for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size())
			{
				
				temp.add(sortedVertices.get(s));
			}

			sortedVertices = temp;
		}

		selfMainBaseRegionVertices = sortedVertices;
	}
	
	// sc76.choi 추가하려면 별개로 함수를 둔다.
	// Expansion patrol을 위한 매소드
	public Position getScoutFleePositionFromSelfExpansionBaseRegionVertices(BaseLocation baseLocationParam, Unit overloadParam)
	{
		// calculate enemy region vertices if we haven't yet
		if (selfExpansionBaseRegionVertices.isEmpty()) {
			calculateSelfExpansionBaseRegionVertices(baseLocationParam);
		}

		if (selfExpansionBaseRegionVertices.isEmpty()) {
			return MyBotModule.Broodwar.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		if (currentExpansionBaseScoutFreeToVertexIndex == -1)
		{
			// so return the closest position in the polygon
			int closestPolygonIndex = getClosestVertexIndex(overloadParam, selfExpansionBaseRegionVertices);

			if (closestPolygonIndex == -1)
			{
				return MyBotModule.Broodwar.self().getStartLocation().toPosition();
			}
			else
			{
				// set the current index so we know how to iterate if we are still fleeing later
				currentExpansionBaseScoutFreeToVertexIndex = closestPolygonIndex;
				return selfExpansionBaseRegionVertices.get(closestPolygonIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else
		{
			double distanceFromCurrentVertex = selfExpansionBaseRegionVertices.get(currentExpansionBaseScoutFreeToVertexIndex).getDistance(overloadParam.getPosition());

			// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
			while (distanceFromCurrentVertex < 128)
			{
				currentExpansionBaseScoutFreeToVertexIndex = (currentExpansionBaseScoutFreeToVertexIndex + 1) % selfExpansionBaseRegionVertices.size();
				distanceFromCurrentVertex = selfExpansionBaseRegionVertices.get(currentExpansionBaseScoutFreeToVertexIndex).getDistance(overloadParam.getPosition());
			}

			return selfExpansionBaseRegionVertices.get(currentExpansionBaseScoutFreeToVertexIndex);
		}
	}
	
	public void calculateSelfExpansionBaseRegionVertices(BaseLocation baseLocation)
	{
		if (baseLocation == null) {
			return;
		}
		
		Region selfRegion = baseLocation.getRegion();
		if (selfRegion == null) {
			return;
		}
		final Position basePosition = MyBotModule.Broodwar.self().getStartLocation().toPosition();
		final Vector<TilePosition> closestTobase = MapTools.Instance().getClosestTilesTo(basePosition);
		Set<Position> unsortedVertices = new HashSet<Position>();

		// check each tile position
		for (int i = 0; i < closestTobase.size(); ++i)
		{
			final TilePosition tp = closestTobase.get(i);

			if (BWTA.getRegion(tp) != selfRegion)
			{
				continue;
			}

			// a tile is 'surrounded' if
			// 1) in all 4 directions there's a tile position in the current region
			// 2) in all 4 directions there's a buildable tile
			boolean surrounded = true;
			if (BWTA.getRegion(new TilePosition(tp.getX() + 1, tp.getY())) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() + 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() + 1)) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() + 1))
					|| BWTA.getRegion(new TilePosition(tp.getX() - 1, tp.getY())) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() - 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() - 1)) != selfRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() - 1)))
			{
				surrounded = false;
			}

			// push the tiles that aren't surrounded 
			// Region의 가장자리 타일들만 추가한다
			if (!surrounded && MyBotModule.Broodwar.isBuildable(tp))
			{
				if (Config.DrawScoutInfo)
				{
					int x1 = tp.getX() * 32 + 2;
					int y1 = tp.getY() * 32 + 2;
					int x2 = (tp.getX() + 1) * 32 - 2;
					int y2 = (tp.getY() + 1) * 32 - 2;
					MyBotModule.Broodwar.drawTextMap(x1 + 3, y1 + 2, "" + BWTA.getGroundDistance(tp, basePosition.toTilePosition()));
					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Green, false);
				}

				unsortedVertices.add(new Position(tp.toPosition().getX() + 16, tp.toPosition().getY() + 16));
			}
		}

		Vector<Position> sortedVertices = new Vector<Position>();
		Position current = unsortedVertices.iterator().next();
		selfExpansionBaseRegionVertices.add(current);
		unsortedVertices.remove(current);

		// while we still have unsorted vertices left, find the closest one remaining to current
		while (!unsortedVertices.isEmpty())
		{
			double bestDist = 1000000;
			Position bestPos = null;

			for (final Position pos : unsortedVertices)
			{
				double dist = pos.getDistance(current);

				if (dist < bestDist)
				{
					bestDist = dist;
					bestPos = pos;
				}
			}

			current = bestPos;
			sortedVertices.add(bestPos);
			unsortedVertices.remove(bestPos);
		}

		// let's close loops on a threshold, eliminating death grooves
		int distanceThreshold = 100;

		while (true)
		{
			// find the largest index difference whose distance is less than the threshold
			int maxFarthest = 0;
			int maxFarthestStart = 0;
			int maxFarthestEnd = 0;

			// for each starting vertex
			for (int i = 0; i < (int)sortedVertices.size(); ++i)
			{
				int farthest = 0;
				int farthestIndex = 0;

				// only test half way around because we'll find the other one on the way back
				for (int j= 1; j < sortedVertices.size() / 2; ++j)
				{
					int jindex = (i + j) % sortedVertices.size();

					if (sortedVertices.get(i).getDistance(sortedVertices.get(jindex)) < distanceThreshold)
					{
						farthest = j;
						farthestIndex = jindex;
					}
				}

				if (farthest > maxFarthest)
				{
					maxFarthest = farthest;
					maxFarthestStart = i;
					maxFarthestEnd = farthestIndex;
				}
			}

			// stop when we have no long chains within the threshold
			if (maxFarthest < 4)
			{
				break;
			}

			double dist = sortedVertices.get(maxFarthestStart).getDistance(sortedVertices.get(maxFarthestEnd));

			Vector<Position> temp = new Vector<Position>();

			for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size())
			{
				
				temp.add(sortedVertices.get(s));
			}

			sortedVertices = temp;
		}

		selfExpansionBaseRegionVertices = sortedVertices;
	}
	
	public int getClosestVertexIndex(Unit unit, Vector<Position> RegionVertices)
	{
		int closestIndex = -1;
		double closestDistance = 10000000;

		for (int i = 0; i < RegionVertices.size(); ++i)
		{
			double dist = unit.getDistance(RegionVertices.get(i));
			if (dist < closestDistance)
			{
				closestDistance = dist;
				closestIndex = i;
			}
		}

		return closestIndex;
	}
	
	/// 나의 Main Base Location 이 있는 Region 의 경계선에 해당하는 Vertex 들의 목록을 리턴합니다
	public Vector<Position> getSelfMainBaseRegionVertices()
	{
		return selfMainBaseRegionVertices;
	}
	
	/// 오버로드 유닛들의 상태를 저장하는 workerData 객체를 리턴합니다
	public OverloadData getOverloadData(){
		return overloadData;
	}
	
	public boolean isFinishedInitialScout() {
		return isFinishedInitialScout;
	}

	public void setFinishedInitialScout(boolean isFinishedInitialScout) {
		this.isFinishedInitialScout = isFinishedInitialScout;
	}

	public int getCurrentOverloadScoutStatus() {
		return currentOverloadScoutStatus;
	}

	public void setCurrentOverloadScoutStatus(int currentOverloadScoutStatus) {
		this.currentOverloadScoutStatus = currentOverloadScoutStatus;
	}

	public Unit getFirstScoutOverload() {
		return firstScoutOverload;
	}

	public void setFirstScoutOverload(Unit firstScoutOverload) {
		this.firstScoutOverload = firstScoutOverload;
	}	
	
    
    public Unit getSecondScoutOverload() {
		return secondScoutOverload;
	}

	public void setSecondScoutOverload(Unit secondScoutOverload) {
		this.secondScoutOverload = secondScoutOverload;
	}
	
//	public void setDropOverload(Unit dropOverload) {
//		this.dropOverload = dropOverload;
//	}	
//
//	public Unit getDropOverload() {
//		return dropOverload;
//	}
	
	public BaseLocation getCurrentScoutTargetBaseLocation() {
		return currentScoutTargetBaseLocation;
	}

	public void setCurrentScoutTargetBaseLocation(BaseLocation currentScoutTargetBaseLocation) {
		this.currentScoutTargetBaseLocation = currentScoutTargetBaseLocation;
	}

	public BaseLocation getSecondScoutTargetBaseLocation() {
		return secondScoutTargetBaseLocation;
	}

	public void setSecondScoutTargetBaseLocation(BaseLocation secondScoutTargetBaseLocation) {
		this.secondScoutTargetBaseLocation = secondScoutTargetBaseLocation;
	}
	
	public ArrayList<Unit> getDropOverloadList() {
		return dropOverloadList;
	}

	public void addDropOverloadList(Unit unit) {
		dropOverloadList.add(unit);
	}

	public void destroyDropOverloadList(Unit unit) {
		dropOverloadList.remove(unit);
	}
}
