# S-Semi 반도체 시료생산 주문관리 시스템

## 배경

"S-Semi" 회사의 반도체 시료생산 주문을 콘솔 기반으로 관리하는 시스템.  
시료 등록부터 주문 예약 → 승인/거절 → 생산 → 출고까지의 전체 흐름을 지원한다.

---

## 개발 환경

| 항목 | 버전 |
|------|------|
| Java | 17 (Temurin) |
| Build | Gradle 9.3.0 |
| Test | JUnit 5 |
| 방법론 | TDD (Red → Green → Review) |

---

## 실행 방법

```bash
# 빌드 및 테스트
./gradlew test

# 실행
./gradlew run
```

Windows PowerShell:
```powershell
$env:JAVA_HOME = "C:\Users\User\.jdks\temurin-17.0.19"
.\gradlew test
.\gradlew run
```

---

## 주요 기능

| Phase | 기능 | 상태 |
|-------|------|------|
| Phase1 | 기반 구조 + 메인 메뉴 | ✅ 완료 |
| Phase2 | 시료 관리 (등록/조회/검색) | ✅ 완료 |
| Phase3 | 시료 주문 예약 | ✅ 완료 |
| Phase4 | 주문 승인/거절 | ✅ 완료 |
| Phase5 | 모니터링 (주문 현황/재고 현황) | ✅ 완료 |
| Phase6 | 생산라인 (생산 진행/큐 관리) | ✅ 완료 |
| Phase7 | 출고 처리 | ✅ 완료 |

---

## 주문 상태 흐름

```
RESERVED → [승인] → 재고 충분  → CONFIRMED → RELEASE
                 → 재고 부족  → PRODUCING → CONFIRMED → RELEASE
         → [거절] → REJECTED
```

### 경로별 재고 차감 시점

| 경로 | 재고 차감 시점 |
|------|--------------|
| 재고 충분 승인 | 승인 시 즉시 차감 (`deductStock`) |
| 재고 부족 승인 → 생산 → 출고 | 출고 시 차감 (`deductStock`) |

---

## 패키지 구조

```
src/main/java/ssemi/order/
├── Main.java
├── domain/
│   ├── Order.java            # 주문 도메인 (상태 전이 규칙 포함)
│   ├── OrderStatus.java      # RESERVED / REJECTED / PRODUCING / CONFIRMED / RELEASE
│   ├── Sample.java           # 시료 도메인 (재고 관리)
│   ├── ProductionJob.java    # 생산 작업 (targetQty 계산)
│   ├── ProductionResult.java # 생산 결과 DTO
│   ├── SampleStockInfo.java  # 재고 현황 DTO
│   └── StockStatus.java      # PLENTY / SHORTAGE / DEPLETED
├── repository/
│   ├── SampleRepository.java
│   ├── OrderRepository.java
│   └── ProductionQueueRepository.java  # FIFO 큐
├── service/
│   ├── SampleService.java
│   ├── OrderService.java
│   ├── ProductionService.java
│   ├── MonitorService.java
│   └── ReleaseService.java
└── ui/
    ├── ConsoleMenu.java      # 메인 메뉴 라우팅
    ├── InputHandler.java     # Scanner 래핑
    ├── SampleUI.java
    ├── OrderUI.java
    ├── MonitorUI.java
    ├── ProductionUI.java
    └── ReleaseUI.java
```

### 아키텍처 원칙

```
ui → service → domain ← repository
```

- 단방향 의존성 유지
- 인메모리 저장소 (`LinkedHashMap` / `LinkedList`)

---

## 실 생산량 계산 공식

```
targetQty = ceil(부족분 / (수율 × 0.9))
totalTime = avgProductionTime × targetQty
```

---

## 테스트 현황

| Phase | 테스트 파일 | 통과 / 전체 |
|-------|------------|------------|
| Phase1 | `Phase1Test` | 7 / 7 |
| Phase2 | `SampleTest`, `SampleRepositoryTest`, `SampleServiceTest` | 17 / 17 |
| Phase3 | `OrderTest`, `OrderRepositoryTest`, `OrderServiceTest` | 13 / 13 |
| Phase4 | `ProductionJobTest`, `ProductionQueueRepositoryTest`, `OrderServiceTest` | 12 / 12 |
| Phase5 | `StockStatusTest`, `MonitorServiceTest` | 11 / 11 |
| Phase6 | `ProductionServiceTest` | 9 / 9 |
| Phase7 | `OrderTest`, `ReleaseServiceTest` | 12 / 12 |
| **합계** | | **81 / 81 (100%)** |

---

## 문서

| 문서 | 설명 |
|------|------|
| [PRD.md](PRD.md) | 제품 요구사항 정의서 |
| [PLAN.md](PLAN.md) | 개발 설계 및 TDD 계획 |
| [PROGRESS.md](PROGRESS.md) | Phase별 진행 현황 |
| [docs/design/](docs/design/) | Phase별 설계 문서 |
