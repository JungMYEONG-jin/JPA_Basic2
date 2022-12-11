package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import study.querydsl.member.domain.Member;
import study.querydsl.team.domain.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitService initService;

    @PostConstruct
    public void init(){
        initService.init();
    }


    @Component
    static class InitService {
        @PersistenceContext private EntityManager em;


        @Transactional
        public void init() {
            Team a = new Team("A");
            Team b = new Team("B");
            em.persist(a);
            em.persist(b);

            for(int i=0;i<100;i++){
                Team select = i%2==0?a:b;
                em.persist(new Member("mem"+i,i,select));
            }
        }
    }

}
