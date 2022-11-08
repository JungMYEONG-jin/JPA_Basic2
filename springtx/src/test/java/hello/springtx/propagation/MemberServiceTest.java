package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * memberService @Transactional off
     * memberRepository @Transactional on
     * logRepository @Transactional on
     */
    @Test
    void outerTxOffSuccess() {
        String username = "outerTxOff_success";

        memberService.joinV1(username);

        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }
    /**
     * memberService @Transactional off
     * memberRepository @Transactional on
     * logRepository @Transactional on
     */
    @Test
    void outerTxOffFail() {
        String username = "로그예외_outerTxOff_success";
        assertThatThrownBy(()->memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }


    /**
     * memberService @Transactional on
     * memberRepository @Transactional off
     * logRepository @Transactional off
     */
    @Test
    void joinV1() {
        String username = "outerTxOff_success";

        memberService.joinV1(username);

        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transactional on
     * memberRepository @Transactional on
     * logRepository @Transactional on
     */
    @Test
    void outerTxOnSuccess() {
        String username = "outerTxOn_success";

        memberService.joinV1(username);

        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }
}
