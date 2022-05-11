package jpabook.jpashop;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public Long save(Member member) {
        em.persist(member);
        //커맨드와 쿼리를 분리해라 원칙
        //사이드이펙트를 일으키는 커맨드성이기때문에 리턴값을 거의 안만든다. 취향이라고함
        return member.getId();
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
