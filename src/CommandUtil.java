import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;

public class CommandUtil {

	/**
	 * 특정한 Unit에게 공격 명령을 내립니다.
	 * 
	 * @param attacker
	 * @param target
	 */
	public void attackUnit(Unit attacker, Unit target)
	{
		if (attacker == null || target == null)
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (attacker.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || attacker.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to attack this target, ignore this command
		if (currentCommand.getUnitCommandType() == UnitCommandType.Attack_Unit &&	currentCommand.getTarget() == target)
		{
			return;
		}

		// if nothing prevents it, attack the target
		attacker.attack(target);
	}

	/**
	 * 특정한 position으로 attak명령으로 이동합니다.
	 * 
	 * @param attacker
	 * @param targetPosition
	 */
	public void attackMove(Unit attacker, final Position targetPosition)
	{
		// Position 객체에 대해서는 == 가 아니라 equals() 로 비교해야 합니다		
		if (attacker == null || !targetPosition.isValid())
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (attacker.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || attacker.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to attack this target, ignore this command
		if (currentCommand.getUnitCommandType() == UnitCommandType.Attack_Move &&	currentCommand.getTargetPosition().equals(targetPosition))
		{
			return;
		}

		// if nothing prevents it, attack the target
		attacker.attack(targetPosition);
	}

	/**
	 * 특정한 지역으로 단순 move합니다.
	 * 
	 * @param attacker
	 * @param targetPosition
	 */
	public void move(Unit attacker, final Position targetPosition)
	{
		if (attacker == null || !targetPosition.isValid())
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (attacker.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || attacker.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Move) && (currentCommand.getTargetPosition().equals(targetPosition)) && attacker.isMoving())
		{
			return;
		}

		// if nothing prevents it, attack the target
		attacker.move(targetPosition);
	}

	/**
	 * 특정한 지역으로 단순 move합니다.
	 * 
	 * @param attacker
	 * @param targetPosition
	 */
	public void patrol(Unit attacker, final Position targetPosition)
	{
		if (attacker == null || !targetPosition.isValid())
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (attacker.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || attacker.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Patrol) && (currentCommand.getTargetPosition().equals(targetPosition)) && attacker.isMoving())
		{
			return;
		}

		// if nothing prevents it, attack the target
		attacker.patrol(targetPosition);
	}
	
	/**
	 * 특정한 unit에게 우 클릭 명령을 내립니다. 보통 move 명령과 비슷합니다.
	 * 
	 * @param unit
	 * @param target
	 */
	public void rightClick(Unit unit, Unit target)
	{
		if (unit == null || target == null)
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (unit.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || unit.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = unit.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Right_Click_Unit) && (target.getPosition().equals(currentCommand.getTargetPosition())))
		{
			return;
		}

		// if nothing prevents it, attack the target
		unit.rightClick(target);
	}

	/**
	 * 특정 유닛을 수리합니다.
	 * 
	 * @param unit
	 * @param target
	 */
	public void repair(Unit unit, Unit target)
	{
		if (unit == null || target == null)
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (unit.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || unit.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = unit.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Repair) && (currentCommand.getTarget() == target))
		{
			return;
		}

		// if nothing prevents it, attack the target
		unit.repair(target);
	}

	/**
	 * 공격 가능한 유닛인지 판변합니다.
	 * @param unit
	 * @return
	 */
	public boolean IsCombatUnit(Unit unit)
	{
		if (unit == null)
		{
			return false;
		}

		// no workers or buildings allowed
		if (unit != null && unit.getType().isWorker() || unit.getType().isBuilding())
		{
			return false;
		}

		// check for various types of combat units
		if (unit.getType().canAttack() ||
			unit.getType() == UnitType.Terran_Medic ||
			unit.getType() == UnitType.Protoss_High_Templar ||
			unit.getType() == UnitType.Protoss_Observer ||
			unit.isFlying() && unit.getType().spaceProvided() > 0)
		{
			return true;
		}

		return false;
	}

	/**
	 * 현재 unit이 유효한 유닛인지 판별합니다. 유효하지 않은  unit에서 명령을 내리면, 에러가 발생하기 때문에 판별되는 함수입니다.
	 * 
	 * @param unit
	 * @return
	 */
	public boolean IsValidUnit(Unit unit)
	{
		if (unit == null)
		{
			return false;
		}

		if (unit.isCompleted()
			&& unit.getHitPoints() > 0
			&& unit.exists()
			&& unit.getType() != UnitType.Unknown
			&& unit.getPosition().isValid())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * 현재 unit이 유효한 유닛인지 판별합니다. 유효하지 않은  unit에서 명령을 내리면, 에러가 발생하기 때문에 판별되는 함수입니다.
	 * 
	 * @author sc76.choi
	 * @param unit
	 * @return
	 */
	public boolean IsValidSelfUnit(Unit unit){
		if (unit == null){
			return false;
		}

		if (unit.getPlayer() == MyBotModule.Broodwar.self()
			&& unit.isCompleted() 
			&& unit.getHitPoints() > 0
			&& unit.exists()
			&& unit.getType() != UnitType.Unknown
			&& unit.getPosition().isValid())
		{
			return true;
		}
		else{
			return false;
		}
	}	

	/**
	 * 현재 적 유닛이 공격 가능한 유닛인지 판별합니다. 
	 * 유효하지 않은  unit에서 명령을 내리면, 에러가 발생하기 때문에 판별되는 함수입니다.
	 * 
	 * @author sc76.choi
	 * @param unit
	 * @return
	 */
	public boolean IsValidEnemyGroundAttackUnit(Unit unit){
		if (unit == null){
			return false;
		}

		if (unit.isFlying()){
			return false;
		}
		
		if (unit.getPlayer() == MyBotModule.Broodwar.enemy()
			&& unit.isCompleted() 
			&& unit.getHitPoints() > 0
			&& unit.exists()
			&& unit.getType() != UnitType.Unknown
			&& unit.getPosition().isValid())
		{
			return true;
		}
		else{
			return false;
		}
	}	
	
	// 미사용
//	public double GetDistanceBetweenTwoRectangles(Rect rect1, Rect rect2)
//	{
//		Rect & mostLeft = rect1.x < rect2.x ? rect1 : rect2;
//		Rect & mostRight = rect2.x < rect1.x ? rect1 : rect2;
//		Rect & upper = rect1.y < rect2.y ? rect1 : rect2;
//		Rect & lower = rect2.y < rect1.y ? rect1 : rect2;
//
//		int diffX = std::max(0, mostLeft.x == mostRight.x ? 0 : mostRight.x - (mostLeft.x + mostLeft.width));
//		int diffY = std::max(0, upper.y == lower.y ? 0 : lower.y - (upper.y + upper.height));
//
//		return std::sqrtf(static_cast<float>(diffX*diffX + diffY*diffY));
//	}
	
	/**
	 * 공격 가능하고 무기를 가진 unit인지 판별합니다.
	 * 
	 * @param attacker
	 * @param target
	 * @return
	 */
	public boolean CanAttack(Unit attacker, Unit target)
	{
		return GetWeapon(attacker, target) != WeaponType.None;
	}

	/**
	 * 공중 공격이 가능한 unit인지 판별합니다.
	 * 
	 * @param unit
	 * @return
	 */
	public boolean CanAttackAir(Unit unit)
	{
		return unit.getType().airWeapon() != WeaponType.None;
	}

	/**
	 * 지상공격이 가능한 unit인지 판별합니다.
	 * 
	 * @param unit
	 * @return
	 */
	public boolean CanAttackGround(Unit unit)
	{
		return unit.getType().groundWeapon() != WeaponType.None;
	}

	
	public double CalculateLTD(Unit attacker, Unit target)
	{
		WeaponType weapon = GetWeapon(attacker, target);

		if (weapon == WeaponType.None)
		{
			return 0;
		}

		return 0; // C++ : static_cast<double>(weapon.damageAmount()) / weapon.damageCooldown();
	}

	/**
	 * 
	 * Unit이 공중공격 혹은 지상공격 무기의 타입을 알수 있습니다.
	 * @param attacker
	 * @param target
	 * @return
	 * 
	 */
	public WeaponType GetWeapon(Unit attacker, Unit target)
	{
		return target.isFlying() ? attacker.getType().airWeapon() : attacker.getType().groundWeapon();
	}

	/**
	 * 
	 * UnitType이 공중공격 혹은 지상공격 타입인지 반환
	 * @param attacker
	 * @param target
	 * @return
	 */
	public WeaponType GetWeapon(UnitType attacker, UnitType target)
	{
		return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
	}

	/**
	 * Unit의 최대 공격 유효 거리를 반환
	 * @param attacker
	 * @param target
	 * @return
	 */
	public int GetAttackRange(Unit attacker, Unit target)
	{
		WeaponType weapon = GetWeapon(attacker, target);

		if (weapon == WeaponType.None)
		{
			return 0;
		}

		int range = weapon.maxRange();

		if ((attacker.getType() == UnitType.Protoss_Dragoon)
			&& (attacker.getPlayer() == MyBotModule.Broodwar.self())
			&& MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0)
		{
			range = 6 * 32;
		}

		return range;
	}

	/**
	 * UnitType의 최대 공격 유효 거리를 반환
	 * @param attacker
	 * @param target
	 * @return
	 */
	public int GetAttackRange(UnitType attacker, UnitType target)
	{
		WeaponType weapon = GetWeapon(attacker, target);

		if (weapon == WeaponType.None)
		{
			return 0;
		}

		return weapon.maxRange();
	}
	
	/**
	 * 아군의 Type별 총 Unit수를 반환 
	 * @param type
	 * @return
	 */
	public int GetAllUnitCount(UnitType type)
	{
		int count = 0;
		for (final Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			// trivial case: unit which exists matches the type
			if (unit.getType() == type)
			{
				count++;
			}

			// case where a zerg egg contains the unit type
			if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == type)
			{
				count += type.isTwoUnitsInOneEgg() ? 2 : 1;
			}

			// case where a building has started constructing a unit but it doesn't yet have a unit associated with it
			if (unit.getRemainingTrainTime() > 0)
			{
				UnitType trainType = unit.getLastCommand().getUnit().getType();

				if (trainType == type && unit.getRemainingTrainTime() == trainType.buildTime())
				{
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * 지정한 UnitType중, 지정한 Postion으로 부터 가장 가까운 Unit을 반환 
	 * 전체 순차탐색을 하기 때문에 느리다
	 * @param type
	 * @param target
	 * @return
	 */
	public Unit GetClosestSelfUnitTypeToTarget(UnitType type, Position target)
	{
		Unit closestUnit = null;
		double closestDist = 100000000;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.getType() == type)
			{
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist)
				{
					closestUnit = unit;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
	}
}