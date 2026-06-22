package kr.elice.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 이커머스 모놀리식 애플리케이션 진입점입니다.
 *
 * <p>하나의 배포 단위 안에서 여섯 개의 bounded context 를 패키지로 분리합니다.
 * catalog 는 상품 카탈로그를, inventory 는 재고 예약을, cart 는 장바구니를,
 * ordering 은 주문 수명주기를, payment 는 결제와 환불을, checkout 은 이들을
 * 묶는 오케스트레이션을 담당합니다.</p>
 */
@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
