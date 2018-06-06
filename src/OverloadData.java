import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Unitset;

public class OverloadData {

	private Map<Integer, Integer> overloadsOnMapPatch = new HashMap<Integer, Integer>();
	
	/// 일꾼 유닛에게 지정하는 임무의 종류
	public  enum OverloadJob { 
		Idle,			///< 하는 일 없음. 대기 상태. 
		Overload,		///< 수리. Terran_SCV 만 가능
		Move,			///< 이동
		Scout, 			///< 정찰. Move와 다름. 정찰지에 도착하면 이동이 없음
		Detector,       ///<Detector 역활
		MyMainBase,
		MyExpansionBase,
		MyFirstChoke,
		MySecondChoke,
		EnemyFirstChoke,
		EnemySecondChoke,
		best3MultiLocation,
		attackWithCombat,
		enemyBasePatrol,
		Center,
		Default 		///< 기본. 미설정 상태. 
	};
	
	/// overload 목록
	private List<Unit> overloads = new ArrayList<Unit>();
	/// Depot 목록
	private List<Unit> depots = new ArrayList<Unit>();

	private Map<Integer, OverloadJob> overloadJobMap = new HashMap<Integer, OverloadJob>();
	private Map<Integer, Integer> depotOverloadCount = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> refineryOverloadCount = new HashMap<Integer, Integer>();
	private Map<Integer, Unit> overloadDepotMap = new HashMap<Integer, Unit>();
	private Map<Integer, OverloadMoveData> overloadMoveMap = new HashMap<Integer, OverloadMoveData>();
	private Map<Integer, Unit> workerRefineryMap = new HashMap<Integer, Unit>();
	
	private CommandUtil commandUtil = new CommandUtil();
	
	public void printOverloadJobMap(){
		if(Config.DEBUG) System.out.println("--------------------printOverloadJobMap-----------------------");
		if(Config.DEBUG) System.out.print(overloadJobMap.size() + ", ");
		for(Map.Entry<Integer, OverloadJob> overload : overloadJobMap.entrySet()){
			if(Config.DEBUG) System.out.print("[");
			if(Config.DEBUG) System.out.print(overload.getKey() + ","+ overload.getValue() + "");
			if(Config.DEBUG) System.out.print("]");
		}
		if(Config.DEBUG) System.out.println();

	}
	
//	public Map<Integer, OverloadJob> getOverloadJobMap(){
//		return overloadJobMap;
//	}
	
	public Map getOverloadJobMapCount(){
		return overloadJobMap;
	}
	
	public OverloadData() 
	{
	     for (Unit unit : MyBotModule.Broodwar.getAllUnits())
	     {
			//if ((unit.getType() == UnitType.Resource_Mineral_Field))
	    	 //System.out.println("OverloadData() unit.getType() : " + unit.getType());
			if ((unit.getType() == UnitType.Zerg_Overlord))
			{
	            overloadsOnMapPatch.put(unit.getID(), 0);
			}
	     }
	}
	
	public final List<Unit> getOverloads()
	{
		return overloads;
	}
	
	public void overloasDestroyed(Unit unit)
	{
		if (unit == null) { return; }

		clearPreviousJob(unit);
		overloads.remove(unit);
	}

	// WorkerJob::Idle 로 일단 추가한다
	public void addOverload(Unit unit)
	{
		if (unit ==  null) { return; }
		//System.out.println("addOverload " + unit.getType());
		overloads.add(unit); // C++ : workers.insert(unit);
		//System.out.println("overloads " + overloads.size());
		
		overloadJobMap.put(unit.getID(), OverloadJob.Idle);
	}
	
	public void addDepot(Unit unit)
	{
		if (unit == null) { return; }
	}

	public List<Unit> getDepots()
	{
		return depots;
	}
	
	public void setOverloadJob(Unit unit, OverloadJob job, Unit jobUnit)
	{
		if (unit == null) { return; }
		
		clearPreviousJob(unit);
		overloadJobMap.put(unit.getID(), job);
		
		if (job == OverloadJob.Move)
		{
			// right click the mineral to start mining
	        //commandUtil.rightClick(unit, mineralToMine);
		}
		else if(job == OverloadJob.Overload)
		{
			
		}
		else if(job == OverloadJob.Scout){
			
		}
	}


	public void setOverloadJob(Unit unit, OverloadJob job, OverloadMoveData wmd)
	{
		if (unit == null) { return; }

		clearPreviousJob(unit);
		overloadJobMap.put(unit.getID(), job);

		if (job == OverloadJob.Move)
		{
			overloadMoveMap.put(unit.getID(), wmd);
		}

		if (overloadJobMap.get(unit.getID()) != OverloadJob.Move)
		{
			//BWAPI::Broodwar->printf("Something went horribly wrong");
		}
	}

	public OverloadData.OverloadJob getOverloadJob(Unit unit)
	{
		if (unit == null)
		{
			return OverloadJob.Default;
		}
		
		if(overloadJobMap.containsKey(unit.getID()))
		{
			return overloadJobMap.get(unit.getID());
		}
//		Iterator<Integer> it = workerJobMap.keySet().iterator();
//		while(it.hasNext())
//		{
//			Integer tempUnit = it.next(); 
//			if(tempUnit.getID() == unit.getID())
//			{
//					
//			}
//		}
		return OverloadJob.Default;
	}
	
	public void clearPreviousJob(Unit unit)
	{
		if (unit == null) { return; }

		OverloadJob previousJob = getOverloadJob(unit);

		if (previousJob == OverloadJob.Move)
		{
			overloadMoveMap.remove(unit.getID()); // C++ : workerMoveMap.erase(unit);
		}

		overloadJobMap.remove(unit.getID()); // C++ : workerJobMap.erase(unit);
	}
	
	public final int getNumOverloads()
	{
		return overloads.size();
	}

	public OverloadMoveData getOverloadMoveData(Unit unit)
	{
//		assert(it != workerMoveMap.end());

		return overloadMoveMap.get(unit.getID());
	}

	
	public char getJobCode(Unit unit)
	{
		if (unit == null) { return 'X'; }

		OverloadData.OverloadJob ol = getOverloadJob(unit);

		if (ol == OverloadData.OverloadJob.Default) return 'D';
		if (ol == OverloadData.OverloadJob.Idle) return 'I';
		if (ol == OverloadData.OverloadJob.Move) return 'O';
		if (ol == OverloadData.OverloadJob.Scout) return 'S';
		return 'X';
	}
}
