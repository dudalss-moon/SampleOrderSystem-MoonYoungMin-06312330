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
