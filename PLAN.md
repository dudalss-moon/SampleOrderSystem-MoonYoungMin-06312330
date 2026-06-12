# S-Semi 반도체 시료생산 주문관리 시스템 개발 계획

## 프로젝트 개요
- 콘솔 기반 반도체 시료생산 주문관리 시스템
- Java 17, JUnit 5, Gradle
- TDD(Test-Driven Development) 방법론 적용

## 개발 Phase 구성

| Phase | 기능 | 설계 문서 | 상태 |
|-------|------|-----------|------|
| Phase1 | 기반 구조 + 메인 메뉴 | [Phase1.md](docs/design/Phase1.md) | 완료 |
| Phase2 | 시료 관리 | [Phase2.md](docs/design/Phase2.md) | 완료 |
| Phase3 | 시료 주문(예약) | [Phase3.md](docs/design/Phase3.md) | 완료 |
| Phase4 | 주문 승인/거절 | [Phase4.md](docs/design/Phase4.md) | 완료 |
| Phase5 | 모니터링 | [Phase5.md](docs/design/Phase5.md) | 완료 |
| Phase6 | 생산라인 (자동 생산 포함) | [Phase6.md](docs/design/Phase6.md) | 완료 |
| Phase7 | 출고처리 | [Phase7.md](docs/design/Phase7.md) | 완료 |
| Phase8 | DB 연동 (H2 JDBC) | [Phase8.md](docs/design/Phase8.md) | 완료 |
| Phase9 | 테스트 커버리지 보강 | [Phase9.md](docs/design/Phase9.md) | 완료 |

## 아키텍처 원칙
- 계층 구조: `ui` → `service` → `domain` → `repository`
- 단방향 의존성 유지
- 도메인 객체 불변성 우선
- TDD: Red → Green → Refactor 사이클

## 패키지 구조
```
src/main/java/ssemi/order/
├── Main.java
├── ui/
│   ├── ConsoleMenu.java
│   ├── SampleUI.java
│   ├── OrderUI.java
│   ├── MonitorUI.java
│   ├── ProductionUI.java
│   └── ReleaseUI.java
├── domain/
│   ├── Sample.java
│   ├── Order.java
│   ├── OrderStatus.java
│   └── ProductionJob.java
├── repository/
│   ├── SampleRepository.java
│   ├── OrderRepository.java
│   └── ProductionQueueRepository.java
└── service/
    ├── SampleService.java
    ├── OrderService.java
    ├── MonitorService.java
    ├── ProductionService.java
    └── ReleaseService.java
```

## 주문 상태 흐름
```
RESERVED → [승인] → 재고 충분  → CONFIRMED → RELEASE
                 → 재고 부족  → PRODUCING → CONFIRMED → RELEASE
         → [거절] → REJECTED
```

---

## Phase1 TDD 계획

### 사이클 1: OrderStatus enum — 유효값 5가지 검증
- **검증할 동작:** `OrderStatus` enum이 정확히 5개의 값을 가지는지 검증
- **메서드명:** `orderStatus_유효값_5가지`
- **@DisplayName:** `"OrderStatus는 RESERVED, REJECTED, PRODUCING, CONFIRMED, RELEASE 5가지 값을 가진다"`
- **기대 결과:** `values().length == 5`, 각 값 존재 확인
- **커버 요구사항:** Phase1.md § 도메인 모델 > OrderStatus

### 사이클 2: Sample 생성 — 필드값 정상 저장
- **검증할 동작:** `Sample` 생성 시 id, name, avgProductionTime, yield, stock 필드 정상 저장
- **메서드명:** `sample_생성_시_필드값_정상저장`
- **@DisplayName:** `"Sample 생성 시 id, name, avgProductionTime, yield, stock 필드가 정상 저장된다"`
- **입력:** id="S001", name="실리콘A", avgProductionTime=30, yield=0.85, stock=100
- **기대 결과:** 각 getter 동일 값 반환
- **커버 요구사항:** Phase1.md § 도메인 모델 > Sample

### 사이클 3: Order 생성 — 초기 상태 RESERVED
- **검증할 동작:** `Order` 생성 시 status 초기값이 `OrderStatus.RESERVED`
- **메서드명:** `order_생성_초기상태_RESERVED`
- **@DisplayName:** `"Order 생성 시 초기 status는 RESERVED이다"`
- **입력:** orderId="O001", sample, customerName="홍길동", quantity=10
- **기대 결과:** `order.getStatus() == OrderStatus.RESERVED`
- **커버 요구사항:** Phase1.md § 도메인 모델 > Order

### 사이클 4: ProductionJob — 실생산량 계산 정확성
- **검증할 동작:** `targetQty = ceil(부족분 / (수율 × 0.9))` 공식 적용
- **메서드명:** `productionJob_실생산량_계산_정확성`
- **@DisplayName:** `"ProductionJob targetQty는 ceil(부족분 / (수율 * 0.9)) 공식으로 계산된다"`
- **입력:** 주문수량=10, 재고=3, yield=0.8 → 부족분=7
- **기대 결과:** `targetQty = ceil(7 / 0.72) = 10`
- **커버 요구사항:** Phase1.md § 도메인 모델 > ProductionJob

### 사이클 5: ProductionJob — 총 생산시간 계산
- **검증할 동작:** `totalTime = avgProductionTime × targetQty` 공식 적용
- **메서드명:** `productionJob_총생산시간_계산`
- **@DisplayName:** `"ProductionJob totalTime은 avgProductionTime * targetQty로 계산된다"`
- **입력:** avgProductionTime=30, targetQty=10
- **기대 결과:** `totalTime = 300`
- **커버 요구사항:** Phase1.md § 도메인 모델 > ProductionJob

### 사이클 6: InputHandler — 정수 입력 정상 처리
- **검증할 동작:** `InputHandler.readInt()`가 정수를 올바르게 읽어오는지
- **메서드명:** `inputHandler_숫자_입력_정상처리`
- **@DisplayName:** `"InputHandler readInt는 유효한 정수 입력을 올바르게 반환한다"`
- **입력:** InputStream("3\n")
- **기대 결과:** `readInt("선택 > ") == 3`
- **커버 요구사항:** Phase1.md § 클래스 설계 > InputHandler

### 사이클 7: InputHandler — 잘못된 입력 예외 처리
- **검증할 동작:** 비정수 입력 후 재입력 유도 및 올바른 정수 반환
- **메서드명:** `inputHandler_잘못된_입력_예외처리`
- **@DisplayName:** `"InputHandler readInt는 비정수 입력 후 재입력을 받아 올바른 값을 반환한다"`
- **입력:** InputStream("abc\n5\n")
- **기대 결과:** `readInt("선택 > ") == 5`
- **커버 요구사항:** Phase1.md § 클래스 설계 > InputHandler

---

## Phase2 TDD 계획

### [SampleTest] 사이클 1: 시료 생성 정상
- **메서드명:** `시료_생성_정상`
- **@DisplayName:** `"유효한 값으로 Sample을 생성하면 모든 필드가 정상 저장된다"`
- **입력:** id="S001", name="알파센서", avgProductionTime=30, yield=0.9, stock=0
- **기대 결과:** 각 getter 동일 값 반환

### [SampleTest] 사이클 2: 수율 0 이하 예외
- **메서드명:** `수율_0이하_예외`
- **@DisplayName:** `"수율이 0 이하이면 IllegalArgumentException이 발생한다"`
- **입력:** yield=0.0
- **기대 결과:** `IllegalArgumentException`

### [SampleTest] 사이클 3: 수율 1 초과 예외
- **메서드명:** `수율_1초과_예외`
- **@DisplayName:** `"수율이 1.0 초과이면 IllegalArgumentException이 발생한다"`
- **입력:** yield=1.1
- **기대 결과:** `IllegalArgumentException`

### [SampleTest] 사이클 4: 평균생산시간 0 이하 예외
- **메서드명:** `평균생산시간_0이하_예외`
- **@DisplayName:** `"평균생산시간이 0 이하이면 IllegalArgumentException이 발생한다"`
- **입력:** avgProductionTime=0
- **기대 결과:** `IllegalArgumentException`

### [SampleTest] 사이클 5: 재고 추가
- **메서드명:** `재고_추가_정상`
- **@DisplayName:** `"addStock(10) 호출 시 재고가 10 증가한다"`
- **입력:** stock=0 → addStock(10)
- **기대 결과:** `stock == 10`

### [SampleTest] 사이클 6: 재고 차감
- **메서드명:** `재고_차감_정상`
- **@DisplayName:** `"deductStock(3) 호출 시 재고가 3 감소한다"`
- **입력:** stock=10 → deductStock(3)
- **기대 결과:** `stock == 7`

### [SampleTest] 사이클 7: 재고 차감 부족 예외
- **메서드명:** `재고_차감_부족_예외`
- **@DisplayName:** `"재고보다 많은 수량을 차감하면 IllegalStateException이 발생한다"`
- **입력:** stock=5 → deductStock(10)
- **기대 결과:** `IllegalStateException`

### [SampleTest] 사이클 8: 재고 충분 여부 확인
- **메서드명:** `재고_충분_여부_확인`
- **@DisplayName:** `"hasEnoughStock은 재고 충분 시 true, 부족 시 false를 반환한다"`
- **입력:** stock=5 → hasEnoughStock(5)=true, hasEnoughStock(6)=false
- **기대 결과:** 각각 true / false

### [SampleRepositoryTest] 사이클 9: 저장 후 ID 조회
- **메서드명:** `시료_저장_후_ID로_조회`
- **@DisplayName:** `"저장한 시료를 ID로 조회하면 동일한 객체가 반환된다"`
- **기대 결과:** `findById("S001").get() == sample`

### [SampleRepositoryTest] 사이클 10: 전체 조회
- **메서드명:** `전체_시료_조회`
- **@DisplayName:** `"findAll은 저장된 모든 시료를 등록 순서대로 반환한다"`
- **기대 결과:** 크기 2, 등록 순서 보장

### [SampleRepositoryTest] 사이클 11: 이름 부분 일치 검색
- **메서드명:** `이름으로_검색_부분일치`
- **@DisplayName:** `"findByName은 이름에 키워드가 포함된 시료를 모두 반환한다"`
- **입력:** "알파" 검색
- **기대 결과:** "알파센서" 포함

### [SampleRepositoryTest] 사이클 12: 존재하지 않는 ID 조회
- **메서드명:** `존재하지않는_ID_조회_빈값`
- **@DisplayName:** `"존재하지 않는 ID로 조회하면 Optional.empty()가 반환된다"`
- **기대 결과:** `Optional.empty()`

### [SampleRepositoryTest] 사이클 13: 중복 ID 존재 여부
- **메서드명:** `중복_ID_존재여부_확인`
- **@DisplayName:** `"existsById는 저장된 ID에 대해 true를 반환한다"`
- **기대 결과:** `existsById("S001") == true`

### [SampleServiceTest] 사이클 14: 시료 등록 정상
- **메서드명:** `시료_등록_정상`
- **@DisplayName:** `"register 호출 시 시료가 저장되고 반환된다"`
- **기대 결과:** 반환된 Sample의 id, name 검증

### [SampleServiceTest] 사이클 15: 중복 ID 등록 예외
- **메서드명:** `중복_ID_등록_예외`
- **@DisplayName:** `"이미 존재하는 ID로 등록하면 IllegalArgumentException이 발생한다"`
- **기대 결과:** `IllegalArgumentException`

### [SampleServiceTest] 사이클 16: 중복 이름 등록 예외
- **메서드명:** `중복_이름_등록_예외`
- **@DisplayName:** `"이미 존재하는 이름으로 등록하면 IllegalArgumentException이 발생한다"`
- **기대 결과:** `IllegalArgumentException`

### [SampleServiceTest] 사이클 17: 검색 결과 없음
- **메서드명:** `검색_결과_없음_빈리스트`
- **@DisplayName:** `"매칭되는 시료가 없으면 빈 리스트가 반환된다"`
- **입력:** keyword="없는시료"
- **기대 결과:** `emptyList()`

---

## Phase3 TDD 계획

### [OrderTest] 사이클 1: 주문 생성 초기상태 RESERVED
- **메서드명:** `주문_생성_초기상태_RESERVED`
- **@DisplayName:** `"Order 생성 시 초기 status는 RESERVED이다"`
- **입력:** orderId="O001", sample, customerName="홍길동", quantity=5
- **기대 결과:** `order.getStatus() == RESERVED`

### [OrderTest] 사이클 2: 주문 상태 정상 전이
- **메서드명:** `주문_상태_정상_전이`
- **@DisplayName:** `"RESERVED 상태에서 CONFIRMED로 정상 전이된다"`
- **입력:** RESERVED → changeStatus(CONFIRMED)
- **기대 결과:** `order.getStatus() == CONFIRMED`

### [OrderTest] 사이클 3: 주문 상태 비허용 전이 예외
- **메서드명:** `주문_상태_비허용_전이_예외`
- **@DisplayName:** `"RELEASE 상태에서 RESERVED로 전이 시 IllegalStateException이 발생한다"`
- **입력:** RELEASE → changeStatus(RESERVED)
- **기대 결과:** `IllegalStateException`

### [OrderTest] 사이클 4: 주문 수량 0 이하 예외
- **메서드명:** `주문_수량_0이하_예외`
- **@DisplayName:** `"주문 수량이 0 이하이면 IllegalArgumentException이 발생한다"`
- **입력:** quantity=0
- **기대 결과:** `IllegalArgumentException`

### [OrderTest] 사이클 5: 고객명 공백 예외
- **메서드명:** `고객명_공백_예외`
- **@DisplayName:** `"고객명이 공백이면 IllegalArgumentException이 발생한다"`
- **입력:** customerName="  "
- **기대 결과:** `IllegalArgumentException`

### [OrderRepositoryTest] 사이클 6: 저장 후 ID 조회
- **메서드명:** `주문_저장_후_ID로_조회`
- **@DisplayName:** `"저장한 주문을 ID로 조회하면 동일한 객체가 반환된다"`
- **기대 결과:** `findById("ORD-0001").get().getOrderId() == "ORD-0001"`

### [OrderRepositoryTest] 사이클 7: 상태별 주문 조회
- **메서드명:** `상태별_주문_조회`
- **@DisplayName:** `"findByStatus는 해당 상태의 주문만 반환한다"`
- **입력:** RESERVED 2건, CONFIRMED 1건 저장 후 findByStatus(RESERVED)
- **기대 결과:** 크기 2, 모두 RESERVED 상태

### [OrderRepositoryTest] 사이클 8: ID 자동생성 순번 증가
- **메서드명:** `ID_자동생성_순번_증가`
- **@DisplayName:** `"generateId는 ORD-0001, ORD-0002 순으로 순번이 증가한다"`
- **기대 결과:** 첫 번째 "ORD-0001", 두 번째 "ORD-0002"

### [OrderRepositoryTest] 사이클 9: 전체 주문 조회 등록 순서 보장
- **메서드명:** `전체_주문_조회_등록순_보장`
- **@DisplayName:** `"findAll은 저장된 주문을 등록 순서대로 반환한다"`
- **기대 결과:** 크기 2, 등록 순서 일치

### [OrderServiceTest] 사이클 10: 정상 주문 예약
- **메서드명:** `정상_주문_예약`
- **@DisplayName:** `"유효한 시료와 수량으로 reserve 호출 시 RESERVED 상태 주문이 반환된다"`
- **기대 결과:** `order.getStatus() == RESERVED`, orderId "ORD-0001" 형식

### [OrderServiceTest] 사이클 11: 존재하지 않는 시료 주문 예외
- **메서드명:** `존재하지않는_시료_주문_예외`
- **@DisplayName:** `"존재하지 않는 시료 ID로 주문 시 IllegalArgumentException이 발생한다"`
- **입력:** sampleId="NONE"
- **기대 결과:** `IllegalArgumentException`

### [OrderServiceTest] 사이클 12: 수량 0 이하 예약 예외
- **메서드명:** `수량_0이하_예약_예외`
- **@DisplayName:** `"주문 수량이 0 이하이면 IllegalArgumentException이 발생한다"`
- **입력:** quantity=0
- **기대 결과:** `IllegalArgumentException`

### [OrderServiceTest] 사이클 13: RESERVED 주문 목록 조회
- **메서드명:** `RESERVED_주문_목록_조회`
- **@DisplayName:** `"findReserved는 RESERVED 상태 주문만 반환한다"`
- **입력:** RESERVED 2건, CONFIRMED 1건 등록
- **기대 결과:** 크기 2, 모두 RESERVED 상태

---

## Phase4 TDD 계획

> Phase1에서 `productionJob_실생산량_계산_정확성`, `productionJob_총생산시간_계산` 이미 검증됨.
> Phase4는 `isCompleted()`, `produce()`, `ProductionQueueRepository`, `OrderService.approve/reject` 구현에 집중.

### [ProductionJobTest] 사이클 1: 생산량 누적
- **메서드명:** `생산량_누적`
- **@DisplayName:** `"produce(3) 호출 시 producedQty가 3 증가한다"`
- **입력:** shortage=7, yield=0.8 → targetQty=10, 이후 produce(3) 호출
- **기대 결과:** `job.getProducedQty() == 3`
- **커버 요구사항:** Phase4.md § ProductionJob > produce(qty)

### [ProductionJobTest] 사이클 2: 생산 완료 여부 확인
- **메서드명:** `생산_완료_여부_확인`
- **@DisplayName:** `"producedQty가 targetQty 이상이면 isCompleted()는 true를 반환한다"`
- **입력:** targetQty=10, produce(10) 호출 → isCompleted()=true; produce(9) 호출 → isCompleted()=false
- **기대 결과:** 각각 true / false
- **커버 요구사항:** Phase4.md § ProductionJob > isCompleted()

### [ProductionQueueRepositoryTest] 사이클 3: enqueue 후 peek 동일
- **메서드명:** `작업_enqueue_후_peek_동일`
- **@DisplayName:** `"enqueue한 첫 번째 작업을 peek으로 조회하면 동일한 객체가 반환된다"`
- **기대 결과:** `repo.peek().get() == job`
- **커버 요구사항:** Phase4.md § ProductionQueueRepository > peek()

### [ProductionQueueRepositoryTest] 사이클 4: FIFO 순서 보장
- **메서드명:** `FIFO_순서_보장`
- **@DisplayName:** `"enqueue 순서대로 dequeue된다"`
- **입력:** job1, job2 순서로 enqueue
- **기대 결과:** dequeue() 첫 번째 job1, 두 번째 job2
- **커버 요구사항:** Phase4.md § ProductionQueueRepository > FIFO

### [ProductionQueueRepositoryTest] 사이클 5: 빈 큐 peek 빈값
- **메서드명:** `빈_큐_peek_빈값`
- **@DisplayName:** `"빈 큐에서 peek 호출 시 Optional.empty()가 반환된다"`
- **기대 결과:** `Optional.empty()`
- **커버 요구사항:** Phase4.md § ProductionQueueRepository > peek() 빈 큐

### [ProductionQueueRepositoryTest] 사이클 6: 대기목록 조회
- **메서드명:** `대기목록_조회`
- **@DisplayName:** `"getQueue는 현재 큐에 있는 작업 목록을 반환한다"`
- **입력:** job1, job2 enqueue 후 job1 dequeue
- **기대 결과:** getQueue() 크기 1, job2만 포함
- **커버 요구사항:** Phase4.md § ProductionQueueRepository > getQueue()

### [OrderServiceTest] 사이클 7: 재고 충분 승인 CONFIRMED
- **메서드명:** `재고_충분_승인_CONFIRMED`
- **@DisplayName:** `"재고가 충분할 때 approve 호출 시 주문 상태가 CONFIRMED로 변경되고 재고가 차감된다"`
- **입력:** stock=10, quantity=5, approve(orderId)
- **기대 결과:** `order.getStatus() == CONFIRMED`, `sample.getStock() == 5`
- **커버 요구사항:** Phase4.md § 승인 처리 규칙 > 재고 충분

### [OrderServiceTest] 사이클 8: 재고 부족 승인 PRODUCING
- **메서드명:** `재고_부족_승인_PRODUCING`
- **@DisplayName:** `"재고가 부족할 때 approve 호출 시 주문 상태가 PRODUCING으로 변경되고 생산큐에 등록된다"`
- **입력:** stock=2, quantity=5, approve(orderId)
- **기대 결과:** `order.getStatus() == PRODUCING`, 생산큐 size=1
- **커버 요구사항:** Phase4.md § 승인 처리 규칙 > 재고 부족

### [OrderServiceTest] 사이클 9: 거절 REJECTED
- **메서드명:** `거절_REJECTED`
- **@DisplayName:** `"reject 호출 시 주문 상태가 REJECTED로 변경된다"`
- **기대 결과:** `order.getStatus() == REJECTED`
- **커버 요구사항:** Phase4.md § 거절 처리 규칙

### [OrderServiceTest] 사이클 10: RESERVED 아닌 주문 승인 예외
- **메서드명:** `RESERVED_아닌_주문_승인_예외`
- **@DisplayName:** `"RESERVED 상태가 아닌 주문을 approve 하면 IllegalStateException이 발생한다"`
- **입력:** CONFIRMED 상태 주문, approve(orderId)
- **기대 결과:** `IllegalStateException`
- **커버 요구사항:** Phase4.md § 승인 처리 규칙 > 대상

### [OrderServiceTest] 사이클 11: 존재하지 않는 주문 승인 예외
- **메서드명:** `존재하지않는_주문_승인_예외`
- **@DisplayName:** `"존재하지 않는 주문 ID로 approve 호출 시 IllegalArgumentException이 발생한다"`
- **입력:** orderId="NONE"
- **기대 결과:** `IllegalArgumentException`
- **커버 요구사항:** Phase4.md § OrderService > approve()

### [OrderServiceTest] 사이클 12: 재고 부족 시 생산량 계산 정확
- **메서드명:** `재고_부족시_생산량_계산_정확`
- **@DisplayName:** `"재고 부족 승인 시 생산 작업의 targetQty가 ceil(부족분 / (yield * 0.9))로 계산된다"`
- **입력:** stock=2, quantity=10, yield=0.8 → shortage=8, targetQty=ceil(8/0.72)=ceil(11.11)=12
- **기대 결과:** `job.getTargetQty() == 12`
- **커버 요구사항:** Phase4.md § 승인 처리 규칙 > 실 생산량 계산

---

## Phase5 TDD 계획

### [StockStatusTest] 사이클 1: 재고 0 고갈
- **메서드명:** `재고0_고갈`
- **@DisplayName:** `"stock이 0이면 StockStatus는 DEPLETED다"`
- **입력:** stock=0, pendingQty=0
- **기대 결과:** `DEPLETED`
- **커버 요구사항:** Phase5.md § 재고 상태 판단 기준

### [StockStatusTest] 사이클 2: 재고 부족
- **메서드명:** `재고부족_부족`
- **@DisplayName:** `"stock이 pendingQty보다 적으면 StockStatus는 SHORTAGE다"`
- **입력:** stock=2, pendingQty=5
- **기대 결과:** `SHORTAGE`
- **커버 요구사항:** Phase5.md § 재고 상태 판단 기준

### [StockStatusTest] 사이클 3: 재고 충분 여유
- **메서드명:** `재고충분_여유`
- **@DisplayName:** `"stock이 pendingQty보다 많으면 StockStatus는 PLENTY다"`
- **입력:** stock=10, pendingQty=3
- **기대 결과:** `PLENTY`
- **커버 요구사항:** Phase5.md § 재고 상태 판단 기준

### [StockStatusTest] 사이클 4: 재고 대기량 동일 여유
- **메서드명:** `재고_대기량_동일_여유`
- **@DisplayName:** `"stock이 pendingQty와 같으면 StockStatus는 PLENTY다"`
- **입력:** stock=5, pendingQty=5
- **기대 결과:** `PLENTY`
- **커버 요구사항:** Phase5.md § 재고 상태 판단 기준

### [MonitorServiceTest] 사이클 5: 주문 현황 상태별 그룹핑
- **메서드명:** `주문현황_상태별_그룹핑`
- **@DisplayName:** `"getOrdersByStatus는 RESERVED/PRODUCING/CONFIRMED/RELEASE 4개 키를 반환하고 REJECTED는 제외한다"`
- **입력:** RESERVED 1건, CONFIRMED 1건, REJECTED 1건 등록
- **기대 결과:** 키 4개(RESERVED/PRODUCING/CONFIRMED/RELEASE), REJECTED 키 없음
- **커버 요구사항:** Phase5.md § 모니터링 대상 주문 상태

### [MonitorServiceTest] 사이클 6: 주문 없는 상태 빈 리스트
- **메서드명:** `주문없는_상태_빈리스트`
- **@DisplayName:** `"해당 상태의 주문이 없으면 빈 리스트가 반환된다"`
- **입력:** RESERVED 1건만 등록
- **기대 결과:** PRODUCING/CONFIRMED/RELEASE → 빈 리스트
- **커버 요구사항:** Phase5.md § MonitorService > getOrdersByStatus()

### [MonitorServiceTest] 사이클 7: REJECTED 주문 제외 확인
- **메서드명:** `REJECTED_주문_제외_확인`
- **@DisplayName:** `"REJECTED 상태 주문은 주문 현황에 포함되지 않는다"`
- **입력:** REJECTED 주문 등록 후 getOrdersByStatus()
- **기대 결과:** 반환된 Map에 REJECTED 키 없음
- **커버 요구사항:** Phase5.md § 모니터링 대상 주문 상태

### [MonitorServiceTest] 사이클 8: 재고 현황 전체 시료 포함
- **메서드명:** `재고현황_전체_시료_포함`
- **@DisplayName:** `"getStockInfos는 등록된 모든 시료에 대한 SampleStockInfo를 반환한다"`
- **입력:** 시료 2개 등록
- **기대 결과:** 크기 2
- **커버 요구사항:** Phase5.md § MonitorService > getStockInfos()

### [MonitorServiceTest] 사이클 9: 대기주문량 RESERVED+PRODUCING 합산
- **메서드명:** `대기주문량_RESERVED_PRODUCING_합산`
- **@DisplayName:** `"pendingQuantity는 RESERVED와 PRODUCING 상태 주문 수량의 합이다"`
- **입력:** RESERVED 수량=3, PRODUCING 수량=4, CONFIRMED 수량=5 등록
- **기대 결과:** `pendingQuantity == 7`
- **커버 요구사항:** Phase5.md § SampleStockInfo > pendingQuantity

### [MonitorServiceTest] 사이클 10: 고갈 판단 정확성
- **메서드명:** `고갈_판단_정확성`
- **@DisplayName:** `"재고가 0이면 StockStatus가 DEPLETED다"`
- **입력:** stock=0인 시료
- **기대 결과:** `stockStatus == DEPLETED`
- **커버 요구사항:** Phase5.md § 재고 상태 판단 기준

### [MonitorServiceTest] 사이클 11: 부족 판단 정확성
- **메서드명:** `부족_판단_정확성`
- **@DisplayName:** `"재고가 대기주문량보다 적으면 StockStatus가 SHORTAGE다"`
- **입력:** stock=2, RESERVED 수량=5인 시료
- **기대 결과:** `stockStatus == SHORTAGE`
- **커버 요구사항:** Phase5.md § 재고 상태 판단 기준

---

## Phase6 TDD 계획 (수동 생산 — 초기 구현)

> `getCurrentJob()` / `getQueueList()`는 Phase4에서 이미 구현됨.
> Phase6은 `processProduction()` / `completeJob()` 구현 및 통합 시나리오에 집중.
> **자동 생산 확장은 하단 "Phase6 자동 생산 TDD 계획" 섹션 참조.**

### [ProductionServiceTest] 사이클 1: 생산 진행 미완료
- **메서드명:** `생산_진행_미완료`
- **@DisplayName:** `"processProduction 호출 후 목표 미달 시 isCompleted는 false이고 producedQty가 증가한다"`
- **입력:** targetQty=4, processProduction(2) 호출
- **기대 결과:** `isCompleted()==false`, `producedQty==2`
- **커버 요구사항:** Phase6.md § 생산 진행 처리

### [ProductionServiceTest] 사이클 2: 생산 진행 완료 재고 증가
- **메서드명:** `생산_진행_완료_재고증가`
- **@DisplayName:** `"생산 완료 시 sample.stock에 targetQty가 추가된다"`
- **입력:** stock=2, targetQty=4, processProduction(4) 호출
- **기대 결과:** `sample.getStock() == 6`
- **커버 요구사항:** Phase6.md § 생산 완료 처리 > addStock

### [ProductionServiceTest] 사이클 3: 생산 완료 주문 상태 CONFIRMED
- **메서드명:** `생산_완료_주문상태_CONFIRMED`
- **@DisplayName:** `"생산 완료 시 주문 상태가 CONFIRMED로 변경된다"`
- **입력:** PRODUCING 상태 주문, processProduction(targetQty)
- **기대 결과:** `order.getStatus() == CONFIRMED`
- **커버 요구사항:** Phase6.md § 생산 완료 처리 > changeStatus(CONFIRMED)

### [ProductionServiceTest] 사이클 4: 생산 완료 큐에서 제거
- **메서드명:** `생산_완료_큐에서_제거`
- **@DisplayName:** `"생산 완료 후 해당 작업이 큐에서 제거된다"`
- **입력:** 작업 1개 등록 후 processProduction(targetQty)
- **기대 결과:** `getCurrentJob().isEmpty()`
- **커버 요구사항:** Phase6.md § 생산 완료 처리 > dequeue

### [ProductionServiceTest] 사이클 5: 복수 큐 완료 후 다음 작업 자동 시작
- **메서드명:** `생산_완료_다음_작업_자동시작`
- **@DisplayName:** `"2개 작업 등록 후 첫 번째 완료 시 다음 작업을 peek할 수 있다"`
- **입력:** job1, job2 enqueue 후 job1 완료
- **기대 결과:** `getCurrentJob().get() == job2`
- **커버 요구사항:** Phase6.md § 생산 완료 처리 > 다음 작업

### [ProductionServiceTest] 사이클 6: 생산 큐 없을 때 진행 예외
- **메서드명:** `생산_큐_없을때_진행_예외`
- **@DisplayName:** `"큐가 비어있을 때 processProduction 호출 시 IllegalStateException이 발생한다"`
- **기대 결과:** `IllegalStateException`
- **커버 요구사항:** Phase6.md § ProductionService > processProduction

### [ProductionServiceTest] 사이클 7: 생산량 초과 입력 처리
- **메서드명:** `생산량_초과_입력_처리`
- **@DisplayName:** `"targetQty를 초과하는 수량 입력 시 targetQty로 보정된다"`
- **입력:** targetQty=4, processProduction(10) 호출
- **기대 결과:** `job.getProducedQty() == 4`, `isCompleted()==true`
- **커버 요구사항:** Phase6.md § 생산 진행 처리 > 보정

### [통합 시나리오] 사이클 8: 재고 부족 승인 후 생산 완료
- **메서드명:** `재고부족_승인_후_생산_완료`
- **@DisplayName:** `"재고 부족으로 PRODUCING된 주문이 생산 완료 후 CONFIRMED로 전환되고 재고가 증가한다"`
- **입력:** stock=2, quantity=5 → approve(재고 부족) → processProduction(targetQty)
- **기대 결과:** `order.getStatus()==CONFIRMED`, `sample.getStock() >= 5`
- **커버 요구사항:** Phase6.md § 재고 흐름 정의

### [통합 시나리오] 사이클 9: 복수 생산 큐 FIFO 처리
- **메서드명:** `복수_생산_큐_FIFO_처리`
- **@DisplayName:** `"2개 작업이 등록된 큐에서 FIFO 순서대로 처리된다"`
- **입력:** job1, job2 enqueue 후 job1 완료 → job2 완료
- **기대 결과:** job1 order CONFIRMED → job2 order CONFIRMED, 큐 비어있음
- **커버 요구사항:** Phase6.md § ProductionService > FIFO 처리

---

## Phase7 TDD 계획

### [OrderTest] 사이클 1: stockDeducted 플래그 기본값 false
- **메서드명:** `stockDeducted_기본값_false`
- **@DisplayName:** `"Order 생성 시 stockDeducted 기본값은 false다"`
- **입력:** Order 생성
- **기대 결과:** `order.isStockDeducted() == false`
- **커버 요구사항:** Phase7.md § Order > stockDeducted

### [OrderTest] 사이클 2: markStockDeducted 호출 후 true
- **메서드명:** `markStockDeducted_호출_후_true`
- **@DisplayName:** `"markStockDeducted() 호출 후 isStockDeducted()는 true를 반환한다"`
- **기대 결과:** `order.isStockDeducted() == true`
- **커버 요구사항:** Phase7.md § Order > markStockDeducted()

### [ReleaseServiceTest] 사이클 3: CONFIRMED 주문 목록 조회
- **메서드명:** `CONFIRMED_주문_목록_조회`
- **@DisplayName:** `"findConfirmed는 CONFIRMED 상태 주문만 반환한다"`
- **입력:** CONFIRMED 2건, RESERVED 1건 등록
- **기대 결과:** 크기 2, 모두 CONFIRMED 상태
- **커버 요구사항:** Phase7.md § ReleaseService > findConfirmed()

### [ReleaseServiceTest] 사이클 4: 출고 처리 RELEASE 전환
- **메서드명:** `출고_처리_RELEASE_전환`
- **@DisplayName:** `"release() 호출 시 주문 상태가 RELEASE로 변경된다"`
- **입력:** CONFIRMED 주문 → release(orderId)
- **기대 결과:** `order.getStatus() == RELEASE`
- **커버 요구사항:** Phase7.md § ReleaseService > release()

### [ReleaseServiceTest] 사이클 5: 재고 부족 경로 출고 재고 차감
- **메서드명:** `재고부족_경로_출고_재고차감`
- **@DisplayName:** `"stockDeducted=false인 CONFIRMED 주문 출고 시 sample.stock이 차감된다"`
- **입력:** stock=5, quantity=3, stockDeducted=false → release()
- **기대 결과:** `sample.getStock() == 2`
- **커버 요구사항:** Phase7.md § 출고 처리 규칙 > 재고 부족 경로

### [ReleaseServiceTest] 사이클 6: 재고 충분 경로 출고 차감 없음
- **메서드명:** `재고충분_경로_출고_차감없음`
- **@DisplayName:** `"stockDeducted=true인 CONFIRMED 주문 출고 시 stock이 차감되지 않는다"`
- **입력:** stock=5, quantity=3, stockDeducted=true → release()
- **기대 결과:** `sample.getStock() == 5`
- **커버 요구사항:** Phase7.md § 출고 처리 규칙 > 재고 충분 경로

### [ReleaseServiceTest] 사이클 7: CONFIRMED 아닌 주문 출고 예외
- **메서드명:** `CONFIRMED_아닌_주문_출고_예외`
- **@DisplayName:** `"CONFIRMED 상태가 아닌 주문을 release하면 IllegalStateException이 발생한다"`
- **입력:** RESERVED 상태 주문 → release()
- **기대 결과:** `IllegalStateException`
- **커버 요구사항:** Phase7.md § ReleaseService > release()

### [ReleaseServiceTest] 사이클 8: 존재하지 않는 주문 출고 예외
- **메서드명:** `존재하지않는_주문_출고_예외`
- **@DisplayName:** `"존재하지 않는 주문 ID로 release 호출 시 IllegalArgumentException이 발생한다"`
- **입력:** orderId="NONE"
- **기대 결과:** `IllegalArgumentException`
- **커버 요구사항:** Phase7.md § ReleaseService > release()

### [ReleaseServiceTest] 사이클 9: 출고 후 CONFIRMED 목록에서 제거
- **메서드명:** `출고_후_CONFIRMED_목록에서_제거`
- **@DisplayName:** `"출고 처리 후 해당 주문이 findConfirmed() 목록에서 제외된다"`
- **입력:** CONFIRMED 주문 1건 → release() → findConfirmed()
- **기대 결과:** 빈 리스트
- **커버 요구사항:** Phase7.md § ReleaseService > findConfirmed()

### [통합 시나리오] 사이클 10: 전체 흐름 재고 충분
- **메서드명:** `전체_흐름_재고충분`
- **@DisplayName:** `"재고 충분 경로에서 출고 시 재고는 approve 시 1회만 차감된다"`
- **입력:** stock=10, qty=5 → approve(CONFIRMED) → release()
- **기대 결과:** `order.getStatus()==RELEASE`, `sample.getStock()==5` (1회만 차감)
- **커버 요구사항:** Phase7.md § 경로별 재고 차감 시점

### [통합 시나리오] 사이클 11: 전체 흐름 재고 부족
- **메서드명:** `전체_흐름_재고부족`
- **@DisplayName:** `"재고 부족 경로에서 출고 시 출고 시점에 재고가 차감된다"`
- **입력:** stock=2, qty=5 → approve(PRODUCING) → processProduction → release()
- **기대 결과:** `order.getStatus()==RELEASE`, 재고 차감 정상
- **커버 요구사항:** Phase7.md § 경로별 재고 차감 시점

### [통합 시나리오] 사이클 12: 복수 주문 순차 출고
- **메서드명:** `복수_주문_순차_출고`
- **@DisplayName:** `"여러 CONFIRMED 주문을 순차적으로 출고할 수 있다"`
- **입력:** CONFIRMED 2건 → 각각 release()
- **기대 결과:** 모두 RELEASE, findConfirmed() 빈 리스트
- **커버 요구사항:** Phase7.md § 복수 주문 출고

---

## Phase9 TDD 계획

### 현재 사이클: 9 / 9 (완료)

### [사이클 1] ProductionResultTest — 값 객체 직접 검증 ✅
- `completed_true_반환` / `job_참조_반환` — PASS

### [사이클 2] InMemory 구현체 엣지케이스 ✅
- `InMemoryOrderRepositoryTest`: generateId 순번·덮어쓰기·빈 결과·Optional.empty — PASS
- `InMemoryProductionQueueRepositoryTest`: generateJobId 순번·빈 큐·FIFO·방어적 복사 — PASS
- `InMemorySampleRepositoryTest`: existsById·existsByName·count·totalStock — PASS

### [사이클 3] InputHandlerTest — 입력 처리 확장 ✅
- `readString_빈_문자열_반환` / `readString_앞뒤_공백_trim` / `readInt_비숫자_후_재입력` / `readInt_음수_허용` / `readDouble_비숫자_후_재입력` — PASS

### [사이클 4] ConsoleMenuTest — 메인 메뉴 UI ✅
- `displaySummary_시료_없음` / `displayMainMenu_출력` / `run_종료_선택` / `run_잘못된_선택_후_종료` / `displaySummary_시료_있음` — PASS

### [사이클 5] SampleUITest — 시료 관리 UI ✅
- 등록 성공·중복 오류·목록 빈/데이터·검색 결과 있음/없음 — PASS

### [사이클 6] OrderUITest — 주문 UI ✅
- 예약 성공·없는 시료 오류·접수 없음·승인(재고 충분/부족)·거절·잘못된 주문번호 — PASS

### [사이클 7] MonitorUITest — 모니터링 UI ✅
- 주문 현황 빈/그룹·재고 레이블 여유/고갈 — PASS

### [사이클 8] ProductionUITest — 생산 라인 UI ✅
- 현재 작업 없음/있음·대기 목록 빈·메뉴_생산진행_항목_없음·메뉴_3선택_잘못된_선택_안내 — PASS

### [사이클 9] ReleaseUITest — 출고 처리 UI ✅
- 출고 대기 없음·처리 성공·잘못된 주문번호·목록 출력 — PASS

---

## Phase8 TDD 계획

### 현재 사이클: 18 / 18 (완료)

### [DatabaseConfigTest] 사이클 1: DB 연결 정상 ✅
- **메서드명:** `데이터베이스_연결_정상` — PASS

### [SchemaInitializerTest] 사이클 2: 스키마 초기화 테이블 생성 ✅
- **메서드명:** `스키마_초기화_테이블_생성` — PASS

### [SchemaInitializerTest] 사이클 3: 중복 초기화 오류 없음 ✅
- **메서드명:** `스키마_중복_초기화_오류없음` — PASS

### [JdbcSampleRepositoryTest] 사이클 4: 시료 저장 후 조회
- **메서드명:** `시료_저장_후_ID로_조회`
- **@DisplayName:** `"save 후 findById로 동일한 시료가 반환된다"`
- **입력:** Sample(id="S001", name="알파센서", avgProductionTime=30, yield=0.9, stock=10) → save → findById("S001")
- **기대 결과:** id, name, avgProductionTime, yield, stock 모두 일치
- **커버 요구사항:** Phase8.md § 사이클 4

**선행 리팩토링 (기존 테스트로 검증)**
1. `SampleRepository` 인터페이스 추출
2. 기존 `SampleRepository` → `inmemory/InMemorySampleRepository`로 이동·개명
3. 기존 테스트·서비스의 참조 업데이트
4. `jdbc/JdbcSampleRepository` 골격 생성

### [JdbcSampleRepositoryTest] 사이클 5: 재고 변경 DB 반영 ✅
- **메서드명:** `재고_변경_DB_반영` — PASS

### [JdbcSampleRepositoryTest] 사이클 6: 전체 시료 조회 등록순 보장 ✅
- **메서드명:** `전체_시료_조회_등록순_보장` — PASS

### [JdbcSampleRepositoryTest] 사이클 7: 이름 부분 일치 검색 ✅
- **메서드명:** `이름_부분일치_검색` — PASS

### [JdbcSampleRepositoryTest] 사이클 8: count/totalStock 정확성 ✅
- **메서드명:** `count_totalStock_정확성` — PASS

### [JdbcOrderRepositoryTest] 사이클 9: 주문 저장 후 ID로 조회
- **메서드명:** `주문_저장_후_ID로_조회`
- **@DisplayName:** `"save 후 findById로 동일한 주문이 반환된다"`
- **입력:** Sample+Order save → findById(orderId)
- **기대 결과:** orderId, customerName, quantity, status, stockDeducted 일치
- **커버 요구사항:** Phase8.md § 사이클 9

**선행 리팩토링 (사이클 6~9 완료)**
1. `OrderRepository` 인터페이스 추출 ✅
2. `inmemory/InMemoryOrderRepository` 생성 ✅
3. 기존 테스트·Main.java 참조 업데이트 ✅

### [JdbcOrderRepositoryTest] 사이클 10: 상태 변경 DB 반영
- **메서드명:** `상태_변경_DB_반영`
- **@DisplayName:** `"changeStatus 후 save하면 DB의 status 값이 갱신된다"`
- **입력:** Order(RESERVED) save → changeStatus(CONFIRMED) → save → findById
- **기대 결과:** `status == CONFIRMED`
- **커버 요구사항:** Phase8.md § 사이클 10

### [JdbcOrderRepositoryTest] 사이클 11: stockDeducted 플래그 반영
- **메서드명:** `stockDeducted_플래그_DB_반영`
- **@DisplayName:** `"markStockDeducted 후 save하면 DB의 stock_deducted가 true로 저장된다"`
- **입력:** Order save → markStockDeducted() → save → findById
- **기대 결과:** `isStockDeducted() == true`
- **커버 요구사항:** Phase8.md § 사이클 11

### [JdbcOrderRepositoryTest] 사이클 12: ID 자동생성 순번 증가
- **메서드명:** `ID_자동생성_순번_증가`
- **@DisplayName:** `"generateId는 ORD-0001, ORD-0002 순으로 순번이 증가한다"`
- **기대 결과:** 첫 번째 "ORD-0001", 두 번째 "ORD-0002"
- **커버 요구사항:** Phase8.md § 사이클 12

### [JdbcOrderRepositoryTest] 사이클 13: 상태별 주문 조회
- **메서드명:** `상태별_주문_조회`
- **@DisplayName:** `"findByStatus는 해당 상태의 주문만 반환한다"`
- **입력:** RESERVED 2건, CONFIRMED 1건 저장 후 findByStatus(RESERVED)
- **기대 결과:** 크기 2, 모두 RESERVED 상태
- **커버 요구사항:** Phase8.md § 사이클 13

### [리팩토링] 사이클 14: ProductionQueueRepository 인터페이스 추출
- `ProductionQueueRepository` → 인터페이스로 변환
- `InMemoryProductionQueueRepository` 생성 (기존 구현 이동) ✅
- 기존 테스트 참조 업데이트 (컴파일 에러 → GREEN 확인) ✅

### [ProductionJobTest] 사이클 15: ProductionJob.restore() 정적 팩토리
- **메서드명:** `restore_정적팩토리_DB_재구성`
- **@DisplayName:** `"restore()로 생성한 ProductionJob은 주입된 targetQty/producedQty/totalTime을 그대로 반환한다"`
- **입력:** restore(jobId, order, targetQty=5, producedQty=3, totalTime=150)
- **기대 결과:** 각 getter 일치
- **커버 요구사항:** Phase8.md § 사이클 15

### [JdbcProductionQueueRepositoryTest] 사이클 16: 작업 저장 후 enqueue 순서 조회
- **메서드명:** `작업_저장_후_enqueue_순서_조회`
- **@DisplayName:** `"enqueue 후 peek으로 첫 번째 작업이 반환되고 FIFO 순서가 보장된다"`
- **입력:** job1, job2 enqueue 후 peek/dequeue
- **기대 결과:** peek → job1, dequeue → job1, peek → job2
- **커버 요구사항:** Phase8.md § 사이클 16

### [JdbcProductionQueueRepositoryTest] 사이클 17: producedQty 업데이트 DB 반영
- **메서드명:** `생산_진행_후_producedQty_DB_반영`
- **@DisplayName:** `"enqueue(job) 재호출 시 producedQty가 DB에 갱신된다"`
- **입력:** enqueue → produce(2) → enqueue(재호출) → peek
- **기대 결과:** peek().get().getProducedQty() == 2
- **커버 요구사항:** Phase8.md § 사이클 17

### [통합 테스트] 사이클 18: DB 재연결 후 데이터 유지
- **메서드명:** `DB_재연결_후_데이터_유지`
- **@DisplayName:** `"DB 재연결 후에도 저장된 시료와 주문 데이터가 유지된다"`
- **입력:** 파일 DB에 시료/주문 저장 → DB 닫기 → 재연결 → findAll
- **기대 결과:** 재연결 후에도 동일한 데이터 반환
- **커버 요구사항:** Phase8.md § 사이클 18

---

## Phase6 자동 생산 TDD 계획 (추가 구현)

> `ScheduledExecutorService` 기반 백그라운드 자동 생산.  
> `avgProductionTime(분)` = 실제 `N초`로 압축 시뮬레이션 (1분 → 1초).

### 사이클 1 — ProductionJob: startTime 필드

**파일:** `ProductionJobTest.java` (기존에 추가)

| # | 메서드명 | DisplayName | 기대 결과 |
|---|---------|-------------|----------|
| 1 | `startTime_기본값_null` | "새로 생성된 ProductionJob의 startTime은 null이다" | `getStartTime() == null` |
| 2 | `startTime_설정_후_조회` | "setStartTime 호출 후 getStartTime이 동일 Instant를 반환한다" | `getStartTime() == 주입값` |

**구현 대상:** `ProductionJob` — `Instant startTime` 필드, `setStartTime()`, `getStartTime()` 추가

### 사이클 2 — ProductionJob: calcProducedByElapsed()

**파일:** `ProductionJobTest.java` (기존에 추가)

| # | 메서드명 | DisplayName | 기대 결과 |
|---|---------|-------------|----------|
| 3 | `경과시간_기반_생산량_계산` | "avgTime=30, 경과60초 → 생산량 2개" | `calcProducedByElapsed(60) == 2` |
| 4 | `경과시간_부족_생산량_0` | "avgTime=30, 경과10초 → 생산량 0개" | `calcProducedByElapsed(10) == 0` |
| 5 | `경과시간_초과_시_targetQty로_보정` | "충분한 경과 시간이어도 targetQty를 초과하지 않는다" | `calcProducedByElapsed(9999) == targetQty` |

**구현 대상:** `ProductionJob.calcProducedByElapsed(long elapsedSeconds)` 추가
- 공식: `min(targetQty, (int)(elapsedSeconds / avgProductionTime))`

### 사이클 3 — ProductionService: processAutoProduction()

**파일:** `AutoProductionServiceTest.java` (신규)

| # | 메서드명 | DisplayName | 기대 결과 |
|---|---------|-------------|----------|
| 6 | `빈_큐_processAutoProduction_무시` | "큐가 비어있을 때 processAutoProduction 호출 시 예외 없이 종료된다" | 예외 없음 |
| 7 | `startTime_없을때_자동설정` | "startTime이 null이면 processAutoProduction 호출 시 현재 시각으로 자동 설정된다" | `getStartTime() != null` |
| 8 | `경과시간_기반_생산량_자동_갱신` | "과거 startTime 설정 시 processAutoProduction 호출로 생산량이 증가한다" | `producedQty > 0` |
| 9 | `자동생산_완료_재고증가_CONFIRMED` | "자동 생산 완료 시 재고가 추가되고 주문이 CONFIRMED로 변경된다" | `stock 증가, status == CONFIRMED` |
| 10 | `자동생산_완료_후_다음작업_startTime_자동설정` | "첫 번째 작업 완료 후 다음 작업의 startTime이 자동 설정된다" | `job2.getStartTime() != null` |

**구현 대상:** `ProductionService.processAutoProduction()` public 메서드

### 사이클 4 — ProductionService: 스케줄러 + shutdown()

**파일:** `AutoProductionServiceTest.java` (추가)

| # | 메서드명 | DisplayName | 기대 결과 |
|---|---------|-------------|----------|
| 11 | `createJob_후_스케줄러_자동생산_완료` | "createJob 후 실제 시간 경과 시 자동으로 생산이 완료된다" | `status == CONFIRMED` |
| 12 | `shutdown_후_스케줄러_종료` | "shutdown 호출 후 스케줄러가 종료된다" | `isShutdown == true` |

**구현 대상:** 스케줄러 내장 + `shutdown()` + 테스트용 생성자 `ProductionService(queueRepo, orderRepo, long periodMillis)`

### 사이클 5 — ProductionUI: 수동 메뉴 제거

**파일:** `ProductionUITest.java` (수정)

| # | 메서드명 | DisplayName | 기대 결과 |
|---|---------|-------------|----------|
| 13 | `메뉴_생산진행_항목_없음` | "생산 라인 메뉴에 '생산 진행' 항목이 표시되지 않는다" | `"3. 생산 진행"` 미포함 |
| 14 | `메뉴_3선택_잘못된_선택_안내` | "메뉴 3 선택 시 올바른 메뉴 안내 후 종료된다" | `"올바른 메뉴"` 포함 |

**구현 대상:** `ProductionUI` — case 3 제거, 메뉴 항목 제거
