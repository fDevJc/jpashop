package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    void 회원가입() {
        //given
        Member member = new Member();
        member.setName("memberA");

        //when
        Long savedId = memberService.join(member);

        //then
        /**
         * @Transactional 애노테이션을 사용할 경우 테스트를 위하여 롤백된다.
         * JPA의 경우 commit이 될때 insert쿼리를 날리기 때문에 로그에 insert쿼리를 확인할 수 없다.
         * insert쿼리를 확인하기 위하여 EntityManager를 주입받아 flush를 해주면 insert쿼리를 확인할 수 있다.
         * 혹은 @Rollback false 를 사용하는 방법도 있다.(이때에는 디비에 인서트까지 됨)
         */

        em.flush();
        assertThat(member).isEqualTo(memberRepository.findById(savedId));
    }

    @Test
    void 중복_회원_가입_에러() {
        //given
        Member member1 = new Member();
        member1.setName("member");
        Member member2 = new Member();
        member2.setName("member");
        //when
        memberService.join(member1);

        //then
        assertThatThrownBy(() -> memberService.join(member2)).isInstanceOf(IllegalStateException.class);
    }
}