
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
	private int currentScoutStatus;
	private int moveTogglingConut = 0;

	
	private Unit firstScoutOverload; // 게임 시작시에, 첫번째 정찰에 투입될 오버로드
	
	private Unit myMainLocationOverload;
	private Unit myExpansionLocationOverload;
	private Unit myFirstChokeOverload;
	private Unit myMainBasePatrolOverload;
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
	
	private Vector<Position> selfBaseRegionVertices = new Vector<Position>();
	private int currentScoutFreeToVertexIndex = -1;
	private Position currentMainBaseScoutTargetPosition = Position.None;
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
		
		setMainBasePatrolOverload();
		//setMainExpansionBasePatrolOverload();
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
	
	public void setMainBasePatrolOverload(){
		if(myMainBasePatrolOverload != null){
			
			if(!commandUtil.IsValidUnit(myMainBasePatrolOverload)) return;
			
			overloadData.setOverloadJob(myMainBasePatrolOverload, OverloadData.OverloadJob.MyMainBasePatrol, (Unit)null);
			currentMainBaseScoutTargetPosition = getScoutFleePositionFromSelfRegionVertices(selfMainBaseLocation, myMainBasePatrolOverload);
			commandUtil.move(myMainBasePatrolOverload, currentMainBaseScoutTargetPosition);
		}
	}

	public void setMainExpansionBasePatrolOverload(){
		if(myFirstChokeOverload != null){
			
			if(!commandUtil.IsValidUnit(myFirstChokeOverload)) return;
			
			overloadData.setOverloadJob(myFirstChokeOverload, OverloadData.OverloadJob.MyFirstChoke, (Unit)null);
			currentExpansionBaseScoutTargetPosition = getScoutFleePositionFromSelfRegionVertices(selfFirstExpansionLocation, myFirstChokeOverload);
			commandUtil.move(myFirstChokeOverload, currentExpansionBaseScoutTargetPosition);
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
		
		if(myMainBasePatrolOverload == null){
			myMainBasePatrolOverload = unit;
		}else
		
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
	
	// sc76.choi 추가하려면 별개로 함수를 둔다.
	public Position getScoutFleePositionFromSelfRegionVertices(BaseLocation baseLocationParam, Unit overloadParam)
	{
		// calculate enemy region vertices if we haven't yet
		if (selfBaseRegionVertices.isEmpty()) {
			calculateSelfRegionVertices(baseLocationParam);
		}

		if (selfBaseRegionVertices.isEmpty()) {
			return MyBotModule.Broodwar.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		if (currentScoutFreeToVertexIndex == -1)
		{
			// so return the closest position in the polygon
			int closestPolygonIndex = getClosestVertexIndex(overloadParam, selfBaseRegionVertices);

			if (closestPolygonIndex == -1)
			{
				return MyBotModule.Broodwar.self().getStartLocation().toPosition();
			}
			else
			{
				// set the current index so we know how to iterate if we are still fleeing later
				currentScoutFreeToVertexIndex = closestPolygonIndex;
				return selfBaseRegionVertices.get(closestPolygonIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else
		{
			double distanceFromCurrentVertex = selfBaseRegionVertices.get(currentScoutFreeToVertexIndex).getDistance(overloadParam.getPosition());

			// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
			while (distanceFromCurrentVertex < 128)
			{
				currentScoutFreeToVertexIndex = (currentScoutFreeToVertexIndex + 1) % selfBaseRegionVertices.size();
				distanceFromCurrentVertex = selfBaseRegionVertices.get(currentScoutFreeToVertexIndex).getDistance(overloadParam.getPosition());
			}

			return selfBaseRegionVertices.get(currentScoutFreeToVertexIndex);
		}
	}
	
	// 나의 MainBaseLocation 이 있는 Region 의 가장자리를  selfBaseRegionVertices 에 저장한다
	// Region 내 모든 건물을 Eliminate 시키기 위한 지도 탐색 로직 작성시 참고할 수 있다
	public void calculateSelfRegionVertices(BaseLocation baseLocation)
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
		selfBaseRegionVertices.add(current);
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

		selfBaseRegionVertices = sortedVertices;
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
	public Vector<Position> getSelfRegionVertices()
	{
		return selfBaseRegionVertices;
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
