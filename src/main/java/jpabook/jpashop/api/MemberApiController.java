package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class MemberApiController {
    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect, collect.size());
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
        private int count;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        System.out.println("MemberApiController.saveMemberV1");
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //PUT 수정
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@RequestBody @Valid UpdateMemberRequest request, @PathVariable Long id) {
        //커맨드와 쿼리를 분리한다 - 김영한님은
        memberService.update(id, request.getName());
        Member member = memberService.findOne(id);
        return new UpdateMemberResponse(member.getName());
    }

    @Data
    static class UpdateMemberResponse {
        private String name;

        public UpdateMemberResponse(String name) {
            this.name = name;
        }
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberRequest {

        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
