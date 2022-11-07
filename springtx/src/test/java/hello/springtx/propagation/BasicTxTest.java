package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
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
    void rollback() {
        log.info("Transaction start");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("rollback start");
        txManager.rollback(status);
        log.info("rollback finished");
    }
}
