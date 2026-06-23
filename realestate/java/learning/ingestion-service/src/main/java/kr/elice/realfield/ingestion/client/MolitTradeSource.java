package kr.elice.realfield.ingestion.client;

import java.util.List;

/**
 * 원천 거래 공급 포트. 실거래 원천(item)을 한 시군구·계약월 단위로 전량 공급한다.
 *
 * <p>운영 프로필은 data.go.kr 실호출({@link MolitApiClient}), `stub` 프로필은 오프라인 캔드
 * 데이터({@code StubMolitClient})를 쓴다. 수집 오케스트레이션은 이 계약에만 의존한다(외부 의존 격리).
 */
public interface MolitTradeSource {
    List<MolitAptTradeItem> fetchAll(String lawdCd, String dealYmd);
}
