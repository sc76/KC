import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import bwapi.Color;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private final Boolean DEBUG = Config.DEBUG;
	private final Boolean DRAW = Config.DRAW;

	// 공격 포지션
	Position TARGET_POSITION = null;
	TilePosition TARGET_TILEPOSITION = null;

	// 공격 포지션
	Position TARGET_POSITION_Z = null;
	TilePosition TARGET_TILEPOSITION_Z = null;

	// 방어 포지션
	Position DEFENCE_POSITION = null;
	TilePosition DEFENCE_TILEPOSITION = null;
	
	// 랜덤 포지션
	Position RANDOM_POSITION = null;
	TilePosition RANDOM_TILEPOSITION = null;
	
	// Center 포지션
	Position CENTER_POSITION = new Position(2000, 2000);
	TilePosition CENTER_TILEPOSITION = CENTER_POSITION.toTilePosition();
	
	public enum CombatState { 
		initialMode,                        // 초반 빌드오더 타임
		defenseMode,						// 아군 진지 방어
		attackStarted,						// 아군 유닛으로 적 공격 시작
		eliminateEnemy						// 적 Eliminate 
	};
		
	CombatState combatState;				/// 전투 상황
	
	// sc76.choi 상황에 맞는 빌드 모드 설정
	public enum BuildState { 
		normalMode,                         // 기본
		onlyZergling,						// 저글링 모드, 저글링이 다수 필요할 때
		onlyHydralist,						// 히드라 모드, 히드라가 다수 필요할 때
		onlyMutalisk,						// 뮤탈 모드, 중반 이후, only 질럿, 저글링만 보일 때		
		fasterMutalisk,						// 빠른 뮤탈 모드, 태란 다수 탱크가 있을 때, 퀸도 빨리 올려 활용한다.
		fasterUltralisk,						// 빠른 울트라 모드, 태란 입구 막음 or 프로토스 앞마당 포토밭을 만들 때 상황
		fastZergling_Z,
		superZergling_Z,
		fastMutalisk_Z,
		lurker_Z,
		blockTheFirstChokePoint_Z,
		hardCoreMarine_T,
		fastVulture_T,
		blockTheFirstChokePoint_T,
		blockTheSecondChokePoint_T,
		vulture_Galia_Tank_T,
		Tank_T,
		totally_attack_T,
		hardCoreZealot_P,
		darkTemplar_P,
		blockDefence2Dragon8_P,
		blockTheFirstChokePoint_P,
		blockTheSecondChokePoint_P,
		carrier_P
	};	
	
	public BuildState buildState = BuildState.normalMode;     				// sc76.choi 상황에 맞는 빌드 모드 설정
	
	// 아군
	Player myPlayer;
	Race myRace;
	
	// 적군
	Player enemyPlayer;
	Race enemyRace;
	
	// 공격, 방어 횟수
	private int countAttackMode;
	private int countDefenceMode;
	
	// 나와 적진의 베이스 갯수
	int myOccupiedBaseLocations = 1;
	int enemyOccupiedBaseLocations = 1;
	
	// 아군 공격 유닛 첫번째, 두번째, 세번째 타입                          프로토스     테란         저그
	UnitType myCombatUnitType1;					/// 질럿          마린          저글링
	UnitType myCombatUnitType2;			  		/// 드라군        메딕          히드라리스크
	UnitType myCombatUnitType3;			  		/// 다크템플러   시즈탱크    러커
	UnitType myCombatUnitType4;					///                  뮤탈
	UnitType myCombatUnitType5;					///                  울트라  
	UnitType myCombatUnitType6;					///                  가디언  

	// 아군 특수 유닛 첫번째, 두번째 타입
	UnitType mySpecialUnitType1;			  	/// 옵저버       사이언스베쓸   오버로드
	UnitType mySpecialUnitType2;				/// 하이템플러   배틀크루저     디파일러
	UnitType mySpecialUnitType3;				///                    스커지
	UnitType mySpecialUnitType4;				///                    퀸

	// 아군 공격 유닛 생산 순서 
	int[] buildOrderArrayOfMyCombatUnitType;	/// 아군 공격 유닛 첫번째 타입, 두번째 타입 생산 순서
	int nextTargetIndexOfBuildOrderArray;	    /// buildOrderArrayMyCombatUnitType 에서 다음 생산대상 아군 공격 유닛

	// 아군의 공격유닛 숫자
	int necessaryNumberOfCombatUnitType1;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 저글링
	int necessaryNumberOfCombatUnitType2;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 히드라
	int necessaryNumberOfCombatUnitType3;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 럴커
	int necessaryNumberOfCombatUnitType4;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 뮤탈
	int necessaryNumberOfCombatUnitType5;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 울트라
	int necessaryNumberOfCombatUnitType6;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 가디언
	
	// 아군의 공격유닛 숫자
	int necessaryNumberOfDefenceUnitType1;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자 // 저글링 
	int necessaryNumberOfDefenceUnitType2;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자 // 히드라
	int necessaryNumberOfDefenceUnitType3;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자	// 럴커
	int necessaryNumberOfDefenceUnitType4;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자	// 뮤탈
	int necessaryNumberOfDefenceUnitType5;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자	// 울트라
	int necessaryNumberOfDefenceUnitType6;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자	// 가디언

	// 아군의 공격유닛 숫자
	int myKilledCombatUnitCount1;				/// 첫번째 유닛 타입의 사망자 숫자 누적값 // 저글링
	int myKilledCombatUnitCount2;				/// 두번째 유닛 타입의 사망자 숫자 누적값 // 히드라
	int myKilledCombatUnitCount3;				/// 세번째 유닛 타입의 사망자 숫자 누적값 // 럴커
	int myKilledCombatUnitCount4;				/// 세번째 유닛 타입의 사망자 숫자 누적값 // 뮤탈
	int myKilledCombatUnitCount5;				/// 세번째 유닛 타입의 사망자 숫자 누적값 // 울트라
	int myKilledCombatUnitCount6;				/// 세번째 유닛 타입의 사망자 숫자 누적값 // 가디언

	// 아군의 일반유닛 숫자
	int maxNumberOfCombatUnitType3;
	
	// 아군의 특수유닛 숫자
	int maxNumberOfSpecialUnitType1;			/// 최대 몇개까지 생산 / 전투참가 시킬것인가 오버로드
	int maxNumberOfSpecialUnitType2;			/// 최대 몇개까지 생산 / 전투참가 시킬것인가 디파일러
	int maxNumberOfSpecialUnitType3;			/// 최대 몇개까지 생산 / 전투참가 시킬것인가 스커지
	int maxNumberOfSpecialUnitType4;			/// 최대 몇개까지 생산 / 전투참가 시킬것인가 퀸 
	
	int myKilledSpecialUnitCount1;				/// 첫번째 특수 유닛 타입의 사망자 숫자 누적값 // 오버로드
	int myKilledSpecialUnitCount2;				/// 두번째 특수 유닛 타입의 사망자 숫자 누적값 // 디파일러
	int myKilledSpecialUnitCount3;				/// 두번째 특수 유닛 타입의 사망자 숫자 누적값  // 스커지
	int myKilledSpecialUnitCount4;				/// 두번째 특수 유닛 타입의 사망자 숫자 누적값  // 퀸
	
	// sc76.choi 아군 최대 생산 제한 유닛수
	int maxNumberOfTrainUnitType1;
	int maxNumberOfTrainUnitType2;
	int maxNumberOfTrainUnitType3;
	int maxNumberOfTrainUnitType4;
	int maxNumberOfTrainUnitType5;
	
	// sc76.choi 아군 특수 유닛 최대 생산 제한 유닛수
	int maxNumberOfTrainSpecialUnitType1;
	int maxNumberOfTrainSpecialUnitType2;
	int maxNumberOfTrainSpecialUnitType3;
	int maxNumberOfTrainSpecialUnitType4; 
	
	// 아군 공격 전체 유닛 목록	
	ArrayList<Unit> myAllCombatUnitList = new ArrayList<Unit>();      
	ArrayList<Unit> myCombatUnitType1List = new ArrayList<Unit>(); // 저글링      
	ArrayList<Unit> myCombatUnitType2List = new ArrayList<Unit>(); // 히드라
	ArrayList<Unit> myCombatUnitType3List = new ArrayList<Unit>(); // 럴커
	ArrayList<Unit> myCombatUnitType1ListAway = new ArrayList<Unit>(); // 저글링      
	ArrayList<Unit> myCombatUnitType2ListAway = new ArrayList<Unit>(); // 히드라
	ArrayList<Unit> myCombatUnitType3ListAway = new ArrayList<Unit>(); // 럴커
	ArrayList<Unit> myCombatUnitType4List = new ArrayList<Unit>(); // 뮤탈      
	ArrayList<Unit> myCombatUnitType5List = new ArrayList<Unit>(); // 울트라      
	ArrayList<Unit> myCombatUnitType6List = new ArrayList<Unit>(); // 가디언      
	ArrayList<Unit> mySpecialUnitType1List = new ArrayList<Unit>(); // 오버로드       
	ArrayList<Unit> mySpecialUnitType2List = new ArrayList<Unit>(); // 디파일러
	ArrayList<Unit> mySpecialUnitType3List = new ArrayList<Unit>(); // 스커지
	ArrayList<Unit> mySpecialUnitType4List = new ArrayList<Unit>(); // 퀸
	
	ArrayList<Unit> myCombatUnitType2MultiDefenceList = new ArrayList<Unit>(); // 히드라 멀티 방어용
	ArrayList<Unit> myCombatUnitType2MultiDefenceList2 = new ArrayList<Unit>(); // 히드라 멀티 방어용
	ArrayList<Unit> myCombatUnitType1ScoutList = new ArrayList<Unit>(); // 저글링 정찰용
	ArrayList<Unit> myCombatUnitType1ScoutList2 = new ArrayList<Unit>(); // 저글링 정찰용
	
	// 아군 방어 건물 첫번째, 두번째 타입
	UnitType myDefenseBuildingType1;			/// 파일런 벙커 크립콜로니
	UnitType myDefenseBuildingType2;			/// 포톤  터렛  성큰콜로니
	UnitType myDefenseBuildingType3;			/// 포톤  터렛  성포어콜로니

	// 아군 방어 건물 건설 숫자
	int necessaryNumberOfDefenseBuilding1;		/// 방어 건물 건설 갯수
	int necessaryNumberOfDefenseBuilding2;		/// 방어 건물 건설 갯수

	// 아군 방어 건물 건설 위치
	BuildOrderItem.SeedPositionStrategy seedPositionStrategyOfMyInitialBuildingType;
	BuildOrderItem.SeedPositionStrategy seedPositionStrategyOfMyDefenseBuildingType;
	BuildOrderItem.SeedPositionStrategy seedPositionStrategyOfMyCombatUnitTrainingBuildingType;

	// 아군 방어 건물 목록 
	ArrayList<Unit> myDefenseBuildingType1List = new ArrayList<Unit>();  // 파일런 벙커 크립
	ArrayList<Unit> myDefenseBuildingType2List = new ArrayList<Unit>();  // 캐논   터렛 성큰

	// 적군 공격 유닛 숫자
	int numberOfCompletedEnemyCombatUnit;
	int numberOfCompletedEnemyWorkerUnit;

	// 적군 유닛 사망자 수 
	int enemyKilledCombatUnitCount;					/// 적군 공격유닛 사망자 숫자 누적값
	int enemyKilledWorkerUnitCount;					/// 적군 일꾼유닛 사망자 숫자 누적값
	
	// 아군 유닛 사망자 수 
	int selfKilledCombatUnitCount;					/// 적군 공격유닛 사망자 숫자 누적값
	int selfKilledWorkerUnitCount;					/// 적군 일꾼유닛 사망자 숫자 누적값
	
	// 아군 / 적군의 본진, 첫번째 길목, 두번째 길목
	BaseLocation myMainBaseLocation; 
	BaseLocation myFirstExpansionLocation; 
	Chokepoint myFirstChokePoint;
	Chokepoint mySecondChokePoint;
	BaseLocation enemyMainBaseLocation;
	BaseLocation enemyFirstExpansionLocation; 
	Chokepoint enemyFirstChokePoint;
	Chokepoint enemySecondChokePoint;
		
	boolean isInitialBuildOrderFinished;	/// setInitialBuildOrder 에서 입력한 빌드오더가 다 끝나서 빌드오더큐가 empty 되었는지 여부

	// sc76.choi 공격을 위한 가장 가까운 아군 타겟 선정
	Unit closesAttackUnitFromEnemyMainBase;
	Position closesAttackUnitOfPositionFromEnemyMainBase;
	
	// 가스, 미네럴 양
	int selfMinerals = 0;
	int selfGas = 0;
	
	// 가용 가스, 미네럴 양
	int selfAvailableMinerals = 0;
	int selfAvailableGas = 0;

	public StrategyManager() {
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		
		// 과거 게임 기록을 로딩합니다
		//loadGameRecordList();
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
		
		// sc76.choi 
		// KCBaseInfoManager.Instance().updateByOneTime();
		//if(Config.DEBUG){
		//	System.out.println(KCBaseInfoManager.Instance().printKCBaseList());
		//}
		
		/// 변수 초기값을 설정합니다
		setVariables();

		/// 게임 초기에 사용할 빌드오더를 세팅합니다
		setInitialBuildOrder();		
	}
	
	/// 변수 초기값을 설정합니다
	// sc76.choi onStart에서 한번만 수행
	void setVariables(){

		// sc76.choi 경기 시작 시, initialMode로 시작
		// sc76.choi excuteCombat에서 스푸링 플이 건설되었으면, defenseMode로 변경된다.
		combatState = CombatState.initialMode;
		
		// 참가자께서 자유롭게 초기값을 수정하셔도 됩니다 
		myPlayer = MyBotModule.Broodwar.self();
		myRace = MyBotModule.Broodwar.self().getRace();
		enemyPlayer = InformationManager.Instance().enemyPlayer;

		myKilledCombatUnitCount1 = 0;
		myKilledCombatUnitCount2 = 0;
		myKilledCombatUnitCount3 = 0;
		
		numberOfCompletedEnemyCombatUnit = 0;
		numberOfCompletedEnemyWorkerUnit = 0;
		enemyKilledCombatUnitCount = 0;
		enemyKilledWorkerUnitCount = 0;
		selfKilledWorkerUnitCount = 0;
		
		isInitialBuildOrderFinished = false;
		
		// 나의 유닛
		// 공격 유닛 종류 설정 
		myCombatUnitType1 = UnitType.Zerg_Zergling;
		myCombatUnitType2 = UnitType.Zerg_Hydralisk;
		myCombatUnitType3 = UnitType.Zerg_Lurker;
		myCombatUnitType4 = UnitType.Zerg_Mutalisk;
		myCombatUnitType5 = UnitType.Zerg_Ultralisk;
		myCombatUnitType6 = UnitType.Zerg_Guardian;

		// 특수 유닛 종류 설정 
		mySpecialUnitType1 = UnitType.Zerg_Overlord;
		mySpecialUnitType2 = UnitType.Zerg_Defiler;
		mySpecialUnitType3 = UnitType.Zerg_Scourge;
		mySpecialUnitType4 = UnitType.Zerg_Queen;

		// 공격 유닛 생산 순서 설정
		// 1 저그링
		// 2 히드라
		// 3 럴커
		// 4 뮤탈
		// 5 울트라 리스크
		// 7 가디언
		buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 2, 2, 3, 1, 1, 2, 2, 2, 3};
		nextTargetIndexOfBuildOrderArray = 0; 			    	// 다음 생산 순서 index

		// 방어 건물 종류 및 건설 갯수 설정
		myDefenseBuildingType1 = UnitType.Zerg_Creep_Colony;
		myDefenseBuildingType2 = UnitType.Zerg_Sunken_Colony;
		myDefenseBuildingType3 = UnitType.Zerg_Spore_Colony;
		
		// 방어 건물 건설 위치 설정 
		seedPositionStrategyOfMyInitialBuildingType = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;	// 본진
		seedPositionStrategyOfMyDefenseBuildingType = BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation;	// 첫번째 choke point
		
	} // end of setVariables
	
	/**
	 * getCombatUnitCountInPosition
	 * @param pos
	 * @param player
	 * @return
	 * 
	 * 시야에 보이는 기준으로, 해당 포지션에서 player의 병력의 수를 리턴한다.
	 */
	public int getCombatUnitCountInPosition(Position pos, Player player,int radius){
		int unitCount = 0 ;
		for(Map.Entry<Integer,UnitInfo> unitInfoEntry : InformationManager.Instance().getUnitAndUnitInfoMap(player).entrySet()) {
			Unit enemyUnit = unitInfoEntry.getValue().getUnit();

			if(commandUtil.IsValidUnit(enemyUnit)) continue;
			
			if(DEBUG) System.out.println("enemyUnit.getType() : " + enemyUnit.getType() + " " + enemyUnit.getType().isBuilding());
			// 해당 player의 공격유닛이 200 거리 안에 몇 마리나 존재 하는지 파악
				if(pos.getDistance(enemyUnit.getPosition()) <= radius){
					if(enemyUnit.getType().isBuilding()){
						unitCount++;
					}
				}
		}
		return unitCount;
	}
	
	public int getCountBurrowedZergling(){
		int unitCount = 0 ;
		
		for(Map.Entry<Integer,UnitInfo> unitInfoEntry : InformationManager.Instance().getUnitAndUnitInfoMap(myPlayer).entrySet()) {
			Unit unit = unitInfoEntry.getValue().getUnit();

			if(unit == null) continue;
			
			if(unit.getType() == UnitType.Zerg_Zergling && unit.isBurrowed() == true){
				unitCount++;
			}
			
		}
		return unitCount;
	}
	
	
	// sc76.choi 현재 적의 거주 지역 중 가장 가까운 곳을 찾아 TARGET_POSITION으로 지정한다.
	// sc76.choi TODO 공격이 3번 이상 막히고, 다른 적 거주지가 있으면 그곳을 TARGET으로 잡아보자.
	public void getTargetPositionForAttack(){
		  
		// 2초에 1번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 24 * 2 != 0) return;

		if(enemyRace == Race.Terran && isInitialBuildOrderFinished == false){
			
			// sc76.choi 아직 발견 전이면, 앞마당을 지정
			// sc76.choi 아직 발견 전이면, 앞마당을 지정
			if(enemyMainBaseLocation == null){
				TARGET_POSITION = mySecondChokePoint.getCenter();
				TARGET_TILEPOSITION = mySecondChokePoint.getCenter().toTilePosition();
			}
			
//			System.out.println("TARGET_POSITION 1     : " + TARGET_POSITION);
//			System.out.println("TARGET_TILEPOSITION 1 : " + TARGET_TILEPOSITION);
//			System.out.println();
			
		}else{
			// sc76.choi 아직 발견 전이면, 앞마당을 지정
			if(enemyMainBaseLocation == null){
				TARGET_POSITION = mySecondChokePoint.getCenter();
				TARGET_TILEPOSITION = mySecondChokePoint.getCenter().toTilePosition();
				
				return;
			}
			  
			// sc76.choi TODO 해당지역에 건물이 없으면 그냥 본진을 타켓을 잡아야 한다. (Basic Bot 버그)
			// sc76.choi TODO 적 본진이 정확히 보이지 않았다면(오버로드가 정찰을 깊숙히 못했을 경우) 본진으로 타켓이 이동하지 않는다.
			// sc76.choi 본진 근처에 적이 있으면 그 pos로 타겟을 잡는다.
			Unit urgentUnit = getClosestCanAttackUnitTypeToTarget(enemyPlayer, null, myMainBaseLocation.getPosition(), Config.TILE_SIZE*80, false);
	
			// sc76.choi 본진에 없으면 나의 멀티지역에 가까운 적 유닛이 있는지
			if(urgentUnit == null){
				Set<Region> selfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer);
				Iterator<Region> it1 = selfRegions.iterator();
				while (it1.hasNext()) {
					Region selfRegion = it1.next();
	
					urgentUnit = getClosestCanAttackUnitTypeToTarget(enemyPlayer, null, selfRegion.getCenter(), Config.TILE_SIZE*50, false);
			    
					// sc76.choi 발견되면 바로 그 지역을 타켓을 잡는다, 찾으면 리턴하는 로직
					if(commandUtil.IsValidUnit(urgentUnit)){
						TARGET_POSITION = urgentUnit.getPosition();
						TARGET_TILEPOSITION = urgentUnit.getTilePosition();
						return;
					}
				}
			}
			  
			// sc76.choi 공격모드라도, 본진 가까이에 적이 있으면 그 곳으로 타겟을 잡는다.
			if(commandUtil.IsValidUnit(urgentUnit)){
				TARGET_POSITION = urgentUnit.getPosition();
				TARGET_TILEPOSITION = urgentUnit.getTilePosition();
			}
			else{
				//BaseLocation targetBaseLocation = enemyMainBaseLocation;
				Region targetBaseLocation = BWTA.getRegion(enemyMainBaseLocation.getPosition());
				double closestDistance = 100000000;
	
				// 나의 MainBaseLocation 와 적진의 BaseLocation중, 가장 가까운 곳을 선정한다.
				//for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(enemyPlayer)) {
				Set<Region> enemyRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().enemyPlayer);
				
				Iterator<Region> it2 = enemyRegions.iterator();
				while (it2.hasNext()) {
					Region enemyRegion = it2.next();
					//double distance = (myMainBaseLocation.getPosition()).getDistance(baseLocation.getPosition());
					double distance = (myMainBaseLocation.getPosition()).getDistance(enemyRegion.getCenter());
			    
					// 적의 확장이 3개 이상이면 확장으로 타겟을 잡는다.
					//if(enemyOccupiedBaseLocations >= 3){
					if(enemyRegions.size() >= 3){
						
						// sc76.choi 적의 본진
						if(enemyRegion.equals( BWTA.getRegion(enemyMainBaseLocation.getPosition()))
							|| (enemyRegion.getX() == BWTA.getRegion(enemyMainBaseLocation.getPosition()).getX() 
								&& enemyRegion.getY()== BWTA.getRegion(enemyMainBaseLocation.getPosition()).getY())) {
							continue;
						}
						
						// sc76.choi 적의 확장
						if(enemyRegion.equals(BWTA.getRegion(enemyFirstExpansionLocation.getPosition()))
							|| (enemyRegion.getX() == BWTA.getRegion(enemyFirstExpansionLocation.getPosition()).getX() 
								&& enemyRegion.getY() == BWTA.getRegion(enemyFirstExpansionLocation.getPosition()).getY())) {
							continue;
						}
//						// sc76.choi 적의 본진
//						if(baseLocation.equals(enemyMainBaseLocation)
//								|| (baseLocation.getX() == enemyMainBaseLocation.getX() && baseLocation.getY() == enemyMainBaseLocation.getY())) {
//							continue;
//						}
//						
//						// sc76.choi 적의 확장
//						if(baseLocation.equals(enemyFirstExpansionLocation)
//								|| (baseLocation.getX() == enemyFirstExpansionLocation.getX() && baseLocation.getY() == enemyFirstExpansionLocation.getY())) {
//							continue;
//						}
					}
					
					if (distance < closestDistance) {
						closestDistance = distance;
						targetBaseLocation = enemyRegion;
					}
				}
	
				if(targetBaseLocation != null){
//					TARGET_POSITION = targetBaseLocation.getPosition();
//					TARGET_TILEPOSITION = targetBaseLocation.getTilePosition();
					TARGET_POSITION = targetBaseLocation.getCenter();
					TARGET_TILEPOSITION = targetBaseLocation.getCenter().toTilePosition();
				}else{
					TARGET_POSITION = mySecondChokePoint.getCenter();
					TARGET_TILEPOSITION = mySecondChokePoint.getCenter().toTilePosition();
				}
			}
		}
		
//		System.out.println("TARGET_POSITION 9     : " + TARGET_POSITION);
//		System.out.println("TARGET_TILEPOSITION 9 : " + TARGET_TILEPOSITION);
//		System.out.println();
	}
	
	public void getTargetPositionForAttack_Z(){
		  
		// 2초에 1번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 24 * 2 != 0) return;

		if(enemyRace == Race.Terran && isInitialBuildOrderFinished == false){
			
			// sc76.choi 아직 발견 전이면, 앞마당을 지정
			if(enemyMainBaseLocation == null){
				TARGET_POSITION_Z = DEFENCE_POSITION;
				TARGET_TILEPOSITION_Z = DEFENCE_TILEPOSITION;
			}else{
			
				TARGET_POSITION_Z = enemySecondChokePoint.getCenter();
				TARGET_TILEPOSITION_Z = enemySecondChokePoint.getCenter().toTilePosition();
			}
			
//			System.out.println("TARGET_POSITION_Z 1     : " + TARGET_POSITION);
//			System.out.println("TARGET_TILEPOSITION_Z 1 : " + TARGET_TILEPOSITION);
//			System.out.println();
			
		}
		// 초기 빌드오더가 끝이 났다면.
		else{
			// sc76.choi 아직 발견 전이면, 앞마당을 지정
			if(enemyMainBaseLocation == null){
				TARGET_POSITION_Z = mySecondChokePoint.getCenter();
				TARGET_TILEPOSITION_Z = mySecondChokePoint.getCenter().toTilePosition();
			}
			  
			// sc76.choi TODO 해당지역에 건물이 없으면 그냥 본진을 타켓을 잡아야 한다. (Basic Bot 버그)
			// sc76.choi TODO 적 본진이 정확히 보이지 않았다면(오버로드가 정찰을 깊숙히 못했을 경우) 본진으로 타켓이 이동하지 않는다.
			// sc76.choi 본진 근처에 적이 있으면 그 pos로 타겟을 잡는다.
			Unit urgentUnit = getClosestCanAttackUnitTypeToTarget(enemyPlayer, null, myMainBaseLocation.getPosition(), Config.TILE_SIZE*80, false);
	
			// sc76.choi 본진에 없으면 나의 멀티지역에 가까운 적 유닛이 있는지
			if(urgentUnit == null){
				Set<Region> selfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer);
				Iterator<Region> it1 = selfRegions.iterator();
				while (it1.hasNext()) {
					Region selfRegion = it1.next();
	
					urgentUnit = getClosestCanAttackUnitTypeToTarget(enemyPlayer, null, selfRegion.getCenter(), Config.TILE_SIZE*50, false);
			    
					// sc76.choi 발견되면 바로 그 지역을 타켓을 잡는다, 찾으면 리턴하는 로직
					if(commandUtil.IsValidUnit(urgentUnit)){
						TARGET_POSITION_Z = urgentUnit.getPosition();
						TARGET_TILEPOSITION_Z = urgentUnit.getTilePosition();
						return;
					}
				}
			}
			  
			// sc76.choi 공격모드라도, 본진 가까이에 적이 있으면 그 곳으로 타겟을 잡는다.
			if(commandUtil.IsValidUnit(urgentUnit)){
				TARGET_POSITION_Z = urgentUnit.getPosition();
				TARGET_TILEPOSITION_Z = urgentUnit.getTilePosition();
			}
			// sc76.choi 근처에 적이 없다면 베이스를 타켓으로 한다.
			else{
				BaseLocation targetBaseLocation = enemyMainBaseLocation;
				double closestDistance = 100000000;
	
				// 나의 MainBaseLocation 와 적진의 BaseLocation중, 가장 가까운 곳을 선정한다.
				for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(enemyPlayer)) {
					double distance = (myMainBaseLocation.getPosition()).getDistance(baseLocation.getPosition());
			    
					// 적의 확장이 3개 이상이면 확장으로 타겟을 잡는다.
					if(enemyOccupiedBaseLocations >= 3){
						
						// sc76.choi 적의 본진
						if(baseLocation.equals(enemyMainBaseLocation)
							|| (baseLocation.getX() == enemyMainBaseLocation.getX() && baseLocation.getY() == enemyMainBaseLocation.getY())) {
							continue;
						}
						
						// sc76.choi 적의 확장
						if(baseLocation.equals(enemyFirstExpansionLocation)
							|| (baseLocation.getX() == enemyFirstExpansionLocation.getX() && baseLocation.getY() == enemyFirstExpansionLocation.getY())) {
							continue;
						}
					}
					
					if (distance < closestDistance) {
						closestDistance = distance;
						targetBaseLocation = baseLocation;
					}
				}
	
				if(targetBaseLocation != null){
					TARGET_POSITION_Z = targetBaseLocation.getPosition();
					TARGET_TILEPOSITION_Z = targetBaseLocation.getTilePosition();
				}else{
					TARGET_POSITION_Z = mySecondChokePoint.getCenter();
					TARGET_TILEPOSITION_Z = mySecondChokePoint.getCenter().toTilePosition();
				}
			}
		}
		
//		System.out.println("TARGET_POSITION 9     : " + TARGET_POSITION);
//		System.out.println("TARGET_TILEPOSITION 9 : " + TARGET_TILEPOSITION);
//		System.out.println();
	}

	// 현재 나의 거주 지역 중 가장 방어해야할 곳을 찾아 DEFENCE_POSITION으로 지정한다.
	public void getTargetPositionForDefence(){

		// 1초에 1번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 24 * 2 != 0) return;

//		if(enemyRace == Race.Zerg){
			// sc76.choi 최초 본진이 디펜스 위치
			DEFENCE_POSITION = myMainBaseLocation.getPosition();
			DEFENCE_TILEPOSITION = myMainBaseLocation.getPosition().toTilePosition();
		  
			// sc76.choi 앞마당에 해처리가 있는지 확인 한다.
			boolean existHatcheryInFirstExpansionRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(myFirstExpansionLocation.getTilePosition()), myPlayer, UnitType.Zerg_Hatchery);
			boolean existLairInFirstExpansionRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(myFirstExpansionLocation.getTilePosition()), myPlayer, UnitType.Zerg_Lair);
			boolean existHiveInFirstExpansionRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(myFirstExpansionLocation.getTilePosition()), myPlayer, UnitType.Zerg_Hive);
			boolean existHatcheryInCenterRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(CENTER_POSITION), myPlayer, UnitType.Zerg_Hatchery);
	 
			// sc76.choi 방어 모드시에 만약 성큰이 지어졌다면 그쪽으로 이동한다. 방어에 약간의 우세한 전략 
			// sc76.choi 앞마당으로 부터 가장 가까운 성큰이기 때문에 좀더 미세한 판단이 필요하다.
			Unit myDefenseBuildingUnit = commandUtil.GetClosestSelfUnitTypeToTarget(UnitType.Zerg_Sunken_Colony, mySecondChokePoint.getCenter());
			Unit myCombatBuildingUnit = commandUtil.GetClosestSelfUnitTypeToTarget(UnitType.Zerg_Hatchery, mySecondChokePoint.getCenter());
			// 앞마당에 해처리가 있으면
			//if (existHatcheryInFirstExpansionRegion || existLairInFirstExpansionRegion || existHiveInFirstExpansionRegion) {
		  
				if(myDefenseBuildingUnit != null && myCombatBuildingUnit != null){
					double d1 = myDefenseBuildingUnit.getDistance(mySecondChokePoint); // 성큰과 center와의 거리
					double d2 = myCombatBuildingUnit.getDistance(mySecondChokePoint); // 해처리와 center화의 거리
					
					if(d1 > d2){
						DEFENCE_POSITION = myCombatBuildingUnit.getPosition();
						DEFENCE_TILEPOSITION = myCombatBuildingUnit.getTilePosition();
					}else{
						DEFENCE_POSITION = myDefenseBuildingUnit.getPosition();
						DEFENCE_TILEPOSITION = myDefenseBuildingUnit.getTilePosition();
					}
				} else if(myCombatBuildingUnit != null){
					DEFENCE_POSITION = myCombatBuildingUnit.getPosition();
					DEFENCE_TILEPOSITION = myCombatBuildingUnit.getTilePosition();
				}
				else{
					DEFENCE_POSITION = myMainBaseLocation.getPosition();
					DEFENCE_TILEPOSITION = myMainBaseLocation.getPosition().toTilePosition();
				}
//			}
//			// sc76.choi TODO 앞마당에 없으면, 다시 성큰 위치를 찾는다 , 성큰이 있어야 한다.
//			else{
//
//				if(myCombatBuildingUnit != null){
//					DEFENCE_POSITION = myCombatBuildingUnit.getPosition();
//					DEFENCE_TILEPOSITION = myCombatBuildingUnit.getTilePosition();
//				}else{
//					DEFENCE_POSITION = myMainBaseLocation.getPosition();
//					DEFENCE_TILEPOSITION = myMainBaseLocation.getPosition().toTilePosition();
//				}
//			}
			
//		}
//		// 저그가 아니면
//		else{
//			if (buildState == BuildState.fastVulture_T || buildState == BuildState.Tank_T){
//				DEFENCE_POSITION = myFirstExpansionLocation.getPosition();
//				DEFENCE_TILEPOSITION = myFirstExpansionLocation.getTilePosition();
//			}else{
//				// sc76.choi 최초 본진이 디펜스 위치
//				DEFENCE_POSITION = myMainBaseLocation.getPosition();
//				DEFENCE_TILEPOSITION = myMainBaseLocation.getTilePosition();
//			  
//				// sc76.choi 앞마당에 해처리가 있는지 확인 한다.
//				boolean existHatcheryInFirstExpansionRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(myFirstExpansionLocation.getTilePosition()), myPlayer, UnitType.Zerg_Hatchery);
//				boolean existLairInFirstExpansionRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(myFirstExpansionLocation.getTilePosition()), myPlayer, UnitType.Zerg_Lair);
//				boolean existHiveInFirstExpansionRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(myFirstExpansionLocation.getTilePosition()), myPlayer, UnitType.Zerg_Hive);
//				boolean existHatcheryInCenterRegion = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(CENTER_POSITION), myPlayer, UnitType.Zerg_Hatchery);
//		 
//				// center를 먹었거나, 테란전의 앞마당 혹은 본진이 막혔을 경우
//				if(existHatcheryInCenterRegion == true 
//		//				|| buildState ==  BuildState.blockTheFirstChokePoint_T || buildState ==  BuildState.blockTheSecondChokePoint_T
//		//				|| (enemyRace == Race.Terran && myKilledCombatUnitCount3 >= 5)
//				){
//					
//					Position POS1 = new Position(CENTER_POSITION.getX() + Config.TILE_SIZE*15, CENTER_POSITION.getY());
//					Position POS2 = new Position(CENTER_POSITION.getX() - Config.TILE_SIZE*15, CENTER_POSITION.getY());
//					
//					if(POS1.getDistance(myMainBaseLocation.getX(), myMainBaseLocation.getY()) > POS2.getDistance(myMainBaseLocation.getX(), myMainBaseLocation.getY())
//							){
//						DEFENCE_POSITION = POS2;
//						DEFENCE_TILEPOSITION = POS2.toTilePosition();
//					}else{
//						DEFENCE_POSITION = POS1;
//						DEFENCE_TILEPOSITION = POS1.toTilePosition();
//					}
//				}
//				// center를 못 먹었으면
//				else if (existHatcheryInFirstExpansionRegion || existLairInFirstExpansionRegion || existHiveInFirstExpansionRegion) {
//			  
//					// sc76.choi 방어 모드시에 만약 성큰이 지어졌다면 그쪽으로 이동한다. 방어에 약간의 우세한 전략 
//					// sc76.choi 앞마당으로 부터 가장 가까운 성큰이기 때문에 좀더 미세한 판단이 필요하다.
//					//  mySecondChokePoint.getCenter()
//					Unit myDefenseBuildingUnit = commandUtil.GetClosestSelfUnitTypeToTarget(UnitType.Zerg_Sunken_Colony, mySecondChokePoint.getCenter());
//					if(myDefenseBuildingUnit != null){
//						 if(myDefenseBuildingUnit.getDistance(myMainBaseLocation.getPosition()) <= Config.TILE_SIZE*40){
//			    
//						// sc76.choi second choke에서 가장 가까운 성큰이 앞마당이면
////						if(BWTA.getRegion(myDefenseBuildingUnit.getPosition()) == BWTA.getRegion(myFirstExpansionLocation.getTilePosition())){
//			     
//		//					double d1 = myFirstExpansionLocation.getDistance(mySecondChokePoint); // 앞마당과 center와의 거리
//		//					double d2 = myDefenseBuildingUnit.getDistance(mySecondChokePoint); // 방어 타워와 center화의 거리
//			     
//							// sc76.choi 두 지점을 비교 센터와 가까운 곳을 DEFENCE_POSITION으로 잡는다. 
//		//					if(d1 > d2){
//								DEFENCE_POSITION = myDefenseBuildingUnit.getPosition();
//								DEFENCE_TILEPOSITION = myDefenseBuildingUnit.getTilePosition();
//		//					}else{
//		//						DEFENCE_POSITION = myFirstExpansionLocation.getPosition();
//		//						DEFENCE_TILEPOSITION = myFirstExpansionLocation.getTilePosition();
//		//					}
//						}else{
//							DEFENCE_POSITION = myFirstExpansionLocation.getPosition();
//							DEFENCE_TILEPOSITION = myFirstExpansionLocation.getTilePosition();
//						}
//					}
//					// sc76.choi 앞마당에 헤처리는 있으나, 성큰이 없으면
//					else{
//						DEFENCE_POSITION = myFirstExpansionLocation.getPosition();
//						DEFENCE_TILEPOSITION = myFirstExpansionLocation.getTilePosition();
//					}
//				}
//				// sc76.choi TODO 앞마당에 없으면, 다시 성큰 위치를 찾는다 , 성큰이 있어야 한다.
//				else{
//					//Unit myDefenseBuildingUnit = commandUtil.GetClosestSelfUnitTypeToTarget(UnitType.Zerg_Sunken_Colony, mySecondChokePoint.getCenter());
//					//if(myDefenseBuildingUnit != null){
//						DEFENCE_POSITION = myMainBaseLocation.getPosition();
//						DEFENCE_TILEPOSITION = myMainBaseLocation.getTilePosition();
//					//}
//				}
//			}
//		}
	}
	
	// 유닛의 위치에서 가까운 DEFENCE_POSITION을 찾는다.
	public Position getTargetPositionForDefence(Unit unit){
		
		Position defencePositionToUnit = DEFENCE_POSITION;
		BaseLocation cloestBase = getCloestOccupiedBaseLocation(unit.getPosition(), true);
		
		if(cloestBase == null){
			defencePositionToUnit = DEFENCE_POSITION;
		}else{
			double d1 = unit.getDistance(DEFENCE_POSITION);
			double d2 = unit.getDistance(cloestBase);
			
			if(d1 > d2){
				defencePositionToUnit = BWTA.getRegion(cloestBase.getPosition()).getCenter();
			}else{
				defencePositionToUnit = DEFENCE_POSITION;
			}
		}
		
		return defencePositionToUnit;
	}
	
	public BaseLocation getCloestOccupiedBaseLocation(Position pos, boolean include){
		BaseLocation cloestBaseLocation = null;
		double closestDist = 1000000000;
		
		List<BaseLocation> selfBaseLocations = InformationManager.Instance().getOccupiedBaseLocations(myPlayer);
		Iterator<BaseLocation> it = selfBaseLocations.iterator();
		
		while (it.hasNext()) {
			BaseLocation selfBaseLocation = it.next();
			
			if(include == false){
				if(selfBaseLocation == myMainBaseLocation || selfBaseLocation == myFirstExpansionLocation){
					continue;
				}
				
				if(selfBaseLocation.equals(myMainBaseLocation)
					|| (selfBaseLocation.getX() == myMainBaseLocation.getX() && selfBaseLocation.getY() == myMainBaseLocation.getY())
				    || selfBaseLocation.equals(myFirstExpansionLocation)
					|| (selfBaseLocation.getX() == myFirstExpansionLocation.getX() && selfBaseLocation.getY() == myFirstExpansionLocation.getY())) {
						continue;
					}
			}
			
			double dist = pos.getDistance(selfBaseLocation.getX(), selfBaseLocation.getY());
			if(closestDist > dist){
				cloestBaseLocation = selfBaseLocation;
				closestDist = dist;
			}
			
		}
		
		return cloestBaseLocation;
	}
	
	
	public Position getRandomPosition(){
		Random random = new Random();
		
		int mapHeight = Config.TILE_SIZE * 128;	// sc76.choi 좌표이기 때문에 전체 맵에서 추출되어야 한다.
		int mapWidth = Config.TILE_SIZE * 128;	// sc76.choi 좌표이기 때문에 전체 맵에서 추출되어야 한다.
		
		int rMapWidth = random.nextInt(mapWidth);
		int rMapHeight = random.nextInt(mapHeight);
		
		return new Position(rMapWidth, rMapHeight);
	}
	
	Position getCalcuatePosition(Unit unit, int dist){
		
		// 12시
		if(unit.getID() % 4 == 0){
			Position p = new Position(unit.getPosition().getX(), unit.getPosition().getY() - Config.TILE_SIZE*dist);
			
			List<Unit> radiusUnits = MyBotModule.Broodwar.getUnitsInRadius(p, Config.TILE_SIZE*3);
			for(Unit aroundUnit : radiusUnits){
				if(aroundUnit.getPlayer() == enemyPlayer){
					p = new Position(unit.getPosition().getX(), unit.getPosition().getY() + Config.TILE_SIZE*dist); 
					break;
				}
			}
			return p;
			//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 12");
		}
		// 3시
		else if(unit.getID() % 4 == 1){
			Position p = new Position(unit.getPosition().getX() + Config.TILE_SIZE*dist, unit.getPosition().getY());
			
			List<Unit> radiusUnits = MyBotModule.Broodwar.getUnitsInRadius(p, Config.TILE_SIZE*3);
			for(Unit aroundUnit : radiusUnits){
				if(aroundUnit.getPlayer() == enemyPlayer){
					p = new Position(unit.getPosition().getX() - Config.TILE_SIZE*dist, unit.getPosition().getY());  
					break;
				}
			}
			
			return p;
			//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 3");
		}
		// 6시
		else if(unit.getID() % 4 == 2){
			Position p = new Position(unit.getPosition().getX(), unit.getPosition().getY() + Config.TILE_SIZE*dist);
			
			List<Unit> radiusUnits = MyBotModule.Broodwar.getUnitsInRadius(p, Config.TILE_SIZE*3);
			for(Unit aroundUnit : radiusUnits){
				if(aroundUnit.getPlayer() == enemyPlayer){
					p = new Position(unit.getPosition().getX(), unit.getPosition().getY() - Config.TILE_SIZE*dist);  
					break;
				}
			}
			
			return p;
			//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 6");
		}
		// 9시
		else{
			Position p = new Position(unit.getPosition().getX() - Config.TILE_SIZE*dist, unit.getPosition().getY());
			
			List<Unit> radiusUnits = MyBotModule.Broodwar.getUnitsInRadius(p, Config.TILE_SIZE*3);
			for(Unit aroundUnit : radiusUnits){
				if(aroundUnit.getPlayer() == enemyPlayer){
					p = new Position(unit.getPosition().getX() + Config.TILE_SIZE*dist, unit.getPosition().getY());  
					break;
				}
			}
			
			return p;
			//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 9");
		}
	}


	/// 게임 초기에 사용할 빌드오더를 세팅합니다
	private KCInitialBuildOrder intialBuilderOrder = new KCInitialBuildOrder();
	public void setInitialBuildOrder() {
		
		// 프로토스 : 초반 방어 후 공격 
		// 테란     : 초반 선 공격
		// 저그     : 초반 선 공격
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			intialBuilderOrder.setInitialBuildOrderAgainstProtoss();
		} 
		else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			intialBuilderOrder.setInitialBuildOrderAgainstTerran();
		} 
		else if (MyBotModule.Broodwar.enemy().getRace() == Race.Zerg) {
			intialBuilderOrder.setInitialBuildOrderAgainstZerg();
		}else{
			intialBuilderOrder.setInitialBuildOrderAgainstRandom();
		}
	}

	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {

		
		// sc76.choi 베이스 정보를 업데이트 합니다.
		//updateKCBaseInfo();
		
		// 각종 기본 Config를 조정합니다.
		excuteConfigration();

		// sc76.choi 적의 유닛을 보고, 빌드를 판단한다.
		isTimeToBuildState();
		
		/// 변수 값을 업데이트 합니다
		updateVariables();
		
		// sc76.choi 일꾼도 주변에 적의 공격 유닛이 있다면 공격한다. 
		commandMyWorkerToAttack();
		
		// sc76.choi 공격 타겟 유닛 할당 
		// updateVariablesForAttackUnit();

		
		/// 일꾼을 계속 추가 생산합니다
		executeWorkerTraining();

		/// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서 SupplyProvider를 추가 건설/생산합니다
		executeSupplyManagement();

		// sc76.choi 방어건물 을 건설합니다
		executeDefenceConstruction();
		
		// 공격유닛 생산 건물을 건설합니다
		executeBuildingConstruction();

		/// 업그레이드 및 테크 리서치를 실행합니다
		executeUpgradeAndTechResearch();

		/// 특수 유닛을 생산할 수 있도록 테크트리에 따라 건설을 실시합니다
		executeTechTreeUpConstruction();

		/// 공격유닛을 계속 추가 생산합니다
		executeCombatUnitTraining();

		/// 전반적인 전투 로직 을 갖고 전투를 수행합니다
		executeCombat();
		
		// sc76.choi 성큰, 스포어 클로리의 공격 타겟을 지정한다.
		controlAdvancedDefenceBuildingCombat();

		// sc76.choi 정찰용 저그링이 움직인다.
		executeRandomScout();

		// sc76.choi 정찰용 저그링이 움직인다.		
		executeRandomScout2();
		
		// sc76.choi 멀티 디펜스용 히드라를 보낸다.		
		executeMultiDefenceHydralisk();
		
		// sc76.choi 멀티 디펜스용 히드라를 보낸다.		
		executeMultiDefenceHydralisk2();
		
		// sc76.choi 각종 보수 작업을 한다.
		executeMaintenance();

		
		/// KTH. 오버로드 드랍 실행합니다
		//executeOverloadDrop();
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi start
		/// StrategyManager 의 수행상황을 표시합니다
		drawStrategyManagerStatus();
		// sc76.choi end
		//////////////////////////////////////////////////////////////////////////////////////////////////////////

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

		// 이번 게임의 로그를 남깁니다
		//saveGameLog();
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
		
		//printUintData();
		
		// excuteTemp();
		
	}

	int getCountCombatType1() {
		return (myCombatUnitType1List.size() + myCombatUnitType1ListAway.size() + myCombatUnitType1ScoutList.size() + myCombatUnitType1ScoutList2.size());
	}
	
	int getCountCombatType2() {
		return (myCombatUnitType2List.size() + myCombatUnitType2ListAway.size() + myCombatUnitType2MultiDefenceList.size() + myCombatUnitType2MultiDefenceList2.size());
	}
	
	int getCountCombatType3() {
		return (myCombatUnitType3List.size() + myCombatUnitType3ListAway.size());
	}
	
	void excuteTemp(){
		if(DEBUG) {
			
			getLurkerDefencePosition();
			
			MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos1.toPosition(), 20, Color.Black, true);
			MyBotModule.Broodwar.drawTextMap(lurkerDefenceBuildingPos1.toPosition(), "1");
			MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos2.toPosition(), 20, Color.Black, true);
			MyBotModule.Broodwar.drawTextMap(lurkerDefenceBuildingPos2.toPosition(), "2");
			MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos3.toPosition(), 20, Color.Black, true);
			MyBotModule.Broodwar.drawTextMap(lurkerDefenceBuildingPos3.toPosition(), "3");
			MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos4.toPosition(), 20, Color.Black, true);
			MyBotModule.Broodwar.drawTextMap(lurkerDefenceBuildingPos4.toPosition(), "4");
		}
	}
	
	/**
	 * 해당 지역에 type이 있는지 검사, type이 null 이면 전체 type 대상
	 * 
	 * @author sc76.choi
	 * @param player
	 * @param type
	 * @param region
	 * @return
	 */
	public boolean existUnitTypeInRegion(Player player, UnitType type, Region region, boolean includeWorker, boolean includeFlyer){

		if (region == null || player == null) {
			return false;
		}
		
		UnitData unitData = InformationManager.Instance().getUnitData(player);
				
		Iterator<Integer> it = unitData.getUnitAndUnitInfoMap().keySet().iterator();

		while (it.hasNext()) {
			final UnitInfo ui = unitData.getUnitAndUnitInfoMap().get(it.next());
			if (ui.getType() == type) {
				
//				if(player == enemyPlayer){
//					System.out.println("enemyPlayer existsPlayerBuildingInRegion : " + ui.getUnitID() + " " + ui.getType());
//				}
				// Terran 종족의 Lifted 건물의 경우, BWTA.getRegion 결과가 null 이다
				if (BWTA.getRegion(ui.getLastPosition()) == null) continue;

				if (BWTA.getRegion(ui.getLastPosition()) == region) {
					return true;
				}
			}else if (type == null){
				
				if (BWTA.getRegion(ui.getUnit().getPosition()) == null) continue;

				if(includeFlyer == true){
					if (BWTA.getRegion(ui.getUnit().getPosition()) == region && ui.getType().isFlyer() == true) {
						if(ui.getType().canAttack() == false) continue;
						if(ui.getType().isWorker()) continue;
						return true;
					}
				}else{
					if (BWTA.getRegion(ui.getUnit().getPosition()) == region) {
						if(ui.getType().canAttack() == false) continue;
						if(ui.getType().isWorker()) continue;
						return true;
					}
				}
				
			}
		}
		return false;
	}
	
	/**
	 * 해당 지역에 type이 몇개 있는지 검사
	 * 
	 * @author sc76.choi
	 * @param player
	 * @param type
	 * @param region
	 * @return
	 */
	public int getCountUnitTypeInRegion(Player player, UnitType type, Region region, boolean includeWorker, boolean includeFlyer){

		if (region == null || player == null) {
			return 0;
		}
		
		int existTypeCount = 0;
		
		UnitData unitData = InformationManager.Instance().getUnitData(player);
		Iterator<Integer> it = unitData.getUnitAndUnitInfoMap().keySet().iterator();

		while (it.hasNext()) {
			final UnitInfo ui = unitData.getUnitAndUnitInfoMap().get(it.next());
			if (ui.getType() == type) {
				
				// Terran 종족의 Lifted 건물의 경우, BWTA.getRegion 결과가 null 이다
				if (BWTA.getRegion(ui.getUnit().getPosition()) == null) continue;

				if (BWTA.getRegion(ui.getUnit().getPosition()) == region) {
					existTypeCount++;
				}
			}
		}
		return existTypeCount;
	}	
	
	/**
	 * 해당 지역에 type이 몇개 있는지 검사
	 * @param player
	 * @param type
	 * @param pos
	 * @param radius
	 * @param includeWorker
	 * @param includeFlyer
	 * @return
	 */
	public int getCountUnitTypeInPosition(Player player, UnitType type, Position pos, int radius){

		if (pos == null || pos == null) {
			return 0;
		}
		
		int existTypeCount = 0;
		
		UnitData unitData = InformationManager.Instance().getUnitData(player);
		Iterator<Integer> it = unitData.getUnitAndUnitInfoMap().keySet().iterator();

		while (it.hasNext()) {
			final UnitInfo ui = unitData.getUnitAndUnitInfoMap().get(it.next());
			
			Unit typeUnit = ui.getUnit();
			

			if (typeUnit.getPlayer() == player && typeUnit.getType() == type) {
				
				if (BWTA.getRegion(typeUnit.getPosition()) == null) continue;
				
				if(typeUnit.getDistance(pos) < radius){
					existTypeCount++;
				}
			}
		}
		
		return existTypeCount;
	}
	
	/**
	 * getUnitAndUnitInfoMap 중에 target으로 부터 가장 가까운 공격 유닛을 찾는다.
	 * 단, 주어진 거리 (closeDistance) 보다 가까이 있는 유닛을 찾을 때 사용한다.
	 * TODO 유닛을 제한을 해야한다. 단순히 옵저버 같은 것 때문에 체크가 될 수 있기 때문에 공격에
	 * TODO 전체 유닛을 다 볼 필요는 없다. 한마리라도 있으면 return가능, 속도가 문제된다면 return 권고
	 * 
	 * @author sc76.choi
	 * @param type
	 * @param target
	 * @return
	 */
	public Unit getClosestCanAttackUnitTypeToTarget(Player player, UnitType type, Position target, int closeDistance, boolean includeFlyer){
		
		Unit closestUnit = null;
		double closestDist = 1000000000;
		
		Iterator<Integer> it = InformationManager.Instance().getUnitData(player).getUnitAndUnitInfoMap().keySet().iterator();
		while (it.hasNext()) {
		//for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			UnitInfo ui = InformationManager.Instance().getUnitData(player).getUnitAndUnitInfoMap().get(it.next());
			
			Unit enemyUnit = ui.getUnit();
			
			if(!commandUtil.IsValidUnit(enemyUnit)) continue;
			
			if(includeFlyer == false){
				if(enemyUnit.getType().isFlyer() == true) continue;
			}
			
			if(enemyUnit.getType() == UnitType.Zerg_Overlord) continue;	
			if(enemyUnit.getType() == UnitType.Protoss_Observer) continue;
			if(enemyUnit.getType() == UnitType.Terran_Science_Vessel) continue;
			if(enemyUnit.isLifted()) continue;
			if(enemyUnit.isCloaked()) continue;
			
			if(enemyUnit.getType() == UnitType.Terran_Vulture_Spider_Mine) continue;
			if(enemyUnit.getType() == UnitType.Terran_Medic) continue;
			if(enemyUnit.getType().isWorker()) continue;
			
			if(type != null && enemyUnit.getType() == type){
				double dist = enemyUnit.getDistance(target);
				
				if(dist > closeDistance) continue; // 설정한 거리보다 멀리 있으면 return
					
				if (closestUnit == null || dist < closestDist){
					closestUnit = enemyUnit;
					closestDist = dist;
				}
				return closestUnit;
			}
			
			// sc76.choi 한마리라도 찾았으면 바로 return하는 로직임.
			int countEnemyUnit = 0;
			if(type == null){
				
				countEnemyUnit++; 
				
				//System.out.println("target1 enemyUnit : " + enemyUnit.getID() + " " + enemyUnit.getType());
				double dist = enemyUnit.getDistance(target);
				
				if(dist > closeDistance) continue; // 설정한 거리보다 멀리 있으면 return
				
				
				//System.out.println("checked enemyUnit : " + enemyUnit.getID() + " " + enemyUnit.getType());
				
				if (closestUnit == null || dist < closestDist){
					closestUnit = enemyUnit;
					closestDist = dist;
					
//					return closestUnit;
					
				}
				if(countEnemyUnit > 1){
					return closestUnit;
				}
			}
		}
		
   		return closestUnit;       	
	}
	
	/// target 으로부터 가장 가까운 IDLE인 Detector를 리턴합니다.
	public Unit getClosestOverloadWithIDLE(Position target)
	{
		Unit closestUnit = null;
		double closestDist = 1000000000;

		for (Unit unit : OverloadManager.Instance().getOverloadData().getOverloads()){
				
				if(unit.getHitPoints() <= 190) continue;
				
			    if(OverloadManager.Instance().getOverloadData().getJobCode(unit) == 'I'){
					double dist = unit.getDistance(target);
					if (closestUnit == null || dist < closestDist){
						closestUnit = unit;
						closestDist = dist;
					}
					return closestUnit;
			    }
		}

		return closestUnit;
	}
	
	// sc76.choi target 으로부터 가장 가까운 Unit를 리턴합니다.
	public Unit getClosestUnitType(Player player, UnitType type, Position target)
	{
		Unit closestUnit = null;
		double closestDist = 1000000000;

		Iterator<Integer> it = InformationManager.Instance().getUnitData(player).getUnitAndUnitInfoMap().keySet().iterator();
		while (it.hasNext()) {
			UnitInfo ui = InformationManager.Instance().getUnitData(player).getUnitAndUnitInfoMap().get(it.next());
			
			if(!commandUtil.IsValidUnit(ui.getUnit())) continue;
				
			if(ui.getType() == type){
				double dist = ui.getUnit().getDistance(target);
				if (closestUnit == null || dist < closestDist){
					closestUnit = ui.getUnit();
					closestDist = dist;
				}
				return closestUnit;
			}
		}

		return closestUnit;
	}
	
	//내림차순(Desc) 정렬
	static class CompareSeqDesc implements Comparator<UnitInfo>{
        @Override
        public int compare(UnitInfo o1, UnitInfo o2) {
            return o1.getDistanceFromEnemyMainBase() > o2.getDistanceFromEnemyMainBase() ? -1 : o1.getDistanceFromEnemyMainBase() < o2.getDistanceFromEnemyMainBase() ? 1:0;
        }  
	}
	
	static class CompareSeqAsc implements Comparator<UnitInfo>{
        @Override
        public int compare(UnitInfo o1, UnitInfo o2) {
            return o1.getDistanceFromEnemyMainBase() < o2.getDistanceFromEnemyMainBase() ? -1 : o1.getDistanceFromEnemyMainBase() > o2.getDistanceFromEnemyMainBase() ? 1:0;
        }  
	}

	public void printUintData(){
		
		int UnitAndUnitInfoMapSize = InformationManager.Instance().getUnitData(myPlayer).getUnitAndUnitInfoMap().size();
		Iterator<Integer> it = InformationManager.Instance().getUnitData(myPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		while (it.hasNext()) {
			UnitInfo ui = InformationManager.Instance().getUnitData(myPlayer).getUnitAndUnitInfoMap().get(it.next());
			
			if(ui.getType() != UnitType.Zerg_Zergling) continue;
			
			if (ui.getType().canAttack()){
				if(ui.getType().isWorker()) continue;
				if(DEBUG) System.out.println("["+ UnitAndUnitInfoMapSize + "]" + ui.getLastPosition() + ui.getUnitID() + " " + ui.getType() + " " + ui.getDistanceFromSelfMainBase() + " " + ui.getDistanceFromEnemyMainBase());
			}
		}
		//System.out.println();
	}
	
	public void updateKCBaseInfo(){
		
		// 2초에 1번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 24 * 2 != 0) return;
				
		KCBaseInfoManager.Instance().update();
		//if(Config.DEBUG){
		//	System.out.println(KCBaseInfoManager.Instance().printKCBaseList());
		//}
	}
	
	public void controlAdvancedDefenceBuildingCombat() {
		
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) return;
		if (isInitialBuildOrderFinished == false) return;
		
		for(Unit unit : myPlayer.getUnits()){
			
			// sc76.choi 크립 클로니를 성큰으로 변태 시킨다.
			if(unit.getType() == UnitType.Zerg_Creep_Colony){
				
				Region creepRegion = BWTA.getRegion(unit.getPosition());
				
				// sc76.choi 해당지역에 (공중)적이 있고, 크립 상태면, 스포어 클로니으로 변태한다.
				if(existUnitTypeInRegion(enemyPlayer, (UnitType)null, creepRegion, false, true)){
					
					if(myPlayer.allUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Spore_Colony) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Spore_Colony, null) == 0){
							
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spore_Colony, unit.getTilePosition(), false);
					}
				}
				// sc76.choi 해당지역에 (지상)적이 있고, 크립 상태면, 스포어 클로니으로 변태한다.
				else if(existUnitTypeInRegion(enemyPlayer, (UnitType)null, creepRegion, false, false)){
					
					if(BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Sunken_Colony) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Sunken_Colony, null) == 0){
						
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Sunken_Colony, unit.getTilePosition(), false);
					}
					
				}
				
				// sc76.choi 본진에 성큰이 있으면, 짓는 것을 취소한다.
				if(enemyRace == Race.Protoss && creepRegion == BWTA.getRegion(myMainBaseLocation.getPosition())){
					
					//if(existUnitTypeInRegion(myPlayer, UnitType.Zerg_Sunken_Colony, BWTA.getRegion(myMainBaseLocation.getPosition()), false, false)){
					if(getCountUnitTypeInPosition(myPlayer, UnitType.Zerg_Sunken_Colony, myMainBaseLocation.getPosition(), Config.TILE_SIZE* 15) >= 2){
						if(unit.canCancelMorph()){
							unit.cancelMorph();
							if(DEBUG) System.out.println("cancel Zerg_Sunken_Colony!!! " + unit.getID());
						}
					}
				}
			}
			
			// sc76.choi 근처에 있는 약한 유닛을 찾아 때린다.
			if(unit.getType() ==UnitType.Zerg_Sunken_Colony || unit.getType() ==UnitType.Zerg_Spore_Colony){
				Unit targetWeakUnit = getEnemyWeakUnitsInRadius(unit.getUnitsInRadius(Config.TILE_SIZE*6));
				if(targetWeakUnit != null){
					commandUtil.attackUnit(unit, targetWeakUnit);
				}
			}
		}
	}
	
	/// 전반적인 전투 로직 을 갖고 전투를 수행합니다
	public void executeCombat() {

		//////////////////////////////////////////////////////////////////////////
		// 초기 빌드오더 타임까지는 본진을 방어 하거나, 특공대 임무를 수행합니다.
		// ///////////////////////////////////////////////////////////////////////
		if (combatState == CombatState.initialMode) {

			/// 아군 공격유닛 들에게 방어를 지시합니다
			commandMyCombatUnitToInitial();
			
			/// 방어 모드로 전환할 때인지 여부를 판단합니다			
			// sc76.choi intial 모드에서 defence모드로 처음 변경되는 시점
			if(myPlayer.allUnitCount(UnitType.Zerg_Spawning_Pool) > 0 
				|| myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0){
				combatState = CombatState.defenseMode;
			}
		}
		
		//////////////////////////////////////////////////////////////////////////
		// 공격을 시작할만한 상황이 되기 전까지는 방어를 합니다
		// ///////////////////////////////////////////////////////////////////////
		if (combatState == CombatState.defenseMode) {

			// sc76.choi defenseMode 모드에서 크립 콜로니가 하나 완성이 되었으면, 
			// sc76.choi 건물간격을 변경 해 준다.
			// sc76.choi TODO 어떤 수치가 가장 효율적인지는 판단해야 한다.
//			if(Config.BuildingDefenseTowerSpacing == 1 
//				 && (myPlayer.allUnitCount(UnitType.Zerg_Creep_Colony) > 0 || 
//					 myPlayer.allUnitCount(UnitType.Zerg_Sunken_Colony) > 0)){
//				System.out.println("adjust BuildingDefenseTowerSpacing 0 - > 1");
//				Config.BuildingDefenseTowerSpacing = 1;
//			}
						
			/// 아군 공격유닛 들에게 방어를 지시합니다
			commandMyCombatUnitToDefense();

			/// 공격 모드로 전환할 때인지 여부를 판단합니다			
			if (isTimeToStartAttack() ) {
				combatState = CombatState.attackStarted;
			}
		}
		//////////////////////////////////////////////////////////////////////////
		// 공격을 시작한 후에는 공격을 계속 실행하다가, 거의 적군 기지를 파괴하면 Eliminate 시키기를 합니다 
		//////////////////////////////////////////////////////////////////////////
		else if (combatState == CombatState.attackStarted) {

			/// 아군 공격유닛 들에게 공격을 지시합니다
			commandMyCombatUnitToAttack();

			/// 방어 모드로 전환할 때인지 여부를 판단합니다			
			if (isTimeToStartDefense()) {
				combatState = CombatState.defenseMode;
			}	
			
			/// 적군을 Eliminate 시키는 모드로 전환할지 여부를 판단합니다 
			if (isTimeToStartElimination() ) {
				combatState = CombatState.eliminateEnemy;
			}
		}
		//////////////////////////////////////////////////////////////////////////
		// 적군을 Eliminate 시키도록 아군 공격 유닛들에게 지시합니다
		//////////////////////////////////////////////////////////////////////////
		else if (combatState == CombatState.eliminateEnemy) {
			commandMyCombatUnitToEliminate();

			// sc76.choi eliminate하다 역공을 당하면 다시 방어 모드로 전환해야 한다. 이후 재정비 후 공격			
			if (isTimeToStartDefense()) {
				combatState = CombatState.defenseMode;
			}	
		}
	}
	
	// sc76.choi 저글링 정찰
	// sc76.choi 정찰중, 오버로드 어택하는 마린 한마리 정도는 어택을 해보자.
	public void executeRandomScout() {
		
		if (MyBotModule.Broodwar.getFrameCount() % (24*5) != 0) return;
		
		if(enemyMainBaseLocation == null) return;
		
		// sc76.choi 일꾼 숫자가 적으면 실행하지 않는다.
		if(WorkerManager.Instance().getNumMineralWorkers() <= 10){
			return;
		}
		
		Position rPosition = getRandomPosition();
		
		// sc76.choi 멀티 할 곳을 한번씩은 가 본다.
		if (MyBotModule.Broodwar.getFrameCount() % (24*50) == 0){
			if(bestMultiLocation != null){
				rPosition = new Position(bestMultiLocation.getX(), bestMultiLocation.getY() + Config.TILE_SIZE*2);
			}else{
				rPosition = getRandomPosition();
			}
		}else{
			rPosition = getRandomPosition();
		}
		
		int scoutSize = myCombatUnitType1ScoutList.size();
		if(scoutSize <= 0) return;
		
		Unit scoutUnit = myCombatUnitType1ScoutList.get(0);
		if(!commandUtil.IsValidUnit(scoutUnit)) return;
		
		// sc76.choi 이동 공간 설정, 나의 본진과 적의 본진, 그리고 가운데 지역은 가지 않는다.
		if(rPosition.getDistance(enemyMainBaseLocation) > Config.TILE_SIZE*35
			&& rPosition.getDistance(myMainBaseLocation) > Config.TILE_SIZE*35){
			if (scoutUnit.isIdle()) {
				
				// sc76.choi 중앙이 아니면
				if(BWTA.getRegion(rPosition) != BWTA.getRegion(new Position(2000, 2000))
					&& (enemyRace == Race.Protoss || enemyRace == Race.Zerg)){
					return;
				}
					
				// sc76.choi 정찰 저글링이 moving으로 이동할지, 어택으로 이동할지 결정
				boolean isOnlyMoving = false;
				for(Unit enemyUnit : scoutUnit.getUnitsInRadius(Config.TILE_SIZE*5)){
					if(enemyUnit.getType().canAttack()){
						if(enemyUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) continue;
						isOnlyMoving = true;
					}
				}
				
				// sc76.choi 적이 있으면 그냥 miving으로 이동
				if(isOnlyMoving){
					commandUtil.move(scoutUnit, rPosition);
				}else{
					commandUtil.attackMove(scoutUnit, rPosition);
				}
				
			}
		}
	}

	// sc76.choi 저글링 정찰
		// sc76.choi 정찰중, 오버로드 어택하는 마린 한마리 정도는 어택을 해보자.
	public void executeRandomScout2() {
		
		if (MyBotModule.Broodwar.getFrameCount() % (24*5) != 0) return;
		
		if(enemyMainBaseLocation == null) return;
		
		// sc76.choi 일꾼 숫자가 적으면 실행하지 않는다.
		if(WorkerManager.Instance().getNumMineralWorkers() <= 10){
			return;
		}
		
		Position rPosition = getRandomPosition();
		// sc76.choi 멀티 할 곳을 한번씩은 가 본다.
		if (MyBotModule.Broodwar.getFrameCount() % (24*30) == 0){
			if(bestMultiLocation != null){
				rPosition = new Position(bestMultiLocation.getX(), bestMultiLocation.getY() + Config.TILE_SIZE*2);
			}else{
				rPosition = getRandomPosition();
			}
		}else{
			rPosition = getRandomPosition();
		}
		
		int scoutSize = myCombatUnitType1ScoutList2.size();
		if(scoutSize <= 0) return;
		
		Unit scoutUnit = myCombatUnitType1ScoutList2.get(0);
		if(!commandUtil.IsValidUnit(scoutUnit)) return;
		
		// sc76.choi 이동 공간 설정, 나의 본진과 적의 본진, 그리고 가운데 지역은 가지 않는다.
		if(rPosition.getDistance(enemyMainBaseLocation) > Config.TILE_SIZE*35
			&& rPosition.getDistance(myMainBaseLocation) > Config.TILE_SIZE*35){
			if (scoutUnit.isIdle()) {
				// sc76.choi 중앙이 아니면
				if(BWTA.getRegion(rPosition) != BWTA.getRegion(new Position(2000, 2000))
					&& (enemyRace == Race.Protoss || enemyRace == Race.Zerg)){
					return;
				}
					
				// sc76.choi 정찰 저글링이 moving으로 이동할지, 어택으로 이동할지 결정
				boolean isOnlyMoving = false;
				for(Unit enemyUnit : scoutUnit.getUnitsInRadius(Config.TILE_SIZE*5)){
					if(enemyUnit.getType().canAttack()){
						isOnlyMoving = true;
					}
				}
				
				// sc76.choi 적이 있으면 그냥 miving으로 이동
				if(isOnlyMoving){
					commandUtil.move(scoutUnit, rPosition);
				}else{
					commandUtil.attackMove(scoutUnit, rPosition);
				}

			}
		}
	}
	
	// sc76.choi 테란의 경우 멀티 시점에, 히드라 두마리 정도 멀티 방어.
	public void executeMultiDefenceHydralisk() {
		
		if (MyBotModule.Broodwar.getFrameCount() % (24*1) != 0) return;
		
		if(enemyMainBaseLocation == null) return;
		if(bestMultiLocation1 == null) return;
		
		//if(InformationManager.Instance().getTotalHatcheryCount() <= 3) return;
		
		Position dPosition = new Position(bestMultiLocation1.getX(), bestMultiLocation1.getY() + Config.TILE_SIZE*3);
		
		int defenceSize = myCombatUnitType2MultiDefenceList.size();
		if(defenceSize <= 0) return;
		
		for(Unit denfenceUnit : myCombatUnitType2MultiDefenceList){
			commandUtil.attackMove(denfenceUnit, dPosition);
		}
	}
	
	// sc76.choi 테란의 경우 멀티 시점에, 히드라 두마리 정도 멀티 방어.
	public void executeMultiDefenceHydralisk2() {
		
		if (MyBotModule.Broodwar.getFrameCount() % (24*2) != 0) return;
		
		if(enemyMainBaseLocation == null) return;
		if(bestMultiLocation2 == null) return;
		
		//if(InformationManager.Instance().getTotalHatcheryCount() <= 4) return;
		
		Position dPosition = new Position(bestMultiLocation2.getX(), bestMultiLocation2.getY() + Config.TILE_SIZE*3);
		
		int defenceSize = myCombatUnitType2MultiDefenceList2.size();
		if(defenceSize <= 0) return;
		
		for(Unit denfenceUnit : myCombatUnitType2MultiDefenceList2){
			// sc76.choi 미리가는걸 방지
			if(selfAvailableMinerals > 260){
				commandUtil.attackMove(denfenceUnit, dPosition);
			}
		}
	}
	
	boolean bTimeToAirDefence;
	public void buildAirDefenceUnit(){
		
		// 1초에 2번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 12 != 0) return;
		
		// sc76.choi 챔버가 없으면 실행하지 않는다.
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) <= 0){
			return;
		}
		
		if(bTimeToAirDefence == false){
			bTimeToAirDefence = isTimeToAirDefence();
		}
		
		if(bTimeToAirDefence){
			
			// sc76.choi 자리 부족으로 lock 유발 방지.
			Config.BuildingDefenseTowerSpacing = 1;
			
			// sc76.choi 본진에 먼저 긴급하게 스포어 클로니를 건설한다.
			excuteUrgentAirDefenceConstructionInBaseLocation(myMainBaseLocation);
			
			// sc76.choi 각 지역에 공중 공격 방어를 위해 실행
			List<BaseLocation> selfBaseLocations = InformationManager.Instance().getOccupiedBaseLocations(myPlayer);
			Iterator<BaseLocation> it2 = selfBaseLocations.iterator();
			
			while (it2.hasNext()) {
				BaseLocation selfBaseLocation = it2.next();
				
				// sc76.choi 나의 본진
				if(selfBaseLocation.equals(myMainBaseLocation)
					|| (selfBaseLocation.getX() == myMainBaseLocation.getX() && selfBaseLocation.getY() == myMainBaseLocation.getY())) {
					continue;
				}
				
				if(selfAvailableMinerals >= 200){
					//System.out.println("buildAirDefenceUnit selfRegion : " + selfRegion.getPoint().toTilePosition());
					
					// creep colony가 없으면
					if(InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Hatchery) == true
							&& InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Creep_Colony) == false
							&& InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Spore_Colony) == false
							&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Creep_Colony) == 0
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Creep_Colony, null) == 0){
						
						if(enemyRace == Race.Zerg){
							//System.out.println("buildAirDefenceUnit selfRegion : Creep_Colony");
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Creep_Colony, selfBaseLocation.getTilePosition(), false);
						}else{
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, selfBaseLocation.getTilePosition(), true);
						}
					}
	
					// creep colony가 있고, spore colony가 없으면
					if(InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Hatchery) == true
						&& InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Creep_Colony) == true
						&& InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Spore_Colony) == false
						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Spore_Colony) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Spore_Colony, null) == 0){
						
						if(enemyRace == Race.Zerg){
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spore_Colony, selfBaseLocation.getTilePosition(), false);						
						}else{
							BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spore_Colony, selfBaseLocation.getTilePosition(), false);
						}
					}
				}
			}
		}
	}
	
	// sc76.choi 재건 한다.
	public void executeMaintenance(){
		
		// 1초에 1번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 24*2 != 0) return;
		
		if(isInitialBuildOrderFinished == true){
			
			int countSelfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer).size();
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// sc76.choi 초기 빌드가 끝나고, 앞마당이 파괴되었으면 재건한다.
			boolean existHatcheryInMyFirstExpansion = existUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
			
			if(existHatcheryInMyFirstExpansion == false){
				
				// 앞마당에 적군만 있으면 방어 타워를 본진에 짓는다.
				boolean isEnemyUnitsInMyFirstExpansion = existUnitTypeInRegion(enemyPlayer, null, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
				
				// 앞마당에 적군이 있으면
				if(isEnemyUnitsInMyFirstExpansion == true){
					if(DEBUG) System.out.println("executeMaintenance hatchery cancle!!");
					if(DEBUG) System.out.println();
					return;
				}

				// 앞마당 헤처리 건설
				if(myPlayer.incompleteUnitCount(UnitType.Zerg_Hatchery) == 0
					&& selfMinerals >= 400
					&& WorkerManager.Instance().getNumMineralWorkers() > 5
					&& myCombatUnitType1List.size() >= necessaryNumberOfCombatUnitType1
					&& myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2
					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hatchery) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0){
					
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
							BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, false);
				}
			}
		
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// sc76.choi 일꾼양이 7마리 이하이면, 가스 채취를 중단하고, 미네랄을 캔다.
			if(WorkerManager.Instance().getWorkerData().getNumWorkers() <= 7){
				for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Gas){
						WorkerManager.Instance().setMineralWorker(worker);
					}
				}
			}

		
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// sc76.choi 스포닝 풀이 파괴되었을 때, 재건한다.
			if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) <= 0
//					&& WorkerManager.Instance().getNumMineralWorkers() > 5
					&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
					&& myPlayer.allUnitCount(UnitType.Zerg_Spawning_Pool) == 0
					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Spawning_Pool) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Spawning_Pool, null) == 0) 
			{
					if(myOccupiedBaseLocations >= 2 && bestMultiLocation != null){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spawning_Pool, 
								bestMultiLocation.getTilePosition(), true);
					}else{
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spawning_Pool, true);
					}
			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// sc76.choi 히드라덴이 파괴되었을 때, 재건한다.
			if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
//					&& WorkerManager.Instance().getNumMineralWorkers() > 5
					&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) <= 0
					&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
					&& myPlayer.allUnitCount(UnitType.Zerg_Hydralisk_Den) == 0
					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hydralisk_Den) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hydralisk_Den, null) == 0) 
			{
				
					if(myOccupiedBaseLocations >= 2 && bestMultiLocation != null){
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hydralisk_Den, 
								bestMultiLocation.getTilePosition(), false);
					}else{
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hydralisk_Den, false);
					}
			}	
			
			// 빠른 뮤탈이면 바로 히드라덴 를 건설한다.
			if(countSelfRegions == 1 && buildState == BuildState.fastMutalisk_Z
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) <= 0
				&& myPlayer.allUnitCount(UnitType.Zerg_Hydralisk_Den) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hydralisk_Den) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hydralisk_Den, null) == 0){
				
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hydralisk_Den, false);
			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// sc76.choi 쳄버가 파괴되었을 때, 재건한다.
			if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
					&& WorkerManager.Instance().getNumMineralWorkers() > 7
					&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) > 8
					&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) <= 0
					&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
					&& myPlayer.allUnitCount(UnitType.Zerg_Evolution_Chamber) == 0
					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Evolution_Chamber) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Evolution_Chamber, null) == 0) 
			{
					// sc76.choi Hive 진행 중이면 Lair를 또 가면 안된다.
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Evolution_Chamber, false);
			}
			
			// 빠른 뮤탈이면 바로 쳄버 를 건설한다.
			if(countSelfRegions == 1 
				&& buildState == BuildState.fastMutalisk_Z
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
				&& myPlayer.allUnitCount(UnitType.Zerg_Evolution_Chamber) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Evolution_Chamber) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Evolution_Chamber, null) == 0){
				
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Evolution_Chamber, false);
			}
			
			// 빠른 다크면 바로 쳄버 를 건설한다.
			if(buildState == BuildState.darkTemplar_P
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
				&& myPlayer.allUnitCount(UnitType.Zerg_Evolution_Chamber) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Evolution_Chamber) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Evolution_Chamber, null) == 0){
				
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Evolution_Chamber, false);
			}
			
			// 첫번째 멀티가 깨지면 다시 재건한다.
			if(bestMultiLocation != null
				&& WorkerManager.Instance().getNumMineralWorkers() > 5
				&& InformationManager.Instance().getTotalHatcheryCount() >= 4
				&& InformationManager.Instance().getTotalHatcheryCount() <= Config.numberOfMyCombatUnitTrainingBuilding
				&& InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(bestMultiLocation.getPosition()), myPlayer, UnitType.Zerg_Hatchery) == false
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hatchery) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0){
				
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hatchery, 
						bestMultiLocation.getTilePosition(), false);
				
			}
		
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// sc76.choi TODO 각 본진별로 헤처리, 가스가 없으면 가스를 재건한다.
			
			// 빠른 뮤탈이면 바로 가스를 건설한다.
			if(countSelfRegions == 1 && buildState == BuildState.fastMutalisk_Z
				&& myPlayer.allUnitCount(UnitType.Zerg_Extractor) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Extractor) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Extractor, null) == 0){
				
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Extractor, false);
			}
			
			if(countSelfRegions >= 2){
				
				//Set<Region> selfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer);
				List<BaseLocation>  selfBaseLocations = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
				Iterator<BaseLocation> it1 = selfBaseLocations.iterator();
				while (it1.hasNext()) {
					//Region selfRegion = it1.next();
					BaseLocation selfBaseLocation = it1.next();
	
					//if(selfBaseLocation == myMainBaseLocation) continue;
					//if(selfBaseLocation == myFirstExpansionLocation) continue;
					
					// sc76.choi 멀티 지역에 가스는 해당 지역에 일꾼이 충분할 때 건설한다.
					int workercountInRegion = getCountUnitTypeInRegion(myPlayer, UnitType.Zerg_Drone, selfBaseLocation.getRegion(), false, false);
					
					// 해처리만 있고, 가스가 없으면 가스를 건설
					if(InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Hatchery) == true
							&& workercountInRegion >= 7
							&& InformationManager.Instance().existsPlayerBuildingInRegion(selfBaseLocation.getRegion(), myPlayer, UnitType.Zerg_Extractor) == false
							&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Extractor) == 0
							&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Extractor, null) == 0
					){
						
						if(selfBaseLocation.getGeysers().isEmpty() == false && selfBaseLocation.getGeysers().size() > 0){ 
							
							TilePosition refineryTilePosition = ConstructionPlaceFinder.Instance().getRefineryPositionNear(selfBaseLocation.getTilePosition());
							if(selfBaseLocation.getGeysers().size() == 1){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Extractor, refineryTilePosition, false);
							}else if(selfBaseLocation.getGeysers().size() == 2){
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Extractor, refineryTilePosition, false);
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Extractor, refineryTilePosition, false);
							}else{
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Extractor, false);
							}
						}
						
					}
	
//					// 가스만 있고, 해처리가 없으면 해처리를 건설
//					if(InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Extractor) == true
//						&& InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Hatchery) == false
//						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hatchery) == 0
//						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0){
//						
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hatchery, selfRegion.getCenter().toTilePosition(), false);
//						
//					}
				}
			}

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// sc76.choi 레어 두번 가는 오류 해결
			int remainingBuildTime = 0;
			Unit cancleLairUnit = null;
			if(myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) > 1){
				//System.out.println("two lair!!!");
			
				for(Unit unit : myPlayer.getUnits()){
					if(unit.isMorphing() && unit.getType() == UnitType.Zerg_Lair){
						if(remainingBuildTime < unit.getRemainingBuildTime()){
							remainingBuildTime = unit.getRemainingBuildTime();
							cancleLairUnit = unit;
						}
					}
				}
				
				if(cancleLairUnit.canCancelMorph()){
					cancleLairUnit.cancelMorph();
					//System.out.println("cancel lair!!! " + cancleLairUnit.getID());
				}
			}
			
			// sc76.choi 버그, 락이 걸린다. 크립클로리가 없는데 빌드 상단에 성큰이나, 스포어가 존재하면 제거한다.
			if (MyBotModule.Broodwar.getFrameCount() % 24*5 == 0){
				if(myPlayer.completedUnitCount(UnitType.Zerg_Creep_Colony) <= 0 ||
						myPlayer.allUnitCount(UnitType.Zerg_Creep_Colony) <= 0){
					
					if(BuildManager.Instance().getBuildQueue().getQueue().size() > 0){
						
						BuildOrderItem bi = BuildManager.Instance().getBuildQueue().getHighestPriorityItem();
						
						if(bi == null) return;
						
						if(bi.metaType.getUnitType() ==  UnitType.Zerg_Sunken_Colony
								|| bi.metaType.getUnitType() ==  UnitType.Zerg_Spore_Colony){
							
							if(DEBUG) System.out.println("removeHighestPriorityItem removeHighestPriorityItem removeHighestPriorityItem !!!");
							if(DEBUG) System.out.println("removeHighestPriorityItem removeHighestPriorityItem removeHighestPriorityItem !!!");
							if(DEBUG) System.out.println("removeHighestPriorityItem removeHighestPriorityItem removeHighestPriorityItem !!!");
							if(DEBUG) System.out.println("removeHighestPriorityItem removeHighestPriorityItem removeHighestPriorityItem !!!");
							
							BuildManager.Instance().getBuildQueue().removeHighestPriorityItem();
							
						}
					}
				}
			}
		} // ininitial check
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi 건설중인 건물이나, egg가 공격 당하고 있으면 취소한다.
		for(Unit myUnit : myPlayer.getUnits()){
			//if((myUnit.getBuildType() == UnitType.Zerg_Lurker_Egg || myUnit.getBuildType().isBuilding())){
			
			if (myUnit.isTraining() == true || myUnit.isMorphing() == true) {
			
				
				if(myUnit.isUnderAttack() == true){
					
//					if(DEBUG) System.out.println("isUnderAttack!!!!");
					
					double remainHitPoint = (myUnit.getHitPoints()*1.0)/(myUnit.getType().maxHitPoints()*1.0);
					if(remainHitPoint <= .1){
						if(myUnit.canCancelMorph()){
							myUnit.cancelMorph();
						}
					}
				}
				
			}
		}
	}
	
	// sc76.choi 일꾼을 rebalancing 한다.
	public void executeRebalancingWorker(){
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi TODO 각 본진별로 헤처리, 가스가 없으면 가스를 재건한다.
		int countSelfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer).size();
		if(countSelfRegions >= 3){
			Set<Region> selfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer);
			
			Iterator<Region> it1 = selfRegions.iterator();
			while (it1.hasNext()) {
				Region selfRegion = it1.next();
				
				if(selfRegion == myMainBaseLocation.getRegion()) continue;
				if(selfRegion == myFirstExpansionLocation.getRegion()) continue;
				
				Unit chooseMineral = null;
				for(Unit mineral : BWTA.getNearestBaseLocation(selfRegion.getCenter()).getMinerals()){
					chooseMineral = mineral;
					break;
				}
				
				if(chooseMineral != null) break;
			}
			
			
			List<BaseLocation>  selfBaseLocations = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
			Iterator<BaseLocation> it2 = selfBaseLocations.iterator();
			while (it2.hasNext()) {
				
				BaseLocation selfBaseLocation = it2.next();
				
				if(selfBaseLocation == myMainBaseLocation 
					 || selfBaseLocation == myFirstExpansionLocation){
					
					
				}
			}
			
		}
	}
	
	/**
	 * 방어 판단을 위한 방어 수
	 * @author csc
	 * @return
	 */	
	boolean isNecessaryNumberOfDefencedUnitType(){ 
		return (myCombatUnitType1List.size() >= necessaryNumberOfDefenceUnitType1
				|| myCombatUnitType2List.size() >= necessaryNumberOfDefenceUnitType2
				|| myCombatUnitType3List.size() >= necessaryNumberOfDefenceUnitType3);
	}
	
	/**
	 * 공격 판단을 위한 공격 수
	 * @author csc
	 * @return
	 */
//	ArrayList<Unit> myCombatUnitType1List = new ArrayList<Unit>(); // 저글링      
//	ArrayList<Unit> myCombatUnitType2List = new ArrayList<Unit>(); // 히드라
//	ArrayList<Unit> myCombatUnitType3List = new ArrayList<Unit>(); // 럴커
//	ArrayList<Unit> myCombatUnitType4List = new ArrayList<Unit>(); // 뮤탈      
//	ArrayList<Unit> myCombatUnitType5List = new ArrayList<Unit>(); // 울트라      
//	ArrayList<Unit> myCombatUnitType6List = new ArrayList<Unit>(); // 가디언      
//	ArrayList<Unit> mySpecialUnitType1List = new ArrayList<Unit>(); // 오버로드       
//	ArrayList<Unit> mySpecialUnitType2List = new ArrayList<Unit>(); // 디파일러
//	ArrayList<Unit> mySpecialUnitType3List = new ArrayList<Unit>(); // 스커지
//	ArrayList<Unit> mySpecialUnitType4List = new ArrayList<Unit>(); // 퀸
	boolean isNecessaryNumberOfCombatUnitType(){
		
		boolean isNecessaryNumberOfCombatUnitType = false;
		
		// sc76.choi 테란일때...
		if(enemyRace == Race.Terran){
			// sc76.choi 초기 빌더오더 중일때, 한번 저글링 러쉬를 가기 위해
			if(isInitialBuildOrderFinished == false){
				
				// sc76.choi defence저글링수만 초반 러쉬를 한번 간다.
				if((myCombatUnitType1List.size() + myCombatUnitType1ScoutList.size() + myCombatUnitType1ScoutList2.size()) 
					>= necessaryNumberOfCombatUnitType1){
//				if(myKilledCombatUnitCount1 < 6){
					isNecessaryNumberOfCombatUnitType = true;
				}
//				}
						
			}else{
				isNecessaryNumberOfCombatUnitType = 
					(
						// 히드라
						(getCountCombatType2() >= necessaryNumberOfCombatUnitType2)  

						// 저글링, 럴커
						|| 
						(getCountCombatType1() >= necessaryNumberOfCombatUnitType1      
						&& myCombatUnitType3List.size() >= necessaryNumberOfCombatUnitType3) 

						// 히드라, 럴커
						|| (getCountCombatType2() >= necessaryNumberOfCombatUnitType2      
						&& myCombatUnitType3List.size() >= necessaryNumberOfCombatUnitType3)

						// 뮤탈
						|| (myCombatUnitType4List.size() >= necessaryNumberOfCombatUnitType4)

						// 울트라
						|| (myCombatUnitType5List.size() >= necessaryNumberOfCombatUnitType5)

						// 가디언
						|| (myCombatUnitType6List.size() >= necessaryNumberOfCombatUnitType6)
					); 
			}
		}else if(enemyRace == Race.Zerg){
			// sc76.choi 초기 빌더오더 중일때, 한번 저글링 러쉬를 가기 위해

				
			// 럴커나, 다크템플러가 있으면 오버로드 속업 후, 공격에 가담
			if(buildState == BuildState.lurker_Z || buildState == BuildState.darkTemplar_P){
				if(myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) == 0){
					return false;
				}
			}
			
			// sc76.choi defence저글링수만 초반 러쉬를 한번 간다.
			if((myCombatUnitType1List.size() + myCombatUnitType1ScoutList.size() + myCombatUnitType1ScoutList2.size()) 
				>= necessaryNumberOfCombatUnitType1){
				isNecessaryNumberOfCombatUnitType = true;
			}
			
			// 저글링 + 히드라
			if((myCombatUnitType1List.size() + myCombatUnitType1ScoutList.size() + myCombatUnitType1ScoutList2.size())  
			    >= necessaryNumberOfCombatUnitType1
				&& myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2){
				isNecessaryNumberOfCombatUnitType = true;
			}
			// 히드라				
			else if(myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2){
				isNecessaryNumberOfCombatUnitType = true;
			}
			// 저글링 + 럴커				
			else if(myCombatUnitType1List.size() >= necessaryNumberOfDefenceUnitType1
					&& myCombatUnitType3List.size() >= necessaryNumberOfCombatUnitType3){
				isNecessaryNumberOfCombatUnitType = true;
			}

		}else if(Race.Protoss == enemyRace){
			// sc76.choi 초기 빌더오더 중일때, 한번 저글링 러쉬를 가기 위해
			if(isInitialBuildOrderFinished == false){
				
//				if(buildState == BuildState.blockDefence2Dragon8_P){
//					isNecessaryNumberOfCombatUnitType = false;
//				}
//				else{
//					// sc76.choi defence저글링수만 초반 러쉬를 한번 간다.
//					if(myCombatUnitType1List.size() + myCombatUnitType1ScoutList.size() + myCombatUnitType1ScoutList2.size() 
//						>= necessaryNumberOfDefenceUnitType1){
//						isNecessaryNumberOfCombatUnitType = true;
//					}
//				}
						
			}else{
				
				if(buildState == BuildState.blockDefence2Dragon8_P){
					if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
						&& myPlayer.getUpgradeLevel(UpgradeType.Grooved_Spines) == 0){
						return false;
					}
				}
				
				// 럴커나, 다크템플러가 있으면 오버로드 속업 후, 공격에 가담
				if(buildState == BuildState.lurker_Z || buildState == BuildState.darkTemplar_P){
					if(myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) == 0){
						return false;
					}
				}
				
				if(buildState != BuildState.blockDefence2Dragon8_P && getCountCombatType1() >= necessaryNumberOfCombatUnitType1){
					isNecessaryNumberOfCombatUnitType = true;
				}
				
				// 저글링 + 히드라
				if(getCountCombatType1() >= necessaryNumberOfCombatUnitType1
					&& getCountCombatType2() >= necessaryNumberOfCombatUnitType2){
					isNecessaryNumberOfCombatUnitType = true;
				}
				// 히드라				
				else if(getCountCombatType2() >= necessaryNumberOfCombatUnitType2){
					isNecessaryNumberOfCombatUnitType = true;
				}
				
//				// 저글링 + 럴커				
//				else if(getCountCombatType1() >= necessaryNumberOfCombatUnitType1
//						&& getCountCombatType3() >= necessaryNumberOfCombatUnitType3){
//					isNecessaryNumberOfCombatUnitType = true;
//				}
				
			}
		}
		// sc76.choi 랜덤 일 때...
		else {
			// sc76.choi 초기 빌더오더 중일때, 한번 저글링 러쉬를 가기 위해
			if(isInitialBuildOrderFinished == false){
				
				// sc76.choi defence저글링수만 초반 러쉬를 한번 간다.
				if(myCombatUnitType1List.size() + myCombatUnitType1ScoutList.size() + myCombatUnitType1ScoutList2.size() 
					>= necessaryNumberOfDefenceUnitType1){
					isNecessaryNumberOfCombatUnitType = true;
				}
						
			}else{
				
//				// 저글링
//				if(myCombatUnitType1List.size() >= necessaryNumberOfCombatUnitType1){
//					isNecessaryNumberOfCombatUnitType = true; 
//				}
//				
				// 히드라
				if(myCombatUnitType1List.size() >= necessaryNumberOfCombatUnitType1
					&& myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2){
					isNecessaryNumberOfCombatUnitType = true;
				}else if(myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2){
					isNecessaryNumberOfCombatUnitType = true;
				}
			}
		}
		
		return isNecessaryNumberOfCombatUnitType;
	}
	
	/// 공격 모드로 전환할 때인지 여부를 리턴합니다
	boolean isTimeToStartAttack(){

		// sc76.choi 최소방어 유닛보다 많고
		if (isNecessaryNumberOfDefencedUnitType())
		{
			// sc76.choi 공격 유닛수가 충족하면
			if (isNecessaryNumberOfCombatUnitType()) {
				
				countAttackMode++;
				return true;
			}
		}
		
		return false;
	}
	
	/// 방어 모드로 전환할 때인지 여부를 리턴합니다
	boolean isTimeToStartDefense() {
		
		boolean returnDefenceMode = false;
		
		// sc76.choi myCombatUnitType1 : 저글링 
		// sc76.choi myCombatUnitType2 : 히드라 
		// sc76.choi myCombatUnitType3 : 럴커
		// sc76.choi myCombatUnitType4 : 뮤탈 
		// sc76.choi myCombatUnitType5 : 울트라 
		// sc76.choi myCombatUnitType6 : 가디언 
		// sc76.choi AND 조건으로 체크 한다.
		
		if(enemyRace == Race.Terran){
			// sc76.choi 초기 빌더오더 중일때, 한번 저글링 러쉬를 가기 위해
			if(isInitialBuildOrderFinished == false){
				if (getCountCombatType1() < necessaryNumberOfDefenceUnitType1){ // 저글링
					countDefenceMode++;
					returnDefenceMode = true;
				}
			}else{
				if (getCountCombatType1() < necessaryNumberOfDefenceUnitType1 // 저글링
						&& getCountCombatType2() < necessaryNumberOfDefenceUnitType2   // 히드라
						&& getCountCombatType3() < necessaryNumberOfDefenceUnitType3   // 럴커
						){
					countDefenceMode++;
					returnDefenceMode = true;
				}
			}
		}else{
			// sc76.choi 초기 빌더오더 중일때, 한번 저글링 러쉬를 가기 위해			
			if(isInitialBuildOrderFinished == false){
				
				if (getCountCombatType1() < necessaryNumberOfDefenceUnitType1){ // 저글링
					countDefenceMode++;
					returnDefenceMode = true;
				}
				
			}else{
				
				// 럴커나, 다크템플러가 있으면 오버로드 속업 후, 공격에 가담
				if(buildState == BuildState.lurker_Z || buildState == BuildState.darkTemplar_P){
					if(myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) == 0){
						return true;
					}
				}
				
				// 저글링 + 히드라
				if(getCountCombatType1() < necessaryNumberOfDefenceUnitType1
					&& getCountCombatType2() < necessaryNumberOfDefenceUnitType2){
					countDefenceMode++;
					returnDefenceMode = true;
				}
//				// 히드라				
//				else if(myCombatUnitType2List.size() < necessaryNumberOfDefenceUnitType2){
//					countDefenceMode++;
//					returnDefenceMode = true;
//				}
//				// 저글링 + 럴커
//				else if(myCombatUnitType1List.size() < necessaryNumberOfDefenceUnitType1
//						&& myCombatUnitType3List.size() < necessaryNumberOfDefenceUnitType3){
//						countDefenceMode++;
//						returnDefenceMode = true;
//				}
			}
		}
		
		return returnDefenceMode;
	}

	/// 적군을 Eliminate 시키는 모드로 전환할지 여부를 리턴합니다
	// sc76.choi 잘 판단해야 한다. 경기가 끝나지 않을 수도 있다.
	boolean isTimeToStartElimination(){
		
		if(enemyMainBaseLocation == null){
			return false;
		}
		
		if (MyBotModule.Broodwar.getFrameCount() % 24*10 != 0) {
			return false;
		}
		
		// 적군 유닛을 많이 죽였고, 아군 서플라이가 100 을 넘었으면
		if (enemyKilledCombatUnitCount >= 10 && enemyKilledWorkerUnitCount >= 10) {

			// 적군 본진에 아군 유닛이 10 이상 도착했으면 거의 게임 끝난 것
			int myUnitCountAroundEnemyMainBaseLocation = 0;
			int enemyUnitCountAroundEnemyMainBaseLocation = 0;
			for(Unit unit : MyBotModule.Broodwar.getUnitsInRadius(enemyMainBaseLocation.getPosition(), Config.TILE_SIZE*10)) {
				if (unit.getPlayer() == myPlayer) {
					myUnitCountAroundEnemyMainBaseLocation ++;
				}
				
				if(myUnitCountAroundEnemyMainBaseLocation > 10){
					if(unit.getPlayer() == enemyPlayer) {
						enemyUnitCountAroundEnemyMainBaseLocation ++ ;
					}
				}
			}
			if (myUnitCountAroundEnemyMainBaseLocation > 10) {
				return true;
			}
		}
		
		int myUnitCountAroundEnemyMainBaseLocation2 = 0;
		int enemyUnitCountAroundEnemyMainBaseLocation2 = 0;
		for(Unit unit : MyBotModule.Broodwar.getUnitsInRadius(enemyMainBaseLocation.getPosition(), Config.TILE_SIZE*10)) {
			if (unit.getPlayer() == myPlayer) {
				myUnitCountAroundEnemyMainBaseLocation2 ++;
			}
		}
		
		if (myUnitCountAroundEnemyMainBaseLocation2 > 20) {
			return true;
		}
		
		// sc76.choi 일꾼만 20마리 죽여도 elimination한다.
		// sc76.choi 그리고, frame count가 28800 = 24 * 60 * 20 (즉 20분이 지났으면)이면 eliminate를 시작한다.
//		if (enemyKilledWorkerUnitCount >= 15 && MyBotModule.Broodwar.getFrameCount() >= 20000) {
//			return true;
//		}
//		
//		// sc76.choi 일꾼만 25마리 죽여도 elimination한다.
//		if (enemyKilledWorkerUnitCount >= 20){
//			return true;
//		}
		
		return false;
	}

	// sc76.choi 초반 빌드오더 타임까지의 초기 상태입니다.
	void commandMyCombatUnitToInitial(){

		// 아군 방어 건물이 세워져있는 위치
		Position myInitialBuildingPosition = null;
		switch (seedPositionStrategyOfMyInitialBuildingType) {
			case MainBaseLocation: myInitialBuildingPosition = myMainBaseLocation.getPosition(); break;
			case FirstChokePoint: myInitialBuildingPosition = myFirstChokePoint.getCenter(); break;
			case FirstExpansionLocation: myInitialBuildingPosition = myFirstExpansionLocation.getPosition(); break;
			case SecondChokePoint: myInitialBuildingPosition = mySecondChokePoint.getCenter(); break;
			default: myInitialBuildingPosition = myMainBaseLocation.getPosition(); break;
		}

		// 아군 공격유닛을 방어 건물이 세워져있는 위치로 배치시킵니다
		// 아군 공격유닛을 아군 방어 건물 뒤쪽에 배치시켰다가 적들이 방어 건물을 공격하기 시작했을 때 다함께 싸우게하면 더 좋을 것입니다
		// sc76.choi 단, 정찰 오버로드는 자기 할일이 있다.
		for (Unit unit : myAllCombatUnitList) {

			if (!commandUtil.IsValidUnit(unit)) continue;
			
			boolean hasCommanded = false;
			
			// sc76.choi 따로 명령 받은 오버로드는 후퇴에서 제외 합니다.
			if(unit.getType() == UnitType.Zerg_Overlord 
				&& (OverloadManager.Instance().getOverloadData().getJobCode(unit) == 'X' || 
				    OverloadManager.Instance().getOverloadData().getJobCode(unit) == 'S')){
				continue;
			}
			
			// 따로 명령 내린 적이 없으면, 방어 건물 주위로 이동시킨다. 단, 오버로드는 제외
			if (hasCommanded == false) {
				
				if (unit.isIdle()) {
					if (unit.canAttack()) {
						commandUtil.attackMove(unit, myInitialBuildingPosition);
					}
					else {
						commandUtil.move(unit, myInitialBuildingPosition);
					}
				}
			}
		}	
	}
	
	/// 아군 공격 유닛들에게 공격을 지시합니다 
	void commandMyCombatUnitToAttack(){
	
		// 최종 타겟은 적군의 Main BaseLocation
		boolean hasCommanded = false;
		
		// 저글링
		for(Unit unit : myCombatUnitType1List){
			hasCommanded = controlCombatUnitType1(unit);
		}
		
		// 히드라
		for(Unit unit : myCombatUnitType2List){
			hasCommanded = controlCombatUnitType2(unit);
		}
		
		// 럴커
		for(Unit unit : myCombatUnitType3List){
			hasCommanded = controlCombatUnitType3(unit);					
		}
		
		// 뮤탈은 별도로 컨트롤 한다.
		hasCommanded = controlCombatUnitType4((Unit)null);

		// 울트라
		for(Unit unit : myCombatUnitType5List){
			hasCommanded = controlCombatUnitType5(unit);					
		}				
		
		// 가디언
		for(Unit unit : myCombatUnitType6List){
			hasCommanded = controlCombatUnitType6(unit);					
		}		

		// 오버로드
		// sc76.choi 따로 명령 받은 오버로드는 공격에서 제외 합니다.	
		// System.out.println("commandMyCombatUnitToAttack() mySpecialUnitType1List : " + mySpecialUnitType1List.size());
		for(Unit unit : mySpecialUnitType1List){
			hasCommanded = controlSpecialUnitType1(unit);
		}
		
		// 디파일러
		for(Unit unit : mySpecialUnitType2List){
			hasCommanded = controlSpecialUnitType2(unit);
		}
		
		// 스커지
		for(Unit unit : mySpecialUnitType3List){				
			hasCommanded = controlSpecialUnitType3(unit);
		}
		
		// 퀸
		for(Unit unit : mySpecialUnitType4List){	
			hasCommanded = controlSpecialUnitType4(unit);
		}
		
		// 따로 명령 내린 적이 없으면, targetPosition 을 향해 공격 이동시킵니다
		if (hasCommanded == false) {
			for (Unit unit : myAllCombatUnitList) {
				if (unit.isIdle()) {
					if (unit.canAttack() ) { 
						
						commandUtil.attackMove(unit, TARGET_POSITION);
						
						hasCommanded = true;
					}
					else {
						
						// canAttack 기능이 없는 유닛타입 중 러커는 일반 공격유닛처럼 targetPosition 을 향해 이동시킵니다
						if (unit.getType() == UnitType.Zerg_Lurker){
							commandUtil.move(unit, TARGET_POSITION);
							hasCommanded = true;
						}
						// canAttack 기능이 없는 다른 유닛타입 (하이템플러, 옵저버, 사이언스베슬, 오버로드) 는
						// 따로 명령을 내린 적이 없으면 다른 공격유닛들과 동일하게 이동하도록 되어있습니다.
						else {
							commandUtil.move(unit, TARGET_POSITION);
							hasCommanded = true;
						}
					}
				}
			}
		}
	}

	/// 아군 공격유닛 들에게 방어를 지시합니다
	void commandMyCombatUnitToDefense(){

		// 아군 공격유닛을 방어 건물이 세워져있는 위치로 배치시킵니다
		// 아군 공격유닛을 아군 방어 건물 뒤쪽에 배치시켰다가 적들이 방어 건물을 공격하기 시작했을 때 다함께 싸우게하면 더 좋을 것입니다
		
		// sc76.choi 단, 정찰 오버로드는 자기 할일이 있다. myAllCombatUnitList에 오버로드 add 되는 기준을 확인 할 것
		//for (Unit unit : myAllCombatUnitList) {

//		if (!commandUtil.IsValidUnit(unit)) continue;
		
		boolean hasCommanded = false;

		// 저글링
		for(Unit unit : myCombatUnitType1List){
			hasCommanded = controlCombatUnitType1(unit);
		}
		
		// 저글링 (멀티지역)
		for(Unit unit : myCombatUnitType1ListAway){
			hasCommanded = controlCombatUnitType1Away(unit);
		}		

		// 히드라
		for(Unit unit : myCombatUnitType2List){
			hasCommanded = controlCombatUnitType2(unit);
		}

		// 히드라 (멀티지역)
		for(Unit unit : myCombatUnitType2ListAway){
			hasCommanded = controlCombatUnitType2Away(unit);
		}		

		// 럴커
		for(Unit unit : myCombatUnitType3List){
			hasCommanded = controlCombatUnitType3(unit);
		}
		
		// 럴커 (멀티지역)	
		for(Unit unit : myCombatUnitType3ListAway){
			hasCommanded = controlCombatUnitType3Away(unit);
		}		
		
		// 뮤탈은 별도로 컨트롤 한다.
		hasCommanded = controlCombatUnitType4((Unit)null);
		
		// 오버로드
		for(Unit unit : mySpecialUnitType1List){					
			hasCommanded = controlSpecialUnitType1(unit);
		}
		
		// 디파일러
		for(Unit unit : mySpecialUnitType2List){
			hasCommanded = controlSpecialUnitType2(unit);
		}
		
		// 스커지
		for(Unit unit : mySpecialUnitType3List){			
			hasCommanded = controlSpecialUnitType3(unit);
		}
		
		// 퀸
		for(Unit unit : mySpecialUnitType4List){				
			hasCommanded = controlSpecialUnitType4(unit);
		}
		
//		// 따로 명령 내린 적이 없으면, 방어 건물 주위로 이동시킨다. 단, 오버로드는 제외
//		if (hasCommanded == false) {
//			for (Unit unit : myAllCombatUnitList) {	
//				// sc76.choi 무조건 후퇴 해야 한다. unit.isIdle() 체크 제거
//				// sc76.choi 먼저 명령을 받은 유닛은 그 명령이 끝날때까지 수행하기 때문
//				//if (unit.isIdle()) {
//					if (unit.canAttack()) {
//						commandUtil.attackMove(unit, DEFENCE_POSITION);
//					}
//					else {
//						commandUtil.move(unit, DEFENCE_POSITION);
//					}
//				//}
//			}
//		}
//		//}	
	}
	
	/// 적군을 Eliminate 시키도록 아군 공격 유닛들에게 지시합니다
	void commandMyCombatUnitToEliminate(){
		
		if (enemyPlayer == null || enemyRace == Race.Unknown){
			return;
		}
		
//		Random random = new Random();
//		int mapHeight = MyBotModule.Broodwar.mapHeight();	// 128
//		int mapWidth = MyBotModule.Broodwar.mapWidth();		// 128
		
		// 아군 공격 유닛들로 하여금 적군의 남은 건물을 알고 있으면 그것을 공격하게 하고, 그렇지 않으면 맵 전체를 랜덤하게 돌아다니도록 합니다
		Unit targetEnemyBuilding = null;
				
		for(Unit enemyUnit : enemyPlayer.getUnits()) {
			if (enemyUnit == null || enemyUnit.exists() == false || enemyUnit.getHitPoints() < 0 ) continue;
			if (enemyUnit.getType().isBuilding()) {
				targetEnemyBuilding = enemyUnit;
				break;
			}
		}
		
		for(Unit unit : myAllCombatUnitList) {
			
			boolean hasCommanded = false;
			boolean hasCommandedSpecialType2 = false;
			
			if (unit.getType() == myCombatUnitType3) { // 럴커
				hasCommanded = controlCombatUnitType3(unit);					
			}
			
			if (unit.getType() == mySpecialUnitType1) {	// 오버로드				
				hasCommandedSpecialType2 = controlSpecialUnitType1(unit);
			}
			
			if (unit.getType() == mySpecialUnitType2) {	// 디파일러	
				hasCommanded = controlSpecialUnitType2(unit);
			}
			
			// 따로 명령 내린 적이 없으면, 적군의 남은 건물 혹은 랜덤 위치로 이동시킨다
			if (hasCommandedSpecialType2 == false) {

				if (unit.isIdle()) {

					Position targetPosition = null;
					if (targetEnemyBuilding != null) {
						targetPosition = targetEnemyBuilding.getPosition();
					}
					else {
						targetPosition = getRandomPosition();
					}

					if (unit.canAttack()) {
						commandUtil.attackMove(unit, targetPosition);
						hasCommanded = true;
					}
					else {
						// canAttack 기능이 없는 유닛타입 중 러커는 일반 공격유닛처럼 targetPosition 을 향해 이동시킵니다
						if (unit.getType() == UnitType.Zerg_Lurker){
							commandUtil.move(unit, targetPosition);
							hasCommanded = true;
						}
						// canAttack 기능이 없는 다른 유닛타입 (하이템플러, 옵저버, 사이언스베슬, 오버로드) 는
						// 따로 명령을 내린 적이 없으면 다른 공격유닛들과 동일하게 이동하도록 되어있습니다.
						else {
							commandUtil.move(unit, targetPosition);
							hasCommanded = true;
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * 일꾼도 주변에 적의 공격 유닛이 있다면 공격한다.
	 * 단점, 본진이나, 멀티 중 한곳만 실행이 가능할 것이다, 단순히 공격일꾼 3마리만 카운트 하기 때문에
	 * 이후에 여력이 있으면 수정하자!(확장하게 되면 고려를 해야 한다.)
	 * 
	 * @author sc76.choi
	 */
	void commandMyWorkerToAttack(){
		
		// 1초에 4번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) return;
		
		boolean existEnemyAroundWorker = false;
		double remainHitPoint = 0.0d;
		int distanceWorkerAround = Config.DISTANCE_WORKER_AROUND;
		int enemyUnitsInMyFirstExpansion = InformationManager.Instance().getCombatUnitCountInRegion(BWTA.getRegion(myFirstExpansionLocation.getPosition()), enemyPlayer);
		int myUnitsInMyFirstExpansion = InformationManager.Instance().getCombatUnitCountInRegion(BWTA.getRegion(myFirstExpansionLocation.getPosition()), myPlayer);
		
		// sc76.choi 공격 투입 일꾼 수 조정
		int countWorkersToCanAttak = Config.COUNT_WORKERS_CANATTACK;
		if(isInitialBuildOrderFinished == false) {
			countWorkersToCanAttak = 1; // 초기빌드까지는 1마리만 공격 가담
		}
		if(isInitialBuildOrderFinished == false && enemyUnitsInMyFirstExpansion >= 4 && myUnitsInMyFirstExpansion <= 4) {
			countWorkersToCanAttak = 5; // 앞마당 공격이면 10 마리
			distanceWorkerAround = Config.TILE_SIZE*30;
		}
		
//		System.out.println("myUnitsInMyFirstExpansion : " + myUnitsInMyFirstExpansion);
//		System.out.println("enemyUnitsInMyFirstExpansion : " + enemyUnitsInMyFirstExpansion);
//		System.out.println("countWorkersToCanAttak : " + countWorkersToCanAttak);
//		System.out.println();
		
		// sc76.choi 전체 아군 유닛의 일꾼을 loop
		for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
			
			// 정상유닛 체크
			if(commandUtil.IsValidSelfUnit(worker) == false){
				return; 
			}
			
			// 각 worker의 주변 DISTANCE_WORKER_AROUND을 살펴 본다.
			Iterator<Unit> iter = MyBotModule.Broodwar.getUnitsInRadius(worker.getPosition(), distanceWorkerAround).iterator();
			while(iter.hasNext()){
				Unit unit = iter.next();
				
				// 지상공격이 가능한 적군이면 CombatWorker으로 변경한다.
				if(commandUtil.IsValidEnemyGroundAttackUnit(unit)){
					
					Unit enemyUnit = unit;
					if(enemyUnit.getType().canAttack() == false) continue; // 공격력이 없으면, 메딕.. 
					existEnemyAroundWorker = true; // 적군 카운트 증

					// 상대 유닛이 공격 중이거나, 나의 일꾼이 공격을 받으면
					if(enemyUnit.getType().isWorker() == true 
						&& (enemyUnit.isStartingAttack() || enemyUnit.isAttacking() || enemyUnit.isConstructing()|| enemyUnit.isBeingConstructed() 
								|| worker.isUnderAttack())){
						//System.out.println("commandMyWorkerToAttack() 1");
						countWorkersToCanAttak = 2;
					}else if(enemyUnit.getType().isWorker() == true ){
						//System.out.println("commandMyWorkerToAttack() 2");
						countWorkersToCanAttak = 0;
					}else if(isInitialBuildOrderFinished == true && enemyUnitsInMyFirstExpansion >= 4) {
						//System.out.println("commandMyWorkerToAttack() 3");
						countWorkersToCanAttak = 5; // 앞마당 공격이면 5 마리
						distanceWorkerAround = Config.TILE_SIZE*30;
					}else if(buildState == BuildState.hardCoreZealot_P){
						//System.out.println("commandMyWorkerToAttack() 4");
						countWorkersToCanAttak = WorkerManager.Instance().getNumWorkers(); // 앞마당 공격이면 5 마리
						distanceWorkerAround = Config.TILE_SIZE*30;
					}
					
					// 부실한 공격 유닛은 해제
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
						remainHitPoint = (worker.getHitPoints()*1.0)/(40*1.0);
						if(remainHitPoint <= .2){
							WorkerManager.Instance().setIdleWorker(worker);
						}
					}
					
					// 이미 공격일꾼이 있으면 (일꾼 공격 합세는 2마리만 한다.)
					if(WorkerManager.Instance().getWorkerData().getNumCombatWorkers() >= countWorkersToCanAttak){
						break;
					}
					
					// 적군과 나와의 거리가 DISTANCE_WORKER_CANATTACK내에 있는,
					// worker(Job이 Mineral이고 체력이 온전한)를 상태를 combat으로 변경한다.
					if(worker.getDistance(enemyUnit) < distanceWorkerAround){
						
						
						// sc76.choi 공격 투입(미네럴, 가스 일꾼)
						if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Minerals
							|| WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Gas){
							
							if(commandUtil.IsValidSelfUnit(worker) == false){
								return; // 정상유닛 체크
							}
							
							remainHitPoint = (worker.getHitPoints()*1.0)/(worker.getType().maxHitPoints()*1.0);
							if(worker.isCarryingMinerals() || worker.isAttacking()) continue; // 미네랄 운반 일꾼은 제외
							if(remainHitPoint >= .4){
								WorkerManager.Instance().setCombatWorker(worker);
								if(WorkerManager.Instance().getWorkerData().getNumCombatWorkers() >= countWorkersToCanAttak) { 
									break;
								}
							}
						}
					}
				}
			} // while
		}
		
		// sc76.choi 적군이 없다면 idle로 변경하여, 다시 일을 할수 있게 한다.
		if(existEnemyAroundWorker == false){
			for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
				
				if(commandUtil.IsValidSelfUnit(worker) == false) {
					return;
				}
				
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
					WorkerManager.Instance().setIdleWorker(worker);
				}
			}
		}
		
		// sc76.choi 멀리나간 일꾼은 돌아오게 한다.
		if(myMainBaseLocation != null){
			for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
				
				if(commandUtil.IsValidSelfUnit(worker) == false) {
					return;
				}
				
				if(worker.getDistance(myMainBaseLocation.getPosition()) > Config.TILE_SIZE*25){
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
						WorkerManager.Instance().setIdleWorker(worker);
					}
				}
			}
		}
	}

	// sc76.choi 주변의 약한 유닛을 찾는다.
	// sc76.choi TODO 하이템플러의 타켓이면 주변을 대피 시켜야 한다.
	public Unit getEnemyWeakUnitsInRadius(List<Unit> units){
		Unit targetUnit = null;
		for(Unit enemyUnit : units){
			
			Unit spiderMine = null;
			if(enemyUnit.getType() == UnitType.Terran_Vulture_Spider_Mine){
				spiderMine = enemyUnit;
				return spiderMine;
			}
			
			Unit turret = null;
			if(enemyUnit.getType() == UnitType.Terran_Missile_Turret
				|| enemyUnit.getBuildType() == UnitType.Terran_Missile_Turret){
				turret = enemyUnit; 
				return turret;
			}
			

			if(enemyUnit.getPlayer() == enemyPlayer && enemyUnit.getType().canAttack() == true 
				&& spiderMine == null
				&& turret == null){
				
				double hitPointRate = 0.0d;
				hitPointRate = (enemyUnit.getHitPoints()*1.0) / (enemyUnit.getType().maxHitPoints()*1.0);
				
				//System.out.println("targetUnit 0 : " + enemyUnit.getID() + " " + hitPointRate + " " + enemyUnit.getHitPoints() + " " + enemyUnit.getType().maxHitPoints());
				
				
				if(targetUnit == null && hitPointRate < 0.40){
					targetUnit = enemyUnit;
					
					if(targetUnit != null){
						MyBotModule.Broodwar.drawCircleMap(targetUnit.getPosition(), 9, Color.White, true);
						//System.out.println("targetUnit : " + targetUnit.getID() + " " + hitPointRate);
					}
					return targetUnit;
				}
				
				// 마인이 있다면 마인을 선택한다.
				if(targetUnit == null){
					targetUnit = spiderMine;
				}
			}
		}
		return targetUnit;
	}

	/// 첫번째 일반공격 유닛 타입의 유닛에 대해 컨트롤 명령을 입력합니다(저글링)
	
	Unit enemyUnitForMainDefence;
	Unit enemyAirUnitForMainDefence;
	Unit enemyUnitForExpansionDefence;
	Unit enemyUnitForDefence;
	boolean isAttackPositionForInitialZergling = false;
	Unit attackTargetForInitialZerglingUnit = null;
	boolean controlCombatUnitType1(Unit unit) {
		
		boolean hasCommanded = false;
		
		if (!commandUtil.IsValidUnit(unit)) return true;
		
		if(combatState == CombatState.attackStarted){
			
			// sc76.choi 테란일때, 저글링 한번 공격 간다.
			if(isInitialBuildOrderFinished == false && enemyRace == Race.Terran){
				
				if(enemyMainBaseLocation == null){

					// sc76.choi 적진을 발견 못했을 때, 현재 정찰 오버로드가 정찰갈 베이스와 가장 가까운 베이스로 먼저 출발 한다.
					if(OverloadManager.Instance().getFirstScoutOverload() != null
						&& OverloadManager.Instance().getSecondScoutOverload() != null
						&& OverloadManager.Instance().getCurrentScoutTargetBaseLocation() != null
						&& OverloadManager.Instance().getSecondScoutTargetBaseLocation() != null){
 
						Position firstScoutOverloadPos = OverloadManager.Instance().getFirstScoutOverload().getPosition();
						Position secondScoutOverloadPos = OverloadManager.Instance().getSecondScoutOverload().getPosition();
						Position firstScoutBasePos = OverloadManager.Instance().getCurrentScoutTargetBaseLocation().getPosition();
						Position secondScoutBasePos = OverloadManager.Instance().getSecondScoutTargetBaseLocation().getPosition();
						
						double d1 = firstScoutOverloadPos.getDistance(firstScoutBasePos.getX(), firstScoutBasePos.getY());
						double d2 = secondScoutOverloadPos.getDistance(secondScoutBasePos.getX(), secondScoutBasePos.getY());
						
						if(d1 < d2){
							commandUtil.move(unit, firstScoutBasePos);
						}else{
							commandUtil.move(unit, secondScoutBasePos);
						}
					}else{
						commandUtil.move(unit, new Position(2000, 2000));
					}
					
				}else{
					
					// sc76.choi 적진 가까이 가서 건설중인 일꾼을 강제 어택한다.
					// TODO 빌드를 좀더 빠르게 하면 더 효율적일 것이다.
//					if(BWTA.getRegion(unit.getPosition()) == BWTA.getRegion(enemyMainBaseLocation.getPosition())){
					if(unit.getDistance(enemyMainBaseLocation.getPosition()) >= Config.TILE_SIZE*6){
						
						if(isAttackPositionForInitialZergling == false){
							commandUtil.move(unit, enemyMainBaseLocation.getPosition());
						}

					}else{
						
						isAttackPositionForInitialZergling = true;
						
						//if(findAttackTargetForInitialZergling() == null){
						attackTargetForInitialZerglingUnit = getClosestUnitType(enemyPlayer, InformationManager.Instance().getWorkerType(enemyRace), enemyFirstExpansionLocation.getPosition());
						if(attackTargetForInitialZerglingUnit == null){
							commandUtil.attackMove(unit, enemyMainBaseLocation.getPosition());
							//unit.attack(findAttackTargetForInitialZergling());
						}else{
							if(DEBUG) System.out.println("attackTargetForInitialZerglingUnit : " + attackTargetForInitialZerglingUnit.getID());
							commandUtil.attackMove(unit, attackTargetForInitialZerglingUnit.getPosition());
						}

					}
				}
			}
			// sc76.choi 테란이 아니고, 초기 빌드가 끝났으면
			else{
				if(unit.isUnderStorm()){
					
					// sc76.choi 주변은 같이 후퇴한다.
					for(Unit aroundMyUnit : myCombatUnitType1List){
						if(aroundMyUnit.getType() == UnitType.Zerg_Zergling){
							commandUtil.move(unit, myMainBaseLocation.getPosition());
						}
					}
				}
				// sc76.choi 스톰을 맡고 있지 않으면
				else{
					//boolean canAttackNow = KCSimulationManager.Instance().canAttackNow(unit.getUnitsInRadius(Config.TILE_SIZE*6));
					KCSimulationResult canAttackNow2 = KCSimulationManager.Instance().canAttackNow2(unit.getUnitsInRadius(Config.TILE_SIZE*6));
					
					// sc76.choi 공격이 가능하면
					if(canAttackNow2.isCanAttackNow()){
						commandUtil.attackMove(unit, TARGET_POSITION_Z);
					}else{
						
						if((enemyRace == Race.Protoss || enemyRace == Race.Terran) 
							&& MyBotModule.Broodwar.getFrameCount() > 24*60*8){
							
						}else{
							// sc76.choi 주변은 같이 후퇴한다.
							for(Unit aroundMyUnit : unit.getUnitsInRadius(Config.TILE_SIZE*6)){
								if(aroundMyUnit.getType() == UnitType.Zerg_Zergling){
									commandUtil.move(unit, DEFENCE_POSITION);
									//commandUtil.move(unit, getCalcuatePosition(aroundMyUnit));
								}
							}
						}
					}
				}
			}
			
			hasCommanded = true;
			
		}
		// 방어 일때
		else if(combatState == CombatState.defenseMode){
			
			// sc76.choi 확장이 시작되었으면 태어난 자리에 있게 한다.
			Position DEFENCE_POSITION_TO_UNIT = getTargetPositionForDefence(unit);
			
			if(unit.isUnderStorm()){
					commandUtil.move(unit, myMainBaseLocation.getPosition());
			}
			// sc76.choi 스톰을 맡고 있지 않으면
			else{
				
				enemyUnitForMainDefence = findAttackTargetForMainDefence();
				enemyAirUnitForMainDefence = findAttackAirTargetForMainDefence();
				enemyUnitForExpansionDefence = findAttackTargetForExpansionDefence();
				enemyUnitForDefence = findAttackTargetForDefence();

				
				List<Unit> unitsAttackingRadius = unit.getUnitsInRadius(Config.TILE_SIZE*5);
				boolean canAttackNow = KCSimulationManager.Instance().canAttackNow(unitsAttackingRadius);
				
				// sc76.choi 확장에 적이 있으면, 본진 베이스 까지 후퇴한다.	
				if (enemyUnitForExpansionDefence != null && commandUtil.IsValidUnit(enemyUnitForExpansionDefence)){
					
					if(canAttackNow){
						if(unit.isIdle() == true){
							commandUtil.attackMove(unit, enemyUnitForExpansionDefence.getPosition());
						}
					}else{
						// sc76.choi 일정시간까지 유효
						if(enemyRace == Race.Protoss && MyBotModule.Broodwar.getFrameCount() > 24*60*6){
							
						}else{
							if(unit.isAttacking() == true){
								// sc76.choi 본진 가까이 있으면 그냥 싸운다.
								commandUtil.move(unit, myMainBaseLocation.getPosition());
							}
						}
					}
				}
				// sc76.choi 본진에 적이 있으면, DEFENCE_POSITION 까지 후퇴한다.				
				else if (enemyUnitForMainDefence != null){
					
					
//					System.out.println("enemyUnitForMainDefence    : " + enemyUnitForMainDefence);
//					System.out.println("enemyAirUnitForMainDefence : " + enemyAirUnitForMainDefence);
					
					// sc76.choi 공중공격 유닛이 없으면 같이 공격 간다.
					if(enemyAirUnitForMainDefence == null){
						if(canAttackNow){
							if(unit.isIdle() == true){
								commandUtil.attackMove(unit, enemyUnitForMainDefence.getPosition());
							}
						}else{
							commandUtil.move(unit, myMainBaseLocation.getPosition());
						}
					
					}
				}
				// sc76.choi DEFENCE_POSITION에 적이 있으면 
				else if (enemyUnitForDefence != null && commandUtil.IsValidUnit(enemyUnitForDefence)){
					
					if(canAttackNow){
						// 공격할 때
						if(unit.isAttacking() == false){
							commandUtil.attackMove(unit, enemyUnitForDefence.getPosition());
						}
					}else{
						if(enemyRace == Race.Protoss && MyBotModule.Broodwar.getFrameCount() > 24*60*6){
							
						}else{
							// sc76.choi 빠질 때
							if(unit.isAttacking() == true){
								commandUtil.move(unit, DEFENCE_POSITION_TO_UNIT);
							}
						}
					}
				}
				else{
					
					if(getCountCombatType2() >= 20) return true;
					
//					//APM 관리
//					if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) {
//						return true;
//					}
					
					int defenceReturnDist = 6; 
					if(getCountCombatType2() > 20) defenceReturnDist = 8;
						
					// 유닛이 멀리 있으면 강제로 소환
					if(unit.getDistance(DEFENCE_POSITION_TO_UNIT) > Config.TILE_SIZE*defenceReturnDist){
						// 확장이 되어 멀리 있는 base에서 오면 어택으로 이동한다.
						commandUtil.move(unit, DEFENCE_POSITION_TO_UNIT);
					}				
				}
			}
			hasCommanded = true;
		}
	
		return hasCommanded;
	}
	
	boolean controlCombatUnitType1Away(Unit unit){
		boolean hasCommanded = true;
		
		// 본진 근처에 적이 있고, 멀티에 공격 유닛이 5마리 이상있으면, 본진의 DEFENCE_POSITION으로 attackMove한다.
		// base가 3개 이상일때 실행한다.
		if(myOccupiedBaseLocations >= 3){
		
			Position MOVE_POSITION = DEFENCE_POSITION;
			if(combatState == CombatState.defenseMode){
				MOVE_POSITION = getTargetPositionForDefence(unit);
				
				if((myCombatUnitType1ListAway.size() + myCombatUnitType2ListAway.size()) >= 10){
					// 10초에 한번 실행
					if (MyBotModule.Broodwar.getFrameCount() % 24*60 != 0) {
						commandUtil.attackMove(unit, DEFENCE_POSITION);
					}
				}
			}else{
				if(unit.isIdle()){
					commandUtil.attackMove(unit, TARGET_POSITION);
				}
			}
		}
		
		return hasCommanded;
	}
	
	boolean controlCombatUnitType2(Unit unit) { 
		boolean hasCommanded = false; 
		Position targetPosition = null;
		//if (unit.getType() == UnitType.Zerg_Hydralisk) {
			
			if (combatState == CombatState.defenseMode) {
				
				// sc76.choi 확장이 시작되었으면 태어난 자리에 있게 한다.
				Position DEFENCE_POSITION_TO_UNIT = getTargetPositionForDefence(unit);
				
				enemyUnitForMainDefence = findAttackTargetForMainDefence();
				enemyUnitForExpansionDefence = findAttackTargetForExpansionDefence();
				enemyUnitForDefence = findAttackTargetForDefence();
				
				List<Unit> units = unit.getUnitsInRadius(Config.TILE_SIZE*6);
				boolean canAttackNow = KCSimulationManager.Instance().canAttackNow(units);
				
				if(unit.isUnderStorm()){
					commandUtil.move(unit, myMainBaseLocation.getPosition());
				}
				// sc76.choi 다른 멀티 지역에 적이 발견되었으면 해당 지점으로 어택
				else if(findAttackTargetForOtherBaseWhenDenfence() != null){
					commandUtil.attackMove(unit, findAttackTargetForOtherBaseWhenDenfence());
				}
				// sc76.choi 스톰을 맞고 있지 않으면
				else{
					
					// sc76.choi 약한 상대를 골라서 공격한다.
					Unit targetWeakUnit = getEnemyWeakUnitsInRadius(units);
					
					// sc76.choi 확장에 적이 있으면, 본진 베이스 까지 후퇴한다.				
					if (enemyUnitForExpansionDefence != null && commandUtil.IsValidUnit(enemyUnitForExpansionDefence)){
							
						// sc76.choi 나의 유닛의 주변, 적군의 공격 포인트 판단.
						if(canAttackNow){
							
							if(unit.isAttacking() == false){
								if(targetWeakUnit != null){
									//System.out.println("attack Unit  1 ");
									commandUtil.attackUnit(unit, targetWeakUnit);							
								}else{
									commandUtil.attackMove(unit, enemyUnitForExpansionDefence.getPosition());
								}
							}
							
						}else{
							if(unit.isAttacking() == true){
								commandUtil.move(unit, DEFENCE_POSITION);
							}
						}
					}
					// sc76.choi 본진에 적이 있으면, DEFENCE_POSITION 까지 후퇴한다.				
					else if (enemyUnitForMainDefence != null && commandUtil.IsValidUnit(enemyUnitForMainDefence)){
						
						if(unit.isAttacking() == false){
							if(targetWeakUnit != null){
								//System.out.println("attack Unit  1 ");
								commandUtil.attackUnit(unit, targetWeakUnit);
							}else{
								commandUtil.attackMove(unit, enemyUnitForMainDefence.getPosition());
							}
						}
					}
					// sc76.choi DEFENCE_POSITION에 적이 있으면 
					else if (enemyUnitForDefence != null && commandUtil.IsValidUnit(enemyUnitForDefence)){
						
						List<Unit> unitsAttackingRadius = unit.getUnitsInRadius(Config.TILE_SIZE*5);
						canAttackNow = KCSimulationManager.Instance().canAttackNow(unitsAttackingRadius);
						
						// sc76.choi 확장이 시작되었으면 태어난 자리에 있게 한다.
						if(canAttackNow){
							//if(unit.isAttacking() == false){
								if(targetWeakUnit != null){
									//System.out.println("attack Unit  1 ");
									commandUtil.attackUnit(unit, targetWeakUnit);
								}else{
									// sc76.choi 적진에서 가까운 쪽으로 히드라를 모은다.
									if(buildState == BuildState.fastMutalisk_Z){
										commandUtil.attackMove(unit, myMainBaseLocation.getPosition());
									}else{
										commandUtil.attackMove(unit, DEFENCE_POSITION_TO_UNIT);
									}
								}
							//}
						}else{
							// sc76.choi 적진에서 가까운 쪽으로 히드라를 모은다.							
							if(buildState == BuildState.fastMutalisk_Z){
								if(unit.isAttacking() == false){
									commandUtil.attackMove(unit, myMainBaseLocation.getPosition());
								}
							}else{
								
								// 2초에 한번 실행
								if (MyBotModule.Broodwar.getFrameCount() % 4 != 0) {
									return true;
								}
								
								int defenceReturnDist = 6; 
								if(getCountCombatType2() > 20) defenceReturnDist = 8;
								
								// 유닛이 멀리 있으면 강제로 소환
								if(unit.getDistance(DEFENCE_POSITION_TO_UNIT) > Config.TILE_SIZE*defenceReturnDist){
									// 확장이 되어 멀리 있는 base에서 오면 어택으로 이동한다.
									if(myOccupiedBaseLocations >= 3){
										commandUtil.move(unit, DEFENCE_POSITION_TO_UNIT);
									}else{
										commandUtil.move(unit, DEFENCE_POSITION);
									}
								}else{
									commandUtil.move(unit, DEFENCE_POSITION);
								}    	
							}
						}
					}
					// sc76.choi 적이 없으면, 앞마당을 벗어난 DEFENCE 지역인 경우.
					else{
						
						if(getCountCombatType2() >= 20) return true;
						
						// 유닛이 멀리 있으면 강제로 소환
						if(unit.getDistance(DEFENCE_POSITION_TO_UNIT) <= Config.TILE_SIZE*4){
							
							if(canAttackNow && unit.getGroundWeaponCooldown() == 0){
								if(targetWeakUnit != null){
									//System.out.println("attack Unit  1 ");
									commandUtil.attackUnit(unit, targetWeakUnit);
								}else{
									// sc76.choi 적진에서 가까운 쪽으로 히드라를 모은다.
									if(buildState == BuildState.fastMutalisk_Z){
										commandUtil.attackMove(unit, myMainBaseLocation.getPosition());
									}else{
										commandUtil.attackMove(unit, DEFENCE_POSITION_TO_UNIT);
									}
								}
							}else{
								// sc76.choi 적진에서 가까운 쪽으로 히드라를 모은다.							
								if(buildState == BuildState.fastMutalisk_Z){
									commandUtil.attackMove(unit, myMainBaseLocation.getPosition());
								}else{
									// 2초에 한번 실행
									if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) {
										return true;
									}
									
								}
							}
						}else{
							commandUtil.move(unit, DEFENCE_POSITION_TO_UNIT);
						} 
					}
				}
				hasCommanded = true;
			}
			// 공격할때
			else{
				
				if(unit.getHitPoints() <= 10){
					commandUtil.move(unit, DEFENCE_POSITION);
				}
				else if(unit.isUnderStorm()){
					// sc76.choi 주변은 같이 후퇴한다.
					for(Unit aroundMyUnit : myCombatUnitType2List){
						if(aroundMyUnit.getType() == UnitType.Zerg_Hydralisk){
							commandUtil.move(unit, myMainBaseLocation.getPosition());
						}
					}
					
				}
				// sc76.choi 스톰을 맡고 있지 않으면
				else{
					// sc76.choi 약한 상대를 골라서 공격한다.
					List<Unit> myUnitsInRadius = unit.getUnitsInRadius(Config.TILE_SIZE*7);
					Unit targetWeakUnit = getEnemyWeakUnitsInRadius(myUnitsInRadius);
					
					// sc76.choi cooldown 시간을 이용한 침 뿌리고, 도망가기
					//boolean canAttackNow = KCSimulationManager.Instance().canAttackNow(myUnitsInRadius);
					boolean canAttackNow = KCSimulationManager.Instance().canAttackNow(myUnitsInRadius);
					
					if(canAttackNow && unit.getGroundWeaponCooldown() == 0){
						if(unit.isAttacking() == false){
							if(targetWeakUnit != null){
								commandUtil.attackUnit(unit, targetWeakUnit);
							}else{
								commandUtil.attackMove(unit, TARGET_POSITION);
							}
						}
					}
					// sc76.choi 공격중, cooldown이 빠졌을때는 뒤로 도망, 갈때는
					else{
						
						if(getCountCombatType2() >= 14) return true;
						
						// sc76.choi Config.TILE_SIZE*3 거리 만큼 적이 있으면 공격을 하지 않는다. 
						// 건물만 있으면, 그냥 계속 공격하도록 한다.
						int checkAroundCanAttakUnit = 0;
						for(Unit who : unit.getUnitsInRadius(Config.TILE_SIZE*5)){
							if(who.getPlayer() == enemyPlayer){
								// sc76.choi 공격가능하지만, 건물은 아닌 유닛만 카운트한다.
								if(who.getType().canAttack() && !who.getType().isBuilding()){
									checkAroundCanAttakUnit++;
								}
							}
						}
						
						// 주변에 빌딩밖에 없으면 전진 공격만 한다.
						if(checkAroundCanAttakUnit == 0){
							if(unit.isAttacking() == false){
								if(targetWeakUnit != null){
									commandUtil.attackUnit(unit, targetWeakUnit);
								}else{
									commandUtil.attackMove(unit, TARGET_POSITION);
								}
							}
						}
						// 주변에 공격 대상이 있으면 그대로 뺀다.
						else{
							// sc76.choi TODO 계속 무브를 하면 벽에서 멍청하게 서 있는다.
							// sc76.choi TODO 적군이 있는 쪽으로 move 하면 안된다.
							// sc76.choi TODO isIdle을 걸면 안된다.
							
							Position calPosition = getCalcuatePosition(unit, 5);
							double hitPointRate = 0.0d;
							hitPointRate = (unit.getHitPoints()*1.0) / (unit.getType().maxHitPoints()*1.0);
							
							if(unit.isAttacking() == true){
								if(hitPointRate > 0.30){
									commandUtil.move(unit, calPosition);
								}else{
									commandUtil.rightClick(unit, calPosition);
								}
							}
						}
					}
					hasCommanded = true;
				}
			}
		//}
		return hasCommanded;
	}
	
	boolean controlCombatUnitType2Away(Unit unit){
		boolean hasCommanded = true;
		
		// 본진 근처에 적이 있고, 멀티에 공격 유닛이 5마리 이상있으면, 본진의 DEFENCE_POSITION으로 attackMove한다.
		// base가 3개 이상일때 실행한다.
		if(myOccupiedBaseLocations >= 3){
		
			
			Position MOVE_POSITION = DEFENCE_POSITION;
			if(combatState == CombatState.defenseMode){
				MOVE_POSITION = getTargetPositionForDefence(unit);
				
				if((myCombatUnitType1ListAway.size() + myCombatUnitType2ListAway.size() + myCombatUnitType3ListAway.size()) >= 10){
					// 10초에 한번 실행
					if (MyBotModule.Broodwar.getFrameCount() % 24*60 != 0) {
						commandUtil.attackMove(unit, DEFENCE_POSITION);
					}
				}
			}else{
				if(unit.isIdle()){
					commandUtil.attackMove(unit, TARGET_POSITION);
				}
			}
		}
		
		return hasCommanded;
	}
	
	/// 러커 유닛에 대해 컨트롤 명령을 내립니다
	boolean controlCombatUnitType3(Unit unit){
		
		boolean hasCommanded = false;

		if(enemyMainBaseLocation == null) return true;
		
		// defenseMode 일 경우
		// 아군 방어 건물이 세워져있는 위치 주위에 버로우시켜놓는다
		// sc76.choi defenseMode이지만, 적진에 깊이 박혀 있으면, 그대로 둔다.
		// sc76.choi 디데일 점검 필요
		if (combatState == CombatState.defenseMode) {
			
			// sc76.choi 버로우 되어 있지 않고, 나의 지역과 가까이 있으면 (35), 버로우 한다.
			if (unit.isBurrowed() == false) {			
				if (unit.getDistance(DEFENCE_POSITION) < Config.TILE_SIZE*2){
					unit.burrow();
				}
				// sc76.choi 나의 본거지와 멀리 있으면 귀환
				else{
					commandUtil.move(unit, DEFENCE_POSITION);
				}
			}
			// sc76.choi 방어 모드 시에, 현재 버로우 되어 있고, 공격 받고 있으면 바로 본진으로 귀환
			// sc76.choi TODO 본진 근처면 그냥 계속 버로우 한다.
			else{
				if(unit.isUnderAttack() 
					&& unit.getDistance(DEFENCE_POSITION) >= Config.TILE_SIZE*4){
					
					// 근처에 적 유닛이 있으면 버로우 시키고, 없으면 언버로우 시킨다
					Position nearEnemyUnitPosition = null;
					double tempDistance = 0;
					for(Unit enemyUnit : MyBotModule.Broodwar.enemy().getUnits()) {
						
						if (enemyUnit.isFlying()) continue;
						tempDistance = unit.getDistance(enemyUnit.getPosition());
						if (tempDistance < 6 * Config.TILE_SIZE) {
							nearEnemyUnitPosition = enemyUnit.getPosition();
						}
					}
					
					
					if (nearEnemyUnitPosition == null) {
						int myUnitCount = 0;
						for(Unit who : unit.getUnitsInRadius(Config.TILE_SIZE*5)){
							if(who.getPlayer() == myPlayer){
								// 
								// sc76.choi 공격가능하지만, 건물은 아닌 유닛만 카운트한다.
								
								if(who.getType() == UnitType.Zerg_Lurker) continue; 
								if(who.getType().isWorker()) continue;
								if(who.getType() == UnitType.Zerg_Overlord) continue;
								
								if(who.getType().canAttack() && !who.getType().isBuilding()){
									myUnitCount++;
								}
							}
						}
						
						if(myUnitCount > 4){
							
							// sc76.choi 적이 가까이에 왔으면 다시 언버로우 한다.
							if(getClosestCanAttackUnitTypeToTarget(enemyPlayer, null, myMainBaseLocation.getPosition(), Config.TILE_SIZE*50, false) != null){
								unit.unburrow();
							}else{
								unit.burrow();	
							}
							
							
						}else{
							unit.unburrow();
						}
					}else{
						unit.burrow();
					}
				}else{
					commandUtil.move(unit, DEFENCE_POSITION);
					//unit.unburrow();
				}
			}
			hasCommanded = true;
		}
		// sc76.choi 공격 일때.
		else {
			if(unit.isUnderStorm()){
				if (unit.isBurrowed() == true) {
					unit.unburrow();
				}else{
					commandUtil.move(unit, myMainBaseLocation.getPosition());
				}
			}
			// sc76.choi 공격 중, 스톰을 맞고 있지 않으면
			else{
				// sc76.choi 근처에 적 유닛이 있으면 버로우 시키고, 없으면 언버로우 시킨다
				Position nearEnemyUnitPosition = null;
				double tempDistance = 0;
				//for(Unit enemyUnit : unit.getUnitsInRadius(Config.TILE_SIZE*7)) {
				
				for(Unit enemyUnit : MyBotModule.Broodwar.enemy().getUnits()) {
					
					//if(unit.getPlayer() == enemyPlayer){
						if (enemyUnit.getType().isFlyer()) continue;
//						if (enemyUnit.getType().isFlyingBuilding()) continue;
						if (enemyUnit.isFlying() && enemyUnit.getType() == UnitType.Terran_Engineering_Bay) continue;
						if (enemyUnit.isFlying() && enemyUnit.getType() == UnitType.Terran_Barracks) continue;
//						if (enemyUnit.getType().isWorker()) continue;
						if (enemyUnit.getType() == UnitType.Terran_Medic) continue;
						
						tempDistance = unit.getDistance(enemyUnit.getPosition());
						if (tempDistance < Config.TILE_SIZE * 7) {
							nearEnemyUnitPosition = enemyUnit.getPosition();
						}
					//}
				}
				
//				Position nearMyUnitPosition = null;
//				double tempDistance2 = 0;
//				for(Unit myUnit : MyBotModule.Broodwar.self().getUnits()) {
//					
//					if (myUnit.getType() == UnitType.Zerg_Lurker){
//					
//						tempDistance2 = unit.getDistance(myUnit.getPosition());
//						if (tempDistance2 < Config.TILE_SIZE * 5) {
//							nearMyUnitPosition = myUnit.getPosition();
//						}
//					}
//				}
				
				// sc76.choi 약한 상대를 골라서 공격한다.
				List<Unit> myUnitsInRadius = unit.getUnitsInRadius(Config.TILE_SIZE*7);
				Unit targetWeakUnit = getEnemyWeakUnitsInRadius(myUnitsInRadius);
				
				// sc76.choi 공격중이나, 버로우 되지 않았으면
				if (unit.isBurrowed() == false) {
					// sc76.choi 적이 있으면
					if (nearEnemyUnitPosition != null || nearEnemyUnitPosition !=null) {
						unit.burrow();
					}else{
						commandUtil.move(unit, TARGET_POSITION);
					}
					hasCommanded = true;
				}
				// 버로우 되어 있으면
				else {
					if (nearEnemyUnitPosition == null) {
						// sc76.choi 적진이 가까이(enemyFirstChokePoint)에 왔으면 다시 언버로우 한다.
						if(unit.getDistance(enemyFirstChokePoint) > Config.TILE_SIZE*3){
							unit.unburrow();
						}
					}
					
					// 버로우 된 상태이고, 공격 중이면 약한 상대를 공격한다.
					if(targetWeakUnit !=null){
						commandUtil.attackUnit(unit, targetWeakUnit);
					}
					hasCommanded = true;
				}
				
				if(hasCommanded == false){
					commandUtil.move(unit, TARGET_POSITION);
				}
			}
		}

		return hasCommanded;
	}
	
	boolean controlCombatUnitType3Away(Unit unit){
		boolean hasCommanded = true;
		
		// 본진 근처에 적이 있고, 멀티에 공격 유닛이 5마리 이상있으면, 본진의 DEFENCE_POSITION으로 attackMove한다.
		// base가 3개 이상일때 실행한다.
		if(myOccupiedBaseLocations >= 3){
		
			// 10초에 한번 실행
			if (MyBotModule.Broodwar.getFrameCount() % 24*60 != 0) {
				return true;
			}
			
			Position MOVE_POSITION = DEFENCE_POSITION;
			if(combatState == CombatState.defenseMode){
				MOVE_POSITION = getTargetPositionForDefence(unit);
				
				if((myCombatUnitType1ListAway.size() + myCombatUnitType2ListAway.size() + myCombatUnitType3ListAway.size()) >= 10){
					// 10초에 한번 실행
					if (MyBotModule.Broodwar.getFrameCount() % 24*60 != 0) {
						if(unit.unburrow() == true){
							unit.burrow();
						}
					}
				}
				else{
					if(unit.getDistance(MOVE_POSITION) > Config.TILE_SIZE*3){
						if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) {
							commandUtil.move(unit, MOVE_POSITION);
						}
					}
				}
			}else{
				if(unit.isIdle()){
					commandUtil.attackMove(unit, TARGET_POSITION);
				}
			}
		}
		
		return hasCommanded;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 뮤탈 유닛에 대해 컨트롤 명령을 내립니다
	////////////////////////////////////////////////////////////////////////////////////////////////////
	Unit enemyUnitForMutalisk = null;
	Unit closestOverloadWithIdle = null;
	
	boolean controlCombatUnitType4(Unit unitType4){
		boolean hasCommanded = false; 
		
//		System.out.println("enemyUnitForMutalisk 0 : ");
//		System.out.println();
		
		// sc76.choi 뮤탈이 5개 이상 죽었고, 그레이트 스파이어가 있다면 방어지역에서 대기, 가디언으로 전환 준비
		if (myKilledCombatUnitCount4 >= 5 && myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0) {
			
			if (MyBotModule.Broodwar.getFrameCount() % (24*10) != 0) return true;
			for(Unit unit : myCombatUnitType4List){
				
				if(unit.getDistance(myMainBaseLocation.getPosition()) > Config.TILE_SIZE*4){
					commandUtil.move(unit, myMainBaseLocation.getPosition());
				}
				
			}
			return true;
		}
			
		// sc76.choi 공격 이라도 4마리가 되지 않으면 모여 있는다. Greater Spire가 올라가 있으면 공격 중지
		if(myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) < 4 
			|| myPlayer.allUnitCount(UnitType.Zerg_Greater_Spire) > 0){
			
			if (MyBotModule.Broodwar.getFrameCount() % (24*10) != 0) return true;
			for(Unit unit : myCombatUnitType4List){
				if(unit.getDistance(myMainBaseLocation.getPosition()) > Config.TILE_SIZE*4){
					commandUtil.move(unit, myMainBaseLocation.getPosition());
				}
			}
			return true;
		}
		
		// sc76.choi 뮤탈 리스크의 공격 대상 선정
		enemyUnitForMutalisk = findAttackTargetForMutalisk();
		
		// sc76.choi 근처에 적이 존재 하면
		if(commandUtil.IsValidUnit(enemyUnitForMutalisk)){
			
//			System.out.println("enemyUnitForMutalisk 1 : " + enemyUnitForMutalisk.getID() + " " + enemyUnitForMutalisk.getType());
//			System.out.println();
			
			// sc76.choi 뮤탈을 위한 오버로드 한마리 추가 getClosestOverload(target)
			if(closestOverloadWithIdle == null){
				closestOverloadWithIdle = getClosestOverloadWithIDLE(myMainBaseLocation.getPosition());
				if(commandUtil.IsValidUnit(closestOverloadWithIdle)){
					myCombatUnitType4List.add(closestOverloadWithIdle);
				}
			}
			
			// sc76.choi 안전한지 체크
			boolean isSafeAround = true;
			// sc76.choi 뮤탈의 타겟 주변이 안전한지 검사.
			for(Unit enemyUnit : enemyUnitForMutalisk.getUnitsInRadius(Config.TILE_SIZE*6)){
				if(enemyUnit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace)
						|| enemyUnit.getType() == UnitType.Zerg_Spore_Colony
						|| enemyUnit.getType() == UnitType.Terran_Missile_Turret
						|| enemyUnit.getType() == UnitType.Terran_Bunker
						|| enemyUnit.getType() == UnitType.Protoss_Photon_Cannon
						){
					isSafeAround = false;
				}
			}
			
//			System.out.println("enemyUnitForMutalisk 2 : " + isSafeAround + " " + enemyUnitForMutalisk.getID() + " " + enemyUnitForMutalisk.getType());
//			System.out.println();
			
			// sc76.choi 공격 가능한 지역이라 판단되면 
			if(isSafeAround){
				for(Unit unit : myCombatUnitType4List){
					// sc76.choi cooldown 시간을 이용한 공격
					if(unit.getGroundWeaponCooldown() == 0 && unit.getHitPoints() > 10 ){
							// sc76.choi 뮤탈 이동
							if(unit.getType() == UnitType.Zerg_Overlord){
								commandUtil.move(unit, enemyUnitForMutalisk.getPosition());
								//commandUtil.move(unit, enemyMainBaseLocation.getPosition());
							}else{
								// TODO 뮤탈은 어디를 공격?
								//commandUtil.attackUnit(unit, enemyUnitForMutalisk);
								commandUtil.attackMove(unit, enemyMainBaseLocation.getPosition());
								if(Config.DEBUG) MyBotModule.Broodwar.drawLineMap(unit.getPosition(), enemyUnitForMutalisk.getTargetPosition(), Color.Yellow);
							}
					}
					// sc76.choi 공격중, cooldown이 빠졌을때는 뒤로 도망, 갈때는
					else{
						
						// sc76.choi 확장이 시작되었으면 태어난 자리에 있게 한다.
						if(unit.getDistance(DEFENCE_POSITION) < Config.TILE_SIZE*35){
							commandUtil.move(unit, DEFENCE_POSITION);
						}
					}
				} // for
			}
			// sc76.choi 안전하지 않으면 본진으로 귀환
			else{
				
				//System.out.println("enemyUnitForMutalisk null : ");
				
				enemyUnitForMutalisk = null; 
				
				for(Unit unit : myCombatUnitType4List){
					//if(unit.isIdle()){
						commandUtil.rightClick(unit, myMainBaseLocation.getPosition());
					//}
				}
			}
		}
		// sc76.choi 타켓이 없으면, 본진귀환 TODO 정찰 해야 한다.
		else{
			
			enemyUnitForMutalisk = null;
			
			for(Unit unit : myCombatUnitType4List){
				if (MyBotModule.Broodwar.getFrameCount() % (24*1) != 0) return true;
				commandUtil.rightClick(unit, myMainBaseLocation.getPosition());
			}
		}

		hasCommanded = true;
		
		return hasCommanded;
	}
	
	// sc76.choi 울트라 유닛에 대해 컨트롤 명령을 내립니다
	boolean controlCombatUnitType5(Unit unit){
		boolean hasCommanded = false; 
		Position targetPosition = null;
		
		if (combatState == CombatState.defenseMode) {
			commandUtil.move(unit, DEFENCE_POSITION);
			hasCommanded = true;
		}else{
			commandUtil.attackMove(unit, TARGET_POSITION);
			hasCommanded = true;
		}
		
		return hasCommanded;
	}
	
	// sc76.choi 가디언 유닛에 대해 컨트롤 명령을 내립니다
	boolean controlCombatUnitType6(Unit unit){
		boolean hasCommanded = false; 
		Position targetPosition = null;
		
		if (combatState == CombatState.defenseMode) {
			commandUtil.move(unit, DEFENCE_POSITION);
			hasCommanded = true;
		}else{
			commandUtil.attackMove(unit, TARGET_POSITION);
			hasCommanded = true;
		}
		
		return hasCommanded;
	}
	
	private Position findAttackTargetForOtherBaseWhenDenfence(){
		Position attackPosition = null;
		
		// sc76.choi 멀티 지역에 적을 체크 한다.
		int countSelfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer).size();
		if(countSelfRegions >= 3){
			Set<Region> selfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer);
			Iterator<Region> it1 = selfRegions.iterator();
			while (it1.hasNext()) {
				Region selfRegion = it1.next();

				if(selfRegion == BWTA.getRegion(myMainBaseLocation.getPosition())) continue;
				if(selfRegion == BWTA.getRegion(myFirstExpansionLocation.getPosition())) continue;
				
				if(existUnitTypeInRegion(enemyPlayer, null, selfRegion, false, true)){
					return selfRegion.getCenter();
				}
			}
		}
		
		
		return attackPosition;
	}
	
	// sc76.choi Defence 모드 일때, 실행한다. 
	// TODO 적의 거리를 따져, 가까운 유닛만 반환해야 한다. 안그러면 계속 싸운다.
	private Unit findAttackTargetForExpansionDefence() {
		
		boolean existHatcheryInMyFirstExpansion = existUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		if(existHatcheryInMyFirstExpansion == false) return null;
			
//		if(enemyUnitForExpansionDefence == null){
		    Unit target = null;
		    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
		    	//if(unit.getDistance(myFirstExpansionLocation.getPoint()) <= Config.TILE_SIZE*4){
		    	if(unit.getDistance(myFirstExpansionLocation) <= Config.TILE_SIZE*12){
		    		if (unit.getType().canAttack()) {
		                target = unit;
		                break;
		    		}
		    	}
		    }
		    return target;
//		}else{
//			return enemyUnitForExpansionDefence;
//		}
	}
	
	// sc76.choi DEFENCE_POSITION 근처에 적을 반환
	private Unit findAttackTargetForDefence() {
		
		boolean existHatcheryInMyFirstExpansion = existUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		if(existHatcheryInMyFirstExpansion == false) return null;
		
//		if(enemyUnitForDefence == null){
			
		    Unit target = null;
		    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
		    	//if(unit.getDistance(myFirstExpansionLocation.getPoint()) <= Config.TILE_SIZE*4){
		    	if(unit.getDistance(DEFENCE_POSITION) <= Config.TILE_SIZE*6){
		    		if (unit.getType().canAttack()) {
		                target = unit;
		                break;
		    		}
		    	}
		    }
		    return target;
//		}else{
//			return enemyUnitForDefence;
//		}
	}
	
	private Unit findAttackTargetForDefenceWorkerRatio() {
		
		boolean existHatcheryInMyFirstExpansion = existUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		if(existHatcheryInMyFirstExpansion == false) return null;
		
//		if(enemyUnitForDefence == null){
		
		Unit target = null;
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			//if(unit.getDistance(myFirstExpansionLocation.getPoint()) <= Config.TILE_SIZE*4){
			if(unit.getDistance(DEFENCE_POSITION) <= Config.TILE_SIZE*12){
				if (unit.getType().canAttack()) {
					target = unit;
					break;
				}
			}
		}
		return target;
//		}else{
//			return enemyUnitForDefence;
//		}
	}
	
	// sc76.choi Defence 모드 일때, 실행한다. 
	// TODO 적의 거리를 따져, 가까운 유닛만 반환해야 한다. 안그러면 계속 싸운다.
	private Unit findAttackTargetForMainDefence() {
		
		Unit target = null;
//		if(enemyUnitForMainDefence == null){
		    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
		    	if(unit.getDistance(myMainBaseLocation.getPoint()) <= Config.TILE_SIZE*15){
		    		if(unit.getType().canAttack() || unit.getType().isFlyer()) {
		    			
		    			if(unit.getType().isWorker()) continue;
		    			if(unit.getType() == UnitType.Zerg_Overlord) continue;
		    			
		    			target = unit;
		    			break;
		    		}
		    	}
		    }
		    return target;
//		}else{
//			return enemyUnitForMainDefence;
//		}
	}

	// sc76.choi Defence 모드 일때, 실행한다. 
	// TODO 적의 거리를 따져, 가까운 유닛만 반환해야 한다. 안그러면 계속 싸운다.
	private Unit findAttackAirTargetForMainDefence() {
		
		if(enemyAirUnitForMainDefence == null){
		    Unit target = null;
		    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
		    	if(unit.getDistance(myMainBaseLocation.getPoint()) <= Config.TILE_SIZE*15){
		    		if(unit.getType().canAttack() && unit.getType().isFlyer()) {
		    			target = unit;
		    			break;
		    		}
		    	}
		    }
		    return target;
		}else{
			return enemyAirUnitForMainDefence;
		}
	}

	// sc76.choi 테란 상대 일때, 최초 저글링 어택을 위해 실행한다. 
	Unit unitAttackTargetForInitialZergling = null;
	private Unit findAttackTargetForInitialZergling() {
		
//		if(unitAttackTargetForInitialZergling == null){
			
		    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
		        if(commandUtil.IsValidUnit(unit)){
		        	
		        	if(unit.getType().isBuilding()) continue;
		        	
		    		if(Race.Terran == enemyRace){
		    			if(unit.getType().isWorker() == true){
		    				if(unit.isConstructing()){
		    					unitAttackTargetForInitialZergling = unit;
			        			break;
		    				}else if(unit.isBeingConstructed()){
		    					unitAttackTargetForInitialZergling = unit;
			        			break;
		    				}else{
		    					unitAttackTargetForInitialZergling = unit;
			        			break;
		    				}
		    			}
		    		}else if (Race.Protoss == enemyRace){
		    			
		    		}else if (Race.Zerg == enemyRace){
		    			
		    		}else if(unit.getType().isFlyer()){
		                
		        	}
		    	}
		    }
//		}
	    return unitAttackTargetForInitialZergling;
	}

	// sc76.choi Defence 모드 일때, 실행한다. 
	// TODO 적의 거리를 따져, 가까운 유닛만 반환해야 한다. 안그러면 계속 싸운다.
	private Unit findAttackTargetForMutalisk() {
		
	    Unit target = null;
	    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
	        if(commandUtil.IsValidUnit(unit)){
	        	
//	        	if(unit.getType().isWorker()) continue;
	        	if(unit.getType().isBuilding()) continue;
	        	
	        	//if (unit.getType().canAttack()) {
	        		
	        		if(Race.Terran == enemyRace){
	        			if(unit.getType() == UnitType.Terran_Dropship){
		        			target = unit;
		        			break;
	        			}else if(unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode){
		        			target = unit;
		        			break;
	        			}else if(unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Terran_Vulture_Spider_Mine){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Terran_Vulture){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Terran_Marine){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Terran_Firebat){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Terran_Medic){
			        		target = unit;
			        		break;
	        			}
	        		}else if (Race.Protoss == enemyRace){
	        			if(unit.getType() == UnitType.Protoss_Shuttle){
		        			target = unit;
		        			break;
	        			}else if(unit.getType() == UnitType.Protoss_Observer){
		        			target = unit;
		        			break;
	        			}else if(unit.getType() == UnitType.Protoss_Scout){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_Corsair){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_Carrier){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_Reaver){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_High_Templar){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_Dark_Templar){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_Probe){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_Dragoon){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Protoss_Zealot){
			        		target = unit;
			        		break;
	        			}else if (unit.getType().isFlyer()) {
	        				target = unit;
			        		break;
	        			}
	        		}else if (Race.Zerg == enemyRace){
	        			if(unit.getType() == UnitType.Zerg_Overlord){
		        			target = unit;
		        			break;
	        			}else if(unit.getType() == UnitType.Zerg_Mutalisk){
		        			target = unit;
		        			break;
	        			}else if(unit.getType() == UnitType.Zerg_Queen){
	        				target = unit;
	        				break;
	        			}else if(unit.getType() == UnitType.Zerg_Scourge){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Guardian){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Devourer){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Drone){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Hydralisk){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Lurker){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Sunken_Colony){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Spore_Colony){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Zerg_Zergling){
			        		target = unit;
			        		break;
	        			}
	        		}else if(unit.getType().isFlyer()){
		                target = unit;
		                break;
		        	}
	        	//}
	    	}
	    }
	    return target;
	}

	// sc76.choi Defence 모드 일때, 실행한다. 
	// TODO 적의 거리를 따져, 가까운 유닛만 반환해야 한다. 안그러면 계속 싸운다.
	private Unit findAttackTargetForScourge() {
	    Unit target = null;
	    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
	    	if(commandUtil.IsValidUnit(unit)){
	
	    		if(Race.Terran == enemyRace){
	    			if(unit.getType() == UnitType.Terran_Dropship){
	        			target = unit;
	        			break;
	    			}else if(unit.getType() == UnitType.Terran_Science_Vessel){
	        			target = unit;
	        			break;
	    			}else if(unit.getType() == UnitType.Terran_Wraith){
		        		target = unit;
		        		break;
	    			}else if(unit.getType() == UnitType.Terran_Valkyrie){
		        		target = unit;
		        		break;
	    			}else if(unit.getType() == UnitType.Terran_Battlecruiser){
		        		target = unit;
		        		break;
	    			}else if (unit.isLifted()) {
	    				target = unit;
		        		break;
	    			}else if (unit.getType().isFlyer()) {
	    				target = unit;
		        		break;
	    			}
	    		}else if (Race.Protoss == enemyRace){
	    			if(unit.getType() == UnitType.Protoss_Shuttle){
	        			target = unit;
	        			break;
	    			}else if(unit.getType() == UnitType.Protoss_Observer){
	        			target = unit;
	        			break;
	    			}else if(unit.getType() == UnitType.Protoss_Scout){
		        		target = unit;
		        		break;
	    			}else if(unit.getType() == UnitType.Protoss_Corsair){
		        		target = unit;
		        		break;
	    			}else if(unit.getType() == UnitType.Protoss_Carrier){
		        		target = unit;
		        		break;
	    			}else if (unit.getType().isFlyer()) {
	    				target = unit;
		        		break;
	    			}
	    		}else if (Race.Zerg == enemyRace){
	    			if(unit.getType() == UnitType.Zerg_Overlord){
	        			target = unit;
	        			break;
	    			}else if(unit.getType() == UnitType.Zerg_Mutalisk){
	        			target = unit;
	        			break;
	    			}else if(unit.getType() == UnitType.Zerg_Scourge){
		        		target = unit;
		        		break;
	    			}else if(unit.getType() == UnitType.Zerg_Guardian){
		        		target = unit;
		        		break;
	    			}else if(unit.getType() == UnitType.Zerg_Devourer){
		        		target = unit;
		        		break;
	    			}else if (unit.getType().isFlyer()) {
	    				target = unit;
		        		break;
	    			}
	    		}else if(unit.getType().isFlyer()){
	                target = unit;
	                break;
	        	}
	    	}
	    }
	    return target;
	}

	// sc76.choi Defence 모드 일때, 실행한다. 
	// TODO 적의 거리를 따져, 가까운 유닛만 반환해야 한다. 안그러면 계속 싸운다.
	private Unit findAttackTargetForQueen() {
	    Unit target = null;
	    for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
	    	
	    	if(commandUtil.IsValidUnit(unit)){
	    		
	        	if (unit.getType().canAttack()) {
	        		
	        		if(Race.Terran == enemyRace){
	        			if(unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode){
		        			target = unit;
		        			break;
	        			}else if(unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode){
			        		target = unit;
			        		break;
	        			}
	        			else if(unit.getType() == UnitType.Terran_Goliath){
			        		target = unit;
			        		break;
	        			}else if(unit.getType() == UnitType.Terran_Vulture){
			        		target = unit;
			        		break;			        		
	        			}
	        		}else if(Race.Zerg == enemyRace){
	        			if(unit.getType() == UnitType.Zerg_Lurker){
		        			target = unit;
		        			break;
	        			}
	        		}else{
	        			
	        		}
	        	}
	    	}
	    }
	    
	    return target;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////// 
	// sc76.choi 오버로드 유닛에 대해 컨트롤 명령을 입력합니다
	
	// sc76.choi closest 유닛의 살짝 뒤에 위치한다.
	Position safePositionForOverload ;

	Position safePosition1;

	Position safePosition2;

	Position safePosition3;

	Position safePosition4;

	boolean controlSpecialUnitType1(Unit unit) {

		boolean hasCommanded = false;		
		
		// sc76.choi 기본적으로 myAllCombatUnitList에 담긴 오버로드만 대상이 된다. (즉 Idle인 오버로드)
		// sc76.choi TODO 공격시에 가장 적진과 가까운 히드라(럴커)를 따라 다니게 된다. 개선 필요
		// sc76.choi 오버로드는 hasCommanded는 항상 true
		if (unit.getType() == UnitType.Zerg_Overlord) {
			
			if (!commandUtil.IsValidUnit(unit)) return true;
			
			Position targetPosition = null;
			
			// sc76.choi 공격모드
			if(combatState == CombatState.attackStarted){
				
				if(enemyMainBaseLocation == null) return true;
				
				// sc76.choi 가장 가까운 공격 유닛의 위치를 찾아 오버로드가 따라가게 한다.	
				if(closesAttackUnitFromEnemyMainBase != null){
					Position enemyMainLocationPosition = enemyMainBaseLocation.getPosition();
					safePositionForOverload = closesAttackUnitFromEnemyMainBase.getPosition();
					safePosition1 = closesAttackUnitFromEnemyMainBase.getPosition();
					safePosition2 = closesAttackUnitFromEnemyMainBase.getPosition();
					safePosition3 = closesAttackUnitFromEnemyMainBase.getPosition();
					safePosition4 = closesAttackUnitFromEnemyMainBase.getPosition();
					
					// sc76.choi TARGET_POSITION 보다 항상 먼 포지션을 준다.
					// 12 시
					if(unit.getID() % 4 == 0){
						safePosition1 = new Position(closesAttackUnitFromEnemyMainBase.getX(), closesAttackUnitFromEnemyMainBase.getY() - Config.TILE_SIZE*4);
						//System.out.println("SpecialUnitType1 ["+unit.getID()+"] go -->> 12");
					}
					
					//System.out.println("safePosition  : " + safePosition.toTilePosition());
					//System.out.println("safePosition1 : " + safePosition1.toTilePosition());
					
					if(safePositionForOverload.getDistance(enemyMainLocationPosition) <= safePosition1.getDistance(enemyMainLocationPosition)){
						safePositionForOverload = safePosition1;
					}
					
					//System.out.println("safePosition  : " + safePosition.toTilePosition());
					//System.out.println();
					
					// 3시
					if(unit.getID() % 4 == 1){
						safePosition2 = new Position(closesAttackUnitFromEnemyMainBase.getX() + Config.TILE_SIZE*4, closesAttackUnitFromEnemyMainBase.getY());
						//System.out.println("SpecialUnitType1 ["+unit.getID()+"] go -->> 3");
					}

					if(safePositionForOverload.getDistance(enemyMainLocationPosition) <= safePosition2.getDistance(enemyMainLocationPosition)){
						safePositionForOverload = safePosition2;
					}
					// 6시
					if(unit.getID() % 4 == 2){
						safePosition3 = new Position(closesAttackUnitFromEnemyMainBase.getX(), closesAttackUnitFromEnemyMainBase.getY()  + Config.TILE_SIZE*4);
						//System.out.println("SpecialUnitType1 ["+unit.getID()+"] go -->> 6");
					}
					
					if(safePositionForOverload.getDistance(enemyMainLocationPosition) <= safePosition3.getDistance(enemyMainLocationPosition)){
						safePositionForOverload = safePosition3;
					}
					
					// 9시
					if(unit.getID() % 4 == 3) {
						safePosition4 = new Position(closesAttackUnitFromEnemyMainBase.getX()  - Config.TILE_SIZE*4, closesAttackUnitFromEnemyMainBase.getY());
						//System.out.println("SpecialUnitType1 ["+unit.getID()+"] go -->> 9");
					}

					if(safePositionForOverload.getDistance(enemyMainLocationPosition) <= safePosition4.getDistance(enemyMainLocationPosition)){
						safePositionForOverload = safePosition4;
					}
					
					if(Config.DRAW) MyBotModule.Broodwar.drawCircleMap(safePositionForOverload, 5, Color.Teal, true);
					if(Config.DRAW) MyBotModule.Broodwar.drawCircleMap(safePositionForOverload, 6, Color.Red, false);
					if(Config.DRAW) MyBotModule.Broodwar.drawCircleMap(safePositionForOverload, 13, Color.Yellow, false);
					
					targetPosition = safePositionForOverload; //closesAttackUnitOfPositionFromEnemyMainBase;
				}else{
					targetPosition = DEFENCE_POSITION;
				}
				
				commandUtil.move(unit, targetPosition);
				hasCommanded = true;
			}
			// sc76.choi 방어나, 초기 모드 일때
			else if(combatState == CombatState.defenseMode || combatState == CombatState.initialMode){
				
				// sc76.choi 멀리 나간 오버로드는 귀환시킨다.
				for(Unit overload : OverloadManager.Instance().getOverloadData().getOverloads()){
					if(OverloadManager.Instance().getOverloadData().getJobCode(overload) == 'A'
						|| OverloadManager.Instance().getOverloadData().getJobCode(overload) == 'I'){
						
						OverloadManager.Instance().getOverloadData().setOverloadJob(overload, OverloadData.OverloadJob.Idle, (Unit)null);
						
						if(overload.getDistance(myMainBaseLocation) > Config.TILE_SIZE*10){
							commandUtil.move(overload, DEFENCE_POSITION);
						}
						
						hasCommanded = true;
					}
				}
				
			}else{
				
			}
		}
		return hasCommanded;
	}
	
	/// 두번째 특수 유닛 타입의 유닛에 대해 컨트롤 명령을 내립니다
	// sc76.choi 1 디파일러
	boolean controlSpecialUnitType2(Unit unit) {

		boolean hasCommanded = false;
		
		// sc76.choi 공격모드
		if(combatState == CombatState.attackStarted){
			Position targetSwarmPosition;
			
			if(closesAttackUnitFromEnemyMainBase != null){
				// sc76.choi 현재 swarm이 뿌려졌으면 할 필요없다.
				if(closesAttackUnitFromEnemyMainBase.isUnderDarkSwarm() == true){
					targetSwarmPosition = DEFENCE_POSITION;
				}else{
					targetSwarmPosition = closesAttackUnitOfPositionFromEnemyMainBase;
				}
			}else{
				targetSwarmPosition = DEFENCE_POSITION;
			}
			
			//if (unit.getType() == UnitType.Zerg_Defiler) {
				//System.out.println("TechType.Consume.energyCost() : " + TechType.Consume.energyCost());
				if (unit.getEnergy() < 100 && myPlayer.hasResearched(TechType.Consume)) {
					
					Unit targetMyUnit = null;
					
					// 가장 가까운 저글링을 컨슘 한다
					double minDistance = 1000000000;
					double tempDistance = 0;
					for(Unit zerglingUnit : myCombatUnitType1List) {
						tempDistance = unit.getDistance(zerglingUnit.getPosition());
						if (minDistance > tempDistance) {
							minDistance = tempDistance;
							targetMyUnit = zerglingUnit;
						}
					}
					
					if (targetMyUnit != null) {
						unit.useTech(TechType.Consume, targetMyUnit);
						hasCommanded = true;
					}
				}	
	
				if (unit.getEnergy() >= 151 && myPlayer.hasResearched(TechType.Plague)) {
					if (targetSwarmPosition != null) {
						Unit targetEnemyUnit = null;
						
						int enemyUnitCount = 0;
						
						if(closesAttackUnitFromEnemyMainBase != null){
							//System.out.println("closesAttackUnitFromEnemyMainBase : " + closesAttackUnitFromEnemyMainBase.getID());
							// 선두의 히드라 주변 
							for(Unit enemyUnit : closesAttackUnitFromEnemyMainBase.getUnitsInRadius(Config.TILE_SIZE*7)){
								
								//System.out.println("For Plague enemyUnitCount 0 : " + enemyUnitCount);
								if(enemyUnit.getPlayer() != enemyPlayer) continue;
								
								//System.out.println("For Plague enemyUnitCount 1 : " + enemyUnitCount);
								if(enemyUnit.getType().isBuilding() == false && enemyUnit.getType().isWorker() == false && enemyUnit.getType().canAttack()
										&& enemyUnit.isLifted() == false){
									//System.out.println("For Plague enemyUnitCount 2 : " + enemyUnitCount);
									enemyUnitCount++;
									targetEnemyUnit = enemyUnit;
								}
							}
						}
						
						// 적군이 5마리 이상이면,  Swarm을 뿌린다.
						if(enemyRace == Race.Terran && enemyUnitCount >= 3 && commandUtil.IsValidUnit(targetEnemyUnit)){
							if(DEBUG) System.out.println("Use Plague enemyUnitCount total : " + enemyUnitCount);
							unit.useTech(TechType.Plague, targetEnemyUnit);
							hasCommanded = true;
						}
					}
				}
				
				// 한번 뿌리면 100이 깍인다.
				else if (unit.getEnergy() >= 100) {
	
					// sc76.choi 공격중이고, 타켓으로 이동 중 적군을 많이 만나면.. Dark_Swarm을 뿌린다.
					if (targetSwarmPosition != null) {
						
						int enemyUnitCount = 0;
						
						if(closesAttackUnitFromEnemyMainBase != null){
							//System.out.println("closesAttackUnitFromEnemyMainBase : " + closesAttackUnitFromEnemyMainBase.getID());
							// 선두의 히드라 주변 
							for(Unit enemyUnit : closesAttackUnitFromEnemyMainBase.getUnitsInRadius(Config.TILE_SIZE*7)){
								
								//System.out.println("For Swarm enemyUnitCount 0 : " + enemyUnitCount);
								if(enemyUnit.getPlayer() != enemyPlayer) continue;
								
								//System.out.println("For Swarm enemyUnitCount 1 : " + enemyUnitCount);
								if(enemyUnit.getType().isBuilding() == false && enemyUnit.getType().isWorker() == false && enemyUnit.isLifted() == false){
									//System.out.println("For Swarm enemyUnitCount 2 : " + enemyUnitCount);
									enemyUnitCount++;
								}
								
								if(enemyUnit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace)){
									enemyUnitCount += 4;
								}
								
							}
							
						}
						// 적군이 5마리 이상이면,  Swarm을 뿌린다.
						if(enemyUnitCount >= 3){
							if(Config.DEBUG) System.out.println("Use Swarm enemyUnitCount total : " + enemyUnitCount);
							unit.useTech(TechType.Dark_Swarm, targetSwarmPosition);
						}
						
						// sc76.choi choke에 가까이 있으면 뿌린다.
						if(closesAttackUnitFromEnemyMainBase != null
							&& closesAttackUnitFromEnemyMainBase.getDistance(enemyFirstChokePoint.getCenter()) <= Config.TILE_SIZE*5
							 && enemyUnitCount > 1){
							if(Config.DEBUG) System.out.println("Use Swarm enemyUnitCount total : " + enemyUnitCount);
							unit.useTech(TechType.Dark_Swarm, enemyFirstChokePoint.getCenter());
						}
						
						hasCommanded = true;
					}
				}
			//}
		}else{
			
			commandUtil.move(unit, myFirstExpansionLocation.getPosition());
			hasCommanded = true;
			
		}

		return hasCommanded;
	}
	
	// 스커지
	Unit enemyUnitForScourge = null;
	boolean controlSpecialUnitType3(Unit unit) {
		boolean hasCommanded = false;
		
		if (unit.getType() == UnitType.Zerg_Scourge) {
			
			Position rPosition = getRandomPosition();
			
			int maxDistForScourgePatrol = 35;
			int minDistForScourgePatrol = 20;

			// defenseMode 일 경우
			if (combatState == CombatState.defenseMode) {
				
				
				if(commandUtil.IsValidUnit(enemyUnitForScourge) == false){
					enemyUnitForScourge = findAttackTargetForScourge();
				}
				
				// 본진으로 부터 1600 이하
				if(myMainBaseLocation.getDistance(rPosition) >= Config.TILE_SIZE * maxDistForScourgePatrol){
					//System.out.println(" -------------------------------------- too long " + targetPosition );
					return true;
				}
				
				if(myMainBaseLocation.getDistance(rPosition) < Config.TILE_SIZE * minDistForScourgePatrol){
					//System.out.println(" -------------------------------------- too short " + targetPosition );
					return true;
				}
				
				if(enemyUnitForScourge == null){
					if(unit.isIdle()){
						commandUtil.attackMove(unit, rPosition);
					}
				}else{
					// sc76.choi 가까이 있으면 
					if(myMainBaseLocation.getDistance(enemyUnitForScourge) < Config.TILE_SIZE * maxDistForScourgePatrol){
						commandUtil.attackUnit(unit, enemyUnitForScourge);
						if(Config.DEBUG) MyBotModule.Broodwar.drawLineMap(unit.getPosition(), enemyUnitForScourge.getTargetPosition(), Color.Red);
					}
				}
			}
			// 공격 모드 일때
			else{
				
				if(commandUtil.IsValidUnit(enemyUnitForScourge) == false){
					enemyUnitForScourge = findAttackTargetForScourge();
				}
				
				if(enemyUnitForScourge == null){
					if(unit.isIdle()){
						if(rPosition.getDistance(enemyMainBaseLocation) > Config.TILE_SIZE*35){
							commandUtil.attackMove(unit, rPosition);
						}
					}
				}else{
					commandUtil.attackUnit(unit, enemyUnitForScourge);
					if(Config.DEBUG) MyBotModule.Broodwar.drawLineMap(unit.getPosition(), enemyUnitForScourge.getTargetPosition(), Color.Red);
				}
			}
			hasCommanded = true;
		}
		
		return hasCommanded;
	}
	
	// 퀸
	Unit enemyUnitForQueen = null;
	boolean controlSpecialUnitType4(Unit unit) {
		boolean hasCommanded = false;
		
		if(enemyMainBaseLocation == null) return true;
		if(enemySecondChokePoint == null) return true;
			
		if (unit.getType() == UnitType.Zerg_Queen) {
			
			Position rPosition = getRandomPosition();
			
			if(commandUtil.IsValidUnit(enemyUnitForQueen) == false){
				enemyUnitForQueen = findAttackTargetForQueen();
				//System.out.println("enemyUnitForQueen : " + enemyUnitForQueen.getID() + " " + enemyUnitForQueen.getType());
			}
			
			
			// defenseMode 일 경우
			if (combatState == CombatState.defenseMode) {
				
				if (unit.getEnergy() > 150 && myPlayer.hasResearched(TechType.Spawn_Broodlings)) {
					
					if(unit.isIdle()){
						if(enemyUnitForQueen == null){
							commandUtil.move(unit, rPosition);
						}else{
							// sc76.choi 가까이 있으면 
							unit.useTech(TechType.Spawn_Broodlings, enemyUnitForQueen);
							System.out.println("defence Use Spawn_Broodlings enemyUnitCount total : " + unit.getID() + " " + enemyUnitForQueen.getID() + " " + enemyUnitForQueen.getType() + " " + enemyUnitForQueen.getPosition());
							if(Config.DEBUG) MyBotModule.Broodwar.drawLineMap(unit.getPosition(), enemyUnitForQueen.getTargetPosition(), Color.Black);
						}
					}
				}
				// sc76.choi 디펜스 모드 이나, 에너지가 없을 때
				else{
					if(unit.isIdle()){
						commandUtil.move(unit, DEFENCE_POSITION);
					}
				}
			}
			// 공격 모드 일때
			else{
				
				if (unit.getEnergy() > 150 && myPlayer.hasResearched(TechType.Spawn_Broodlings)) {

					if(unit.isIdle()){
						
						if(enemyUnitForQueen == null){
							if(rPosition.getDistance(enemyMainBaseLocation) > Config.TILE_SIZE*35){
								commandUtil.move(unit, rPosition);
							}
						}else{
							// sc76.choi 가까이 있으면 
							unit.useTech(TechType.Spawn_Broodlings, enemyUnitForQueen);
							if(Config.DEBUG) System.out.println("attack Use Spawn_Broodlings enemyUnitCount total : " + unit.getID() + " " + enemyUnitForQueen.getID() + " " + enemyUnitForQueen.getType() + " " + enemyUnitForQueen.getPosition());
							if(Config.DRAW) MyBotModule.Broodwar.drawLineMap(unit.getPosition(), enemyUnitForQueen.getTargetPosition(), Color.White);
						}
					}
				}
				// sc76.choi 공격 모드이나 에너지가 없으면
				else{
					commandUtil.move(unit, DEFENCE_POSITION);
				}
			}
			hasCommanded = true;
		}
		
		return hasCommanded;
	}
	
	// sc76.choi Defence 모드 일때, 실행한다. 
	// TODO 적의 거리를 따져, 가까운 유닛만 반환해야 한다. 안그러면 계속 싸운다.
    private boolean isTimeToAirDefence() {
    	
    	if(bTimeToAirDefence) return true;
        Unit target = null;
        
        for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
        	if(unit != null){
        		if(Race.Terran == enemyRace){
        			
        			if(unit.getType() == UnitType.Terran_Starport){
	       				bTimeToAirDefence = true;
	       				break;
	       			}else if(unit.getType() == UnitType.Terran_Wraith){
	       				bTimeToAirDefence = true;
	       				break;
	       			}else if(unit.getType() == UnitType.Terran_Dropship){
	       				bTimeToAirDefence = true;
	       				break;
	       			}else if(unit.getType() == UnitType.Terran_Valkyrie){
	       				bTimeToAirDefence = true;
	       				break;
	       			}else if(unit.getType() == UnitType.Terran_Science_Vessel){
	       				bTimeToAirDefence = true;
	       				break;
	       			}else if(unit.getType() == UnitType.Terran_Battlecruiser){
	       				bTimeToAirDefence = true;
	       				break;
	       			}
        		}else if (Race.Protoss == enemyRace){
        			if(unit.getType() == UnitType.Protoss_Stargate){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Protoss_Shuttle){
        				bTimeToAirDefence = true;
	        			break;
        			}else if(unit.getType() == UnitType.Protoss_Observer){
        				bTimeToAirDefence = true;
	        			break;
        			}else if(unit.getType() == UnitType.Protoss_Scout){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Protoss_Corsair){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Protoss_Carrier){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Protoss_Fleet_Beacon){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Protoss_Interceptor){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Protoss_Dark_Templar){
        				bTimeToAirDefence = true;
		        		break;
        			}
        			
        			
        		}else if (Race.Zerg == enemyRace){
        			if(unit.getType() == UnitType.Zerg_Spire){
        				bTimeToAirDefence = true;
	        			break;
        			}else if(unit.getType() == UnitType.Zerg_Lurker){
        				bTimeToAirDefence = true;
	        			break;
        			}else if(unit.getType() == UnitType.Zerg_Lurker_Egg){
        				bTimeToAirDefence = true;
	        			break;
        			}else if(unit.getType() == UnitType.Zerg_Mutalisk){
        				bTimeToAirDefence = true;
	        			break;
        			}else if(unit.getType() == UnitType.Zerg_Scourge){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Zerg_Queen){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Zerg_Guardian){
        				bTimeToAirDefence = true;
		        		break;
        			}else if(unit.getType() == UnitType.Zerg_Devourer){
        				bTimeToAirDefence = true;
		        		break;
        			}
        		}else{
	       			// 	
	       		}
        	}
        }
        
        return bTimeToAirDefence;
    }

    public void isTimeToBuildState() {

    	if(enemyMainBaseLocation == null) return;
    	
    	// 적의 AdvancedDefenceBuildning 수
    	int countEnemyAdvancedDefenceBuilding = 0;
    	
    	// 적의 BasicCombatUnit 수    	
    	int countEnemyBasicCombatUnitType = 0;
    	
    	// 적의 BasicCombatBuilding 수    	
    	int countEnemyBasicCombatBuildingType = 0;    	
    	
    	// 적의 AdvancedCombatUnit 수    	
    	int countEnemyAdvancedCombatUnitType = 0;

    	// 적의 VultureCombatUnit 수    	
    	int countEnemyBunkDefenceBuildingType = 0;
    	
    	// 적의 VultureCombatUnit 수    	
    	int countEnemyVultureCombatUnitType = 0;
    	
    	// 적의 GoliathCombatUnit 수    	
    	int countEnemyGoliathCombatUnitType = 0;
    	
    	// 적의 TankCombatUnit 수    	
    	int countEnemyTankCombatUnitType = 0;    	
    	
    	// 적의 ClockingCombatUnit 수
    	int countClockingCombatUnitType = 0;
    	
    	///////////////////////////////////////////////////////////////////////////////////////////////////////
    	// 빌드 판단을 위해 적의 상태를 확인 한다.
    	///////////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	if(InformationManager.Instance().getUnitData(enemyPlayer) != null){
    		
			Iterator<Integer> it = InformationManager.Instance().getUnitData(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
			while(it.hasNext()){
				UnitInfo ui = InformationManager.Instance().getUnitData(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
				
				// valid 체크
				if(commandUtil.IsValidUnit(ui.getUnit()) == false){
					continue;
				}
				
				if(enemyRace == Race.Protoss){
					
					if(ui.getType() == UnitType.Protoss_Photon_Cannon){
						countEnemyAdvancedDefenceBuilding++;
					}
					
					if(ui.getType() == UnitType.Protoss_Zealot){
						countEnemyBasicCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Protoss_Dragoon){
						countEnemyAdvancedCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Protoss_Dark_Templar
							&& myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) == 0){
						countClockingCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Protoss_Cybernetics_Core
						&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 5)){
						buildState = BuildState.blockDefence2Dragon8_P;
					}
					
					if(ui.getType() == UnitType.Protoss_Fleet_Beacon || ui.getType() == UnitType.Protoss_Carrier || ui.getType() == UnitType.Protoss_Interceptor){
						buildState = BuildState.carrier_P;
					}
				}else if(enemyRace == Race.Terran){
					
					// vulture_Galia_Tank_T
					if(ui.getType() == UnitType.Terran_Marine){
						countEnemyBasicCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Terran_Medic){
						countEnemyAdvancedCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Terran_Bunker){
						countEnemyBunkDefenceBuildingType++;
					}
										
					if(ui.getType() == UnitType.Terran_Barracks){
						countEnemyBasicCombatBuildingType++;
					}
					
					if(ui.getType() == UnitType.Terran_Vulture){
						countEnemyVultureCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Terran_Goliath){
						countEnemyGoliathCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
						|| ui.getType() == UnitType.Terran_Siege_Tank_Siege_Mode){
						countEnemyTankCombatUnitType++;
					}
					
				}else if(enemyRace == Race.Zerg){
					
					if(ui.getType() == UnitType.Zerg_Zergling){
						countEnemyBasicCombatUnitType++;
					}
					
					if(ui.getType() == UnitType.Zerg_Sunken_Colony){
						countEnemyAdvancedDefenceBuilding++;
					}
					
					if(getCountUnitTypeInPosition(enemyPlayer, UnitType.Zerg_Sunken_Colony, enemyMainBaseLocation.getPosition(), Config.TILE_SIZE*12) > 2
							&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 5)){
						
						buildState = BuildState.blockTheFirstChokePoint_Z;
					}
					
					// sc76.choi 빠른 저글링 러쉬
					if((existUnitTypeInRegion(enemyPlayer, UnitType.Zerg_Drone, myMainBaseLocation.getRegion(), false, false)
						|| existUnitTypeInRegion(enemyPlayer, UnitType.Zerg_Zergling, myMainBaseLocation.getRegion(), false, false)
						|| existUnitTypeInRegion(enemyPlayer, UnitType.Zerg_Zergling, myFirstExpansionLocation.getRegion(), false, false)
					)
						 && MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 5)){
						buildState = BuildState.fastZergling_Z;
					}
					
					// sc76.choi 빠른 뮤탈 러쉬
					if((enemyPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
							|| enemyPlayer.incompleteUnitCount(UnitType.Zerg_Spire) > 0
//							|| enemyPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
//							|| enemyPlayer.incompleteUnitCount(UnitType.Zerg_Lair) > 0
							|| enemyPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 1)
						&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 6)){
							buildState = BuildState.fastMutalisk_Z;
					}
					
					// sc76.choi 럴커 출현 
					if(ui.getType() == UnitType.Zerg_Lurker
							&& myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) == 0){
						countClockingCombatUnitType++;
						buildState = BuildState.lurker_Z;
					}
				}
			}
		}
    	
    	//////////////////////////////////////////////////////////////////////////////////////////////////
    	// sc76.choi 선택된 빌드에 의해 조정
    	
		/////////////////////////////////////////////////////////////////////////////////////////
		// 프로토스    	
    	if(enemyRace == Race.Protoss){
    		
    		if(countEnemyBasicCombatUnitType >= 5
    			&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 6)){
    			
    			buildState = BuildState.hardCoreZealot_P;
    			
    		}
    		
    		if(countEnemyBasicCombatUnitType >= 1
    			&& existUnitTypeInRegion(enemyPlayer, UnitType.Protoss_Zealot, myFirstExpansionLocation.getRegion(), false, false) == true
        		&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 6)){
    			
    			buildState = BuildState.hardCoreZealot_P;
        	}
    		
    		// hardCoreZealot_P 의 config
    		if(buildState == BuildState.hardCoreZealot_P){
    			// Config 조정
    			Config.BuildingDefenseTowerSpacing = 2;
    			
       			Config.necessaryNumberOfDefenseBuilding1AgainstProtoss = 3;
       			Config.necessaryNumberOfDefenseBuilding2AgainstProtoss = 3;
       			
       			Config.necessaryNumberOfDefenceUnitType1AgainstProtoss = 9;
       			Config.necessaryNumberOfCombatUnitType1AgainstProtoss = 16;
       			
       			Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = 8;
       			Config.necessaryNumberOfCombatUnitType2AgainstProtoss = 14;
       			
    			// sc76.choi 저글링 4 마리 추가	    		
	    		excuteUrgenturgent_Add_Zergling1();
	    		// sc76.choi 본진에 성큰하나 건설
	    		excuteUrgentDefenceConstructionInBaseLocation(myFirstExpansionLocation);
    		}
    		
    		if(countEnemyBasicCombatUnitType >= 6
        			&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 7)){
        			
        		buildState = BuildState.blockDefence2Dragon8_P;
        			
        	}
    		
    		// 포토캐넌이 2개이상이고, 드라곤이 6개 이상 혹은 드라곤이 8개 이상 있다면
    		// TODO 가스 일꾼 수를 줄여야 한다. 1 or 2
    		if((countEnemyAdvancedDefenceBuilding >= 2 && countEnemyAdvancedCombatUnitType >= 6)
    			 || countEnemyAdvancedCombatUnitType >= 8){
    			
    			buildState = BuildState.blockDefence2Dragon8_P;
    			
			}

    		if(countEnemyAdvancedDefenceBuilding  >= 2 
    			|| enemyPlayer.allUnitCount(UnitType.Protoss_Photon_Cannon) >= 2){
    			
    			buildState = BuildState.blockDefence2Dragon8_P;
    			
			}
    		
    		if(countEnemyBasicCombatUnitType >= 5 && countEnemyAdvancedCombatUnitType >= 4){
    			
       			buildState = BuildState.blockDefence2Dragon8_P;
			}

    		// blockDefence2Dragon8_P 의 config
    		if(buildState == BuildState.blockDefence2Dragon8_P){
    			// Config 조정
    			// 저글링
       			Config.necessaryNumberOfDefenceUnitType1AgainstProtoss = 9;
       			Config.necessaryNumberOfCombatUnitType1AgainstProtoss = 16;
       			
       			// 히드라
       			Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = 10;
       			Config.necessaryNumberOfCombatUnitType2AgainstProtoss = 18;
       			
       			// 럴커
       			Config.necessaryNumberOfDefenceUnitType3AgainstProtoss = 1;
       			Config.necessaryNumberOfCombatUnitType3AgainstProtoss = 4;
       			
       			// 저글링, 히드라 생산제한
       			Config.maxNumberOfTrainUnitType1AgainstProtoss = 18;
       			Config.maxNumberOfTrainUnitType2AgainstProtoss = 35;
    	   		
       			// 멀티에 증설
       			excuteUrgentCombatConstructionInMultiBase();
       			
       			if(getCountCombatType2() >= 2){
	       			// 즉각 해처리 증설
	       			excuteUrgentCombatConstructionInBaseLocation(mySecondChokePoint.getCenter().toTilePosition());
       			}
       			
    	   		if(getCountCombatType2() >= 6){
    	   			
    	   			if(myKilledCombatUnitCount2 >= 30){
    	   				Config.BuildingDefenseTowerSpacing = 1;
    	   				Config.necessaryNumberOfDefenseBuilding1AgainstProtoss = 5;
    		   			Config.necessaryNumberOfDefenseBuilding2AgainstProtoss = 5;
    	   			}else{
    	   				Config.BuildingDefenseTowerSpacing = 1;
    		   			Config.necessaryNumberOfDefenseBuilding1AgainstProtoss = 5;
    		   			Config.necessaryNumberOfDefenseBuilding2AgainstProtoss = 5;
    	   			}
    	   			
    	   			// 메인 증설
    	   			excuteUrgentCombatConstructionInBaseLocation2();
    	   			
    			}
    		}
    		
    		
//    		if (countEnemyAdvancedCombatUnitType > 0
//    				&& MyBotModule.Broodwar.getFrameCount() > (24 * 60 * 7)){
//    			buildState = BuildState.blockDefence2Dragon8_P;
//    		}
//    		
//    		if (enemyPlayer.allUnitCount(UnitType.Protoss_Cybernetics_Core) > 0
//    				&& MyBotModule.Broodwar.getFrameCount() > (24 * 60 * 7)){
//    			buildState = BuildState.blockDefence2Dragon8_P;
//    		}
    		
    		// sc76.choi 다시 normalMmode로 변경해 준다. 
    		// TODO 이전 모드로 변경해 주면 좋을까?
    		if(countClockingCombatUnitType > 0
    			&& myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) <= 0){
    			
    			buildState = BuildState.darkTemplar_P;
    			
    			
    		}
//    		else if (buildState == BuildState.darkTemplar_P
//        			&& (myPlayer.isUpgrading(UpgradeType.Pneumatized_Carapace) == true
//        			     || myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) > 0)){
//    			
//    			buildState = BuildState.normalMode;
//    		}
    		
    		if((countEnemyAdvancedDefenceBuilding  >= 4 
    			|| enemyPlayer.allUnitCount(UnitType.Protoss_Photon_Cannon) >= 4)
    			&& MyBotModule.Broodwar.getFrameCount() > (24 * 60 * 7)){
    			
    			buildState = BuildState.carrier_P;
    			
    			// sc76.choi 방어 타입 갯수 늘림, 정수로 할당
	   			Config.necessaryNumberOfDefenceUnitType1AgainstProtoss = 4;
	   			
	   			Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = 15;
	   			Config.necessaryNumberOfCombatUnitType2AgainstProtoss = 38;
	   			
	   			Config.maxNumberOfTrainUnitType1AgainstProtoss = 10;
	   			Config.maxNumberOfTrainUnitType2AgainstProtoss = 50;
	   			Config.maxNumberOfTrainUnitType2AgainstProtoss = 1;
	   			
				excuteUrgentCombatConstructionInBaseLocation(mySecondChokePoint.getCenter().toTilePosition());
    			
    		}
		}
		/////////////////////////////////////////////////////////////////////////////////////////
		// 테란
    	else if(enemyRace == Race.Terran){
			
	   		if(buildState != BuildState.hardCoreMarine_T
	   			&& (countEnemyBasicCombatUnitType >= 8 || countEnemyAdvancedCombatUnitType >= 1 || countEnemyBasicCombatBuildingType >= 2)
	    			&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 7)){
	   			
	    			buildState = BuildState.hardCoreMarine_T;
	    			
	    			Config.BuildingDefenseTowerSpacing = 2;
	    			
	       			Config.necessaryNumberOfDefenseBuilding1AgainstTerran = 3;
	       			Config.necessaryNumberOfDefenseBuilding2AgainstTerran = 3;
	       			
	       			Config.necessaryNumberOfDefenceUnitType1AgainstTerran = 2; // 저글링
	       			Config.necessaryNumberOfCombatUnitType1AgainstTerran = 6;
	       			
	    			// sc76.choi 저글링 4 마리 추가	    		
		    		//excuteUrgenturgent_Add_Zergling1();
		    		
		    		// sc76.choi 본진에 성큰하나 건설
		    		//excuteUrgentDefenceConstructionInBaseLocation(myFirstExpansionLocation);
		    		
	   		}
	   		
	   		if(countEnemyVultureCombatUnitType >= 2
	    			&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 8)){
	   			
	    			buildState = BuildState.fastVulture_T;
	    			
	    			if(enemyPlayer.getName().equals("")){
	    				
	    				// sc76.choi 방어 타입 갯수 늘림, 정수로 할당
			   			Config.necessaryNumberOfDefenceUnitType1AgainstProtoss = 4;

			   			Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = 2;
			   			Config.necessaryNumberOfCombatUnitType2AgainstProtoss = 10;

			   			Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = 3;
			   			Config.necessaryNumberOfCombatUnitType2AgainstProtoss = 24;
			   			
			   			Config.necessaryNumberOfDefenceUnitType3AgainstProtoss = 1;
			   			Config.necessaryNumberOfCombatUnitType3AgainstProtoss = 7;
			   			
			   			Config.maxNumberOfTrainUnitType1AgainstProtoss = 10;
			   			Config.maxNumberOfTrainUnitType2AgainstProtoss = 40;
			   			Config.maxNumberOfTrainUnitType3AgainstProtoss = 10;
			   			
	    			}else{
	    				
	    				Config.BuildingDefenseTowerSpacing = 3;
	    				Config.necessaryNumberOfDefenseBuilding1AgainstTerran = 2;
	    				Config.necessaryNumberOfDefenseBuilding2AgainstTerran = 2;
	    				
	    				Config.necessaryNumberOfDefenceUnitType1AgainstTerran = 2; // 저글링
	    				Config.necessaryNumberOfCombatUnitType1AgainstTerran = 8;
	    			}
	       			
	       			// 즉각 해처리 증설
	       			excuteUrgentCombatConstructionInBaseLocation(myMainBaseLocation.getTilePosition());
	       			
	    	}
	    	
	   		if (countEnemyGoliathCombatUnitType >= 4 && countEnemyTankCombatUnitType >= 2){
	    		
    			buildState = BuildState.vulture_Galia_Tank_T;
    			
       			Config.necessaryNumberOfDefenceUnitType2AgainstTerran = 6;
       			Config.necessaryNumberOfCombatUnitType2AgainstTerran = 11;
       			
       			Config.maxNumberOfTrainUnitType2AgainstTerran = 24;
       			
       			Config.BuildingDefenseTowerSpacing = 3;
       			
       			Config.necessaryNumberOfDefenseBuilding1AgainstTerran = 5;
       			Config.necessaryNumberOfDefenseBuilding2AgainstTerran = 5;
	    	}
	   		
	   		
	   		if(enemyMainBaseLocation !=null
	   			&& (existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Missile_Turret, enemyMainBaseLocation.getRegion(), false, false)
	   			|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Bunker, enemyMainBaseLocation.getRegion(), false, false)
	   			|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Tank_Mode, enemyMainBaseLocation.getRegion(), false, false)
	   			|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Siege_Mode, enemyMainBaseLocation.getRegion(), false, false))
	   			){
	   			
	   			buildState = BuildState.blockTheFirstChokePoint_T;
	   			
	   		}
	   		
	   		if(enemyMainBaseLocation !=null
		   		&& (existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Bunker, enemyMainBaseLocation.getRegion(), false, false)
		   		|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Tank_Mode, enemyMainBaseLocation.getRegion(), false, false)
		   		|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Siege_Mode, enemyMainBaseLocation.getRegion(), false, false))
		   		){
	   			
		   			buildState = BuildState.blockTheFirstChokePoint_T;
		   			
		   		}
	   		
	   		if(enemyFirstExpansionLocation !=null
		   		&& (existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Missile_Turret, enemyFirstExpansionLocation.getRegion(), false, false)
		   		|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Bunker, enemyFirstExpansionLocation.getRegion(), false, false)
		   		|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Tank_Mode, enemyFirstExpansionLocation.getRegion(), false, false)
		   		|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Siege_Mode, enemyFirstExpansionLocation.getRegion(), false, false))
		   		){
	   			
		   			buildState = BuildState.blockTheSecondChokePoint_T;
		   			
		   	}
	   		
	   		if(enemyFirstExpansionLocation !=null
		   		&& (existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Bunker, enemyFirstExpansionLocation.getRegion(), false, false)
		   		|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Tank_Mode, enemyFirstExpansionLocation.getRegion(), false, false)
		   		|| existUnitTypeInRegion(enemyPlayer, UnitType.Terran_Siege_Tank_Siege_Mode, enemyFirstExpansionLocation.getRegion(), false, false))
		   		){
	   			
		   			buildState = BuildState.blockTheSecondChokePoint_T;
		   			
		   	}
	   		
	   		if(myKilledCombatUnitCount3 >= 4 
	   			&& (buildState == BuildState.vulture_Galia_Tank_T
	   				|| buildState == BuildState.fastVulture_T
	   				|| buildState == BuildState.Tank_T
	   				|| buildState == BuildState.blockTheFirstChokePoint_T
	   				|| buildState == BuildState.blockTheSecondChokePoint_T)){
	   			
	   			buildState = BuildState.totally_attack_T;
	   			
				Config.BuildingDefenseTowerSpacing = 3;
				
	   			Config.necessaryNumberOfDefenseBuilding1AgainstTerran = 3;
	   			Config.necessaryNumberOfDefenseBuilding2AgainstTerran = 3;
	   			
				// sc76.choi 방어 타입 갯수 늘림, 정수로 할당
				Config.maxNumberOfTrainUnitType1AgainstTerran = 30; 		
				Config.necessaryNumberOfDefenceUnitType1AgainstTerran = 6;
				Config.necessaryNumberOfCombatUnitType1AgainstTerran = 12;
				
				Config.maxNumberOfTrainUnitType2AgainstTerran = 24;
				Config.necessaryNumberOfDefenceUnitType2AgainstTerran = 6;
				Config.necessaryNumberOfCombatUnitType2AgainstTerran = 12;
				
				Config.maxNumberOfTrainUnitType3AgainstTerran = 10;
				Config.necessaryNumberOfDefenceUnitType3AgainstTerran = 1;
				Config.necessaryNumberOfCombatUnitType3AgainstTerran = 3;
				
	   			// 즉각 해처리 증설
	   			excuteUrgentCombatConstructionInBaseLocation(myMainBaseLocation.getTilePosition());
	   		}
	   		
		}else if(enemyRace == Race.Zerg){
			
			if((countEnemyBasicCombatUnitType >= 10)
	    			&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 6)){
				
				buildState = BuildState.fastZergling_Z;
				
	   			Config.necessaryNumberOfDefenceUnitType1AgainstZerg = 11;
	   			Config.necessaryNumberOfCombatUnitType1AgainstZerg = 17;
	   			
				// sc76.choi 저글링 4 마리 추가	    		
				excuteUrgenturgent_Add_Zergling1();
				// sc76.choi 본진에 성큰하나 건설
				excuteUrgentDefenceConstructionInBaseLocation(myMainBaseLocation);
			}
			
			if(buildState == BuildState.fastZergling_Z
					&& (countEnemyBasicCombatUnitType >= 14)
	    			&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 10)){
				
				buildState = BuildState.superZergling_Z;
				
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = 8;
	   			Config.necessaryNumberOfCombatUnitType1AgainstZerg = 22;
	   			
	   			Config.necessaryNumberOfDefenceUnitType2AgainstZerg = 8;
	   			Config.necessaryNumberOfCombatUnitType2AgainstZerg = 14;
	   			
	   			Config.maxNumberOfTrainUnitType1AgainstZerg = 30;
	   			Config.maxNumberOfTrainUnitType2AgainstZerg = 24;
	   			
			}
			
			if(countEnemyAdvancedDefenceBuilding >= 2
				&& MyBotModule.Broodwar.getFrameCount() < (24 * 60 * 6)){
				
				buildState = BuildState.superZergling_Z;
				
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = 8;
	   			Config.necessaryNumberOfCombatUnitType1AgainstZerg = 22;
	   			
	   			Config.necessaryNumberOfDefenceUnitType2AgainstZerg = 8;
	   			Config.necessaryNumberOfCombatUnitType2AgainstZerg = 14;
	   			
	   			Config.maxNumberOfTrainUnitType1AgainstZerg = 30;
	   			Config.maxNumberOfTrainUnitType2AgainstZerg = 24;
				
			}
			
			if(buildState == BuildState.fastMutalisk_Z){
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = 1;
	   			Config.necessaryNumberOfCombatUnitType1AgainstZerg = 4;
			}
					
			// 본진에 성큰이 두개 이상.
			if(buildState == BuildState.blockTheFirstChokePoint_Z){
				
	   			Config.necessaryNumberOfDefenceUnitType1AgainstZerg = 11;
	   			Config.necessaryNumberOfCombatUnitType1AgainstZerg = 18;
	   			
	   			Config.necessaryNumberOfDefenceUnitType2AgainstZerg = 7;
	   			Config.necessaryNumberOfCombatUnitType2AgainstZerg = 14;
			}
			
			
			
    		// sc76.choi 다시 normalMmode로 변경해 준다. 
    		// TODO 이전 모드로 변경해 주면 좋을까?
	    	if(countClockingCombatUnitType > 0
	    			&& myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) <= 0){
	    		
	    			buildState = BuildState.lurker_Z;
	    			
    		}else if (buildState == BuildState.lurker_Z
        			&& (myPlayer.isUpgrading(UpgradeType.Pneumatized_Carapace) == true
        			     || myPlayer.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) > 0)){
    			
    			buildState = BuildState.normalMode;
    		}
		}
    	
		if(buildState == BuildState.lurker_Z || buildState == BuildState.darkTemplar_P){
			if(enemyRace == Race.Protoss){
				Config.necessaryNumberOfDefenseBuilding1AgainstProtoss = 3;
	   			Config.necessaryNumberOfDefenseBuilding2AgainstProtoss = 3;
			}else if (enemyRace == Race.Zerg){
				Config.necessaryNumberOfDefenseBuilding1AgainstZerg = 2;
	   			Config.necessaryNumberOfDefenseBuilding2AgainstZerg = 2;
			}
		}
    }
    
	// KTH. Drop 명령  수행합니다
//	CircuitBreakr startLocation.getTilePosition()
//	11 시 => [7, 9]
//	 1 시 => [7, 118]
//	 7 시 => [117, 9]
//	 5 시 => [117, 118]
//	Spirit startLocation.getTilePosition()
//	11 시 => [7, 7]
//	 1 시 => [7, 117]
//	 7 시 => [117, 7]
//	 5 시 => [117, 117]
	boolean chkOverloadArrived = false;     // Overload Drop시 가로방향 위치시 미네랄 뒤쪽으로 이동하기 위한 체크
	boolean checkDrop = false;              // Drop 완료 여부       

	private void executeOverloadDrop() {
		
		if(enemyRace != Race.Terran){
			return;
		}
		
//		if (combatState == CombatState.attackStarted) {
		if(OverloadManager.Instance().getDropOverloadList().size() == 0) return;

		
		if(!checkDrop && myPlayer.getUpgradeLevel(UpgradeType.Ventral_Sacs) > 0) {
			if(OverloadManager.Instance().getDropOverloadList().size() < Config.COUNT_OVERLOAD_DROP) return;
			
			for (Unit unitOverload : OverloadManager.Instance().getDropOverloadList()) {
				if (unitOverload == null || unitOverload.exists() == false || unitOverload.getHitPoints() <= 0) continue;
			
				boolean chkLoadComplete = false;
				int cnt = 0;
				Position calPosition = null;
				for(Unit loadUnit : unitOverload.getLoadedUnits()) {
					cnt += loadUnit.getType().spaceRequired();
				}
				if(cnt != 8 ) {
					int unit_cnt = cnt;
//					for(Unit unit : myAllCombatUnitList) {	
					for(Unit unit : myPlayer.getUnits()) {
						if (unit == null || unit.exists() == false || unit.getHitPoints() <= 0) continue;
						if (unit.isLoaded()) continue;
						if ((unit.getType() == UnitType.Zerg_Hydralisk || unit.getType() == UnitType.Zerg_Zergling) && !unit.isBurrowed()) {
//								|| unit.getType() == UnitType.Zerg_Lurker) && !unit.isBurrowed()) {
							if(unit_cnt < 8 && (unit_cnt + unit.getType().spaceRequired()) > 8 ) continue;
							unit_cnt += unit.getType().spaceRequired();
//							if(unit.getType() == UnitType.Zerg_Lurker && unit.isBurrowed()) {
//								unit.unburrow();
//							}
							unit.rightClick(unitOverload);
							if(unit_cnt > 6) {
								chkLoadComplete = true;
								break;
							}
							
						}
					}
				} else {
					chkLoadComplete = true;
				}
				if(!chkLoadComplete) break;
			}
			
			for (Unit unitOverload : OverloadManager.Instance().getDropOverloadList()) {
				if (unitOverload == null || unitOverload.exists() == false || unitOverload.getHitPoints() <= 0) continue;
				int cnt = 0;
				for(Unit loadUnit : unitOverload.getLoadedUnits()) {
					cnt += loadUnit.getType().spaceRequired();
				}
				if(cnt > 6) {
					checkDrop = true;
				} else {
					checkDrop = false;
					break;
				}
			}
			if (checkDrop) {
				for (Unit unitOverload : OverloadManager.Instance().getDropOverloadList()) {
					if (unitOverload == null || unitOverload.exists() == false || unitOverload.getHitPoints() <= 0) continue;
					Position calPosition = null;
					calPosition = dropPosition(unitOverload);
					commandUtil.move(unitOverload, calPosition);
				}
			}
		}
		if(checkDrop) {
			for (Unit unitOverload : OverloadManager.Instance().getDropOverloadList()) {
				if (unitOverload == null || unitOverload.exists() == false || unitOverload.getHitPoints() <= 0) continue;
				
				Position calPosition = null;
				if(unitOverload.getLoadedUnits().size() != 0) {
					boolean enemyView1 = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(unitOverload.getTilePosition()),enemyPlayer, UnitType.Terran_Missile_Turret);
					boolean enemyView2 = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(unitOverload.getTilePosition()),enemyPlayer, UnitType.Protoss_Photon_Cannon);
					boolean enemyView3 = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(unitOverload.getTilePosition()),enemyPlayer, UnitType.Zerg_Sunken_Colony);
					boolean enemyView4 = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(unitOverload.getTilePosition()),enemyPlayer, UnitType.Zerg_Spore_Colony);
					boolean enemyView5 = InformationManager.Instance().existsPlayerBuildingInRegion(BWTA.getRegion(unitOverload.getTilePosition()),enemyPlayer, UnitType.Terran_Bunker);
					
					if(enemyView1 || enemyView2 || enemyView3 || enemyView4 || enemyView5) {
						for(int i=0; i<unitOverload.getLoadedUnits().size(); i++) {
							Unit unit = unitOverload.getLoadedUnits().get(i);
							unitOverload.unload(unit);
							if(unit.getType() == UnitType.Zerg_Lurker) { // 럴커가 버로우를 하지 않음
								unit.burrow();
							}
						}
						//commandUtil.move(unitOverload, myMainBaseLocation.getPosition());
					}
					if(unitOverload.getOrderTargetPosition().getX() == 0 && unitOverload.getOrderTargetPosition().getY() == 0) { // 목적지에 도달했을때
						if (chkOverloadArrived) { // Drop 위치에 왔을때
							for(int i=0; i<unitOverload.getLoadedUnits().size(); i++) {
								Unit unit = unitOverload.getLoadedUnits().get(i);
								unitOverload.unload(unit);
								if(unit.getType() == UnitType.Zerg_Lurker) {
									unit.burrow();
								}
							}
//							unitOverload.unloadAll(true);
						} else {
							calPosition = dropPosition(unitOverload);
							commandUtil.move(unitOverload, calPosition);
						}
					}					
//				} else {
//					OverloadManager.Instance().getOverloadData().setOverloadJob(unitOverload, OverloadData.OverloadJob.Idle, (Unit)null);
//					OverloadManager.Instance().destroyDropOverloadList(unitOverload);
//					commandUtil.move(unitOverload, myMainBaseLocation.getPosition());
				}
				
			}
		}
		// 계속 Drop 시도 -> 한번만 Drop할 경우 주석 처리 
//		if (OverloadManager.Instance().getDropOverloadList().size()==0) {
//			checkDrop = false;
//			chkOverloadArrived = false;
//			System.out.println("checkDrop ==> " + checkDrop);
//		}
	}
	
	// KTH. Drop 위치 찾기한다
	// Overload Drop 공격시 벽에 붙어서 드랍이동하는 위치 설정 	
	private Position dropPosition(Unit myUnit) {
		
		Position calPosition;
		if(myMainBaseLocation.getTilePosition().getX() == enemyMainBaseLocation.getTilePosition().getX()
				&& myMainBaseLocation.getTilePosition().getY() != enemyMainBaseLocation.getTilePosition().getY()) { // 아군, 적군 세로로 위치
			// 세로방향 위치일때는 중앙으로 이동후 위,아래로 이동;
			if(myUnit.getPosition().getX() == enemyMainBaseLocation.getTilePosition().getX()*Config.TILE_SIZE) { // Overload와 적베이스기지에 도달했을때 자원 채취 뒤쪽으로 이동
				chkOverloadArrived = true;
				if(enemyMainBaseLocation.getTilePosition().getX() < MyBotModule.Broodwar.mapWidth()/2) { // 왼쪽 자원 채취 뒤쪽으로 이동
					calPosition = new Position(Config.TILE_SIZE, enemyMainBaseLocation.getTilePosition().getY()*Config.TILE_SIZE);
				} else { // 오른쪽 자원 채취 뒤쪽으로 이동
					calPosition = new Position((MyBotModule.Broodwar.mapWidth()-1)*Config.TILE_SIZE, enemyMainBaseLocation.getTilePosition().getY()*Config.TILE_SIZE);
				}
			} else if(myUnit.getPosition().getY() == Config.TILE_SIZE || myUnit.getPosition().getY() == (MyBotModule.Broodwar.mapHeight()-1)*Config.TILE_SIZE-25) { // 벽에 도달했을때 이동, -25는 경계선의 공백이 존재해서 빼줌
				calPosition = new Position(enemyMainBaseLocation.getTilePosition().getX()*Config.TILE_SIZE, myUnit.getPosition().getY());
			} else if(myMainBaseLocation.getTilePosition().getY() < MyBotModule.Broodwar.mapHeight()/2) { // 아래쪽 벽으로 이동
				if(myUnit.getPosition().getY()<2000) {
					calPosition = new Position(2000,2000); // 세로방향 위치일때는 중앙으로 이동;
				} else {
					calPosition = new Position(myUnit.getPosition().getX(), (MyBotModule.Broodwar.mapHeight()-1)*Config.TILE_SIZE-25); // -25는 경계선의 공백이 존재해서 빼줌
				}
			} else { // 위쪽 벽으로 이동
				if(myUnit.getPosition().getY()>2000) {
					calPosition = new Position(2000,2000); // 세로방향 위치일때는 중앙으로 이동;
				} else {
					calPosition = new Position(myUnit.getPosition().getX(), Config.TILE_SIZE);
				}
			}

		} else if(myMainBaseLocation.getTilePosition().getX() != enemyMainBaseLocation.getTilePosition().getX()
				&& myMainBaseLocation.getTilePosition().getY() == enemyMainBaseLocation.getTilePosition().getY()) { // 아군, 적군 가로로 위치
	
			if(myUnit.getPosition().getX() == enemyMainBaseLocation.getTilePosition().getX()*Config.TILE_SIZE) { // Overload와 적베이스기지에 도달했을때 자원 채취 뒤쪽으로 이동
				chkOverloadArrived = true;
				if(enemyMainBaseLocation.getTilePosition().getX() < MyBotModule.Broodwar.mapWidth()/2) { // 왼쪽 자원 채취 뒤쪽으로 이동
					calPosition = new Position(Config.TILE_SIZE, enemyMainBaseLocation.getTilePosition().getY()*Config.TILE_SIZE);
				} else { // 오른쪽 자원 채취 뒤쪽으로 이동
					calPosition = new Position((MyBotModule.Broodwar.mapWidth()-1)*Config.TILE_SIZE, enemyMainBaseLocation.getTilePosition().getY()*Config.TILE_SIZE);
				}
			} else if(myUnit.getPosition().getY() == Config.TILE_SIZE || myUnit.getPosition().getY() == (MyBotModule.Broodwar.mapHeight()-1)*Config.TILE_SIZE-25) { // 벽에 도달했을때 이동, -25는 경계선의 공백이 존재해서 빼줌
				calPosition = new Position(enemyMainBaseLocation.getTilePosition().getX()*Config.TILE_SIZE, myUnit.getPosition().getY());
			} else if(myMainBaseLocation.getTilePosition().getY() < MyBotModule.Broodwar.mapHeight()/2) { // 위쪽 벽으로 이동
				calPosition = new Position(myUnit.getPosition().getX(), Config.TILE_SIZE);
			} else { // 아래쪽 벽으로 이동
				calPosition = new Position(myUnit.getPosition().getX(), (MyBotModule.Broodwar.mapHeight()-1)*Config.TILE_SIZE-25); // -25는 경계선의 공백이 존재해서 빼줌
			}
		} else { // 아군, 적군 대각선으로 위치
			if(myUnit.getPosition().getX() == enemyMainBaseLocation.getTilePosition().getX()*Config.TILE_SIZE) { // Overload와 적베이스기지에 도달했을때 자원 채취 뒤쪽으로 이동
				chkOverloadArrived = true;
				if(enemyMainBaseLocation.getTilePosition().getX() < MyBotModule.Broodwar.mapWidth()/2) { // 왼쪽 자원 채취 뒤쪽으로 이동
					calPosition = new Position(Config.TILE_SIZE, enemyMainBaseLocation.getTilePosition().getY()*Config.TILE_SIZE);
				} else { // 오른쪽 자원 채취 뒤쪽으로 이동
					calPosition = new Position((MyBotModule.Broodwar.mapWidth()-1)*Config.TILE_SIZE, enemyMainBaseLocation.getTilePosition().getY()*Config.TILE_SIZE);
				}
			} else if(myUnit.getPosition().getY() == Config.TILE_SIZE || myUnit.getPosition().getY() == (MyBotModule.Broodwar.mapHeight()-1)*Config.TILE_SIZE-25) { // 벽에 도달했을때 이동, -25는 경계선의 공백이 존재해서 빼줌
				calPosition = new Position(enemyMainBaseLocation.getTilePosition().getX()*Config.TILE_SIZE, myUnit.getPosition().getY());
			} else if(myMainBaseLocation.getTilePosition().getY() < MyBotModule.Broodwar.mapHeight()/2) { // 아래쪽 벽으로 이동
				calPosition = new Position(myUnit.getPosition().getX(), (MyBotModule.Broodwar.mapHeight()-1)*Config.TILE_SIZE-25); // -25는 경계선의 공백이 존재해서 빼줌
			} else { // 위쪽 벽으로 이동
				calPosition = new Position(myUnit.getPosition().getX(), Config.TILE_SIZE);
			}
		}
		return calPosition;
	}
		
    
	/// StrategyManager 의 수행상황을 표시합니다
	private final Character brown = '';
	private final char red = '';
	private final char teal = '';
	private final char blue = '';
	private final char purple = '';
	private final char white = '';
	private void drawStrategyManagerStatus() {

		MyBotModule.Broodwar.drawCircleMap(StrategyManager.Instance().DEFENCE_POSITION, Config.TILE_SIZE*6, Color.Green);
		
		MyBotModule.Broodwar.drawTextScreen(410, 20, red + "Avail Mi : " + selfAvailableMinerals);
		MyBotModule.Broodwar.drawTextScreen(510, 20, red + "Avail Ga : " + selfAvailableGas);
		// 전투 상황
		MyBotModule.Broodwar.drawTextScreen(410, 30, red + "CombatState : " + combatState.toString());
		MyBotModule.Broodwar.drawTextScreen(410, 40, red + "BuildState : " + buildState);
		MyBotModule.Broodwar.drawTextScreen(410, 50, red + "Attak Pos. : " + TARGET_TILEPOSITION + TARGET_POSITION);
		MyBotModule.Broodwar.drawTextScreen(410, 60, red + "Attak Pos.Z: " + TARGET_TILEPOSITION_Z + TARGET_POSITION_Z);
		MyBotModule.Broodwar.drawTextScreen(410, 70, red + "Defence Pos. : " + DEFENCE_TILEPOSITION + DEFENCE_POSITION);
		
		//MyBotModule.Broodwar.drawTextScreen(440, 70, "isDefence : " + isNecessaryNumberOfDefencedUnitType());
		MyBotModule.Broodwar.drawTextScreen(410, 80, "myUnit : " + 
		"[" + getCountCombatType1() + "/" + necessaryNumberOfCombatUnitType1 +"/" +  necessaryNumberOfDefenceUnitType1 +"]" +
		"[" + getCountCombatType2() + "/" + necessaryNumberOfCombatUnitType2 +"/" +  necessaryNumberOfDefenceUnitType2 +"]" +
		"[" + getCountCombatType3() + "/" + necessaryNumberOfCombatUnitType3 +"/" +  necessaryNumberOfDefenceUnitType3 +"]");
		//" Air : " + bTimeToAirDefence);
		MyBotModule.Broodwar.drawTextScreen(410, 90, "Ground : " + necessaryNumberOfDefenseBuilding1 + " " + necessaryNumberOfDefenseBuilding2 + ", Air : " + bTimeToAirDefence);		
				

		if(!Config.DRAW){
			return;
		}
		
		int y = 170;
		int t = 240;
		
		// 아군 공격유닛 숫자 및 적군 공격유닛 숫자
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My Hatchery");
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + InformationManager.Instance().getTotalHatcheryCount());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " );
		
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + myCombatUnitType1.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + myCombatUnitType1List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledCombatUnitCount1);
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + myCombatUnitType2.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + myCombatUnitType2List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledCombatUnitCount2);
		
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + myCombatUnitType3.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + myCombatUnitType3List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledCombatUnitCount3);
		
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + myCombatUnitType4.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + myCombatUnitType4List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledCombatUnitCount4);

		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + myCombatUnitType5.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + myCombatUnitType5List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledCombatUnitCount5);		
		
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + myCombatUnitType6.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + myCombatUnitType6List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledCombatUnitCount6);		
		
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + mySpecialUnitType1.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + mySpecialUnitType1List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledSpecialUnitCount1);
		
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + mySpecialUnitType2.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + mySpecialUnitType2List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledSpecialUnitCount2);

		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + mySpecialUnitType3.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + mySpecialUnitType3List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledSpecialUnitCount3);
		
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + mySpecialUnitType4.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + mySpecialUnitType4List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledSpecialUnitCount4);

		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My Worker");
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + WorkerManager.Instance().getWorkerData().getWorkers().size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + selfKilledWorkerUnitCount);

		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "Enemy CombatUnit");
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + numberOfCompletedEnemyCombatUnit);
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + enemyKilledCombatUnitCount);
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "Enemy WorkerUnit");
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + numberOfCompletedEnemyWorkerUnit);
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + enemyKilledWorkerUnitCount);
		y += 20;

		// setInitialBuildOrder 에서 입력한 빌드오더가 다 끝나서 빌드오더큐가 empty 되었는지 여부
		MyBotModule.Broodwar.drawTextScreen(170, y, "isInitialBuildOrderFinished " + isInitialBuildOrderFinished);
		y += 10;
		
	}
	
	private static StrategyManager instance = new StrategyManager();

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	private CommandUtil commandUtil = new CommandUtil();
		
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가를 위한 변수 및 메소드 선언

	/// 한 게임에 대한 기록을 저장하는 자료구조
	private class GameRecord {
		String mapName;
		String enemyName;
		String enemyRace;
		String enemyRealRace;
		String myName;
		String myRace;
		int gameFrameCount = 0;
		int myWinCount = 0;
		int myLoseCount = 0;
	}

	/// 과거 전체 게임들의 기록을 저장하는 자료구조
	ArrayList<GameRecord> gameRecordList = new ArrayList<GameRecord>();

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////


	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		
		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
		saveGameRecordList(isWinner);
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////		
	}

	/// 변수 값을 업데이트 합니다
	// sc76.choi 
	void updateVariables(){

		enemyRace = InformationManager.Instance().enemyRace;
		
		if (isInitialBuildOrderFinished == false &&
			  (BuildManager.Instance().buildQueue.isEmpty() 
//			  || myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
//			  || myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
              )
		) {
			BuildManager.Instance().buildQueue.clearAll();
			isInitialBuildOrderFinished = true;
		}

		// sc76.choi 나의 각종 유닛의 숫자 셋팅
		setUnitNumeres();
		
		// 적군의 공격유닛 숫자
		numberOfCompletedEnemyCombatUnit = 0;
		numberOfCompletedEnemyWorkerUnit = 0;
		for(Map.Entry<Integer,UnitInfo> unitInfoEntry : InformationManager.Instance().getUnitAndUnitInfoMap(enemyPlayer).entrySet()) {
			UnitInfo enemyUnitInfo = unitInfoEntry.getValue(); 
			if (enemyUnitInfo.getType().isWorker() == false && enemyUnitInfo.getType().canAttack() && enemyUnitInfo.getLastHealth() > 0) {
				numberOfCompletedEnemyCombatUnit ++; 
			}
			if (enemyUnitInfo.getType().isWorker() == true ) {
				numberOfCompletedEnemyWorkerUnit ++; 
			}
		}
		
		// 아군 / 적군의 본진, 첫번째 길목, 두번째 길목
		myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(myPlayer); 
		myFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(myPlayer); 
		myFirstChokePoint = InformationManager.Instance().getFirstChokePoint(myPlayer);
		mySecondChokePoint = InformationManager.Instance().getSecondChokePoint(myPlayer);
		enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(enemyPlayer);
		enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(enemyPlayer); 
		enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(enemyPlayer);
		enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(enemyPlayer);
		
		// 아군 방어 건물 목록, 공격 유닛 목록
		myDefenseBuildingType1List.clear();
		myDefenseBuildingType2List.clear();
		
		myAllCombatUnitList.clear();
		myCombatUnitType1List.clear(); // 저글링
		myCombatUnitType2List.clear(); // 히드라
		myCombatUnitType3List.clear(); // 럴커
		myCombatUnitType1ListAway.clear(); // 저글링
		myCombatUnitType2ListAway.clear(); // 히드라
		myCombatUnitType3ListAway.clear(); // 럴커
		myCombatUnitType4List.clear(); // 뮤탈
		myCombatUnitType5List.clear(); // 울트라
		myCombatUnitType6List.clear(); // 가디언
		
		mySpecialUnitType1List.clear(); // 오버로드
		mySpecialUnitType2List.clear(); // 디파일러
		mySpecialUnitType3List.clear(); // 스커지
		mySpecialUnitType4List.clear(); // 퀸
		
		myCombatUnitType2MultiDefenceList.clear(); // 히드라
		myCombatUnitType2MultiDefenceList2.clear(); // 히드라
		myCombatUnitType1ScoutList.clear(); // 저글링 정찰용
		myCombatUnitType1ScoutList2.clear(); // 저글링 정찰용
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
		// sc76.choi 모든 공격 대상 유닛을 ArrayList에 담는다. 
		// sc76.choi for(Unit unit : myPlayer.getUnits()) {
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		
		int distClosesAttackUnitFromEnemyMainBase = 10000000;
		closesAttackUnitFromEnemyMainBase = null;
		closesAttackUnitOfPositionFromEnemyMainBase = null;
		
		Iterator<Integer> it = InformationManager.Instance().getUnitData(myPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		
		while (it.hasNext()) {
			UnitInfo ui = InformationManager.Instance().getUnitData(myPlayer).getUnitAndUnitInfoMap().get(it.next());
			
			Unit unit;
			
			// sc76.choi Valid인 객체만 unit으로 선정된다.
			if(!commandUtil.IsValidUnit(ui.getUnit())){
	        	continue;
	        }else{
	        	unit = ui.getUnit();
	        }		
			
			// 저글링 정찰용
			if (unit.getType() == myCombatUnitType1) {
				
				if(enemyRace == Race.Protoss || enemyRace == Race.Terran){
					// sc76.choi 초반 빌더 진행 후, 저그 빼고 바로 실행한다.
					if(isInitialBuildOrderFinished == true){
						
						if(myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 3){
							if(myCombatUnitType1ScoutList.size() <= 0){
								myCombatUnitType1ScoutList.add(unit);
							}
						}
						
						if(myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 5){
							if(myCombatUnitType1ScoutList2.size() <= 0){
								if(myCombatUnitType1ScoutList.contains(unit)) continue;
								myCombatUnitType1ScoutList2.add(unit);
							}
						}
					}
					
					else{
						// sc76.choi 최소 히드라 4마리가 모이면.
						if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4){
							if(myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4){
								if(myCombatUnitType1ScoutList.size() <= 0){
									myCombatUnitType1ScoutList.add(unit);
								}
							}
							
							if(myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 6){
								if(myCombatUnitType1ScoutList2.size() <= 0){
									if(myCombatUnitType1ScoutList.contains(unit)) continue;
									myCombatUnitType1ScoutList2.add(unit);
								}
							}
						}
					}
				}
			}
			
			// 히드라 멀티 Defence용(defence Mode일때)
			if (unit.getType() == myCombatUnitType2 && combatState == CombatState.defenseMode) {
				// sc76.choi 초반 빌더 진행 후, 저그 빼고 바로 실행한다.
				if(isInitialBuildOrderFinished == true && (enemyRace == Race.Terran)){
					
					if(bestMultiLocation != null
						&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 3){
						if(myCombatUnitType2MultiDefenceList.size() < 2){
							
							if(myCombatUnitType2MultiDefenceList2.contains(unit)) continue;
							myCombatUnitType2MultiDefenceList.add(unit);
							
						}
					}
					
					if(bestMultiLocation2 != null
						&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 3){
						if(myCombatUnitType2MultiDefenceList2.size() < 1){
							
							if(myCombatUnitType2MultiDefenceList.contains(unit)) continue;
							myCombatUnitType2MultiDefenceList2.add(unit);
							
						}
					}
				}
			}
			
			// 저글링
			if (unit.getType() == myCombatUnitType1) {
				
				// sc76.choi 정찰용 저글링은 제외한다.
				if(isInitialBuildOrderFinished == true){
					if(myCombatUnitType1ScoutList.contains(unit)) continue;
					if(myCombatUnitType1ScoutList2.contains(unit)) continue;
				}
				
				// 멀리 떨어진 곳에서 생산된 유닛
				if(unit.getDistance(myMainBaseLocation.getPosition()) > Config.TILE_SIZE*35
						&& combatState == CombatState.defenseMode
						&& myOccupiedBaseLocations >= 3){
					
					if(myCombatUnitType1ScoutList.contains(unit)) continue;
					if(myCombatUnitType1ScoutList2.contains(unit)) continue;
					if(myCombatUnitType1List.contains(unit)) continue;
					
					myCombatUnitType1ListAway.add(unit);
				}else{
					
					if(myCombatUnitType1ListAway.contains(unit)) continue;
					myCombatUnitType1List.add(unit);
				}
				myAllCombatUnitList.add(unit);
			}
			
			// 럴커
			else if (unit.getType() == myCombatUnitType3) {
				
				// sc76.choi 적진과 가장 가까운 럴커를 찾는다.

				if(distClosesAttackUnitFromEnemyMainBase > ui.getDistanceFromEnemyMainBase()){
					closesAttackUnitFromEnemyMainBase = unit;
					closesAttackUnitOfPositionFromEnemyMainBase = ui.getUnit().getPosition();
					distClosesAttackUnitFromEnemyMainBase = ui.getDistanceFromEnemyMainBase();
					
					// getDistanceFromEnemyMainBase가 마이너스 값을 가지는 경우가 있음
					if(distClosesAttackUnitFromEnemyMainBase < 0){
						distClosesAttackUnitFromEnemyMainBase = 10000000;
					}
				}

//				if(unit.getDistance(myMainBaseLocation.getPosition()) > Config.TILE_SIZE*40
//						&& combatState == CombatState.defenseMode
//						&& myOccupiedBaseLocations >= 3){
//					
//					if(myCombatUnitType3ListAway.contains(unit)) continue;
//					myCombatUnitType3ListAway.add(unit);
//				}else{
//					if(myCombatUnitType3ListAway.contains(unit)) continue;
//				} 
				
				myCombatUnitType3List.add(unit);
				myAllCombatUnitList.add(unit);
			}

			// 히드라
			else if (unit.getType() == myCombatUnitType2) {
				
				if(commandUtil.IsValidUnit(unit) == false) continue;
				if(myCombatUnitType2MultiDefenceList.contains(unit)) continue; 
				if(myCombatUnitType2MultiDefenceList2.contains(unit)) continue;
					
				// sc76.choi 적진과 가장 가까운 히드라를 찾는다. 럴커가 없을때
				if(distClosesAttackUnitFromEnemyMainBase > ui.getDistanceFromEnemyMainBase()){
					closesAttackUnitFromEnemyMainBase = unit;
					closesAttackUnitOfPositionFromEnemyMainBase = ui.getUnit().getPosition();
					distClosesAttackUnitFromEnemyMainBase = ui.getDistanceFromEnemyMainBase();
					
					// getDistanceFromEnemyMainBase가 마이너스 값을 가지는 경우가 있음
					if(distClosesAttackUnitFromEnemyMainBase < 0){
						distClosesAttackUnitFromEnemyMainBase = 10000000;
					}
				}
					
				if(unit.getDistance(myMainBaseLocation.getPosition()) > Config.TILE_SIZE*35
					&& combatState == CombatState.defenseMode
					&& myOccupiedBaseLocations >= 3){
					
					if(myCombatUnitType2MultiDefenceList.contains(unit)) continue;
					if(myCombatUnitType2MultiDefenceList2.contains(unit)) continue;
					if(myCombatUnitType2List.contains(unit)) continue;
					
					myCombatUnitType2ListAway.add(unit);
				}else{
					
					if(myCombatUnitType2ListAway.contains(unit)) continue;
					
					// 공격할 만큼만 채운다.
					if(myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2){
						continue;
					}
					myCombatUnitType2List.add(unit);
				}	
				 
				myAllCombatUnitList.add(unit);
			}
			
			
			// 뮤탈
			else if (unit.getType() == myCombatUnitType4) {
				myCombatUnitType4List.add(unit); 
				myAllCombatUnitList.add(unit);
			}
			
			// 울트라
			else if (unit.getType() == myCombatUnitType5) { 
				myCombatUnitType5List.add(unit); 
				myAllCombatUnitList.add(unit);
			}

			// 가디언
			else if (unit.getType() == myCombatUnitType6) { 
				myCombatUnitType6List.add(unit); 
				myAllCombatUnitList.add(unit);
			}
			
			// 오버로드
			else if (unit.getType() == mySpecialUnitType1) {
				
				// maxNumberOfSpecialUnitType1 숫자까지만 특수유닛 부대에 포함시킨다 (저그 종족의 경우 오버로드가 전부 전투참여했다가 위험해질 수 있으므로)
				// sc76.choi defence 모드 시에 좀 애매 하다. 본진 으로 귀한하지 않는 유닛이 생길 수 있다.
				char jobCode = OverloadManager.Instance().getOverloadData().getJobCode(unit);

				// 오버로드 상태가 Idle인거 2기만 Drop에 사용
				if(myPlayer.getUpgradeLevel(UpgradeType.Ventral_Sacs) > 0 && OverloadManager.Instance().getDropOverloadList().size() < Config.COUNT_OVERLOAD_DROP) {
					if(jobCode == 'I'){
						OverloadManager.Instance().getOverloadData().setOverloadJob(unit, OverloadData.OverloadJob.Drop, (Unit)null);
						OverloadManager.Instance().addDropOverloadList(unit);
						//System.out.println("Drop용 Overload ===> " + unit.getID() + " : " + OverloadManager.Instance().getDropOverloadList().size());
					}
				} else if (mySpecialUnitType1List.size() < maxNumberOfSpecialUnitType1) {

					//  'A'를 채웠으나 모자라면 'I'에서 찾는다.
					if(jobCode == 'A' || jobCode == 'I'){
						OverloadManager.Instance().getOverloadData().setOverloadJob(unit, OverloadData.OverloadJob.AttackMove, (Unit)null);
						mySpecialUnitType1List.add(unit); 
						myAllCombatUnitList.add(unit);
					}
					
				}else{
					if(jobCode == 'A'){
						OverloadManager.Instance().getOverloadData().setOverloadJob(unit, OverloadData.OverloadJob.Idle, (Unit)null);
					}
				}
			}
			else if (unit.getType() == mySpecialUnitType2) { 
				// maxNumberOfSpecialUnitType2 숫자까지만 특수유닛 부대에 포함시킨다
				if (mySpecialUnitType2List.size() < maxNumberOfSpecialUnitType2) {
					mySpecialUnitType2List.add(unit); 
					myAllCombatUnitList.add(unit);
				}
			}
			// 스커지
			else if (unit.getType() == mySpecialUnitType3) { 
				// maxNumberOfSpecialUnitType2 숫자까지만 특수유닛 부대에 포함시킨다
				if (mySpecialUnitType3List.size() < maxNumberOfSpecialUnitType3) {
					mySpecialUnitType3List.add(unit); 
					myAllCombatUnitList.add(unit);
				}
			}
			// 퀸
			else if (unit.getType() == mySpecialUnitType4) { 
				// maxNumberOfSpecialUnitType2 숫자까지만 특수유닛 부대에 포함시킨다
				if (mySpecialUnitType4List.size() < maxNumberOfSpecialUnitType4) {
					mySpecialUnitType4List.add(unit); 
					myAllCombatUnitList.add(unit);
				}
			}
			else if (unit.getType() == myDefenseBuildingType1) { 
				myDefenseBuildingType1List.add(unit); 
			}
			else if (unit.getType() == myDefenseBuildingType2) { 
				myDefenseBuildingType2List.add(unit); 
			}			
		}
		
		selfMinerals = InformationManager.Instance().selfPlayer.minerals();
		selfGas = InformationManager.Instance().selfPlayer.gas();
		
		selfAvailableMinerals = BuildManager.Instance().getAvailableMinerals();
		selfAvailableGas = BuildManager.Instance().getAvailableGas();
		
		// sc76.choi 공격 포지션을 찾는다.
		getTargetPositionForAttack();
		getTargetPositionForAttack_Z();
		
		// sc76.choi 공격 포지션을 찾는다.
		getTargetPositionForDefence();		
		
		myOccupiedBaseLocations = InformationManager.Instance().getOccupiedBaseLocations(myPlayer).size();
		enemyOccupiedBaseLocations = InformationManager.Instance().getOccupiedBaseLocations(enemyPlayer).size();
		
		getLurkerDefencePosition();
		
//		System.out.println("Away Size 1 : " + myCombatUnitType1ListAway.size());
//		System.out.println("Away Size 2 : " + myCombatUnitType2ListAway.size());
//		System.out.println("Away Size 3 : " + myCombatUnitType3ListAway.size());
//		System.out.println();
	}
	
	/**
	 * 전투 상황에 맞게 뽑을 유닛을 컨트롤 한다.(CombatNeedUnitState)
	 * buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 2, 2, 3};
	 * buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 2, 2, 3, 1, 1, 2, 2, 2, 3};
	 * @ sc76.choi
	 */
	String strBuildOrderStep = new String();
	public void updatebuildOrderArray(){
		
		// sc76.choi 공격 유닛 생산 순서 설정
		// sc76.choi 단, 배열의 length는 동일하게 가야 한다. 에러 방지
		
		if (enemyRace == Race.Protoss) {
			updatebuildOrderArrayForProtoss();
		}
		else if (enemyRace == Race.Terran) {
			updatebuildOrderArrayForTerran();
		}
		else if (enemyRace == Race.Zerg) {
			updatebuildOrderArrayForZerg();
		}else{
			updatebuildOrderArrayForTerran();
		}
	}
	
	public void updatebuildOrderArrayForProtoss(){
		
		int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());
		
		// 적군 러쉬로 인한 황폐화
		if(isInitialBuildOrderFinished == true && workerCount <= 5){
			buildOrderArrayOfMyCombatUnitType = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			return;
		}
		
		// 1 : 저글링
		// 2 : 히드라
		// 3 : 러커
		// 4 : 뮤탈
		// 5 : 울트라
		// 6 : 가디언
		
		// sc76.choi 럴커 개발되고, 스파이어 없을 때,
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
			&& myPlayer.hasResearched(TechType.Lurker_Aspect) == true
			&& myCombatUnitType2List.size() >= 2
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) <= 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) <= 0			
		){
			
			if(selfGas < 200){
				strBuildOrderStep = "P 10";
				buildOrderArrayOfMyCombatUnitType = new int[]{3, 1, 2, 2, 2, 1, 1, 1, 2, 2, 1, 2}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
			}else{
				strBuildOrderStep = "P 20";
				if(buildState == BuildState.blockDefence2Dragon8_P){
					//buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 2, 1, 3, 1, 2, 2, 1, 2, 2, 3}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈					
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 1, 1, 3, 1, 2, 1, 1, 1, 2, 3}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else if(buildState == BuildState.carrier_P){
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 3}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					buildOrderArrayOfMyCombatUnitType = new int[]{3, 1, 2, 2, 3, 1, 2, 1, 3, 2, 1, 3}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}
			}
			
		}
		// sc76.choi 히드라(럴커), 스파이어만 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			  && myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0 // 스파이어 
			  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) <= 0) { // 울트라리스크 가벤
			
			// sc76.choi 가스가 없고, 히드라가 없으면 럴커를 넣으면 안된다. lock 걸림
			if(selfGas < 100){
				strBuildOrderStep = "P 30";
				buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 1, 2, 1, 3, 1, 1, 2, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
			}else{
				strBuildOrderStep = "P 60";
				buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 2, 3, 1, 2, 2, 2, 3, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
			}
			
		}
		// sc76.choi 히드라(럴커), 스파이어, 울트라 카벤 모두 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0 // 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0) { // 울트라리스크 가벤
			
				if(selfGas < 100){
					strBuildOrderStep = "P 70";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					strBuildOrderStep = "P 80";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 2, 2, 5, 1, 1, 2, 3, 5, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}
				
		}
		// sc76.choi 그레이트 스파이어, 울트라 카벤 모두 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0 // 그레이트 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0) { // 울트라리스크 가벤
			
				if(selfGas < 100){
					strBuildOrderStep = "P 90";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 1, 2, 2, 1, 3, 2, 2, 2, 1, 1}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					strBuildOrderStep = "P 100";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 4, 1, 1, 2, 2, 6, 3, 5, 6}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}
				
		}
		// sc76.choi 그레이트 스파이어만, 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0 // 그레이트 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) <= 0) { // 울트라리스크 가벤
			
				if(selfGas < 100){
					strBuildOrderStep = "P 110";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 1, 2, 1, 3, 2, 2, 2, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					strBuildOrderStep = "P 120";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 2, 4, 6, 2, 2, 4, 6, 3, 1, 2}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}
				
		}
		else{
			// 기본
			if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) <= 0){
				strBuildOrderStep = "P 1";
				buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커				
			}else{
				strBuildOrderStep = "P 2";
				if(buildState == BuildState.blockDefence2Dragon8_P){
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 1, 2, 1, 2, 2, 2, 2, 1, 2, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
				}else{
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
				}
			}
		}
	}
	
	public void updatebuildOrderArrayForTerran(){

		int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());
		
		// 적군 러쉬로 인한 황폐화
		if(isInitialBuildOrderFinished == true && workerCount <= 5){
			buildOrderArrayOfMyCombatUnitType = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			return;
		}

		// 1 : 저글링
		// 2 : 히드라
		// 3 : 러커
		// 4 : 뮤탈
		// 5 : 울트라
		// 6 : 가디언
		
		boolean existHydralisk = (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) > 0);
		
		// sc76.choi 럴커 개발되고, 스파이어 없을 때,
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
			&& myPlayer.hasResearched(TechType.Lurker_Aspect) == true
			&& myCombatUnitType2List.size() >= 1
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) <= 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) <= 0			
		){
			
			if(selfGas < 100){
				
				if(existHydralisk) {
					strBuildOrderStep = "T 10";
					buildOrderArrayOfMyCombatUnitType = new int[]{3, 1, 2, 3, 2, 0, 1, 0, 2, 1, 2, 0};						
				}
				else {
					strBuildOrderStep = "T 20";					
					buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2};
				}
				
			}
			// 가스가 있으면
			else{
				if(existHydralisk) {
					// 빠른 럴커 변태를 위해
					if((myPlayer.allUnitCount(UnitType.Zerg_Lurker_Egg) <= 0
						|| BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lurker) == 0)
							&& myPlayer.allUnitCount(UnitType.Zerg_Lurker) <= 0){
						
						strBuildOrderStep = "T 30-1";
						if(selfGas > 200 && selfMinerals > 200){
							excuteUrgenturgent_Add_Lurker1();
						}
						buildOrderArrayOfMyCombatUnitType = new int[]{3, 3, 1, 3, 3, 3, 3, 3, 3, 1, 3, 1};
					}
					else{
						strBuildOrderStep = "T 30";
						buildOrderArrayOfMyCombatUnitType = new int[]{3, 1, 2, 1, 3, 2, 1, 1, 3, 2, 1, 3};
					}
				}
				else {
					strBuildOrderStep = "T 40";
					buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 2};
				}
			}
			
		}
		// sc76.choi 스파이어만 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0 // 스파이어 
			  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) <= 0) { // 울트라리스크 가벤
			
			// sc76.choi 가스가 없고, 히드라가 없으면 럴커를 넣으면 안된다. lock 걸림
			if(selfGas < 200){
				strBuildOrderStep = "T 50";
				buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 1, 2, 1, 2, 1, 3, 2, 2, 1, 4};
			}else if(selfGas < 200){
				if(existHydralisk){
					strBuildOrderStep = "T 60";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 3, 1, 3, 1, 1, 2, 3, 1, 1};
				}
				else{
					strBuildOrderStep = "T 70";
					buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 1};
				}
					
			}else{
				if(existHydralisk) {
					strBuildOrderStep = "T 80";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 3, 1, 2, 3, 2, 2, 3, 1, 2, 1, 4};
				}
				else{
					strBuildOrderStep = "T 90";
					buildOrderArrayOfMyCombatUnitType = new int[]{2, 2, 1, 2, 2, 4, 1, 2, 1, 2, 1, 4};
				}
			}
		}
		// sc76.choi 스파이어, 울트라 카벤 모두 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0 // 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0) { // 울트라리스크 가벤
			
				if(selfGas < 200){
					strBuildOrderStep = "T 100";
					buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 1, 3, 2, 2, 1, 2, 2, 3, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
					
				}else{
					if(existHydralisk){
						strBuildOrderStep = "T 110";
						buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 2, 3, 2, 5, 1, 3, 2, 3, 5, 4};
					}
					else{
						strBuildOrderStep = "T 120";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 4, 2, 2, 5, 1, 2, 2, 2, 5, 4};
					}
				}
				
		}
		// sc76.choi 그레이트 스파이어, 울트라 카벤 모두 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0 // 그레이트 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0) { // 울트라리스크 가벤
			
				if(selfGas < 200){
					if(existHydralisk) {
						strBuildOrderStep = "T 130";
						buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 3, 2, 2, 3, 1, 2, 3, 2, 1, 4};
					}
					else {
						strBuildOrderStep = "T 140";
						buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 4};
					}
				}else{
					
					if(existHydralisk) {
						strBuildOrderStep = "T 150";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 4, 4, 1, 1, 2, 6, 3, 5, 6};
					}
					else {
						strBuildOrderStep = "T 160";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 4, 4, 3, 1, 2, 6, 2, 5, 6};
					}
				}
				
		}
		// sc76.choi 그레이트 스파이어만, 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0 // 그레이트 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) <= 0) { // 울트라리스크 가벤
			
				if(selfGas < 200){
					strBuildOrderStep = "T 170";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					
					if(existHydralisk){
						strBuildOrderStep = "T 180";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 4, 6, 2, 1, 4, 6, 3, 1, 2}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
					}
					else {
						strBuildOrderStep = "T 190";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 2, 2, 4, 6, 3, 1, 4, 6, 2, 1, 2}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
					}
				}
				
		}
		else{
			// 기본
			if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) <= 0){
				strBuildOrderStep = "T 1";
				buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커				
			}else{
				strBuildOrderStep = "T 2";
				buildOrderArrayOfMyCombatUnitType = new int[]{2, 2, 1, 2, 2, 1, 2, 2, 1, 2, 2, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
			}
		}
	}

	public void updatebuildOrderArrayForZerg(){
		
		int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());
		
		// 적군 러쉬로 인한 황폐화
		if(isInitialBuildOrderFinished == true && workerCount <= 5){
			buildOrderArrayOfMyCombatUnitType = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			return;
		}
		
		// 1 : 저글링
		// 2 : 히드라
		// 3 : 러커
		// 4 : 뮤탈
		// 5 : 울트라
		// 6 : 가디언
		
		// sc76.choi 럴커 개발되고, 스파이어 없을 때,
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
			&& myPlayer.hasResearched(TechType.Lurker_Aspect) == true
			&& myCombatUnitType2List.size() >= 2
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) <= 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) <= 0			
		){
			
			if(selfGas < 200){
				strBuildOrderStep = "Z 10";
				buildOrderArrayOfMyCombatUnitType = new int[]{3, 1, 2, 2, 2, 1, 1, 1, 2, 2, 1, 2}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
			}else{
				strBuildOrderStep = "Z 20";
				buildOrderArrayOfMyCombatUnitType = new int[]{3, 1, 2, 2, 3, 1, 1, 1, 3, 2, 1, 3}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
			}
			
		}
		// sc76.choi 스파이어만 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0 // 스파이어 
			  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) <= 0) { // 울트라리스크 가벤
			
			// sc76.choi 가스가 없고, 히드라가 없으면 럴커를 넣으면 안된다. lock 걸림
			if(selfGas < 200){
				strBuildOrderStep = "Z 30";
				buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
			}else{
				// 테란일때, 럴커를 위주로
				if(enemyRace == Race.Terran){
					if(selfGas < 200){
						strBuildOrderStep = "Z 40";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 2, 3, 1, 1, 1, 2, 3, 1, 1}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
					}else{
						strBuildOrderStep = "Z 50";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 2, 3, 4, 1, 1, 2, 3, 4, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
					}
				}else{
					if(selfGas < 200){
						strBuildOrderStep = "Z 61";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 1, 3, 1, 1, 2, 1, 1, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
					}else{
						strBuildOrderStep = "Z 60";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 2, 3, 4, 2, 2, 2, 3, 2, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
					}
				}
			}
			
		}
		// sc76.choi 스파이어, 울트라 카벤 모두 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0 // 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0) { // 울트라리스크 가벤
			
				if(selfGas < 200){
					strBuildOrderStep = "Z 70";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					strBuildOrderStep = "Z 80";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 4, 2, 2, 5, 1, 1, 2, 3, 5, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}
				
		}
		// sc76.choi 그레이트 스파이어, 울트라 카벤 모두 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0 // 그레이트 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0) { // 울트라리스크 가벤
			
				if(selfGas < 200){
					strBuildOrderStep = "Z 90";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 2, 2, 1, 1, 2, 2, 2, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					strBuildOrderStep = "Z 100";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 4, 4, 1, 1, 2, 6, 3, 5, 6}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}
				
		}
		// sc76.choi 그레이트 스파이어만, 올라가 있을 때,
		else if (myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0 // 그레이트 스파이어 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) <= 0) { // 울트라리스크 가벤
				if(selfGas < 200){
					strBuildOrderStep = "Z 110";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 4}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}else{
					strBuildOrderStep = "Z 120";
					buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 4, 6, 1, 1, 4, 6, 3, 1, 2}; 	// 저글링 히드라 히드라 럴커 뮤탈 뮤탈
				}
		}
		else{
			// 기본
			if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) <= 0){
				strBuildOrderStep = "Z 1";
				buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커				
			}else{
				if(buildState == BuildState.fastMutalisk_Z){
					strBuildOrderStep = "Z 2";
					buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
				}else{
					if(selfGas < 100){
						strBuildOrderStep = "Z 3";
						buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
					}else{
						if(buildState == BuildState.fastZergling_Z){
							strBuildOrderStep = "Z 4";
							buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
						}else if(buildState == BuildState.superZergling_Z){
							strBuildOrderStep = "Z 5";
							buildOrderArrayOfMyCombatUnitType = new int[]{1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
						}else{
							strBuildOrderStep = "Z 6";
							buildOrderArrayOfMyCombatUnitType = new int[]{2, 1, 2, 1, 1, 1, 2, 1, 1, 2, 2, 1}; 	// 저글링 저글링 히드라 히드라 히드라 러커
						}
					}
				}
			}
			
		}
	}

//	void updateVariablesForAttackUnit(){
//		// 2초에 한번만 실행
//		if (MyBotModule.Broodwar.getFrameCount() % (24 * 2) != 0) {
//			return;
//		}
//
//		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		// sc76.choi 공격을 위한 아군 타겟 변수 할당
//		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		if(enemyMainBaseLocation != null){
//			//closesAttackUnitFromEnemyMainBase = getClosestCanAttackUnitTypeToTarget(UnitType.Zerg_Hydralisk, enemyMainBaseLocation.getPosition());
//			closesAttackUnitFromEnemyMainBase = null;
//		}
//	}

	// sc76.choi 초기에 태어났을 때, DEFENCE_POSITION으로 이동시킨다.
	// sc76.choi APM을 줄이는데, 큰 기여를 했음
	public void moveUnitToDefencePosition(Unit unit){
		
		if(unit.getType() == UnitType.Zerg_Zergling
			|| unit.getType() == UnitType.Zerg_Hydralisk
			|| unit.getType() == UnitType.Zerg_Lurker){
			
			if(unit.isIdle() == true || unit.isMoving() == false){
					
				// sc76.choi 본진이나, 앞마당에 태어난 유닛은 최초 설정한  DEFENCE_POSITION에 모인다.
				if( BWTA.getRegion(unit.getPosition()) == BWTA.getRegion(myMainBaseLocation.getPosition())
					|| BWTA.getRegion(unit.getPosition()) == BWTA.getRegion(myFirstExpansionLocation.getPosition())){
					
					if(unit.getDistance(DEFENCE_POSITION) > Config.TILE_SIZE*5){
						commandUtil.move(unit, DEFENCE_POSITION);
					}
					
				}
			}
		}
	}

	public void onUnitComplete(Unit unit) {
		moveUnitToDefencePosition(unit);
	}
	
	public void onUnitCreate(Unit unit) {
		moveUnitToDefencePosition(unit);
	}
	
	public void onUnitShow(Unit unit) {
		moveUnitToDefencePosition(unit);
	}	
	
	
	/// 아군 / 적군 공격 유닛 사망 유닛 숫자 누적값을 업데이트 합니다
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isNeutral()) {
			return;
		}
		
		if (unit.getPlayer() == myPlayer) {
			// 저글링
			if (unit.getType() == myCombatUnitType1) {
				myKilledCombatUnitCount1 ++;				
			}
			// 히드라
			else if (unit.getType() == myCombatUnitType2) {
				myKilledCombatUnitCount2 ++;		
			} 
			// 럴커
			else if (unit.getType() == myCombatUnitType3 ) {
				myKilledCombatUnitCount3 ++;		
			} 
			// 뮤탈
			else if (unit.getType() == myCombatUnitType4 ) {
				myKilledCombatUnitCount4 ++;		
			} 
			// 울트라
			else if (unit.getType() == myCombatUnitType5 ) {
				myKilledCombatUnitCount5 ++;		
			}
			// 가디언
			else if (unit.getType() == myCombatUnitType6 ) {
				myKilledCombatUnitCount6 ++;		
			} 			
			else if (myCombatUnitType3 == UnitType.Terran_Siege_Tank_Tank_Mode && unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				myKilledCombatUnitCount3 ++;		
			}
			else if (unit.getType() == mySpecialUnitType1 ) {
				myKilledSpecialUnitCount1 ++;		
			} 
			else if (unit.getType() == mySpecialUnitType2 ) {
				myKilledSpecialUnitCount2 ++;		
			} 
			else if (unit.getType() == mySpecialUnitType3 ) {
				myKilledSpecialUnitCount3 ++;		
			}
			else if (unit.getType() == mySpecialUnitType4 ) {
				myKilledSpecialUnitCount4 ++;		
			}
			
			/// 적군 일꾼 유닛타입의 사망 유닛 숫자 누적값
			if (unit.getType().isWorker() == true) {
				selfKilledWorkerUnitCount ++;
			}
		}
		else if (unit.getPlayer() == enemyPlayer) {
			/// 적군 공격 유닛타입의 사망 유닛 숫자 누적값
			if (unit.getType().isWorker() == false && unit.getType().isBuilding() == false) {
				enemyKilledCombatUnitCount ++;
			}
			/// 적군 일꾼 유닛타입의 사망 유닛 숫자 누적값
			if (unit.getType().isWorker() == true) {
				enemyKilledWorkerUnitCount ++;
			}
		} 
	}
	
	// sc76.choi 최소 방어 수, 공격 가능 수를 조절한다.
	boolean isIncreasingUnitType1Number4 = false;
	boolean isIncreasingUnitType1Number3 = false;
	boolean isIncreasingUnitType1Number2 = false;
	boolean isIncreasingUnitType1Number1 = false;
	boolean isIncreasingUnitType2Number4 = false;
	boolean isIncreasingUnitType2Number3 = false;
	boolean isIncreasingUnitType2Number2 = false;
	boolean isIncreasingUnitType2Number1 = false;
	boolean isIncreasingUnitType3Number4 = false;
	boolean isIncreasingUnitType3Number3 = false;
	boolean isIncreasingUnitType3Number2 = false;
	boolean isIncreasingUnitType3Number1 = false;
	
	/**
	 * 각종 config를 조정한다. 
	 */
	public void excuteConfigration(){
		
		if (MyBotModule.Broodwar.getFrameCount() % 24*2 != 0) return;
		
		/////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi 해처리가 많아지면 공간을 넓게 해서 짓는다.
		int myHatcheryCount = InformationManager.Instance().getTotalHatcheryCount();
		if(myHatcheryCount >= 2){
			Config.BuildingSpacing = 2;
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi 해처리가 많고 확장이 되면, 일꾼을 계속 뽑기 위해 미네럴당 일꾼 수 조절을 한다.
		if(InformationManager.Instance().getTotalHatcheryCount() >= 3){
			Config.optimalWorkerCount = 2.0;
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////
		// mineralWorker Rebalancing
		if(myOccupiedBaseLocations >= 3){
			// 본진에 갇혀 있을때
			if(buildState == BuildState.fastVulture_T || buildState == BuildState.Tank_T || buildState == BuildState.blockDefence2Dragon8_P){
				if(combatState == CombatState.attackStarted || findAttackTargetForDefenceWorkerRatio() == null){
					WorkerManager.Instance().getWorkerData().mineralAndMineralWorkerRatio = 1.1;	
				}else{
					WorkerManager.Instance().getWorkerData().mineralAndMineralWorkerRatio = 3;
				}
			}else{
				WorkerManager.Instance().getWorkerData().mineralAndMineralWorkerRatio = 1.2;
			}
		}else{
			if(WorkerManager.Instance().getNumWorkers() >= 20){
				WorkerManager.Instance().getWorkerData().mineralAndMineralWorkerRatio = 1.4;
			}else{
				WorkerManager.Instance().getWorkerData().mineralAndMineralWorkerRatio = 1.8;
			}
		}
		

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi 가스 일꾼을 조절 한다.
		
		int allWorkers = WorkerManager.Instance().getNumWorkers();
			
		if(allWorkers <= 5){
			Config.WorkersPerRefinery = 0;
		}else{

			if(enemyRace == Race.Terran){
				
				Config.WorkersPerRefinery = 3;
				
				if(myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
					|| myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0){
					if(selfMinerals*3 <= selfGas){
						Config.WorkersPerRefinery = 1;
					}else{
						Config.WorkersPerRefinery = 3;
					}				
				}else{
					if(myOccupiedBaseLocations >= 3){
						if(selfMinerals*3 <= selfGas){
							Config.WorkersPerRefinery = 1;
						}else{
							Config.WorkersPerRefinery = 3;
						}
					}
				}
			}else if(enemyRace == Race.Zerg){
				if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0){
					if(selfMinerals*3 <= selfGas){
						Config.WorkersPerRefinery = 1;
					}else{
						Config.WorkersPerRefinery = 3;
					}
				}else{
					if(myOccupiedBaseLocations >= 3){
						Config.WorkersPerRefinery = 3;
					}else{
						if(myPlayer.incompleteUnitCount(UnitType.Zerg_Spire) > 0 
								|| myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
								|| buildState == BuildState.lurker_Z
								|| buildState == BuildState.darkTemplar_P){
							if(selfMinerals*3 <= selfGas){
								Config.WorkersPerRefinery = 1;
							}else{
								Config.WorkersPerRefinery = 3;
							}
						}else{
							if(selfMinerals*3 <= selfGas){
								Config.WorkersPerRefinery = 1;
							}else{
								Config.WorkersPerRefinery = 2;
							}	
						}
					}
				}
			}else if(enemyRace == Race.Protoss){
				if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0){
					if(selfMinerals*3 <= selfGas){
						Config.WorkersPerRefinery = 1;
					}else{
						Config.WorkersPerRefinery = 3;
					}				
				}else{
					if(myOccupiedBaseLocations >= 3){
						if(selfMinerals*3 <= selfGas){
							Config.WorkersPerRefinery = 1;
						}else{
							Config.WorkersPerRefinery = 3;
						}	
					}else{
	//					if(myPlayer.hasResearched(TechType.Lurker_Aspect)){
						if(myPlayer.incompleteUnitCount(UnitType.Zerg_Spire) > 0 || myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0){
							Config.WorkersPerRefinery = 3;
						}else{
							if(selfMinerals*3 <= selfGas){
								Config.WorkersPerRefinery = 1;
							}else{
								Config.WorkersPerRefinery = 3;
							}	
						}
					}
				}
			}
			// 랜덤
			else{
				if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0){
					if(selfMinerals*3 <= selfGas){
						Config.WorkersPerRefinery = 1;
					}else{
						Config.WorkersPerRefinery = 3;
					}				
				}else{
					if(myOccupiedBaseLocations >= 3){
						Config.WorkersPerRefinery = 3;
					}else{
	//					if(myPlayer.hasResearched(TechType.Lurker_Aspect)){
						if(myPlayer.incompleteUnitCount(UnitType.Zerg_Spire) > 0 || myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0){
							Config.WorkersPerRefinery = 3;
						}else{
							Config.WorkersPerRefinery = 2;
						}
					}
				}
			}
		}
		
	
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi 최소 방어 수, 공격 가능 수를 조절한다.
		if(enemyRace == Race.Protoss){
			
			// 저글링
			if(myKilledCombatUnitCount1 >= 20 && isIncreasingUnitType1Number3 == false){
				Config.necessaryNumberOfCombatUnitType1AgainstZerg = Config.necessaryNumberOfCombatUnitType1AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = Config.necessaryNumberOfDefenceUnitType1AgainstZerg + 1;
				isIncreasingUnitType1Number3 = true;
			}else if(myKilledCombatUnitCount1 >= 10 && isIncreasingUnitType1Number2 == false){
				Config.necessaryNumberOfCombatUnitType1AgainstZerg = Config.necessaryNumberOfCombatUnitType1AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = Config.necessaryNumberOfDefenceUnitType1AgainstZerg + 1;
				isIncreasingUnitType1Number2 = true;
			}else if(myKilledCombatUnitCount1 >= 5 && isIncreasingUnitType1Number1 == false){
				Config.necessaryNumberOfCombatUnitType1AgainstZerg = Config.necessaryNumberOfCombatUnitType1AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = Config.necessaryNumberOfDefenceUnitType1AgainstZerg + 1;
				isIncreasingUnitType1Number1 = true;
			}
			
			if(myKilledCombatUnitCount2 >= 30 && isIncreasingUnitType2Number4 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstProtoss = Config.necessaryNumberOfCombatUnitType2AgainstProtoss + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss + 1;
				isIncreasingUnitType2Number4 = true;
			}else if(myKilledCombatUnitCount2 >= 20 && isIncreasingUnitType2Number3 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstProtoss = Config.necessaryNumberOfCombatUnitType2AgainstProtoss + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss + 1;
				isIncreasingUnitType2Number3 = true;
			}else if(myKilledCombatUnitCount2 >= 10 && isIncreasingUnitType2Number2 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstProtoss = Config.necessaryNumberOfCombatUnitType2AgainstProtoss + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss + 1;
				isIncreasingUnitType2Number2 = true;
			}else if(myKilledCombatUnitCount2 >= 5 && isIncreasingUnitType2Number1 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstProtoss = Config.necessaryNumberOfCombatUnitType2AgainstProtoss + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstProtoss = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss + 1;
				isIncreasingUnitType2Number1 = true;
			}
		}else if(enemyRace == Race.Zerg){
			
			// 저글링
			if(myKilledCombatUnitCount1 >= 20 && isIncreasingUnitType1Number3 == false){
				Config.necessaryNumberOfCombatUnitType1AgainstZerg = Config.necessaryNumberOfCombatUnitType1AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = Config.necessaryNumberOfDefenceUnitType1AgainstZerg + 1;
				isIncreasingUnitType1Number3 = true;
			}else if(myKilledCombatUnitCount1 >= 10 && isIncreasingUnitType1Number2 == false){
				Config.necessaryNumberOfCombatUnitType1AgainstZerg = Config.necessaryNumberOfCombatUnitType1AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = Config.necessaryNumberOfDefenceUnitType1AgainstZerg + 1;
				isIncreasingUnitType1Number2 = true;
			}else if(myKilledCombatUnitCount1 >= 5 && isIncreasingUnitType1Number1 == false){
				Config.necessaryNumberOfCombatUnitType1AgainstZerg = Config.necessaryNumberOfCombatUnitType1AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstZerg = Config.necessaryNumberOfDefenceUnitType1AgainstZerg + 1;
				isIncreasingUnitType1Number1 = true;
			}
			
			if(myKilledCombatUnitCount2 >= 20 && isIncreasingUnitType2Number3 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstZerg = Config.necessaryNumberOfCombatUnitType2AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstZerg = Config.necessaryNumberOfDefenceUnitType2AgainstZerg + 1;
				isIncreasingUnitType2Number3 = true;
			}else if(myKilledCombatUnitCount2 >= 10 && isIncreasingUnitType2Number2 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstZerg = Config.necessaryNumberOfCombatUnitType2AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstZerg = Config.necessaryNumberOfDefenceUnitType2AgainstZerg + 1;
				isIncreasingUnitType2Number2 = true;
			}else if(myKilledCombatUnitCount2 >= 5 && isIncreasingUnitType2Number1 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstZerg = Config.necessaryNumberOfCombatUnitType2AgainstZerg + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstZerg = Config.necessaryNumberOfDefenceUnitType2AgainstZerg + 1;
				isIncreasingUnitType2Number1 = true;
			}
		}else if(enemyRace == Race.Terran){
			if(myKilledCombatUnitCount2 >= 30 && isIncreasingUnitType2Number4 == false){
				
				Config.necessaryNumberOfCombatUnitType2AgainstTerran = Config.necessaryNumberOfCombatUnitType2AgainstTerran + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstTerran = Config.necessaryNumberOfDefenceUnitType2AgainstTerran + 1;
				Config.necessaryNumberOfCombatUnitType1AgainstTerran = Config.necessaryNumberOfCombatUnitType1AgainstTerran + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstTerran = Config.necessaryNumberOfDefenceUnitType1AgainstTerran + 1;
				isIncreasingUnitType2Number4 = true;
				
			}else if(myKilledCombatUnitCount2 >= 20 && isIncreasingUnitType2Number3 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstTerran = Config.necessaryNumberOfCombatUnitType2AgainstTerran + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstTerran = Config.necessaryNumberOfDefenceUnitType2AgainstTerran + 1;
				Config.necessaryNumberOfCombatUnitType1AgainstTerran = Config.necessaryNumberOfCombatUnitType1AgainstTerran + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstTerran = Config.necessaryNumberOfDefenceUnitType1AgainstTerran + 1;
				isIncreasingUnitType2Number3 = true;
				
			}else if(myKilledCombatUnitCount2 >= 10 && isIncreasingUnitType2Number2 == false){
				Config.necessaryNumberOfCombatUnitType2AgainstTerran = Config.necessaryNumberOfCombatUnitType2AgainstTerran + 1;
				Config.necessaryNumberOfDefenceUnitType2AgainstTerran = Config.necessaryNumberOfDefenceUnitType2AgainstTerran + 1;
				Config.necessaryNumberOfCombatUnitType1AgainstTerran = Config.necessaryNumberOfCombatUnitType1AgainstTerran + 1;
				Config.necessaryNumberOfDefenceUnitType1AgainstTerran = Config.necessaryNumberOfDefenceUnitType1AgainstTerran + 1;
				
				isIncreasingUnitType2Number2 = true;
			}
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi 방어 건물 spacing(BuildingDefenseTowerSpacing 기본 1)을 조절한다.
		// sc76.choi Finder에 의해 성큰의 위치를 조정 할 수 없기때문에, spacing 값으로 조절 한다.
		if(enemyRace == Race.Protoss){
			if(myPlayer.completedUnitCount(UnitType.Zerg_Creep_Colony) <= 2
					&& myPlayer.completedUnitCount(UnitType.Zerg_Sunken_Colony) <= 2){
				Config.BuildingDefenseTowerSpacing = 2;
			}else{
				Config.BuildingDefenseTowerSpacing = 1;
			}
		}else if(enemyRace == Race.Terran){
			if(myPlayer.completedUnitCount(UnitType.Zerg_Creep_Colony) <= 0
					&& myPlayer.completedUnitCount(UnitType.Zerg_Sunken_Colony) <= 0){
					Config.BuildingDefenseTowerSpacing = 4;
			}else{
				Config.BuildingDefenseTowerSpacing = 3;
			}
		}
	}
	
	/// 일꾼을 계속 추가 생산합니다
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 최대 45마리까지만 생산
		if(WorkerManager.Instance().getWorkerData().getNumWorkers() >= Config.numberOfMyWorkerUnitTrainingBuilding){
			return;
		}
			
		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());
			
			int eggWorkerCount = 0;

			if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Zerg_Egg) {
						// Zerg_Egg 에게 morph 명령을 내리면 isMorphing = true,
						// isBeingConstructed = true, isConstructing = true 가 된다
						// Zerg_Egg 가 다른 유닛으로 바뀌면서 새로 만들어진 유닛은 잠시
						// isBeingConstructed = true, isConstructing = true 가
						// 되었다가,
						if (unit.isMorphing() && unit.getBuildType() == UnitType.Zerg_Drone) {
							workerCount++;
							eggWorkerCount++;
						}
					}
				}
			} else {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining()) {
							workerCount += unit.getTrainingQueue().size();
						}
					}
				}
			}

			// 최적의 일꾼 수 = 미네랄 * (1~1.5) + 가스 * 3
			int optimalWorkerCount = 0;
			for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(myPlayer)) {
				optimalWorkerCount += baseLocation.getMinerals().size() * Config.optimalWorkerCount; // 1.8;
				optimalWorkerCount += baseLocation.getGeysers().size() * 3;
			}
						
			if (workerCount < optimalWorkerCount) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Protoss_Nexus || unit.getType() == UnitType.Terran_Command_Center || unit.getType() == UnitType.Zerg_Larva) {
						if (unit.isTraining() == false && unit.isMorphing() == false) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0 && eggWorkerCount == 0) {
								// std.cout + "worker enqueue" + std.endl;
								
								// sc76.choi 30마리 이상이면, 확장에서 일꾼을 생산한다.
								if(WorkerManager.Instance().getWorkerData().getNumWorkers() < 22){
									if(enemyRace == Race.Terran){
										// sc76.choi fastVulture_T면 드론을 많이 뽑는다.
										if((buildState == BuildState.fastVulture_T
											|| buildState == BuildState.blockTheFirstChokePoint_T
											|| buildState == BuildState.blockTheSecondChokePoint_T)
												&& workerCount <= 30){
											BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);										
											BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);										
											BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);										

										}else{
											if(myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
													|| myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0){
												BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);										
												BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);										
											}else{
												BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
											}
										}
									}else{
										if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
												&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 6){
												BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);										
												BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);										
											}else{
												BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
											}
									}
								}else if(bestMultiLocation != null 
										 &&WorkerManager.Instance().getWorkerData().getNumWorkers() < 40
										 && InformationManager.Instance().getTotalHatcheryCount() >= 4){
									if(enemyRace == Race.Terran){
										if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0){
											//BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), bestMultiLocation.getTilePosition(), false);										
											BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), bestMultiLocation.getTilePosition(), false);										
										}else{
											BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), bestMultiLocation.getTilePosition(), false);
										}
									}else{
										if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
												&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 6){
												//BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getWorkerType()), bestMultiLocation.getTilePosition(), false);										
												BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), bestMultiLocation.getTilePosition(), false);										
											}else{
												BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), bestMultiLocation.getTilePosition(), false);
											}
									}
								}else if(InformationManager.Instance().getTotalHatcheryCount() >= 3){
									// 태란이 아닐때
									BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), myFirstExpansionLocation.getTilePosition(), false);
									
								}else if(InformationManager.Instance().getTotalHatcheryCount() >= 2){
									// 태란 일때,
									BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), myFirstExpansionLocation.getTilePosition(), false);
									
								}else{
									BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified);
								}
							}
						}
					}
				}
			}
		}
	}

	/// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서<br>
	/// SupplyProvider를 추가 건설/생산합니다
	public void executeSupplyManagement() {

//		System.out.println("MyBotModule.Broodwar.self().supplyUsed()  : " +  MyBotModule.Broodwar.self().supplyUsed());
//		System.out.println("MyBotModule.Broodwar.self().supplyTotal() : " +  MyBotModule.Broodwar.self().supplyTotal());
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 가이드 추가 및 콘솔 출력 명령 주석 처리

		// InitialBuildOrder 진행중 혹은 그후라도 서플라이 건물이 파괴되어 데드락이 발생할 수 있는데, 이 상황에 대한 해결은 참가자께서 해주셔야 합니다.
		// 오버로드가 학살당하거나, 서플라이 건물이 집중 파괴되는 상황에 대해  무조건적으로 서플라이 빌드 추가를 실행하기 보다 먼저 전략적 대책 판단이 필요할 것입니다

		// BWAPI::Broodwar->self()->supplyUsed() > BWAPI::Broodwar->self()->supplyTotal()  인 상황이거나
		// BWAPI::Broodwar->self()->supplyUsed() + 빌드매니저 최상단 훈련 대상 유닛의 unit->getType().supplyRequired() > BWAPI::Broodwar->self()->supplyTotal() 인 경우
		// 서플라이 추가를 하지 않으면 더이상 유닛 훈련이 안되기 때문에 deadlock 상황이라고 볼 수도 있습니다.
		// 저그 종족의 경우 일꾼을 건물로 Morph 시킬 수 있기 때문에 고의적으로 이런 상황을 만들기도 하고, 
		// 전투에 의해 유닛이 많이 죽을 것으로 예상되는 상황에서는 고의적으로 서플라이 추가를 하지 않을수도 있기 때문에
		// 참가자께서 잘 판단하셔서 개발하시기 바랍니다.
		
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		// InitialBuildOrder 진행중이라도 supplyUsed 가 supplyTotal 보다 커져버리면 실행하도록 합니다

		// 초반에 오버로드 잡힐 경우를 대비
//		if(isInitialBuildOrderFinished == false){
//			if(myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
//				 && (MyBotModule.Broodwar.self().supplyTotal() - MyBotModule.Broodwar.self().supplyUsed()) <= 0){
//				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Overlord, true);
//			}
//		}
		
		if (isInitialBuildOrderFinished == false 
				&& MyBotModule.Broodwar.self().supplyUsed() < MyBotModule.Broodwar.self().supplyTotal()  ) {
			return;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 6
				!= 0) {
			return;
		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() < 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			
			int supplyMargin = 12;
			if(InformationManager.Instance().getTotalHatcheryCount() >= 6) {
				supplyMargin = 24;
			}else if(InformationManager.Instance().getTotalHatcheryCount() >= 4) {
				supplyMargin = 18;
			}

			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {
				
				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;

				// 저그 종족인 경우, 생산중인 Zerg_Overlord (Zerg_Egg) 를 센다. Hatchery 등 건물은 세지 않는다
				if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == UnitType.Zerg_Overlord) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
						// 갓태어난 Overlord 는 아직 SupplyTotal 에 반영안되어서, 추가 카운트를 해줘야함
						if (unit.getType() == UnitType.Zerg_Overlord && unit.isConstructing()) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
					}
				}
				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
				else {
					onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
							InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
							* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();
				}

				// 주석처리
				//System.out.println("currentSupplyShortage : " + currentSupplyShortage + " onBuildingSupplyCount : " + onBuildingSupplyCount);

				if (currentSupplyShortage > onBuildingSupplyCount) {
					
					// BuildQueue 최상단에 SupplyProvider 가 있지 않으면 enqueue 한다
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() 
							&& currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) 
						{
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						// 주석처리
						//System.out.println("enqueue supply provider "
						//		+ InformationManager.Instance().getBasicSupplyProviderUnitType());
						BuildManager.Instance().buildQueue.queueAsHighestPriority(
								new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}

		// BasicBot 1.1 Patch End ////////////////////////////////////////////////		
	}
	
	// sc76.choi initialBuildOrder 때문에 별도의 boolean을 두어 컨트롤 한다.
	boolean fastZergling_Z_DefenceBuilding1 = false;
	boolean fastZergling_Z_DefenceBuilding2 = false;
	void excuteUrgentDefenceConstructionInBaseLocation(BaseLocation base){
		
		if(fastZergling_Z_DefenceBuilding1 == false){
			
			BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
					base.getTilePosition(), true);
			
			fastZergling_Z_DefenceBuilding1 = true;
		}
	
		if(fastZergling_Z_DefenceBuilding2 == false
			&& existUnitTypeInRegion(myPlayer, UnitType.Zerg_Creep_Colony, base.getRegion(), false, false) == true
		){
			
			BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType2, 
					base.getTilePosition(), true);

			// sc76.choi 클로니 대신 Drone 추가를 해준다.
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//5
			
			fastZergling_Z_DefenceBuilding2 = true;
			
		}
	}
	
	// sc76.choi initialBuildOrder 때문에 별도의 boolean을 두어 컨트롤 한다.
	boolean blockDefence2Dragon8_P_CombatBuilding = false;
	void excuteUrgentCombatConstructionInBaseLocation(TilePosition tp){
		
		if(tp == null) return;
			
		if(blockDefence2Dragon8_P_CombatBuilding == false){
			
			if(selfAvailableMinerals > 200
					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hatchery) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0){
				
				if(DEBUG) System.out.println("excuteUrgentCombatConstructionInBaseLocation : " + tp);
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hatchery, 
						tp, true);
				
				blockDefence2Dragon8_P_CombatBuilding = true;
			}
			
		}
	}
	
	boolean blockDefence2Dragon8_P_CombatBuilding2 = false;
	void excuteUrgentCombatConstructionInBaseLocation2(){
		
		if(blockDefence2Dragon8_P_CombatBuilding2 == false){
			
			if( selfAvailableMinerals > 200
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hatchery) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0){
					
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hatchery, 
						myMainBaseLocation.getTilePosition(), true);
				
				if(DEBUG) System.out.println("excuteUrgentCombatConstructionInBaseLocation2 !!!!" );
				
				blockDefence2Dragon8_P_CombatBuilding2 = true;
			}
			
		}
	}
	
	// sc76.choi 확장이 안되고 미네럴 덩이가 얼마 없을때, 강제 확장한다.
	boolean urgentCombatConstructionInMultiBase = false;
	void excuteUrgentCombatConstructionInMultiBase(){
		
		if(urgentCombatConstructionInMultiBase == false){
			
			//if(bestMultiLocation != null && selfAvailableMinerals > 300){
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hatchery, 
						bestMultiLocation.getTilePosition(), true);
				
				urgentCombatConstructionInMultiBase = true;
				
				if(DEBUG) System.out.println("ConstructionInMultiBase !!!! ");
			//}
			
		}
	}
	
	// sc76.choi initialBuildOrder 때문에 별도의 boolean을 두어 컨트롤 한다.
	boolean urgent_AirDefenceBuilding1 = false;
	boolean urgent_AirDefenceBuilding2 = false;
	void excuteUrgentAirDefenceConstructionInBaseLocation(BaseLocation base){
		
			if(urgent_AirDefenceBuilding1 == false
//				&& existUnitTypeInRegion(myPlayer, UnitType.Zerg_Creep_Colony, myMainBaseLocation.getRegion()) == false
//				&& existUnitTypeInRegion(myPlayer, UnitType.Zerg_Spore_Colony, myMainBaseLocation.getRegion()) == false
			){
				if(enemyRace == Race.Zerg){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
							base.getTilePosition(), true);
					
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
//							base.getTilePosition(), true);
				}
				else{
					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
							base.getTilePosition(), true);
				}
				urgent_AirDefenceBuilding1 = true;
			}
		
			if(urgent_AirDefenceBuilding2 == false
				&& existUnitTypeInRegion(myPlayer, UnitType.Zerg_Creep_Colony, myMainBaseLocation.getRegion(), false, false) == true
				&& getCountUnitTypeInRegion(myPlayer, UnitType.Zerg_Creep_Colony, myMainBaseLocation.getRegion(), false, false) > 0
			){
				
				if(enemyRace == Race.Zerg){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spore_Colony,
							base.getTilePosition(), false);
					
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spore_Colony,
//							base.getTilePosition(), false);
				}else{
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spore_Colony,
							base.getTilePosition(), false);
				}
				
				// sc76.choi 클로니 대신 Drone 추가를 해준다.
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Drone, false);	//5
				
				urgent_AirDefenceBuilding2 = true;
				
			}
		//}
	}
	
	// sc76.choi initialBuildOrder 때문에 별도의 boolean을 두어 컨트롤 한다.
	boolean urgent_Add_Zergling1 = false;
	void excuteUrgenturgent_Add_Zergling1(){
		
		if(urgent_Add_Zergling1 == false){
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Drone, false);
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Drone, false);
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Zergling, false);
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Zergling, false);
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Zergling, false);
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Overlord, true);
			
//			System.out.println("excuteUrgenturgent_Add_Zergling1");
//			System.out.println("excuteUrgenturgent_Add_Zergling1");
//			System.out.println("excuteUrgenturgent_Add_Zergling1");
//			System.out.println("excuteUrgenturgent_Add_Zergling1");
			
			urgent_Add_Zergling1 = true;
		}
	}
	
	boolean urgent_Add_Lurker1 = false;
	void excuteUrgenturgent_Add_Lurker1(){
		
		if(urgent_Add_Lurker1 == false){
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lurker, false);
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lurker, false);
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lurker, false);
			
			urgent_Add_Lurker1 = true;
		}
	}
	
	
	// sc76.choi 방어건물을 건설합니다
	void executeDefenceConstruction(){
		
		// sc76.choi 공중 공격을 대비한, 스포어 클로니 
		buildAirDefenceUnit();
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24*1 != 0) {
			return;
		}
		
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		// sc76.choi 일꾼 숫자가 적으면 건설하지 않는다.
		if(WorkerManager.Instance().getNumMineralWorkers() <= 6){
			return;
		}
		
		// TODO 앞마당에 적군만 있으면 방어 타워를 본진에 짓는다.
		int enemyUnitsInMyFirstExpansion = InformationManager.Instance().getCombatUnitCountInRegion(BWTA.getRegion(myFirstExpansionLocation.getPosition()), enemyPlayer);
		int myUnitsInMyFirstExpansion = InformationManager.Instance().getCombatUnitCountInRegion(BWTA.getRegion(myFirstExpansionLocation.getPosition()), myPlayer);
		int countHatcheryInMyFirstExpansion = getCountUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		int countCreepColonyInMyFirstExpansion = getCountUnitTypeInRegion(myPlayer, UnitType.Zerg_Creep_Colony, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		int countSunkenColonyInMyFirstExpansion = getCountUnitTypeInRegion(myPlayer, UnitType.Zerg_Sunken_Colony, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		int countSporeColonyInMyFirstExpansion = getCountUnitTypeInRegion(myPlayer, UnitType.Zerg_Spore_Colony, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		
		if(countHatcheryInMyFirstExpansion <= 0
				&& countCreepColonyInMyFirstExpansion <= 0
				&& countSunkenColonyInMyFirstExpansion <= 0
				&& countSporeColonyInMyFirstExpansion <= 0){
//			System.out.println("executeDefenceConstruction 1 cancle!!");
//			System.out.println();
			return;
		}
		
		if(enemyUnitsInMyFirstExpansion > 0 && myUnitsInMyFirstExpansion <= 0){
			if(DEBUG) System.out.println("executeDefenceConstruction 2 cancle!!");
			if(DEBUG) System.out.println();
			return;
		}

		// sc76.choi 테란일 경우, 럴커 개발이 시작되어야 지을 수 있다.
		if(enemyRace == Race.Terran 
			&& myPlayer.isResearching(TechType.Lurker_Aspect) == false
			&& myPlayer.hasResearched(TechType.Lurker_Aspect) == false){
			return;
		}
		
		boolean	isPossibleToConstructDefenseBuildingType1 = false;
		boolean	isPossibleToConstructDefenseBuildingType2 = false;	
		
		// 현재 방어 건물 갯수
		int numberOfMyDefenseBuildingType1 = 0; 
		int numberOfMyDefenseBuildingType2 = 0;
		
		// 저그의 경우 크립 콜로니 갯수를 셀 때 성큰 콜로니 갯수까지 포함해서 세어야, 크립 콜로니를 지정한 숫자까지만 만든다
		//numberOfMyDefenseBuildingType1 += myPlayer.allUnitCount(myDefenseBuildingType1);
		numberOfMyDefenseBuildingType1 += getCountUnitTypeInPosition(myPlayer, myDefenseBuildingType1, DEFENCE_POSITION, Config.TILE_SIZE*7);
		numberOfMyDefenseBuildingType1 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1);
		numberOfMyDefenseBuildingType1 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType1, null);
		//numberOfMyDefenseBuildingType1 += myPlayer.allUnitCount(myDefenseBuildingType2);
		numberOfMyDefenseBuildingType1 += getCountUnitTypeInPosition(myPlayer, myDefenseBuildingType2, DEFENCE_POSITION, Config.TILE_SIZE*7);
		numberOfMyDefenseBuildingType1 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2);
		//numberOfMyDefenseBuildingType2 += myPlayer.allUnitCount(myDefenseBuildingType2);
		numberOfMyDefenseBuildingType2 += getCountUnitTypeInPosition(myPlayer, myDefenseBuildingType2, DEFENCE_POSITION, Config.TILE_SIZE*7);
		numberOfMyDefenseBuildingType2 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2);


		if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0) {
			isPossibleToConstructDefenseBuildingType1 = true;	
		}
		if (myPlayer.completedUnitCount(UnitType.Zerg_Creep_Colony) > 0) {
			isPossibleToConstructDefenseBuildingType2 = true;	
		}
		
		boolean existHatcheryInMyFirstExpansion = existUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(myFirstExpansionLocation.getPosition()), false, false);
		
		// sc76.choi 기본 지역 방어 타워
		if(existHatcheryInMyFirstExpansion == true && enemyUnitsInMyFirstExpansion <= 0){
			if(enemyRace == Race.Terran){
				// 앞마당 방어 건물 증설을 우선적으로 실시한다
				if (isPossibleToConstructDefenseBuildingType1 == true 
					&& numberOfMyDefenseBuildingType1 < necessaryNumberOfDefenseBuilding1) {
					if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1) == 0 ) {
						if (selfAvailableMinerals >= myDefenseBuildingType1.mineralPrice()) {
							
							BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
									BuildOrderItem.SeedPositionStrategy.SecondChokePoint, false);
							
						}			
					}
				}
				if (isPossibleToConstructDefenseBuildingType2 == true
					&& numberOfMyDefenseBuildingType2 < necessaryNumberOfDefenseBuilding2) {
					if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2) == 0 ) {
						if (selfAvailableMinerals >= myDefenseBuildingType2.mineralPrice()) {
							
							BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType2, 
									BuildOrderItem.SeedPositionStrategy.SecondChokePoint, false);
	
						}			
					}
				}
			}else{
				// 앞마당 방어 건물 증설을 우선적으로 실시한다
				if (isPossibleToConstructDefenseBuildingType1 == true 
					&& numberOfMyDefenseBuildingType1 < necessaryNumberOfDefenseBuilding1) {
					if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1) == 0 ) {
						if (selfAvailableMinerals >= myDefenseBuildingType1.mineralPrice()) {
							
							BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
									BuildOrderItem.SeedPositionStrategy.SecondChokePoint, false);
							
						}			
					}
				}
				if (isPossibleToConstructDefenseBuildingType2 == true
					&& numberOfMyDefenseBuildingType2 < necessaryNumberOfDefenseBuilding2) {
					if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2) == 0 ) {
						if (selfAvailableMinerals >= myDefenseBuildingType2.mineralPrice()) {
							
							BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType2, 
									BuildOrderItem.SeedPositionStrategy.SecondChokePoint, false);
	
						}			
					}
				}
			}
		}	
		
		// 앞마당이나, 확장에 적군이 있으면 실행 금지
		if(findAttackAirTargetForMainDefence() != null || findAttackTargetForExpansionDefence() != null){
			return;
		}
	
		// sc76.choi 확장 지역에 성큰을 건설한다.
		int countSelfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer).size();
		if(countSelfRegions >= 3){
			Set<Region> selfRegions = InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer);
			Iterator<Region> it1 = selfRegions.iterator();
			while (it1.hasNext()) {
				Region selfRegion = it1.next();

				if(selfRegion == BWTA.getRegion(myMainBaseLocation.getPosition())) continue;
				
				// creep colony가 없으면
				if(InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Hatchery) == true
						&& InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Creep_Colony) == false
						&& InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Sunken_Colony) == false
						&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Creep_Colony) == 0
						&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Creep_Colony, null) == 0){
					
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, selfRegion.getCenter().toTilePosition(), false);
				}

				// creep colony가 있고, spore colony가 없으면
				if(InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Hatchery) == true
					&& InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Creep_Colony) == true
					&& InformationManager.Instance().existsPlayerBuildingInRegion(selfRegion, myPlayer, UnitType.Zerg_Sunken_Colony) == false
					&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Sunken_Colony) == 0
					&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Sunken_Colony, null) == 0){
					
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony, false);
					
				}
			}
		}
		

	}
	
	/// 공격유닛 생산 건물을 건설합니다
	void executeBuildingConstruction() {
		
//		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
//		if (isInitialBuildOrderFinished == false) {
//			return;
//		}
		
		// sc76.choi 일꾼 숫자가 적으면 건설하지 않는다.
		if(WorkerManager.Instance().getNumMineralWorkers() <= 7){
			return;
		}
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		// TODO 앞마당에 적군만 있으면 방어 타워를 본진에 짓는다.
		int enemyUnitsInMyFirstExpansion = InformationManager.Instance().getCombatUnitCountInRegion(BWTA.getRegion(myFirstExpansionLocation.getPosition()), enemyPlayer);
		int myUnitsInMyFirstExpansion = InformationManager.Instance().getCombatUnitCountInRegion(BWTA.getRegion(myFirstExpansionLocation.getPosition()), myPlayer);
		
		if(isInitialBuildOrderFinished == true 
			&& enemyUnitsInMyFirstExpansion > 0 && myUnitsInMyFirstExpansion <= 0){
			
   			// 즉각 해처리 증설
   			excuteUrgentCombatConstructionInBaseLocation(myMainBaseLocation.getTilePosition());
   			
			if(DEBUG) System.out.println("executeCombatConstruction 1 cancle!!");
			if(DEBUG) System.out.println();
			return;
		}
		
		
		boolean isPossibleToConstructCombatUnitTrainingBuildingType = false;
		int canMultiExpansionCount = 4;
		// sc76.choi TODO 히드라의 갯수로 해처리를 더 지을지 말지 결정한다.
		// sc76.choi 테란일때, 확장 판단
		if(enemyRace == Race.Terran){
			canMultiExpansionCount = 2;
			if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0){
				isPossibleToConstructCombatUnitTrainingBuildingType = true;
			}else{
				isPossibleToConstructCombatUnitTrainingBuildingType = false;
			}
			
		}
		else if(enemyRace == Race.Protoss){
			canMultiExpansionCount = 5;
			if(myOccupiedBaseLocations >= 3){
				if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) > 4){
					isPossibleToConstructCombatUnitTrainingBuildingType = true;
				}else{
					isPossibleToConstructCombatUnitTrainingBuildingType = false;
				}				
			}else{
				if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) > 4){
					isPossibleToConstructCombatUnitTrainingBuildingType = true;
				}else{
					isPossibleToConstructCombatUnitTrainingBuildingType = false;
				}
			}
		}
		// sc76.choi 테란 아닐 때.
		else{
			canMultiExpansionCount = 4;
			if(myOccupiedBaseLocations >= 3){
				if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) > 4){
					isPossibleToConstructCombatUnitTrainingBuildingType = true;
				}else{
					isPossibleToConstructCombatUnitTrainingBuildingType = false;
				}				
			}else{
				if(myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) > 4){
					isPossibleToConstructCombatUnitTrainingBuildingType = true;
				}else{
					isPossibleToConstructCombatUnitTrainingBuildingType = false;
				}
			}
		}
	
		// 현재 공격 유닛 생산 건물 갯수
		int numberOfMyCombatUnitTrainingBuilding = InformationManager.Instance().getTotalHatcheryCount();
		numberOfMyCombatUnitTrainingBuilding += BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getBasicCombatBuildingType());
		numberOfMyCombatUnitTrainingBuilding += ConstructionManager.Instance().getConstructionQueueItemCount(InformationManager.Instance().getBasicCombatBuildingType(), null);
		
		int canAvailableCombatBuildingMinerals = 400;
		
		if(enemyRace == Race.Terran){
			if(numberOfMyCombatUnitTrainingBuilding >= 5){
				canAvailableCombatBuildingMinerals = 550;
			}else if(numberOfMyCombatUnitTrainingBuilding >= 4){
				canAvailableCombatBuildingMinerals = 500;
			}else if(numberOfMyCombatUnitTrainingBuilding >= 3){
				canAvailableCombatBuildingMinerals = 350;
			}
		}else if(enemyRace == Race.Protoss){
			if(numberOfMyCombatUnitTrainingBuilding >= 5){
				canAvailableCombatBuildingMinerals = 6500;
			}else if(numberOfMyCombatUnitTrainingBuilding >= 4){
				canAvailableCombatBuildingMinerals = 600;
			}else if(numberOfMyCombatUnitTrainingBuilding >= 3){
				canAvailableCombatBuildingMinerals = 500;
			}
		}else{
			if(numberOfMyCombatUnitTrainingBuilding >= 5){
				canAvailableCombatBuildingMinerals = 550;
			}else if(numberOfMyCombatUnitTrainingBuilding >= 4){
				canAvailableCombatBuildingMinerals = 550;
			}else if(numberOfMyCombatUnitTrainingBuilding >= 3){
				canAvailableCombatBuildingMinerals = 450;
			}
		}
		// 공격 유닛 생산 건물 증설 : 돈이 남아돌면 실시. 최대 8개 까지만
		if (isPossibleToConstructCombatUnitTrainingBuildingType == true
			&& selfAvailableMinerals > canAvailableCombatBuildingMinerals){
			
//			System.out.println("canAvailableCombatBuildingMinerals          : " + canAvailableCombatBuildingMinerals);
//			System.out.println("canMultiExpansionCount                      : " + canMultiExpansionCount);
			
			if (numberOfMyCombatUnitTrainingBuilding < Config.numberOfMyCombatUnitTrainingBuilding) { // 10
				
				// canMultiExpansionCount 개 까지는 main location 주변에 나머지는 확장한다.
				if (numberOfMyCombatUnitTrainingBuilding < canMultiExpansionCount
					 && BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hatchery) == 0 
					 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0) 
				{
					// sc76.choi 해처리 갯수가 3개 이상이면 밖에 짓는다.
					// sc76.choi 뛰울 최소한 공간 조정
					if(InformationManager.Instance().getTotalHatcheryCount() <= canMultiExpansionCount){
						Config.BuildingResourceDepotSpacing = 1; // 뛰울 최소한 공간 조정
						BuildManager.Instance().buildQueue.queueAsHighestPriority(InformationManager.Instance().getBasicCombatBuildingType(), 
								myMainBaseLocation.getTilePosition(), false);
					}
//					else{
//						Config.BuildingResourceDepotSpacing = 1; // 뛰울 최소한 공간 조정
//						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hatchery,
//								seedPositionStrategyOfMyDefenseBuildingType.SecondChokePoint,  false);
//					}
					
					if(DEBUG) System.out.println("**********************************************************");
					if(DEBUG) System.out.println("                  add hatchery 1");
					if(DEBUG) System.out.println("**********************************************************");
					if(DEBUG) System.out.println();
				}
				// 확장지역에 건설한다.
				else{
					if(combatState == CombatState.attackStarted || combatState == CombatState.defenseMode || combatState == CombatState.eliminateEnemy){
						if(BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getBasicCombatBuildingType()) == 0
							&& ConstructionManager.Instance().getConstructionQueueItemCount(InformationManager.Instance().getBasicCombatBuildingType(), null) == 0){
							
							if(DEBUG) System.out.println("**********************************************************");
							if(DEBUG) System.out.println("                  add hatchery 2");
							if(DEBUG) System.out.println("**********************************************************");
							if(DEBUG) System.out.println();
							
							int mineralsCount = 0;
							for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(myPlayer)) {
								mineralsCount += baseLocation.getMinerals().size();
							}
							
							// sc76.choi 미네랄이 얼마 없으면 강제 확장한다.
							if(InformationManager.Instance().getTotalHatcheryCount() >= 3 && mineralsCount <= 13){
								
								BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hatchery,
										BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified,  false);
								
							}else{
								
								BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
										BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified,  false);
								
							}
						}
					}
				}
			}
		}
	}
	
	// sc76.choi TODO 확장할 가장 좋은 곳을 찾는다.
	// sc76.choi TODO 확장 방어와 확장된 곳에 drone 생산, 가스 건설 등등을 해결해야 한다.
	BaseLocation bestMultiLocation = null;
	BaseLocation bestMultiLocation1 = null;
	
	public BaseLocation getBestMultiLocation(){
		
//		if(isInitialBuildOrderFinished == false) return null;
		if(enemyMainBaseLocation == null) return null;
			
		double tempDistFromMyMainLocation = 100000000.0;
		
		BaseLocation myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(myPlayer);
		BaseLocation enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(enemyPlayer);
		BaseLocation enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(enemyPlayer);
		
		//for(BaseLocation b : BWTA.getStartLocations()){
		for(BaseLocation b : BWTA.getBaseLocations()){
			
//			// 가스가 없다면 skip
//			if(enemyRace == Race.Terran && (b.getGeysers().isEmpty() || b.getGeysers().size() == 0)){
//				continue;
//			}
			
			
			// 적진과 가깝다면 skip
			if(enemyMainBaseLocation != null && b.getDistance(enemyMainBaseLocation.getPosition()) < Config.TILE_SIZE*80){
				continue;
			}
			
			// sc76.choi 나의 본진
			if(b.equals(myMainBaseLocation)
				|| (b.getX() == myMainBaseLocation.getX() && b.getY() == myMainBaseLocation.getY())) {
				continue;
			}
			
			// sc76.choi 나의 확장
			if(b.equals(myFirstExpansionLocation)
				|| (b.getX() == myFirstExpansionLocation.getX() && b.getY() == myFirstExpansionLocation.getY())) {
				continue;
			}
			
			// sc76.choi 적의 본진
			if(enemyMainBaseLocation != null && b.equals(enemyMainBaseLocation)
				|| (b.getX() == enemyMainBaseLocation.getX() && b.getY() == enemyMainBaseLocation.getY())) {
				continue;
			}
			
			// sc76.choi 적의 확장
			if(enemyMainBaseLocation != null && b.equals(enemyFirstExpansionLocation)
				|| (b.getX() == enemyFirstExpansionLocation.getX() && b.getY() == enemyFirstExpansionLocation.getY())) {
				continue;
			}
			
			double nowDist = myMainBaseLocation.getGroundDistance(b);
			if(nowDist > 0 && nowDist < tempDistFromMyMainLocation){
				tempDistFromMyMainLocation = nowDist;
				bestMultiLocation = b;
			}
		}

		int numberOfMyCombatUnitTrainingBuilding = InformationManager.Instance().getTotalHatcheryCount() 
				                                   + myPlayer.incompleteUnitCount(UnitType.Zerg_Hatchery);
		
		// sc76.choi 해처리가 충분하면 다음 확장 포지션
		int countForMultibyRace = 0;
		if(enemyRace == Race.Terran){
			countForMultibyRace = 2;
		}else{
			countForMultibyRace = 3;
		}
		
		if(numberOfMyCombatUnitTrainingBuilding >= 2 && numberOfMyCombatUnitTrainingBuilding <= 3){
			bestMultiLocation = bestMultiLocation;
			bestMultiLocation1 = bestMultiLocation;
		}else if(numberOfMyCombatUnitTrainingBuilding >= 3 && numberOfMyCombatUnitTrainingBuilding <= 5){
			
			if(bestMultiLocation1 != null && existUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(bestMultiLocation1.getPosition()), false, false) == false){
				bestMultiLocation = bestMultiLocation1;
			}else{
				bestMultiLocation = getBestMultiLocation2();
			}
			
			//bestMultiLocation2 = getBestMultiLocation2();
		}else if(numberOfMyCombatUnitTrainingBuilding >= 5 && numberOfMyCombatUnitTrainingBuilding <= 7){
			
			if(bestMultiLocation2 != null && existUnitTypeInRegion(myPlayer, UnitType.Zerg_Hatchery, BWTA.getRegion(bestMultiLocation2.getPosition()), false, false) == false){
				bestMultiLocation = bestMultiLocation2;
			}else{
				bestMultiLocation = getBestMultiLocation3();
			}
		}else if(numberOfMyCombatUnitTrainingBuilding >= 7){
			
			bestMultiLocation = getBestMultiLocation4();
		}else{
			bestMultiLocation = myMainBaseLocation;
		}
		
		return bestMultiLocation;
	}
	
	// sc76.choi TODO 확장할 가장 좋은 곳을 찾는다.
	// sc76.choi TODO 확장 방어와 확장된 곳에 drone 생산, 가스 건설 등등을 해결해야 한다.
	BaseLocation bestMultiLocation2 = null;
	public BaseLocation getBestMultiLocation2(){
		
		if(isInitialBuildOrderFinished == false) return null;
		if(enemyMainBaseLocation == null) return null;
		
		double tempDistFromMyMainLocation = 100000000.0;
		
		BaseLocation myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		BaseLocation enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
		BaseLocation enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.enemy());
		
		// 그냥 베이스에서 찾는다.
		for(BaseLocation b : BWTA.getBaseLocations()){
			
			
			//System.out.println("getBestMultiLocation2 111111111 : " + b.getTilePosition());
			
			// 적진과 가깝다면 skip
			if(b.getDistance(enemyMainBaseLocation.getPosition()) < Config.TILE_SIZE*80){
				continue;
			}
			
			// 가스가 없다면 skip
			if(enemyRace == Race.Terran && (b.getGeysers().isEmpty() || b.getGeysers().size() == 0)){
				continue;
			}
			
			// sc76.choi 나의 본진
			if(b.equals(myMainBaseLocation)
				|| (b.getX() == myMainBaseLocation.getX() && b.getY() == myMainBaseLocation.getY())) {
				continue;
			}
			

			//System.out.println("getBestMultiLocation2 222222222 : " + b.getTilePosition());
			// sc76.choi 나의 확장			
			if(b.equals(myFirstExpansionLocation)
					|| (b.getX() == myFirstExpansionLocation.getX() && b.getY() == myFirstExpansionLocation.getY())) {
					continue;
			}
			
			//System.out.println("getBestMultiLocation2 333333333 : " + b.getTilePosition());
			// sc76.choi 적의 본진			
			if(b.equals(enemyMainBaseLocation)
				|| (b.getX() == enemyMainBaseLocation.getX() && b.getY() == enemyMainBaseLocation.getY())) {
				continue;
			}
			
			//System.out.println("getBestMultiLocation2 444444444 : " + b.getTilePosition());
			// sc76.choi 적의 확장			
			if(b.equals(enemyFirstExpansionLocation)
				|| (b.getX() == enemyFirstExpansionLocation.getX() && b.getY() == enemyFirstExpansionLocation.getY())) {
				continue;
			}
			
			//System.out.println("getBestMultiLocation2 555555555 : " + b.getTilePosition());
			// sc76.choi 첫번째 멀티			
			if(bestMultiLocation1 != null &&
				(b.equals(bestMultiLocation1)
					|| (b.getX() == bestMultiLocation1.getX() && b.getY() == bestMultiLocation1.getY()))
			) {
				continue;
			}
			
			double nowDist = myMainBaseLocation.getGroundDistance(b);
			if(nowDist > 0 && nowDist < tempDistFromMyMainLocation){
				tempDistFromMyMainLocation = nowDist;
				bestMultiLocation2 = b;
			}
		}
		
		//System.out.println("bestMultiLocation     666666666 : " + bestMultiLocation.getTilePosition());
		// System.out.println("getBestMultiLocation2 777777777 : " + bestMultiLocation2.getTilePosition());

		return bestMultiLocation2;
	}
	
	// sc76.choi TODO 확장할 가장 좋은 곳을 찾는다.
	// sc76.choi TODO 확장 방어와 확장된 곳에 drone 생산, 가스 건설 등등을 해결해야 한다.
	BaseLocation bestMultiLocation3 = null;
	public BaseLocation getBestMultiLocation3(){
		
		if(isInitialBuildOrderFinished == false) return null;
		if(enemyMainBaseLocation == null) return null;
		
		double tempDistFromMyMainLocation = 100000000.0;
		
		BaseLocation myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		BaseLocation enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
		BaseLocation enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.enemy());
		
		// 그냥 베이스에서 찾는다.
		for(BaseLocation b : BWTA.getBaseLocations()){
			
			// 가스가 없다면 skip
			if(enemyRace == Race.Terran && (b.getGeysers().isEmpty() || b.getGeysers().size() == 0)){
				continue;
			}
			
			// 적진과 가깝다면 skip
			if(b.getDistance(enemyMainBaseLocation.getPosition()) < Config.TILE_SIZE*80){
				continue;
			}
			
			//System.out.println("getBestMultiLocation3 111111111 : " + b.getTilePosition());
			// sc76.choi 나의본진
			if(b.equals(myMainBaseLocation)
				|| (b.getX() == myMainBaseLocation.getX() && b.getY() == myMainBaseLocation.getY())) {
				continue;
			}
			
			// sc76.choi 나의확장			
			if(b.equals(myFirstExpansionLocation)
					|| (b.getX() == myFirstExpansionLocation.getX() && b.getY() == myFirstExpansionLocation.getY())) {
					continue;
			}
			
			// sc76.choi 적의 본진
			if(b.equals(enemyMainBaseLocation)
				|| (b.getX() == enemyMainBaseLocation.getX() && b.getY() == enemyMainBaseLocation.getY())) {
				continue;
			}
			
			//System.out.println("getBestMultiLocation3 444444444 : " + b.getTilePosition());
			// sc76.choi 적의 확장
			if(b.equals(enemyFirstExpansionLocation)
				|| (b.getX() == enemyFirstExpansionLocation.getX() && b.getY() == enemyFirstExpansionLocation.getY())) {
				continue;
			}
			
			//System.out.println("getBestMultiLocation3 555555555 : " + b.getTilePisosition());
			// sc76.choi 첫번째 멀티
			if(bestMultiLocation1 != null && 
				(b.equals(bestMultiLocation1)
					|| (b.getX() == bestMultiLocation1.getX() && b.getY() == bestMultiLocation1.getY()))) {
				continue;
			}
			
			//System.out.println("getBestMultiLocation3 555555555 : " + b.getTilePosition());
			// sc76.choi 두번째 멀티
			if(bestMultiLocation2 != null && 
				(b.equals(bestMultiLocation2)
					|| (b.getX() == bestMultiLocation2.getX() && b.getY() == bestMultiLocation2.getY()))) {
				continue;
			}
			
			//double nowDist = enemyMainBaseLocation.getAirDistance(b);
			int nowDist = BWTA.getGroundDistance2(myMainBaseLocation.getTilePosition(), b.getTilePosition());
			if(nowDist > 0 && nowDist < tempDistFromMyMainLocation){
				tempDistFromMyMainLocation = nowDist;
				bestMultiLocation3 = b;
			}
		}
		
		// System.out.println("bestMultiLocation     666666666 : " + bestMultiLocation.getTilePosition());
		// System.out.println("getBestMultiLocation3 777777777 : " + bestMultiLocation3.getTilePosition());

		return bestMultiLocation3;
	}
	
	// sc76.choi TODO 확장할 가장 좋은 곳을 찾는다.
	// sc76.choi TODO 확장 방어와 확장된 곳에 drone 생산, 가스 건설 등등을 해결해야 한다.
	BaseLocation bestMultiLocation4 = null;
	public BaseLocation getBestMultiLocation4(){
		
		if(isInitialBuildOrderFinished == false) return null;
		if(enemyMainBaseLocation == null) return null;
		
		double tempDistFromMyMainLocation = 100000000.0;
		
		BaseLocation myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		BaseLocation enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
		BaseLocation enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.enemy());
		
		// 그냥 베이스에서 찾는다.
		for(BaseLocation b : BWTA.getStartLocations()){
			
			// 가스가 없다면 skip
			if(b.getGeysers().isEmpty() || b.getGeysers().size() == 0){
				continue;
			}
			
			// 적진과 가깝다면 skip
			if(b.getDistance(enemyMainBaseLocation.getPosition()) < Config.TILE_SIZE*80){
				continue;
			}
			
			// sc76.choi 나의본진
			if(b.equals(myMainBaseLocation)
				|| (b.getX() == myMainBaseLocation.getX() && b.getY() == myMainBaseLocation.getY())) {
				continue;
			}
			
			// sc76.choi 나의확장			
			if(b.equals(myFirstExpansionLocation)
					|| (b.getX() == myFirstExpansionLocation.getX() && b.getY() == myFirstExpansionLocation.getY())) {
					continue;
			}
			
			// sc76.choi 적의 본진
			if(b.equals(enemyMainBaseLocation)
				|| (b.getX() == enemyMainBaseLocation.getX() && b.getY() == enemyMainBaseLocation.getY())) {
				continue;
			}
			
			// sc76.choi 적의 확장
			if(b.equals(enemyFirstExpansionLocation)
				|| (b.getX() == enemyFirstExpansionLocation.getX() && b.getY() == enemyFirstExpansionLocation.getY())) {
				continue;
			}
			
			// sc76.choi 첫번째 멀티
			if(bestMultiLocation1 != null && 
				(b.equals(bestMultiLocation1)
					|| (b.getX() == bestMultiLocation1.getX() && b.getY() == bestMultiLocation1.getY()))) {
				continue;
			}
			
			// sc76.choi 두번째 멀티
			if(bestMultiLocation2 != null && 
				(b.equals(bestMultiLocation2)
					|| (b.getX() == bestMultiLocation2.getX() && b.getY() == bestMultiLocation2.getY()))) {
				continue;
			}

			// sc76.choi 세번째 멀티
			if(bestMultiLocation3 != null && 
				(b.equals(bestMultiLocation3)
					|| (b.getX() == bestMultiLocation3.getX() && b.getY() == bestMultiLocation3.getY()))) {
				continue;
			}
			
			int nowDist = BWTA.getGroundDistance2(myMainBaseLocation.getTilePosition(), b.getTilePosition());
			if(nowDist > 0 && nowDist < tempDistFromMyMainLocation){
				tempDistFromMyMainLocation = nowDist;
				bestMultiLocation4 = b;
			}
		}
		
		return bestMultiLocation4;
	}	
	
	/// 업그레이드 및 테크 리서치를 실행합니다
	private KCUpgradeAndTech upGradeAndTech = new KCUpgradeAndTech();
	void executeUpgradeAndTechResearch() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}

		// sc76.choi 적군의 종족에 따라 업그레이드 전략을 분기한다.
		// sc76.choi TODO 업그레이드를 위한 자원 확보를 해야하는 로직이 필요하다.
		if (enemyRace == Race.Protoss) {
			upGradeAndTech.upGradeAndTechAgainstProtoss(); // sc76.choi 생산 건물의 업그레이드
			upGradeAndTech.chamberUpgradeAgainstProtoss(); // sc76.choi 챔버 건물의 업그레이드
		}
		else if (enemyRace == Race.Terran) {
			upGradeAndTech.upGradeAndTechAgainstTerran(); // sc76.choi 생산 건물의 업그레이드
			upGradeAndTech.chamberUpgradeAgainstTerran(); // sc76.choi 챔버 건물의 업그레이드
		}
		else if (enemyRace == Race.Zerg) {
			upGradeAndTech.upGradeAndTechAgainstZerg(); // sc76.choi 생산 건물의 업그레이드
			upGradeAndTech.chamberUpgradeAgainstZerg(); // sc76.choi 챔버 건물의 업그레이드
		}else{
			upGradeAndTech.upGradeAndTechAgainstProtoss(); // sc76.choi 생산 건물의 업그레이드
			upGradeAndTech.chamberUpgradeAgainstProtoss(); // sc76.choi 챔버 건물의 업그레이드
		}
	}

	/// 특수 유닛을 생산할 수 있도록 테크트리에 따라 건설을 실시합니다
	private KCTechTreeUp techTreeUp = new KCTechTreeUp();
	void executeTechTreeUpConstruction() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		// 적군의 종족에 따라
		if (enemyRace == Race.Protoss) {
			techTreeUp.techTreeupAgainstProtoss();
		}
		else if (enemyRace == Race.Terran) {
			techTreeUp.techTreeupAgainstTerran();
		}
		else if (enemyRace == Race.Zerg) {
			techTreeUp.techTreeupAgainstZerg();
		}else{
			techTreeUp.techTreeupAgainstProtoss();
		}
	}


	/// 공격유닛을 계속 추가 생산합니다
	public void executeCombatUnitTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 1초에 4번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) {
			return;
		}
		
		// sc76.choi 전투 상황에 맞게 뽑을 유닛을 컨트롤 한다.(CombatNeedUnitState)
		updatebuildOrderArray();
		
		if (myPlayer.supplyUsed() <= 390 ){
			// 공격 유닛 생산
			UnitType nextUnitTypeToTrain = getNextCombatUnitTypeToTrain();
			
			UnitType producerType = (new MetaType(nextUnitTypeToTrain)).whatBuilds();
			
			for(Unit unit : myPlayer.getUnits()) {
				
				if (unit.getType() == producerType) {
					if (unit.isTraining() == false && unit.isMorphing() == false) {

						if (BuildManager.Instance().buildQueue.getItemCount(nextUnitTypeToTrain) == 0) {	

							boolean isPossibleToTrain = false;
							boolean isLowestPriority = false; // sc76.choi Priority를 조정한다.
							
							if (nextUnitTypeToTrain == UnitType.Zerg_Drone) {
								int allCountOfCombatUnitType1 = this.getCurrentTrainUnitCount(UnitType.Zerg_Drone);
								
								if (allCountOfCombatUnitType1 <= Config.numberOfMyWorkerUnitTrainingBuilding) {
									isPossibleToTrain = true;
								}else{
									isPossibleToTrain = false;
								}
								isLowestPriority = false;
							}
							else if (nextUnitTypeToTrain == UnitType.Zerg_Zergling ) {
								if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0) {
									
									// sc76.choi 생산 제한
									int allCountOfCombatUnitType1 = this.getCurrentTrainUnitCount(myCombatUnitType1);
									if (allCountOfCombatUnitType1 <= maxNumberOfTrainUnitType1) {
										isPossibleToTrain = true;
									}else{
										isPossibleToTrain = false;
									}
									isLowestPriority = false;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Zerg_Hydralisk) {
								if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0) {
									isPossibleToTrain = true;
									isLowestPriority = false;
								}							
							}
							// sc76.choi 럴커 생산 시, 주의 히드라가 없으면 락이 걸린다.
							else if (nextUnitTypeToTrain == UnitType.Zerg_Lurker) {
								
								if(buildState == BuildState.carrier_P){
									isPossibleToTrain = false;
								}
								else if (unit.getType() == UnitType.Zerg_Hydralisk 
									&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
									&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) > 0
									&& myPlayer.hasResearched(TechType.Lurker_Aspect) == true) {
									
									// sc76.choi 럴커의 생산제한을 한다.
									int allCountOfCombatUnitType3 = this.getCurrentTrainUnitCount(myCombatUnitType3);
									if (allCountOfCombatUnitType3 <= maxNumberOfTrainUnitType3) {
										isPossibleToTrain = true;
									}else{
										isPossibleToTrain = false;
									}
									
									if(myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 1){
										isLowestPriority = false;
									}else{
										isLowestPriority = false;
									}
								}							
							}
							// sc76.choi TODO 가스가 작으면 만들지 않는다.
							else if (nextUnitTypeToTrain == UnitType.Zerg_Mutalisk) {
								
								// sc76.choi 저그나, 프로토스 일때는 생산하지 않음
								if(enemyRace == Race.Zerg || enemyRace == Race.Protoss){
									isPossibleToTrain = false;
								}
								
								else if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
									|| myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0) {
									
									// sc76.choi 뮤탈의 생산제한을 한다.
									int allCountOfCombatUnitType4 = this.getCurrentTrainUnitCount(myCombatUnitType4);
									if (allCountOfCombatUnitType4 <= maxNumberOfTrainUnitType4) {
										isPossibleToTrain = true;
									}else{
										isPossibleToTrain = false;
									}
									
									isLowestPriority = false;
								}							
							}
							// sc76.choi TODO 가스가 작으면 만들지 않는다.
							else if (nextUnitTypeToTrain == UnitType.Zerg_Ultralisk) {
								if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0) {
									isPossibleToTrain = true;
									isLowestPriority = false;
								}							
							}
							// sc76.choi 가디언 생산 시, 주의 뮤탈이 없으면 락이 걸린다.
							else if (nextUnitTypeToTrain == UnitType.Zerg_Guardian) {
								
								if (unit.getType() == UnitType.Zerg_Mutalisk
									&& myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0) {
									
									// sc76.choi 럴커의 생산제한을 한다.
									int allCountOfCombatUnitType6 = this.getCurrentTrainUnitCount(myCombatUnitType6);
									if (myCombatUnitType4List.size() >= 2) {
										isPossibleToTrain = true;
									}else{
										isPossibleToTrain = false;
									}
									
									if(myPlayer.completedUnitCount(UnitType.Zerg_Guardian) >= 1){
										isLowestPriority = false;
									}else{
										isLowestPriority = true;
									}
								}							
							}
							
							
							// sc76.choi 병력 생산 지시
							if (isPossibleToTrain) {
								
								if(nextUnitTypeToTrain == UnitType.Zerg_Drone){
									if(DEBUG) System.out.println("Drone !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
								}
								// sc76.choi TODO 주의 럴커일 경우에는 히드라가 반드시 있어야 한다. 히드라 체크를 해야한다.
								if ((nextUnitTypeToTrain == UnitType.Zerg_Lurker && myCombatUnitType2List.size() >= 2)
									|| (nextUnitTypeToTrain == UnitType.Zerg_Guardian && myCombatUnitType4List.size() >= 2)
								){
									if(enemyRace == Race.Terran && myCombatUnitType2List.size() >= 4){
										BuildManager.Instance().buildQueue.queueAsHighestPriority(nextUnitTypeToTrain, bestMultiLocation.getTilePosition(), isLowestPriority);
									}else{
										BuildManager.Instance().buildQueue.queueAsHighestPriority(nextUnitTypeToTrain, isLowestPriority);
									}
								}else{
									if(enemyRace == Race.Terran && myCombatUnitType2List.size() >= 4){
										BuildManager.Instance().buildQueue.queueAsHighestPriority(nextUnitTypeToTrain, bestMultiLocation.getTilePosition(), isLowestPriority);
									}else{
										BuildManager.Instance().buildQueue.queueAsLowestPriority(nextUnitTypeToTrain, isLowestPriority);
									}
								}
							}
							
							nextTargetIndexOfBuildOrderArray++;
							if (nextTargetIndexOfBuildOrderArray >= buildOrderArrayOfMyCombatUnitType.length) {
								nextTargetIndexOfBuildOrderArray = 0;
							}	

							break;
						}
					}
				}
			}
			
//			// 특수 유닛 생산 - 1 오버로드		
//			if (BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType1) == 0) {	
//				
//				boolean isPossibleToTrain = false;
//				if (mySpecialUnitType1 == UnitType.Protoss_Observer) {
//					if (myPlayer.completedUnitCount(UnitType.Protoss_Robotics_Facility) > 0 
//						&& myPlayer.completedUnitCount(UnitType.Protoss_Observatory) > 0 ) {
//						isPossibleToTrain = true;
//					}							
//				}
//				else if (mySpecialUnitType1 == UnitType.Terran_Science_Vessel) {
//					if (myPlayer.completedUnitCount(UnitType.Terran_Starport) > 0 
//						&& myPlayer.completedUnitCount(UnitType.Terran_Control_Tower) > 0 
//						&& myPlayer.completedUnitCount(UnitType.Terran_Science_Facility) > 0 ) {
//						isPossibleToTrain = true;
//					}							
//				}
//				// 저그 오버로드는 executeSupplyManagement 에서 이미 생산하므로 추가 생산할 필요 없다
//				
//				boolean isNecessaryToTrainMore = false;
//				if (myPlayer.allUnitCount(mySpecialUnitType1) + BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType1) 
//						< maxNumberOfSpecialUnitType1) {
//					isNecessaryToTrainMore = true;
//				}							
//				
//				if (isPossibleToTrain && isNecessaryToTrainMore) {
//					
//					producerType = (new MetaType(mySpecialUnitType1)).whatBuilds();
//
//					for(Unit unit : myPlayer.getUnits()) {
//						if (unit.getType() == producerType) {
//							if (unit.isTraining() == false && unit.isMorphing() == false) {
//		
//								BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType1, true);
//								break;
//							}
//						}
//					}
//				}
//			}

			///////////////////////////////////////////////////////////////////////////////////////////////
			// 럴커 유닛 생산
			// TODO 자원이 없을 때는 True로 생산하면 lock이 걸린다.
			// sc76.choi TODO 가스가 작으면 만들지 않는다.
//			if (BuildManager.Instance().buildQueue.getItemCount(myCombatUnitType3) == 0) {	
//				
//				boolean isPossibleToTrain = false;
//
//				if (myCombatUnitType3 == UnitType.Zerg_Lurker) {
//					if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
//						  && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 1) {
//						if(myPlayer.hasResearched(TechType.Lurker_Aspect) == true){
//							isPossibleToTrain = true;
//						}
//					}							
//				}
//				
//				boolean isNecessaryToTrainMore = false;
//				int allCountOfCombatUnitType3 = this.getCurrentTrainUnitCount(myCombatUnitType3);
//				
//				if (allCountOfCombatUnitType3 < maxNumberOfCombatUnitType3) {
//					isNecessaryToTrainMore = true;
//				}							
//				
//				if (isPossibleToTrain && isNecessaryToTrainMore) {
//					producerType = (new MetaType(myCombatUnitType3)).whatBuilds();
//					for(Unit unit : myPlayer.getUnits()) {
//						if (unit.getType() == producerType) {
//							if (unit.isTraining() == false && unit.isMorphing() == false) {
//								
//								if(allCountOfCombatUnitType3 <= 0 && selfGas >= 100){
//									//System.out.println("allCountOfCombatUnitType3 : " + allCountOfCombatUnitType3);
//									BuildManager.Instance().buildQueue.queueAsHighestPriority(myCombatUnitType3, false);
//								}else{
//									BuildManager.Instance().buildQueue.queueAsLowestPriority(myCombatUnitType3, false);									
//								}
//								break;
//							}
//							
//						}
//					}
//				}
//			}
			
			// 특수 유닛 생산 - 2 디파일러
			// TODO 자원이 없을 때는 True로 생산하면 lock이 걸린다.
			// sc76.choi TODO 가스가 작으면 만들지 않는다.
			if (BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType2) == 0) {	
				
				boolean isPossibleToTrain = false;

				if (mySpecialUnitType2 == UnitType.Zerg_Defiler) {
					if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0) {
						isPossibleToTrain = true;
					}							
				}
				
				boolean isNecessaryToTrainMore = false;
				
				int allCountOfSpecialUnitType2 = this.getCurrentTrainUnitCount(mySpecialUnitType2);
				
				if (allCountOfSpecialUnitType2 < maxNumberOfSpecialUnitType2) {
					isNecessaryToTrainMore = true;
				}							
				
				if (isPossibleToTrain && isNecessaryToTrainMore) {
					
					producerType = (new MetaType(mySpecialUnitType2)).whatBuilds();
					
					for(Unit unit : myPlayer.getUnits()) {
						if (unit.getType() == producerType) {
							if (unit.isTraining() == false && unit.isMorphing() == false) {
								
								if(selfGas < 300){
									if(selfGas > 100){
										BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType2, false);
									}
								}else{
									BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType2, true);
								}
								break;
							}
							
						}
					}
				}
			}
			
			// 특수 유닛 생산 - 3 스커지
			// TODO 자원이 없을 때는 True로 생산하면 lock이 걸린다.	
			// sc76.choi TODO 가스가 작으면 만들지 않는다.
			if (BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType3) == 0) {	
				
				boolean isPossibleToTrain = false;
				if (mySpecialUnitType3 == UnitType.Zerg_Scourge) {
					if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
						 || myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0) {
						isPossibleToTrain = true;
					}							
				}
				
				boolean isNecessaryToTrainMore = false;
				
				// 저그 종족의 경우, Egg 안에 있는 것까지 카운트 해야함 
//				int allCountOfSpecialUnitType3 = myPlayer.allUnitCount(mySpecialUnitType3) + BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType3);
//				if (mySpecialUnitType3.getRace() == Race.Zerg) {
//					for(Unit unit : myPlayer.getUnits()) {
//
//						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == mySpecialUnitType3) {
//							allCountOfSpecialUnitType3++;
//						}
//						// 갓태어난 유닛은 아직 반영안되어있을 수 있어서, 추가 카운트를 해줘야함
//						//if (unit.getType() == mySpecialUnitType2 && unit.isConstructing()) {
//						//	allCountOfSpecialUnitType2++;
//						//}
//					}
//					  
//				}
				
				int allCountOfSpecialUnitType3 = this.getCurrentTrainUnitCount(mySpecialUnitType3);
				
				if (allCountOfSpecialUnitType3 < maxNumberOfTrainSpecialUnitType3) {
					// sc76.choi 히드라가 충분 할때, 생산 시작한다
//					if((myCombatUnitType2List.size() + myCombatUnitType2MultiDefenceList.size()) >= 4){
//						|| myCombatUnitType6List.size() >= 1){
						isNecessaryToTrainMore = true;
//					}
				}							
				
				if (isPossibleToTrain && isNecessaryToTrainMore) {
					
					producerType = (new MetaType(mySpecialUnitType3)).whatBuilds();
					
					for(Unit unit : myPlayer.getUnits()) {
						if (unit.getType() == producerType) {
							if (unit.isTraining() == false && unit.isMorphing() == false) {
		
								if(selfGas < 300){
									if(selfGas > 100){
										BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType3, false);
									}
								}else{
									BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType3, true);
								}
								break;
							}
							
						}
					}
				}
			}
			
			// 특수 유닛 생산 - 4 퀸
			// TODO 자원이 없을 때는 True로 생산하면 lock이 걸린다.	
			// sc76.choi TODO 가스가 작으면 만들지 않는다.
			if (BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType4) == 0) {	
				
				boolean isPossibleToTrain = false;
				if (mySpecialUnitType4 == UnitType.Zerg_Queen) {
					if (myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0) {
						isPossibleToTrain = true;
					}							
				}
				
				boolean isNecessaryToTrainMore = false;
				
				
				// 저그 종족의 경우, Egg 안에 있는 것까지 카운트 해야함 
				int allCountOfSpecialUnitType4 = myPlayer.allUnitCount(mySpecialUnitType4) + BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType4);
				if (mySpecialUnitType4.getRace() == Race.Zerg) {
					for(Unit unit : myPlayer.getUnits()) {

						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == mySpecialUnitType4) {
							allCountOfSpecialUnitType4++;
						}
						// 갓태어난 유닛은 아직 반영안되어있을 수 있어서, 추가 카운트를 해줘야함
						//if (unit.getType() == mySpecialUnitType2 && unit.isConstructing()) {
						//	allCountOfSpecialUnitType2++;
						//}
					}
					  
				}
				if (allCountOfSpecialUnitType4 < maxNumberOfTrainSpecialUnitType4) {
					// sc76.choi 뮤탈이 4마리 이상 있으면 생산시작한다.
					if(myCombatUnitType4List.size() >= 4){
						isNecessaryToTrainMore = true;
					}
				}							
				
				if (isPossibleToTrain && isNecessaryToTrainMore) {
					
					producerType = (new MetaType(mySpecialUnitType3)).whatBuilds();
					
					for(Unit unit : myPlayer.getUnits()) {
						if (unit.getType() == producerType) {
							if (unit.isTraining() == false && unit.isMorphing() == false) {
								if(selfGas < 300){
									if(selfGas > 100){
										BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType4, false);
									}
								}else{
									BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType4, true);
								}
								break;
							}
							
						}
					}
				}
			} // 특수 유닛 생산 - 4 퀸			
		}
	}

	// sc76.choi 현재 생산 진행 중인 Type의 수를 반환한다.
	public int getCurrentTrainUnitCount(UnitType type){
		
		// sc76.choi Train 중이거나, Queue에 있는 것 까지 합친다. 
		int allCountOfSpecialUnitType3 = myPlayer.allUnitCount(type) + BuildManager.Instance().buildQueue.getItemCount(type);

		// 저그 종족의 경우, Egg 안에 있는 것까지 카운트 해야함 
		if (type.getRace() == Race.Zerg) {
			for(Unit unit : myPlayer.getUnits()) {
				
				// unit.getBuildType() 이 럴커로 나오지 않고 히드라로 나온다.
				if(unit.getType() == UnitType.Zerg_Lurker_Egg && type == UnitType.Zerg_Lurker){
					if(DEBUG) System.out.println("unit.getBuildType("+type+") "+allCountOfSpecialUnitType3+ ": " + unit.getID() + " " + unit.getBuildType());
					allCountOfSpecialUnitType3++;
				}
				
				if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == type) {
					allCountOfSpecialUnitType3++;
				}
				// 갓태어난 유닛은 아직 반영안되어있을 수 있어서, 추가 카운트를 해줘야함
				if (unit.getType() == mySpecialUnitType2 && unit.isConstructing()) {
					allCountOfSpecialUnitType3++;
				}
			}
			  
		}

		return allCountOfSpecialUnitType3;
	}
	
	/// 다음에 생산할 공격유닛 UnitType 을 리턴합니다
	public UnitType getNextCombatUnitTypeToTrain() {
		
		UnitType nextUnitTypeToTrain = null;
		//System.out.println("getNextCombatUnitTypeToTrain buildOrderArrayOfMyCombatUnitType.length : " + buildOrderArrayOfMyCombatUnitType.length);
		//System.out.println("getNextCombatUnitTypeToTrain nextTargetIndexOfBuildOrderArray : " + nextTargetIndexOfBuildOrderArray);
		try{
			
			if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 0) {
				nextUnitTypeToTrain = UnitType.Zerg_Drone; // 드론
			}
			else if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 1) {
				nextUnitTypeToTrain = myCombatUnitType1; // 저글링
			}
			else if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 2) {
				nextUnitTypeToTrain = myCombatUnitType2; // 히드라
			}
			else if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 3) {
				nextUnitTypeToTrain = myCombatUnitType3; // 럴커
			}
			else if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 4) {
				nextUnitTypeToTrain = myCombatUnitType4; // 뮤탈
			}
			else if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 5) {
				nextUnitTypeToTrain = myCombatUnitType5; // 울트라
			}
			else if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 6) {
				nextUnitTypeToTrain = myCombatUnitType6; // 가디언
			}
		
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return nextUnitTypeToTrain;	
	}
	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

	/// 과거 전체 게임 기록을 로딩합니다
	void loadGameRecordList() {
	
		// 과거의 게임에서 bwapi-data\write 폴더에 기록했던 파일은 대회 서버가 bwapi-data\read 폴더로 옮겨놓습니다
		// 따라서, 파일 로딩은 bwapi-data\read 폴더로부터 하시면 됩니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\read\\NoNameBot_GameRecord.dat";
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(gameRecordFileName));

			System.out.println("loadGameRecord from file: " + gameRecordFileName);

			String currentLine;
			StringTokenizer st;  
			GameRecord tempGameRecord;
			while ((currentLine = br.readLine()) != null) {
				
				st = new StringTokenizer(currentLine, " ");
				tempGameRecord = new GameRecord();
				if (st.hasMoreTokens()) { tempGameRecord.mapName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myWinCount = Integer.parseInt(st.nextToken());}
				if (st.hasMoreTokens()) { tempGameRecord.myLoseCount = Integer.parseInt(st.nextToken());}
				if (st.hasMoreTokens()) { tempGameRecord.enemyName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.enemyRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.enemyRealRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.gameFrameCount = Integer.parseInt(st.nextToken());}
			
				gameRecordList.add(tempGameRecord);
			}
		} catch (FileNotFoundException e) {
			System.out.println("loadGameRecord failed. Could not open file :" + gameRecordFileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
	}

	/// 과거 전체 게임 기록 + 이번 게임 기록을 저장합니다
	void saveGameRecordList(boolean isWinner) {

		// 이번 게임의 파일 저장은 bwapi-data\write 폴더에 하시면 됩니다.
		// bwapi-data\write 폴더에 저장된 파일은 대회 서버가 다음 경기 때 bwapi-data\read 폴더로 옮겨놓습니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\write\\NoNameBot_GameRecord.dat";

		System.out.println("saveGameRecord to file: " + gameRecordFileName);

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		/// 이번 게임에 대한 기록
		GameRecord thisGameRecord = new GameRecord();
		thisGameRecord.mapName = mapName;
		thisGameRecord.myName = myName;
		thisGameRecord.myRace = MyBotModule.Broodwar.self().getRace().toString();
		thisGameRecord.enemyName = enemyName;
		thisGameRecord.enemyRace = MyBotModule.Broodwar.enemy().getRace().toString();
		thisGameRecord.enemyRealRace = InformationManager.Instance().enemyRace.toString();
		thisGameRecord.gameFrameCount = MyBotModule.Broodwar.getFrameCount();
		if (isWinner) {
			thisGameRecord.myWinCount = 1;
			thisGameRecord.myLoseCount = 0;
		}
		else {
			thisGameRecord.myWinCount = 0;
			thisGameRecord.myLoseCount = 1;
		}
		// 이번 게임 기록을 전체 게임 기록에 추가
		gameRecordList.add(thisGameRecord);

		// 전체 게임 기록 write
		StringBuilder ss = new StringBuilder();
		for (GameRecord gameRecord : gameRecordList) {
			ss.append(gameRecord.mapName + " ");
			ss.append(gameRecord.myName + " ");
			ss.append(gameRecord.myRace + " ");
			ss.append(gameRecord.myWinCount + " ");
			ss.append(gameRecord.myLoseCount + " ");
			ss.append(gameRecord.enemyName + " ");
			ss.append(gameRecord.enemyRace + " ");
			ss.append(gameRecord.enemyRealRace + " ");
			ss.append(gameRecord.gameFrameCount + "\n");
		}
		
		//Common.overwriteToFile(gameRecordFileName, ss.toString());
	}

	/// 이번 게임 중간에 상시적으로 로그를 저장합니다
	void saveGameLog() {
		
		// 100 프레임 (5초) 마다 1번씩 로그를 기록합니다
		// 참가팀 당 용량 제한이 있고, 타임아웃도 있기 때문에 자주 하지 않는 것이 좋습니다
		// 로그는 봇 개발 시 디버깅 용도로 사용하시는 것이 좋습니다
		if (MyBotModule.Broodwar.getFrameCount() % 100 != 0) {
			return;
		}

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameLogFileName = "bwapi-data\\write\\NoNameBot_LastGameLog.dat";

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		StringBuilder ss = new StringBuilder();
		ss.append(mapName + " ");
		ss.append(myName + " ");
		ss.append(MyBotModule.Broodwar.self().getRace().toString() + " ");
		ss.append(enemyName + " ");
		ss.append(InformationManager.Instance().enemyRace.toString() + " ");
		ss.append(MyBotModule.Broodwar.getFrameCount() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyUsed() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyTotal() + "\n");

		Common.appendTextToFile(gameLogFileName, ss.toString());
	}

	TilePosition lurkerDefenceBuildingPos1;
	TilePosition lurkerDefenceBuildingPos2;
	TilePosition lurkerDefenceBuildingPos3;
	TilePosition lurkerDefenceBuildingPos4;
	
	public void getLurkerDefencePosition(){
		
		if(enemyRace != Race.Terran){
			return;
		}
		
		
		//System.out.println("MyBotModule.Broodwar.self().getStartLocation().getX() : " + MyBotModule.Broodwar.self().getStartLocation().getX());
		//System.out.println("MyBotModule.Broodwar.self().getStartLocation().getY() : " + MyBotModule.Broodwar.self().getStartLocation().getY());
		
//		Config.BuildingSpacing = 0;
//		Config.BuildingResourceDepotSpacing = 0;
		
		if(MyBotModule.Broodwar.mapFileName().equals("(4)Spirit.scx") || MyBotModule.Broodwar.mapFileName().equals("Fighting Spirit 1.3.scx")){
			if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
			   MyBotModule.Broodwar.self().getStartLocation().getY() == 7){
				//1시
				lurkerDefenceBuildingPos1 = new TilePosition(74, 30);			
				lurkerDefenceBuildingPos2 = new TilePosition(87, 35);
				lurkerDefenceBuildingPos3 = new TilePosition(58, 30);
				lurkerDefenceBuildingPos4 = new TilePosition(75, 45);
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 116){
				//7시
				lurkerDefenceBuildingPos1 = new TilePosition(40, 90);			
				lurkerDefenceBuildingPos2 = new TilePosition(53, 97);
				lurkerDefenceBuildingPos3 = new TilePosition(45, 79);
				lurkerDefenceBuildingPos4 = new TilePosition(64, 88);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 6){
				//11시
				lurkerDefenceBuildingPos1 = new TilePosition(33, 48);			
				lurkerDefenceBuildingPos2 = new TilePosition(35, 63);
				lurkerDefenceBuildingPos3 = new TilePosition(46, 40);
				lurkerDefenceBuildingPos4 = new TilePosition(61, 35);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 117){
				//5시
				lurkerDefenceBuildingPos1 = new TilePosition(91, 79);			
				lurkerDefenceBuildingPos2 = new TilePosition(80, 86);
				lurkerDefenceBuildingPos3 = new TilePosition(91, 64);
				lurkerDefenceBuildingPos4 = new TilePosition(64, 85);
			}
		}
		
		else if(MyBotModule.Broodwar.mapFileName().equals("(4)CircuitBreaker.scx")){
			if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 9){
				//1시
				lurkerDefenceBuildingPos1 = new TilePosition(82, 41);			
				lurkerDefenceBuildingPos2 = new TilePosition(94, 47);
				lurkerDefenceBuildingPos3 = new TilePosition(65, 47);
				lurkerDefenceBuildingPos4 = new TilePosition(85, 70);
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 118){
				//7시
				lurkerDefenceBuildingPos1 = new TilePosition(35, 81);			
				lurkerDefenceBuildingPos2 = new TilePosition(42, 86);
				lurkerDefenceBuildingPos3 = new TilePosition(35, 63);
				lurkerDefenceBuildingPos4 = new TilePosition(58, 80);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 9){
				//11시
				lurkerDefenceBuildingPos1 = new TilePosition(36, 50);			
				lurkerDefenceBuildingPos2 = new TilePosition(48, 44);
				lurkerDefenceBuildingPos3 = new TilePosition(38, 57);
				lurkerDefenceBuildingPos4 = new TilePosition(52, 47);
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 118){
				//5시
				lurkerDefenceBuildingPos1 = new TilePosition(88, 83);			
				lurkerDefenceBuildingPos2 = new TilePosition(73, 79);
				lurkerDefenceBuildingPos3 = new TilePosition(75, 93);
				lurkerDefenceBuildingPos4 = new TilePosition(88, 58);
			}
		}
		
//		System.out.println("lurkerDefenceBuildingPos1.toPosition() : " + lurkerDefenceBuildingPos1.toPosition());
//		System.out.println("lurkerDefenceBuildingPos2.toPosition() : " + lurkerDefenceBuildingPos2.toPosition());
//		System.out.println("lurkerDefenceBuildingPos3.toPosition() : " + lurkerDefenceBuildingPos3.toPosition());
//		System.out.println("lurkerDefenceBuildingPos4.toPosition() : " + lurkerDefenceBuildingPos4.toPosition());
//		System.out.println();
		
		MyBotModule.Broodwar.drawTextScreen(lurkerDefenceBuildingPos1.toPosition(), white + "1");
		MyBotModule.Broodwar.drawTextScreen(lurkerDefenceBuildingPos2.toPosition(), white + "2");
		MyBotModule.Broodwar.drawTextScreen(lurkerDefenceBuildingPos3.toPosition(), white + "3");
		MyBotModule.Broodwar.drawTextScreen(lurkerDefenceBuildingPos4.toPosition(), white + "4");
		
		MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos1.toPosition(), 20, Color.Black, false);
		MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos2.toPosition(), 20, Color.Black, false);
		MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos3.toPosition(), 20, Color.Black, false);
		MyBotModule.Broodwar.drawCircleMap(lurkerDefenceBuildingPos4.toPosition(), 20, Color.Black, false);
	}
	
	void setUnitNumeres(){
		
		if(enemyRace != Race.None && enemyRace == Race.Protoss){
			
			// sc76.choi 각 종족별 방어, 공격에 필요한 유닛 수 설정
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstProtoss;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstProtoss;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstProtoss;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstProtoss;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstProtoss;
			necessaryNumberOfCombatUnitType4 = Config.necessaryNumberOfCombatUnitType4AgainstProtoss;
			necessaryNumberOfCombatUnitType5 = Config.necessaryNumberOfCombatUnitType5AgainstProtoss;
			necessaryNumberOfCombatUnitType6 = Config.necessaryNumberOfCombatUnitType6AgainstProtoss;
			
			// 일반 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfCombatUnitType3 = Config.maxNumberOfCombatUnitType3AgainstProtoss; // 럴커
			
			// 특수 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfSpecialUnitType1 = Config.maxNumberOfSpecialUnitType1AgainstProtoss; // 오버로드  
			maxNumberOfSpecialUnitType2 = Config.maxNumberOfSpecialUnitType2AgainstProtoss; // 디파일러
			maxNumberOfSpecialUnitType3 = Config.maxNumberOfSpecialUnitType3AgainstProtoss; // 스커지
			maxNumberOfSpecialUnitType4 = Config.maxNumberOfSpecialUnitType4AgainstProtoss; // 퀸
			
			
			// sc76.choi 아군 최대 생산 제한 유닛수
			maxNumberOfTrainUnitType1 = Config.maxNumberOfTrainUnitType1AgainstProtoss;
			maxNumberOfTrainUnitType2 = Config.maxNumberOfTrainUnitType2AgainstProtoss;
			maxNumberOfTrainUnitType3 = Config.maxNumberOfTrainUnitType3AgainstProtoss;
			maxNumberOfTrainUnitType4 = Config.maxNumberOfTrainUnitType4AgainstProtoss;
			maxNumberOfTrainUnitType5 = Config.maxNumberOfTrainUnitType5AgainstProtoss;
			
			maxNumberOfTrainSpecialUnitType1 = Config.maxNumberOfTrainSpecialUnitType1AgainstProtoss;
			maxNumberOfTrainSpecialUnitType2 = Config.maxNumberOfTrainSpecialUnitType2AgainstProtoss;
			maxNumberOfTrainSpecialUnitType3 = Config.maxNumberOfTrainSpecialUnitType3AgainstProtoss;
			maxNumberOfTrainSpecialUnitType4 = Config.maxNumberOfTrainSpecialUnitType4AgainstProtoss; 
			
			// 방어 건물 종류 및 건설 갯수 설정
			necessaryNumberOfDefenseBuilding1 = Config.necessaryNumberOfDefenseBuilding1AgainstProtoss;
			necessaryNumberOfDefenseBuilding2 = Config.necessaryNumberOfDefenseBuilding2AgainstProtoss;
			 
		}else if(enemyRace != Race.None && enemyRace == Race.Zerg){
			
			// sc76.choi 각 종족별 방어, 공격에 필요한 유닛 수 설정
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstZerg;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstZerg;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstZerg;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstZerg;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstZerg;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstZerg;
			necessaryNumberOfCombatUnitType4 = Config.necessaryNumberOfCombatUnitType4AgainstZerg;
			necessaryNumberOfCombatUnitType5 = Config.necessaryNumberOfCombatUnitType5AgainstZerg;
			necessaryNumberOfCombatUnitType6 = Config.necessaryNumberOfCombatUnitType6AgainstZerg;
			
			// 일반 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfCombatUnitType3 = Config.maxNumberOfCombatUnitType3AgainstZerg; // 럴커
			
			// 특수 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfSpecialUnitType1 = Config.maxNumberOfSpecialUnitType1AgainstZerg; // 오버로드  
			maxNumberOfSpecialUnitType2 = Config.maxNumberOfSpecialUnitType2AgainstZerg; // 디파일러
			maxNumberOfSpecialUnitType3 = Config.maxNumberOfSpecialUnitType3AgainstZerg; // 스커지
			maxNumberOfSpecialUnitType4 = Config.maxNumberOfSpecialUnitType4AgainstZerg; // 퀸
			
			// sc76.choi 아군 최대 생산 제한 유닛수
			maxNumberOfTrainUnitType1 = Config.maxNumberOfTrainUnitType1AgainstZerg;
			maxNumberOfTrainUnitType2 = Config.maxNumberOfTrainUnitType2AgainstZerg;
			maxNumberOfTrainUnitType3 = Config.maxNumberOfTrainUnitType3AgainstZerg;
			maxNumberOfTrainUnitType4 = Config.maxNumberOfTrainUnitType4AgainstZerg;
			maxNumberOfTrainUnitType5 = Config.maxNumberOfTrainUnitType5AgainstZerg;
			
			maxNumberOfTrainSpecialUnitType1 = Config.maxNumberOfTrainSpecialUnitType1AgainstZerg;
			maxNumberOfTrainSpecialUnitType2 = Config.maxNumberOfTrainSpecialUnitType2AgainstZerg;
			maxNumberOfTrainSpecialUnitType3 = Config.maxNumberOfTrainSpecialUnitType3AgainstZerg;
			maxNumberOfTrainSpecialUnitType4 = Config.maxNumberOfTrainSpecialUnitType4AgainstZerg; 
			
			// 방어 건물 종류 및 건설 갯수 설정
			necessaryNumberOfDefenseBuilding1 = Config.necessaryNumberOfDefenseBuilding1AgainstZerg;
			necessaryNumberOfDefenseBuilding2 = Config.necessaryNumberOfDefenseBuilding2AgainstZerg;			
		}else if(enemyRace != Race.None && enemyRace == Race.Terran){
			
			// sc76.choi 각 종족별 방어, 공격에 필요한 유닛 수 설정
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstTerran;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstTerran;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstTerran;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstTerran;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstTerran;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstTerran;
			necessaryNumberOfCombatUnitType4 = Config.necessaryNumberOfCombatUnitType4AgainstTerran;
			necessaryNumberOfCombatUnitType5 = Config.necessaryNumberOfCombatUnitType5AgainstTerran;
			necessaryNumberOfCombatUnitType6 = Config.necessaryNumberOfCombatUnitType6AgainstTerran;
			
			// 일반 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfCombatUnitType3 = Config.maxNumberOfCombatUnitType3AgainstTerran; // 럴커
			
			// 특수 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfSpecialUnitType1 = Config.maxNumberOfSpecialUnitType1AgainstTerran; // 오버로드  
			maxNumberOfSpecialUnitType2 = Config.maxNumberOfSpecialUnitType2AgainstTerran; // 디파일러
			maxNumberOfSpecialUnitType3 = Config.maxNumberOfSpecialUnitType3AgainstTerran; // 스커지
			maxNumberOfSpecialUnitType4 = Config.maxNumberOfSpecialUnitType4AgainstTerran; // 퀸
			
			// sc76.choi 아군 최대 생산 제한 유닛수
			maxNumberOfTrainUnitType1 = Config.maxNumberOfTrainUnitType1AgainstTerran;
			maxNumberOfTrainUnitType2 = Config.maxNumberOfTrainUnitType2AgainstTerran;
			maxNumberOfTrainUnitType3 = Config.maxNumberOfTrainUnitType3AgainstTerran;
			maxNumberOfTrainUnitType4 = Config.maxNumberOfTrainUnitType4AgainstTerran;
			maxNumberOfTrainUnitType5 = Config.maxNumberOfTrainUnitType5AgainstTerran;
			
			maxNumberOfTrainSpecialUnitType1 = Config.maxNumberOfTrainSpecialUnitType1AgainstTerran;
			maxNumberOfTrainSpecialUnitType2 = Config.maxNumberOfTrainSpecialUnitType2AgainstTerran;
			maxNumberOfTrainSpecialUnitType3 = Config.maxNumberOfTrainSpecialUnitType3AgainstTerran;
			maxNumberOfTrainSpecialUnitType4 = Config.maxNumberOfTrainSpecialUnitType4AgainstTerran; 
			
			// 방어 건물 종류 및 건설 갯수 설정
			necessaryNumberOfDefenseBuilding1 = Config.necessaryNumberOfDefenseBuilding1AgainstTerran;
			necessaryNumberOfDefenseBuilding2 = Config.necessaryNumberOfDefenseBuilding2AgainstTerran;
		}else{
			
			// sc76.choi 각 종족별 방어, 공격에 필요한 유닛 수 설정
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstProtoss;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstProtoss;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstProtoss;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstProtoss;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstProtoss;
			necessaryNumberOfCombatUnitType4 = Config.necessaryNumberOfCombatUnitType4AgainstProtoss;
			necessaryNumberOfCombatUnitType5 = Config.necessaryNumberOfCombatUnitType5AgainstProtoss;
			
			// 일반 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfCombatUnitType3 = Config.maxNumberOfCombatUnitType3AgainstProtoss; // 럴커
			
			// 특수 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfSpecialUnitType1 = Config.maxNumberOfSpecialUnitType1AgainstProtoss; // 오버로드  
			maxNumberOfSpecialUnitType2 = Config.maxNumberOfSpecialUnitType2AgainstProtoss; // 디파일러
			maxNumberOfSpecialUnitType3 = Config.maxNumberOfSpecialUnitType3AgainstProtoss; // 스커지
			maxNumberOfSpecialUnitType4 = Config.maxNumberOfSpecialUnitType4AgainstProtoss; // 퀸
			
			
			// sc76.choi 아군 최대 생산 제한 유닛수
			maxNumberOfTrainUnitType1 = Config.maxNumberOfTrainUnitType1AgainstProtoss;
			maxNumberOfTrainUnitType2 = Config.maxNumberOfTrainUnitType2AgainstProtoss;
			maxNumberOfTrainUnitType3 = Config.maxNumberOfTrainUnitType3AgainstProtoss;
			maxNumberOfTrainUnitType4 = Config.maxNumberOfTrainUnitType4AgainstProtoss;
			maxNumberOfTrainUnitType5 = Config.maxNumberOfTrainUnitType5AgainstProtoss;
			
			maxNumberOfTrainSpecialUnitType1 = Config.maxNumberOfTrainSpecialUnitType1AgainstProtoss;
			maxNumberOfTrainSpecialUnitType2 = Config.maxNumberOfTrainSpecialUnitType2AgainstProtoss;
			maxNumberOfTrainSpecialUnitType3 = Config.maxNumberOfTrainSpecialUnitType3AgainstProtoss;
			maxNumberOfTrainSpecialUnitType4 = Config.maxNumberOfTrainSpecialUnitType4AgainstProtoss; 
			
			// 방어 건물 종류 및 건설 갯수 설정
			necessaryNumberOfDefenseBuilding1 = Config.necessaryNumberOfDefenseBuilding1AgainstProtoss;
			necessaryNumberOfDefenseBuilding2 = Config.necessaryNumberOfDefenseBuilding2AgainstProtoss;
		}

	}
	
	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	public int getCountAttack() {
		return countAttackMode;
	}

	public void setCountAttack(int countAttack) {
		this.countAttackMode = countAttack;
	}

	public int getCountDefence() {
		return countDefenceMode;
	}

	public void setCountDefence(int countDefence) {
		this.countDefenceMode = countDefence;
	}
	
	public ArrayList<Unit> getMyAllCombatUnitList() {
		return myAllCombatUnitList;
	}
	
	public Unit getClosesAttackUnitFromEnemyMainBase() {
		return closesAttackUnitFromEnemyMainBase;
	}
	
	public BuildOrderItem.SeedPositionStrategy getSeedPositionStrategyOfMyInitialBuildingType() {
		return seedPositionStrategyOfMyInitialBuildingType;
	}

	public BuildOrderItem.SeedPositionStrategy getSeedPositionStrategyOfMyDefenseBuildingType() {
		return seedPositionStrategyOfMyDefenseBuildingType;
	}

	public BuildOrderItem.SeedPositionStrategy getSeedPositionStrategyOfMyCombatUnitTrainingBuildingType() {
		return seedPositionStrategyOfMyCombatUnitTrainingBuildingType;
	}
	
	public int[] getBuildOrderArrayOfMyCombatUnitType() {
		return buildOrderArrayOfMyCombatUnitType;
	}
	
	public int getNextTargetIndexOfBuildOrderArray() {
		return nextTargetIndexOfBuildOrderArray;
	}

}