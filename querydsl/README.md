#### from절의 서브쿼리 한계
JPA, JPAQL 서브쿼리의 한계점으로 from절의 서브쿼리(인라인뷰)는 지원하지 않는다.
당연히 Querydsl도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select절의
서브쿼리는 지원한다. Querydsl도 하이버네이트 구현체를 사용하면 select절의 서브쿼리를 지원한다.

>해결방안은?

1. 서브쿼리를 join으로 변경한다.
2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
3. nativeSQL을 사용한다.


#### 프로젝션이란?
select에서 뽑을 칼럼을 지정하는것임.
대상이 1개면 해당 타입으로 결과 반환 아니라면 튜플이나 DTO로 조회

#### DTO 프로젝션중 칼럼명이 다르면? 
원래는 칼럼이 다르므로 나이만 값이 제대로 들어가고 이름은 null로 들어감
```java
@Test
    void dtoByFieldNotMatchedName(){
        // 원래는 칼럼이 다르므로 나이만 값이 제대로 들어가고 이름은 null로 들어감 
        List<UserDto> fetch = queryFactory.select(Projections.fields(UserDto.class, member.username, member.age))
                .from(member)
                .fetch();
        for (UserDto memberDto : fetch) {
            System.out.println("UserDto = " + memberDto);
        }
    }
```

```shell
UserDto = UserDto(name=null, age=10)
UserDto = UserDto(name=null, age=20)
UserDto = UserDto(name=null, age=30)
UserDto = UserDto(name=null, age=40)
```
이럴때 as를 해서 이름을 맞추면 제대로 들어온다.
```java
@Test
    void dtoByFieldNotMatchedName(){
        // 원래는 칼럼이 다르므로 나이만 값이 제대로 들어가고 이름은 null로 들어감
        List<UserDto> fetch = queryFactory.select(Projections.fields(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();
        for (UserDto memberDto : fetch) {
            System.out.println("UserDto = " + memberDto);
        }
    }
```
```shell
UserDto = UserDto(name=mem1, age=10)
UserDto = UserDto(name=mem2, age=20)
UserDto = UserDto(name=mem3, age=30)
UserDto = UserDto(name=mem4, age=40)
```
또는 ExpressionUtils.as(value, name) 해도 된다~.

