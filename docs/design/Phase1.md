# Phase1: 기반 구조 + 메인 메뉴

## 1. 기능 개요
- 프로젝트 패키지 구조 및 도메인 모델 기반 설계
- 콘솔 메인 메뉴 구현 (기능 선택 화면 + 전체 시료 요약 정보)

## 2. 도메인 모델

### OrderStatus (Enum)
```
RESERVED   - 주문 접수
REJECTED   - 주문 거절 (비정상 흐름)
PRODUCING  - 재고 부족으로 생산 중
CONFIRMED  - 출고 대기 중
RELEASE    - 출고 완료
```

### Sample (시료)
| 필드 | 타입 | 설명 |
|------|------|------|
| id | String | 시료 고유 ID |
| name | String | 시료 이름 |
| avgProductionTime | int | 평균 생산시간 (분) |
| yield | double | 수율 (0.0 ~ 1.0) |
| stock | int | 현재 재고 수량 |

### Order (주문)
| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | String | 주문 고유 ID |
| sample | Sample | 주문 시료 |
| customerName | String | 고객명 |
| quantity | int | 주문 수량 |
| status | OrderStatus | 현재 주문 상태 |

### ProductionJob (생산 작업)
| 필드 | 타입 | 설명 |
|------|------|------|
| jobId | String | 작업 ID |
| order | Order | 연결된 주문 |
| targetQty | int | 실 생산 목표량 (ceil(부족분 / (수율 * 0.9))) |
| producedQty | int | 현재 생산 완료량 |
| totalTime | int | 총 생산 예상 시간 (분) |

## 3. 클래스 설계

### ConsoleMenu
```
책임: 메인 메뉴 루프, 사용자 입력 라우팅
메서드:
  + run(): void
  + displayMainMenu(): void
  + displaySummary(): void
  - readChoice(): int
```

### InputHandler
```
책임: Scanner 래핑, 입력 유효성 검증
메서드:
  + readInt(prompt: String): int
  + readString(prompt: String): String
  + readDouble(prompt: String): double
```

## 4. 메인 메뉴 화면 구성
```
========================================
   S-Semi 반도체 시료생산 주문관리 시스템
========================================
[시료 요약]
 등록 시료 수: N개 | 전체 재고: N개

[메뉴 선택]
  1. 시료 관리
  2. 주문 (접수/승인/거절)
  3. 모니터링
  4. 생산 라인
  5. 출고 처리
  0. 종료
----------------------------------------
선택 > 
```

## 5. 시퀀스 흐름
```
Main → ConsoleMenu.run()
  └─ 루프:
       ConsoleMenu.displaySummary()   // 시료 요약
       ConsoleMenu.displayMainMenu()  // 메뉴 출력
       InputHandler.readInt()         // 선택 입력
       → 각 UI 핸들러로 라우팅
```

## 6. TDD 테스트 계획

### Phase1Test
| 테스트 케이스 | 검증 내용 |
|---------------|-----------|
| `sample_생성_시_필드값_정상저장` | id, name, avgProductionTime, yield, stock 검증 |
| `order_생성_초기상태_RESERVED` | 주문 생성 시 status = RESERVED |
| `orderStatus_유효값_5가지` | RESERVED, REJECTED, PRODUCING, CONFIRMED, RELEASE |
| `productionJob_실생산량_계산_정확성` | ceil(부족분 / (수율 * 0.9)) 공식 검증 |
| `productionJob_총생산시간_계산` | avgProductionTime * targetQty 검증 |
| `inputHandler_숫자_입력_정상처리` | 정수 입력 검증 |
| `inputHandler_잘못된_입력_예외처리` | 비정수 입력 시 재입력 유도 |

## 7. 구현 순서 (TDD 사이클)
1. `OrderStatus` enum 정의
2. `Sample` 도메인 + 테스트
3. `Order` 도메인 + 테스트
4. `ProductionJob` 도메인 + 테스트
5. `InputHandler` 유틸 + 테스트
6. `ConsoleMenu` UI + 통합 흐름 확인
