"""공용 도메인 빌딩블록입니다.

오류 코드, 도메인 예외, 금액 값 객체, 페이지 값 객체를 모아 둡니다.
자바 solution 의 shared 패키지(Money·DomainException·ErrorCode·Page)를
순수 파이썬으로 옮긴 것입니다.
"""

from .errors import DomainException, ErrorCode
from .money import Money
from .page import Page

__all__ = ["DomainException", "ErrorCode", "Money", "Page"]
