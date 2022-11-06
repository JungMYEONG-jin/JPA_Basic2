package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

@SpringBootTest
public class InitTest {


    @Autowired
    Hello hello;

    @Test
    void postTest() {
        // init code
    }

    @TestConfiguration
    static class InitTestConfig{

        @Bean
        Hello hello(){
            return new Hello();
        }
    }

    @Slf4j
    static class Hello{

        /**
         * false로 나옴 왜?
         * 초기화 코드가 먼저 호출되고 그다음 Transactional AOP가 적용됨
         * 따라서 false
         */
        @PostConstruct
        @Transactional
        public void initV1(){
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello @Postconstruct is active {}", isActive);
        }

        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2(){
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello @EventListener is active {}", isActive);
        }
    }
}
