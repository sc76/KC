import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * 
 * 해당 Unit의 ID, UnitType, 소속 Player, HitPoint, lastPosition, completed(건물이 완성된 것인지) 등을 저장해두는 자료구조<br>
 * 적군 유닛의 경우 아군 시야 내에 있지 않아 invisible 상태가 되었을 때 정보를 조회할수도 없어지고 파악했던 정보도 유실되기 때문에 별도 자료구조가 필요합니다
 *  
 * sc76.choi 쩌러 Bot의 fromEnemyMainDist, fromSelfMainDist를 가져온다.
 *  
 * @author csc
 * 
 */ 
   

public class UnitInfo {

	private int unitID;
	private int lastHealth;
	private int lastShields;
	private Player player;
	private Unit unit;
	private Position lastPosition;
	private int distanceFromEnemyMainBase;
	private int distanceFromSelfMainBase;	
	private UnitType type;
	private boolean completed;
//	private char bornRegion;
//	private char job;

	public UnitInfo()
	{
		unitID = 0;
		lastHealth = 0;
		player = null;
		unit = null;
		lastPosition = Position.None;
		distanceFromEnemyMainBase = 0;
		distanceFromSelfMainBase = 0;
		type = UnitType.None;
		completed = false;
//		bornRegion = 'M';
//		job = 'N';
	}

//	public char getJob() {
//		return job;
//	}
//
//	public void setJob(char job) {
//		this.job = job;
//	}

	public int getDistanceFromEnemyMainBase() {
		return distanceFromEnemyMainBase;
	}

	public void setDistanceFromEnemyMainBase(int distanceFromEnemyMainBase) {
		this.distanceFromEnemyMainBase = distanceFromEnemyMainBase;
	}

	public int getDistanceFromSelfMainBase() {
		return distanceFromSelfMainBase;
	}

	public void setDistanceFromSelfMainBase(int distanceFromSelfMainBase) {
		this.distanceFromSelfMainBase = distanceFromSelfMainBase;
	}

	public UnitType getType() {
		return type;
	}

	public boolean isCompleted() {
		return completed;
	}

	public Position getLastPosition() {
		return lastPosition;
	}

	public int getUnitID() {
		return unitID;
	}

	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}

	public int getLastHealth() {
		return lastHealth;
	}

	public void setLastHealth(int lastHealth) {
		this.lastHealth = lastHealth;
	}

	public int getLastShields() {
		return lastShields;
	}

	public void setLastShields(int lastShields) {
		this.lastShields = lastShields;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public void setLastPosition(Position lastPosition) {
		this.lastPosition = lastPosition;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
//	
//	public char getBornRegion() {
//		return bornRegion;
//	}
//
//	public void setBornRegion(char bornRegion) {
//		this.bornRegion = bornRegion;
//	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitInfo)) return false;

        UnitInfo that = (UnitInfo) o;

        if (this.getUnitID() != that.getUnitID()) return false;

        return true;
    }

	
//		const bool operator == (BWAPI::Unit unit) const
//		{
//			return unitID == unit->getID();
//		}
//
//		const bool operator == (const UnitInfo & rhs) const
//		{
//			return (unitID == rhs.unitID);
//		}
//
//		const bool operator < (const UnitInfo & rhs) const
//		{
//			return (unitID < rhs.unitID);
//		}
};