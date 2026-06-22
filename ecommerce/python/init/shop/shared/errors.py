"""도메인 오류 코드와 도메인 예외입니다.

도메인 계층은 ``DomainException`` 만 던지고, 웹 계층(이 포팅에서는 호출자)이
오류 코드에 맞는 응답으로 변환합니다. 도메인은 HTTP 를 알지 못합니다.
각 코드는 자바 ``ErrorCode`` enum 과 1:1 로 대응하며, HTTP 상태는 의미 보존을
위해 참고값으로 함께 담아 둡니다.
"""

from __future__ import annotations

from enum import Enum


class ErrorCode(Enum):
    """도메인 오류 코드입니다. 요구사항의 거부 사유 하나하나가 코드 하나입니다.

    값은 (HTTP 상태) 로, 자바 enum 의 ``status()`` 와 같은 의미를 유지합니다.
    """

    NOT_FOUND = 404
    INVALID_PRICE = 400
    INVALID_AMOUNT = 400
    INVALID_QTY = 400
    EMPTY_NAME = 400
    EMPTY_CART = 400
    INSUFFICIENT_STOCK = 409
    PRODUCT_ARCHIVED = 409
    INVALID_STATE_TRANSITION = 409
    PAYMENT_REQUIRED = 409
    ALREADY_PAID = 409
    PAYMENT_DECLINED = 402
    REFUND_NOT_ALLOWED = 409

    @property
    def status(self) -> int:
        """이 오류 코드에 대응하는 HTTP 상태 코드를 돌려줍니다."""
        return self.value


class DomainException(Exception):
    """도메인 규칙 위반을 표현하는 예외입니다.

    오류 코드를 함께 실어, 호출자가 사유에 맞는 응답을 만들 수 있게 합니다.
    """

    def __init__(self, code: ErrorCode, message: str) -> None:
        super().__init__(message)
        self._code = code

    @property
    def code(self) -> ErrorCode:
        """위반된 도메인 규칙의 오류 코드를 돌려줍니다."""
        return self._code
