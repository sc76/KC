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
 *  대전 시작 중, 대전 가능 정보를 저장해 두는 자료 구조<br>
 */
public class KCSimulationResult {
	private boolean canAttackNow; // 대전 가능성
	private int myPoint; // 나의 점수
	private int enemyPoint; // 적의 점수
	private boolean existEnemyAdvancedDefenceBuilding; // Starting Position 유무
	
	public KCSimulationResult()	{
		canAttackNow = true;
		myPoint = 1;
		enemyPoint = 0;
		existEnemyAdvancedDefenceBuilding = false;
	}

	public boolean isCanAttackNow() {
		return canAttackNow;
	}

	public void setCanAttackNow(boolean canAttackNow) {
		this.canAttackNow = canAttackNow;
	}

	public int getMyPoint() {
		return myPoint;
	}

	public void setMyPoint(int myPoint) {
		this.myPoint = myPoint;
	}

	public int getEnemyPoint() {
		return enemyPoint;
	}

	public void setEnemyPoint(int enemyPoint) {
		this.enemyPoint = enemyPoint;
	}

	public boolean isExistEnemyAdvancedDefenceBuilding() {
		return existEnemyAdvancedDefenceBuilding;
	}

	public void setExistEnemyAdvancedDefenceBuilding(boolean existEnemyAdvancedDefenceBuilding) {
		this.existEnemyAdvancedDefenceBuilding = existEnemyAdvancedDefenceBuilding;
	}
	
}
