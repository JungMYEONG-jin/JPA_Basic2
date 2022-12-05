package study.querydsl;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.member.domain.Member;
import study.querydsl.member.domain.QMember;
import study.querydsl.team.domain.QTeam;
import study.querydsl.team.domain.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static study.querydsl.member.domain.QMember.member;
import static study.querydsl.team.domain.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team a = new Team("A");
        Team b = new Team("B");
        em.persist(a);
        em.persist(b);

        Member mem1 = new Member("mem1", 10, a);
        Member mem2 = new Member("mem20", 20, a);
        Member mem3 = new Member("mem30", 30, b);
        Member mem4 = new Member("mem40", 40, b);
        em.persist(mem1);
        em.persist(mem2);
        em.persist(mem3);
        em.persist(mem4);
    }
    @Test
    public void startJPQL(){
        String query = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(query, Member.class).setParameter("username", "mem1").getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("mem1");
    }

    @Test
    void startQuerydsl() {
        Member findMember = queryFactory.selectFrom(member).where(member.username.eq("mem1")).fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("mem1");
    }

    @Test
    void search() {
        List<Member> memberJPAQuery = queryFactory.selectFrom(member)
                .where(member.age.goe(30)).fetch();
        for (Member member1 : memberJPAQuery) {
            System.out.println("member1 = " + member1);
        }
        assertThat(memberJPAQuery.size()).isEqualTo(2);
    }

    @Test
    void searchAndParam() {
        List<Member> memberJPAQuery = queryFactory.selectFrom(member)
                .where(member.age.goe(30).and(member.username.startsWith("mem"))).fetch();
        for (Member member1 : memberJPAQuery) {
            System.out.println("member1 = " + member1);
        }
        assertThat(memberJPAQuery.size()).isEqualTo(2);
    }

    @Test
    void resultFetchTest() {
        List<Member> fetch = queryFactory.selectFrom(member).fetch();
        assertThat(fetch.size()).isEqualTo(4);
        Member findMember = queryFactory.selectFrom(QMember.member).fetchFirst();
        assertThatThrownBy(()->queryFactory.selectFrom(member).fetchOne()).isInstanceOf(NonUniqueResultException.class);

        long total = queryFactory.selectFrom(member).fetchCount();
    }

    @Test
    void sort() {
        em.persist((new Member(null, 222)));
        em.persist((new Member("mem5", 222)));
        em.persist((new Member("mem6", 222)));

        List<Member> result = queryFactory.selectFrom(member).where(member.age.eq(222))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("mem5");
        assertThat(member6.getUsername()).isEqualTo("mem6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging() {
        QueryResults<Member> memberQueryResults = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(memberQueryResults.getTotal()).isEqualTo(4);
        assertThat(memberQueryResults.getLimit()).isEqualTo(2);
        assertThat(memberQueryResults.getOffset()).isEqualTo(1);
        assertThat(memberQueryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    void aggregation() {
        List<Tuple> fetch = queryFactory.select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        // tuple은 querydsl의 tuple

        Tuple tuple = fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
    }

    @Test
    void group() throws Exception {
        List<Tuple> fetch = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("A");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("B");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }
}
