package kr.elice.shop.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.context.WebApplicationContext;

/**
 * 전 구간 E2E 테스트입니다. 실제 HTTP 라우팅을 통해 API 를 호출해, 요구사항
 * 원문의 모든 비즈니스 룰이 API 경계에서 동작하는지 검증합니다.
 *
 * <p>각 시나리오는 독립적인 컨텍스트에서 돌도록 {@link DirtiesContext} 로 상태를
 * 초기화합니다. 인메모리 저장소가 시나리오 간 간섭하지 않게 하기 위함입니다.</p>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShopE2ETest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────

    private JsonNode postJson(String url, String body, String idemKey, HttpStatus expect)
            throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            req = req.content(body);
        }
        if (idemKey != null) {
            req = req.header("Idempotency-Key", idemKey);
        }
        MvcResult res = mvc.perform(req).andExpect(status().is(expect.value())).andReturn();
        String content = res.getResponse().getContentAsString();
        return content.isEmpty() ? om.createObjectNode() : om.readTree(content);
    }

    private JsonNode getJson(String url) throws Exception {
        MvcResult res = mvc.perform(get(url)).andExpect(status().isOk()).andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }

    private String createProduct(String name, long price, int stock) throws Exception {
        String body = "{\"name\":\"%s\",\"price\":%d,\"initialStock\":%d}".formatted(name, price, stock);
        return postJson("/api/products", body, null, HttpStatus.CREATED).get("id").asText();
    }

    private String createCartWith(String productId, int qty) throws Exception {
        String cartId = postJson("/api/carts", null, null, HttpStatus.CREATED).get("cartId").asText();
        String item = "{\"productId\":\"%s\",\"qty\":%d}".formatted(productId, qty);
        postJson("/api/carts/" + cartId + "/items", item, null, HttpStatus.OK);
        return cartId;
    }

    private int available(String productId) throws Exception {
        return getJson("/api/inventory/" + productId).get("available").asInt();
    }

    // ── 시나리오 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("E2E-1 전체 여정: 상품 → 장바구니 → 체크아웃 → 결제 → 이행")
    void fullJourney() throws Exception {
        String pid = createProduct("노트북", 1_000_000, 10);
        String cart = createCartWith(pid, 2);

        JsonNode order = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart),
                null, HttpStatus.CREATED);
        assertThat(order.get("status").asText()).isEqualTo("CREATED");
        assertThat(order.get("totalAmount").asLong()).isEqualTo(2_000_000);
        assertThat(available(pid)).isEqualTo(8); // 예약으로 가용분 2 감소

        String orderId = order.get("id").asText();
        JsonNode pay = postJson("/api/payments", "{\"orderId\":\"%s\",\"method\":\"card\"}".formatted(orderId),
                null, HttpStatus.CREATED);
        assertThat(pay.get("status").asText()).isEqualTo("CAPTURED");
        assertThat(getJson("/api/orders/" + orderId).get("status").asText()).isEqualTo("PAID");

        JsonNode fulfilled = postJson("/api/orders/" + orderId + "/fulfill", null, null, HttpStatus.OK);
        assertThat(fulfilled.get("status").asText()).isEqualTo("FULFILLED");
    }

    @Test
    @DisplayName("E2E-2 결제 전 취소: 예약이 풀려 가용 재고가 복원된다")
    void cancelBeforePayment() throws Exception {
        String pid = createProduct("노트북", 1_000_000, 5);
        String cart = createCartWith(pid, 3);
        String orderId = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart),
                null, HttpStatus.CREATED).get("id").asText();
        assertThat(available(pid)).isEqualTo(2);

        JsonNode cancelled = postJson("/api/orders/" + orderId + "/cancel", null, null, HttpStatus.OK);
        assertThat(cancelled.get("status").asText()).isEqualTo("CANCELLED");
        assertThat(available(pid)).isEqualTo(5);
    }

    @Test
    @DisplayName("E2E-3 결제 후 취소: 결제가 환불되고 재고가 복원된다")
    void cancelAfterPaymentRefunds() throws Exception {
        String pid = createProduct("노트북", 1_000_000, 5);
        String cart = createCartWith(pid, 3);
        String orderId = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart),
                null, HttpStatus.CREATED).get("id").asText();
        String payId = postJson("/api/payments", "{\"orderId\":\"%s\",\"method\":\"card\"}".formatted(orderId),
                null, HttpStatus.CREATED).get("id").asText();

        postJson("/api/orders/" + orderId + "/cancel", null, null, HttpStatus.OK);
        assertThat(getJson("/api/payments/" + payId).get("status").asText()).isEqualTo("REFUNDED");
        assertThat(available(pid)).isEqualTo(5);
    }

    @Test
    @DisplayName("E2E-4 oversell 방지: 가용분을 넘는 두 번째 체크아웃은 거부된다")
    void oversellPrevented() throws Exception {
        String pid = createProduct("한정판", 100_000, 5);
        String cart1 = createCartWith(pid, 3);
        String cart2 = createCartWith(pid, 3);
        postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart1), null, HttpStatus.CREATED);
        JsonNode fail = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart2),
                null, HttpStatus.CONFLICT);
        assertThat(fail.get("code").asText()).isEqualTo("INSUFFICIENT_STOCK");
        assertThat(available(pid)).isEqualTo(2); // 첫 주문 예약만 유지, 둘째는 보상 해제
    }

    @Test
    @DisplayName("E2E-5 검색·페이징: 이름 검색과 페이지 크기가 동작한다")
    void searchAndPagination() throws Exception {
        for (int i = 1; i <= 7; i++) {
            createProduct("커피원두 " + i, 10_000 + i, 100);
        }
        createProduct("녹차", 8_000, 100);

        JsonNode page1 = getJson("/api/products?q=커피원두&page=1&size=5");
        assertThat(page1.get("total").asLong()).isEqualTo(7);
        assertThat(page1.get("pages").asInt()).isEqualTo(2);
        assertThat(page1.get("items")).hasSize(5);

        JsonNode page2 = getJson("/api/products?q=커피원두&page=2&size=5");
        assertThat(page2.get("items")).hasSize(2);
    }

    @Test
    @DisplayName("E2E-6 멱등성: 같은 키의 체크아웃·결제는 한 번만 반영된다")
    void idempotentCheckoutAndPayment() throws Exception {
        String pid = createProduct("노트북", 1_000_000, 10);
        String cart = createCartWith(pid, 2);

        String key = "checkout-key-1";
        String o1 = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart), key,
                HttpStatus.CREATED).get("id").asText();
        String o2 = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart), key,
                HttpStatus.CREATED).get("id").asText();
        assertThat(o1).isEqualTo(o2);
        assertThat(available(pid)).isEqualTo(8); // 예약은 한 번만

        String payKey = "pay-key-1";
        String body = "{\"orderId\":\"%s\",\"method\":\"card\"}".formatted(o1);
        String p1 = postJson("/api/payments", body, payKey, HttpStatus.CREATED).get("id").asText();
        String p2 = postJson("/api/payments", body, payKey, HttpStatus.CREATED).get("id").asText();
        assertThat(p1).isEqualTo(p2);
        assertThat(getJson("/api/orders/" + o1).get("status").asText()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("E2E-7 아카이브 차단: ARCHIVED 상품은 장바구니에 담을 수 없다")
    void archivedProductBlocked() throws Exception {
        String pid = createProduct("단종상품", 50_000, 5);
        postJson("/api/products/" + pid + "/archive", null, null, HttpStatus.OK);

        String cartId = postJson("/api/carts", null, null, HttpStatus.CREATED).get("cartId").asText();
        JsonNode fail = postJson("/api/carts/" + cartId + "/items",
                "{\"productId\":\"%s\",\"qty\":1}".formatted(pid), null, HttpStatus.CONFLICT);
        assertThat(fail.get("code").asText()).isEqualTo("PRODUCT_ARCHIVED");
    }

    @Test
    @DisplayName("E2E-8 이행 가드: 결제 전 주문은 이행이 거부된다")
    void fulfillRequiresPaid() throws Exception {
        String pid = createProduct("노트북", 1_000_000, 5);
        String cart = createCartWith(pid, 1);
        String orderId = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart),
                null, HttpStatus.CREATED).get("id").asText();

        JsonNode fail = postJson("/api/orders/" + orderId + "/fulfill", null, null, HttpStatus.CONFLICT);
        assertThat(fail.get("code").asText()).isEqualTo("PAYMENT_REQUIRED");
    }

    @Test
    @DisplayName("E2E-9 결제 거절: declined 수단은 402 로 거부되고 주문은 CREATED 로 남는다")
    void paymentDeclined() throws Exception {
        String pid = createProduct("노트북", 1_000_000, 5);
        String cart = createCartWith(pid, 1);
        String orderId = postJson("/api/checkout", "{\"cartId\":\"%s\"}".formatted(cart),
                null, HttpStatus.CREATED).get("id").asText();

        JsonNode fail = postJson("/api/payments",
                "{\"orderId\":\"%s\",\"method\":\"declined\"}".formatted(orderId),
                null, HttpStatus.PAYMENT_REQUIRED);
        assertThat(fail.get("code").asText()).isEqualTo("PAYMENT_DECLINED");
        assertThat(getJson("/api/orders/" + orderId).get("status").asText()).isEqualTo("CREATED");
    }
}
