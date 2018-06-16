import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
import bwta.Chokepoint;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private int countAttack;
	private int countDefence;
	
	// 아군
	Player myPlayer;
	Race myRace;
	
	// 적군
	Player enemyPlayer;
	Race enemyRace;
	
	// 아군 공격 유닛 첫번째, 두번째, 세번째 타입                       프로토스     테란            저그
	UnitType myCombatUnitType1;					/// 질럿         마린           저글링
	UnitType myCombatUnitType2;			  		/// 드라군       메딕           히드라리스크
	UnitType myCombatUnitType3;			  		/// 다크템플러   시즈탱크       러커
	UnitType myCombatUnitType4;					///                    뮤탈

	// 아군 특수 유닛 첫번째, 두번째 타입
	UnitType mySpecialUnitType1;			  	/// 옵저버       사이언스베쓸   오버로드
	UnitType mySpecialUnitType2;				/// 하이템플러   배틀크루저     디파일러
	UnitType mySpecialUnitType3;				///                    스커지
	UnitType mySpecialUnitType4;				///                    울트라
	UnitType mySpecialUnitType5;				///                    퀸

	// 아군 공격 유닛 생산 순서 
	int[] buildOrderArrayOfMyCombatUnitType;	/// 아군 공격 유닛 첫번째 타입, 두번째 타입 생산 순서
	int nextTargetIndexOfBuildOrderArray;	    /// buildOrderArrayMyCombatUnitType 에서 다음 생산대상 아군 공격 유닛

	// 아군의 공격유닛 숫자
	int necessaryNumberOfCombatUnitType1;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 저글링
	int necessaryNumberOfCombatUnitType2;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 히드라
	int necessaryNumberOfCombatUnitType3;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 // 럴커
	
	// 아군의 공격유닛 숫자
	int necessaryNumberOfDefenceUnitType1;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자 // 저글링 
	int necessaryNumberOfDefenceUnitType2;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자 // 히드라
	int necessaryNumberOfDefenceUnitType3;		/// 방어을 시작하기위해 필요한 최소한의 유닛 숫자	// 럴커

	// 아군의 공격유닛 숫자
	int myKilledCombatUnitCount1;				/// 첫번째 유닛 타입의 사망자 숫자 누적값 // 저글링
	int myKilledCombatUnitCount2;				/// 두번째 유닛 타입의 사망자 숫자 누적값 // 히드라
	int myKilledCombatUnitCount3;				/// 세번째 유닛 타입의 사망자 숫자 누적값 // 럴커
	int myKilledCombatUnitCount4;				/// 세번째 유닛 타입의 사망자 숫자 누적값 // 뮤탈

	// 아군의 특수유닛 숫자
	int maxNumberOfSpecialUnitType1;			/// 최대 몇개까지 생산 / 전투참가 시킬것인가 오버로드
	int maxNumberOfSpecialUnitType2;			/// 최대 몇개까지 생산 / 전투참가 시킬것인가 디파일러
	int maxNumberOfSpecialUnitType3;			/// 최대 몇개까지 생산 / 전투참가 시킬것인가 스커지
	
	int myKilledSpecialUnitCount1;				/// 첫번째 특수 유닛 타입의 사망자 숫자 누적값
	int myKilledSpecialUnitCount2;				/// 두번째 특수 유닛 타입의 사망자 숫자 누적값
	
	// 아군 공격 전체 유닛 목록	
	ArrayList<Unit> myAllCombatUnitList = new ArrayList<Unit>();      
	
	ArrayList<Unit> myCombatUnitType1List = new ArrayList<Unit>(); // 저글링      
	ArrayList<Unit> myCombatUnitType2List = new ArrayList<Unit>(); // 히드라
	ArrayList<Unit> myCombatUnitType3List = new ArrayList<Unit>(); // 럴커
	ArrayList<Unit> myCombatUnitType4List = new ArrayList<Unit>(); // 뮤탈      

	ArrayList<Unit> mySpecialUnitType1List = new ArrayList<Unit>(); // 오버로드       
	ArrayList<Unit> mySpecialUnitType2List = new ArrayList<Unit>(); // 디파일러
	ArrayList<Unit> mySpecialUnitType3List = new ArrayList<Unit>(); // 스커지
	
	// 아군 방어 건물 첫번째, 두번째 타입
	UnitType myDefenseBuildingType1;			/// 파일런 벙커 크립콜로니
	UnitType myDefenseBuildingType2;			/// 포톤  터렛  성큰콜로니

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

	enum CombatState { 
		initialMode,                        // 초반 빌드오더 타임
		defenseMode,						// 아군 진지 방어
		attackStarted,						// 아군 유닛으로 적 공격 시작
		eliminateEnemy						// 적 Eliminate 
	};
		
	CombatState combatState;				/// 전투 상황

	// sc76.choi 공격을 위한 가장 가까운 아군 타겟 선정
	Unit closesAttackUnitFromEnemyMainBase = null;

	// target으로 부터 가장 가까운 공격 유닛을 찾기 위한 변수
	ArrayList<UnitInfo> unitListByType = new ArrayList<UnitInfo>();
	
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
		KCBaseInfoManager.Instance().updateByOneTime();
		//if(Config.DEBUG){
		//	System.out.println(KCBaseInfoManager.Instance().printKCBaseList());
		//}
		
		/// 변수 초기값을 설정합니다
		setVariables();

		/// 게임 초기에 사용할 빌드오더를 세팅합니다
		setInitialBuildOrder();		
	}
	
	/// 변수 초기값을 설정합니다
	// onStart에서 한번만 수행
	void setVariables(){
		
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
		combatState = CombatState.initialMode;
		
		// 나의 유닛
		if (myRace == Race.Protoss) {
		}
		else if (myRace == Race.Terran) {
		}
		else if (myRace == Race.Zerg) {
			
			// 공격 유닛 종류 설정 
			myCombatUnitType1 = UnitType.Zerg_Zergling;
			myCombatUnitType2 = UnitType.Zerg_Hydralisk;
			myCombatUnitType3 = UnitType.Zerg_Lurker;
			myCombatUnitType4 = UnitType.Zerg_Mutalisk;

			// 공격 유닛 생산 순서 설정
			buildOrderArrayOfMyCombatUnitType = new int[]{1,1,2,2,2,3}; 	// 저글링 저글링 히드라 히드라 히드라 러커 ...
			nextTargetIndexOfBuildOrderArray = 0; 			    	// 다음 생산 순서 index

			// 특수 유닛 종류 설정 
			mySpecialUnitType1 = UnitType.Zerg_Overlord;
			mySpecialUnitType2 = UnitType.Zerg_Defiler;
			mySpecialUnitType3 = UnitType.Zerg_Scourge;

			// 특수 유닛을 최대 몇개까지 생산 / 전투참가 시킬것인가
			maxNumberOfSpecialUnitType1 = 3; // 오버로드  
			maxNumberOfSpecialUnitType2 = 2; // 디파일러
			maxNumberOfSpecialUnitType3 = 6; // 스커지

			// 방어 건물 종류 및 건설 갯수 설정
			myDefenseBuildingType1 = UnitType.Zerg_Creep_Colony;
			necessaryNumberOfDefenseBuilding1 = 2; 					
			myDefenseBuildingType2 = UnitType.Zerg_Sunken_Colony;
			necessaryNumberOfDefenseBuilding2 = 2; 					
		
			// 방어 건물 건설 위치 설정 
			seedPositionStrategyOfMyInitialBuildingType = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;	// 본진
			seedPositionStrategyOfMyDefenseBuildingType = BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation;	// 첫번째 choke point
			seedPositionStrategyOfMyCombatUnitTrainingBuildingType = BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation;	// 앞마당
		}
		
		if(enemyRace != null && enemyRace == Race.Protoss){
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstProtoss;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstProtoss;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstProtoss;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstProtoss;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstProtoss;
			
		}else if(enemyRace != null && enemyRace == Race.Zerg){
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstZerg;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstZerg;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstZerg;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstZerg;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstZerg;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstZerg;
		}else if(enemyRace != null && enemyRace == Race.Terran){
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstTerran;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstTerran;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstTerran;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstTerran;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstTerran;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstTerran;
		}else{
			necessaryNumberOfDefenceUnitType1 = Config.necessaryNumberOfDefenceUnitType1AgainstProtoss;
			necessaryNumberOfDefenceUnitType2 = Config.necessaryNumberOfDefenceUnitType2AgainstProtoss;
			necessaryNumberOfDefenceUnitType3 = Config.necessaryNumberOfDefenceUnitType3AgainstProtoss;
			
			necessaryNumberOfCombatUnitType1 = Config.necessaryNumberOfCombatUnitType1AgainstProtoss;
			necessaryNumberOfCombatUnitType2 = Config.necessaryNumberOfCombatUnitType2AgainstProtoss;
			necessaryNumberOfCombatUnitType3 = Config.necessaryNumberOfCombatUnitType3AgainstProtoss;
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

		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi start
		// 베이스 정보를 업데이트 합니다.
		updateKCBaseInfo();
		// 일꾼도 주변에 적의 공격 유닛이 있다면 공격한다. 
		commandMyWorkerToAttack();
		
		// sc76.choi end
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/// 변수 값을 업데이트 합니다
		updateVariables();
		
		// sc76.choi 공격 타겟 유닛 할당 
		updateVariablesForAttackUnit();

		/// 일꾼을 계속 추가 생산합니다
		executeWorkerTraining();

		/// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서 SupplyProvider를 추가 건설/생산합니다
		executeSupplyManagement();

		/// 방어건물 및 공격유닛 생산 건물을 건설합니다
		executeBuildingConstruction();

		/// 업그레이드 및 테크 리서치를 실행합니다
		executeUpgradeAndTechResearch();

		/// 특수 유닛을 생산할 수 있도록 테크트리에 따라 건설을 실시합니다
		executeTechTreeUpConstruction();

		/// 공격유닛을 계속 추가 생산합니다
		executeCombatUnitTraining();

		/// 전반적인 전투 로직 을 갖고 전투를 수행합니다
		executeCombat();

		/// StrategyManager 의 수행상황을 표시합니다
		drawStrategyManagerStatus();

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

		// 이번 게임의 로그를 남깁니다
		//saveGameLog();
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
		
		//printUintData();
	}
	
	/**
	 * myAllCombatUnitList 중에 target으로 부터 가장 가까운 공격 유닛을 찾는다.
	 * 공격시 APM이 급격히 올라가는 버그가 있다. 
	 * @param type
	 * @param target
	 * @return
	 */
	public Unit getClosestCanAttackUnitTypeToTarget(UnitType type, Position target){
		
		Unit closestUnitForAttack = null;

		Iterator<Integer> it = InformationManager.Instance().getUnitData(myPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		while (it.hasNext()) {
			UnitInfo ui = InformationManager.Instance().getUnitData(myPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui.getType() == type){
				// sc76.choi BBE 죽은 유닛도 쌓이는 것 같다.
				if(!commandUtil.IsValidSelfUnit(ui.getUnit())) {
		        	continue;
		        }
				unitListByType.add(ui);
			}
		}
		
		//System.out.println("unitListByType.size() : " + unitListByType.size());
		if(unitListByType.isEmpty()){
			return closestUnitForAttack;
		}
		
		// 적진에 가까운 순으로 오름차순
        Collections.sort(unitListByType,new CompareSeqAsc());

        //for (UnitInfo rtnUnitInfo : unitListByType){
       	//	closestUnitForAttack = rtnUnitInfo.getUnit();
       	//	//System.out.println(" getClosestCanAttackUnitTypeToTarget : " + j++ + " " + closestUnitForAttack.getID());
       	//	break;
        //}
        
        if(!commandUtil.IsValidSelfUnit(unitListByType.get(0).getUnit())) {
        	return closestUnitForAttack;
        }
        closestUnitForAttack = unitListByType.get(0).getUnit();
        //System.out.println("getClosestCanAttackUnitTypeToTarget : " + closestUnitForAttack.getID());
   		return closestUnitForAttack;       	
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
				System.out.println("["+ UnitAndUnitInfoMapSize + "]" + ui.getLastPosition() + ui.getUnitID() + " " + ui.getType() + " " + ui.getDistanceFromSelfMainBase() + " " + ui.getDistanceFromEnemyMainBase());
			}
		}
		System.out.println();
	}
	
	public void updateKCBaseInfo(){
		
		// 2초에 1번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 24 * 2 != 0) return;
				
		KCBaseInfoManager.Instance().update();
		//if(Config.DEBUG){
		//	System.out.println(KCBaseInfoManager.Instance().printKCBaseList());
		//}
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
			if(myPlayer.allUnitCount(UnitType.Zerg_Hatchery) >= 2
					&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) >= 2){
				combatState = CombatState.defenseMode;
			}
		}
		
		//////////////////////////////////////////////////////////////////////////
		// 공격을 시작할만한 상황이 되기 전까지는 방어를 합니다
		// ///////////////////////////////////////////////////////////////////////
		if (combatState == CombatState.defenseMode) {

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
			if (isTimeToStartDefense() ) {
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
		}
	}
	
	/// 공격 모드로 전환할 때인지 여부를 리턴합니다
	boolean isTimeToStartAttack(){

		// sc76.choi 최소방어 유닛보다 많고
		if (myCombatUnitType1List.size() >= necessaryNumberOfDefenceUnitType1 // 저글링
			|| myCombatUnitType2List.size() >= necessaryNumberOfDefenceUnitType2 // 히드라
			|| myCombatUnitType3List.size() >= necessaryNumberOfDefenceUnitType3 // 럴커
		)
		{
			//////////////////////////////////////////////////////////////////////////////
			// sc76.choi 공격 유닛수가 충족하면
			// 1. 저글링이 12마리이상, 럴커가 2마리 이상
			// 2. 히드라가 8마리이상, 럴커가 1마리 이상
			//////////////////////////////////////////////////////////////////////////////
			if ((myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2) ||
					
				(myCombatUnitType1List.size() >= necessaryNumberOfCombatUnitType1 
				&& myCombatUnitType3List.size() >= necessaryNumberOfCombatUnitType3) ||
				
				(myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2
				&& myCombatUnitType3List.size() >= necessaryNumberOfCombatUnitType3)
			) {
				
				// 에너지 100 이상 갖고있는 특수 유닛이 존재하면 
//				boolean isSpecialUnitHasEnoughEnergy = false;
//				for(Unit unit : mySpecialUnitType1List) {
//					if (unit.getEnergy() > 100) {
//						isSpecialUnitHasEnoughEnergy = true;
//						break;
//					}
//				}				
//				for(Unit unit : mySpecialUnitType2List) {
//					if (unit.getEnergy() > 100) {
//						isSpecialUnitHasEnoughEnergy = true;
//						break;
//					}
//				}				
//				if (isSpecialUnitHasEnoughEnergy) {
//					return true;
//				}
				countAttack++;
				return true;
			}
			
			if (myCombatUnitType2List.size() >= necessaryNumberOfCombatUnitType2) {
				countAttack++;
				return true;
			}
		}
		
		return false;
	}

	/// 방어 모드로 전환할 때인지 여부를 리턴합니다
	boolean isTimeToStartDefense() {
		// 공격 유닛 숫자가 10 미만으로 떨어지면 후퇴
		// myCombatUnitType1 : 저글링 
		// myCombatUnitType2 : 히드라 
		// myCombatUnitType3 : 럴커 
		if (myCombatUnitType2List.size() < necessaryNumberOfDefenceUnitType2){
			countDefence++;
			return true;
		}
		
		return false;
	}

	/// 적군을 Eliminate 시키는 모드로 전환할지 여부를 리턴합니다
	// sc76.choi 잘 판단해야 한다. 경기가 끝나지 않을 수도 있다.
	boolean isTimeToStartElimination(){

		// 적군 유닛을 많이 죽였고, 아군 서플라이가 100 을 넘었으면
		if (enemyKilledCombatUnitCount >= 15 && enemyKilledWorkerUnitCount >= 10) {

			// 적군 본진에 아군 유닛이 10 이상 도착했으면 거의 게임 끝난 것
			int myUnitCountAroundEnemyMainBaseLocation = 0;
			int enemyUnitCountAroundEnemyMainBaseLocation = 0;
			for(Unit unit : MyBotModule.Broodwar.getUnitsInRadius(enemyMainBaseLocation.getPosition(), 8 * Config.TILE_SIZE)) {
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
		
		// sc76.choi 일꾼만 20마리 죽여도 elimination한다.
		// sc76.choi 그리고, frame count가 28800 = 24 * 60 * 20 (즉 20분이 지났으면)이면 eliminate를 시작한다.
		if (enemyKilledWorkerUnitCount >= 20 && MyBotModule.Broodwar.getFrameCount() >= 20000) {
			return true;
		}
		
		// sc76.choi 일꾼만 25마리 죽여도 elimination한다.
		if (enemyKilledWorkerUnitCount >= 25){
			return true;
		}
		
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
	
	/// 아군 공격유닛 들에게 방어를 지시합니다
	void commandMyCombatUnitToDefense(){

		// 아군 방어 건물이 세워져있는 위치
		// sc76.choi setVariables에서 BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation 로 지정
		Position myDefenseBuildingPosition = null;
		switch (seedPositionStrategyOfMyDefenseBuildingType) {
			case MainBaseLocation: 
				myDefenseBuildingPosition = myMainBaseLocation.getPosition(); 
				break;
			case FirstChokePoint: 
				myDefenseBuildingPosition = myFirstChokePoint.getCenter(); 
				break;
			case FirstExpansionLocation: 
				myDefenseBuildingPosition = myFirstExpansionLocation.getPosition();
				
				// sc76.choi 방어 모드시에 만약 성큰이 지어졌다면 그쪽으로 이동한다. 방어에 약간의 우세한 전략 
				// sc76.choi 앞마당으로 부터 가장 가까운 성큰이기 때문에 좀더 미세한 판단이 필요하다.
				Unit myDefenseBuildingUnit = commandUtil.GetClosestSelfUnitTypeToTarget(UnitType.Zerg_Sunken_Colony, new Position(2000, 2000));
				if(myDefenseBuildingUnit != null){
					double d1 = myFirstExpansionLocation.getDistance(mySecondChokePoint); // 확장과 center와의 거리
					double d2 = myDefenseBuildingUnit.getDistance(mySecondChokePoint); // 방어 타워와 center화의 거리
					// sc76.choi 방어 타워가 앞마당 헤처리보다 센터로 부터 더 멀리 있으면, 그냥 앞마당으로 모인다. 
					if(d1 > d2){
						myDefenseBuildingPosition = myDefenseBuildingUnit.getPosition();
					}
				}
				break;
			case SecondChokePoint: 
				myDefenseBuildingPosition = mySecondChokePoint.getCenter(); 
				break;
			default: 
				myDefenseBuildingPosition = myMainBaseLocation.getPosition(); 
				break;
		}

		// 아군 공격유닛을 방어 건물이 세워져있는 위치로 배치시킵니다
		// 아군 공격유닛을 아군 방어 건물 뒤쪽에 배치시켰다가 적들이 방어 건물을 공격하기 시작했을 때 다함께 싸우게하면 더 좋을 것입니다
		// sc76.choi 단, 정찰 오버로드는 자기 할일이 있다. myAllCombatUnitList에 오버로드 add 되는 기준을 확인 할 것 
		for (Unit unit : myAllCombatUnitList) {

			if (!commandUtil.IsValidUnit(unit)) continue;
			
			boolean hasCommanded = false;

			// sc76.choi cooldown 시간을 이용한 침 뿌리고, 도망가기 구현
			if (unit.getType() == myCombatUnitType2) {
				hasCommanded = controlCombatUnitType2(unit);
			}
			
			if (unit.getType() == myCombatUnitType3) {
				hasCommanded = controlCombatUnitType3(unit);
			}
			if (unit.getType() == mySpecialUnitType1) {					
				hasCommanded = controlSpecialUnitType1(unit);
			}
			if (unit.getType() == mySpecialUnitType2) {					
				hasCommanded = controlSpecialUnitType2(unit);
			}
			
			// 따로 명령 내린 적이 없으면, 방어 건물 주위로 이동시킨다. 단, 오버로드는 제외
			if (hasCommanded == false) {
				
				// sc76.choi 무조건 후퇴 해야 한다. unit.isIdle() 체크 제거
				// sc76.choi 먼저 명령을 받은 유닛은 그 명령이 끝날때까지 수행하기 때문
				//if (unit.isIdle()) {
					if (unit.canAttack()) {
						commandUtil.attackMove(unit, myDefenseBuildingPosition);
					}
					else {
						commandUtil.move(unit, myDefenseBuildingPosition);
					}
				//}
			}
		}	
	}
	
	/// 아군 공격 유닛들에게 공격을 지시합니다 
	void commandMyCombatUnitToAttack(){

		Position targetPosition = null;
		
		// 최종 타겟은 적군의 Main BaseLocation
		BaseLocation targetEnemyBaseLocation = enemyMainBaseLocation;
		
		if (targetEnemyBaseLocation != null){

			///////////////////////////////////////////////////////////////////////////////////////
			// targetPosition 을 설정한다
			///////////////////////////////////////////////////////////////////////////////////////
			targetPosition = targetEnemyBaseLocation.getPosition();
			
			// 모든 아군 공격유닛들로 하여금 targetPosition 을 향해 공격하게 한다
			for (Unit unit : myAllCombatUnitList) {
				
				boolean hasCommanded = false;
				
				// sc76.choi 저글링도 히드라를 따라간다.
				if (unit.getType() == myCombatUnitType1) {
					hasCommanded = controlCombatUnitType1(unit);
				}
				
				// sc76.choi cooldown 시간을 이용한 침 뿌리고, 도망가기 구현
				if (unit.getType() == myCombatUnitType2) {
					hasCommanded = controlCombatUnitType2(unit);
				}
				
				// 럴커
				if (unit.getType() == myCombatUnitType3) {
					hasCommanded = controlCombatUnitType3(unit);					
				}
				
				// sc76.choi 따로 명령 받은 오버로드는 공격에서 제외 합니다.				
				if (unit.getType() == mySpecialUnitType1) {		
					hasCommanded = controlSpecialUnitType1(unit);
				}
				
				if (unit.getType() == mySpecialUnitType2) {					
					hasCommanded = controlSpecialUnitType2(unit);
				}
				
				// 따로 명령 내린 적이 없으면, targetPosition 을 향해 공격 이동시킵니다
				if (hasCommanded == false) {

					if (unit.isIdle()) {
						if (unit.canAttack() ) { 
							
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
	}

	/// 적군을 Eliminate 시키도록 아군 공격 유닛들에게 지시합니다
	void commandMyCombatUnitToEliminate(){
		
		if (enemyPlayer == null || enemyRace == Race.Unknown) 
		{
			return;
		}
		
		Random random = new Random();
		int mapHeight = MyBotModule.Broodwar.mapHeight();	// 128
		int mapWidth = MyBotModule.Broodwar.mapWidth();		// 128
		
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
			
			if (unit.getType() == myCombatUnitType3) { // 럴커
				hasCommanded = controlCombatUnitType3(unit);					
			}
			
			if (unit.getType() == mySpecialUnitType1) {	// 오버로드				
				hasCommanded = controlSpecialUnitType1(unit);
			}
			
			if (unit.getType() == mySpecialUnitType2) {	// 디파일러	
				hasCommanded = controlSpecialUnitType2(unit);
			}
			
			// 따로 명령 내린 적이 없으면, 적군의 남은 건물 혹은 랜덤 위치로 이동시킨다
			if (hasCommanded == false) {

				if (unit.isIdle()) {

					Position targetPosition = null;
					if (targetEnemyBuilding != null) {
						targetPosition = targetEnemyBuilding.getPosition();
					}
					else {
						targetPosition = new Position(random.nextInt(mapWidth * Config.TILE_SIZE), random.nextInt(mapHeight * Config.TILE_SIZE));
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
		// 전체 아군 유닛의 일꾼을 loop
		for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
			if(!commandUtil.IsValidSelfUnit(worker)) return; // 정상유닛 체크
			
			// 각 worker의 주변 DISTANCE_WORKER_CANATTACK을 살펴 본다.
			Iterator<Unit> iter = MyBotModule.Broodwar.getUnitsInRadius(worker.getPosition(), Config.DISTANCE_WORKER_CANATTACK).iterator();
			while(iter.hasNext()){
				Unit unit = iter.next();
				
				// 지상공격이 가능한 적군이면 CombatWorker으로 변경한다.
				if(commandUtil.IsValidEnemyGroundAttackUnit(unit)){
					
					Unit enemyUnit = unit;
					existEnemyAroundWorker = true; // 적군 카운트 증
					
					// 부실한 공격 유닛은 해제
					if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
						remainHitPoint = (double)(worker.getHitPoints()*1.0/worker.getInitialHitPoints()*1.0);
						//System.out.println("combat Unit remainHitPoint : " + worker.getHitPoints() + "[" +worker.getID()+ "]");
						if(remainHitPoint <= .7){
							System.out.println("remove Unit remainHitPoint : " + worker.getHitPoints()+ "/" + worker.getInitialHitPoints() + " " +remainHitPoint + "[" +worker.getID()+ "]");
							WorkerManager.Instance().setIdleWorker(worker);
						}
					}
					
					// 이미 공격일꾼이 있으면 (일꾼 공격 합세는 2마리만 한다.)
					if(WorkerManager.Instance().getWorkerData().getNumCombatWorkers() >= Config.COUNT_WORKERS_CANATTACK){
						break;
					}
					
					// 적군과 나와의 거리가 DISTANCE_WORKER_CANATTACK내에 있는,
					// worker(Job이 Mineral이고 체력이 온전한)를 상태를 combat으로 변경한다.
					if(worker.getDistance(enemyUnit) < Config.DISTANCE_WORKER_CANATTACK){

						// 공격 투입
						if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Minerals){
							if(!commandUtil.IsValidSelfUnit(worker)) return; // 정상유닛 체크
							
							remainHitPoint = (double)(worker.getHitPoints()*1.0/worker.getInitialHitPoints()*1.0);
							if(worker.isCarryingMinerals() || worker.isAttacking()) continue; // 미네랄 운반 일꾼은 제외
							if(remainHitPoint >= .8){
								WorkerManager.Instance().setCombatWorker(worker);
								System.out.println("add Unit remainHitPoint : " + remainHitPoint + "[" +worker.getID()+ "]");
								// 일꾼 공격 합세는 2마리만 한다.
								if(WorkerManager.Instance().getWorkerData().getNumCombatWorkers() >= Config.COUNT_WORKERS_CANATTACK) { 
									break;
								}
							}
						}
					}
				}
			} // while
		}
		
		// 적군이 없다면 idle로 변경하여, 다시 일을 할수 있게 한다.
		if(!existEnemyAroundWorker){
			for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
				if(!commandUtil.IsValidSelfUnit(worker)) return;
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
					WorkerManager.Instance().setIdleWorker(worker);
				}
			}
		}
	}
	


	/// 첫번째 일반공격 유닛 타입의 유닛에 대해 컨트롤 명령을 입력합니다(저글링)
	boolean controlCombatUnitType1(Unit unit) {
		
		boolean hasCommanded = false;
		
		if (unit.getType() == UnitType.Zerg_Zergling) {			
			if (!commandUtil.IsValidUnit(unit)) return true;
			
			Position targetPosition = null;
			if(combatState == CombatState.attackStarted){
				// sc76.choi 가장 가까운 공격 유닛(히드라)의 위치를 찾아 오버로드가 따라가게 한다. 
				// sc76.choi 36이면 서킷에서 다리를 양쪽으로 건널 수 있다.
				if(closesAttackUnitFromEnemyMainBase != null 
						&& unit.getDistance(enemyMainBaseLocation.getPosition()) > Config.TILE_SIZE*38){  
					targetPosition = closesAttackUnitFromEnemyMainBase.getPosition(); 
				}else{ 
					targetPosition = enemyMainBaseLocation.getPosition(); 
				}
				
				// 적진과 가까이에 있으면 그냥 자유롭게 싸운다.
				// TODO 단, 공격 타겟이 항상 enemyMainBaseLocation는 아니다, 수정 해야 한다. 메인 타켓 Position은 별도는 global하게 관리 되어야 한다.
				//if(unit.getDistance(enemyMainBaseLocation.getPosition()) <= Config.TILE_SIZE*30){
				//	targetPosition = enemyMainBaseLocation.getPosition();
				//}
				
				// 12시
				if(unit.getID() % 4 == 0){
					targetPosition = new Position(targetPosition.getX(), targetPosition.getY() - Config.TILE_SIZE*10);
					
					// 12시가 invalid이면 6시
					if(!targetPosition.isValid()){
						targetPosition = new Position(targetPosition.getX(), targetPosition.getY() + Config.TILE_SIZE*10);
					}
				}
				// 3시
				else if(unit.getID() % 4 == 1){
					targetPosition = new Position(targetPosition.getX() + Config.TILE_SIZE*10, targetPosition.getY());
					
					// 3시가 invalid이면 9시
					if(!targetPosition.isValid()){
						targetPosition = new Position(targetPosition.getX() - Config.TILE_SIZE*10, targetPosition.getY());
					}					
				}
				// 6시
				else if(unit.getID() % 4 == 2){
					targetPosition = new Position(targetPosition.getX(), targetPosition.getY() + Config.TILE_SIZE*10);
					
					// 6시가 invalid이면 12시
					if(!targetPosition.isValid()){
						targetPosition = new Position(targetPosition.getX(), targetPosition.getY() - Config.TILE_SIZE*10);
					}
				}
				// 9시
				else{
					targetPosition = new Position(targetPosition.getX() - Config.TILE_SIZE*10, targetPosition.getY());
					// 9시가 invalid이면 3시
					if(!targetPosition.isValid()){
						targetPosition = new Position(targetPosition.getX() + Config.TILE_SIZE*10, targetPosition.getY());
					}										
				}
				
				// 4방향의 포지션 중, valid지역이 아니면 그냥 적진을 targetPosition으로 지정한다.
				if(!targetPosition.isValid()){
					targetPosition = enemyMainBaseLocation.getPosition();
				}
				
				commandUtil.attackMove(unit, targetPosition);
				
				hasCommanded = true;
			}else if(combatState == CombatState.defenseMode || combatState == CombatState.initialMode){
				targetPosition = myMainBaseLocation.getPosition();
				commandUtil.patrol(unit, myFirstExpansionLocation.getPosition());
				hasCommanded = true;
			}
		}
		
		return hasCommanded;
	}
	
	boolean controlCombatUnitType2(Unit unit) { 
		boolean hasCommanded = false; Position targetPosition = null;
		if (unit.getType() == UnitType.Zerg_Hydralisk) {
			
			if (combatState == CombatState.defenseMode) {
				
				// 하기의 로직이 별 이득이 없음
				
//				// sc76.choi Config.TILE_SIZE*3 거리 만큼 적이 있으면 공격을 하지 않는다. 
//				// 건물만 있으면, 그냥 계속 공격하도록 한다.
//				int checkAroundCanAttakUnit = 0;
//				for(Unit who : unit.getUnitsInRadius(Config.TILE_SIZE*4)){
//					if(who.getPlayer() == enemyPlayer){
//						if(who.getType().canAttack()){
//							System.out.println("who ID ["+who.getID()+"] : " + who.getType() + ", " + who.getType().canAttack());
//							checkAroundCanAttakUnit++;
//						}
//					}
//				}
//				
//				// 주변에 공격 대상이 있으면 방어모드에서 공격
//				if(checkAroundCanAttakUnit > 0){
//					
//					System.out.println("CombatState.defenseMode checkAroundCanAttakUnit["+unit.getID()+"] : " + checkAroundCanAttakUnit);
//					
//					targetPosition = myFirstExpansionLocation.getPosition();
//					
//					// sc76.choi cooldown 시간을 이용한 침 뿌리고, 도망가기
//					if(unit.getGroundWeaponCooldown() == 0){
//						commandUtil.attackMove(unit, targetPosition);
//					}
//					// 도망간다.
//					else{
//						targetPosition = myFirstExpansionLocation.getPosition();
//						
//						// 후퇴시 12, 3, 6, 9시 방향으로 랜덤하게 도망
//						// 12시
//						Position calPosition = myFirstExpansionLocation.getPosition();
//						
//						if(unit.getID() % 4 == 0){
//							calPosition = new Position(unit.getPosition().getX(), unit.getPosition().getY() - Config.TILE_SIZE*4);
//							System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 12");
//						}
//						// 3시
//						else if(unit.getID() % 4 == 1){
//							calPosition = new Position(unit.getPosition().getX() + Config.TILE_SIZE*4, unit.getPosition().getY());
//							System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 3");
//						}
//						// 6시ㅣ
//						else if(unit.getID() % 4 == 2){
//							calPosition = new Position(unit.getPosition().getX(), unit.getPosition().getY()  + Config.TILE_SIZE*4);
//							System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 6");
//						}
//						// 9시
//						else{
//							calPosition = new Position(unit.getPosition().getX()  - Config.TILE_SIZE*4, unit.getPosition().getY());
//							System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 9");
//						}
//						
//						double d1 = unit.getDistance(myFirstExpansionLocation.getPosition()); // 유닛의 포지션에서 본진의 거리
//						double d2 = calPosition.getDistance(myMainBaseLocation.getPosition()); // 계산된 포지션에서 본의 거리
//						
//						// 가야할 곳이 전진과 더 가깝다면, 그냥 본진 방향
//						// TODO 수정 대피 할곳
//						if(d1 < d2){
//							targetPosition = myMainBaseLocation.getPosition();
//						}else{
//							targetPosition = calPosition;;
//						}
//						
//						commandUtil.move(unit, targetPosition);
//					}
//				}
//				// 주변에 없으면 가만히 있는다.
//				else{
//				
//					
//				}
				
				hasCommanded = false;
			}else{
				// sc76.choi cooldown 시간을 이용한 침 뿌리고, 도망가기
				if(unit.getGroundWeaponCooldown() == 0 
					&& unit.getHitPoints() > 10
				){
					targetPosition = enemyMainBaseLocation.getPosition();
	
					// sc76.choi Config.TILE_SIZE*3 거리 만큼 적이 있으면 공격을 하지 않는다. 도망갈때의 포지션 만큼 이동을 계속 한다.
					for(Unit who : unit.getUnitsInRadius(Config.TILE_SIZE*4)){
						if(who.getPlayer() == enemyPlayer
								&& who.getType().isBuilding() == false
								&& who.canAttack()){
							return true;
						}
					}
	
					commandUtil.attackMove(unit, targetPosition);
				}
				// 도망 갈때는
				else{
					
					// sc76.choi Config.TILE_SIZE*3 거리 만큼 적이 있으면 공격을 하지 않는다. 
					// 건물만 있으면, 그냥 계속 공격하도록 한다.
					int checkAroundCanAttakUnit = 0;
					for(Unit who : unit.getUnitsInRadius(Config.TILE_SIZE*5)){
						if(who.getPlayer() == enemyPlayer){
							//System.out.println("who ID ["+who.getID()+"] : " + who.getPlayer() + ", " + who.getType().canAttack());
							if(who.getType().canAttack()){
								checkAroundCanAttakUnit++;
							}
						}
					}
					//System.out.println("checkAroundCanAttakUnit["+unit.getID()+"] : " + checkAroundCanAttakUnit);
					
					// 주변에 빌딩밖에 없으면 전진 공격만 한다.
					if(checkAroundCanAttakUnit == 0){
						targetPosition = enemyMainBaseLocation.getPosition();
						commandUtil.attackMove(unit, targetPosition);
						hasCommanded = true;
						return hasCommanded;
					}
					// 주변에 공격 대상이 있으면 그대로 뺀다.
					else{
					
						targetPosition = myFirstExpansionLocation.getPosition();
						
						// 적진에서 1300보다 안쪽에 있으면, 개활지가 아니다. 그래서 그냥 본진으로 뺀다.
						if(unit.getDistance(enemyMainBaseLocation.getPoint()) < 1300){
							commandUtil.move(unit, targetPosition);
						}else{
						
							// 후퇴시 12, 3, 6, 9시 방향으로 랜덤하게 도망
							// 12시
							Position calPosition = myFirstExpansionLocation.getPosition();
							
							if(unit.getID() % 4 == 0){
								calPosition = new Position(unit.getPosition().getX(), unit.getPosition().getY() - Config.TILE_SIZE*4);
								//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 12");
							}
							// 3시
							else if(unit.getID() % 4 == 1){
								calPosition = new Position(unit.getPosition().getX() + Config.TILE_SIZE*4, unit.getPosition().getY());
								//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 3");
							}
							// 6시ㅣ
							else if(unit.getID() % 4 == 2){
								calPosition = new Position(unit.getPosition().getX(), unit.getPosition().getY()  + Config.TILE_SIZE*4);
								//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 6");
							}
							// 9시
							else{
								calPosition = new Position(unit.getPosition().getX()  - Config.TILE_SIZE*4, unit.getPosition().getY());
								//System.out.println("controlCombatUnitType2 ["+unit.getID()+"] go -->> 9");
							}
							
							double d1 = unit.getDistance(enemyMainBaseLocation.getPosition()); // 유닛의 포지션에서 적진의 거리
							double d2 = calPosition.getDistance(enemyMainBaseLocation.getPosition()); // 계산된 포지션에서 적진의 거리
							
							// 가야할 곳이 전진과 더 가깝다면, 그냥 본진 방향
							if(d1 > d2){
								targetPosition = myFirstExpansionLocation.getPosition();
							}else{
								targetPosition = calPosition;;
							}
							
							commandUtil.move(unit, targetPosition);
						}
					}
				}
				hasCommanded = true;
			}
		}
		return hasCommanded;
	}
	
	/// 러커 유닛에 대해 컨트롤 명령을 내립니다
	boolean controlCombatUnitType3(Unit unit){
		
		boolean hasCommanded = false;
		
		// defenseMode 일 경우
		if (combatState == CombatState.defenseMode) {
			
			// 아군 방어 건물이 세워져있는 위치 주위에 버로우시켜놓는다
			Position myDefenseBuildingPosition = null;
			switch (seedPositionStrategyOfMyDefenseBuildingType) {
				case MainBaseLocation: myDefenseBuildingPosition = myMainBaseLocation.getPosition(); break;
				case FirstChokePoint: myDefenseBuildingPosition = myFirstChokePoint.getCenter(); break;
				case FirstExpansionLocation: myDefenseBuildingPosition = myFirstExpansionLocation.getPosition(); break;
				case SecondChokePoint: myDefenseBuildingPosition = mySecondChokePoint.getCenter(); break;
				default: myDefenseBuildingPosition = myMainBaseLocation.getPosition(); break;
			}

			// sc76.choi, defenseMode이지만, 적진에 깊이 박혀 있으면, 그대로 둔다.
			if (myDefenseBuildingPosition != null) {
				if (unit.isBurrowed() == false) {			
					if (unit.getDistance(myDefenseBuildingPosition) < 5 * Config.TILE_SIZE) {
						unit.burrow();
						hasCommanded = true;
					}
				}
			}
		}
		// defenseMode 가 아닐 경우
		else {
			
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
			
			if (unit.isBurrowed() == false) {				
				
				if (nearEnemyUnitPosition != null) {
					unit.burrow();
					hasCommanded = true;
				}
			}
			else {
				if (nearEnemyUnitPosition == null) {
					// sc76.choi Config.TILE_SIZE*5 거리 만큼 적이 있으면 unburrow를 하면 안된다.
					for(Unit who : unit.getUnitsInRadius(Config.TILE_SIZE*5)){
						if(who.getPlayer() == enemyPlayer && who.canAttack()){
							unit.burrow();
							hasCommanded = true;
						}else{
							unit.unburrow();
							hasCommanded = true;
						}
					}
					
				}
			}			
		}

		return hasCommanded;
	}
	
	/// 첫번째 특수 유닛 타입의 유닛에 대해 컨트롤 명령을 입력합니다
	boolean controlSpecialUnitType1(Unit unit) {

		boolean hasCommanded = false;		
		
		// sc76.choi 기본적으로 myAllCombatUnitList에 담긴 오버로드만 대상이 된다. (즉 Idle인 오버로드)
		// sc76.choi 오버로드는 hasCommanded는 항상 true
		// sc76.choi TODO 공격시에 가장 적진과 가까운 히드라를 따라 다니게 된다. 개선 필요
		if (unit.getType() == UnitType.Zerg_Overlord) {			
			if (!commandUtil.IsValidUnit(unit)) return true;
			
			Position targetPosition = null;
			if(combatState == CombatState.attackStarted){
				// sc76.choi 가장 가까운 공격 유닛의 위치를 찾아 오버로드가 따라가게 한다.	
				if(closesAttackUnitFromEnemyMainBase != null){
					targetPosition = closesAttackUnitFromEnemyMainBase.getPosition();
				}else{
					targetPosition = enemyMainBaseLocation.getPosition();
				}
				commandUtil.move(unit, targetPosition);
				OverloadManager.Instance().getOverloadData().setOverloadJob(unit, OverloadData.OverloadJob.AttackMove, (Unit)null);
			}else if(combatState == CombatState.defenseMode || combatState == CombatState.initialMode){
				targetPosition = myMainBaseLocation.getPosition();
				//commandUtil.patrol(unit, myFirstExpansionLocation.getPosition());
				OverloadManager.Instance().getOverloadData().setOverloadJob(unit, OverloadData.OverloadJob.Idle, (Unit)null);				
			}else{
				
			}
			
			hasCommanded = true;
		}
		
		//System.out.println("controlSpecialUnitType1(Unit) hasCommanded : " + hasCommanded);
		return hasCommanded;
	}
	
	/// 두번째 특수 유닛 타입의 유닛에 대해 컨트롤 명령을 내립니다
	boolean controlSpecialUnitType2(Unit unit) {

		///////////////////////////////////////////////////////////////////
		///////////////////////// 아래의 코드를 수정해보세요 ///////////////////////
		//
		// TODO 2. 아군 하이템플러/배틀크루저/디파일러가 특수 기술을 사용하게 하는 로직       (예상 개발시간 20분)
		//
		// 목표 : 두번째 특수유닛 타입은 특수 기술을 갖고있는 하이템플러/배틀크루저/디파일러 입니다. 
		//
		//      현재는 특수기술 사용 대상을 정하는 로직이 구현 안되어있습니다.
		//
		//      적군 유닛들의 목록 MyBotModule.Broodwar.enemy().getUnits() 을 사용하여
		//      특수 기술 사용 대상을 적절히 정하도록 해보세요
		// 
		//      return false = 유닛에게 따로 컨트롤 명령을 입력하지 않음  -> 다른 공격유닛과 동일하게 이동하도록 합니다
		//      return true = 유닛에게 따로 컨트롤 명령을 입력했음
		// 
		// 추가 : 테란 종족 첫번째 특수유닛 타입 사이언스베슬에 대해서도 특수 기술을 사용하게 하려면
		//		controlSpecialUnitType1 함수를 수정하시면 됩니다
		// 
		///////////////////////////////////////////////////////////////////
		
		boolean hasCommanded = false;
		
		// 프로토스 종족 하이템플러의 경우 
		if (unit.getType() == UnitType.Protoss_High_Templar) {
			
			if (unit.getEnergy() >= TechType.Psionic_Storm.energyCost() && myPlayer.hasResearched(TechType.Psionic_Storm)) {
				
				Position targetPosition = null;
				
				// targetPosition 을 적절히 정해보세요
				
				if (targetPosition != null) {
					unit.useTech(TechType.Psionic_Storm, targetPosition);
					hasCommanded = true;
				}
			}			
		}
		else if (unit.getType() == UnitType.Terran_Battlecruiser) {
			
			if (unit.getEnergy() >= TechType.Yamato_Gun.energyCost() && myPlayer.hasResearched(TechType.Yamato_Gun)) {
				
				Unit targetEnemyUnit = null;
				
				// targetEnemyUnit 을 적절히 정해보세요
				
				if (targetEnemyUnit != null) {
					unit.useTech(TechType.Yamato_Gun, targetEnemyUnit);
					hasCommanded = true;
				}
			}	
		}
		else if (unit.getType() == UnitType.Zerg_Defiler) {

			if (unit.getEnergy() < 200 && myPlayer.hasResearched(TechType.Consume)) {
				
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

			if (unit.getEnergy() >= TechType.Plague.energyCost() && myPlayer.hasResearched(TechType.Plague)) {

				Unit targetEnemyUnit = null;
				
				// targetEnemyUnit 을 적절히 정해보세요
				
				if (targetEnemyUnit != null) {
					unit.useTech(TechType.Plague, targetEnemyUnit);
					hasCommanded = true;
				}				
			}
			else if (unit.getEnergy() >= TechType.Dark_Swarm.energyCost()) {
				
				Position targetPosition = null;

				// targetPosition 을 적절히 정해보세요
				
				if (targetPosition != null) {
					unit.useTech(TechType.Dark_Swarm, targetPosition);
					hasCommanded = true;
				}
			}
		}

		return hasCommanded;
	}
	
	
	/// StrategyManager 의 수행상황을 표시합니다
	private void drawStrategyManagerStatus() {
		
		int y = 200;
		int t = 240;
		// 아군 공격유닛 숫자 및 적군 공격유닛 숫자
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
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + mySpecialUnitType1.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + mySpecialUnitType1List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledSpecialUnitCount1);
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My " + mySpecialUnitType2.toString().replaceAll("Zerg_", ""));
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + mySpecialUnitType2List.size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + myKilledSpecialUnitCount2);

		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "My Worker");
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + WorkerManager.Instance().getWorkerData().getWorkers().size());
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + selfKilledWorkerUnitCount);

		y += 20;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "Enemy CombatUnit");
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + numberOfCompletedEnemyCombatUnit);
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + enemyKilledCombatUnitCount);
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(200+t, y, "Enemy WorkerUnit");
		MyBotModule.Broodwar.drawTextScreen(300+t, y, "alive " + numberOfCompletedEnemyWorkerUnit);
		MyBotModule.Broodwar.drawTextScreen(350+t, y, "killed " + enemyKilledWorkerUnitCount);
		y += 20;

		// setInitialBuildOrder 에서 입력한 빌드오더가 다 끝나서 빌드오더큐가 empty 되었는지 여부
		MyBotModule.Broodwar.drawTextScreen(200, y, "isInitialBuildOrderFinished " + isInitialBuildOrderFinished);
		y += 10;
		// 전투 상황
		MyBotModule.Broodwar.drawTextScreen(200, y, "combatState " + combatState.toString());
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
	void updateVariables(){

		enemyRace = InformationManager.Instance().enemyRace;
		
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}

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
		myCombatUnitType1List.clear();
		myCombatUnitType2List.clear();
		myCombatUnitType3List.clear();
		myCombatUnitType4List.clear(); // 뮤탈
		mySpecialUnitType1List.clear(); // 오버로드
		mySpecialUnitType2List.clear(); // 디파일러
		mySpecialUnitType3List.clear(); // 스커지
		
		// target으로 부터 가장 가까운 공격 유닛을 찾기 위한 변수
		unitListByType.clear();
		
		for(Unit unit : myPlayer.getUnits()) {		
			
			if (unit == null || unit.exists() == false || unit.getHitPoints() <= 0) continue;
			
			if (unit.getType() == myCombatUnitType1) { 
				myCombatUnitType1List.add(unit);
				myAllCombatUnitList.add(unit);
			}
			else if (unit.getType() == myCombatUnitType2) { 
				myCombatUnitType2List.add(unit); 
				myAllCombatUnitList.add(unit);
			}
			else if (unit.getType() == myCombatUnitType3) { 
				myCombatUnitType3List.add(unit); 
				myAllCombatUnitList.add(unit);
			}else if (unit.getType() == myCombatUnitType4) { 
				myCombatUnitType4List.add(unit); 
				myAllCombatUnitList.add(unit);
			}
			else if (unit.getType() == mySpecialUnitType1) {
				// maxNumberOfSpecialUnitType1 숫자까지만 특수유닛 부대에 포함시킨다 (저그 종족의 경우 오버로드가 전부 전투참여했다가 위험해질 수 있으므로)
				// sc76.choi defence 모드 시에 좀 애매 하다. 본진 으로 귀한하지 않는 유닛이 생길 수 있다.
				if (mySpecialUnitType1List.size() < maxNumberOfSpecialUnitType1) {
					// sc76.choi Idel인 오버로드만 추가 한다.
					if(OverloadManager.Instance().getOverloadData().getJobCode(unit) == 'I' 
						|| OverloadManager.Instance().getOverloadData().getJobCode(unit) == 'A'){
						mySpecialUnitType1List.add(unit); 
						myAllCombatUnitList.add(unit);
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
			else if (unit.getType() == myDefenseBuildingType1) { 
				myDefenseBuildingType1List.add(unit); 
			}
			else if (unit.getType() == myDefenseBuildingType2) { 
				myDefenseBuildingType2List.add(unit); 
			}			
		}
		
		
	}
	
	/**
	 * 전투 상황에 맞게 뽑을 유닛을 컨트롤 한다.(CombatNeedUnitState)
	 * 
	 * @ sc76.choi
	 */
	void updatebuildOrderArray(){
		
		// 공격 유닛 생산 순서 설정
		if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0) {
			buildOrderArrayOfMyCombatUnitType = new int[]{1,1,4,4}; 	// 저글링 저글링 히드라 히드라 히드라 러커 ...
		}else{
			buildOrderArrayOfMyCombatUnitType = new int[]{1,1,2,2,2,3}; 	// 저글링 저글링 히드라 히드라 히드라 러커 ...
		}
	}
	
	void updateVariablesForAttackUnit(){
		// 2초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % (24 * 2) != 0) {
			return;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// sc76.choi 공격을 위한 아군 타겟 변수 할당
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(enemyMainBaseLocation != null){
			closesAttackUnitFromEnemyMainBase = getClosestCanAttackUnitTypeToTarget(UnitType.Zerg_Hydralisk, enemyMainBaseLocation.getPosition());
		}
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
			else if (myCombatUnitType3 == UnitType.Terran_Siege_Tank_Tank_Mode && unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				myKilledCombatUnitCount3 ++;		
			}
			else if (unit.getType() == mySpecialUnitType1 ) {
				myKilledSpecialUnitCount1 ++;		
			} 
			else if (unit.getType() == mySpecialUnitType2 ) {
				myKilledSpecialUnitCount2 ++;		
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
	
	/// 일꾼을 계속 추가 생산합니다
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
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
				optimalWorkerCount += baseLocation.getMinerals().size() * 1.5;
				optimalWorkerCount += baseLocation.getGeysers().size() * 3;
			}
						
			if (workerCount < optimalWorkerCount) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Protoss_Nexus || unit.getType() == UnitType.Terran_Command_Center || unit.getType() == UnitType.Zerg_Larva) {
						if (unit.isTraining() == false && unit.isMorphing() == false) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue
									.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0 && eggWorkerCount == 0) {
								// std.cout + "worker enqueue" + std.endl;
								BuildManager.Instance().buildQueue.queueAsLowestPriority(
										new MetaType(InformationManager.Instance().getWorkerType()), false);
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
		if (isInitialBuildOrderFinished == false 
				&& MyBotModule.Broodwar.self().supplyUsed() < MyBotModule.Broodwar.self().supplyTotal()  ) {
			return;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() < 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 12;

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

	/// 방어건물 및 공격유닛 생산 건물을 건설합니다
	void executeBuildingConstruction() {
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		// 전투 상황에 맞게 뽑을 유닛을 컨트롤 한다.(CombatNeedUnitState)
		updatebuildOrderArray();
		
		boolean			isPossibleToConstructDefenseBuildingType1 = false;
		boolean			isPossibleToConstructDefenseBuildingType2 = false;	
		boolean			isPossibleToConstructCombatUnitTrainingBuildingType = false;
		
		// 방어 건물 증설을 우선적으로 실시한다
		
		// 현재 방어 건물 갯수
		int numberOfMyDefenseBuildingType1 = 0; 
		int numberOfMyDefenseBuildingType2 = 0;
		
		if (myRace == Race.Protoss) {
			numberOfMyDefenseBuildingType1 += myPlayer.allUnitCount(myDefenseBuildingType1);
			numberOfMyDefenseBuildingType1 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1);
			numberOfMyDefenseBuildingType1 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType1, null);
			numberOfMyDefenseBuildingType2 += myPlayer.allUnitCount(myDefenseBuildingType2);
			numberOfMyDefenseBuildingType2 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2);
			numberOfMyDefenseBuildingType2 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType2, null);
			
			isPossibleToConstructDefenseBuildingType1 = true;
			if (myPlayer.completedUnitCount(UnitType.Protoss_Forge) > 0) {
				isPossibleToConstructDefenseBuildingType2 = true;	
			}
			if (myPlayer.completedUnitCount(UnitType.Protoss_Pylon) > 0) {
				isPossibleToConstructCombatUnitTrainingBuildingType = true;	
			}
			
		}
		else if (myRace == Race.Terran) {
			numberOfMyDefenseBuildingType1 += myPlayer.allUnitCount(myDefenseBuildingType1);
			numberOfMyDefenseBuildingType1 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1);
			numberOfMyDefenseBuildingType1 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType1, null);
			numberOfMyDefenseBuildingType2 += myPlayer.allUnitCount(myDefenseBuildingType2);
			numberOfMyDefenseBuildingType2 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2);
			numberOfMyDefenseBuildingType2 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType2, null);
			
			if (myPlayer.completedUnitCount(UnitType.Terran_Barracks) > 0) {
				isPossibleToConstructDefenseBuildingType1 = true;	
			}
			if (myPlayer.completedUnitCount(UnitType.Terran_Engineering_Bay) > 0) {
				isPossibleToConstructDefenseBuildingType2 = true;	
			}
			isPossibleToConstructCombatUnitTrainingBuildingType = true;	
			
		}
		else if (myRace == Race.Zerg) {
			// 저그의 경우 크립 콜로니 갯수를 셀 때 성큰 콜로니 갯수까지 포함해서 세어야, 크립 콜로니를 지정한 숫자까지만 만든다
			numberOfMyDefenseBuildingType1 += myPlayer.allUnitCount(myDefenseBuildingType1);
			numberOfMyDefenseBuildingType1 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1);
			numberOfMyDefenseBuildingType1 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType1, null);
			numberOfMyDefenseBuildingType1 += myPlayer.allUnitCount(myDefenseBuildingType2);
			numberOfMyDefenseBuildingType1 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2);
			numberOfMyDefenseBuildingType2 += myPlayer.allUnitCount(myDefenseBuildingType2);
			numberOfMyDefenseBuildingType2 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2);

			if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0) {
				isPossibleToConstructDefenseBuildingType1 = true;	
			}
			if (myPlayer.completedUnitCount(UnitType.Zerg_Creep_Colony) > 0) {
				isPossibleToConstructDefenseBuildingType2 = true;	
			}
			isPossibleToConstructCombatUnitTrainingBuildingType = true;
		}

		if (isPossibleToConstructDefenseBuildingType1 == true 
			&& numberOfMyDefenseBuildingType1 < necessaryNumberOfDefenseBuilding1) {
			if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1) == 0 ) {
				if (BuildManager.Instance().getAvailableMinerals() >= myDefenseBuildingType1.mineralPrice()) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
							seedPositionStrategyOfMyDefenseBuildingType, false);
				}			
			}
		}
		if (isPossibleToConstructDefenseBuildingType2 == true
			&& numberOfMyDefenseBuildingType2 < necessaryNumberOfDefenseBuilding2) {
			if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2) == 0 ) {
				if (BuildManager.Instance().getAvailableMinerals() >= myDefenseBuildingType2.mineralPrice()) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType2, 
							seedPositionStrategyOfMyDefenseBuildingType, false);
				}			
			}
		}

		// 현재 공격 유닛 생산 건물 갯수
		int numberOfMyCombatUnitTrainingBuilding = myPlayer.completedUnitCount(InformationManager.Instance().getBasicCombatBuildingType());
		numberOfMyCombatUnitTrainingBuilding += BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getBasicCombatBuildingType());
		numberOfMyCombatUnitTrainingBuilding += ConstructionManager.Instance().getConstructionQueueItemCount(InformationManager.Instance().getBasicCombatBuildingType(), null);
		
		// 공격 유닛 생산 건물 증설 : 돈이 남아돌면 실시. 최대 6개 까지만
		if (isPossibleToConstructCombatUnitTrainingBuildingType == true
			&& BuildManager.Instance().getAvailableMinerals() > 300 
			&& numberOfMyCombatUnitTrainingBuilding < 6) {
			// 게이트웨이 / 배럭 / 해처리 증설
			if (BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getBasicCombatBuildingType()) == 0 ) 
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(InformationManager.Instance().getBasicCombatBuildingType(), false);
			}
		}
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

		// 적군의 종족에 따라
		if (enemyRace == Race.Protoss) {
			upGradeAndTech.upGradeAndTechAgainstProtoss();
		}
		else if (enemyRace == Race.Terran) {
			upGradeAndTech.upGradeAndTechAgainstTerran();
		}
		else if (enemyRace == Race.Zerg) {
			upGradeAndTech.upGradeAndTechAgainstZerg();
		}else{
			upGradeAndTech.upGradeAndTechAgainstProtoss();
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

		if (myRace == Race.Protoss) {
		}
		else if (myRace == Race.Terran) {
		}
		else if (myRace == Race.Zerg) {
			
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
		
		if (myPlayer.supplyUsed() <= 390 ) 
		{
			// 공격 유닛 생산
			UnitType nextUnitTypeToTrain = getNextCombatUnitTypeToTrain();
			
			UnitType producerType = (new MetaType(nextUnitTypeToTrain)).whatBuilds();
			
			for(Unit unit : myPlayer.getUnits()) {
				
				if (unit.getType() == producerType) {
					if (unit.isTraining() == false && unit.isMorphing() == false) {

						if (BuildManager.Instance().buildQueue.getItemCount(nextUnitTypeToTrain) == 0) {	

							boolean isPossibleToTrain = false;
							if (nextUnitTypeToTrain == UnitType.Protoss_Zealot) {
								if (myPlayer.completedUnitCount(UnitType.Protoss_Gateway) > 0 ) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Protoss_Dragoon) {
								if (myPlayer.completedUnitCount(UnitType.Protoss_Gateway) > 0 && myPlayer.completedUnitCount(UnitType.Protoss_Cybernetics_Core) > 0 ) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Protoss_Dark_Templar) {
								if (myPlayer.completedUnitCount(UnitType.Protoss_Gateway) > 0 && myPlayer.completedUnitCount(UnitType.Protoss_Templar_Archives) > 0 ) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Terran_Marine) {
								if (myPlayer.completedUnitCount(UnitType.Terran_Barracks) > 0 ) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Terran_Medic) {
								if (myPlayer.completedUnitCount(UnitType.Terran_Barracks) > 0 && myPlayer.completedUnitCount(UnitType.Terran_Academy) > 0 ) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Terran_Siege_Tank_Tank_Mode) {
								if (myPlayer.completedUnitCount(UnitType.Factories) > 0 && myPlayer.completedUnitCount(UnitType.Terran_Machine_Shop) > 0 ) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Zerg_Zergling ) {
								if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Zerg_Hydralisk) {
								if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0) {
									isPossibleToTrain = true;
								}							
							}
							else if (nextUnitTypeToTrain == UnitType.Zerg_Lurker) {
								if (unit.getType() == UnitType.Zerg_Hydralisk 
									&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
									&& myPlayer.hasResearched(TechType.Lurker_Aspect) == true) {
									isPossibleToTrain = true;
								}							
							}else if (nextUnitTypeToTrain == UnitType.Zerg_Mutalisk) {
								if (myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0) {
									isPossibleToTrain = true;
								}							
							}
							
							if (isPossibleToTrain) {
								BuildManager.Instance().buildQueue.queueAsLowestPriority(nextUnitTypeToTrain, false);
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
			
			// 특수 유닛 생산			
			if (BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType1) == 0) {	
				
				boolean isPossibleToTrain = false;
				if (mySpecialUnitType1 == UnitType.Protoss_Observer) {
					if (myPlayer.completedUnitCount(UnitType.Protoss_Robotics_Facility) > 0 
						&& myPlayer.completedUnitCount(UnitType.Protoss_Observatory) > 0 ) {
						isPossibleToTrain = true;
					}							
				}
				else if (mySpecialUnitType1 == UnitType.Terran_Science_Vessel) {
					if (myPlayer.completedUnitCount(UnitType.Terran_Starport) > 0 
						&& myPlayer.completedUnitCount(UnitType.Terran_Control_Tower) > 0 
						&& myPlayer.completedUnitCount(UnitType.Terran_Science_Facility) > 0 ) {
						isPossibleToTrain = true;
					}							
				}
				// 저그 오버로드는 executeSupplyManagement 에서 이미 생산하므로 추가 생산할 필요 없다
				
				boolean isNecessaryToTrainMore = false;
				if (myPlayer.allUnitCount(mySpecialUnitType1) + BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType1) 
						< maxNumberOfSpecialUnitType1) {
					isNecessaryToTrainMore = true;
				}							
				
				if (isPossibleToTrain && isNecessaryToTrainMore) {
					
					producerType = (new MetaType(mySpecialUnitType1)).whatBuilds();

					for(Unit unit : myPlayer.getUnits()) {
						if (unit.getType() == producerType) {
							if (unit.isTraining() == false && unit.isMorphing() == false) {
		
								BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType1, true);
								break;
							}
						}
					}
				}
			}

			if (BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType2) == 0) {	
				
				boolean isPossibleToTrain = false;

				if (mySpecialUnitType2 == UnitType.Protoss_High_Templar) {
					if (myPlayer.completedUnitCount(UnitType.Protoss_Gateway) > 0 
							&& myPlayer.completedUnitCount(UnitType.Protoss_Templar_Archives) > 0 ) {
						isPossibleToTrain = true;
					}							
				}
				else if (mySpecialUnitType2 == UnitType.Terran_Battlecruiser) {
					if (myPlayer.completedUnitCount(UnitType.Terran_Starport) > 0 
							&& myPlayer.completedUnitCount(UnitType.Terran_Physics_Lab) > 0) {
						isPossibleToTrain = true;
					}							
				}
				else if (mySpecialUnitType2 == UnitType.Zerg_Defiler) {
					if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0) {
						isPossibleToTrain = true;
					}							
				}
				
				boolean isNecessaryToTrainMore = false;
				
				// 저그 종족의 경우, Egg 안에 있는 것까지 카운트 해야함 
				int allCountOfSpecialUnitType2 = myPlayer.allUnitCount(mySpecialUnitType2) + BuildManager.Instance().buildQueue.getItemCount(mySpecialUnitType2);
				if (mySpecialUnitType2.getRace() == Race.Zerg) {
					for(Unit unit : myPlayer.getUnits()) {

						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == mySpecialUnitType2) {
							allCountOfSpecialUnitType2++;
						}
						// 갓태어난 유닛은 아직 반영안되어있을 수 있어서, 추가 카운트를 해줘야함
						//if (unit.getType() == mySpecialUnitType2 && unit.isConstructing()) {
						//	allCountOfSpecialUnitType2++;
						//}
					}
					  
				}
				if (allCountOfSpecialUnitType2 < maxNumberOfSpecialUnitType2) {
					isNecessaryToTrainMore = true;
				}							
				
				if (isPossibleToTrain && isNecessaryToTrainMore) {
					
					producerType = (new MetaType(mySpecialUnitType2)).whatBuilds();
					
					for(Unit unit : myPlayer.getUnits()) {
						if (unit.getType() == producerType) {
							if (unit.isTraining() == false && unit.isMorphing() == false) {
		
								BuildManager.Instance().buildQueue.queueAsLowestPriority(mySpecialUnitType2, true);
								break;
							}
							
						}
					}
				}
			}
		}
	}

	/// 다음에 생산할 공격유닛 UnitType 을 리턴합니다
	public UnitType getNextCombatUnitTypeToTrain() {
		
		UnitType nextUnitTypeToTrain = null;

		if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 1) {
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

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	public int getCountAttack() {
		return countAttack;
	}

	public void setCountAttack(int countAttack) {
		this.countAttack = countAttack;
	}

	public int getCountDefence() {
		return countDefence;
	}

	public void setCountDefence(int countDefence) {
		this.countDefence = countDefence;
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
}