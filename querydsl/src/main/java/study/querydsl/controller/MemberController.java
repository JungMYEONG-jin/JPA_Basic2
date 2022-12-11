package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.pred.MemberSearchCondition;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchV1(@Param("condition") MemberSearchCondition condition){
        return memberJpaRepository.searchByWhere(condition);
    }
}
