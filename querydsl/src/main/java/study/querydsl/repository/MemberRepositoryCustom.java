package study.querydsl.repository;

import study.querydsl.dto.MemberTeamDto;
import study.querydsl.pred.MemberSearchCondition;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
