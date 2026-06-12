# Phase7: 출고처리

## 1. 기능 개요
재고가 확보된 `CONFIRMED` 상태 주문에 대해 출고를 처리.
- CONFIRMED 상태 주문 목록 확인
- 특정 주문에 대해 출고 실행 → `RELEASE` 상태로 전환

## 2. 도메인 규칙

### 출고 처리 규칙
- 대상: `CONFIRMED` 상태 주문만
- 재고 충분 승인 경로: Phase4에서 이미 재고 차감 완료 → 출고 시 추가 차감 없음
- 재고 부족 승인 경로: 생산 완료로 재고 증가 후 출고 → 출고 시 `sample.deductStock(quantity)` 수행
- `order.changeStatus(RELEASE)` 로 최종 완료

> **경로별 재고 차감 시점 정리**
>
> | 경로 | 재고 차감 시점 |
> |------|---------------|
> | 재고 충분 승인 (Phase4) | 승인 시 즉시 차감 |
> | 재고 부족 승인 → 생산 → 출고 (Phase4→6→7) | 출고 시 차감 |
>
> 따라서 출고 처리 시 재고 차감 여부는 주문의 이력(충분/부족 경로)에 따라 결정.
> 구현 단순화를 위해 **Order에 `stockDeducted` 플래그**를 두어 이중 차감 방지.

## 3. 클래스 설계

### Order (확장)
```java
public class Order {
    // ... 기존 필드
    private boolean stockDeducted;   // 재고 차감 완료 여부

    + isStockDeducted(): boolean
    + markStockDeducted(): void
}
```

### ReleaseService
```java
public class ReleaseService {
    - orderRepository: OrderRepository
    - sampleRepository: SampleRepository

    + findConfirmed(): List<Order>
    + release(orderId: String): Order
    // release 내부 로직:
    //   1. CONFIRMED 확인
    //   2. !order.isStockDeducted() 이면 sample.deductStock(quantity)
    //   3. order.changeStatus(RELEASE)
}
```

### ReleaseUI
```java
public class ReleaseUI {
    - releaseService: ReleaseService
    - input: InputHandler

    + show(): void
    - displayConfirmedOrders(): void
    - executeRelease(): void
}
```

## 4. 화면 구성

### 출고 처리 화면
```
========================================
              출고 처리
========================================
[CONFIRMED - 출고 대기 목록]
------------------------------------------------------------
 주문번호  | 시료명     | 고객명  | 수량
------------------------------------------------------------
 ORD-0001 | 알파센서   | 홍길동 |   5
 ORD-0002 | 베타칩     | 김철수 |   3
------------------------------------------------------------
총 2건

출고할 주문번호 > ORD-0001
→ 출고 완료! [주문번호: ORD-0001 | 상태: RELEASE]
```

### 출고 대기 목록 없을 때
```
[출고 대기 중인 주문이 없습니다.]
```

## 5. 시퀀스 흐름

### 출고 처리
```
ReleaseUI.executeRelease()
  → ReleaseService.findConfirmed() → 목록 표시 (없으면 안내 후 종료)
  → InputHandler.readString("주문번호") → orderId
  → ReleaseService.release(orderId)
      → OrderRepository.findById() → CONFIRMED 상태 확인
      → order.isStockDeducted()? NO
          → sample.deductStock(quantity)
          → order.markStockDeducted()
      → order.changeStatus(RELEASE)
      → OrderRepository.save()
  → 완료 메시지 출력
```

## 6. TDD 테스트 계획

### ReleaseServiceTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `CONFIRMED_주문_목록_조회` | findConfirmed() → CONFIRMED 상태만 반환 |
| `출고_처리_RELEASE_전환` | release() → order.status == RELEASE |
| `재고부족_경로_출고_재고차감` | stockDeducted=false → deductStock() 호출 |
| `재고충분_경로_출고_차감없음` | stockDeducted=true → deductStock() 미호출 |
| `CONFIRMED_아닌_주문_출고_예외` | RESERVED 주문 release() 시 IllegalStateException |
| `존재하지않는_주문_출고_예외` | 없는 orderId 시 IllegalArgumentException |
| `출고_후_CONFIRMED_목록에서_제거` | release() 후 findConfirmed()에 미포함 |

### 통합 시나리오 테스트
| 시나리오 | 검증 내용 |
|----------|-----------|
| `전체_흐름_재고충분` | 예약→승인(CONFIRMED)→출고(RELEASE), 재고 차감 1회만 |
| `전체_흐름_재고부족` | 예약→승인(PRODUCING)→생산완료(CONFIRMED)→출고(RELEASE), 출고 시 차감 |
| `복수_주문_순차_출고` | 여러 CONFIRMED 주문 순차 출고 정상 처리 |

## 7. 구현 순서 (TDD 사이클)
1. `Order.stockDeducted` 플래그 테스트 → 구현
2. `ReleaseService.findConfirmed()` 테스트 → 구현
3. `ReleaseService.release()` 테스트 → 구현 (재고 차감 분기 포함)
4. `ReleaseUI` 구현
5. 통합 시나리오 테스트 작성 및 검증
