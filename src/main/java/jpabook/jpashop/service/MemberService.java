package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     * @param member
     * @return
     */
    @Transactional
    //기본이 false 클래스 레벨에서 true를 주고 메서드단위에서 false
    public Long join(Member member) {
        //상황: 같은 이름 중복 막을 경우
        validateDuplicateMember(member);

        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> members = memberRepository.findByName(member.getName());
        if (!members.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     * @return
     */
//    @Transactional(readOnly = true) //조회하는 곳에서는 JPA가 성능을 최적화
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOnd(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
