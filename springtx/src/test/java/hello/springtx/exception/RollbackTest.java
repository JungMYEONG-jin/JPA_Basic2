package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;


/**
 * runtime 복구가 안됨 즉 rollback 해야함
 * check 복구 가능 commit
 * 비즈니스 상황에서 발생한 에러는 check로 하고 복구 하는 방향으로 하면 될듯?
 */

@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService rollbackService;


    @Test
    void runtimeTest() {
        Assertions.assertThatThrownBy(()->rollbackService.runtimeException()).isInstanceOf(RuntimeException.class);
    }


    @Test
    void checkTest() throws MyException {
        Assertions.assertThatThrownBy(()->rollbackService.checkException()).isInstanceOf(MyException.class);
    }

    @Test
    void rollbackForTest() throws MyException {
        Assertions.assertThatThrownBy(()->rollbackService.rollbackFor()).isInstanceOf(MyException.class);
    }

    @TestConfiguration
    static class RollbackConfig{
        @Bean
        RollbackService rollbackService(){
            return new RollbackService();
        }
    }

    @Slf4j
    static class RollbackService{
        //runtime exception rollback
        @Transactional
        public void runtimeException(){
            log.info("call runtimeException");
            throw new RuntimeException();
        }
        //check exception commit
        @Transactional
        public void checkException() throws MyException {
            log.info("checkException");
            throw new MyException();
        }

        //check exception rollbackfor
        @Transactional(rollbackFor = MyException.class)
        public void rollbackFor() throws MyException {
            log.info("checkException");
            throw new MyException();
        }
    }

    static class MyException extends Exception{

    }

}
