package kr.elice.shop.inventory.web;

import java.util.Map;
import kr.elice.shop.inventory.application.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 재고 가용량 조회 API 입니다.
 *
 * <p>예약·확정·해제는 checkout·payment 오케스트레이션이 내부에서 호출하므로
 * 외부에는 가용량 조회만 노출합니다. 데모에서 oversell 방지 효과를 눈으로
 * 확인하는 용도입니다.</p>
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventory;

    public InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/{productId}")
    public Map<String, Object> available(@PathVariable String productId) {
        return Map.of("productId", productId, "available", inventory.available(productId));
    }
}
