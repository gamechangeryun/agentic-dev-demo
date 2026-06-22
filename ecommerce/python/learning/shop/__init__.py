"""이커머스 모놀리식 도메인입니다(파이썬 포팅).

단일 패키지 안에서 DDD bounded context 를 모듈로 분리합니다:
catalog · inventory · cart · ordering · payment · checkout. 외부 DB·브로커
의존 없이 인메모리 저장소(dict)로 동작합니다. 자바 Spring Boot solution 의
도메인 규칙을 순수 파이썬으로 옮긴 것입니다.
"""

from .app import ShopApp

__all__ = ["ShopApp"]
