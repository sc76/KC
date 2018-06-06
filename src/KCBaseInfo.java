import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

// 
// 해당 Unit의 ID, UnitType, 소속 Player, HitPoint, lastPosition, completed(건물이 완성된 것인지) 등을 저장해두는 자료구조<br>
// 적군 유닛의 경우 아군 시야 내에 있지 않아 invisible 상태가 되었을 때 정보를 조회할수도 없어지고 파악했던 정보도 유실되기 때문에 별도 자료구조가 필요합니다
/**
 * 
 * @author harshnet037
 * 
 * <P>
 *  게임 시작, 진행시에 map의 전체 정보를 저장해 두는 자료 구조<br>
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
public class KCBaseInfo {

	private Position originPosition;
	private boolean isScouted;
	private int scoutCount;
	private boolean isStartingBase;
	private boolean isExistEnemyBuilding;
	private boolean isExistEnemyArmy;
	private String kindOfEnemyArmy;
	private int baseAmountMineral;
	private int baseAmountGas;
	private int currentAmountMineral;
	private int currentAmountGas;
	private double distanceFromMyBase;
	
	public KCBaseInfo()
	{
		originPosition = new Position(0, 0);
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
		distanceFromMyBase = 0.0d;
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
	public double getDistanceFromMyBase() {
		return distanceFromMyBase;
	}
	public void setDistanceFromMyBase(double distanceFromMyBase) {
		this.distanceFromMyBase = distanceFromMyBase;
	}
}
