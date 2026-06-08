package kr.elice.shop.shared;

import java.util.List;

/**
 * 페이지 조회 결과를 표현하는 공용 값 객체입니다.
 *
 * <p>목록 API 는 전체 건수와 페이지 수를 함께 돌려주어, 호출자가 다음 페이지
 * 존재 여부를 계산할 수 있게 합니다.</p>
 */
public record Page<T>(List<T> items, long total, int page, int size, int pages) {

    public static <T> Page<T> of(List<T> all, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        long total = all.size();
        int pages = (int) ((total + safeSize - 1) / safeSize);
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return new Page<>(all.subList(from, to), total, safePage, safeSize, pages);
    }
}
