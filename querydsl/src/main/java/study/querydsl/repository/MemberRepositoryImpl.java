package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.member.domain.Member;
import study.querydsl.pred.MemberSearchCondition;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.member.domain.QMember.member;
import static study.querydsl.team.domain.QTeam.team;

@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return jpaQueryFactory.select(new QMemberTeamDto(member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        getLoe(condition.getAgeLoe()),
                        getGoe(condition.getAgeGoe()))
                .fetch();
    }

    /**
     * PAGE 사이즈 최적화
     * @param condition
     * @param pageable
     * @return
     */
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = jpaQueryFactory.select(new QMemberTeamDto(member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        getLoe(condition.getAgeLoe()),
                        getGoe(condition.getAgeGoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> contents = results.getResults();
        long totalCnt = results.getTotal();
        return new PageImpl<>(contents, pageable, totalCnt);
    }

    /**
     * count를 직접 가져오는 버전
     * @param condition
     * @param pageable
     * @return
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> results = jpaQueryFactory.select(new QMemberTeamDto(member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        getLoe(condition.getAgeLoe()),
                        getGoe(condition.getAgeGoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = jpaQueryFactory.select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        getLoe(condition.getAgeLoe()),
                        getGoe(condition.getAgeGoe()));

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchCount);
    }

    private BooleanExpression ageBetween(int start, int end){
        return getGoe(start).and(getLoe(end));
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
