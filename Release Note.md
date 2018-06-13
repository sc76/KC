2018-06-13 : <기능추가>
				Defence모드시에, 앞마당의 성큰(센터와 가까운)으로 공격 병력 모이게 함
				
2018-06-13 : <기능추가>
				UXManager에 draw 추가
				히드라 오버로드, 뮤탈, 디파일러등 사정거리를 가진 유닛에 drawCircle을 적용
				
2018-06-13 : <기능추가>
				UnitData로 각 유닛의 거리를 산정했고,
				공격시에 적진으로 부터 가장가까운 유닛(히드라)을 타겟으로
				저글링을 움직이도록 한다.
				하지만, 히드라만 특정하게 타겟을 잡을수는 없다. 추가 보안 로직 필요
				
2018-06-13 : <기능추가>
				앞마당과 본진에 오버로드가 한마리씩 patrol 하도록 기능 추가
				TODO patrol 오버로드가 죽었을때, 다시 patrol 할 것인가?
				
2018-06-13 : <기능추가>
				초반 정찰 오버로드가 적진을 발견하면, 그 후로는
				적진 본진과 두번째 choke까지 patrol을 한다.
				
2018-06-13 : <기능추가><기능이전>
				UnitData로 각 유닛의 거리를 산정했고,
				공격시에 적진으로 부터 가장가까운 유닛(히드라)을 타겟으로
				오버로드를 움직이도록 한다.
				
				공격시 상태 : AttackMove
				후퇴시 상태 : Idle
				
2018-06-12 : <버그수정>
				오버로드가 공격에 동참하지 못하는 오류 수정
				
2018-06-12 : <기능추가>
				공격 일꾼 완성
				
2018-06-12 : <기능추가>
				setMainBasePatrolOverload	
				나의 본진에 오버로드가 Veritices로 돌면서 정찰하는 로직 시작
				기본 APM에 무리가 없다.
				
2018-06-12 : <기능추가>
				UX에 APM 및 local speed 추가
				
2018-06-10 : <기능이전>
				쩌러 Bot의 일부 유용한 기능과 Kata2 Bot을 merge함
				
-------------------------------------------------------------------
<TODO or Bug List>
0. 공격, 방어 상태 정리
	예를 들면, 한참 공격 가고 있는데, 앞마당에 적군 특공대 진입

0. 사거리 정리
	http://www.todayhumor.co.kr/board/view.php?table=humorbest&no=85755
	
1. handleGasWorkers의 가스 보정 분기 시점
	- 가스 채취 드론 리밸런싱이 잘 안됨

5. Lair가 가끔 중복 건설된다.
6. isTimeToStartElimination가 true가 되지 못하는 경우가 있다.
7. 공격 가능 시점은 종족별로 따로 판단한다.
   (예 저그는 오버로드 속도업이 된 이후 공격간다.)
8. 초반에 멀티에 해처리가 깨어지면 일꾼 밸랜싱이 안된다.
9. 어설프게 있는 유닛 앞마다으로 집결(tile size 20)
10. 각종 업그레이트, 테크트리의 Type을 정리해야 한다.
11. 적의 메인 베이스를 못찾았을때 를 대비 해야 한다.(모든 기능이 수행 안됨)
12. 오버로드 회피

    for(Region region : BWTA.getRegions())
		for(Chokepoint Chokepoint : region.getChokepoints())
			List<Position> chokes.add(Chokepoint)
			
13. 본진, 앞마당 defence시에 활용

	for(Unit enemyUnit : MyBotModule.Broodwar.enemy().getUnits()) {
		tempDistance = unit.getDistance(enemyUnit.getPosition());
		if (tempDistance < 6 * Config.TILE_SIZE) {
			nearEnemyUnitPosition = enemyUnit.getPosition();
		}
14. 히드라 회피시에 본진과 가장 가까운 다른 히드라로 moveattack한다.
15. 정찰 일꾼 복귀 후, 다시 미네럴 일꾼으로 변하지 않는 오류 발생(basic bot 에러)

<DONE List>
4. 일꾼이 지나다니는 곳에 건물을 건설하지 못하도록 해야 한다.(완료 config수정으로 해결)