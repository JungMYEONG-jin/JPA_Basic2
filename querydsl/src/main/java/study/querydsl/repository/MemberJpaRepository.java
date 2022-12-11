package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.member.domain.Member;
import study.querydsl.member.domain.QMember;
import study.querydsl.pred.MemberSearchCondition;
import study.querydsl.team.domain.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.member.domain.QMember.*;
import static study.querydsl.team.domain.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll(){
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByUsername(String username){
        return queryFactory.selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            booleanBuilder.and((member.username.eq(condition.getUsername())));
        }
        if (hasText(condition.getTeamName())) {
            booleanBuilder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            booleanBuilder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            booleanBuilder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory.select(new QMemberTeamDto(member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(booleanBuilder)
                .fetch();
    }
    
    public List<MemberTeamDto> searchByWhere(MemberSearchCondition condition){
        
        return queryFactory.select(new QMemberTeamDto(member.id.as("memberId"),
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
