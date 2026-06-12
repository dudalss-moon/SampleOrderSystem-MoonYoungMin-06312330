# Phase2: 시료 관리

## 1. 기능 개요
시료(Sample)는 시스템의 가장 기본 단위이며, 등록된 시료만 주문 가능.
- 시료 등록: 새로운 시료를 시스템에 추가
- 시료 조회: 등록된 전체 시료 목록 + 현재 재고 수량 표시
- 시료 검색: 이름 등 속성으로 특정 시료 검색

## 2. 도메인 규칙

### 시료 등록 규칙
- 시료 ID는 시스템 내 유일해야 함
- 이름 중복 불허
- 평균 생산시간 > 0
- 수율: 0.0 < yield ≤ 1.0
- 초기 재고는 0

### 시료 검색 규칙
- 이름 부분 일치(contains) 검색 지원
- 검색 결과 없을 경우 안내 메시지 출력

## 3. 클래스 설계

### Sample (도메인)
```java
public class Sample {
    private final String id;
    private final String name;
    private final int avgProductionTime;  // 분
    private final double yield;           // 0.0 ~ 1.0
    private int stock;                    // 가변 (생산/출고에 따라 변동)

    // 재고 추가/차감 메서드
    + addStock(int qty): void
    + deductStock(int qty): void
    + hasEnoughStock(int qty): boolean
}
```

### SampleRepository (인메모리)
```java
public class SampleRepository {
    private final Map<String, Sample> store = new LinkedHashMap<>()

    + save(sample: Sample): void
    + findById(id: String): Optional<Sample>
    + findByName(name: String): List<Sample>
    + findAll(): List<Sample>
    + existsById(id: String): boolean
    + existsByName(name: String): boolean
}
```

### SampleService
```java
public class SampleService {
    - repository: SampleRepository

    + register(id, name, avgTime, yield): Sample
    + findAll(): List<Sample>
    + search(keyword: String): List<Sample>
    + findById(id: String): Sample
}
```

### SampleUI
```java
public class SampleUI {
    - service: SampleService
    - input: InputHandler

    + show(): void
    - registerSample(): void
    - listSamples(): void
    - searchSample(): void
}
```

## 4. 화면 구성

### 시료 관리 메뉴
```
========================================
              시료 관리
========================================
  1. 시료 등록
  2. 시료 목록 조회
  3. 시료 검색
  0. 메인 메뉴로
----------------------------------------
선택 > 
```

### 시료 등록 화면
```
[시료 등록]
시료 ID       > S001
시료 이름     > 알파센서
평균생산시간(분) > 30
수율 (0~1.0)  > 0.9
→ 시료 'S001 - 알파센서'이 등록되었습니다.
```

### 시료 목록 조회
```
[시료 목록]
------------------------------------------------------------
 ID    | 이름         | 생산시간(분) | 수율  | 재고
------------------------------------------------------------
 S001  | 알파센서      |     30      | 0.90 |   0개
 S002  | 베타칩        |     45      | 0.85 |  10개
------------------------------------------------------------
총 2개 등록됨
```

### 시료 검색
```
[시료 검색]
검색어 > 알파
------------------------------------------------------------
 ID    | 이름         | 생산시간(분) | 수율  | 재고
------------------------------------------------------------
 S001  | 알파센서      |     30      | 0.90 |   0개
------------------------------------------------------------
1개 검색됨
```

## 5. 시퀀스 흐름

### 시료 등록
```
SampleUI.registerSample()
  → InputHandler: id, name, avgTime, yield 입력
  → SampleService.register()
      → Repository.existsById() 중복 체크
      → Repository.existsByName() 이름 중복 체크
      → new Sample() 생성
      → Repository.save()
  → 성공 메시지 출력
```

### 시료 조회
```
SampleUI.listSamples()
  → SampleService.findAll()
      → Repository.findAll()
  → 테이블 형식 출력
```

## 6. TDD 테스트 계획

### SampleTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `시료_생성_정상` | 모든 필드 정상 설정 |
| `수율_0이하_예외` | yield <= 0 시 IllegalArgumentException |
| `수율_1초과_예외` | yield > 1.0 시 IllegalArgumentException |
| `평균생산시간_0이하_예외` | avgTime <= 0 시 IllegalArgumentException |
| `재고_추가_정상` | addStock(10) → stock = 10 |
| `재고_차감_정상` | stock=10, deductStock(3) → stock = 7 |
| `재고_차감_부족_예외` | stock=5, deductStock(10) 시 IllegalStateException |
| `재고_충분_여부_확인` | hasEnoughStock() true/false 검증 |

### SampleRepositoryTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `시료_저장_후_ID로_조회` | save → findById 정상 반환 |
| `전체_시료_조회` | findAll() 등록 순서 보장 |
| `이름으로_검색_부분일치` | "알파" 검색 시 "알파센서" 포함 반환 |
| `존재하지않는_ID_조회_빈값` | Optional.empty() 반환 |
| `중복_ID_존재여부_확인` | existsById() true 반환 |

### SampleServiceTest
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `시료_등록_정상` | register() → Repository에 저장 |
| `중복_ID_등록_예외` | 같은 ID로 register() 시 IllegalArgumentException |
| `중복_이름_등록_예외` | 같은 이름으로 register() 시 IllegalArgumentException |
| `검색_결과_없음_빈리스트` | findByName() 매칭 없을 때 빈 리스트 |

## 7. 구현 순서 (TDD 사이클)
1. `Sample` 도메인 테스트 → 구현
2. `SampleRepository` 테스트 → 구현
3. `SampleService` 테스트 → 구현
4. `SampleUI` 구현 (UI는 통합 확인)
