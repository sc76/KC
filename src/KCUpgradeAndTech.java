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
	UpgradeType necessaryUpgradeType4 = UpgradeType.Adrenal_Glands; // 저글링 아드레날린
	UpgradeType necessaryUpgradeType5 = UpgradeType.Ventral_Sacs; // 오버로드 수송업
	
	TechType necessaryTechType1 = TechType.Lurker_Aspect; // 럴커
	TechType necessaryTechType2 = TechType.Consume; // 컨슘
	TechType necessaryTechType3 = TechType.Plague; // 플레이그
	TechType necessaryTechType4 = TechType.Spawn_Broodlings; // 브루들링
	
	
	boolean			isTimeToStartUpgradeType1 = false;	/// 업그레이드할 타이밍인가, 히드라 사정 업
	boolean			isTimeToStartUpgradeType2 = false;	/// 업그레이드할 타이밍인가, 히드라 발업
	boolean			isTimeToStartUpgradeType3 = false;	/// 업그레이드할 타이밍인가, 오버로드 속도업
	boolean			isTimeToStartUpgradeType4 = false;	/// 업그레이드할 타이밍인가, 저글링 아드레날린
	boolean			isTimeToStartUpgradeType5 = false;	/// 업그레이드할 타이밍인가, 오버로드 수송업
	
	boolean			isTimeToStartResearchTech1 = false;	/// 리서치할 타이밍인가, 럴커
	boolean			isTimeToStartResearchTech2 = false;	/// 리서치할 타이밍인가, 디파일러 컴슘
	boolean			isTimeToStartResearchTech3 = false;	/// 리서치할 타이밍인가, 플레이그
	boolean			isTimeToStartResearchTech4 = false;	/// 리서치할 타이밍인가, 브루들링
	
	public void upGradeAndTechAgainstProtoss(){
		// sc76.choi 프로토스 일때는 발업 먼저 업그레이드 후, 사정거리 업그레이드와 저글링 발업을 같이 진행한다.
		// 히드라 사정 업그레이드
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.getUpgradeLevel(UpgradeType.Muscular_Augments) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8) {
			isTimeToStartUpgradeType1 = true;
		}
		
		// 히드라 발업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8) {
			isTimeToStartUpgradeType2 = true;
		}
		
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 러커 리서치 후 업그레이드한다
		// sc76.choi 오버로드 속도업
		// sc76.choi myPlayer.hasResearched(TechType.Lurker_Aspect) 조건을 제거 했다. 이동속도는 빠르게 연구한다.
		// sc76.choi 럴커가 하나라도 있다면, 빠른 드랍을 위해 업그레이드 한다.(KTH 수송업 업그레이드 먼저 하도록 추가)
		// sc76.choi  myPlayer.hasResearched(necessaryTechType1) 럴커가 연구와 동시에 오버로드 속도업을 한다.
		// sc76.choi Lair 갯수를 체크 할때는 Hive(completedUnitCount, incompleteUnitCount)도 같이 체크를 해야 한다.
		
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) +
						myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
						myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
//				&& myPlayer.isResearching(necessaryTechType1) == true
			) {
			isTimeToStartUpgradeType3 = true;
		}	
		
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) +
				myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
				&& myPlayer.isResearching(necessaryTechType1) == true
  			    && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2				
		) {
			isTimeToStartUpgradeType5 = true;
		}			
		
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0 
			  && myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
		) {
			isTimeToStartUpgradeType4 = true;
		}
		
		// 러커는 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0 
			  && myPlayer.getUpgradeLevel(UpgradeType.Muscular_Augments) > 0 
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
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType2, false);
			}
		}
		
		// 플레이그
		if (isTimeToStartResearchTech3) 
		{
			if (myPlayer.isResearching(necessaryTechType3) == false
				&& myPlayer.hasResearched(necessaryTechType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType3, false);
			}
		}		

		
		// 업그레이드는 낮은 우선순위로 실행
		// sc76.choi 히드라 사정 업그레이드
		// sc76.choi 챔버 방어 업그레이드
		if (isTimeToStartUpgradeType1) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType1) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType1, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost, false); // 저글링 속도업(Faster Zergling movement)
			}
		}
		
		// 히드라 발업
		// sc76.choi 챔버 공격 업그레이드		
		if (isTimeToStartUpgradeType2) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType2) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType2, true);
				
			}
		}

		// 오버로드 수송업 
		// KTH necessaryUpgradeType5 : Ventral_Sacs
		if (isTimeToStartUpgradeType5) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType5) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType5) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType5) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType5, false);
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
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryUpgradeType3, false);
			}
		}
		
		// 저글링 아드레날린
		if (isTimeToStartUpgradeType4) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType4) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType4) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType4) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType4, true);
			}
		}		

		// 뮤탈 방어 1 업
		if (StrategyManager.Instance().myKilledCombatUnitCount3 < 10
			  && myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
			  && myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Carapace) == 0
			  && myPlayer.isUpgrading(UpgradeType.Zerg_Flyer_Carapace) == false
			  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Flyer_Carapace) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Carapace, false);
		}	
		
		// 뮤탈 공격 1 업
		if (StrategyManager.Instance().myKilledCombatUnitCount3 < 10
			  && myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
			  && myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Carapace) > 0
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Attacks) == 0
			  && myPlayer.isUpgrading(UpgradeType.Zerg_Flyer_Attacks) == false
			  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Flyer_Attacks) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Attacks, false);
		}
		
		// 울트라 벙어 1업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) == 0
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}	
		
		// 울트라 벙어 2업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) > 0
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}
		
		// 울트라 벙어 3업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) > 1
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}		
	}
	
	/**
	 * sc76.choi Evolution Chamber의 기본 업그레이드를 컨트롤 한다.
	 * 
	 * @author sc76.choi
	 */
	// Zerg_Carapace 방어 업그레이드
	// Zerg_Missile_Attacks 히드라 공격 업그레이드
	// Zerg_Melee_Attacks 저글링 공격 업그레이드
	public void chamberUpgradeAgainstProtoss(){
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// Zerg_Carapace 지상 갑피 업그레이드 1 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 2 || myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 2)
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, true);
		}
		
		// Zerg_Carapace 지상 갑피 업그레이드  2 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 0 // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 0  // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);
		}
		
		// Zerg_Carapace 지상 갑피 업그레이드 3 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 1 // 다른 업그레이드가 2레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 1  // 다른 업그레이드가 2레벨 올라왔으면			
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 1 단계 (히드라리스크, 러커)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 2 단계 (히드라리스크, 러커)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 1 // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 0  // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 3 단계 (히드라리스크, 러커)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 2
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 1
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 1 단계 (저글링, 울트라리스크, 브루들링)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}
		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 2 단계 (저글링, 울트라리스크, 브루들링)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 1
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}
		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 3 단계 (저글링, 울트라리스크, 브루들링)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 2
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}		
				
	}

	public void upGradeAndTechAgainstZerg(){

		// 히드라 사정 업그레이드
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.getUpgradeLevel(UpgradeType.Muscular_Augments) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8) {
			isTimeToStartUpgradeType1 = true;
		}
		
		// 히드라 발업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 8) {
			isTimeToStartUpgradeType2 = true;
		}
		
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 러커 리서치 후 업그레이드한다
		// sc76.choi 오버로드 속도업
		// sc76.choi myPlayer.hasResearched(TechType.Lurker_Aspect) 조건을 제거 했다. 이동속도는 빠르게 연구한다.
		// sc76.choi 럴커가 하나라도 있다면, 빠른 드랍을 위해 업그레이드 한다.(KTH 수송업 업그레이드 먼저 하도록 추가)
		// sc76.choi  myPlayer.hasResearched(necessaryTechType1) 럴커가 연구와 동시에 오버로드 속도업을 한다.
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
//				&& myPlayer.isResearching(necessaryTechType1) == true
			) {
			isTimeToStartUpgradeType3 = true;
		}		
		
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
			  myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
			  myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
			  && myPlayer.isResearching(necessaryTechType1) == true
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2				
		) {
			isTimeToStartUpgradeType5 = true;
		}		
		
		// 저글링 아드레날린
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0 
				  && myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0
			) {
				isTimeToStartUpgradeType4 = true;
		}
		
		// 저글링 아드레날린
		if (isTimeToStartUpgradeType4) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType4) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType4) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType4) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType4, true);
			}
		}	
		
		// 러커는 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			  && myPlayer.getUpgradeLevel(necessaryUpgradeType2) > 0 // 히드라 속도 업 후에
			  && (myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				  myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				  myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0) {
			isTimeToStartResearchTech1 = true;
		}
		
		// 컨슘은 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0) {
			isTimeToStartResearchTech2 = true;
		}
		
		// 플레이그
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
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType1, true);
			}
		}
		
		
		// 히드라 발업
		if (isTimeToStartUpgradeType2) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType2) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType2, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost); // 저글링 속도업(Faster Zergling movement)
			}
		}

		// 오버로드 속도업
		if (isTimeToStartUpgradeType3) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType3) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType3) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryUpgradeType3, false);
			}
		}

		// 뮤탈 방어 1 업
		if (StrategyManager.Instance().myKilledCombatUnitCount3 < 10
			  && myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
			  && myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Carapace) == 0
			  && myPlayer.isUpgrading(UpgradeType.Zerg_Flyer_Carapace) == false
			  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Flyer_Carapace) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Carapace, false);
		}	
		
		// 뮤탈 공격 1 업
		if (StrategyManager.Instance().myKilledCombatUnitCount3 < 10
			  && myPlayer.completedUnitCount(UnitType.Zerg_Spire) > 0
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
			  && myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Carapace) > 0
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Attacks) == 0
			  && myPlayer.isUpgrading(UpgradeType.Zerg_Flyer_Attacks) == false
			  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Flyer_Attacks) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Attacks, false);
		}
		
		// 울트라 벙어 1업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) == 0
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}	
		
		// 울트라 벙어 2업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) > 0
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}
		
		// 울트라 벙어 3업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) > 1
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}		
	}
	
	public void chamberUpgradeAgainstZerg(){
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// Zerg_Carapace 지상 갑피 업그레이드 1 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 2 
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, true);
		}
		
		// Zerg_Carapace 지상 갑피 업그레이드  2 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 2 
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12			
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 0 // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 0  // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);
		}
		
		// Zerg_Carapace 지상 갑피 업그레이드 3 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 1 // 다른 업그레이드가 2레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 1  // 다른 업그레이드가 2레벨 올라왔으면			
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 1 단계 (히드라리스크, 러커)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 2 
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12				
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 2 단계 (히드라리스크, 러커)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12			
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 1 // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 0  // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 3 단계 (히드라리스크, 러커)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 12			
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 2
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 1
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 1 단계 (저글링, 울트라리스크, 브루들링)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}
		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 2 단계 (저글링, 울트라리스크, 브루들링)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 1
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}
		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 3 단계 (저글링, 울트라리스크, 브루들링)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 2
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}		
				
	}
	
	public void upGradeAndTechAgainstTerran(){

		// sc76.choi 히드라 사정 업그레이드, 테란은 럴커를 먼저 업그레이드 한다.
		if (isTimeToStartResearchTech1
				&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
				&& myPlayer.getUpgradeLevel(UpgradeType.Muscular_Augments) > 0				
		) {
			isTimeToStartUpgradeType1 = true;
		}
		
		// sc76.choi 히드라 발업, 테란은 럴커를 먼저 업그레이드 한다.
		if (isTimeToStartResearchTech1
			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
//			&& myPlayer.getUpgradeLevel(UpgradeType.Grooved_Spines) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 1) {
			isTimeToStartUpgradeType2 = true;
		}
		
		// 업그레이드 / 리서치를 너무 성급하게 하다가 위험에 빠질 수 있으므로, 최소 러커 리서치 후 업그레이드한다
		// sc76.choi 오버로드 속도업
		// sc76.choi myPlayer.hasResearched(TechType.Lurker_Aspect) 조건을 제거 했다. 이동속도는 빠르게 연구한다.
		// sc76.choi 럴커가 하나라도 있다면, 빠른 드랍을 위해 업그레이드 한다.(KTH 수송업 업그레이드 먼저 하도록 추가)
		// sc76.choi  myPlayer.hasResearched(necessaryTechType1) 럴커가 연구와 동시에 오버로드 속도업을 한다.
		
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				myPlayer.completedUnitCount(UnitType.Zerg_Hive) + 
				myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
				&& myPlayer.isResearching(necessaryTechType1) == true) {
			isTimeToStartUpgradeType3 = true;
		}		
		
		if ((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				myPlayer.completedUnitCount(UnitType.Zerg_Hive) + 
				myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0 
			  && myPlayer.isResearching(necessaryTechType1) == true
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2) {
			isTimeToStartUpgradeType5 = true;
		}
		
		// 저글링 아드레 날린
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0 
			  && myPlayer.completedUnitCount(UnitType.Zerg_Spawning_Pool) > 0) {
			isTimeToStartUpgradeType4 = true;
		}
		
		// 러커는 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
//			  && myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk) >= 1
			  && (myPlayer.completedUnitCount(UnitType.Zerg_Lair) + 
				  myPlayer.completedUnitCount(UnitType.Zerg_Hive) +
				  myPlayer.incompleteUnitCount(UnitType.Zerg_Hive)) > 0) {
			isTimeToStartResearchTech1 = true;
		}
		
		// 컨슘은 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0) {
			isTimeToStartResearchTech2 = true;
		}			

		// 플레이그
		if (myPlayer.completedUnitCount(UnitType.Zerg_Defiler_Mound) > 0
				&& myPlayer.hasResearched(necessaryTechType2) == true) {
			isTimeToStartResearchTech3 = true;
		}			
		
		// 브루들링은 최우선으로 리서치한다
		if (myPlayer.completedUnitCount(UnitType.Zerg_Queens_Nest) > 0) {
			isTimeToStartResearchTech4 = true;
		}	

		
		// sc76.choi 히드라 사정 업그레이드
		if (isTimeToStartUpgradeType1) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType1) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType1) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType1) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType1, true);
			}
		}
		
		
		// sc76.choi 히드라 발업
		if (isTimeToStartUpgradeType2) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType2) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType2) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType2) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType2, true);
				
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost, false); // 저글링 속도업(Faster Zergling movement)
			}
		}

		// KTH 오버로드 수송업
		// System.out.println("isTimeToStartUpgradeType5 = " + isTimeToStartUpgradeType5 + " " + necessaryUpgradeType5);
		if (isTimeToStartUpgradeType5) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType5) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType5) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType5) == 0)
			{
				if(myPlayer.hasResearched(TechType.Lurker_Aspect) == true){
					BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType5, true);
				}
			}
		}

		// 오버로드 속도업
		if (isTimeToStartUpgradeType3) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType3) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType3) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType3) == 0)
			{
				// sc76.choi 테란일 경우 럴커 테크 연구를 먼저 진행한다.
//				System.out.println("myPlayer.hasResearched(TechType.Lurker_Aspect) : " + myPlayer.hasResearched(TechType.Lurker_Aspect));
				if(myPlayer.hasResearched(TechType.Lurker_Aspect) == true
					 && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 1){
					
					BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryUpgradeType3, false);
					
				}
			}
		}
		
		// 저글링 아드레날린
		// sc76.choi necessaryUpgradeType3 : Pneumatized_Carapace
		if (isTimeToStartUpgradeType4) 
		{
			if (myPlayer.getUpgradeLevel(necessaryUpgradeType4) == 0 
				&& myPlayer.isUpgrading(necessaryUpgradeType4) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType4) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType4, true);
			}
		}	
		
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
		
		// 퀸 브루들링
		if (isTimeToStartResearchTech4) 
		{
			if (myPlayer.isResearching(necessaryTechType4) == false
				&& myPlayer.hasResearched(necessaryTechType4) == false
				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType4) == 0)
			{
				if(myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 4){
					BuildManager.Instance().buildQueue.queueAsHighestPriority(necessaryTechType4, true);
				}
			}
		}
		
		// 뮤탈 방어 1 업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
			  && myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Carapace) == 0
			  && myPlayer.isUpgrading(UpgradeType.Zerg_Flyer_Carapace) == false
			  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Flyer_Carapace) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Carapace, false);
		}	
		
		// 뮤탈 공격 1 업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Greater_Spire) > 0
			  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
			  && myPlayer.completedUnitCount(UnitType.Zerg_Mutalisk) >= 2
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Carapace) > 0
			  && myPlayer.getUpgradeLevel(UpgradeType.Zerg_Flyer_Attacks) == 0
			  && myPlayer.isUpgrading(UpgradeType.Zerg_Flyer_Attacks) == false
			  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Flyer_Attacks) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Attacks, false);
		}
		
		// 울트라 벙어 1업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) == 0
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}	
		
		// 울트라 벙어 2업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) > 0
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}
		
		// 울트라 벙어 3업
		if (myPlayer.completedUnitCount(UnitType.Zerg_Ultralisk_Cavern) > 0
				  && myPlayer.completedUnitCount(UnitType.Zerg_Lurker) >= 2
				  && myPlayer.completedUnitCount(UnitType.Zerg_Zergling) >= 4
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) > 1
				  && myPlayer.getUpgradeLevel(UpgradeType.Chitinous_Plating) <= 3
				  && myPlayer.isUpgrading(UpgradeType.Chitinous_Plating) == false
				  && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Chitinous_Plating) == 0
		) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);
		}		
	}
	
	public void chamberUpgradeAgainstTerran(){
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// Zerg_Carapace 지상 갑피 업그레이드 1 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& (myPlayer.completedUnitCount(UnitType.Zerg_Lurker)) >= 2				
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, true);
		}
		
		// Zerg_Carapace 지상 갑피 업그레이드  2 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 0 // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 0  // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);
		}
		
		// Zerg_Carapace 지상 갑피 업그레이드 3 단계 (드론, 저글링, 히드라리스크, 러커, 디파일러, 울트라리스크, 라바, 브루들링, 인페스티드 테란,코쿤)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 1 // 다른 업그레이드가 2레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 1  // 다른 업그레이드가 2레벨 올라왔으면			
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Carapace) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Carapace) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 1 단계 (히드라리스크, 러커)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 2 단계 (히드라리스크, 러커)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 1 // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 0  // 다른 업그레이드가 1레벨 올라왔으면
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		// Zerg_Missile_Attacks 원거리 공격 업그레이드 3 단계 (히드라리스크, 러커)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Carapace) > 2
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) > 1
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Missile_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Missile_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 1 단계 (저글링, 울트라리스크, 브루들링)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 0
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}
		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 2 단계 (저글링, 울트라리스크, 브루들링)
		if((myPlayer.completedUnitCount(UnitType.Zerg_Lair) + myPlayer.completedUnitCount(UnitType.Zerg_Hive)) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 1
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 1
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}
		
		// Zerg_Melee_Attacks 근접 공격 업그레이드 3 단계 (저글링, 울트라리스크, 브루들링)
		if(myPlayer.completedUnitCount(UnitType.Zerg_Hive) > 0
			&& myPlayer.completedUnitCount(UnitType.Zerg_Evolution_Chamber) > 0
//			&& myPlayer.completedUnitCount(UnitType.Zerg_Hydralisk_Den) > 0
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Missile_Attacks) > 2
			&& myPlayer.getUpgradeLevel(UpgradeType.Zerg_Melee_Attacks) == 2
			&& myPlayer.isUpgrading(UpgradeType.Zerg_Melee_Attacks) == false
			&& BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Zerg_Melee_Attacks) == 0)
		{
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
		}		
			
	}
}
