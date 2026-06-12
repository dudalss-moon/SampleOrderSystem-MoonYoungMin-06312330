# Phase6: 생산라인

## 1. 기능 개요

생산라인에 대한 정보를 표시하고, **시료의 평균생산시간을 기반으로 자동 생산을 진행**.

- 현재 생산 중인 시료 정보 표기 (실시간 생산 진행 상황 포함)
- 대기 중인 생산큐 목록 출력 (FIFO)
- **자동 생산**: 생산큐에 작업이 등록되는 순간 백그라운드에서 `avgProductionTime`을 기반으로 자동으로 생산이 진행됨
- 생산 완료 시 재고 자동 추가 + 주문 CONFIRMED 전환 + 다음 작업 자동 시작

---

## 2. 도메인 규칙

### 자동 생산 진행 규칙

- 생산큐에 작업이 `enqueue` 되면 `ProductionJob`에 **시작 시각(`startTime`)을 기록**
- 시료 1개 생산 소요 시간 = `sample.avgProductionTime` (분)
- 경과 시간 기반 생산량 계산:
  ```
  producedQty = min(targetQty, floor(경과시간(분) / avgProductionTime))
  ```
- `ProductionService`는 백그라운드 스레드(`ScheduledExecutorService`)를 통해 주기적으로 생산 진행 상태를 갱신
- 갱신 주기: `avgProductionTime`초 (실제 시간 기준, 분 단위 생산을 초 단위로 압축)
- 생산 완료 조건: `producedQty >= targetQty`

### 생산 완료 처리

생산 완료 시 다음을 **자동으로** 처리:

1. `sample.addStock(targetQty)` — 생산된 수량을 재고에 추가
2. `order.changeStatus(CONFIRMED)` — 주문 상태 PRODUCING → CONFIRMED
3. `queueRepo.dequeue()` — 현재 작업을 큐에서 제거
4. 다음 작업이 있으면 해당 작업의 `startTime`을 기록하고 자동 생산 계속 진행

> **재고 흐름 정의**
> - 생산 완료: `sample.addStock(targetQty)` (재고 증가만)
> - 승인(재고 충분, Phase4): `sample.deductStock(quantity)` 즉시 수행 → CONFIRMED
> - 승인(재고 부족, Phase4): 재고 차감 없이 PRODUCING → 생산 완료 후 재고 증가만
> - 출고(Phase7): CONFIRMED 주문의 `sample.deductStock(quantity)` 수행 → RELEASE

---

## 3. 클래스 설계

### ProductionJob (도메인 — 확장)

```java
public class ProductionJob {
    private final String jobId;
    private final Order order;
    private final int targetQty;
    private int producedQty;
    private final int totalTime;       // avgProductionTime * targetQty (분)
    private Instant startTime;         // 생산 시작 시각 (enqueue 시 기록)

    + getJobId(): String
    + getOrder(): Order
    + getTargetQty(): int
    + getProducedQty(): int
    + getTotalTime(): int
    + getStartTime(): Instant
    + setStartTime(Instant): void
    + isCompleted(): boolean           // producedQty >= targetQty
    + produce(qty: int): void          // 생산량 누적 (최대 targetQty)
    + calcProducedByElapsed(minutesElapsed: long): int
                                       // floor(minutesElapsed / avgProductionTime)
}
```

### ProductionQueueRepository (변경 없음)

```java
+ enqueue(job: ProductionJob): void   // enqueue 시 job.startTime 기록
+ peek(): Optional<ProductionJob>
+ dequeue(): Optional<ProductionJob>
+ getQueue(): List<ProductionJob>
+ generateJobId(): String
```

> `enqueue()` 내부에서 첫 번째 작업인 경우 `job.setStartTime(Instant.now())` 호출

### ProductionService (확장)

```java
public class ProductionService {
    - queueRepo: ProductionQueueRepository
    - orderRepo: OrderRepository
    - sampleRepo: SampleRepository              // 생산 완료 시 재고 DB 반영
    - scheduler: ScheduledExecutorService       // 자동 생산 스케줄러

    // 스케줄러 없음 (테스트·수동 생산용)
    + ProductionService(queueRepo, orderRepo, sampleRepo)
    // 스케줄러 활성화 (운영용) — ConsoleMenu 5-arg 생성자에서 사용
    + ProductionService(queueRepo, orderRepo, sampleRepo, periodMillis: long)

    + createJob(order: Order): ProductionJob
    + getCurrentJob(): Optional<ProductionJob>
    + getQueueList(): List<ProductionJob>
    + processAutoProduction(): void              // 스케줄러가 주기적으로 호출
    + completeJob(job: ProductionJob): void      // 완료 처리 (재고·주문 DB 반영 포함)
    + shutdown(): void                           // 스케줄러 종료 (애플리케이션 종료 시)
    + isSchedulerShutdown(): boolean
}
```

**스케줄러 동작 (ConsoleMenu → Main.java에서 1000ms 주기로 활성화):**

```java
// ConsoleMenu 5-arg 생성자에서 시작 (schedulerPeriodMillis > 0 인 경우)
scheduler = Executors.newSingleThreadScheduledExecutor();
scheduler.scheduleAtFixedRate(
    this::processAutoProduction,
    0,
    1000,        // 1000ms (1초)마다 실행 (1초 = 1분으로 압축)
    TimeUnit.MILLISECONDS
);
```

**processAutoProduction() 로직:**

```java
void processAutoProduction() {
    Optional<ProductionJob> current = queueRepo.peek();
    if (current.isEmpty()) return;

    ProductionJob job = current.get();
    if (job.getStartTime() == null) {
        job.setStartTime(Instant.now());
        return;
    }

    long elapsedSeconds = Duration.between(job.getStartTime(), Instant.now()).toSeconds();
    int newProduced = job.calcProducedByElapsed(elapsedSeconds);

    if (newProduced > job.getProducedQty()) {
        job.produce(newProduced - job.getProducedQty());
    }

    if (job.isCompleted()) {
        completeJob(job);
        queueRepo.peek().ifPresent(next -> next.setStartTime(Instant.now()));
    }
}
```

**completeJob() 로직 — DB 저장 포함:**

```java
void completeJob(ProductionJob job) {
    Order order = job.getOrder();
    order.getSample().addStock(job.getTargetQty());
    sampleRepository.save(order.getSample());   // 재고 DB 반영
    order.changeStatus(CONFIRMED);
    orderRepository.save(order);               // 주문 상태 DB 반영
    queueRepo.dequeue();
}
```

> **주의:** `sampleRepository.save()` 와 `orderRepository.save()` 를 반드시 호출해야
> JDBC 환경에서 모니터링이 PRODUCING → CONFIRMED 전환을 정확히 반영한다.

### ConsoleMenu (스케줄러 연동)

```java
// 운영 환경 (Main.java): 1초 주기 스케줄러 활성화
new ConsoleMenu(input, sampleRepo, orderRepo, queueRepo, 1000L)

// 테스트 환경: 스케줄러 없음
new ConsoleMenu(input, sampleRepo, orderRepo)              // 3-arg
new ConsoleMenu(input, sampleRepo, orderRepo, queueRepo)   // 4-arg
```

- `run()` 종료 시 `productionService.shutdown()` 자동 호출 (try-finally)

### ProductionUI (변경)

```java
public class ProductionUI {
    - productionService: ProductionService
    - input: InputHandler

    + show(): void
    - displayCurrentJob(): void    // 실시간 진행 현황 표시
    - displayQueue(): void         // 대기 목록 표시
    // processProduction() 제거 — 자동 생산으로 대체
}
```

---

## 4. 화면 구성

### 생산라인 메뉴

```
========================================
              생산 라인
========================================
  1. 현재 생산 현황
  2. 생산 대기 목록
  0. 메인 메뉴로
----------------------------------------
선택 > 
```

> **"3. 생산 진행" 항목 제거** — 생산은 자동으로 진행되므로 수동 입력 불필요

### 현재 생산 현황 (자동 갱신)

```
[현재 생산 중]
------------------------------------------------------------
 작업 ID  : JOB-0001
 주문번호  : ORD-0001
 시료명    : 알파센서
 고객명    : 홍길동
 주문 수량 : 5개
 실 생산량 : 4개 (목표)
 현재 생산 : 2개  ← 경과 시간 기반 자동 계산
 예상 시간 : 120분 (총)
 생산 시작 : 2026-06-12 10:30:00
------------------------------------------------------------
※ 생산 대기 중인 작업: 2건
```

### 생산 완료 알림 (자동, 다음 현황 조회 시 반영)

```
[자동 생산 완료]
→ 알파센서 4개가 재고에 추가되었습니다.
→ 주문 ORD-0001 상태가 CONFIRMED으로 자동 변경되었습니다.
→ 다음 작업 JOB-0002 생산을 자동 시작합니다.
```

### 생산 큐 없을 때

```
[현재 생산 중인 작업이 없습니다.]
```

---

## 5. 시퀀스 흐름

### 자동 생산 등록 (Phase4 승인 시 연계)

```
OrderService.approve(orderId)
  → sample.hasEnoughStock()? NO
  → order.changeStatus(PRODUCING)
  → ProductionService.createJob(order)
      → targetQty = ceil((qty - stock) / (yield * 0.9))
      → totalTime = avgTime * targetQty
      → new ProductionJob(jobId, order, shortage)
      → queueRepo.enqueue(job)   ← startTime 기록
      → 스케줄러 미시작 시 시작
  → 이후 스케줄러가 자동으로 생산 진행
```

### 자동 생산 진행 (백그라운드 스케줄러)

```
[매 1초] ProductionService.processAutoProduction()
  → queueRepo.peek() → 현재 작업 확인
  → 경과 시간(초) / avgProductionTime(분) = 생산된 수량
  → job.produce(증분) 호출
  → job.isCompleted()? YES
      → sample.addStock(targetQty)       ← 재고 자동 추가
      → order.changeStatus(CONFIRMED)    ← 주문 자동 CONFIRMED
      → orderRepo.save(order)
      → queueRepo.dequeue()
      → 다음 작업 있으면 startTime 기록 후 계속
```

### 생산 현황 조회 (UI)

```
ProductionUI.displayCurrentJob()
  → ProductionService.getCurrentJob() → 현재 작업 (경과 시간 반영된 최신 상태)
  → 작업 정보 + 현재까지 생산량 출력
```

---

## 6. TDD 테스트 계획

### ProductionJobTest (추가)

| 테스트 케이스 | 검증 내용 |
|---|---|
| `startTime_기록_후_경과시간_생산량_계산` | `calcProducedByElapsed(60)`, avgTime=30 → 2개 |
| `경과시간_초과_targetQty_보정` | 경과시간 충분 시 producedQty = targetQty |

### ProductionServiceTest (추가)

| 테스트 케이스 | 검증 내용 |
|---|---|
| `생산_진행_미완료` | produce() 후 isCompleted=false, producedQty 증가 |
| `생산_진행_완료_재고증가` | 완료 시 sample.stock += targetQty |
| `생산_완료_주문상태_CONFIRMED` | order.status → CONFIRMED |
| `생산_완료_큐에서_제거` | 완료 후 getCurrentJob() 다음 작업 반환 |
| `생산_완료_다음_작업_자동시작` | 2개 큐 등록 후 첫 완료 시 다음 작업 peek |
| `생산_큐_없을때_자동생산_무시` | 큐 비어있을 때 processAutoProduction() 정상 종료 |
| `생산량_초과_입력_처리` | targetQty 초과 시 targetQty로 보정 |

### 통합 시나리오 테스트

| 시나리오 | 검증 내용 |
|---|---|
| `재고부족_승인_후_자동생산_완료` | PRODUCING → 자동생산 → CONFIRMED, 재고 증가 |
| `복수_생산_큐_FIFO_자동처리` | 2개 작업 등록 후 순서대로 자동 처리 |

---

## 7. 구현 순서 (TDD 사이클)

1. `ProductionJob` — `startTime` 필드 및 `calcProducedByElapsed()` 테스트 → 구현
2. `ProductionService.processAutoProduction()` 테스트 → 구현
3. `ProductionService.completeJob()` 테스트 → 구현
4. `ScheduledExecutorService` 통합 — `createJob()` 시 스케줄러 시작
5. `ProductionUI` — 메뉴에서 "생산 진행" 항목 제거, 현황 표시에 실시간 진행량 반영
6. 통합 시나리오 테스트 작성 및 검증
7. `Main.java` — 애플리케이션 종료 시 `ProductionService.shutdown()` 호출
