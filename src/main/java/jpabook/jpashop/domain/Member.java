package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    public Long id;

    private String name;

    @Embedded
    private Address address;

    //member클래스의 member변수에 거울일 뿐이야
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}