package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.member.domain.Member;
import study.querydsl.member.domain.QMember;
import study.querydsl.pred.MemberSearchCondition;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.member.domain.QMember.*;
import static study.querydsl.team.domain.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect(){
        return select(member).from(member).fetch();
    }

    public List<Member> basicSelectFrom(){
        return selectFrom(member).fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable){
        JPAQuery<Member> query = selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        getGoe(condition.getAgeGoe()),
                        getLoe(condition.getAgeLoe()));
        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable){
        return applyPagination(pageable, query -> query.
                selectFrom(member)
                .leftJoin(member.team, team).
                where(usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                getGoe(condition.getAgeGoe()),
                getLoe(condition.getAgeLoe())));
    }

    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable){
        return applyPagination(pageable, query -> query.
                selectFrom(member)
                .leftJoin(member.team, team).
                where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        getGoe(condition.getAgeGoe()),
                        getLoe(condition.getAgeLoe())), countQuery -> countQuery
                        .select(member.id)
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                getGoe(condition.getAgeGoe()),
                                getLoe(condition.getAgeLoe())));
    }


    private BooleanExpression getGoe(Integer age) {
        return age!=null ? member.age.goe(age) : null;
    }

    private BooleanExpression getLoe(Integer age) {
        return age!=null ? member.age.goe(age) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName)?team.name.eq(teamName):null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username)?member.username.eq(username):null;
    }
}
