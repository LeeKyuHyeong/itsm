# CLAUDE.md

## Commit Rules
- 커밋 메시지에 Claude가 진행했다는 내용(Co-Authored-By 등)을 절대 포함하지 않는다.
- 커밋 전 반드시 `TODO.md`를 확인하여 완료된 항목을 `[x]`로 업데이트한다.

## Development Rules
- **TDD (Test-Driven Development)** 방식으로 개발한다.
  - 1) Red: 실패하는 테스트를 먼저 작성한다.
  - 2) Green: 테스트를 통과하는 최소한의 코드를 작성한다.
  - 3) Refactor: 테스트가 통과하는 상태에서 코드를 개선한다.
- 백엔드: JUnit 5 + MockMvc + Mockito 사용
- 프론트엔드: Vitest + Vue Test Utils 사용
- 기능 구현 전 반드시 테스트 코드를 먼저 작성한다.

## Entity 검수 규칙
- JPA Entity에서 `char(1)` 컬럼(`is_active`, `is_visible`, `allow_comment` 등 Y/N 값)은 반드시 `@Column(columnDefinition = "char(1)")` 어노테이션을 명시해야 한다. 누락 시 Hibernate가 `varchar(255)`로 매핑하여 런타임 오류가 발생한다.
- Entity를 **새로 추가하거나 수정할 때** 반드시 해당 Entity의 모든 `char(1)` 필드에 `columnDefinition`이 정확히 선언되어 있는지 검수한다.

## 다국어 & 테마 규칙
- 이 프로젝트는 **다크/라이트 테마** 및 **한국어(ko)/영어(en) 다국어**를 모두 지원한다.
- 프론트엔드 UI 작업 시 두 테마 모두에서 정상 표시되는지 확인한다. CSS 변수(`var(--color-*)`)를 사용하고, 하드코딩된 색상값을 쓰지 않는다.
- 사용자에게 보이는 모든 텍스트는 i18n 키(`t('...')`)를 사용한다. 하드코딩된 한국어/영어 문자열을 직접 넣지 않는다.
- DB에서 관리되는 데이터(메뉴명, 공통코드, 게시판명, 배치명 등)는 `_en` 접미사 컬럼(예: `menu_nm_en`, `code_nm_en`)을 통해 영문명을 함께 저장하고, 프론트엔드에서 locale에 따라 분기 표시한다.
