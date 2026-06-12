# S-Semi 반도체 시료생산 주문관리 시스템 — 개발 진행 현황

> 최종 업데이트: 2026-06-12 (Phase6 자동 생산 구현 완료)

## 전체 진행률

```
Phase1 ██████████ 완료
Phase2 ██████████ 완료
Phase3 ██████████ 완료
Phase4 ██████████ 완료
Phase5 ██████████ 완료
Phase6 ██████████ 완료
Phase7 ██████████ 완료
Phase8 ██████████ 완료
Phase9 ██████████ 완료
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

### ✅ Phase3 — 시료 주문(예약) `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase3.md](docs/design/Phase3.md) |
| 커밋 | `feat: Phase3 시료 주문(예약) TDD 구현` |
| 테스트 | 13 / 13 통과 |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `domain/Order.java` | 유효성 검증 + changeStatus 상태 전이 규칙 |
| `repository/OrderRepository.java` | LinkedHashMap 기반, findById / findByStatus / generateId |
| `service/OrderService.java` | reserve (시료 존재 검증) / findReserved / findAll / findById |
| `ui/OrderUI.java` | 주문 예약 / 접수 목록 화면 |
| `ui/ConsoleMenu.java` | 메뉴 2번 OrderUI 라우팅 연결 |

**TDD 테스트 케이스**

| # | 클래스 | 테스트 메서드 | 결과 |
|---|--------|--------------|------|
| 1 | `OrderTest` | `주문_생성_초기상태_RESERVED` | ✅ PASS |
| 2 | `OrderTest` | `주문_상태_정상_전이` | ✅ PASS |
| 3 | `OrderTest` | `주문_상태_비허용_전이_예외` | ✅ PASS |
| 4 | `OrderTest` | `주문_수량_0이하_예외` | ✅ PASS |
| 5 | `OrderTest` | `고객명_공백_예외` | ✅ PASS |
| 6 | `OrderRepositoryTest` | `주문_저장_후_ID로_조회` | ✅ PASS |
| 7 | `OrderRepositoryTest` | `상태별_주문_조회` | ✅ PASS |
| 8 | `OrderRepositoryTest` | `ID_자동생성_순번_증가` | ✅ PASS |
| 9 | `OrderRepositoryTest` | `전체_주문_조회_등록순_보장` | ✅ PASS |
| 10 | `OrderServiceTest` | `정상_주문_예약` | ✅ PASS |
| 11 | `OrderServiceTest` | `존재하지않는_시료_주문_예외` | ✅ PASS |
| 12 | `OrderServiceTest` | `수량_0이하_예약_예외` | ✅ PASS |
| 13 | `OrderServiceTest` | `RESERVED_주문_목록_조회` | ✅ PASS |

---

### ✅ Phase4 — 주문 승인/거절 `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase4.md](docs/design/Phase4.md) |
| 테스트 | 12 / 12 통과 |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `domain/ProductionJob.java` | `produce()` / `isCompleted()` 추가 |
| `repository/ProductionQueueRepository.java` | FIFO 큐, enqueue/dequeue/peek/getQueue/generateJobId |
| `service/ProductionService.java` | `createJob()` — 생산큐 등록 |
| `service/OrderService.java` | `approve()` (재고 충분→CONFIRMED, 부족→PRODUCING) / `reject()` |
| `ui/OrderUI.java` | 승인/거절 인터랙션 화면 |

**TDD 테스트 케이스**

| # | 클래스 | 테스트 메서드 | 결과 |
|---|--------|--------------|------|
| 1 | `ProductionJobTest` | `생산량_누적` | ✅ PASS |
| 2 | `ProductionJobTest` | `생산_완료_여부_확인` | ✅ PASS |
| 3 | `ProductionQueueRepositoryTest` | `작업_enqueue_후_peek_동일` | ✅ PASS |
| 4 | `ProductionQueueRepositoryTest` | `FIFO_순서_보장` | ✅ PASS |
| 5 | `ProductionQueueRepositoryTest` | `빈_큐_peek_빈값` | ✅ PASS |
| 6 | `ProductionQueueRepositoryTest` | `대기목록_조회` | ✅ PASS |
| 7 | `OrderServiceTest` | `재고_충분_승인_CONFIRMED` | ✅ PASS |
| 8 | `OrderServiceTest` | `재고_부족_승인_PRODUCING` | ✅ PASS |
| 9 | `OrderServiceTest` | `거절_REJECTED` | ✅ PASS |
| 10 | `OrderServiceTest` | `RESERVED_아닌_주문_승인_예외` | ✅ PASS |
| 11 | `OrderServiceTest` | `존재하지않는_주문_승인_예외` | ✅ PASS |
| 12 | `OrderServiceTest` | `재고_부족시_생산량_계산_정확` | ✅ PASS |

---

### ✅ Phase5 — 모니터링 `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase5.md](docs/design/Phase5.md) |
| 테스트 | 11 / 11 통과 |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `domain/StockStatus.java` | PLENTY/SHORTAGE/DEPLETED + `of(stock, pendingQty)` 팩토리 |
| `domain/SampleStockInfo.java` | 재고 현황 DTO (sample, pendingQuantity, stockStatus) |
| `service/MonitorService.java` | `getOrdersByStatus()` / `getStockInfos()` / `calcPendingQty()` |
| `ui/MonitorUI.java` | 주문 현황 / 재고 현황 화면 |

**TDD 테스트 케이스**

| # | 클래스 | 테스트 메서드 | 결과 |
|---|--------|--------------|------|
| 1 | `StockStatusTest` | `재고0_고갈` | ✅ PASS |
| 2 | `StockStatusTest` | `재고부족_부족` | ✅ PASS |
| 3 | `StockStatusTest` | `재고충분_여유` | ✅ PASS |
| 4 | `StockStatusTest` | `재고_대기량_동일_여유` | ✅ PASS |
| 5 | `MonitorServiceTest` | `주문현황_상태별_그룹핑` | ✅ PASS |
| 6 | `MonitorServiceTest` | `주문없는_상태_빈리스트` | ✅ PASS |
| 7 | `MonitorServiceTest` | `REJECTED_주문_제외_확인` | ✅ PASS |
| 8 | `MonitorServiceTest` | `재고현황_전체_시료_포함` | ✅ PASS |
| 9 | `MonitorServiceTest` | `대기주문량_RESERVED_PRODUCING_합산` | ✅ PASS |
| 10 | `MonitorServiceTest` | `고갈_판단_정확성` | ✅ PASS |
| 11 | `MonitorServiceTest` | `부족_판단_정확성` | ✅ PASS |

---

### ✅ Phase6 — 생산라인 `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase6.md](docs/design/Phase6.md) |
| 테스트 | 16 / 16 통과 |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `service/ProductionService.java` | `processAutoProduction()` / `completeJob()` / 스케줄러 생성자 / `shutdown()` 추가 |
| `domain/ProductionJob.java` | `startTime` 필드 / `getStartTime()` / `setStartTime()` / `calcProducedByElapsed()` 추가 |
| `domain/ProductionResult.java` | 생산 결과 DTO (completed, job) |
| `ui/ProductionUI.java` | 수동 생산 메뉴 항목 제거 (자동 생산으로 대체) |
| `ui/ConsoleMenu.java` | 메뉴 4번 ProductionUI 라우팅 연결 |

**TDD 테스트 케이스**

| # | 클래스 | 테스트 메서드 | 결과 |
|---|--------|--------------|------|
| 1 | `ProductionServiceTest` | `생산_진행_미완료` | ✅ PASS |
| 2 | `ProductionServiceTest` | `생산_진행_완료_재고증가` | ✅ PASS |
| 3 | `ProductionServiceTest` | `생산_완료_주문상태_CONFIRMED` | ✅ PASS |
| 4 | `ProductionServiceTest` | `생산_완료_큐에서_제거` | ✅ PASS |
| 5 | `ProductionServiceTest` | `생산_완료_다음_작업_자동시작` | ✅ PASS |
| 6 | `ProductionServiceTest` | `생산_큐_없을때_진행_예외` | ✅ PASS |
| 7 | `ProductionServiceTest` | `생산량_초과_입력_처리` | ✅ PASS |
| 8 | `ProductionServiceTest` | `재고부족_승인_후_생산_완료` | ✅ PASS |
| 9 | `ProductionServiceTest` | `복수_생산_큐_FIFO_처리` | ✅ PASS |
| 10 | `AutoProductionServiceTest` | `빈_큐일때_자동생산_무시` | ✅ PASS |
| 11 | `AutoProductionServiceTest` | `startTime_자동설정` | ✅ PASS |
| 12 | `AutoProductionServiceTest` | `경과시간_기반_생산량_갱신` | ✅ PASS |
| 13 | `AutoProductionServiceTest` | `생산_자동완료_재고_주문상태_갱신` | ✅ PASS |
| 14 | `AutoProductionServiceTest` | `다음_작업_startTime_자동설정` | ✅ PASS |
| 15 | `AutoProductionServiceTest` | `스케줄러_통합_자동생산` | ✅ PASS |
| 16 | `AutoProductionServiceTest` | `shutdown_후_스케줄러_종료` | ✅ PASS |

---

### ✅ Phase7 — 출고처리 `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase7.md](docs/design/Phase7.md) |
| 테스트 | 12 / 12 통과 |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `domain/Order.java` | `stockDeducted` 플래그, `isStockDeducted()` / `markStockDeducted()` 추가 |
| `service/OrderService.java` | `approve()` 재고 충분 분기에 `markStockDeducted()` 추가 |
| `service/ReleaseService.java` | `findConfirmed()` / `release()` 구현 (차감 분기 포함) |
| `ui/ReleaseUI.java` | 출고 대기 목록 / 출고 처리 화면 |
| `ui/ConsoleMenu.java` | 메뉴 5번 ReleaseUI 라우팅 연결 |

**TDD 테스트 케이스**

| # | 클래스 | 테스트 메서드 | 결과 |
|---|--------|--------------|------|
| 1 | `OrderTest` | `stockDeducted_기본값_false` | ✅ PASS |
| 2 | `OrderTest` | `markStockDeducted_호출_후_true` | ✅ PASS |
| 3 | `ReleaseServiceTest` | `CONFIRMED_주문_목록_조회` | ✅ PASS |
| 4 | `ReleaseServiceTest` | `출고_처리_RELEASE_전환` | ✅ PASS |
| 5 | `ReleaseServiceTest` | `재고부족_경로_출고_재고차감` | ✅ PASS |
| 6 | `ReleaseServiceTest` | `재고충분_경로_출고_차감없음` | ✅ PASS |
| 7 | `ReleaseServiceTest` | `CONFIRMED_아닌_주문_출고_예외` | ✅ PASS |
| 8 | `ReleaseServiceTest` | `존재하지않는_주문_출고_예외` | ✅ PASS |
| 9 | `ReleaseServiceTest` | `출고_후_CONFIRMED_목록에서_제거` | ✅ PASS |
| 10 | `ReleaseServiceTest` | `전체_흐름_재고충분` | ✅ PASS |
| 11 | `ReleaseServiceTest` | `전체_흐름_재고부족` | ✅ PASS |
| 12 | `ReleaseServiceTest` | `복수_주문_순차_출고` | ✅ PASS |

---

---

### ✅ Phase9 — 테스트 커버리지 보강 `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase9.md](docs/design/Phase9.md) |
| 테스트 | 50 / 50 통과 (전체 148 / 148) |

**신규 테스트 파일 (11개)**

| 파일 | 테스트 수 | 대상 |
|------|----------|------|
| `ProductionResultTest` | 2 | 값 객체 직접 검증 |
| `InMemoryOrderRepositoryTest` | 4 | 구현체 엣지케이스 |
| `InMemoryProductionQueueRepositoryTest` | 4 | FIFO·방어적 복사 |
| `InMemorySampleRepositoryTest` | 4 | 집계 메서드 |
| `InputHandlerTest` | 5 | readString·readDouble·재입력 루프 |
| `ConsoleMenuTest` | 5 | 메인 메뉴 UI |
| `SampleUITest` | 6 | 시료 관리 UI |
| `OrderUITest` | 7 | 주문 UI |
| `MonitorUITest` | 4 | 모니터링 UI |
| `ProductionUITest` | 5 | 생산 라인 UI |
| `ReleaseUITest` | 4 | 출고 처리 UI |

---

### ✅ Phase8 — DB 연동 (H2 JDBC) `완료` (2026-06-12)

| 항목 | 내용 |
|------|------|
| 설계 문서 | [Phase8.md](docs/design/Phase8.md) |
| 브랜치 | `feature/phase8-db-jdbc` |
| 테스트 | 17 / 17 통과 (전체 98 / 98) |

**구현 파일**

| 파일 | 역할 |
|------|------|
| `db/DatabaseConfig.java` | H2 JDBC 연결 관리 (`AutoCloseable`) |
| `db/SchemaInitializer.java` | DDL 멱등 초기화 (4개 테이블 + sequences) |
| `repository/SampleRepository.java` | 인터페이스로 변환 |
| `repository/OrderRepository.java` | 인터페이스로 변환 |
| `repository/ProductionQueueRepository.java` | 인터페이스로 변환 |
| `repository/inmemory/InMemorySampleRepository.java` | 기존 인메모리 구현 분리 |
| `repository/inmemory/InMemoryOrderRepository.java` | 기존 인메모리 구현 분리 |
| `repository/inmemory/InMemoryProductionQueueRepository.java` | 기존 인메모리 구현 분리 |
| `repository/jdbc/JdbcSampleRepository.java` | H2 JDBC 시료 저장소 (MERGE INTO upsert) |
| `repository/jdbc/JdbcOrderRepository.java` | H2 JDBC 주문 저장소 (JOIN 재구성, sequences ID) |
| `repository/jdbc/JdbcProductionQueueRepository.java` | H2 JDBC 생산큐 저장소 (FIFO enqueue_order) |
| `domain/ProductionJob.java` | `restore()` 정적 팩토리 추가 (DB 재구성용) |
| `Main.java` | H2 파일DB + JDBC 구현체로 전환 |

**TDD 테스트 케이스**

| # | 클래스 | 테스트 메서드 | 결과 |
|---|--------|--------------|------|
| 1 | `DatabaseConfigTest` | `데이터베이스_연결_정상` | ✅ PASS |
| 2 | `SchemaInitializerTest` | `스키마_초기화_테이블_생성` | ✅ PASS |
| 3 | `SchemaInitializerTest` | `스키마_중복_초기화_오류없음` | ✅ PASS |
| 4 | `JdbcSampleRepositoryTest` | `시료_저장_후_ID로_조회` | ✅ PASS |
| 5 | `JdbcSampleRepositoryTest` | `재고_변경_DB_반영` | ✅ PASS |
| 6 | `JdbcSampleRepositoryTest` | `전체_시료_조회_등록순_보장` | ✅ PASS |
| 7 | `JdbcSampleRepositoryTest` | `이름_부분일치_검색` | ✅ PASS |
| 8 | `JdbcSampleRepositoryTest` | `count_totalStock_정확성` | ✅ PASS |
| 9 | `JdbcOrderRepositoryTest` | `주문_저장_후_ID로_조회` | ✅ PASS |
| 10 | `JdbcOrderRepositoryTest` | `상태_변경_DB_반영` | ✅ PASS |
| 11 | `JdbcOrderRepositoryTest` | `stockDeducted_플래그_DB_반영` | ✅ PASS |
| 12 | `JdbcOrderRepositoryTest` | `ID_자동생성_순번_증가` | ✅ PASS |
| 13 | `JdbcOrderRepositoryTest` | `상태별_주문_조회` | ✅ PASS |
| 14 | `ProductionJobTest` | `restore_정적팩토리_DB_재구성` | ✅ PASS |
| 15 | `JdbcProductionQueueRepositoryTest` | `작업_저장_후_enqueue_순서_조회` | ✅ PASS |
| 16 | `JdbcProductionQueueRepositoryTest` | `생산_진행_후_producedQty_DB_반영` | ✅ PASS |
| 17 | `DbPersistenceIntegrationTest` | `DB_재연결_후_데이터_유지` | ✅ PASS |

---

## 테스트 현황 요약

| Phase | 테스트 파일 | 통과 | 전체 | 진행률 |
|-------|------------|------|------|--------|
| Phase1 | `Phase1Test` | 7 | 7 | 100% |
| Phase2 | `SampleTest`, `SampleRepositoryTest`, `SampleServiceTest` | 17 | 17 | 100% |
| Phase3 | `OrderTest`, `OrderRepositoryTest`, `OrderServiceTest` | 13 | 13 | 100% |
| Phase4 | `ProductionJobTest`, `ProductionQueueRepositoryTest`, `OrderServiceTest` | 17 | 17 | 100% |
| Phase5 | `StockStatusTest`, `MonitorServiceTest` | 11 | 11 | 100% |
| Phase6 | `ProductionServiceTest`, `AutoProductionServiceTest` | 16 | 16 | 100% |
| Phase7 | `OrderTest`, `ReleaseServiceTest` | 12 | 12 | 100% |
| Phase8 | `DatabaseConfigTest`, `SchemaInitializerTest`, `JdbcSampleRepositoryTest`, `JdbcOrderRepositoryTest`, `JdbcProductionQueueRepositoryTest`, `DbPersistenceIntegrationTest` | 17 | 17 | 100% |
| Phase9 | `ProductionResultTest`, `InMemoryOrderRepositoryTest`, `InMemoryProductionQueueRepositoryTest`, `InMemorySampleRepositoryTest`, `InputHandlerTest`, `ConsoleMenuTest`, `SampleUITest`, `OrderUITest`, `MonitorUITest`, `ProductionUITest`, `ReleaseUITest` | 50 | 50 | 100% |
| **합계** | | **160** | **160** | **100%** |

---

## 주문 상태 흐름

```
RESERVED → [승인] → 재고 충분  → CONFIRMED → RELEASE
                 → 재고 부족  → PRODUCING → CONFIRMED → RELEASE
         → [거절] → REJECTED
```
