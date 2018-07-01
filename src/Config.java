import java.util.Set;

import bwapi.Color;
import bwapi.UnitType;

/// 봇 프로그램 설정
public class Config {
	
	/// KC custom start
	public static boolean DEBUG = true; // debug show 여부를 판단할 boolean
	
	public static boolean IS_DRAW = true; // draw를 그릴지 말지 여부 
	
	// sc76.choi 추가 정보를 화면에 그린다.
	public static boolean DrawSightInfo = true; 
	
	public static int showConsoleLogDelayDisplayTime = 24; // 1초 24 Frame console에 보여주는 delay time
	
	// sc76.choi 일꾼이 공격에 합세할때, 적군의 거리를 판단할 때 쓰입니다.
	public static int DISTANCE_WORKER_CANATTACK = 32 * 7; // TILE_SIZE
	
	// sc76.choi 일꾼 공격 합세숫자
	public static int COUNT_WORKERS_CANATTACK = 3; 
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// sc76.choi 방어 모드로 전환하기 위해 필요한 최소한의 유닛 숫자 설정
	public static int necessaryNumberOfDefenceUnitType1AgainstProtoss = 4; // 저글링
	public static int necessaryNumberOfDefenceUnitType2AgainstProtoss = 5; // 히드라
	public static int necessaryNumberOfDefenceUnitType3AgainstProtoss = 0; // 럴커
	public static int necessaryNumberOfDefenceUnitType4AgainstProtoss = 0; // 뮤탈
	public static int necessaryNumberOfDefenceUnitType5AgainstProtoss = 0; // 울트라
	
	public static int necessaryNumberOfDefenceUnitType1AgainstZerg = 4; // 저글링
	public static int necessaryNumberOfDefenceUnitType2AgainstZerg = 5; // 히드라
	public static int necessaryNumberOfDefenceUnitType3AgainstZerg = 0; // 럴커
	public static int necessaryNumberOfDefenceUnitType4AgainstZerg = 0; // 뮤탈
	public static int necessaryNumberOfDefenceUnitType5AgainstZerg = 0; // 울트라
	
	public static int necessaryNumberOfDefenceUnitType1AgainstTerran = 6; // 저글링
	public static int necessaryNumberOfDefenceUnitType2AgainstTerran = 6; // 히드라
	public static int necessaryNumberOfDefenceUnitType3AgainstTerran = 0; // 럴커
	public static int necessaryNumberOfDefenceUnitType4AgainstTerran = 0; // 뮤탈
	public static int necessaryNumberOfDefenceUnitType5AgainstTerran = 0; // 울트라
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// sc76.choi 공격 모드로 전환하기 위해 필요한 최소한의 유닛 숫자 설정
	public static int necessaryNumberOfCombatUnitType1AgainstProtoss = 16;  // 저글링
	public static int necessaryNumberOfCombatUnitType2AgainstProtoss = 12; // 히드라
	public static int necessaryNumberOfCombatUnitType3AgainstProtoss = 2;  // 럴커
	public static int necessaryNumberOfCombatUnitType4AgainstProtoss = 6;  // 뮤탈
	public static int necessaryNumberOfCombatUnitType5AgainstProtoss = 3;  // 울트라
	public static int necessaryNumberOfSpecialCombatUnitType1AgainstProtoss = 1; // 오버로드
	public static int necessaryNumberOfSpecialCombatUnitType2AgainstProtoss = 1; // 디파일러
	
	public static int necessaryNumberOfCombatUnitType1AgainstZerg = 12; // 저글링
	public static int necessaryNumberOfCombatUnitType2AgainstZerg = 12;  // 히드라
	public static int necessaryNumberOfCombatUnitType3AgainstZerg = 2;  // 럴커
	public static int necessaryNumberOfCombatUnitType4AgainstZerg = 6;  // 뮤탈
	public static int necessaryNumberOfCombatUnitType5AgainstZerg = 3;  // 울트라
	public static int necessaryNumberOfSpecialCombatUnitType1AgainstZerg = 1; // 오버로드
	public static int necessaryNumberOfSpecialCombatUnitType2AgainstZerg = 3; // 디파일러
	
	public static int necessaryNumberOfCombatUnitType1AgainstTerran = 25; // 저글링
	public static int necessaryNumberOfCombatUnitType2AgainstTerran = 12;  // 히드라
	public static int necessaryNumberOfCombatUnitType3AgainstTerran = 1;  // 럴커
	public static int necessaryNumberOfCombatUnitType4AgainstTerran = 6;  // 뮤탈
	public static int necessaryNumberOfCombatUnitType5AgainstTerran = 3;  // 웉트라
	public static int necessaryNumberOfSpecialCombatUnitType1AgainstTerran = 1; // 오버로드
	public static int necessaryNumberOfSpecialCombatUnitType2AgainstTerran = 3; // 디파일러	

	//////////////////////////////////////////////////////////////////////////////////////////
	// 아군의 일반 유닛 최대 전투참가 제한 수
	public static int maxNumberOfCombatUnitType1AgainstProtoss = 12;  // 저글링
	public static int maxNumberOfCombatUnitType2AgainstProtoss = 16; // 히드라
	public static int maxNumberOfCombatUnitType3AgainstProtoss = 4;  // 럴커
	public static int maxNumberOfCombatUnitType4AgainstProtoss = 8;  // 뮤탈
	public static int maxNumberOfCombatUnitType5AgainstProtoss = 3;  // 울트라
	
	public static int maxNumberOfSpecialUnitType1AgainstProtoss = 1;  // 오버로드
	public static int maxNumberOfSpecialUnitType2AgainstProtoss = 2; // 디파일러
	public static int maxNumberOfSpecialUnitType3AgainstProtoss = 6;  // 스커지
	public static int maxNumberOfSpecialUnitType4AgainstProtoss = 2;  // 퀸
	
	public static int maxNumberOfCombatUnitType1AgainstZerg = 12;  // 저글링
	public static int maxNumberOfCombatUnitType2AgainstZerg = 12; // 히드라
	public static int maxNumberOfCombatUnitType3AgainstZerg = 4;  // 럴커
	public static int maxNumberOfCombatUnitType4AgainstZerg = 8;  // 뮤탈
	public static int maxNumberOfCombatUnitType5AgainstZerg = 3;  // 울트라
	
	public static int maxNumberOfSpecialUnitType1AgainstZerg = 1;  // 오버로드
	public static int maxNumberOfSpecialUnitType2AgainstZerg = 3; // 디파일러
	public static int maxNumberOfSpecialUnitType3AgainstZerg = 4;  // 스커지
	public static int maxNumberOfSpecialUnitType4AgainstZerg = 2;  // 퀸
	
	public static int maxNumberOfCombatUnitType1AgainstTerran = 16;  // 저글링
	public static int maxNumberOfCombatUnitType2AgainstTerran = 12; // 히드라
	public static int maxNumberOfCombatUnitType3AgainstTerran = 4;  // 럴커
	public static int maxNumberOfCombatUnitType4AgainstTerran = 8;  // 뮤탈
	public static int maxNumberOfCombatUnitType5AgainstTerran = 3;  // 울트라
	
	public static int maxNumberOfSpecialUnitType1AgainstTerran = 2;  // 오버로드
	public static int maxNumberOfSpecialUnitType2AgainstTerran = 3; // 디파일러
	public static int maxNumberOfSpecialUnitType3AgainstTerran = 4;  // 스커지
	public static int maxNumberOfSpecialUnitType4AgainstTerran = 4;  // 퀸	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// sc76.choi 아군 최대 생산 제한 유닛수
	public static int maxNumberOfTrainUnitType1AgainstProtoss = 20; // 저글링
	public static int maxNumberOfTrainUnitType2AgainstProtoss = 16; // 히드라
	public static int maxNumberOfTrainUnitType3AgainstProtoss = 7;  // 럴커
	public static int maxNumberOfTrainUnitType4AgainstProtoss = 8;  // 뮤탈
	public static int maxNumberOfTrainUnitType5AgainstProtoss = 3;  // 울트라
	
	public static int maxNumberOfTrainSpecialUnitType1AgainstProtoss = 1;  // 오버로드
	public static int maxNumberOfTrainSpecialUnitType2AgainstProtoss = 2;  // 디파일러
	public static int maxNumberOfTrainSpecialUnitType3AgainstProtoss = 6;  // 스커지
	public static int maxNumberOfTrainSpecialUnitType4AgainstProtoss = 1;  // 퀸
	
	public static int maxNumberOfTrainUnitType1AgainstZerg = 20; // 저글링
	public static int maxNumberOfTrainUnitType2AgainstZerg = 16; // 히드라
	public static int maxNumberOfTrainUnitType3AgainstZerg = 7;  // 럴커
	public static int maxNumberOfTrainUnitType4AgainstZerg = 8;  // 뮤탈
	public static int maxNumberOfTrainUnitType5AgainstZerg = 3;  // 울트라
	
	public static int maxNumberOfTrainSpecialUnitType1AgainstZerg = 1;  // 오버로드
	public static int maxNumberOfTrainSpecialUnitType2AgainstZerg = 3;  // 디파일러
	public static int maxNumberOfTrainSpecialUnitType3AgainstZerg = 6;  // 스커지
	public static int maxNumberOfTrainSpecialUnitType4AgainstZerg = 1;  // 퀸
	
	public static int maxNumberOfTrainUnitType1AgainstTerran = 20; // 저글링
	public static int maxNumberOfTrainUnitType2AgainstTerran = 16; // 히드라
	public static int maxNumberOfTrainUnitType3AgainstTerran = 7;  // 럴커
	public static int maxNumberOfTrainUnitType4AgainstTerran = 8;  // 뮤탈
	public static int maxNumberOfTrainUnitType5AgainstTerran = 3;  // 울트라
	
	public static int maxNumberOfTrainSpecialUnitType1AgainstTerran = 2;  // 오버로드
	public static int maxNumberOfTrainSpecialUnitType2AgainstTerran = 3;  // 디파일러
	public static int maxNumberOfTrainSpecialUnitType3AgainstTerran = 6;  // 스커지
	public static int maxNumberOfTrainSpecialUnitType4AgainstTerran = 3;  // 퀸
	
	// 방어 건물 종류 및 건설 갯수 설정
	public static int necessaryNumberOfDefenseBuilding1AgainstProtoss = 2;
	public static int necessaryNumberOfDefenseBuilding2AgainstProtoss = 2; 			
	
	public static int necessaryNumberOfDefenseBuilding1AgainstZerg = 2;
	public static int necessaryNumberOfDefenseBuilding2AgainstZerg = 2;
	
	public static int necessaryNumberOfDefenseBuilding1AgainstTerran = 3;
	public static int necessaryNumberOfDefenseBuilding2AgainstTerran = 3;	
	
	public static int numberOfMyCombatUnitTrainingBuilding = 8;
	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 봇 이름 및 파일 경로 기본값 변경
	/// 봇 이름
	public static final String BotName = "KC";
	/// 봇 개발자 이름
	public static final String BotAuthors = "KC";
	
	/// 로그 파일 이름
	public static String LogFilename = BotName + "_LastGameLog.dat";
	/// 읽기 파일 경로
	public static String ReadDirectory = "bwapi-data\\read\\";
	/// 쓰기 파일 경로
	public static String WriteDirectory = "bwapi-data\\write\\";		

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	
	/// 로컬에서 게임을 실행할 때 게임스피드 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)<br>
	/// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame<br>
	/// Fastest: 42 ms/frame.  1초에 24 frame. 일반적으로 1초에 24frame을 기준 게임속도로 합니다<br>
	/// Normal: 67 ms/frame. 1초에 15 frame<br>
	/// As fast as possible : 0 ms/frame. CPU가 할수있는 가장 빠른 속도.
	public static int SetLocalSpeed = 0;
	
	public static void setSetLocalSpeed(int setLocalSpeed) {
		SetLocalSpeed = setLocalSpeed;
	}
	public static int getSetLocalSpeed() {
		return SetLocalSpeed;
	}
	/// 로컬에서 게임을 실행할 때 FrameSkip (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)<br>
	/// frameskip을 늘리면 화면 표시도 업데이트 안하므로 훨씬 빠릅니다
    public static int SetFrameSkip = 0;
    
    /// 로컬에서 게임을 실행할 때 사용자 키보드/마우스 입력 허용 여부 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)	
    public static boolean EnableUserInput = true;
    
    /// 로컬에서 게임을 실행할 때 전체 지도를 다 보이게 할 것인지 여부 (코드 제출 후 서버에서 게임을 실행할 때는 서버 설정을 사용함)
    // 사용하지 말것 sc76.choi : true로 변경 하지 말것(오버로드가 정찰을 못감)
	//public static boolean EnableCompleteMapInformation = false;
	
	/// MapGrid 에서 한 개 GridCell 의 size
	public static int MAP_GRID_SIZE = 32;
	
	/// StarCraft 및 BWAPI 에서 1 Tile = 32 * 32 Point (Pixel) 입니다<br>
	/// Position 은 Point (Pixel) 단위이고, TilePosition 은 Tile 단위입니다 
	public static int TILE_SIZE = 32;

	/// 각각의 Refinery 마다 투입할 일꾼 최대 숫자
	public static int WorkersPerRefinery = 3;
	
	/// 건물과 건물간 띄울 최소한의 간격 - 일반적인 건물의 경우
	public static int BuildingSpacing = 2;
	
	/// 건물과 건물간 띄울 최소한의 간격 - ResourceDepot 건물의 경우 (Nexus, Hatchery, Command Center)
	public static int BuildingResourceDepotSpacing = 0;
	
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기에
	public static int BuildingPylonEarlyStageSpacing = 4;
	
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기 이후에
	public static int BuildingPylonSpacing = 2;
	
	/// 건물과 건물간 띄울 최소한의 간격 - Terran_Supply_Depot 건물의 경우
	public static int BuildingSupplyDepotSpacing = 0;
	
	/// 건물과 건물간 띄울 최소한의 간격 - 방어 건물의 경우 (포톤캐논. 성큰콜로니. 스포어콜로니. 터렛. 벙커)
	public static int BuildingDefenseTowerSpacing = 1; // sc76.choi 0 -> 3 조정
	
	/// 화면 표시 여부 - 게임 정보
	public static boolean DrawGameInfo = true;
	
	/// 화면 표시 여부 - 미네랄, 가스
	public static boolean DrawResourceInfo = true;
	
	/// 화면 표시 여부 - 지도
	public static boolean DrawBWTAInfo = true;
	
	/// 화면 표시 여부 - 바둑판
	public static boolean DrawMapGrid = false;

	/// 화면 표시 여부 - 유닛 HitPoint
	public static boolean DrawUnitHealthBars = true;
	
	/// 화면 표시 여부 - 유닛 통계
	public static boolean DrawEnemyUnitInfo = false;
	
	/// 화면 표시 여부 - 유닛 ~ Target 간 직선
	public static boolean DrawUnitTargetInfo = false;

	/// 화면 표시 여부 - 빌드 큐
	public static boolean DrawProductionInfo = true;

	/// 화면 표시 여부 - 건물 Construction 상황
	public static boolean DrawBuildingInfo = false;
	
	/// 화면 표시 여부 - 건물 ConstructionPlace 예약 상황, 지을수 없는 곳 표시 
	public static boolean DrawReservedBuildingTiles = false;
	
	/// 화면 표시 여부 - 정찰 상태
	public static boolean DrawScoutInfo = true;
	
	/// 화면 표시 여부 - 일꾼 목록
	public static boolean DrawWorkerInfo = true;
	
	/// 화면 표시 여부 - 마우스 커서	
	public static boolean DrawMouseCursorInfo = true;

	public static final Color ColorLineTarget = Color.White;
	public static final Color ColorLineMineral = Color.Cyan;
	public static final Color ColorUnitNearEnemy = Color.Red;
	public static final Color ColorUnitNotNearEnemy = Color.Green;
}