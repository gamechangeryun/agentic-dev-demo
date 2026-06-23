package kr.elice.realfield.ingestion.client;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * data.go.kr 오프라인 스텁(`stub` 프로필). 네트워크·인증키 없이 결정적 캔드 데이터를 공급한다.
 *
 * <p>종로구(11110)·2024년 5월 5건: 정상 4 + 해제 1. 정상 거래금액은 7·8·9·10억이라
 * 해제 1건(20억)을 제외한 중위 거래금액은 (8억+9억)/2 = <b>8.5억(850,000,000원)</b>,
 * 유효 거래수는 <b>4</b>가 된다(서비스 부팅 E2E의 기대값).
 */
@Component
@Profile("stub")
public class StubMolitClient implements MolitTradeSource {

    @Override
    public List<MolitAptTradeItem> fetchAll(String lawdCd, String dealYmd) {
        return List.of(
                item("경복궁아파트", "28", "84.0", "10", "5", "70,000", null, null),
                item("청운현대", "31", "84.0", "7", "12", "80,000", null, null),
                item("북악스카이", "44", "101.0", "15", "18", "90,000", null, null),
                item("인왕산뷰", "52", "114.0", "20", "24", "100,000", null, null),
                // 해제거래(canceled): 시세 집계에서 제외된다.
                item("경복궁아파트", "28", "84.0", "3", "27", "200,000", "O", "24.06.01"));
    }

    private static MolitAptTradeItem item(
            String aptNm, String jibun, String area, String floor, String dealDay,
            String dealAmount, String cdealType, String cdealDay) {
        return new MolitAptTradeItem(
                "11110", "청운동", jibun, aptNm, area,
                "2024", "5", dealDay, dealAmount, floor, "2003",
                "중개거래", cdealType, cdealDay);
    }
}
