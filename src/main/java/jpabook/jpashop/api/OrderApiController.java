package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            order.getOrderItems().stream().forEach(orderItem -> orderItem.getItem().getName());
        }
        return orders;
    }

    /*
        11번의 쿼리실행됨
         1. Order 조회 쿼리 실행 -> 2개
         2. Member 조회 쿼리 실행
         3. Delivery 조회 쿼리 실행
         4. OrderItem 조회 쿼리 실행 -> 2개
         5. Item 조회 쿼리 실행
         6. Item 조회 쿼리 실행
         7. Member 조회 쿼리 실행
         8. Delivery 조회 쿼리 실행
         9. OrderItem 조회 쿼리 실행 -> 2개
        10. Item 조회 쿼리 실행
        11. Item 조회 쿼리 실행
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
    }

    /*
        order 중복으로 인해 distinct 사용
        order 1 item 1
        order 1 item 2
        order 2 item 3
        order 2 item 4
        처럼 데이터 베이스는 4줄로 나온다. 그래서 distinct가 없는경우 로우수가 많아짐
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        return orderRepository.findAllWithItem().stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
    }

    /*
        fetch join + hibernate default_batch_fetch_size ( 100 ~ 1000 사이를 권장 )
        1. Order 조회 쿼리 실행 -> 2개
        2. OrderItem 조회 쿼리 실행 ( 이때 in 절이 사용된다 )
        3. Item 조회 쿼리 실행 ( 이때 in 절이 사용된다 )

        v3와 비교하여 쿼리수는 1:3으로 2개는 더 많다.
        하지만 v3의 경우 한방 쿼리이기는 하지만 데이터는 중복된 데이터가 더 많다. 즉 DB -> AP 로 반환되는 데이터양이 많다는 이야기
        호출 수 vs 한번의 데이터양 생각해보고 정하면될듯
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return orderRepository.findAllWithMemberDelivery(offset, limit).stream()
                .map(order -> new OrderDto(order))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> ordersV6() {
        return orderQueryRepository.findAllByDto_flat();
    }

        @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
