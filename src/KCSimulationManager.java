import java.util.ArrayList;
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
	
	int myBasicCombatUnitTypePoint = 1;
	int myAdvencedCombatUnitTypePoint = 4;
	int myAdvencedCombatUnitType2Point = 4;
	int myBasicDefenceUnitTypePoint = 0;
	int myDefenceCombatUnitTypePoint = 5;
	
	// static singleton 객체를 리턴합니다
	private static KCSimulationManager instance = new KCSimulationManager();
	public static KCSimulationManager Instance() {
		return instance;
	}
	
	public KCSimulationManager() {
		
	}
	
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
		
		for(Unit unit : Units){
			if(unit.getPlayer() == selfPlayer){
				if(unit.getType() == InformationManager.Instance().getBasicCombatUnitType()){
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
				//System.out.println("unit.getPlayer          : " + unit.getPlayer().toString());
				//System.out.println("unit.enemyRace          : " + enemyRace);
				//System.out.println("unit.getType            : " + unit.getType());
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
				//System.out.println("      enemyPoint By 1   : " + enemyPoint);
			}
		}
//		System.out.println("-------------------------------------------------------------");
//		System.out.println("countBasicCombatUnit     : " + countBasicCombatUnit);
//		System.out.println("countAdvencedCombatUnit  : " + countBasicCombatUnit);
//		System.out.println("countBasicDefenceUnit    : " + countBasicCombatUnit);
//		System.out.println("countAdvencedDefenceUnit : " + countBasicCombatUnit);
//		System.out.println();
//		System.out.println("canAttackNow myPoint     : " + myPoint);
//		System.out.println("canAttackNow enemyPoint  : " + enemyPoint);
		
		// TODO sc76.choi 각 false 값을 누적해서 계속 false가 나오면 CombatState를 defence모드로 변경한다.
		// TODO sc76.choi 개별 전투에서 모두 패배를 기록하고 있다고 판단한다.
		if(myPoint >= enemyPoint){
			return true;
		}
		return false;
	}
	
	/*
	UnitType.Protoss_Zealot;
	UnitType.Terran_Marine;
	UnitType.Zerg_Zergling;
	 */
	public int getBasicCombatUnitTypePoint(Unit unit){
		
		if (enemyRace == Race.Protoss) {
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 1;
			}
			return 3;
		} else if (enemyRace == Race.Terran) {
			return 1;
		} else if (enemyRace == Race.Zerg) {
			return 1;
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
				return 2;
			}
			return 3;
		} else if (enemyRace == Race.Terran) {
			return 0;
		} else if (enemyRace == Race.Zerg) {
			return 1;
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
			return 3;
		} else if (enemyRace == Race.Terran) {
			return 2;
		} else if (enemyRace == Race.Zerg) {
			return 5;
		} else {
			return 0;
		}
	}	
	
	/*
	UnitType.Protoss_Pylon;
	UnitType.Terran_Bunker;
	UnitType.Zerg_Creep_Colony;
	 */
	public int getBasicDefenceBuildingTypePoint(Unit unit){
		if (enemyRace == Race.Protoss) {
			return 2;
		} else if (enemyRace == Race.Terran) {
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 1;
			}
			return 2;
		} else if (enemyRace == Race.Zerg) {
			return 1;
		} else {
			return 0;
		}
	}	
	
	/*
	UnitType.Protoss_Photon_Cannon;
	UnitType.Terran_Missile_Turret;
	UnitType.Zerg_Sunken_Colony;
	 */
	public int getAdvencedDefenceBuildingTypePoint(Unit unit){
		if (enemyRace == Race.Protoss) {
			// sc76.choi 아직 지어지고 있으면 0로 리턴
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 0;
			}		
			return 1;
		} else if (enemyRace == Race.Terran) {
			return 0;
		} else if (enemyRace == Race.Zerg) {
			if(selfPlayer.getUpgradeLevel(UpgradeType.Adrenal_Glands) > 0){
				return 0;
			}			
			return 1;
		} else {
			return 0;
		}
	}	
}
