package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection // dto를 qfile로 생성
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}