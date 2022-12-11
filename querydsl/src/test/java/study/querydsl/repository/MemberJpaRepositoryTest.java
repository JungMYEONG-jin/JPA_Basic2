package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.member.domain.Member;
import study.querydsl.pred.MemberSearchCondition;
import study.querydsl.team.domain.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberJpaRepository memberJpaRepository;


    @Test
    void basicTEst() {
        Member mem1 = new Member("mem1", 10);
        memberJpaRepository.save(mem1);

        Member findMember = memberJpaRepository.findById(mem1.getId()).get();
        assertThat(findMember).isEqualTo(mem1);

        List<Member> all = memberJpaRepository.findAll();
        assertThat(all).containsExactly(mem1);

        List<Member> mem11 = memberJpaRepository.findByUsername("mem1");
        assertThat(mem11).containsExactly(mem1);
    }

    @Test
    void searchTest() {
        Team a = new Team("A");
        Team b = new Team("B");
        em.persist(a);
        em.persist(b);

        Member mem1 = new Member("mem1", 10, a);
        Member mem2 = new Member("mem2", 20, a);
        Member mem3 = new Member("mem3", 30, b);
        Member mem4 = new Member("mem4", 40, b);
        em.persist(mem1);
        em.persist(mem2);
        em.persist(mem3);
        em.persist(mem4);

        MemberSearchCondition condition = new MemberSearchCondition();

        condition.setTeamName("B");

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(condition);
        assertThat(memberTeamDtos).extracting("username").containsExactly("mem3", "mem4");
    }

    @Test
    void searchWhereTest() {
        Team a = new Team("A");
        Team b = new Team("B");
        em.persist(a);
        em.persist(b);

        Member mem1 = new Member("mem1", 10, a);
        Member mem2 = new Member("mem2", 20, a);
        Member mem3 = new Member("mem3", 30, b);
        Member mem4 = new Member("mem4", 40, b);
        em.persist(mem1);
        em.persist(mem2);
        em.persist(mem3);
        em.persist(mem4);

        MemberSearchCondition condition = new MemberSearchCondition();

        condition.setTeamName("B");

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByWhere(condition);
        assertThat(memberTeamDtos).extracting("username").containsExactly("mem3", "mem4");
    }
}