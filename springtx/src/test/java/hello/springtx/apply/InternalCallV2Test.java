package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    /**
     * internal call을 시작함
     * Transactional 있네
     * proxy 적용
     *
     * external call 실행
     * Transactional 없네
     * internal 실행
     * @Transactional 있는데 왜 안됨?
     * why?
     * client -> external(AOP Proxy) -> but external 에 Transactional 없으므로 없이 실행
     * external -> internal 실행함 여기서 문제 발생
     * 그냥 내가 내 method 실행한거임
     * 즉 target에서 그냥 실제 자신의 method 실행한거
     * 나 자신은 proxy 관리 받지 않는 상태
     * 즉 proxy 적용 안됨
     */

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class {}", callService.getClass());
    }

    @Test
    void internalCall() {
        callService.internal();
    }

    @Test
    void externalCallV2() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig{

        @Bean
        CallService callService(){
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService(){
            return new InternalService();
        }

    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService{

        private final InternalService internalService;

        public void external(){
            log.info("call external");
            printTx();
            internalService.internal();
        }

        @Transactional
        public void internal(){
            log.info("call internal");
            printTx();
        }


        private void printTx(){
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active {}", txActive);
        }
    }

    static class InternalService
    {

        @Transactional
        public void internal(){
            log.info("call internal");
            printTx();
        }
        private void printTx(){
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active {}", txActive);
        }
    }



}
