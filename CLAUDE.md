# CLAUDE.md

## Commit Rules
- 커밋 메시지에 Claude가 진행했다는 내용(Co-Authored-By 등)을 절대 포함하지 않는다.

## Development Rules
- **TDD (Test-Driven Development)** 방식으로 개발한다.
  - 1) Red: 실패하는 테스트를 먼저 작성한다.
  - 2) Green: 테스트를 통과하는 최소한의 코드를 작성한다.
  - 3) Refactor: 테스트가 통과하는 상태에서 코드를 개선한다.
- 백엔드: JUnit 5 + MockMvc + Mockito 사용
- 프론트엔드: Vitest + Vue Test Utils 사용
- 기능 구현 전 반드시 테스트 코드를 먼저 작성한다.
