package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/*
    x To One( ManyToOne , OneToOne ) 컬렉션이 아닌거
    Order
    order -> member
    order -> delivery
 */
@RequiredArgsConstructor
@RestController
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    /*
        V1 엔티티를 바로 리턴
        1. 바로 실행하면 에러발생
        jackson 라이브러리 입장에서
        order -> member -> order -> member -> order -> member ....... 무한 루프
        해결방법 => @JsonIgnore

        2. @JsonIgnore 애노테이션 추가후 또 에러
        com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: java.util.ArrayList[0]->jpabook.jpashop.domain.Order["member"]->jpabook.jpashop.domain.Member$HibernateProxy$OhObHQ0V["hibernateLazyInitializer"])
        Jackson라이브러리가 Order를 json으로 만들려고 하는데 member객체를 보니 순수한 객체가 아니라 proxy객체다( 실제로 Member member = new ByteBuddyInterceptor...식으로 되어있음)
        해결방법 => jackson-datatype-hibernate5 라이브러리 사용
        참고: 엔티티를 외부에 노출할필요없다. 실무에서는 사용할 일 없다.
    */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getId(); //Lazy 강제 초기화
        }
        return orders;
    }
    /*
        V2 DTO를 리턴
        dto로 변환해서 엔티티의 변경이 API 스펙의 변경으로 이어지지 않는다.
        하지만 order, member, deliver 로 인하여 쿼리가 상당히 많이 나가게 된다.
        실행시
        1번 쿼리 : Order 셀렉트 쿼리 -> 주문 2개
        2번 쿼리 : order1의 member 관련 셀렉트 쿼리
        3번 쿼리 : order1의 delivery 관련 셀렉트 쿼리
        4번 쿼리 : order2의 member 관련 셀력트 쿼리
        5번 쿼리 : order2의 delivery 관련 셀렉트 쿼리
        총 다섯번의 쿼리가 실행된다.
        ==> N + 1 문제 발생
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }
    /*
        fetch join 적용
        ==> 한방쿼리로 한번에 조회
            select
                order0_.order_id as order_id1_6_0_,
                member1_.member_id as member_i1_4_1_,
                delivery2_.delivery_id as delivery1_2_2_,
                order0_.delivery_id as delivery4_6_0_,
                order0_.member_id as member_i5_6_0_,
                order0_.order_date as order_da2_6_0_,
                order0_.status as status3_6_0_,
                member1_.city as city2_4_1_,
                member1_.street as street3_4_1_,
                member1_.zipcode as zipcode4_4_1_,
                member1_.name as name5_4_1_,
                delivery2_.city as city2_2_2_,
                delivery2_.street as street3_2_2_,
                delivery2_.zipcode as zipcode4_2_2_,
                delivery2_.status as status5_2_2_
            from
                orders order0_
            inner join
                member member1_
                    on order0_.member_id=member1_.member_id
            inner join
                delivery delivery2_
                    on order0_.delivery_id=delivery2_.delivery_id
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        return orderRepository.findAllWithMemberDelivery().stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderRepository.findOrderDTOs();
    }

    @Data
    @AllArgsConstructor
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        //중요하지 않은거에서 중요한걸 의존하는거기때문에 크게 신경안써도 된다.
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
