package study.querydsl.member.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.team.domain.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional
@SpringBootTest
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test
    public void testEntity(){
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

        //초기화
        em.flush(); // 영속성 query db에 다 날림
        em.clear();

        List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();
        for (Member member : result) {
            System.out.println("member = " + member);
        }

    }

}