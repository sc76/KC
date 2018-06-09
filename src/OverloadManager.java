
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Game;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;


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
	private int currentScoutStatus;
	private int moveTogglingConut = 0;

	
	private Unit firstScoutOverload; // 게임 시작시에, 첫번째 정찰에 투입될 오버로드
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
	Chokepoint enemyFirstChokePoint = null;
	Chokepoint enemySecondChokePoint = null;
	
	// static singleton 객체를 리턴합니다
	private static OverloadManager instance = new OverloadManager();
	public static OverloadManager Instance() {
		return instance;
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
		enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
		
		assignFirstScoutOverload(); // firstScoutOverload를 지정한다.
		initialFirstScoutOverload(); // 지정된 오버로드를 정찰 시킨다.
		//handleMoveOverloads();
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
					OverloadManager.Instance().setScoutOverload(firstScoutOverload);
				}
			}
		//}
	}
	
	/// 정찰 유닛을 필요하면 새로 지정합니다
	public void assignScoutIfNeeded(){
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
					if (MyBotModule.Broodwar.getFrameCount() % Config.showConsoleLogDelayDisplayTime == 0) {
						if(Config.DEBUG) System.out.println("- restSearchOverload          : " + currentScoutUnit.getID() + " " + currentScoutUnit.getType());
					}
					// set unit as scout unit
					OverloadManager.Instance().setScoutOverload(currentScoutUnit);
				}
			}
		}
	}
	
	/**
	 * initialScoutOverload
	 * 경기시작하자 말자, overload 정찰을 보낸다.
	 */
	public void initialFirstScoutOverload(){
		
		if(isFinishedInitialScout) return; // 정찰이 끝났으면 수행하지 않음
		
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
				commandUtil.move(firstScoutOverload, closestBaseLocation.getPosition());
			}
		}
		// 적진이 발견되었다면,
		else{
			//if(isFinishedInitialScout) return;
//			if (firstScoutOverload == null || firstScoutOverload.exists() == false || firstScoutOverload.getHitPoints() <= 0 ){
//				// 다시 정찰이 필요하다면 assing해 준다.
//				assignFirstScoutOverload();
//			}

//			if(MyBotModule.Broodwar.enemy().getRace() == Race.Terran){
//				commandUtil.move(firstScoutOverload, enemySecondChokeLocation);
//			}else{
//				commandUtil.move(firstScoutOverload, enemyMainLocation);
//			}
			
			// 적진과 TilePosition 3개 정도의 가까이에 들어 왔으면, 두번째 적군의 chokepoint에 패트롤 한다.  
			double distanceFromEnemyMainBaseLocation = enemyMainBaseLocation.getDistance(firstScoutOverload.getPosition());
			if(distanceFromEnemyMainBaseLocation <= (double)TilePosition.SIZE_IN_PIXELS*3){
				firstScoutOverload.patrol(enemySecondChokePoint.getCenter());
				currentOverloadScoutStatus = ScoutStatus.NoScout.ordinal();
				isFinishedInitialScout = true; // 초반 정찰 끝
			}
			//setIdleOverload(firstScoutOverload); // 정찰 임무를 풀어준다. Scout Job 유지
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
			setOverloadsBasicPosition(unit);
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
	public void setOverloadsBasicPosition(Unit unit){

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
		
		// 나의 첫번째 choke position
		if(myFirstChokeOverload == null){
			myFirstChokeOverload = unit;
			overloadData.setOverloadJob(myFirstChokeOverload, OverloadData.OverloadJob.MyFirstChoke, (Unit)null);
			commandUtil.move(myFirstChokeOverload, selfFirstChokePosition);
			//if(Config.DEBUG) System.out.println("** myFirstChokeOverload : " + myFirstChokeOverload.getID());
		}
		// 나의 두번째 choke position
		else if(mySecondChokeOverload == null){
			mySecondChokeOverload = unit;
			overloadData.setOverloadJob(mySecondChokeOverload, OverloadData.OverloadJob.MySecondChoke, (Unit)null);
			commandUtil.move(mySecondChokeOverload, selfSecondChokePosition);
			//if(Config.DEBUG) System.out.println("** mySecondChokeOverload : " + mySecondChokeOverload.getID());
		}
		// 센터 position
		else if(centerChokeOverload == null){
			centerChokeOverload = unit;
			overloadData.setOverloadJob(centerChokeOverload, OverloadData.OverloadJob.Center , (Unit)null);
			commandUtil.move(centerChokeOverload, centerLocationPosition);
			//if(Config.DEBUG) System.out.println("** mySecondChokeOverload : " + mySecondChokeOverload.getID());
		} 
		// 적의 두번째 choke position
		else if(enemySecondChokeOverload == null && enemyMainBaseLocation != null){
			enemySecondChokeOverload = unit;
					overloadData.setOverloadJob(enemySecondChokeOverload, OverloadData.OverloadJob.EnemySecondChoke, (Unit)null);
					commandUtil.move(enemySecondChokeOverload, enemySecondChokePosition);
					//if(Config.DEBUG) System.out.println("** mySecondChokeOverload : " + mySecondChokeOverload.getID());
		}
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
}
