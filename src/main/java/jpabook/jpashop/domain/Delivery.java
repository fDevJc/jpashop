package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {
    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    //ORDINAL 쓰면 숫자로 들어가는데 운영중에 순서가 바뀌면 큰일난다.
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; // [READY, COMP]
}
