# Phase6: 생산라인

## 1. 기능 개요
생산라인에 대한 정보를 표시하고 생산을 진행.
- 현재 생산 중인 시료 정보 표기
- 대기 중인 생산큐 목록 출력 (FIFO)
- 생산 실행: 현재 생산 작업을 진행 → 완료 시 재고 추가 + 주문 CONFIRMED 전환

## 2. 도메인 규칙

### 생산 완료 처리
- 생산 완료 조건: `producedQty >= targetQty`
- 생산 완료 시:
  1. `sample.addStock(targetQty)` - 생산된 수량을 재고에 추가
  2. `order.changeStatus(CONFIRMED)` - 주문 상태 PRODUCING → CONFIRMED
  3. 큐에서 현재 작업 dequeue
  4. 다음 큐 작업이 있으면 자동 시작 안내

### 생산 진행 처리 (콘솔 기반 시뮬레이션)
- "생산 진행" 선택 시 현재 작업을 1회 실행 단위로 진행
- 1회 진행: `job.produce(진행수량)` 호출
- 완료 확인 후 후속 처리

> **재고 흐름 정의**
> - 생산 완료: `sample.addStock(targetQty)` (재고 증가만)
> - 승인(재고 충분, Phase4): `sample.deductStock(quantity)` 즉시 수행 → CONFIRMED
> - 승인(재고 부족, Phase4): 재고 차감 없이 PRODUCING → 생산 완료 후 재고 증가만
> - 출고(Phase7): CONFIRMED 주문의 `sample.deductStock(quantity)` 수행 → RELEASE

## 3. 클래스 설계

### ProductionService (확장)
```java
public class ProductionService {
    - queueRepo: ProductionQueueRepository
    - orderRepo: OrderRepository

    + createJob(order: Order): ProductionJob      // Phase4에서 구현
    + getCurrentJob(): Optional<ProductionJob>
    + getQueueList(): List<ProductionJob>
    + processProduction(qty: int): ProductionResult
    + completeJob(job: ProductionJob): void
}
```

### ProductionResult (DTO)
```java
public class ProductionResult {
    private final boolean completed;
    private final ProductionJob job;
    private final String message;
}
```

### ProductionUI
```java
public class ProductionUI {
    - productionService: ProductionService
    - input: InputHandler

    + show(): void
    - displayCurrentJob(): void
    - displayQueue(): void
    - processProduction(): void
}
```

## 4. 화면 구성

### 생산라인 메뉴
```
========================================
              생산 라인
========================================
  1. 현재 생산 현황
  2. 생산 대기 목록
  3. 생산 진행
  0. 메인 메뉴로
----------------------------------------
선택 > 
```

### 현재 생산 현황
```
[현재 생산 중]
------------------------------------------------------------
 작업 ID  : JOB-0001
 주문번호  : ORD-0001
 시료명    : 알파센서
 고객명    : 홍길동
 주문 수량 : 5개
 실 생산량 : 4개 (목표)
 현재 생산 : 0개
 예상 시간 : 120분
------------------------------------------------------------
※ 생산 대기 중인 작업: 2건
```

### 생산 대기 목록
```
[생산 대기 목록]
------------------------------------------------------------
 순서 | 작업ID   | 주문번호  | 시료명     | 생산량
------------------------------------------------------------
  1   | JOB-0002 | ORD-0003 | 알파센서   |   3개
  2   | JOB-0003 | ORD-0005 | 베타칩     |   7개
------------------------------------------------------------
총 2건 대기 중
```

### 생산 진행
```
[생산 진행]
현재 작업: JOB-0001 (알파센서 4개 생산)
생산 수량 입력 > 4
→ 생산 완료! 알파센서 4개가 재고에 추가되었습니다.
→ 주문 ORD-0001 상태가 CONFIRMED으로 변경되었습니다.
→ 다음 작업 JOB-0002 생산을 시작합니다.
```

### 생산 큐 없을 때
```
[현재 생산 중인 작업이 없습니다.]
```

## 5. 시퀀스 흐름

### 생산 진행
```
ProductionUI.processProduction()
  → ProductionService.getCurrentJob() → 현재 작업 확인 (없으면 안내 후 종료)
  → InputHandler.readInt("생산 수량") → qty
  → ProductionService.processProduction(qty)
      → job.produce(qty)
      → job.isCompleted()? YES
          → sample.addStock(job.targetQty)
          → order.changeStatus(CONFIRMED)
          → queueRepo.dequeue()
          → 다음 작업 있으면 자동 시작 메시지
      → ProductionResult 반환
  → 결과 출력
```

## 6. TDD 테스트 계획

### ProductionServiceTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `생산_진행_미완료` | produce() 후 isCompleted=false, producedQty 증가 |
| `생산_진행_완료_재고증가` | 완료 시 sample.stock += targetQty |
| `생산_완료_주문상태_CONFIRMED` | order.status → CONFIRMED |
| `생산_완료_큐에서_제거` | 완료 후 getCurrentJob() 다음 작업 반환 |
| `생산_완료_다음_작업_자동시작` | 2개 큐 등록 후 첫 완료 시 다음 작업 peek |
| `생산_큐_없을때_진행_예외` | 큐 비어있을 때 processProduction() 시 IllegalStateException |
| `생산량_초과_입력_처리` | targetQty 초과 입력 시 targetQty로 보정 |

### 통합 시나리오 테스트 (생산 관련)
| 시나리오 | 검증 내용 |
|----------|-----------|
| `재고부족_승인_후_생산_완료` | PRODUCING → 생산 진행 → CONFIRMED, 재고 증가 |
| `복수_생산_큐_FIFO_처리` | 2개 작업 등록 후 순서대로 처리 |

## 7. 구현 순서 (TDD 사이클)
1. `ProductionService.getCurrentJob()` / `getQueueList()` 테스트 → 구현
2. `ProductionService.processProduction()` 테스트 → 구현
3. `ProductionService.completeJob()` 테스트 → 구현
4. `ProductionUI` 구현
5. 통합 시나리오 테스트 작성 및 검증
