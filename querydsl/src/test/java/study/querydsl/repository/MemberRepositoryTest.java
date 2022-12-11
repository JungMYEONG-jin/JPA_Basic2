package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.member.domain.Member;
import study.querydsl.member.domain.QMember;
import study.querydsl.pred.MemberSearchCondition;
import study.querydsl.team.domain.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchNullPointerException;
import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.member.domain.QMember.member;

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

    @Test
    void pageTest() {
        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(1, 3);
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("mem4");
    }

    /**
     * QuerydslPredicateExecutor
     */
    @Test
    void queryDSLPredicateExecutorTest() {
        Iterable<Member> all = memberRepository.findAll(member.age.between(20, 40).and(member.username.like("%mem%")));
        for (Member member1 : all) {
            System.out.println("member1 = " + member1);
        }
    }






}