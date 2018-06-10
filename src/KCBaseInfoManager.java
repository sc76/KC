
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

/**
 * 
 * @author sc76.choi
 * 
 * <P>
 *  게임 시작, 진행시에 map의 전체 정보를 저장해 두는 자료 구조를 업데이트 합니다.<br>
 *  게임 중에도 계속 업데이트 됩니다.<br>
 *  시야에 밝혀진 시점의 상황을 계속 업데이트 합니다.<br>
 *  MapGrid와, MapTool과 같이 활용됩니다.<br>
 * <P>  
 *  base 포지션, 정찰여부, 정찰 횟수, starting인지 여부, 적군이 건물을 건설여부, 적군의 존재 여부, 적군의 유형, 기본 미네날 양, 기본 가스 양, 남아 있는 미네날 양, 남아있는 가스 양, 나의 본진으로 부터의 거리
 * <P>  
 *	import bwta.BWTA;
 *	import bwta.BaseLocation;
 *	import bwta.Region;
 *  for (BaseLocation baseLocation : BWTA.getBaseLocations()){}
 *<P>  
 *  // GroundDistance 를 기준으로 가장 가까운 곳으로 선정
 *	double distanceFromMyBase = (double)(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getGroundDistance(baseLocation));
 */

public class KCBaseInfoManager {
	public static Game Broodwar;
	
	private static KCBaseInfoManager instance = new KCBaseInfoManager();
	
	/// static singleton 객체를 리턴합니다
	public static KCBaseInfoManager Instance() {
		return instance;
	}
	
	// 각 Base에 대한 상황을 저장하는 자료구조 객체
	private List<KCBaseInfo> kcBaseList = new ArrayList<KCBaseInfo>();
	
	
	// 경기가 시작이후, base 정보를 계속 업데이트 합니다.
	// StrategyManager.onStart에서 한번 호출 합니다.
	public void updateByOneTime() {
		int baseId = 0;
		for (BaseLocation baseLocation : BWTA.getBaseLocations()){
			
			KCBaseInfo currentBase = new KCBaseInfo(); // 저장한 base 객체 생성
			
			BaseLocation selfBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
			currentBase.setBaseId(++baseId);
			currentBase.setInintialBaseOwner((selfBaseLocation.getPosition().equals(baseLocation.getPosition())) ? "S" : "");
			currentBase.setOriginPosition(baseLocation.getPosition()); // position 정보
			currentBase.setOriginTilePosition(baseLocation.getTilePosition()); // tile position 정보
			//currentBase.setScouted(false); // 정찰 여부
			//currentBase.setScoutCount(0); // 정찰 횟수
			currentBase.setStartingBase(baseLocation.isStartLocation()); // start팅 여부
			//currentBase.setExistEnemyArmy(false); // 적군 존재 여부
			//currentBase.setExistEnemyBuilding(false); // 적군 건물 존재 여부
			currentBase.setKindOfEnemyArmy(""); // 적군이 있을때 유닛이 공중공격 가능한지, 지상공격 가능한지 구분
			double distanceByGroundFromMyBase = (double)selfBaseLocation.getGroundDistance(baseLocation);
			currentBase.setDistanceByGroundFromMyBase(distanceByGroundFromMyBase); // 나의 본진으로 부터의 땅으로 이동시 거리
			double distanceByAirFromMyBase = (double)selfBaseLocation.getAirDistance(baseLocation)+0.5;
			currentBase.setDistanceByAirFromMyBase(distanceByAirFromMyBase); // 나의 본진으로 부터의 공중으로 이동시 거리
			
			// 초기 미네랄 량 측정
			int baseAmountMineral = 0; 
			for(Unit mineral : baseLocation.getMinerals()){
				if(mineral.getType() == UnitType.Resource_Mineral_Field){
					baseAmountMineral += mineral.getInitialResources();
				}
			}
			currentBase.setBaseAmountMineral(baseAmountMineral);
			currentBase.setCurrentAmountMineral(baseAmountMineral);

			// 초기 가스 량 측정
			int baseAmountGas = 0; 
			for(Unit gas : baseLocation.getGeysers()){
				if(gas.getType() == UnitType.Resource_Vespene_Geyser){
					baseAmountGas +=  gas.getInitialResources();
				}
			}
			currentBase.setBaseAmountGas(baseAmountGas);
			currentBase.setCurrentAmountGas(baseAmountGas);
			
			kcBaseList.add(currentBase);
		}
	}
	
	/**
	 * List 객체에 담긴 KCBaseInfo를 갱신 한다.
	 * StrategyManager update에서 호출 합니다.
	 * @author sc76.choi
	 */	
	public void update() {
		boolean isExploredTemp = false;
		for(KCBaseInfo kcBaseInfo : kcBaseList){
			
			BaseLocation currentBase = BWTA.getNearestBaseLocation(kcBaseInfo.getOriginPosition());  
			isExploredTemp = (MyBotModule.Broodwar.isExplored(currentBase.getTilePosition())
								&& MyBotModule.Broodwar.isVisible(currentBase.getTilePosition()));

			// 정찰 된 base이면
			if(isExploredTemp){
				//BaseLocation selfBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
				//currentBase.setInintialBaseOwner((selfBaseLocation.getPosition().equals(currentBase.getPosition())) ? "S" : "");
				
				//currentBase.setScouted(false); // 정찰 여부
				//currentBase.setScoutCount(0); // 정찰 횟수
				//currentBase.setExistEnemyArmy(false); // 적군 존재 여부
				//currentBase.setExistEnemyBuilding(false); // 적군 건물 존재 여부
				
				// 남은 미네랄 량 측정
				int baseAmountMineral = 0; 
				for(Unit mineral : currentBase.getMinerals()){
					if(mineral.getType() == UnitType.Resource_Mineral_Field){
						baseAmountMineral += mineral.getResources();
					}
				}
				kcBaseInfo.setCurrentAmountMineral(baseAmountMineral);
	
				// 남은 가스 량 측정
				int baseAmountGas = 0; 
				for(Unit gas : currentBase.getGeysers()){
					if(gas.getType() == UnitType.Resource_Vespene_Geyser){
						baseAmountGas +=  gas.getResources();
					}
				}
				kcBaseInfo.setCurrentAmountGas(baseAmountGas);
			}
		}
	}
	
	/**
	 * List 객체에 담긴 KCBaseInfo를 print 한다.
	 * StrategyManager.onStart에서 호출 합니다.
	 * @author sc76.choi
	 */
	public String printKCBaseList(){
		
		String baseInfo = "";
		
		for(KCBaseInfo kcBaseInfo : kcBaseList){
			baseInfo += "baseId                        : " + kcBaseInfo.getBaseId() + "\n";
			baseInfo += "setInintialBaseOwner          : " + kcBaseInfo.getInintialBaseOwner() + "\n";
			baseInfo += "getOriginTilePosition         : " + kcBaseInfo.getOriginTilePosition() + "\n";
			baseInfo += "getOriginPosition             : " + kcBaseInfo.getOriginPosition() + "\n";
			baseInfo += "isScouted                     : " + kcBaseInfo.isScouted() + "\n";
			baseInfo += "getScoutCount                 : " + kcBaseInfo.getScoutCount() + "\n";
			baseInfo += "isStartingBase                : " + kcBaseInfo.isStartingBase() + "\n";
			baseInfo += "isExistEnemyArmy              : " + kcBaseInfo.isExistEnemyArmy() + "\n";
			baseInfo += "isExistEnemyBuilding          : " + kcBaseInfo.isExistEnemyBuilding() + "\n";
			baseInfo += "getKindOfEnemyArmy            : " + kcBaseInfo.getKindOfEnemyArmy() + "\n";
			baseInfo += "getBaseAmountMineral          : " + kcBaseInfo.getBaseAmountMineral() + "\n";
			baseInfo += "getBaseAmountGas              : " + kcBaseInfo.getBaseAmountGas() + "\n";
			baseInfo += "getCurrentAmountMineral       : " + kcBaseInfo.getCurrentAmountMineral() + "\n";
			baseInfo += "getCurrentAmountGas           : " + kcBaseInfo.getCurrentAmountGas() + "\n";
			baseInfo += "getDistanceByGroundFromMyBase : " + kcBaseInfo.getDistanceByGroundFromMyBase() + "\n";
			baseInfo += "getDistanceByAirFromMyBase    : " + kcBaseInfo.getDistanceByAirFromMyBase() + "\n";
			
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}
		
		return baseInfo;
	}
}
