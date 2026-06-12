# Phase4: 주문 승인/거절

## 1. 기능 개요
접수된 주문(RESERVED)에 대해 담당자가 승인 또는 거절을 처리.
- 승인: 재고 상황에 따라 자동으로 2가지 방식 처리
  - 재고 충분 → 즉시 `CONFIRMED`
  - 재고 부족 → 생산라인 등록 + `PRODUCING`
- 거절: 즉시 `REJECTED`

## 2. 도메인 규칙

### 승인 처리 규칙
- 대상: `RESERVED` 상태 주문만
- 재고 판단: `sample.stock >= order.quantity`
  - 충분 시: `sample.deductStock(quantity)` → `order.changeStatus(CONFIRMED)`
  - 부족 시: 생산라인에 등록 → `order.changeStatus(PRODUCING)`
    - 실 생산량: `ceil(부족분 / (yield * 0.9))`
    - 부족분 = `order.quantity - sample.stock`
    - 총 생산시간: `sample.avgProductionTime * targetQty`

### 거절 처리 규칙
- 대상: `RESERVED` 상태 주문만
- `order.changeStatus(REJECTED)`
- 재고 변화 없음

## 3. 클래스 설계

### ProductionJob (도메인)
```java
public class ProductionJob {
    private final String jobId;
    private final Order order;
    private final int targetQty;    // ceil(부족분 / (yield * 0.9))
    private int producedQty;        // 현재까지 생산된 수량
    private final int totalTime;    // avgProductionTime * targetQty (분)

    + getJobId(): String
    + getOrder(): Order
    + getTargetQty(): int
    + getProducedQty(): int
    + getTotalTime(): int
    + isCompleted(): boolean        // producedQty >= targetQty
    + produce(qty: int): void       // 생산량 누적
}
```

### ProductionQueueRepository
```java
public class ProductionQueueRepository {
    private final Queue<ProductionJob> queue = new LinkedList<>()  // FIFO
    private final List<ProductionJob> all = new ArrayList<>()
    private int sequence = 1

    + enqueue(job: ProductionJob): void
    + peek(): Optional<ProductionJob>   // 현재 생산 중인 작업
    + dequeue(): Optional<ProductionJob>
    + getQueue(): List<ProductionJob>   // 대기 목록 (불변 뷰)
    + generateJobId(): String           // "JOB-" + String.format("%04d", seq++)
}
```

### OrderService (확장)
```java
// Phase3에 추가
+ approve(orderId: String): Order
+ reject(orderId: String): Order
```

### ProductionService
```java
public class ProductionService {
    - productionQueueRepo: ProductionQueueRepository
    - orderRepo: OrderRepository

    + createJob(order: Order): ProductionJob
    + getCurrentJob(): Optional<ProductionJob>
    + getQueueList(): List<ProductionJob>
}
```

## 4. 화면 구성

### 주문 승인/거절 화면
```
[접수된 주문 목록 - RESERVED]
------------------------------------------------------------
 주문번호  | 시료명     | 고객명  | 수량 | 재고 현황
------------------------------------------------------------
 ORD-0001 | 알파센서   | 홍길동 |   5 |  재고:2개 (부족)
 ORD-0002 | 베타칩     | 김철수 |   3 |  재고:8개 (여유)
------------------------------------------------------------

처리할 주문번호 > ORD-0001
  1. 승인
  2. 거절
  0. 취소
선택 > 1

→ [재고 부족] 생산라인에 등록되었습니다.
   주문번호: ORD-0001 | 상태: PRODUCING
   실 생산량: 4개 | 예상 생산시간: 120분
```

### 재고 충분 시
```
→ [재고 충분] 즉시 출고 대기 상태로 전환되었습니다.
   주문번호: ORD-0002 | 상태: CONFIRMED
```

### 거절 처리
```
→ 주문이 거절되었습니다.
   주문번호: ORD-0001 | 상태: REJECTED
```

## 5. 시퀀스 흐름

### 승인 (재고 충분)
```
OrderUI → OrderService.approve(orderId)
  → OrderRepository.findById() → RESERVED 확인
  → sample.hasEnoughStock(quantity)? YES
  → sample.deductStock(quantity)
  → order.changeStatus(CONFIRMED)
  → OrderRepository.save()
  → 결과 반환
```

### 승인 (재고 부족)
```
OrderUI → OrderService.approve(orderId)
  → OrderRepository.findById() → RESERVED 확인
  → sample.hasEnoughStock(quantity)? NO
  → order.changeStatus(PRODUCING)
  → ProductionService.createJob(order)
      → targetQty = ceil((qty - stock) / (yield * 0.9))
      → totalTime = avgTime * targetQty
      → new ProductionJob(...)
      → ProductionQueueRepo.enqueue(job)
  → 결과 반환
```

### 거절
```
OrderUI → OrderService.reject(orderId)
  → OrderRepository.findById() → RESERVED 확인
  → order.changeStatus(REJECTED)
  → 결과 반환
```

## 6. TDD 테스트 계획

### ProductionJobTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `실생산량_계산_정확성` | ceil(부족분 / (yield * 0.9)) 수식 검증 |
| `총생산시간_계산` | avgTime * targetQty |
| `생산_완료_여부_확인` | producedQty >= targetQty 시 true |
| `생산량_누적` | produce(3) → producedQty += 3 |

### ProductionQueueRepositoryTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `작업_enqueue_후_peek_동일` | 첫 번째 작업 peek 확인 |
| `FIFO_순서_보장` | enqueue 순서대로 dequeue |
| `빈_큐_peek_빈값` | Optional.empty() 반환 |
| `대기목록_조회` | getQueue() 현재 큐 상태 반환 |

### OrderServiceTest (승인/거절)
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `재고_충분_승인_CONFIRMED` | approve() → status=CONFIRMED, 재고 차감 |
| `재고_부족_승인_PRODUCING` | approve() → status=PRODUCING, 생산큐 등록 |
| `거절_REJECTED` | reject() → status=REJECTED |
| `RESERVED_아닌_주문_승인_예외` | CONFIRMED 상태 주문 approve() 시 예외 |
| `존재하지않는_주문_승인_예외` | 없는 orderId 시 IllegalArgumentException |
| `재고_부족시_생산량_계산_정확` | 부족분 기반 targetQty 계산 검증 |

## 7. 구현 순서 (TDD 사이클)
1. `ProductionJob` 생산량 계산 테스트 → 구현
2. `ProductionQueueRepository` 테스트 → 구현
3. `ProductionService.createJob()` 테스트 → 구현
4. `OrderService.approve()` / `reject()` 테스트 → 구현
5. `OrderUI` 승인/거절 화면 구현
