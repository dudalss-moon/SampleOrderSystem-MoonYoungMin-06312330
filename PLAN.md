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
| Phase4 | 주문 승인/거절 | [Phase4.md](docs/design/Phase4.md) | 미시작 |
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
