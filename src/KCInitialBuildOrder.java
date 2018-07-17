import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class KCInitialBuildOrder {
	
	private TilePosition firstDefenceBuildingPos = null;
	private TilePosition secondDefenceBuildingPos = null;
	private TilePosition thirdDefenceBuildingPos = null;
	
	public void setInitialBuildOrderAgainstProtoss(){
		
		getDefencePosition(); 
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//5
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//6
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//7
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//8
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord); // 두번째 오버로드
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//9
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//10
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//11
		
		// 2 번째 해처리
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType()); //12 해처리
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool); //11 스포닝풀

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//13
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//14
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//13
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 세번째 오버로드
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor); //19
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
		
		// 3번째 해처리	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //14 해처리
		
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//18
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony, 
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//18
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost, false); // 저글링 속도업(Faster Zergling movement)
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk_Den);	//21
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//23
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//24			
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//24			
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//25
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//26
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//27			

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 다섯번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//28
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//29
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//34
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//35
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber, 
				BuildOrderItem.SeedPositionStrategy.MainBaseLocation); //26
		
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);		
	}
	
	public void setInitialBuildOrderAgainstZerg(){
		
		getDefencePosition();
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//5
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//6
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//7
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//8
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//9
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool); //11 스포닝풀

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord); // 두번째 오버로드
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//10
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//9
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//10

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.FirstChokePoint);	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//12
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//13
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //10 해처리
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //14 해처리

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 세번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//16
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17

		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
		//		seedPositionStrategyOfMyDefenseBuildingType);	//16
					
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//17
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//18
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor); //19
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.SecondChokePoint);	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//22
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber,
				BuildOrderItem.SeedPositionStrategy.MainBaseLocation); //31
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk_Den);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost); // 저글링 속도업(Faster Zergling movement)
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//26
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27			

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 다섯번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//34
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//35
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//36
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//37
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);	
	}
	
	public void setInitialBuildOrderAgainstTerran(){
		
		getDefencePosition();
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//5
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//6
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//7
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//8
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord); // 두번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//9
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//10
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool); //11 스포닝풀

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor); //19
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //10 해처리

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//13
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 세번째 오버로드

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//16
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost); // 저글링 속도업(Faster Zergling movement)
					
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//18
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk_Den);	//21
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
		
		// sc76.choi 확장 가스
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//23
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//25
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//26
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27			
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 다섯번째 오버로드
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//28
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//29
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber,
				BuildOrderItem.SeedPositionStrategy.MainBaseLocation); //31
	}
	
	public void setInitialBuildOrderAgainstRandom(){
		
		getDefencePosition(); 
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//5
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//6
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//7
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//8
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord); // 두번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//9

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool); //11 스포닝풀
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//10
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		
		// 2 번째 해처리
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType()); //10 해처리

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//13
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor); //19

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 세번째 오버로드

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//16
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17

		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
		//		seedPositionStrategyOfMyDefenseBuildingType);	//16
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost); // 저글링 속도업(Faster Zergling movement)
		
		// 3 번째 해처리
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstChokePoint); //14 해처리
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//18
		
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//21

		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//18
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
		//		seedPositionStrategyOfMyDefenseBuildingType);	//19
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk_Den);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				firstDefenceBuildingPos, true);
//				BuildOrderItem.SeedPositionStrategy.FirstChokePoint);	//20
//		StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22


		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				secondDefenceBuildingPos, true);
//				StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//23
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//24			

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony, 
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//25
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//26
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27			

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 다섯번째 오버로드
		
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//28
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//29
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//30
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//34
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//35
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//36
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//37
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber, false); //26
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);			
	}
	
	public void getDefencePosition(){
		
		//Config.BuildingSpacing = 0;
		//Config.BuildingResourceDepotSpacing = 0;
		
		if(MyBotModule.Broodwar.mapFileName().equals("(4)OverWatch.scx")){
			if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
			   MyBotModule.Broodwar.self().getStartLocation().getY() == 7){
				//1시
				firstDefenceBuildingPos = new TilePosition(90, 10);
				secondDefenceBuildingPos = new TilePosition(94, 13);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 117){
				//7시
				firstDefenceBuildingPos = new TilePosition(31, 118);
				secondDefenceBuildingPos = new TilePosition(35, 115);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 7){
				//11시
				firstDefenceBuildingPos = new TilePosition(10,30);
				secondDefenceBuildingPos = new TilePosition(11,34);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 117){
				//5시
				firstDefenceBuildingPos = new TilePosition(115,98);
				secondDefenceBuildingPos = new TilePosition(115, 94);
			}
		}
		
		else if(MyBotModule.Broodwar.mapFileName().equals("(4)CircuitBreaker.scx")){
			if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 9){
				//1시
				firstDefenceBuildingPos = new TilePosition(118,23);
				secondDefenceBuildingPos = new TilePosition(122,23);			
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 118){
				//7시
				firstDefenceBuildingPos = new TilePosition(118,23);
				secondDefenceBuildingPos = new TilePosition(122,23);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 7 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 9){
				//11시
				firstDefenceBuildingPos = new TilePosition(118,23);
				secondDefenceBuildingPos = new TilePosition(122,23);	
			}
			else if(MyBotModule.Broodwar.self().getStartLocation().getX() == 117 &&
					MyBotModule.Broodwar.self().getStartLocation().getY() == 118){
				//5시
				firstDefenceBuildingPos = new TilePosition(118,23);
				secondDefenceBuildingPos = new TilePosition(122,23);
			}
		}
		
	}
}
