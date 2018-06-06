
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Game;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;


/// Overload 상태를 관리하고 컨트롤하는 class
public class OverloadManager {
	public static Game Broodwar;
	private int currentOverloadScoutStatus;
	private int currentScoutStatus;
	private int moveTogglingConut = 0;

	private Unit firstScoutOverload;
	private Unit currentScoutUnit;
	
	private Unit myMainLocationOverload;
	private Unit myExpansionLocationOverload;
	private Unit myFirstChokeOverload;
	private Unit mySecondChokeOverload;
	private Unit centerChokeOverload;
	private Unit enemyFirstChokeOverload;
	private Unit enemySecondChokeOverload;
	private Unit best3MultiLocationOverload;
	private Unit enemyBasePatrolOverload;
	
	private Unit restSearchOverload;
	private boolean isFinishedInitialScout = false;
	private int firstTwoMoveScoutOverload = 0;	
	
	public enum ScoutStatus {
		NoScout,						///< 정찰 유닛을 미지정한 상태
		MovingToAnotherBaseLocation,	///< 적군의 BaseLocation 이 미발견된 상태에서 정찰 유닛을 이동시키고 있는 상태
		MoveAroundEnemyBaseLocation   	///< 적군의 BaseLocation 이 발견된 상태에서 정찰 유닛을 이동시키고 있는 상태
	};
	
	/// 오버로드 목록
	private List<Unit> overloads = new ArrayList<Unit>();
	
	private BaseLocation currentScoutTargetBaseLocation = null;
	
	/// 각 Overload 에 대한 OverloadJob 상황을 저장하는 자료구조 객체
	private OverloadData overloadData = new OverloadData();
	
	private CommandUtil commandUtil = new CommandUtil();
	
	private static OverloadManager instance = new OverloadManager();
	
	/// static singleton 객체를 리턴합니다
	public static OverloadManager Instance() {
		return instance;
	}
	
	/// 경기가 시작되면 오버로드를 정찰합니다.
	public void update() {
		// harsshNet
		assignFirstScoutOverload(); // firstScoutOverload를 지정한다.
		initialScoutOverload(); // 
	
	}
	
	/// 게임 시작시에 정찰 오버로드을 필요하면 새로 지정합니다
	public void assignFirstScoutOverload()
	{
		if(isFinishedInitialScout) return;
		
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
		
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
					OverloadManager.Instance().setScoutOverload(firstScoutOverload);
				}
			}
		//}
	} // AssignFirstScoutOverload
	
	/// 정찰 유닛을 필요하면 새로 지정합니다
	public void assignScoutIfNeeded()
	{
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());

		if (enemyBaseLocation == null)
		{
			if (currentScoutUnit == null || currentScoutUnit.exists() == false || currentScoutUnit.getHitPoints() <= 0)
			{
				currentScoutUnit = null;
				currentScoutStatus = ScoutStatus.NoScout.ordinal();

				// first building (Pylon / Supply Depot / Spawning Pool) 을 건설 시작한 후, 가장 가까이에 있는 Worker 를 정찰유닛으로 지정한다
				Unit restSearchOverload = null;

				for (Unit unit : MyBotModule.Broodwar.self().getUnits())
				{
					//if (unit.getType().isBuilding() == true && unit.getType().isResourceDepot() == false)
					//if(unit.getType() == UnitType.Zerg_Spawning_Pool)
					if(unit.getType() == UnitType.Zerg_Overlord)
					{
						currentScoutUnit = unit;
						break;
					}
				}

				if (currentScoutUnit != null){
					// set unit as scout unit
					if (MyBotModule.Broodwar.getFrameCount() % Config.showDelayDisplayTime == 0) {
						if(Config.DEBUG) System.out.println("- restSearchOverload          : " + currentScoutUnit.getID() + " " + currentScoutUnit.getType());
					}
					// set unit as scout unit
					OverloadManager.Instance().setScoutOverload(currentScoutUnit);
				}
			}
		}
	}
	
	public void restUnitSearchScoutOverload(){
//		initAssignScoutIfNeeded();
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			//if (unit.getType().isBuilding() == true && unit.getType().isResourceDepot() == false)
			//if(unit.getType() == UnitType.Zerg_Spawning_Pool)
			
			if(unit.getType() == UnitType.Zerg_Overlord)
			{
				restSearchOverload = unit;
				break;
			}
		}
		
		for (BaseLocation b : BWTA.getBaseLocations()) {
			// do something. For example send some unit to attack that position:
			// myUnit.attack(b.getPosition());
			//if (MyBotModule.Broodwar.isExplored(b.getTilePosition()) == false){
				//currentOverloadScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();
				if(restSearchOverload != null){ 
					restSearchOverload.move(b.getPosition());
				}
			//}
		}
	}
	
	/**
	 * initialScoutOverload
	 * 경기시작하자 말자, overload 정찰을 보낸다.
	 */
	public void initialScoutOverload(){
		
		if(isFinishedInitialScout) return; // 정찰이 끝났으면 수행하지 않음
		
		if (firstScoutOverload == null || firstScoutOverload.exists() == false || firstScoutOverload.getHitPoints() <= 0 ){
			firstScoutOverload = null;
			currentOverloadScoutStatus = ScoutStatus.NoScout.ordinal();
			return;
		}
		
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		BaseLocation myBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		
		// 적이 없으면 맵을 투어
		if (enemyBaseLocation == null){
			BaseLocation closestBaseLocation = null;
			double closestDistance = 1000000000;
			double tempDistance = 0;
			
			if(currentScoutTargetBaseLocation == null){
				currentScoutTargetBaseLocation = myBaseLocation; // harshZerg 수정
			}
			
			boolean isExploredTemp = false;
			Position tempPosition = null;
			for (BaseLocation startLocation : BWTA.getStartLocations())
			{
				isExploredTemp = MyBotModule.Broodwar.isExplored(startLocation.getTilePosition());
				
				// if we haven't explored it yet (방문했었던 곳은 다시 가볼 필요 없음)
				if (isExploredTemp == false)
				{
					// GroundDistance 를 기준으로 가장 가까운 곳으로 선정
					tempDistance = currentScoutTargetBaseLocation.getAirDistance(startLocation) + 0.5;
					//tempDistance = (double)(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getAirDistance(startLocation) + 0.5);

					if (tempDistance > 0 && tempDistance < closestDistance) {
						closestBaseLocation = startLocation;
						closestDistance = tempDistance;
					}
				}
			}
			
			currentScoutTargetBaseLocation = closestBaseLocation;

			
			if (currentScoutTargetBaseLocation != null && firstScoutOverload != null) {
				currentOverloadScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();
				//System.out.println("move scout overload : " + currentScoutTargetBaseLocation.getPosition());
				commandUtil.move(firstScoutOverload, closestBaseLocation.getPosition());
			}
			//firstScoutOverload.move(closestBaseLocation.getPosition());
			
//			for (BaseLocation b : BWTA.getBaseLocations()) {
//				// If this is a possible start location,
//				if (b.isStartLocation()) {
//					// do something. For example send some unit to attack that position:
//					// myUnit.attack(b.getPosition());
//					
//					if (MyBotModule.Broodwar.isExplored(b.getTilePosition()) == false){
//						currentOverloadScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();
//						firstScoutOverload.move(b.getPosition());
//					}
//				}
//			}
		}
		// 적진이 발견되었다면, 적진의 앞마당에 포진
		else{
			//if(isFinishedInitialScout) return;
//			if (firstScoutOverload == null || firstScoutOverload.exists() == false || firstScoutOverload.getHitPoints() <= 0 ){
//				// 다시 정찰이 필요하다면 assing해 준다.
//				assignFirstScoutOverload();
//			}
			// TODO harshZerg 20170611:2002 적군 앞마당 확인 - nullpoint error 조심
			// TODO harshZerg 다시 정찰 가는 것이 라면 어디로 갈지 선택해야 한다.
			//Chokepoint firstEnemyChokePoint = BWTA.getNearestChokepoint(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer).getTilePosition());
			//Chokepoint firstEnemyChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
			Chokepoint firstEnemyChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
			Position enemyMainLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer).getPosition();
			Position enemyFirstChokeLocation = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer).getSides().second;
			Position enemySecondChokeLocation = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer).getSides().second;
//			int x = firstEnemyChokePoint.getSides().first.getX(); // + Config.TILE_SIZE*3;
//			int y = firstEnemyChokePoint.getSides().first.getY(); // + Config.TILE_SIZE*2;
//			commandUtil.move(firstScoutOverload, new Position(x, y));
			if(MyBotModule.Broodwar.enemy().getRace() == Race.Terran){
				commandUtil.move(firstScoutOverload, enemySecondChokeLocation);
			}else{
				commandUtil.move(firstScoutOverload, enemyMainLocation);
			}
			//firstScoutOverload.patrol(firstEnemyChokePoint.getCenter());
			currentOverloadScoutStatus = ScoutStatus.NoScout.ordinal();
			//setIdleOverload(firstScoutOverload); // 정찰 임무를 풀어준다. Scout Job 유지
			isFinishedInitialScout = true; // 초반 정찰 끝
		}
	}

	/**
	 * setInitialRelley
	 * 초반 한번만 수행
	 * 첫번째 정찰 overload가 적진에 도착했으면, 
	 * 그 다음은 앞마당까지 rally를 한다.
	 */
	boolean isFinishedInitialPatrol = false;
	public void setInitialPatrol(){
		
		if(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy()) == null){
			return;
		}
		
		if(firstScoutOverload == null || firstScoutOverload.exists() == false || firstScoutOverload.getHitPoints() <= 0){
			return;
		}
		
		if(isFinishedInitialPatrol) return; // patrol이 수행되었으면 return
		
		Position enemyMainLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy()).getPosition();
		Position enemyFirstChokeLocation = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.enemy()).getPoint();
		
		if(firstScoutOverload.getPosition().equals(enemyMainLocation)){
			firstScoutOverload.patrol(enemyFirstChokeLocation);
			if(Config.DEBUG) System.out.println("** setInitialPatrol : " + firstScoutOverload.getID());
			isFinishedInitialPatrol = true;
		}
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

	/// 해당 일꾼 정찰 unit 의 OverloadJob 값를 Idle 로 변경합니다
	public void setIdleOverload(Unit unit){
		if (unit == null) return;
		overloadData.setOverloadJob(unit, OverloadData.OverloadJob.Idle, (Unit)null);
	}
	
	// TODO harshZerg 20170611:1737 target으로 오버로드를 보낸다.
	public void moveScoutOverloadToTarget(Position targetPosion){
		if (targetPosion != null) 
		{
			// 오버로드 이동
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
									
				// idle overload를 move Command 로 앞마당으로 보냅니다
				if (unit.getType() == UnitType.Zerg_Overlord) {
					
					// 정찰중인 임무는 계속 하도록 둔다
					if(overloadData.getOverloadJob(unit) == OverloadData.OverloadJob.Scout){
						continue;
					}
					
					if(firstScoutOverload.equals(unit))
						continue;
					
					//if (unit.isIdle()) {
						if(moveTogglingConut % 4 == 0){
							int x = targetPosion.getX() + Config.TILE_SIZE*5;
							int y = targetPosion.getY() + Config.TILE_SIZE*5;
							commandUtil.move(unit, new Position(x, y));
							//commandUtil.move(unit, targetBaseLocation.getPosition());
						}else if(moveTogglingConut % 4 == 1){
							int x = targetPosion.getX() + Config.TILE_SIZE*5;
							int y = targetPosion.getY() - Config.TILE_SIZE*5;
							commandUtil.move(unit, new Position(x, y));
							//commandUtil.move(unit, targetBaseLocation.getPosition());
						}else if(moveTogglingConut % 4 == 2){
							int x = targetPosion.getX() - Config.TILE_SIZE*5;
							int y = targetPosion.getY() + Config.TILE_SIZE*5;
							commandUtil.move(unit, new Position(x, y));
							//commandUtil.move(unit, targetBaseLocation.getPosition());
						}else if(moveTogglingConut % 4 == 3){
							int x = targetPosion.getX() - Config.TILE_SIZE*5;
							int y = targetPosion.getY() - Config.TILE_SIZE*5;
							commandUtil.move(unit, new Position(x, y));
							//commandUtil.move(unit, targetBaseLocation.getPosition());
						}
						moveTogglingConut++;
					//}
				} 
			}
		}
	}
	
	// sets a worker as a scout
	public void setScoutOverload(Unit overload){
		if (overload == null) return;
		overloadData.setOverloadJob(overload, OverloadData.OverloadJob.Scout, (Unit)null);
	}
	
	// sets a worker as a scout
	public void setSDetectorOverload(Unit overload){
		if (overload == null) return;
		overloadData.setOverloadJob(overload, OverloadData.OverloadJob.Detector, (Unit)null);
	}	

	/// target 으로부터 가장 가까운 Detector를 리턴합니다
	public Unit getClosestOverload(Position target)
	{
		Unit closestUnit = null;
		double closestDist = 1000000000;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			//if (unit.getType().isDetector())
			if (unit.getType() == UnitType.Zerg_Overlord)
			{
				double dist = unit.getDistance(target);
				//System.out.println("unit : " + unit.getType());
				//System.out.println("dist : " + dist);
				if (closestUnit == null || dist < closestDist)
				{
					closestUnit = unit;
					closestDist = dist;
				}
				return closestUnit;
			}
		}

		return closestUnit;
	}

	// 정찰 상태를 리턴합니다
	public int getScoutStatus()
	{
		return currentOverloadScoutStatus;
	}
	
	/// 정찰 유닛을 리턴합니다
	public Unit getScoutUnit()
	{
		return currentScoutUnit;
	}

	public void onUnitShow(Unit unit){
		if(unit.getType() == UnitType.Zerg_Overlord &&
				unit.getPlayer() == MyBotModule.Broodwar.self() &&
				unit.getHitPoints() >= 0){
			overloadData.addOverload(unit);
		}
	}
	
	/**
	 * onUnitCreate
	 * @param unit
	 * 
	 * overload 정보를 갱신한다.
	 */
	public void onUnitComplete(Unit unit) { 
		if(unit.getPlayer() == MyBotModule.Broodwar.self()){
			if(unit.getType() == UnitType.Zerg_Overlord){
				overloads.add(unit);
			}
		}
	}

	/**
	 * onUnitCreate
	 * @param unit
	 * 
	 * overload 정보를 갱신한다.
	 */
	public void onUnitDestroy(Unit unit){
		if(unit.getPlayer() == MyBotModule.Broodwar.self()){
			if(unit.getType() == UnitType.Zerg_Overlord &&
					unit.getHitPoints() >= 0){
				overloadData.clearPreviousJob(unit);
				overloads.remove(unit);
			}
		}
	}
	
	public void getOverloadJobMapCount()
	{
		overloadData.getOverloadJobMapCount();
		
	}

	public void printOverloadJobMap(){
		overloadData.printOverloadJobMap();
	}

//	public void moveBeforeCombatOverload(Position targetPosition, int canMoveOverloeads){
//		
//		if (targetPosition != null && 1 == 2 ) {
//			// 오버로드 이동
//			int tempOverloadCount = 0;
//			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
//				if (MyBotModule.Broodwar.getFrameCount() % Config.showDelayDisplayTime == 0) {
//					if(Config.DEBUG) System.out.println("-------------------moveBeforeCombatOverload2-----------------");
//					if(Config.DEBUG) System.out.println("- moverBore targetPosition     : " + targetPosition + " ");
//					if(Config.DEBUG) System.out.println("- canMoveOverloeads            : " + canMoveOverloeads + " ");
//					if(Config.DEBUG) System.out.println("- tempOverloadCount            : " + tempOverloadCount + " ");
//				}
//					
//				// TODO harshZerg Zerg_Overlord 중, Detector와 Scout 역활이 아닌 idle인 유닛을 적진에 보낸다.
//				if (unit.getType() == UnitType.Zerg_Overlord){
////					&& 
////				}
////						(overloadData.getOverloadJob(unit) == OverloadData.OverloadJob.Detector ||
////							overloadData.getOverloadJob(unit) == OverloadData.OverloadJob.Idle ||
////							overloadData.getOverloadJob(unit) == OverloadData.OverloadJob.Default)) {
//					
//					//unit.stop();
//					
//					// Detector로 지정
//					// set Detector role
//					setSDetectorOverload(unit);
//					
//					if (MyBotModule.Broodwar.getFrameCount() % Config.showDelayDisplayTime == 0) {
//						if(Config.DEBUG) System.out.println("- canMoveOverloeads            : " + canMoveOverloeads );
//						if(Config.DEBUG) System.out.println("- moving overload              : " + unit.getID() + " " + overloadData.getOverloadJob(unit));
//					}
//					
//					//commandUtil.move(unit, targetBaseLocation.getPosition());
//					if(firstTwoMoveScoutOverload % 2 == 0){
//						// TODO 앞마당의 공격당하기 불가능한 포지션 지정 ????, 맵별 하디코딩이라도 해야 할까?
//						int x = targetPosition.getX() + Config.TILE_SIZE*4;
//						int y = targetPosition.getY() + Config.TILE_SIZE*3;
//						commandUtil.move(unit, new Position(x, y));
//					}else{
//						int x = targetPosition.getX() - Config.TILE_SIZE*4;
//						int y = targetPosition.getY() - Config.TILE_SIZE*3;
//						commandUtil.move(unit, new Position(x, y));
//					}
//					firstTwoMoveScoutOverload++; // 오버로드 출발시 증가
//					
//					tempOverloadCount++;
//				}
//				
//				// harshZerg 필요한 overload를 보냈으면 종료
//				if(tempOverloadCount >= canMoveOverloeads) break;
//			}
//		}
//	}
	
	/**
	 * setOverloadsPosition
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
	public void setOverloadsBasicPosition(Unit unit){

//		if (MyBotModule.Broodwar.getFrameCount() % Config.showDelayDisplayTime == 0) {
//			if(Config.DEBUG) System.out.println("** setOverloadsPosition firstScoutOverload : " + getFirstScoutOverload().getID());
//			//if(Config.DEBUG) System.out.println("** setOverloadsPosition centerChokeOverload : " + centerChokeOverload.getID());
//			if(Config.DEBUG) System.out.println("** setOverloadsPosition myFirstChokeOverload : " + myFirstChokeOverload.getID());
//			if(Config.DEBUG) System.out.println("** setOverloadsPosition mySecondChokeOverload : " + mySecondChokeOverload.getID());
//			if(Config.DEBUG) System.out.println("** setOverloadsPosition enemySecondChokeOverload : " + enemySecondChokeOverload.getID());
//		}
		Position myMainPosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getPosition();
		Position myExpansionPosition = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self()).getPosition();
		Position myFirstChokePosition = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getCenter();
		Position mySecondChokePosition = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self()).getSides().second;
		Position centerPosition = new Position(2000, 2000);
		
		if(MyBotModule.Broodwar.self().completedUnitCount(UnitType.Zerg_Overlord) < 1) return;
		
		if(myFirstChokeOverload == null){
			myFirstChokeOverload = unit;
			overloadData.setOverloadJob(myFirstChokeOverload, OverloadData.OverloadJob.MyFirstChoke, (Unit)null);
			
			commandUtil.move(myFirstChokeOverload, myFirstChokePosition);
			if(Config.DEBUG) System.out.println("** myFirstChokeOverload : " + myFirstChokeOverload.getID());
		}
		else if(mySecondChokeOverload == null){
			mySecondChokeOverload = unit;
			overloadData.setOverloadJob(mySecondChokeOverload, OverloadData.OverloadJob.MySecondChoke, (Unit)null);
			
			commandUtil.move(mySecondChokeOverload, mySecondChokePosition);
			//if(Config.DEBUG) System.out.println("** mySecondChokeOverload : " + mySecondChokeOverload.getID());
		}else if(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy()) != null){
			if(MyBotModule.Broodwar.enemy().getRace() != Race.Terran){
				Position enemyFirstChokePosition = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.enemy()).getCenter();
				Position enemySecondChokePosition = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.enemy()).getSides().second;
				
				if(enemySecondChokeOverload == null){
					enemySecondChokeOverload = unit;
					overloadData.setOverloadJob(enemySecondChokeOverload, OverloadData.OverloadJob.EnemySecondChoke, (Unit)null);
					
					commandUtil.move(enemySecondChokeOverload, enemySecondChokePosition);
					if(Config.DEBUG) System.out.println("** enemySecondChokeOverload : " + enemySecondChokeOverload.getID());
				}
			}
		}else if(myExpansionLocationOverload == null){
			myExpansionLocationOverload = unit;
			overloadData.setOverloadJob(myExpansionLocationOverload, OverloadData.OverloadJob.MyExpansionBase, (Unit)null);
			
			commandUtil.move(myExpansionLocationOverload, myExpansionPosition);
			//if(Config.DEBUG) System.out.println("** myExpansionLocationOverload : " + myExpansionLocationOverload.getID());
		}else if(myMainLocationOverload == null){
			myMainLocationOverload = unit;
			overloadData.setOverloadJob(myMainLocationOverload, OverloadData.OverloadJob.MyMainBase, (Unit)null);
			
			commandUtil.move(myMainLocationOverload, myMainPosition);
			//if(Config.DEBUG) System.out.println("** myMainLocationOverload : " + myMainLocationOverload.getID());
		}else if(enemyBasePatrolOverload == null){
			if(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy()) != null){
				enemyBasePatrolOverload = unit;
				overloadData.setOverloadJob(enemyBasePatrolOverload, OverloadData.OverloadJob.enemyBasePatrol, (Unit)null);
				
				enemyBasePatrolOverload.patrol(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy()).getPosition());
			}
		}
	}
	
	public List<Unit> getOverloads() {
		return overloads;
	}

	public void setOverloads(List<Unit> overloads) {
		this.overloads = overloads;
	}
	
	/// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 리턴합니다
	public OverloadData getOverloadData()
	{
		return overloadData;
	}
}
