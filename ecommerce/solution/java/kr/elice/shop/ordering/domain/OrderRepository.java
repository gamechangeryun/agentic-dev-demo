package kr.elice.shop.ordering.domain;

import java.util.List;
import java.util.Optional;

/** 주문 저장소 포트입니다. 상태 필터 목록 질의를 포함합니다. */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(String id);

    /** id 오름차순 전체. 상태 필터·페이징은 애플리케이션 계층에서 적용합니다. */
    List<Order> findAll();

    String nextId();
}
