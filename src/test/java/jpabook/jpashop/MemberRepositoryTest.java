package jpabook.jpashop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
//    테스트 케이스에 있으면 롤백한다.
    @Rollback(value = false)
//    해당 애노테이션을 주면 롤백 X
    void testMember() {
        //given
        Member member = new Member();
        member.setUserName("A");
        //when
        Long saveId = memberRepository.save(member);
        Member foundMember = memberRepository.find(saveId);

        //then
        assertThat(foundMember.getId()).isEqualTo(saveId);
        assertThat(foundMember.getUserName()).isEqualTo(member.getUserName());
        //영속성 컨텍스트 1차 캐시
        assertThat(foundMember).isEqualTo(member);
    }
}