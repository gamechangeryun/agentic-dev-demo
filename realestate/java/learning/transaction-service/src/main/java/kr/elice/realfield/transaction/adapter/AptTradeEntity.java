package kr.elice.realfield.transaction.adapter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.elice.realfield.common.AptTransaction;

/**
 * 거래원장 영속 엔티티.
 *
 * <p>{@code natural_key} 컬럼에 유니크 제약을 두어 DB 레벨에서도 멱등을 보장한다(AC-4).
 * 도메인 {@link AptTransaction}과의 변환은 {@link #from}/{@link #toDomain}이 담당한다.
 */
@Entity
@Table(name = "apt_trade",
        uniqueConstraints = @UniqueConstraint(name = "uk_apt_trade_natural_key", columnNames = "natural_key"))
public class AptTradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "natural_key", nullable = false, unique = true, length = 200)
    private String naturalKey;

    private String sggCd;
    private String umdNm;
    private String aptNm;
    private double exclusiveArea;
    @Column(name = "floor_no")
    private int floor;
    private int buildYear;
    private int dealYear;
    private int dealMonth;
    private int dealDay;
    private long dealAmountWon;
    private boolean canceled;

    protected AptTradeEntity() {
    }

    private AptTradeEntity(AptTransaction t) {
        this.naturalKey = t.naturalKey();
        this.sggCd = t.sggCd();
        this.umdNm = t.umdNm();
        this.aptNm = t.aptNm();
        this.exclusiveArea = t.exclusiveArea();
        this.floor = t.floor();
        this.buildYear = t.buildYear();
        this.dealYear = t.dealYear();
        this.dealMonth = t.dealMonth();
        this.dealDay = t.dealDay();
        this.dealAmountWon = t.dealAmountWon();
        this.canceled = t.canceled();
    }

    static AptTradeEntity from(AptTransaction t) {
        return new AptTradeEntity(t);
    }

    AptTransaction toDomain() {
        return new AptTransaction(sggCd, umdNm, aptNm, exclusiveArea, floor, buildYear,
                dealYear, dealMonth, dealDay, dealAmountWon, canceled);
    }
}
