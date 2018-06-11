	/**
	 * combatWorker
	 * 일꾼도 주변에 적의 공격 유닛이 있다면 공격한다.
	 * 
	 * @author sc76.choi
	 */
	void combatWorker(){
		// 1초에 1번만 실행합니다
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) return;
		
		int countEnemyAroundWorker = 0;
		int countSelfCombatWorker = 0;
		
		// 현재 공격 일꾼 수
		for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
			if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
				countSelfCombatWorker++;
			}
		}
		
		for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
			if(!commandUtil.IsValidSelfUnit(worker)) return;
			
			// 각 worker의 주변 DISTANCE_WORKER_CANATTACK을 살펴 본다.
			Iterator iter = MyBotModule.Broodwar.getUnitsInRadius(worker.getPosition(), Config.DISTANCE_WORKER_CANATTACK).iterator();
			while(iter.hasNext()){
				Unit unit = (Unit)iter.next();
				if(!commandUtil.IsValidUnit(unit)) return;
				
				// 지상공격이 가능한 적군이면 CombatWorker으로 변경한다.
				if(commandUtil.IsValidEnemyGroundAttackUnit(unit)){
					countEnemyAroundWorker++; // 적군 카운트
					
					// 이미 공격일꾼이 있으면 (일꾼 공격 합세는 2마리만 한다.)
					if(countSelfCombatWorker >= Config.COUNT_WORKERS_CANATTACK) break;
					
					// 적군과 나와의 거리가 DISTANCE_WORKER_CANATTACK내에 있는,
					// worker(Job이 Mineral이고 체력이 온전한)를 상태를 combat으로 변경한다.
					if(worker.getDistance(unit) < Config.DISTANCE_WORKER_CANATTACK){

						// 공격 투입
						if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Minerals
								&& worker.getInitialHitPoints() <= worker.getHitPoints()){
							WorkerManager.Instance().setCombatWorker(worker);
							countSelfCombatWorker++;
							
							// 일꾼 공격 합세는 2마리만 한다.
							if(countSelfCombatWorker >= Config.COUNT_WORKERS_CANATTACK) break;
						}
						
						// 공격 해제
						if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat
								&& worker.getInitialHitPoints() > worker.getHitPoints()){
							WorkerManager.Instance().setIdleWorker(worker);
							
							if(countSelfCombatWorker > 0) countSelfCombatWorker--;
						}
					}
				}
			} // while
		}
		
		// 적군이 없다면 idle로 변경하여, 다시 일을 할수 있게 한다.
		if(countEnemyAroundWorker <= 0){
			for (Unit worker : WorkerManager.Instance().getWorkerData().getWorkers()) {
				if(!commandUtil.IsValidSelfUnit(worker)) return;
				if(WorkerManager.Instance().getWorkerData().getWorkerJob(worker) == WorkerData.WorkerJob.Combat){
					WorkerManager.Instance().setIdleWorker(worker);
				}
			}
		}
	}
