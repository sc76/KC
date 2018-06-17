import bwapi.Player;
import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;


public class KCUpgradeAndTech {

	Player myPlayer = MyBotModule.Broodwar.self();
	Race myRace = MyBotModule.Broodwar.self().getRace();
	
	// 업그레이드 및 리서치 대상 설정
	UpgradeType necessaryUpgradeType1 = UpgradeType.Grooved_Spines; // 히드라 사정업
	UpgradeType necessaryUpgradeType2 = UpgradeType.Muscular_Augments; // 히드라 발업
	UpgradeType necessaryUpgradeType3 = UpgradeType.Pneumatized_Carapace; // 오버로드 속도업
	
	TechType necessaryTechType1 = TechType.Lurker_Aspect; // 럴커
	TechType necessaryTechType2 = TechType.Consume; // 컨슘
	TechType necessaryTechType3 = TechType.Plague; // 플레이그
	
	public void upGradeAndTechAgainstProtoss(){
		boolean			isTimeToStartUpgradeType1 = false;	/// 업그레이드할 타이밍인가, 히드라 사정 업
		boolean			isTimeToStartUpgradeType2 = false;	/// 업그레이드할 타이밍인가, 히드라 발업
		boolean			isTimeToStartUpgradeType3 = false;	/// 업그레이드할 타이밍인가, 오버로드 속도업
		boolean			isTimeToStartResearchTech1 = false;	/// 리서치할 타이밍인가, 럴커
		boolean			isTimeToStartResearchTech2 = false;	/// 리서치할 타이밍인가
		boolean			isTimeToStartResearchTech3 = false;	/// 리서치할 타이밍인가


		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라 4기 생산 후 업그레이드한다
		// 히드라 사정 업그레이드
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4) {
			isTimeToStartUpgradeType1 = true;
		}
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라 사정거리 업그레이드 후 업그레이드한다
		// 히드라 발업
		if (myPlayer.getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
			isTimeToStartUpgradeType2 = true;
		}			
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 러커 리서치 후 업그레이드한다
		// sc76.choi 오버로드 속도업
		// sc76.choi myPlayer.hasResearched(TechType.Lurker_Aspect) 조건을 제거 했다. 이동속도는 빠르게 연구한다.
		// sc76.choi 럴커가 하나라도 있다면, 빠른 드랍을 위해 업그레이드 한다.
		// sc76.choi  myPlayer.hasResearched(necessaryTechType1) 럴커가 연구와 동시에 오버로드 속도업을 한다.
		// sc76.choi Lair 갯수를 체크 할때는 Hive(completedUnitCount, incompleteUnitCount)도 같이 체크를 해야 한다.
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
				&& myPlayer.isResearching(necessaryTechType1) == true) {
			isTimeToStartUpgradeType3 = true;
		}			
		// 러커는 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
			  && (myPlayer.completedUnitCount(UnitType.Zerg_Lair) +
				  myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				  myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0) {
			isTimeToStartResearchTech1 = true;
		}
		// 컨슘은 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0) {
			isTimeToStartResearchTech2 = true;
		}			
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 컨슘 리서치 후 리서치한다
		// 컨슘
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0
				&& myPlayer.hasResearched(necessaryTechType2) == true) {
			isTimeToStartResearchTech3 = true;
		}			
		
		
		// 테크 리서치는 높은 우선순위로 우선적으로 실행
		// 럴커
		if (isTimeToStartResearchTech1) 
		{
			if (myPlayer.isResearching(necessaryTechType1) == false
				&& myPlayer.hasResearched(necessaryTechType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType1, true);
			}
		}
		
		// 컨슘
		if (isTimeToStartResearchTech2) 
		{
			if (myPlayer.isResearching(necessaryTechType2) == false
				&& myPlayer.hasResearched(necessaryTechType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType2, true);
			}
		}
		
		// 플레이그
		if (isTimeToStartResearchTech3) 
		{
			if (myPlayer.isResearching(necessaryTechType3) == false
				&& myPlayer.hasResearched(necessaryTechType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType3, true);
			}
		}		
		
		// 업그레이드는 낮은 우선순위로 실행
		// sc76.choi 히드라 사정 업그레이드
		if (isTimeToStartUpgradeType1) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType1) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType1, false);
			}
		}
		
		
		// 히드라 발업
		if (isTimeToStartUpgradeType2) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType2) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType2, false);
			}
		}

		// 오버로드 속도업 
		// sc76.choi necessaryUpgradeType3 : Pneumatized_Carapace
		if (isTimeToStartUpgradeType3) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType3) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType3, true);
			}
		}

		// BWAPI 4.1.2 의 버그때문에, 오버로드 업그레이드를 위해서는 반드시 Zerg_Lair 가 있어야함	
		// sc76.choi 오버로드 이동속도
		//if (myRace == Race.Zerg) {
		//	if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Pneumatized_Carapace) > 0) {
		//		if (myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0 
		//			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
		//			&& (myPlayer.allUnitCount(UnitType.Zerg_Hive) < 1
		//			|| BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) < 1)) 
		//		{
		//			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, false);					
		//		}
		//	}
		//}
	}

	public void upGradeAndTechAgainstZerg(){
		boolean			isTimeToStartUpgradeType1 = false;	/// 업그레이드할 타이밍인가, 히드라 사정 업
		boolean			isTimeToStartUpgradeType2 = false;	/// 업그레이드할 타이밍인가, 히드라 발업
		boolean			isTimeToStartUpgradeType3 = false;	/// 업그레이드할 타이밍인가, 오버로드 속도업
		boolean			isTimeToStartResearchTech1 = false;	/// 리서치할 타이밍인가, 럴커
		boolean			isTimeToStartResearchTech2 = false;	/// 리서치할 타이밍인가
		boolean			isTimeToStartResearchTech3 = false;	/// 리서치할 타이밍인가


		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라 4기 생산 후 업그레이드한다
		// 히드라 사정 업그레이드
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4) {
			isTimeToStartUpgradeType1 = true;
		}
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라 사정거리 업그레이드 후 업그레이드한다
		// 히드라 발업
		if (myPlayer.getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
			isTimeToStartUpgradeType2 = true;
		}			
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 러커 리서치 후 업그레이드한다
		// sc76.choi 오버로드 속도업
		// sc76.choi myPlayer.hasResearched(TechType.Lurker_Aspect) 조건을 제거 했다. 이동속도는 빠르게 연구한다.
		// sc76.choi 럴커가 하나라도 있다면, 빠른 드랍을 위해 업그레이드 한다.
		// sc76.choi  myPlayer.hasResearched(necessaryTechType1) 럴커가 연구와 동시에 오버로드 속도업을 한다.
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
				&& myPlayer.isResearching(necessaryTechType1) == true) {
			isTimeToStartUpgradeType3 = true;
		}			
		// 러커는 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
			  && (myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				  myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				  myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0) {
			isTimeToStartResearchTech1 = true;
		}
		// 컨슘은 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0) {
			isTimeToStartResearchTech2 = true;
		}			
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 컨슘 리서치 후 리서치한다
		// 컨슘
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0
				&& myPlayer.hasResearched(necessaryTechType2) == true) {
			isTimeToStartResearchTech3 = true;
		}			
		
		
		// 테크 리서치는 높은 우선순위로 우선적으로 실행
		// 럴커
		if (isTimeToStartResearchTech1) 
		{
			if (myPlayer.isResearching(necessaryTechType1) == false
				&& myPlayer.hasResearched(necessaryTechType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType1, true);
			}
		}
		
		// 컨슘
		if (isTimeToStartResearchTech2) 
		{
			if (myPlayer.isResearching(necessaryTechType2) == false
				&& myPlayer.hasResearched(necessaryTechType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType2, true);
			}
		}
		
		// 플레이그
		if (isTimeToStartResearchTech3) 
		{
			if (myPlayer.isResearching(necessaryTechType3) == false
				&& myPlayer.hasResearched(necessaryTechType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType3, true);
			}
		}		
		
		// 업그레이드는 낮은 우선순위로 실행
		// sc76.choi 히드라 사정 업그레이드
		if (isTimeToStartUpgradeType1) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType1) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType1, false);
			}
		}
		
		
		// 히드라 발업
		if (isTimeToStartUpgradeType2) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType2) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType2, false);
			}
		}

		// 오버로드 속도업
		if (isTimeToStartUpgradeType3) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType3) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType3, true);
			}
		}

		// BWAPI 4.1.2 의 버그때문에, 오버로드 업그레이드를 위해서는 반드시 Zerg_Lair 가 있어야함	
		// sc76.choi 오버로드 이동속도
		//if (myRace == Race.Zerg) {
		//	if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Pneumatized_Carapace) > 0) {
		//		if (myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0 
		//			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
		//			&& (myPlayer.allUnitCount(UnitType.Zerg_Hive) < 1
		//			|| BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) < 1)) 
		//		{
		//			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, false);					
		//		}
		//	}
		//}
	}
	
	public void upGradeAndTechAgainstTerran(){
		boolean			isTimeToStartUpgradeType1 = false;	/// 업그레이드할 타이밍인가, 히드라 사정 업
		boolean			isTimeToStartUpgradeType2 = false;	/// 업그레이드할 타이밍인가, 히드라 발업
		boolean			isTimeToStartUpgradeType3 = false;	/// 업그레이드할 타이밍인가, 오버로드 속도업
		boolean			isTimeToStartResearchTech1 = false;	/// 리서치할 타이밍인가, 럴커
		boolean			isTimeToStartResearchTech2 = false;	/// 리서치할 타이밍인가
		boolean			isTimeToStartResearchTech3 = false;	/// 리서치할 타이밍인가


		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라 4기 생산 후 업그레이드한다
		// 히드라 사정 업그레이드
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 4) {
			isTimeToStartUpgradeType1 = true;
		}
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 히드라 사정거리 업그레이드 후 업그레이드한다
		// 히드라 발업
		if (myPlayer.getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
			isTimeToStartUpgradeType2 = true;
		}			
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 러커 리서치 후 업그레이드한다
		// sc76.choi 오버로드 속도업
		// sc76.choi myPlayer.hasResearched(TechType.Lurker_Aspect) 조건을 제거 했다. 이동속도는 빠르게 연구한다.
		// sc76.choi 럴커가 하나라도 있다면, 빠른 드랍을 위해 업그레이드 한다.
		// sc76.choi  myPlayer.hasResearched(necessaryTechType1) 럴커가 연구와 동시에 오버로드 속도업을 한다.
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
				&& myPlayer.isResearching(necessaryTechType1) == true) {
			isTimeToStartUpgradeType3 = true;
		}			
		// 러커는 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
			  && (myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				  myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				  myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0) {
			isTimeToStartResearchTech1 = true;
		}
		// 컨슘은 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0) {
			isTimeToStartResearchTech2 = true;
		}			
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 컨슘 리서치 후 리서치한다
		// 컨슘
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0
				&& myPlayer.hasResearched(necessaryTechType2) == true) {
			isTimeToStartResearchTech3 = true;
		}			
		
		
		// 테크 리서치는 높은 우선순위로 우선적으로 실행
		// 럴커
		System.out.println(" isTimeToStartResearchTech1 :  " + isTimeToStartResearchTech1);
		System.out.println(" myPlayer.isResearching(necessaryTechType1) :  " + myPlayer.isResearching(necessaryTechType1));
		System.out.println(" myPlayer.hasResearched(necessaryTechType1) :  " + myPlayer.hasResearched(necessaryTechType1));
		System.out.println(" BuildManager.Instance().buildQueue.getItemCount(necessaryTechType1) :  " + BuildManager.Instance().buildQueue.getItemCount(necessaryTechType1));
		if (isTimeToStartResearchTech1) 
		{
			if (myPlayer.isResearching(necessaryTechType1) == false
				&& myPlayer.hasResearched(necessaryTechType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType1, true);
			}
		}
		
		// 컨슘
		if (isTimeToStartResearchTech2) 
		{
			if (myPlayer.isResearching(necessaryTechType2) == false
				&& myPlayer.hasResearched(necessaryTechType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType2, true);
			}
		}
		
		// 플레이그
		if (isTimeToStartResearchTech3) 
		{
			if (myPlayer.isResearching(necessaryTechType3) == false
				&& myPlayer.hasResearched(necessaryTechType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType3, true);
			}
		}		
		
		// 업그레이드는 낮은 우선순위로 실행
		// sc76.choi 히드라 사정 업그레이드
		if (isTimeToStartUpgradeType1) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType1) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType1, false);
			}
		}
		
		
		// 히드라 발업
		if (isTimeToStartUpgradeType2) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType2) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType2, false);
			}
		}

		// 오버로드 속도업
		if (isTimeToStartUpgradeType3) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType3) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType3, true);
			}
		}

		// BWAPI 4.1.2 의 버그때문에, 오버로드 업그레이드를 위해서는 반드시 Zerg_Lair 가 있어야함	
		// sc76.choi 오버로드 이동속도
		//if (myRace == Race.Zerg) {
		//	if (BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Pneumatized_Carapace) > 0) {
		//		if (myPlayer.allUnitCount(UnitType.Zerg_Lair) == 0 
		//			&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Lair) == 0
		//			&& (myPlayer.allUnitCount(UnitType.Zerg_Hive) < 1
		//			|| BuildManager.Instance().buildQueue.getItemCount(UnitType.Zerg_Hive) < 1)) 
		//		{
		//			BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Zerg_Lair, false);					
		//		}
		//	}
		//}
	}
}
