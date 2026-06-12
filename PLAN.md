# S-Semi 반도체 시료생산 주문관리 시스템 개발 계획

## 프로젝트 개요
- 콘솔 기반 반도체 시료생산 주문관리 시스템
- Java 17, JUnit 5, Gradle
- TDD(Test-Driven Development) 방법론 적용

## 개발 Phase 구성

| Phase | 기능 | 설계 문서 | 상태 |
|-------|------|-----------|------|
| Phase1 | 기반 구조 + 메인 메뉴 | [Phase1.md](docs/design/Phase1.md) | 미시작 |
| Phase2 | 시료 관리 | [Phase2.md](docs/design/Phase2.md) | 미시작 |
| Phase3 | 시료 주문(예약) | [Phase3.md](docs/design/Phase3.md) | 미시작 |
| Phase4 | 주문 승인/거절 | [Phase4.md](docs/design/Phase4.md) | 진행중 |
| Phase5 | 모니터링 | [Phase5.md](docs/design/Phase5.md) | 미시작 |
| Phase6 | 생산라인 | [Phase6.md](docs/design/Phase6.md) | 미시작 |
| Phase7 | 출고처리 | [Phase7.md](docs/design/Phase7.md) | 미시작 |

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
