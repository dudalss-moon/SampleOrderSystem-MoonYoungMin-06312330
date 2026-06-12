# Phase3: 시료 주문(예약)

## 1. 기능 개요
고객이 원하는 시료와 수량을 주문하면 담당자가 주문을 생성.
생성된 주문의 초기 상태는 `RESERVED`.

## 2. 도메인 규칙

### 주문 예약 규칙
- 주문 ID는 자동 생성 (순번 기반, 예: ORD-0001)
- 존재하는 시료 ID만 주문 가능
- 고객명 필수 입력 (공백 불허)
- 주문 수량 > 0
- 동일 고객이 동일 시료에 대한 RESERVED 상태 중복 주문 가능 (허용)

## 3. 클래스 설계

### Order (도메인)
```java
public class Order {
    private final String orderId;
    private final Sample sample;
    private final String customerName;
    private final int quantity;
    private OrderStatus status;

    + getOrderId(): String
    + getSample(): Sample
    + getCustomerName(): String
    + getQuantity(): int
    + getStatus(): OrderStatus
    + changeStatus(newStatus: OrderStatus): void  // 유효한 상태 전이만 허용
}
```

### 주문 상태 전이 규칙
```
RESERVED  → CONFIRMED  (재고 충분 시 승인)
RESERVED  → PRODUCING  (재고 부족 시 승인)
RESERVED  → REJECTED   (거절)
PRODUCING → CONFIRMED  (생산 완료)
CONFIRMED → RELEASE    (출고)
```

### OrderRepository
```java
public class OrderRepository {
    private final Map<String, Order> store = new LinkedHashMap<>()
    private int sequence = 1

    + save(order: Order): void
    + findById(orderId: String): Optional<Order>
    + findByStatus(status: OrderStatus): List<Order>
    + findAll(): List<Order>
    + generateId(): String  // "ORD-" + String.format("%04d", sequence++)
}
```

### OrderService
```java
public class OrderService {
    - orderRepository: OrderRepository
    - sampleRepository: SampleRepository

    + reserve(sampleId: String, customerName: String, quantity: int): Order
    + findReserved(): List<Order>
    + findAll(): List<Order>
}
```

### OrderUI
```java
public class OrderUI {
    - orderService: OrderService
    - input: InputHandler

    + show(): void
    - reserveOrder(): void
    - listReservedOrders(): void
}
```

## 4. 화면 구성

### 주문 메뉴
```
========================================
           주문 (접수/승인/거절)
========================================
  1. 주문 예약
  2. 접수된 주문 목록 (승인/거절)
  0. 메인 메뉴로
----------------------------------------
선택 > 
```

### 주문 예약 화면
```
[주문 예약]
시료 ID   > S001
고객명    > 홍길동
주문 수량 > 5
→ 주문이 접수되었습니다. [주문번호: ORD-0001 | 상태: RESERVED]
```

### 예약 주문 목록
```
[접수된 주문 목록 - RESERVED]
------------------------------------------------------------
 주문번호  | 시료명     | 고객명  | 수량 | 상태
------------------------------------------------------------
 ORD-0001 | 알파센서   | 홍길동 |   5 | RESERVED
 ORD-0002 | 베타칩     | 김철수 |   3 | RESERVED
------------------------------------------------------------
총 2건
```

## 5. 시퀀스 흐름

### 주문 예약
```
OrderUI.reserveOrder()
  → InputHandler: sampleId, customerName, quantity 입력
  → OrderService.reserve()
      → SampleRepository.findById() → 시료 존재 확인
      → quantity > 0 검증
      → OrderRepository.generateId()
      → new Order(id, sample, customer, qty, RESERVED)
      → OrderRepository.save()
  → 주문번호 + 상태 출력
```

## 6. TDD 테스트 계획

### OrderTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `주문_생성_초기상태_RESERVED` | new Order() → status == RESERVED |
| `주문_상태_정상_전이` | RESERVED → CONFIRMED 전이 성공 |
| `주문_상태_비허용_전이_예외` | RELEASE → RESERVED 시 IllegalStateException |
| `주문_수량_0이하_예외` | quantity <= 0 시 IllegalArgumentException |
| `고객명_공백_예외` | customerName blank 시 IllegalArgumentException |

### OrderRepositoryTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `주문_저장_후_ID로_조회` | save → findById 정상 반환 |
| `상태별_주문_조회` | findByStatus(RESERVED) 목록 반환 |
| `ID_자동생성_순번_증가` | generateId() → ORD-0001, ORD-0002 순 |
| `전체_주문_조회_등록순_보장` | findAll() 순서 보장 |

### OrderServiceTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `정상_주문_예약` | reserve() → Order 반환, status=RESERVED |
| `존재하지않는_시료_주문_예외` | 없는 sampleId 시 IllegalArgumentException |
| `수량_0이하_예약_예외` | quantity=0 시 IllegalArgumentException |
| `RESERVED_주문_목록_조회` | findReserved() → RESERVED 상태만 반환 |

## 7. 구현 순서 (TDD 사이클)
1. `Order` 도메인 + 상태 전이 테스트 → 구현
2. `OrderRepository` 테스트 → 구현
3. `OrderService.reserve()` 테스트 → 구현
4. `OrderUI` 구현
