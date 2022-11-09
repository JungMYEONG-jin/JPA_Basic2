package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepository {

    private final EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 별도의 트랜잭션을 사용하는 곳이  되는거임. 즉 다른곳에 영향을 주지 않음.
    public void save(Log logMessage){
        log.info("save log");
        em.persist(logMessage);

        if (logMessage.getMessage().contains("로그예외")){
            log.info("log exception occurred");
            throw new RuntimeException("예외 발생");
        }
    }

    public Optional<Log> find(String message){
        return em.createQuery("select l from Log l where l.message = :message", Log.class)
                .setParameter("message", message)
                .getResultList().stream().findAny();
    }
}
