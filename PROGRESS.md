# S-Semi 반도체 시료생산 주문관리 시스템 — 개발 진행 현황

> 최종 업데이트: 2026-06-12

## 전체 진행률

```
Phase1 ██████████ 완료
Phase2 ██████████ 완료
Phase3 ░░░░░░░░░░ 미시작
Phase4 ░░░░░░░░░░ 미시작
Phase5 ░░░░░░░░░░ 미시작
Phase6 ░░░░░░░░░░ 미시작
Phase7 ░░░░░░░░░░ 미시작
```

---

## Phase별 상세 현황

### ✅ Phase1 — 기반 구조 + 메인 메뉴 `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase1.md](docs/design/Phase1.md) |
| 커밋 | `feat: Phase1 기반 구조 + 메인 메뉴 TDD 구현` |
| 테스트 | 7 / 7 통과 |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `domain/OrderStatus.java` | 주문 상태 Enum (5종) |
| `domain/Sample.java` | 시료 도메인 |
| `domain/Order.java` | 주문 도메인 (초기 RESERVED) |
| `domain/ProductionJob.java` | 생산작업 도메인 (targetQty/totalTime 계산) |
| `ui/InputHandler.java` | Scanner 래핑, 입력 검증 |
| `ui/ConsoleMenu.java` | 메인 메뉴 루프 + 라우팅 |
| `repository/SampleRepository.java` | 시료 인메모리 저장소 |
| `Main.java` | 애플리케이션 진입점 |

**TDD 테스트 케이스**

| # | 테스트 메서드 | 결과 |
|---|--------------|------|
| 1 | `orderStatus_유효값_5가지` | ✅ PASS |
| 2 | `sample_생성_시_필드값_정상저장` | ✅ PASS |
| 3 | `order_생성_초기상태_RESERVED` | ✅ PASS |
| 4 | `productionJob_실생산량_계산_정확성` | ✅ PASS |
| 5 | `productionJob_총생산시간_계산` | ✅ PASS |
| 6 | `inputHandler_숫자_입력_정상처리` | ✅ PASS |
| 7 | `inputHandler_잘못된_입력_예외처리` | ✅ PASS |

---

### ✅ Phase2 — 시료 관리 `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase2.md](docs/design/Phase2.md) |
| 커밋 | `feat: Phase2 시료 관리 TDD 구현` |
| 테스트 | 17 / 17 통과 |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `domain/Sample.java` | 유효성 검증 추가 + addStock / deductStock / hasEnoughStock |
| `repository/SampleRepository.java` | Map 기반 재구현, findById / findByName / existsById / existsByName |
| `service/SampleService.java` | 시료 등록 (중복 검증) / 조회 / 검색 |
| `ui/SampleUI.java` | 시료 등록 / 목록 / 검색 화면 |
| `ui/ConsoleMenu.java` | 메뉴 1번 SampleUI 라우팅 연결 |

**TDD 테스트 케이스**

| # | 클래스 | 테스트 메서드 | 결과 |
|---|--------|--------------|------|
| 1 | `SampleTest` | `시료_생성_정상` | ✅ PASS |
| 2 | `SampleTest` | `수율_0이하_예외` | ✅ PASS |
| 3 | `SampleTest` | `수율_1초과_예외` | ✅ PASS |
| 4 | `SampleTest` | `평균생산시간_0이하_예외` | ✅ PASS |
| 5 | `SampleTest` | `재고_추가_정상` | ✅ PASS |
| 6 | `SampleTest` | `재고_차감_정상` | ✅ PASS |
| 7 | `SampleTest` | `재고_차감_부족_예외` | ✅ PASS |
| 8 | `SampleTest` | `재고_충분_여부_확인` | ✅ PASS |
| 9 | `SampleRepositoryTest` | `시료_저장_후_ID로_조회` | ✅ PASS |
| 10 | `SampleRepositoryTest` | `전체_시료_조회` | ✅ PASS |
| 11 | `SampleRepositoryTest` | `이름으로_검색_부분일치` | ✅ PASS |
| 12 | `SampleRepositoryTest` | `존재하지않는_ID_조회_빈값` | ✅ PASS |
| 13 | `SampleRepositoryTest` | `중복_ID_존재여부_확인` | ✅ PASS |
| 14 | `SampleServiceTest` | `시료_등록_정상` | ✅ PASS |
| 15 | `SampleServiceTest` | `중복_ID_등록_예외` | ✅ PASS |
| 16 | `SampleServiceTest` | `중복_이름_등록_예외` | ✅ PASS |
| 17 | `SampleServiceTest` | `검색_결과_없음_빈리스트` | ✅ PASS |

---

### ⬜ Phase3 — 시료 주문(예약) `미시작`

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase3.md](docs/design/Phase3.md) |
| 테스트 | 0 / 13 |

**구현 예정 파일**

| 파일 | 역할 |
|------|------|
| `domain/Order.java` | changeStatus + 상태 전이 규칙 + 유효성 강화 |
| `repository/OrderRepository.java` | findById / findByStatus / generateId |
| `service/OrderService.java` | reserve / findReserved / findAll |
| `ui/OrderUI.java` | 주문 예약 화면 |

**예정 테스트 케이스 (13건)**

| 클래스 | 테스트 수 | 주요 검증 |
|--------|----------|----------|
| `OrderTest` | 5 | 초기상태, 상태전이, 비허용전이 예외, 유효성 |
| `OrderRepositoryTest` | 4 | save/findById/findByStatus/generateId |
| `OrderServiceTest` | 4 | reserve, 시료없음 예외, 수량 예외, RESERVED 목록 |

---

### ⬜ Phase4 — 주문 승인/거절 `미시작`

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase4.md](docs/design/Phase4.md) |
| 테스트 | 0 / 14 |

**구현 예정 파일**

| 파일 | 역할 |
|------|------|
| `domain/ProductionJob.java` | isCompleted / produce 메서드 추가 |
| `repository/ProductionQueueRepository.java` | FIFO 큐, enqueue/dequeue/peek/generateJobId |
| `service/ProductionService.java` | createJob |
| `service/OrderService.java` | approve / reject 추가 |

**예정 테스트 케이스 (14건)**

| 클래스 | 테스트 수 | 주요 검증 |
|--------|----------|----------|
| `ProductionJobTest` | 4 | targetQty, totalTime, isCompleted, produce |
| `ProductionQueueRepositoryTest` | 4 | enqueue/peek/dequeue/FIFO |
| `OrderServiceTest` (확장) | 6 | 재고충분/부족 승인, 거절, 예외 처리 |

---

### ⬜ Phase5 — 모니터링 `미시작`

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase5.md](docs/design/Phase5.md) |
| 테스트 | 0 / 11 |

**구현 예정 파일**

| 파일 | 역할 |
|------|------|
| `domain/StockStatus.java` | 재고 상태 Enum (PLENTY/SHORTAGE/DEPLETED) |
| `domain/SampleStockInfo.java` | 재고 현황 DTO |
| `service/MonitorService.java` | 주문/재고 현황 조회 |
| `ui/MonitorUI.java` | 모니터링 화면 |

**예정 테스트 케이스 (11건)**

| 클래스 | 테스트 수 | 주요 검증 |
|--------|----------|----------|
| `StockStatusTest` | 4 | DEPLETED/SHORTAGE/PLENTY 판단 |
| `MonitorServiceTest` | 7 | 상태별 그룹핑, REJECTED 제외, 재고 현황 |

---

### ⬜ Phase6 — 생산라인 `미시작`

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase6.md](docs/design/Phase6.md) |
| 테스트 | 0 / 9 |

**구현 예정 파일**

| 파일 | 역할 |
|------|------|
| `service/ProductionService.java` | getCurrentJob / processProduction / completeJob |
| `domain/ProductionResult.java` | 생산 결과 DTO |
| `ui/ProductionUI.java` | 생산라인 화면 |

**예정 테스트 케이스 (9건)**

| 클래스 | 테스트 수 | 주요 검증 |
|--------|----------|----------|
| `ProductionServiceTest` | 7 | 미완료/완료 처리, 재고증가, CONFIRMED 전환, FIFO |
| 통합 시나리오 | 2 | 재고부족 전체 흐름, 복수 큐 처리 |

---

### ⬜ Phase7 — 출고처리 `미시작`

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase7.md](docs/design/Phase7.md) |
| 테스트 | 0 / 10 |

**구현 예정 파일**

| 파일 | 역할 |
|------|------|
| `domain/Order.java` | stockDeducted 플래그 추가 |
| `service/ReleaseService.java` | findConfirmed / release |
| `ui/ReleaseUI.java` | 출고 처리 화면 |

**예정 테스트 케이스 (10건)**

| 클래스 | 테스트 수 | 주요 검증 |
|--------|----------|----------|
| `ReleaseServiceTest` | 7 | RELEASE 전환, 차감 분기, 예외 처리 |
| 통합 시나리오 | 3 | 재고충분/부족 전체 흐름, 복수 주문 출고 |

---

## 테스트 현황 요약

| Phase | 테스트 파일 | 통과 | 전체 | 진행률 |
|-------|------------|------|------|--------|
| Phase1 | `Phase1Test` | 7 | 7 | 100% |
| Phase2 | `SampleTest`, `SampleRepositoryTest`, `SampleServiceTest` | 17 | 17 | 100% |
| Phase3 | `OrderTest`, `OrderRepositoryTest`, `OrderServiceTest` | 0 | 13 | 0% |
| Phase4 | `ProductionJobTest`, `ProductionQueueRepositoryTest`, `OrderServiceTest` | 0 | 14 | 0% |
| Phase5 | `StockStatusTest`, `MonitorServiceTest` | 0 | 11 | 0% |
| Phase6 | `ProductionServiceTest` | 0 | 9 | 0% |
| Phase7 | `ReleaseServiceTest` | 0 | 10 | 0% |
| **합계** | | **24** | **81** | **30%** |

---

## 주문 상태 흐름

```
RESERVED → [승인] → 재고 충분  → CONFIRMED → RELEASE
                 → 재고 부족  → PRODUCING → CONFIRMED → RELEASE
         → [거절] → REJECTED
```
