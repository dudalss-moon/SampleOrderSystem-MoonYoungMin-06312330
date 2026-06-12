# Phase5: 모니터링

## 1. 기능 개요
담당자가 현재 시스템 상태를 한눈에 파악할 수 있도록 구성.
- 주문량 확인: 상태별(RESERVED / CONFIRMED / PRODUCING / RELEASE) 주문 목록
  - REJECTED는 유효한 주문이 아니므로 제외
- 재고량 확인: 시료별 현재 재고 수량 + 주문 대비 상태 표기
  - 여유: 주문 대비 재고 충분
  - 부족: 주문 대비 재고 수량 부족
  - 고갈: 재고 수량이 0

## 2. 도메인 규칙

### 재고 상태 판단 기준
- `고갈`: `sample.stock == 0`
- `부족`: `sample.stock < 해당 시료의 RESERVED + PRODUCING 주문 수량 합계`
- `여유`: 그 외 (재고가 대기 주문량 이상)

### 모니터링 대상 주문 상태
- 표시: `RESERVED`, `PRODUCING`, `CONFIRMED`, `RELEASE`
- 제외: `REJECTED`

## 3. 클래스 설계

### StockStatus (Enum)
```java
public enum StockStatus {
    PLENTY,    // 여유
    SHORTAGE,  // 부족
    DEPLETED   // 고갈
}
```

### SampleStockInfo (DTO)
```java
public class SampleStockInfo {
    private final Sample sample;
    private final int pendingQuantity;   // RESERVED + PRODUCING 주문 수량 합
    private final StockStatus stockStatus;
}
```

### MonitorService
```java
public class MonitorService {
    - orderRepository: OrderRepository
    - sampleRepository: SampleRepository

    + getOrdersByStatus(): Map<OrderStatus, List<Order>>
    + getStockInfos(): List<SampleStockInfo>
    - calcStockStatus(sample, pendingQty): StockStatus
    - calcPendingQty(sampleId): int
}
```

### MonitorUI
```java
public class MonitorUI {
    - monitorService: MonitorService
    - input: InputHandler

    + show(): void
    - displayOrderMonitor(): void
    - displayStockMonitor(): void
}
```

## 4. 화면 구성

### 모니터링 메뉴
```
========================================
              모니터링
========================================
  1. 주문 현황
  2. 재고 현황
  0. 메인 메뉴로
----------------------------------------
선택 > 
```

### 주문 현황
```
[주문 현황]
============================================================
[RESERVED - 접수 대기]
 주문번호  | 시료명     | 고객명  | 수량
 ORD-0003 | 알파센서   | 이영희 |   2
 (1건)

[PRODUCING - 생산 중]
 주문번호  | 시료명     | 고객명  | 수량
 ORD-0001 | 알파센서   | 홍길동 |   5
 (1건)

[CONFIRMED - 출고 대기]
 주문번호  | 시료명     | 고객명  | 수량
 ORD-0002 | 베타칩     | 김철수 |   3
 (1건)

[RELEASE - 출고 완료]
 (0건)
============================================================
```

### 재고 현황
```
[재고 현황]
------------------------------------------------------------
 시료명     | 현재재고 | 대기주문량 | 상태
------------------------------------------------------------
 알파센서   |     0개  |      7개  | [고갈]
 베타칩     |     5개  |      3개  | [부족]
 감마웨이퍼  |    20개  |      2개  | [여유]
------------------------------------------------------------
```

## 5. 시퀀스 흐름

### 주문 현황 조회
```
MonitorUI.displayOrderMonitor()
  → MonitorService.getOrdersByStatus()
      → OrderRepository.findAll()
      → status 기준 그룹핑 (REJECTED 제외)
      → Map<OrderStatus, List<Order>> 반환
  → 상태별 섹션 출력
```

### 재고 현황 조회
```
MonitorUI.displayStockMonitor()
  → MonitorService.getStockInfos()
      → SampleRepository.findAll()
      → 시료별:
          calcPendingQty(sampleId)
            → RESERVED + PRODUCING 주문 수량 합계
          calcStockStatus(sample, pendingQty)
            → DEPLETED / SHORTAGE / PLENTY 판단
      → List<SampleStockInfo> 반환
  → 테이블 출력
```

## 6. TDD 테스트 계획

### StockStatusTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `재고0_고갈` | stock=0 → DEPLETED |
| `재고부족_부족` | stock=2, pending=5 → SHORTAGE |
| `재고충분_여유` | stock=10, pending=3 → PLENTY |
| `재고_대기량_동일_여유` | stock=5, pending=5 → PLENTY |

### MonitorServiceTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `주문현황_상태별_그룹핑` | 4개 상태 키 존재, REJECTED 미포함 |
| `주문없는_상태_빈리스트` | 해당 상태 주문 없으면 빈 리스트 |
| `재고현황_전체_시료_포함` | 모든 시료에 대한 SampleStockInfo 반환 |
| `대기주문량_RESERVED_PRODUCING_합산` | 두 상태 수량만 합산 검증 |
| `REJECTED_주문_제외_확인` | REJECTED 주문 현황에 미포함 |
| `고갈_판단_정확성` | stock=0 → DEPLETED |
| `부족_판단_정확성` | stock < pendingQty → SHORTAGE |

## 7. 구현 순서 (TDD 사이클)
1. `StockStatus` enum 정의
2. `SampleStockInfo` DTO 정의
3. `MonitorService.getOrdersByStatus()` 테스트 → 구현
4. `MonitorService.getStockInfos()` 테스트 → 구현
5. `MonitorUI` 구현
