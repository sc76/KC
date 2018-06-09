import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

/**
 * 
 * @author sc76.choi
 * 
 * <P>
 *  게임 시작, 진행시에 map의 전체 정보를 저장해 두는 자료 구조<br>
 *  게임 중에도 계속 업데이트 됩니다.<br>
 *  시야에 밝혀진 시점의 상황을 계속 업데이트 합니다.<br>
 *  MapGrid와, MapTool과 같이 활용됩니다.<br>
 * <P>  
 *  base 포지션, 정찰여부, 정찰 횟수, starting인지 여부, 적군이 건물을 건설여부, 적군의 존재 여부, 적군의 유형, 기본 미네날 양, 기본 가스 양, 남아 있는 미네날 양, 남아있는 가스 양, 나의 본진으로 부터의 거리
 */
public class KCBaseInfo {
	private int baseId; // base ID
	private String initialBaseOwner; // 초기 base 소유자 나 : "S", 적 : "E"
	private Position originPosition; // 초기 지역 Position
	private TilePosition originTilePosition; // 초기 지역 Tile Position
	private boolean isScouted; // 정찰 유무
	private int scoutCount; // 정찰 횟수
	private boolean isStartingBase; // Starting Position 유무
	private boolean isExistEnemyBuilding; // base 주변에 적의 건물이 있는지 유무
	private boolean isExistEnemyArmy; // base 주변에 적군이 있는지 유무
	private String kindOfEnemyArmy; // 적이 있다면 적군의 공격 유형(G 지상공격, A 공중공격, B 둘다 가능)
	private int baseAmountMineral; // 초기 base의 미네널 양
	private int baseAmountGas; // 초기 base의 가스양
	private int currentAmountMineral; // 현재 남아있는 미네럴 양
	private int currentAmountGas; // 현재 남아있는 가스 양
	private double distanceByGroundFromMyBase; // 나의 본진으로 부터 지상 거리
	private double distanceByAirFromMyBase; // 나의 본진으로 부터 공중거리
	
	public KCBaseInfo()
	{
		baseId = 0;
		initialBaseOwner = "";
		originPosition = new Position(0, 0);
		originTilePosition = new TilePosition(0, 0);
		isScouted = false;
		scoutCount = 0;
		isStartingBase = false;
		isExistEnemyBuilding = false;
		isExistEnemyArmy = false;
		kindOfEnemyArmy = "";
		baseAmountMineral = 0;
		baseAmountGas = 0;
		currentAmountMineral = 0;
		currentAmountGas = 0;
		distanceByGroundFromMyBase = 0.0d;
		distanceByAirFromMyBase = 0.0d;
	}
	
	public String getInintialBaseOwner() {
		return initialBaseOwner;
	}

	public void setInintialBaseOwner(String initialBaseOwner) {
		this.initialBaseOwner = initialBaseOwner;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public TilePosition getOriginTilePosition() {
		return originTilePosition;
	}

	public void setOriginTilePosition(TilePosition originTilePosition) {
		this.originTilePosition = originTilePosition;
	}

	public double getDistanceByGroundFromMyBase() {
		return distanceByGroundFromMyBase;
	}

	public void setDistanceByGroundFromMyBase(double distanceByGroundFromMyBase) {
		this.distanceByGroundFromMyBase = distanceByGroundFromMyBase;
	}

	public double getDistanceByAirFromMyBase() {
		return distanceByAirFromMyBase;
	}

	public void setDistanceByAirFromMyBase(double distanceByAirFromMyBase) {
		this.distanceByAirFromMyBase = distanceByAirFromMyBase;
	}

	public Position getOriginPosition() {
		return originPosition;
	}
	public void setOriginPosition(Position originPosition) {
		this.originPosition = originPosition;
	}
	public boolean isScouted() {
		return isScouted;
	}
	public void setScouted(boolean isScouted) {
		this.isScouted = isScouted;
	}
	public int getScoutCount() {
		return scoutCount;
	}
	public void setScoutCount(int scoutCount) {
		this.scoutCount = scoutCount;
	}
	public boolean isStartingBase() {
		return isStartingBase;
	}
	public void setStartingBase(boolean isStartingBase) {
		this.isStartingBase = isStartingBase;
	}
	public boolean isExistEnemyBuilding() {
		return isExistEnemyBuilding;
	}
	public void setExistEnemyBuilding(boolean isExistEnemyBuilding) {
		this.isExistEnemyBuilding = isExistEnemyBuilding;
	}
	public boolean isExistEnemyArmy() {
		return isExistEnemyArmy;
	}
	public void setExistEnemyArmy(boolean isExistEnemyArmy) {
		this.isExistEnemyArmy = isExistEnemyArmy;
	}
	public String getKindOfEnemyArmy() {
		return kindOfEnemyArmy;
	}
	public void setKindOfEnemyArmy(String kindOfEnemyArmy) {
		this.kindOfEnemyArmy = kindOfEnemyArmy;
	}
	public int getBaseAmountMineral() {
		return baseAmountMineral;
	}
	public void setBaseAmountMineral(int baseAmountMineral) {
		this.baseAmountMineral = baseAmountMineral;
	}
	public int getBaseAmountGas() {
		return baseAmountGas;
	}
	public void setBaseAmountGas(int baseAmountGas) {
		this.baseAmountGas = baseAmountGas;
	}
	public int getCurrentAmountMineral() {
		return currentAmountMineral;
	}
	public void setCurrentAmountMineral(int currentAmountMineral) {
		this.currentAmountMineral = currentAmountMineral;
	}
	public int getCurrentAmountGas() {
		return currentAmountGas;
	}
	public void setCurrentAmountGas(int currentAmountGas) {
		this.currentAmountGas = currentAmountGas;
	}
}
