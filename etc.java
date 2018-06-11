	/**
	 * combatWorker
	 * 일꾼도 주변에 적의 공격 유닛이 있다면 공격한다.
	 * 단점, 본진이나, 멀티 중 한곳만 실행이 가능할 것이다, 단순히 공격일꾼 3마리만 카운트 하기 때문에
	 * 
	 * @author sc76.choi
	 */
	void commandMyWorkerToAttack(){
		// 1초에 4번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) return;
		
		boolean existEnemyAroundWorker = false;
		
		for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
			if(!commandUtil.IsValidSelfUnit(worker)) return;
			
			// 각 worker의 주변 DISTANCE_WORKER_CANATTACK을 살펴 본다.
			Iterator<Unit> iter = MyBotModule.Broodwar.getUnitsInRadius(worker.getPosition(), Config.DISTANCE_WORKER_CANATTACK).iterator();
			while(iter.hasNext()){
				Unit unit = iter.next();
				
				// 지상공격이 가능한 적군이면 CombatWorker으로 변경한다.
				if(commandUtil.IsValidEnemyGroundAttackUnit(unit)){
					Unit enemyUnit = unit;
					
					existEnemyAroundWorker = true; // 적군 카운트 증
					//System.out.println("countEnemyAroundWorker : " + countEnemyAroundWorker);
					//System.out.println("commandUtil.IsValidEnemyGroundAttackUnit(unit) : " + unit.getID() + ", " + commandUtil.IsValidEnemyGroundAttackUnit(unit));
					
					// 이미 공격일꾼이 있으면 (일꾼 공격 합세는 2마리만 한다.)
					if(WorkerManager.Instance().getWorkerData().getNumCombatWorkers() >= Config.COUNT_WORKERS_CANATTACK){
						break;
					}
					
					// 적군과 나와의 거리가 DISTANCE_WORKER_CANATTACK내에 있는,
					// worker(Job이 Mineral이고 체력이 온전한)를 상태를 combat으로 변경한다.
					if(worker.getDistance(enemyUnit) < Config.DISTANCE_WORKER_CANATTACK){

						// 공격 투입
						if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Minerals){
							WorkerManager.Instance().setCombatWorker(worker);
							
							//System.out.println("WorkerManager.Instance().getWorkerData().getNumCombatWorkers() : " + WorkerManager.Instance().getWorkerData().getNumCombatWorkers());
							// 일꾼 공격 합세는 2마리만 한다.
							if(WorkerManager.Instance().getWorkerData().getNumCombatWorkers() >= Config.COUNT_WORKERS_CANATTACK) { 
								break;
							}
						}
						
						// 공격 해제
						/*
						if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat
								&& worker.getInitialHitPoints() > worker.getHitPoints()){
							WorkerManager.Instance().setIdleWorker(worker);
							
							if(countSelfCombatWorker > 0) countSelfCombatWorker--;
						}
						*/
					}
				}
			} // while
		}
		
		// 적군이 없다면 idle로 변경하여, 다시 일을 할수 있게 한다.
		if(!existEnemyAroundWorker){
			for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
				if(!commandUtil.IsValidSelfUnit(worker)) return;
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
					WorkerManager.Instance().setIdleWorker(worker);
				}
			}
		}
	}
