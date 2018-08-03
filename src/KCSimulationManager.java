import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class KCSimulationManager {

	public Player selfPlayer;		///< 아군 Player		
	public Player enemyPlayer;		///< 아군 Player의 종족		
	public Race selfRace;			///< 적군 Player		
	public Race enemyRace;			///< 적군 Player의 종족  
	
	private BaseLocation selfMainBaseLocation = null;
	private BaseLocation selfFirstExpansionLocation = null;
	private Chokepoint selfFirstChokePoint = null;
	private Chokepoint selfSecondChokePoint = null;	
	private BaseLocation enemyMainBaseLocation = null;
	private Chokepoint enemyFirstChokePoint = null;
	private Chokepoint enemySecondChokePoint = null;
	
	private UnitType enemyBasicCombatUnitType = null;
	private UnitType enemyAdvancedCombatUnitType = null;
	private UnitType enemyAdvancedCombatUnitType2 = null;
	private UnitType enemyBasicDefenceUnitType = null;
	private UnitType enemyAdvencedDefenceUnitType = null;
	
	private int myPoint = 0;
	private int enemyPoint = 0;
	
	private int countBasicCombatUnit = 0;
	private int countAdvencedCombatUnit = 0;
	private int countAdvencedCombatUnit2 = 0;
	private int countBasicDefenceUnit = 0;
	private int countAdvencedDefenceUnit = 0;
	
	// static singleton 객체를 리턴합니다
	private static KCSimulationManager instance = new KCSimulationManager();
	public static KCSimulationManager Instance() {
		return instance;
	}
	
	public KCSimulationManager() {
	}
	
	private CommandUtil commandUtil = new CommandUtil();
	
	/**
	 * 
	[enemyBasicCombatUnitType]
	UnitType.Protoss_Zealot;
	UnitType.Terran_Marine;
	UnitType.Zerg_Zergling;
		
	[enemyAdvancedCombatUnitType]
	UnitType.Protoss_Dragoon;
	UnitType.Terran_Medic;
	UnitType.Zerg_Hydralisk;
	
	[enemyBasicDefenceUnitType]
	UnitType.Protoss_Pylon;
	UnitType.Terran_Bunker;
	UnitType.Zerg_Creep_Colony;
	
	[enemyAdvencedDefenceUnitType]
	UnitType.Protoss_Photon_Cannon;
	UnitType.Terran_Missile_Turret;
	UnitType.Zerg_Sunken_Colony;
	 */
	
	public boolean canAttackNow(List<Unit> Units){
		
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();
		
		selfMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		selfFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
		selfFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
		selfSecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
		enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
		
		enemyBasicCombatUnitType = InformationManager.Instance().getBasicCombatUnitType(enemyRace);
		enemyAdvancedCombatUnitType = InformationManager.Instance().getAdvancedCombatUnitType(enemyRace);
		enemyAdvancedCombatUnitType2 = InformationManager.Instance().getAdvancedCombatUnitType2(enemyRace);
		enemyBasicDefenceUnitType = InformationManager.Instance().getBasicDefenseBuildingType(enemyRace);
		enemyAdvencedDefenceUnitType = InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace);
		
		myPoint = 0;
		enemyPoint = 0;
		
		countBasicCombatUnit = 0;
		countAdvencedCombatUnit = 0;
		countAdvencedCombatUnit2 = 0;
		countBasicDefenceUnit = 0;
		countAdvencedDefenceUnit = 0;
		
		int myWorkerUnitTypePoint = 1;
		int myBasicCombatUnitTypePoint = 10;
		int myAdvencedCombatUnitTypePoint = 40;
		int myAdvencedCombatUnitType2Point = 40; // 울트라리스크
		int myBasicDefenceUnitTypePoint = 0;
		int myDefenceCombatUnitTypePoint = 50;
		
		for(Unit unit : Units){
			if(unit.getPlayer() == selfPlayer){
				if(unit.getType() == InformationManager.Instance().getWorkerType()){
					myPoint += myWorkerUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getBasicCombatUnitType()){
					myPoint += myBasicCombatUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType()){
					myPoint += myAdvencedCombatUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType2()){
					myPoint += myAdvencedCombatUnitType2Point;					
				}else if(unit.getType() == InformationManager.Instance().getBasicDefenseBuildingType()){
					myPoint += myBasicDefenceUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType()){
					myPoint += myDefenceCombatUnitTypePoint;
				}
			}else{
				if(unit.getType() == InformationManager.Instance().getBasicCombatUnitType(enemyRace)){
					countBasicCombatUnit++;
					enemyPoint += getBasicCombatUnitTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType(enemyRace)){
					countAdvencedCombatUnit++;
					enemyPoint += getAdvencedCombatUnitTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType2(enemyRace)){
					countAdvencedCombatUnit2++;
					enemyPoint += getAdvencedCombatUnitType2Point(unit);
				}else if(unit.getType() == InformationManager.Instance().getBasicDefenseBuildingType(enemyRace)){
					countBasicDefenceUnit++;
					enemyPoint += getBasicDefenceBuildingTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace)){
					countAdvencedDefenceUnit++;
					enemyPoint += getAdvencedDefenceBuildingTypePoint(unit);
				}
			}
		}
		
		// TODO sc76.choi 각 false 값을 누적해서 계속 false가 나오면 CombatState를 defence모드로 변경한다.
		// TODO sc76.choi 개별 전투에서 모두 패배를 기록하고 있다고 판단한다.
		if(myPoint >= enemyPoint){
			return true;
		}
		return false;
	}
	
	public KCSimulationResult canAttackNow2(List<Unit> Units){
		
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();
		
		selfMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		selfFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
		selfFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
		selfSecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
		enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
		
		enemyBasicCombatUnitType = InformationManager.Instance().getBasicCombatUnitType(enemyRace);
		enemyAdvancedCombatUnitType = InformationManager.Instance().getAdvancedCombatUnitType(enemyRace);
		enemyAdvancedCombatUnitType2 = InformationManager.Instance().getAdvancedCombatUnitType2(enemyRace);
		enemyBasicDefenceUnitType = InformationManager.Instance().getBasicDefenseBuildingType(enemyRace);
		enemyAdvencedDefenceUnitType = InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace);
		
		myPoint = 0;
		enemyPoint = 0;
		
		countBasicCombatUnit = 0;
		countAdvencedCombatUnit = 0;
		countAdvencedCombatUnit2 = 0;
		countBasicDefenceUnit = 0;
		countAdvencedDefenceUnit = 0;
		
		int myWorkerUnitTypePoint = 10;
		int myBasicCombatUnitTypePoint = 10;
		int myAdvencedCombatUnitTypePoint = 40;
		int myAdvencedCombatUnitType2Point = 40; // 울트라리스크
		int myBasicDefenceUnitTypePoint = 0;
		int myDefenceCombatUnitTypePoint = 50;
		
		boolean existEnemyAdvancedDefenceBuilding = false;
		
		for(Unit unit : Units){
			if(unit.getPlayer() == selfPlayer){
				if(unit.getType() == InformationManager.Instance().getWorkerType()){
					myPoint += myWorkerUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getBasicCombatUnitType()){
					myPoint += myBasicCombatUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType()){
					myPoint += myAdvencedCombatUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType2()){
					myPoint += myAdvencedCombatUnitType2Point;					
				}else if(unit.getType() == InformationManager.Instance().getBasicDefenseBuildingType()){
					myPoint += myBasicDefenceUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType()){
					myPoint += myDefenceCombatUnitTypePoint;
				}
			}else{
				if(unit.getType() == InformationManager.Instance().getBasicCombatUnitType(enemyRace)){
					countBasicCombatUnit++;
					enemyPoint += getBasicCombatUnitTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType(enemyRace)){
					countAdvencedCombatUnit++;
					enemyPoint += getAdvencedCombatUnitTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType2(enemyRace)){
					countAdvencedCombatUnit2++;
					enemyPoint += getAdvencedCombatUnitType2Point(unit);
				}else if(unit.getType() == InformationManager.Instance().getBasicDefenseBuildingType(enemyRace)){
					countBasicDefenceUnit++;
					enemyPoint += getBasicDefenceBuildingTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace)){
					existEnemyAdvancedDefenceBuilding = true;
					countAdvencedDefenceUnit++;
					enemyPoint += getAdvencedDefenceBuildingTypePoint(unit);
				}
			}
		}
		
		// TODO sc76.choi 각 false 값을 누적해서 계속 false가 나오면 CombatState를 defence모드로 변경한다.
		// TODO sc76.choi 개별 전투에서 모두 패배를 기록하고 있다고 판단한다.
		//KCSimulationResult result = new KCSimulationResult();
		
		KCSimulationResult.Instance().setCanAttackNow(myPoint >= enemyPoint);
		KCSimulationResult.Instance().setMyPoint(myPoint);
		KCSimulationResult.Instance().setEnemyPoint(enemyPoint);
		KCSimulationResult.Instance().setExistEnemyAdvancedDefenceBuilding(existEnemyAdvancedDefenceBuilding);
		
		return KCSimulationResult.Instance();
	}
	
	// sc76.choi 전체 토탈 병력을 계산해 본다.
	// 단, 일꾼은 제외 한다.
	public KCSimulationResult canGreateAttackNow(){
		
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();
		
		selfMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		selfFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
		selfFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
		selfSecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
		enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer);
		
		enemyBasicCombatUnitType = InformationManager.Instance().getBasicCombatUnitType(enemyRace);
		enemyAdvancedCombatUnitType = InformationManager.Instance().getAdvancedCombatUnitType(enemyRace);
		enemyAdvancedCombatUnitType2 = InformationManager.Instance().getAdvancedCombatUnitType2(enemyRace);
		enemyBasicDefenceUnitType = InformationManager.Instance().getBasicDefenseBuildingType(enemyRace);
		enemyAdvencedDefenceUnitType = InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace);
		
		myPoint = 0;
		enemyPoint = 0;
		
		countBasicCombatUnit = 0;
		countAdvencedCombatUnit = 0;
		countAdvencedCombatUnit2 = 0;
		countBasicDefenceUnit = 0;
		countAdvencedDefenceUnit = 0;
		
		int myWorkerUnitTypePoint = 0;
		int myBasicCombatUnitTypePoint = 10;
		int myAdvencedCombatUnitTypePoint = 40;
		int myAdvencedCombatUnitType2Point = 40; // 울트라리스크
		int myBasicDefenceUnitTypePoint = 0;
		int myDefenceCombatUnitTypePoint = 50;
		
		boolean existEnemyAdvancedDefenceBuilding = false;
		
		UnitData unitData = InformationManager.Instance().getUnitData(selfPlayer);
		Iterator<Integer> it = unitData.getUnitAndUnitInfoMap().keySet().iterator();
		
		while (it.hasNext()) {
			
			Unit unit = unitData.getUnitAndUnitInfoMap().get(it.next()).getUnit();
			
			if(commandUtil.IsValidUnit(unit)){
				if(unit.getType() == InformationManager.Instance().getWorkerType()){
					//myPoint += myWorkerUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getBasicCombatUnitType()){
					myPoint += myBasicCombatUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType()){
					myPoint += myAdvencedCombatUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType2()){
					myPoint += myAdvencedCombatUnitType2Point;					
				}else if(unit.getType() == InformationManager.Instance().getBasicDefenseBuildingType()){
					myPoint += myBasicDefenceUnitTypePoint;
				}else if(unit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType()){
					myPoint += myDefenceCombatUnitTypePoint;
				}
			}
			
			UnitData unitData2 = InformationManager.Instance().getUnitData(enemyPlayer);
			Iterator<Integer> it2 = unitData2.getUnitAndUnitInfoMap().keySet().iterator();
			
			while (it2.hasNext()) {			
				if(unit.getType() == InformationManager.Instance().getBasicCombatUnitType(enemyRace)){
					countBasicCombatUnit++;
					enemyPoint += getBasicCombatUnitTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType(enemyRace)){
					countAdvencedCombatUnit++;
					enemyPoint += getAdvencedCombatUnitTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedCombatUnitType2(enemyRace)){
					countAdvencedCombatUnit2++;
					enemyPoint += getAdvencedCombatUnitType2Point(unit);
				}else if(unit.getType() == InformationManager.Instance().getBasicDefenseBuildingType(enemyRace)){
					countBasicDefenceUnit++;
					enemyPoint += getBasicDefenceBuildingTypePoint(unit);
				}else if(unit.getType() == InformationManager.Instance().getAdvancedDefenseBuildingType(enemyRace)){
					existEnemyAdvancedDefenceBuilding = true;
					countAdvencedDefenceUnit++;
					enemyPoint += getAdvencedDefenceBuildingTypePoint(unit);
				}
			}
		}
		
		// TODO sc76.choi 각 false 값을 누적해서 계속 false가 나오면 CombatState를 defence모드로 변경한다.
		// TODO sc76.choi 개별 전투에서 모두 패배를 기록하고 있다고 판단한다.
		//KCSimulationResult result = KCSimulationResult.Instance();
		
		KCSimulationResult.Instance().setCanAttackNow(myPoint >= enemyPoint);
		KCSimulationResult.Instance().setMyPoint(myPoint);
		KCSimulationResult.Instance().setEnemyPoint(enemyPoint);
		KCSimulationResult.Instance().setExistEnemyAdvancedDefenceBuilding(existEnemyAdvancedDefenceBuilding);
		
		return KCSimulationResult.Instance();
	}
	
	/*
	UnitType.Protoss_Zealot;
	UnitType.Terran_Marine;
	UnitType.Zerg_Zergling;
	 */
	public int getBasicCombatUnitTypePoint(Unit unit){
		
		if (enemyRace == Race.Protoss) {
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 8;
			}
			return 14;
		} else if (enemyRace == Race.Terran) {
			return 8;
		} else if (enemyRace == Race.Zerg) {
			return 8;
		} else {
			return 0;
		}
	}
	
	/*
	UnitType.Protoss_Dragoon;
	UnitType.Terran_Medic;
	UnitType.Zerg_Hydralisk;
	 */
	public int getAdvencedCombatUnitTypePoint(Unit unit){
		if (enemyRace == Race.Protoss) {
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 18;
			}
			return 25;
		} else if (enemyRace == Race.Terran) {
			return 0;
		} else if (enemyRace == Race.Zerg) {
			return 20;
		} else {
			return 0;
		}
	}
	
	/*
	UnitType.Protoss_Archon;
	UnitType.Terran_Firebat;
	UnitType.Zerg_Ultralisk;
	 */
	
	public int getAdvencedCombatUnitType2Point(Unit unit){
		
		if (enemyRace == Race.Protoss) {
			return 25;
		} else if (enemyRace == Race.Terran) {
			return 18;
		} else if (enemyRace == Race.Zerg) {
			return 40;
		} else {
			return 0;
		}
	}	
	
	/*
	UnitType.Protoss_Pylon;
	UnitType.Terran_Missile_Turret;
	UnitType.Zerg_Creep_Colony;
	 */
	public int getBasicDefenceBuildingTypePoint(Unit unit){
		if (enemyRace == Race.Protoss) {
			return 0;
		} else if (enemyRace == Race.Terran) {
			return 0;
		} else if (enemyRace == Race.Zerg) {
			return 8;
		} else {
			return 0;
		}
	}	
	
	/*
	UnitType.Protoss_Photon_Cannon;
	UnitType.Terran_Bunker;
	UnitType.Zerg_Sunken_Colony;
	 */
	public int getAdvencedDefenceBuildingTypePoint(Unit unit){
		if (enemyRace == Race.Protoss) {
			// sc76.choi 아직 지어지고 있으면 0로 리턴
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 0;
			}		
			return 20;
		} else if (enemyRace == Race.Terran) {
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 9;
			}
			return 18;			
		} else if (enemyRace == Race.Zerg) {
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 0;
			}			
			return 20;
		} else {
			return 0;
		}
	}	
}
