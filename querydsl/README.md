#### JPA 중요
객체와 관계형 데이터베이스 테이블이 어떻게 매핑되는지는 이해하는게 가장 중요.
JPA의 목적은 객체 지향 프로그래밍과 DB 사이의 패러다임 불일치를 해결하는것임.
실제로 Hibernate 문서에서도 절반 정도가 객체와 테이블의 매핑임.

연관 관계를 매핑할 때 생각해야 할 것은 크케 3가지 존재.
- 방향 : 단방향, 양방향
- 연관 관계의 주인: 양방향일때 관리 주체는 누구인가?
- 다중성: 다대일, 일대다, 일대일, 다대다

1. 단방향 양방향
DB 에서는 외래키 하나로 양 테이블 조인이 가능하다. 따라서 DB에서는 양방향 단방향 나눌 필요가 없다.
그러나 객체는 참조용 필드가 있는 객체만 다른 객체 참조가 가능하다.
그렇기 때문에 하나의 객체만 참조용 필드를 갖으면 단방향, 양쪽이면 양방향이라고 합니다.
엄밀하게 말하면 그냥 두 객체가 서로 단방향 참조를 가져 양방향이 되는것임.
비즈니스 로직에 따라 두 객체가 참조가 필요한지 생각해서 결정하면 됩니다.
2. 연관관계의 주인
두 객체가 양방향 관계일때 연관관계의 주인을 지정해야 합니다.
주인 지정은두 관계에서 제어의 권한(외래키를 비롯한 테이블 레코드 저장, 수정, 삭제)을 갖는
실질적인 관계가 누구인지 알려주는것임. 연관관계의 주인이 아니라면 조회만 가능하고
주인이라면 조회, 저장, 수정, 삭제가 가능함. 주인이 아닌 곳에 mappedBy를 쓴다!! 주인은 JoinColumn
즉 외래키가 있는곳을 주인으로 하면됨.
user가 team_id 외래키를 가지니까 user가 주인이 되는것임.
> 그런데 굳이 왜 지정을 해야하는지?

두 연관 관계의 입장을 명확하게 정해서 관리를 용이하게 하려고 주인을 정해야함.
예를 들어 post, board가 있는데 board를 수정할때 fk를 수정할지
board에서 post를 수정할때 fk를 수정할지 결정해야 하기 때문.

>주인만 제어하면 되나?

사실 둘 다 변경해줘서 동기화 해주는게 좋긴함.

3. 다중성
다대일 단방향
```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
}
```
다대일 양방향 일쪽에 @OneToMany를 갖는다.
```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToMany(mappedBy = "team")
    List<Member> members = new ArrayList<>();
}
```

일대다
일쪽에 주인을 둔것. 참고로 실무에서는 일대다 단방향은 쓰지 않도록 하자.
DB 입장에서는 무조건 다쪽에서 왜래키를 관리한다.
하지만 이건 일쪽에서 다쪽 객체를 조작하는 방법임.
```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private int age;
}

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToMany
    @JoinColumn(name="member_id")
    List<Member> members = new ArrayList<>();
}
```
결과적으로 일대다(1:N) 단방향, 양방향은 쓰지 말고 차라리 다대일(N:1) 양방향으로 쓰는 것이 맞다라고 단순화하여 결론 내리면 될 것 같습니다.

다대다(N:N)는 실무 사용 금지 ❌

중간 테이블이 숨겨져 있기 때문에 자기도 모르는 복잡한 조인의 쿼리(Query)가 발생하는 경우가 생길 수 있기 때문입니다.
다대다로 자동생성된 중간테이블은 두 객체의 테이블의 외래 키만 저장되기 때문에 문제가 될 확률이 높습니다. 
JPA를 해보면 중간 테이블에 외래 키 외에 다른 정보가 들어가는 경우가 많기 때문에 
다대다를 일대다, 다대일로 풀어서 만드는 것(중간 테이블을 Entity로 만드는 것)이 추후 변경에도 유연하게 대처할 수 있습니다.