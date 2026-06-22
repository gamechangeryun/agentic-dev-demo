# 01_planning · 색인

이커머스 쇼핑 서비스의 계획 산출물 색인입니다.

| 문서 | 내용 |
| --- | --- |
| `01_feature/shop_feature_spec.md` | 여섯 bounded context 의 EARS 명세와 AC 매핑 |

## AC 요약

- catalog 7개 (AC-C1~C7): 등록·검색·재고·아카이브·멱등
- inventory 5개 (AC-I1~I5): 예약·확정·해제·oversell·동시성
- cart 3개 (AC-T1~T3): 담기·수량·아카이브 차단
- ordering 7개 (AC-O1~O7): 체크아웃·보상·총액·상태전환·이행가드·멱등
- payment 5개 (AC-P1~P5): 승인·거절·멱등·환불·환불가드

총 27개 AC 가 JUnit 단위 14 + E2E 9 테스트로 검증됩니다.
