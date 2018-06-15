import java.util.Set;

import bwapi.Color;
import bwapi.UnitType;

/// 봇 프로그램 설정
public class Config {
	
	/// KC custom start
	public static final boolean DEBUG = true; // debug show 여부를 판단할 boolean
	public static int showConsoleLogDelayDisplayTime = 24; // 1초 24 Frame console에 보여주는 delay time
	// 일꾼이 공격에 합세할때, 적군의 거리를 판단할 때 쓰입니다.
	public static int DISTANCE_WORKER_CANATTACK = 32 * 3; // TILE_SIZE
	public static int COUNT_WORKERS_CANATTACK = 7; // 일꾼 공격 합세는 2마리만 한다.
	public static boolean DrawSightInfo = true; // 유닛의 사정거리 만큼 원을 그린다.

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