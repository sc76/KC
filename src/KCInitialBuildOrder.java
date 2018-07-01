import bwapi.UnitType;
import bwapi.UpgradeType;

public class KCInitialBuildOrder {
	
	public void setInitialBuildOrderAgainstProtoss(){
		
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
				BuildOrderItem.SeedPositionStrategy.FirstChokePoint);	//20
//		StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22


		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//23
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//24			

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony, 
				BuildOrderItem.SeedPositionStrategy.FirstChokePoint);
		
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
	
	public void setInitialBuildOrderAgainstZerg(){
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//5
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//6
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//7
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//8
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord); // 두번째 오버로드
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//9
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//10
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool); //11 스포닝풀

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType()); //10 해처리

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.MainBaseLocation);	//20

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//11
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//12
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//13
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//15

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //14 해처리

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 세번째 오버로드

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//15
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//16
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//17

		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
		//		seedPositionStrategyOfMyDefenseBuildingType);	//16
					
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//17
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//18

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.SecondChokePoint);	//20

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
		//		seedPositionStrategyOfMyDefenseBuildingType);	//19
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor); //19

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk_Den);	//21
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		
		

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost); // 저글링 속도업(Faster Zergling movement)
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//25
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//26
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27			

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber,
				BuildOrderItem.SeedPositionStrategy.MainBaseLocation); //31
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 다섯번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//27
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//34
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//35
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//27
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//28
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//36
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//37
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);	
	}
	
	public void setInitialBuildOrderAgainstTerran(){
		
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
		

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //10 해처리

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
					
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //14 해처리
		
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
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);	//20
//		StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22


		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//23
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//24			

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//25
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//26
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27			

		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 다섯번째 오버로드
		
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//28
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//29
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//30
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);	//20
//		StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//27	
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//34
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);	//35
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//36
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//37
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber,
				BuildOrderItem.SeedPositionStrategy.SecondChokePoint); //31
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);		
	}
	
	public void setInitialBuildOrderAgainstRandom(){

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
				BuildOrderItem.SeedPositionStrategy.FirstChokePoint);	//20
//		StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22


		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
				StrategyManager.Instance().getSeedPositionStrategyOfMyDefenseBuildingType());	//20
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);	// 네번째 오버로드
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, false);	//22
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone, false);	//23
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling);	//24			

		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony, 
				BuildOrderItem.SeedPositionStrategy.FirstChokePoint);
		
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
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
				BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation); //31
		
		//BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);		
	}
}
