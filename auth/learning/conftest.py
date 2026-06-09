# -*- coding: utf-8 -*-
"""pytest 패키지 경로 설정. 시작 버전에는 픽스처가 없습니다.

구현(server/contexts/auth)과 테스트는 학습자가 build 단계(S09)에서 만듭니다. 그때
필요한 픽스처도 이 파일에 추가합니다. 완성 형태는 ../complete/conftest.py 를 참고하세요.
"""
import os
import sys

sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))
