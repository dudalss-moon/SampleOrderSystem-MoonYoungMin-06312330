# Phase9: 테스트 커버리지 보강

## 1. 기능 개요

SubAgent3(Test Verifier)가 탐지한 누락 테스트 케이스를 보강한다.  
기존 98개 테스트를 토대로, 미검증 영역(도메인 값 객체·InMemory 구현체 엣지케이스·UI 계층·InputHandler)을 커버하는 테스트를 추가한다.

### 보강 대상 요약

| 대상 | 누락 원인 | 추가 테스트 수(예상) |
|------|-----------|----------------------|
| `ProductionResult` | 값 객체 직접 테스트 없음 | 2 |
| `InMemoryOrderRepository` | 구현체 엣지케이스 미검증 | 4 |
| `InMemoryProductionQueueRepository` | 구현체 엣지케이스 미검증 | 4 |
| `InMemorySampleRepository` | 집계 메서드 미검증 | 4 |
| `InputHandler` | readString·readDouble·재입력 루프 미검증 | 5 |
| `ConsoleMenu` | UI 계층 전체 미검증 | 5 |
| `SampleUI` | UI 계층 전체 미검증 | 6 |
| `OrderUI` | UI 계층 전체 미검증 | 7 |
| `MonitorUI` | UI 계층 전체 미검증 | 4 |
| `ProductionUI` | UI 계층 전체 미검증 | 5 |
| `ReleaseUI` | UI 계층 전체 미검증 | 4 |

**예상 추가 테스트 수: 약 50개** (기존 98 → 약 148개)

---

## 2. 테스트 전략

### UI 계층 공통 전략

콘솔 UI는 `System.in` / `System.out` 의존성을 `InputHandler`가 캡슐화하므로,  
`ByteArrayInputStream` + `ByteArrayOutputStream`을 주입하여 입출력을 시뮬레이션한다.

```java
// 입력 시뮬레이션 헬퍼
private InputHandler inputOf(String... lines) {
    String joined = String.join(System.lineSeparator(), lines) + System.lineSeparator();
    ByteArrayInputStream in = new ByteArrayInputStream(joined.getBytes());
    return new InputHandler(in, new PrintStream(out));
}

// 출력 캡처
private final ByteArrayOutputStream out = new ByteArrayOutputStream();
```

서비스 의존성은 `InMemory` 구현체를 직접 사용한다 (Mockito 미사용 원칙 유지).

---

## 3. 사이클별 구현 계획

---

### 사이클 1 — ProductionResultTest

**파일**: `src/test/java/ssemi/order/ProductionResultTest.java`

| # | 테스트 메서드 | 검증 내용 |
|---|--------------|-----------|
| 1 | `completed_true_반환` | `isCompleted()` → `true` |
| 2 | `job_참조_반환` | `getJob()` → 생성 시 주입한 `ProductionJob` 동일 참조 |

**핵심 시나리오**
```java
@Test
void completed_true_반환() {
    ProductionJob job = /* 샘플 job 생성 */;
    ProductionResult result = new ProductionResult(true, job);
    assertTrue(result.isCompleted());
}

@Test
void job_참조_반환() {
    ProductionJob job = /* 샘플 job 생성 */;
    ProductionResult result = new ProductionResult(false, job);
    assertSame(job, result.getJob());
}
```

---

### 사이클 2 — InMemory 구현체 엣지케이스

#### 2-A. InMemoryOrderRepositoryTest (추가)

**파일**: `src/test/java/ssemi/order/InMemoryOrderRepositoryTest.java`

| # | 테스트 메서드 | 검증 내용 |
|---|--------------|-----------|
| 1 | `generateId_순번_증가` | 연속 호출 시 `ORD-0001`, `ORD-0002` 순 반환 |
| 2 | `save_동일_ID_덮어쓰기` | 같은 orderId로 재저장 시 최신 객체로 교체 |
| 3 | `findByStatus_빈_결과` | 해당 상태 주문 없을 때 빈 리스트 반환 |
| 4 | `findById_없는_ID` | 존재하지 않는 ID → `Optional.empty()` |

#### 2-B. InMemoryProductionQueueRepositoryTest (추가)

**파일**: `src/test/java/ssemi/order/InMemoryProductionQueueRepositoryTest.java`

| # | 테스트 메서드 | 검증 내용 |
|---|--------------|-----------|
| 1 | `generateJobId_순번_증가` | 연속 호출 시 `JOB-0001`, `JOB-0002` 순 반환 |
| 2 | `빈_큐_peek_dequeue` | 빈 큐에서 `peek()` / `dequeue()` → `Optional.empty()` |
| 3 | `FIFO_순서_보장` | 3개 enqueue 후 dequeue 순서가 등록 순서와 동일 |
| 4 | `getQueue_방어적_복사` | 반환된 리스트 수정이 내부 큐에 영향 없음 |

#### 2-C. InMemorySampleRepositoryTest (추가)

**파일**: `src/test/java/ssemi/order/InMemorySampleRepositoryTest.java`

| # | 테스트 메서드 | 검증 내용 |
|---|--------------|-----------|
| 1 | `existsById_없는_ID` | 미등록 ID → `false` |
| 2 | `existsByName_없는_이름` | 미등록 이름 → `false` |
| 3 | `count_여러_시료` | 3개 저장 후 `count()` → 3 |
| 4 | `totalStock_합산` | stock 각각 10·20·30인 3개 저장 → `totalStock()` → 60 |

---

### 사이클 3 — InputHandlerTest 확장

**파일**: `src/test/java/ssemi/order/InputHandlerTest.java` (기존 Phase1Test에서 분리 또는 신규)

| # | 테스트 메서드 | 검증 내용 |
|---|--------------|-----------|
| 1 | `readString_빈_문자열_반환` | 빈 줄 입력 → `""` 반환 |
| 2 | `readString_앞뒤_공백_trim` | `"  hello  "` 입력 → `"hello"` 반환 |
| 3 | `readInt_비숫자_후_재입력` | `"abc\n3\n"` 입력 → 첫 입력 무시, `3` 반환 |
| 4 | `readInt_음수_허용` | `"-5\n"` 입력 → `-5` 반환 |
| 5 | `readDouble_비숫자_후_재입력` | `"xyz\n1.5\n"` 입력 → `1.5` 반환 |

---

### 사이클 4 — ConsoleMenuTest

**파일**: `src/test/java/ssemi/order/ConsoleMenuTest.java`

**공통 픽스처**
```java
private InMemorySampleRepository sampleRepo;
private InMemoryOrderRepository orderRepo;
private ByteArrayOutputStream out;

@BeforeEach
void setUp() {
    sampleRepo = new InMemorySampleRepository();
    orderRepo  = new InMemoryOrderRepository();
    out = new ByteArrayOutputStream();
}
```

| # | 테스트 메서드 | 입력 시나리오 | 검증 내용 |
|---|--------------|--------------|-----------|
| 1 | `displaySummary_시료_없음` | — | `"0개"` 포함 출력 |
| 2 | `displayMainMenu_출력` | — | `"S-Semi"`, `"1. 시료 관리"` 포함 |
| 3 | `run_종료_선택` | `"0\n"` | `"시스템을 종료합니다."` 출력 후 반환 |
| 4 | `run_잘못된_선택_후_종료` | `"9\n0\n"` | `"올바른 메뉴를 선택해주세요."` 출력 후 종료 |
| 5 | `displaySummary_시료_있음` | — | 사전 등록 시료 수·재고 합산 출력 |

---

### 사이클 5 — SampleUITest

**파일**: `src/test/java/ssemi/order/SampleUITest.java`

| # | 테스트 메서드 | 입력 시나리오 | 검증 내용 |
|---|--------------|--------------|-----------|
| 1 | `시료_등록_성공` | `"1\nS01\nAlpha\n30\n0.9\n0\n"` | `"Alpha"` 등록 확인 메시지 출력 |
| 2 | `시료_등록_중복_오류` | S01 기등록 후 동일 ID 재입력 | `"오류:"` 포함 출력 |
| 3 | `시료_목록_빈_경우` | `"2\n0\n"` | `"0개 등록됨"` 출력 |
| 4 | `시료_목록_데이터_있음` | 사전 등록 후 `"2\n0\n"` | 시료명 포함 테이블 출력 |
| 5 | `시료_검색_결과_있음` | `"3\nAlpha\n0\n"` | 해당 시료 행 출력 |
| 6 | `시료_검색_결과_없음` | `"3\nZZZ\n0\n"` | `"검색 결과가 없습니다."` 출력 |

---

### 사이클 6 — OrderUITest

**파일**: `src/test/java/ssemi/order/OrderUITest.java`

| # | 테스트 메서드 | 입력 시나리오 | 검증 내용 |
|---|--------------|--------------|-----------|
| 1 | `주문_예약_성공` | `"1\nS01\nHong\n2\n0\n"` | `"주문이 접수되었습니다"` 출력 |
| 2 | `주문_예약_없는_시료_오류` | `"1\nINVALID\nHong\n2\n0\n"` | `"오류:"` 출력 |
| 3 | `접수_주문_없음` | `"2\n0\n"` | `"접수된 주문이 없습니다."` 출력 |
| 4 | `주문_승인_재고_충분` | RESERVED 주문 있고 재고 충분 → 승인 | `"즉시 출고 대기"` 출력 |
| 5 | `주문_승인_재고_부족` | RESERVED 주문 있고 재고 부족 → 승인 | `"생산라인에 등록"` 출력 |
| 6 | `주문_거절` | RESERVED 주문 → 거절 선택 | `"거절되었습니다"` 출력 |
| 7 | `잘못된_주문번호_입력` | 없는 주문번호 입력 | `"오류:"` 출력 후 반환 |

---

### 사이클 7 — MonitorUITest

**파일**: `src/test/java/ssemi/order/MonitorUITest.java`

| # | 테스트 메서드 | 입력 시나리오 | 검증 내용 |
|---|--------------|--------------|-----------|
| 1 | `주문_현황_빈_경우` | `"1\n0\n"` | 각 상태 `"(0건)"` 포함 출력 |
| 2 | `주문_현황_상태별_그룹` | RESERVED·CONFIRMED 주문 각 1건 사전 등록 → `"1\n0\n"` | 상태별 구분 출력 확인 |
| 3 | `재고_현황_레이블_여유` | stock 충분한 시료 → `"2\n0\n"` | `"[여유]"` 출력 |
| 4 | `재고_현황_레이블_고갈` | stock=0 시료 → `"2\n0\n"` | `"[고갈]"` 출력 |

---

### 사이클 8 — ProductionUITest

**파일**: `src/test/java/ssemi/order/ProductionUITest.java`

| # | 테스트 메서드 | 입력 시나리오 | 검증 내용 |
|---|--------------|--------------|-----------|
| 1 | `현재_작업_없음` | `"1\n0\n"` | `"현재 생산 중인 작업이 없습니다"` 출력 |
| 2 | `현재_작업_있음` | 생산큐에 job 등록 후 `"1\n0\n"` | 작업 ID, 시료명 포함 출력 |
| 3 | `대기_목록_빈_경우` | `"2\n0\n"` | `"대기 중인 작업이 없습니다"` 출력 |
| 4 | `생산_진행_완료` | job 등록 후 targetQty만큼 입력 | `"생산 완료"`, `"CONFIRMED"` 포함 출력 |
| 5 | `생산_진행_중간` | job 등록 후 targetQty 미만 입력 | `"생산 진행 중"` 포함 출력 |

---

### 사이클 9 — ReleaseUITest

**파일**: `src/test/java/ssemi/order/ReleaseUITest.java`

| # | 테스트 메서드 | 입력 시나리오 | 검증 내용 |
|---|--------------|--------------|-----------|
| 1 | `출고_대기_없음` | `"1\n0\n"` | `"출고 대기 중인 주문이 없습니다"` 출력 |
| 2 | `출고_처리_성공` | CONFIRMED 주문 등록 후 주문번호 입력 | `"출고 완료"`, `"RELEASE"` 출력 |
| 3 | `잘못된_주문번호` | 없는 주문번호 입력 | `"오류:"` 출력 |
| 4 | `출고_대기_목록_출력` | CONFIRMED 주문 2건 등록 후 `"1\n..."` | 주문번호 포함 테이블 출력 |

---

## 4. 패키지 구조 (신규 파일)

```
src/test/java/ssemi/order/
├── ProductionResultTest.java          (신규 — 사이클 1)
├── InMemoryOrderRepositoryTest.java   (신규 — 사이클 2-A)
├── InMemoryProductionQueueRepositoryTest.java  (신규 — 사이클 2-B)
├── InMemorySampleRepositoryTest.java  (신규 — 사이클 2-C)
├── InputHandlerTest.java              (신규 — 사이클 3)
├── ConsoleMenuTest.java               (신규 — 사이클 4)
├── SampleUITest.java                  (신규 — 사이클 5)
├── OrderUITest.java                   (신규 — 사이클 6)
├── MonitorUITest.java                 (신규 — 사이클 7)
├── ProductionUITest.java              (신규 — 사이클 8)
└── ReleaseUITest.java                 (신규 — 사이클 9)
```

**변경 파일 없음** — 기존 구현 코드는 수정하지 않는다.

---

## 5. 의존성

신규 의존성 없음. `InputHandler(InputStream, PrintStream)` 생성자가 이미 존재하므로  
`ByteArrayInputStream` / `ByteArrayOutputStream`으로 테스트 가능.

---

## 6. 완료 기준

- 신규 테스트 약 50개 전체 PASS
- 기존 98개 테스트 회귀 없음
- `./gradlew test` 전체 GREEN
