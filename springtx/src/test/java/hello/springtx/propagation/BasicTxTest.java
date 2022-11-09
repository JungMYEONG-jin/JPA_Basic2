package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@SpringBootTest
@Slf4j
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config{
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource){
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("Transaction start");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("commit start");
        txManager.commit(status);
        log.info("commit finished");
    }

    @Test
    void double_commit() {
        log.info("Transaction 1 start");
        TransactionStatus status1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("commit 1 start");
        txManager.commit(status1);
        log.info("commit 1 finished");

        log.info("Transaction 2 start");
        TransactionStatus status2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("commit 2 start");
        txManager.commit(status2);
        log.info("commit 2 finished");
    }

    @Test
    void rollback() {
        log.info("Transaction start");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("rollback start");
        txManager.rollback(status);
        log.info("rollback finished");
    }

    @Test
    void double_commit_rollback() {
        log.info("Transaction 1 start");
        TransactionStatus status1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("commit 1 start");
        txManager.commit(status1);
        log.info("commit 1 finished");

        log.info("Transaction 2 start");
        TransactionStatus status2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("rollback 2 start");
        txManager.rollback(status2);
    }

    @Test
    void innerTest() {

        log.info("External Transaction 1 start");
        TransactionStatus external = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("External.isNew()={}", external.isNewTransaction());

        log.info("Inner Transaction start");
        TransactionStatus internal = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNew()={}", internal.isNewTransaction());
        log.info("inner commit");
        txManager.commit(internal);

        log.info("external commit");
        txManager.commit(external);

    }

    @Test
    void externalRollback() {
        log.info("External Transaction 1 start");
        TransactionStatus external = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("Inner Transaction start");
        TransactionStatus internal = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("internal commit");
        txManager.commit(internal);
        log.info("external rollback");
        txManager.rollback(external);
    }

    @Test
    void inner_rollback() {
        log.info("External Transaction 1 start");
        TransactionStatus external = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("Inner Transaction start");
        TransactionStatus internal = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("internal rollback");
        txManager.rollback(internal); // rollback-only 표시

        log.info("external commit");
        Assertions.assertThatThrownBy(()-> txManager.commit(external)).isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("External Transaction 1 start");
        TransactionStatus external = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("external.isNew()={}");

        log.info("Inner Transaction start");
        DefaultTransactionAttribute def = new DefaultTransactionAttribute();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(def);
        log.info("inner.isNew={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 콜백");
        txManager.rollback(inner);

    }


}
