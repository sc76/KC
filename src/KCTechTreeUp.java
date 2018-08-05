import bwapi.Player;
import bwapi.Race;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class KCTechTreeUp {

	Player myPlayer = MyBotModule.Broodwar.self();
	Race myRace = MyBotModule.Broodwar.self().getRace();

	// sc76.choi 상황에 맞는 빌드 모드 설정
	public enum BuildState { 
		normalMode,                         // 기본
		onlyZergling,						// 저글링 모드, 저글링이 다수 필요할 때
		onlyHydralist,						// 히드라 모드, 히드라가 다수 필요할 때
		onlyMutalisk,						// 뮤탈 모드, 중반 이후, only 질럿, 저글링만 보일 때		
		fasterMutalisk,						// 빠른 뮤탈 모드, 태란 다수 탱크가 있을 때, 퀸도 빨리 올려 활용한다.
		fasterUltralisk,						// 빠른 울트라 모드, 태란 입구 막음 or 프로토스 앞마당 포토밭을 만들 때 상황
		fastZergling_Z,
		fastMutalisk_Z,
		lurker_Z,
		blockTheFirstChokePoint_T,
		blockTheSecondChokePoint_T,
		vulture_Galia_Tank_T,
		darkTemplar_P,
		blockDefence2Dragon8_P,
		blockTheFirstChokePoint_P,
		blockTheSecondChokePoint_P,
		carrier_P
	};	
	
	public void techTreeupAgainstProtoss(){
		
		// sc76.choi 기본 Spawning Pool 테크 작성예정
		
		
		// sc76.choi 기본 Lair 테크
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Lair) == 0 && myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) == 0)
		    && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12
			&& myPlayer.getUpgradeLevel(UpgradeType.Muscular_Augments) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Grooved_Spines) > 0
			&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
			// sc76.choi Hive 진행 중이면 Lair를 또 가면 안된다.
			if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0 ||
				(myPlayer.completedUnitCount(UnitType.Zerg_Hive) + myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 ||
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0 ||
				ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) > 0
				)
			{
			}else{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
			}
		}
		
		// sc76.choi 빠른 Lair 테크(상황에 따라) 작성 예정
		// sc76.choi 빠른 Lair 테크(상황에 따라) 작성 예정
		if (StrategyManager.Instance().buildState == StrategyManager.BuildState.darkTemplar_P 
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Lair) == 0 && myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) == 0)
			&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
			// sc76.choi Hive 진행 중이면 Lair를 또 가면 안된다.
			if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0 ||
				(myPlayer.completedUnitCount(UnitType.Zerg_Hive) + myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 ||
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0 ||
				ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) > 0
				)
			{
			}else{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
			}
		}
		
		// sc76.choi 기본 spire 테크 작성예정
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
//				&& InformationManager.Instance().getTotalHatcheryCount() >= 3
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
				&& myPlayer.allUnitCount(UnitType.Zerg_Spire) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Spire) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Spire, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spire, true);
		}
		
		// sc76.choi 빠른 spire 테크 작성예정					
		
		// sc76.choi 기본 greater spire 테크 작성예정
		if (StrategyManager.Instance().myKilledCombatUnitCount4 >= 5
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
				&& myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
				&& myPlayer.allUnitCount(UnitType.Zerg_Greater_Spire) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Greater_Spire) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Greater_Spire, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Greater_Spire, true);
		}
		
		// sc76.choi 기본 Queens_Nest
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 10
			&& myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			&& myPlayer.allUnitCount(UnitType.Zerg_Queens_Nest) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Queens_Nest) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Queens_Nest, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Queens_Nest, true);
		}
		
		// sc76.choi 빠른  Queens_Nest 테크(상황에 따라) 작성 예정
		
		// sc76.choi 기본  Hive 테크(상황에 따라) 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
			&& myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Hive) == 0 || myPlayer.incompleteUnitCount(UnitType.Zerg_Hive) == 0)
			&& myPlayer.allUnitCount(UnitType.Zerg_Hive) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hive, true);
		}

		// sc76.choi 기본 디파일러 마운트 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType2
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 10
			&& myPlayer.allUnitCount(UnitType.Zerg_Defiler_Mound) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Defiler_Mound) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Defiler_Mound, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Defiler_Mound, true);
		}
		
		// sc76.choi 기본 울트라 카벤 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 10
		    && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8				
			&& myPlayer.allUnitCount(UnitType.Zerg_Ultralisk_Cavern) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Ultralisk_Cavern) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Ultralisk_Cavern, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Ultralisk_Cavern, true);
		}
	
	}
	
	public void techTreeupAgainstZerg(){
		
		// sc76.choi 기본 Spawning Pool 테크 작성예정
		
		// sc76.choi 기본 Lair 테크
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) >= 3
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Lair) == 0 && myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) == 0)
		    && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
		    && myPlayer.getUpgradeLevel(UpgradeType.Grooved_Spines) > 0 // 히드라 발업
//		    && myPlayer.getUpgradeLevel(UpgradeType.Muscular_Augments) > 0 // 히드라 사정업		    
			&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
			// sc76.choi Hive 진행 중이면 Lair를 또 가면 안된다.
			if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0 ||
				(myPlayer.completedUnitCount(UnitType.Zerg_Hive) + myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 ||
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0 ||
				ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) > 0
				)
			{
			}else{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
			}
		}
		
		// sc76.choi 빠른 Lair 테크(상황에 따라) 작성 예정
		if (StrategyManager.Instance().buildState == StrategyManager.BuildState.lurker_Z 
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Lair) == 0 && myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) == 0)
			&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
			// sc76.choi Hive 진행 중이면 Lair를 또 가면 안된다.
			if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0 ||
				(myPlayer.completedUnitCount(UnitType.Zerg_Hive) + myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 ||
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0 ||
				ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) > 0
				)
			{
			}else{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
			}
		}
		
		// sc76.choi 기본 spire 테크 작성예정
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
//				&& InformationManager.Instance().getTotalHatcheryCount() >= 3
				&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType1
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
				&& myPlayer.allUnitCount(UnitType.Zerg_Spire) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Spire) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Spire, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spire, true);
		}
		
		// sc76.choi 빠른 spire 테크 작성예정					
		
		// sc76.choi 기본 greater spire 테크 작성예정
		if (StrategyManager.Instance().myKilledCombatUnitCount4 >= 5
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				&& myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
				&& myPlayer.allUnitCount(UnitType.Zerg_Greater_Spire) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Greater_Spire) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Greater_Spire, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Greater_Spire, true);
		}
		
		// sc76.choi 기본 Queens_Nest
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= StrategyManager.Instance().necessaryNumberOfCombatUnitType1			
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
			&& myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			&& myPlayer.allUnitCount(UnitType.Zerg_Queens_Nest) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Queens_Nest) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Queens_Nest, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Queens_Nest, true);
		}
		
		// sc76.choi 빠른  Queens_Nest 테크(상황에 따라) 작성 예정
		
		// sc76.choi 기본  Hive 테크(상황에 따라) 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType1				
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
			&& myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Hive) == 0 || myPlayer.incompleteUnitCount(UnitType.Zerg_Hive) == 0)
			&& myPlayer.allUnitCount(UnitType.Zerg_Hive) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hive, true);
		}

		// sc76.choi 기본 디파일러 마운트 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType1				
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType2
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 10
			&& myPlayer.allUnitCount(UnitType.Zerg_Defiler_Mound) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Defiler_Mound) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Defiler_Mound, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Defiler_Mound, true);
		}
		
		// sc76.choi 기본 디파일러 마운트 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 10
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType2			
			&& myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0
			&& myPlayer.allUnitCount(UnitType.Zerg_Ultralisk_Cavern) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Ultralisk_Cavern) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Ultralisk_Cavern, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Ultralisk_Cavern, true);
		}
	
	}
	
	public void techTreeupAgainstTerran(){
		
		// sc76.choi 기본 Spawning Pool 테크 작성예정
		// sc76.choi 기본 Lair 테크
		// sc76.choi 테란은 빠른 레어를 간다.
		if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) > 0
				&& (myPlayer.completedUnitCount(UnitType.Zerg_Lair) == 0 && myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) == 0)
//			    && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4
				&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
				// sc76.choi Hive 진행 중이면 Lair를 또 가면 안된다.
				if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0 ||
					(myPlayer.completedUnitCount(UnitType.Zerg_Hive) + myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 ||
					BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0 ||
					ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) > 0
					)
				{
				}else{
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
				}
		}
		
		// sc76.choi 빠른 Lair 테크(상황에 따라) 작성 예정
		
		
		// sc76.choi 기본 spire 테크 작성예정
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
//				&& InformationManager.Instance().getTotalHatcheryCount() >= 3
				&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
				&& myPlayer.allUnitCount(UnitType.Zerg_Spire) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Spire) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Spire, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spire, true);
		}
		
		// sc76.choi 빠른 spire 테크 작성예정	
		
		// sc76.choi 기본 Queens_Nest
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		// sc76.choi 기본 Queens_Nest
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			&& myPlayer.allUnitCount(UnitType.Zerg_Queens_Nest) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Queens_Nest) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Queens_Nest, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Queens_Nest, true);
		}
		
		// sc76.choi 빠른  Queens_Nest 테크(상황에 따라) 작성 : 럴커가 너무 많이 죽으면 빠른 하이브를 가기 위한 준비를 한다.
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
				&& StrategyManager.Instance().myKilledCombatUnitCount3 >= 5
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8				
				&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				&& myPlayer.allUnitCount(UnitType.Zerg_Queens_Nest) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Queens_Nest) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Queens_Nest, null) == 0) 
		{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Queens_Nest, true);
		}
		
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType1
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 1			
			&& myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0
			&& myPlayer.allUnitCount(UnitType.Zerg_Hive) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hive, true);
		}

		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= StrategyManager.Instance().necessaryNumberOfDefenceUnitType1
			&& myPlayer.allUnitCount(UnitType.Zerg_Defiler_Mound) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Defiler_Mound) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Defiler_Mound, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Defiler_Mound, true);
		}
	
		// sc76.choi 기본 greater spire 테크 작성예정
		if (StrategyManager.Instance().myKilledCombatUnitCount4 >= 5
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				&& myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
				&& myPlayer.completedUnitCount(UnitType.Zerg_Defiler) >= 1
				&& myPlayer.allUnitCount(UnitType.Zerg_Greater_Spire) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Greater_Spire) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Greater_Spire, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Greater_Spire, true);
		}
		
		// sc76.choi 기본 울트라 카벤 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 10
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 4	
			&& myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0
			&& myPlayer.allUnitCount(UnitType.Zerg_Ultralisk_Cavern) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Ultralisk_Cavern) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Ultralisk_Cavern, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Ultralisk_Cavern, true);
		}
	}	
}
