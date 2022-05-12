package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    Member member;
    Item book;

    @BeforeEach
    void beforeEach() {
        member = new Member();
        member.setName("member");
        member.setAddress(new Address("서울", "도로", "123"));
        em.persist(member);
        book = new Book();
        book.setName("JPA");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);
    }

    @Test
    void 상품주문() {
        //given

        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order foundOrder = orderRepository.findById(orderId);

        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(foundOrder.getOrderItems().size()).isEqualTo(1);
        assertThat(foundOrder.getTotalPrice()).isEqualTo(book.getPrice() * orderCount);
        assertThat(book.getStockQuantity()).as("수량은 8이어야한다.").isEqualTo(10 - orderCount);
    }

    @Test
    void 상품주문_재고수량초과_에러() {
        //given
        int orderCount = 11;
        //when

        //then
        assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount)).isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    void 주문취소() {
        //given
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order foundOrder = orderRepository.findById(orderId);
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).isEqualTo(10);
    }
}