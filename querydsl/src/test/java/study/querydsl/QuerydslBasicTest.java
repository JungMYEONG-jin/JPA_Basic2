package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.member.domain.Member;
import study.querydsl.member.domain.QMember;
import study.querydsl.team.domain.QTeam;
import study.querydsl.team.domain.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.stringTemplate;
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
        Member mem2 = new Member("mem2", 20, a);
        Member mem3 = new Member("mem3", 30, b);
        Member mem4 = new Member("mem4", 40, b);
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

    @Test
    void join() {
        List<Member> members = queryFactory.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("A"))
                .fetch();
        assertThat(members.size()).isEqualTo(2);
        assertThat(members).extracting("username")
                .containsExactly("mem1", "mem2");
    }


    /**
     * 회원 이름과 팀 이름 같은 회원
     */
    @Test
    void thetaJoin() {
        em.persist((new Member("A", 222)));
        em.persist((new Member("B", 222)));
        em.persist((new Member("C", 222)));

        List<Member> members = queryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        assertThat(members).extracting("username")
                .containsExactly("A", "B");
    }

    @Test
    void joinOnFiltering() {
        List<Tuple> list = queryFactory.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("A"))
                .fetch();
        Tuple tuple = list.get(0);
        for (Tuple tuple1 : list) {
            System.out.println("tuple1 = " + tuple1);
        }
    }

    @Test
    void joinOnNoRelation() {
        em.persist((new Member("A", 222)));
        em.persist((new Member("B", 222)));
        em.persist((new Member("C", 222)));

        List<Tuple> fetch = queryFactory.select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinTest() {
        em.flush();
        em.clear();

        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("mem1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("fetch join not applied").isFalse();
    }

    @Test
    void fetchJoinSuccessTest() {
        em.flush();
        em.clear();

        Member findMember = queryFactory.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("mem1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("fetch join not applied").isTrue();
    }

    @Test
    void subQuery() {
        // sub 쿼리랑 member 중복 안되게 하려고
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub))).fetch();

        assertThat(result).extracting("age").containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회
     */
    @Test
    void subQueryTwo() {
        // sub 쿼리랑 member 중복 안되게원 하려고
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.goe(JPAExpressions
                        .select(memberSub.age.avg())
                        .from(memberSub))).fetch();

        assertThat(result).extracting("age").containsExactly(30, 40);
    }


    /**
     * in
     */
    @Test
    void subQueryThree() {
        // sub 쿼리랑 member 중복 안되게원 하려고
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.in(JPAExpressions
                        .select(memberSub.age)
                        .from(memberSub)
                        .where(memberSub.age.gt(10)))).fetch();

        assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> fetch = queryFactory.select(member.username,
                        JPAExpressions.select(memberSub.age.avg()).from(memberSub))
                .from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void basicCase() {
        List<String> fetch = queryFactory.select(member.age.when(10).then("ten years old")
                        .when(20).then("20th")
                        .otherwise("adult"))
                .from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void complexCase() {
        List<String> result = queryFactory.select(new CaseBuilder().when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void constant() {
        List<Tuple> a = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : a) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void concat() {
        // user_age
        List<String> fetch = queryFactory.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void projectionOne() {
        List<Integer> fetch = queryFactory.select(member.age)
                .from(member)
                .fetch();
        for (Integer integer : fetch) {
            System.out.println("integer = " + integer);
        }
    }

    @Test
    void projectionTwo() {
        List<Tuple> fetch = queryFactory.select(member.age, member.username)
                .from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
            System.out.println(" age = " + tuple.get(member.age));
            System.out.println(" name = " + tuple.get(member.username));
        }
    }

    /**
     * new 연산자 통해 Dto 결과 가져오기
     * 순수 JPA에서 조회는 new 연산이 필요
     * 지저분함
     * 생성자 방식만 지원함
     */
    @Test
    void findDtoByJPQL() {
        List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class).getResultList();
        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl
     * setter
     */
    @Test
    void setterDto(){
        List<MemberDto> fetch = queryFactory.select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl
     * field
     */
    @Test
    void dtoByField(){
        List<MemberDto> fetch = queryFactory.select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl
     * constructor
     */
    @Test
    void dtoByConstructor(){
        List<MemberDto> fetch = queryFactory.select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }


    /**
     * Querydsl
     * field
     */
    @Test
    void dtoByFieldNotMatchedName(){
        // 원래는 칼럼이 다르므로 나이만 값이 제대로 들어가고 이름은 null로 들어감
        QMember qMember = new QMember("qMember");
        List<UserDto> fetch = queryFactory.select(Projections.fields(UserDto.class, member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions.
                                select(qMember.age.max())
                                .from(qMember), "age")))
                .from(member)
                .fetch();
        for (UserDto memberDto : fetch) {
            System.out.println("UserDto = " + memberDto);
        }
    }
    /**
     * Querydsl
     * field
     */
    @Test
    void dtoByConstructorNotMatchedName(){
        // 원래는 칼럼이 다르므로 나이만 값이 제대로 들어가고 이름은 null로 들어감
        QMember qMember = new QMember("qMember");
        List<UserDto> fetch = queryFactory.select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (UserDto memberDto : fetch) {
            System.out.println("UserDto = " + memberDto);
        }
    }

    @Test
    void findDtoByQueryProjection() {
        List<MemberDto> fetch = queryFactory.select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void dynamicQuery_BooleanBuilder(){
        String usernameParam = "mem1";
        Integer ageParam = null;
        List<Member> result = searchMember1(usernameParam, ageParam);
        for (Member member1 : result) {
            System.out.println("mem1 = " + member1);
        }
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (usernameParam!=null)
            booleanBuilder.and(member.username.eq(usernameParam));
        if(ageParam!=null)
            booleanBuilder.and(member.age.eq(ageParam));

        return queryFactory.selectFrom(member)
                .where(booleanBuilder)
                .fetch();
    }

    @Test
    void dynamicQuery_WhereExpression(){
        String usernameParam = "mem1";
        Integer ageParam = 10;
        List<Member> fetch = searchMember2(usernameParam, ageParam);
        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return queryFactory.selectFrom(member)
                .where(allEq(usernameParam, ageParam))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameParam) {
        //null 처리
        return usernameParam!=null?member.username.eq(usernameParam):null;
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam!=null?member.age.eq(ageParam):null;
    }

    private BooleanExpression allEq(String username, Integer age){
        return usernameEq(username).and(ageEq(age));
    }

    /**
     * bulk 연산
     */
    @Test
    void bulkUpdate() {

        // 벌크 연산 조심해야됨
        // 영속성 컨텍스트에는 아직 이전 네임으로 된 상태다

        // 영속성 1, 2, 3, 4
        // db 1, 2, 3, 4

        long cnt = queryFactory.update(member)
                .set(member.username, "비호")
                .where(member.age.lt(21))
                .execute();

        em.flush();
        em.clear();

        // 영속성 1, 2, 3, 4
        // db 비, 비, 3, 4
        // 현재 상태가 안맞는다
        // DB에서 가져온걸 영속성 컨텍스트에 넣어야함.
        // but 영속성 컨텍스트에 있으므로 db에 있는걸 버리고 영속성 씀
        // 그러면 이전 이름이 그대로 나옴 이를 해결하려면 영속성 초기화 해버리면된다.
        // 벌크후 영속성컨텍스트 초기화 해주자~
        List<Member> fetch = queryFactory.selectFrom(member).fetch();
        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }

    @Test
    void bulkAdd() {
        long execute = queryFactory
                .update(member)
                .set(member.age, member.age.multiply(2))
                .execute();
    }

    @Test
    void bulkDelete() {
        long execute = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }

    @Test
    void sqlFunction() {
        List<String> fetch = queryFactory
                .select(stringTemplate("function('replace', {0}, {1}, {2})",
                        member.username, "mem", "M"))
                .from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void sqlFunction2() {
        // stringTemplate("function('lower', {0})", member.username))
        List<String> fetch = queryFactory.select(member.username)
                .from(member)
                .where(member.username.eq(member.username.lower()))
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }
}
