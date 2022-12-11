package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    public void before(){
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
    }

    @Test
    void basicTest(){
        List<Member> mem11 = memberRepository.findByUsername("mem1");
        Member member = mem11.get(0);
        assertThat(mem11).containsExactly(member);
    }


    @Test
    void searchWhereTest() {

        MemberSearchCondition condition = new MemberSearchCondition();

        condition.setTeamName("B");

        List<MemberTeamDto> memberTeamDtos = memberRepository.search(condition);
        assertThat(memberTeamDtos).extracting("username").containsExactly("mem3", "mem4");
    }


}