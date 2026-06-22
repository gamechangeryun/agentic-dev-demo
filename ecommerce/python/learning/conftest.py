"""pytest 부트스트랩입니다.

테스트가 ``import shop`` 으로 도메인을 불러올 수 있도록, 이 디렉토리(python/)를
모듈 검색 경로 맨 앞에 둡니다. 외부 패키징 도구 없이 표준 구조로 동작합니다.
"""

import os
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)
