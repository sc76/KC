import bwapi.Player;
import bwapi.Race;
import bwapi.UnitType;

public class KCTechTreeUp {

	Player myPlayer = MyBotModule.Broodwar.self();
	Race myRace = MyBotModule.Broodwar.self().getRace();
	
	public void techTreeupAgainstProtoss(){
		
		// sc76.choi 기본 Spawning Pool 테크 작성예정
		
		
		// sc76.choi 기본 Lair 테크
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lair) <= 0
			&& myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) <= 0
		    && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12
			&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
			// sc76.choi Hive 진행 중이면 Lair를 또 가면 안된다.
			if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0 ||
				(myPlayer.completedUnitCount(UnitType.Zerg_Hive) + myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 ||
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0 ||
				ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) > 0)
			{
			}else{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
			}
		}
		
		// sc76.choi 빠른 Lair 테크(상황에 따라) 작성 예정
		
		// sc76.choi 기본 spire 테크 작성예정
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
				&& (myPlayer.completedUnitCount(UnitType.Zerg_Hatchery) + 
					myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
					myPlayer.completedUnitCount(UnitType.Zerg_Hive)
				   ) >= 3
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4
				&& myPlayer.allUnitCount(UnitType.Zerg_Spire) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Spire) == 0
				&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Spire, null) == 0) 
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Spire, true);
			}
		
		// sc76.choi 빠른 spire 테크 작성예정					
		
		
		// sc76.choi 기본 Queens_Nest
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4
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
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 6
			&& myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0
			&& myPlayer.allUnitCount(UnitType.Zerg_Hive) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hive, true);
		}

		// sc76.choi 기본 디파일러 마운트 작성 예정
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4
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
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 10
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
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lair) <= 0
			&& myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) <= 0
		    && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12
			&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
			// Hive 진행 중이면 Lair를 또 가면 안된다.
			if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0
					|| BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0
					|| ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) > 0)
			{
			}else{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
			}
		}
		
		// sc76.choi 빠른 Lair 테크(상황에 따라) 작성 예정
		
		// sc76.choi 기본 Queens_Nest
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 4
			&& myPlayer.allUnitCount(UnitType.Zerg_Queens_Nest) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Queens_Nest) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Queens_Nest, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Queens_Nest, true);
		}
		
		// sc76.choi 빠른  Queens_Nest 테크(상황에 따라) 작성 예정
		
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 6
			&& myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0
			&& myPlayer.allUnitCount(UnitType.Zerg_Hive) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hive, true);
		}

		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 16
			&& myPlayer.allUnitCount(UnitType.Zerg_Defiler_Mound) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Defiler_Mound) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Defiler_Mound, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Defiler_Mound, true);
		}
	
	}
	
	public void techTreeupAgainstTerran(){

		
		// sc76.choi 기본 Spawning Pool 테크 작성예정
		// sc76.choi 기본 Lair 테크
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lair) <= 0
			&& myPlayer.incompleteUnitCount(UnitType.Zerg_Lair) <= 0
		    && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12
			&& myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0) 
		{
			// Hive 진행 중이면 Lair를 또 가면 안된다.
			if (myPlayer.allUnitCount(UnitType.Zerg_Hive) > 0
					|| BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) > 0
					|| ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) > 0)
			{
			}else{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, true);
			}
		}
		
		// sc76.choi 빠른 Lair 테크(상황에 따라) 작성 예정
		
		// sc76.choi 기본 Queens_Nest
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 4
			&& myPlayer.allUnitCount(UnitType.Zerg_Queens_Nest) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Queens_Nest) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Queens_Nest, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Queens_Nest, true);
		}
		
		// sc76.choi 빠른  Queens_Nest 테크(상황에 따라) 작성 예정
		
		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 12기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Lair) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 6
			&& myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0
			&& myPlayer.allUnitCount(UnitType.Zerg_Hive) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Hive, true);
		}

		// 고급 건물 생산을 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라리스크 4기 생산 후 건설한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 16
			&& myPlayer.allUnitCount(UnitType.Zerg_Defiler_Mound) == 0
			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Defiler_Mound) == 0
			&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Defiler_Mound, null) == 0) 
		{
			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Defiler_Mound, true);
		}
	
	}	
}
