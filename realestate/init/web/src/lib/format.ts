/**
 * 한국어 표기 포맷 유틸. 원(KRW) 단위 정수를 억/만원 단위로 사람이 읽기 좋게 변환합니다.
 */

const EOK = 100_000_000; // 1억
const MAN = 10_000; // 1만

/**
 * 원 단위 금액을 "12억 3,400만원" 형태로 변환합니다.
 * - 1억 이상: "N억 M만원" (M이 0이면 "N억")
 * - 1만 이상: "M만원"
 * - 그 외: "N원"
 */
export function formatWon(won: number): string {
  if (won == null || Number.isNaN(won)) return "-";
  if (won === 0) return "0원";

  const sign = won < 0 ? "-" : "";
  const abs = Math.abs(Math.round(won));

  if (abs >= EOK) {
    const eok = Math.floor(abs / EOK);
    const manPart = Math.floor((abs % EOK) / MAN);
    if (manPart === 0) return `${sign}${eok.toLocaleString("ko-KR")}억원`;
    return `${sign}${eok.toLocaleString("ko-KR")}억 ${manPart.toLocaleString("ko-KR")}만원`;
  }

  if (abs >= MAN) {
    const man = Math.floor(abs / MAN);
    const rest = abs % MAN;
    if (rest === 0) return `${sign}${man.toLocaleString("ko-KR")}만원`;
    return `${sign}${man.toLocaleString("ko-KR")}만 ${rest.toLocaleString("ko-KR")}원`;
  }

  return `${sign}${abs.toLocaleString("ko-KR")}원`;
}

/** 짧은 축약 표기: 억 단위 소수 1자리. 차트 축 라벨 등에 사용합니다. (예: 12.3억) */
export function formatWonShort(won: number): string {
  if (won == null || Number.isNaN(won)) return "-";
  if (won === 0) return "0";
  const abs = Math.abs(won);
  if (abs >= EOK) {
    return `${(won / EOK).toFixed(1)}억`;
  }
  if (abs >= MAN) {
    return `${Math.round(won / MAN).toLocaleString("ko-KR")}만`;
  }
  return won.toLocaleString("ko-KR");
}

/** ㎡당 단가를 "만원/㎡" 단위로 표기합니다. (예: 1,250만원/㎡) */
export function formatPerM2(wonPerM2: number): string {
  if (wonPerM2 == null || Number.isNaN(wonPerM2) || wonPerM2 === 0) return "-";
  const man = Math.round(wonPerM2 / MAN);
  return `${man.toLocaleString("ko-KR")}만원/㎡`;
}

/** 전용면적을 "84.93㎡ (약 25.7평)" 형태로 표기합니다. */
export function formatArea(areaM2: number): string {
  if (areaM2 == null || Number.isNaN(areaM2)) return "-";
  const pyeong = areaM2 / 3.305785;
  return `${areaM2.toLocaleString("ko-KR", { maximumFractionDigits: 2 })}㎡ (약 ${pyeong.toFixed(1)}평)`;
}

/** 면적만 ㎡로 간단 표기합니다. (예: 84.93㎡) */
export function formatAreaShort(areaM2: number): string {
  if (areaM2 == null || Number.isNaN(areaM2)) return "-";
  return `${areaM2.toLocaleString("ko-KR", { maximumFractionDigits: 2 })}㎡`;
}

/** 계약일을 "2024.05.13" 형태로 표기합니다. */
export function formatDealDate(year: number, month: number, day: number): string {
  const mm = String(month).padStart(2, "0");
  const dd = String(day).padStart(2, "0");
  return `${year}.${mm}.${dd}`;
}
