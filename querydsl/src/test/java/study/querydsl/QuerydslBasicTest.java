package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.member.domain.Member;
import study.querydsl.team.domain.Team;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.member.domain.QMember.member;

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
}
