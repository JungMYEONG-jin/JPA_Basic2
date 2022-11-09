package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username){
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("memberRepository call start");
        memberRepository.save(member);
        log.info("memberRepository call end");

        log.info("logRepository call start");
        logRepository.save(logMessage);
        log.info("logRepository call end");
    }

    public void joinV2(String username){
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("memberRepository call start");
        memberRepository.save(member);
        log.info("memberRepository call end");

        log.info("logRepository call start");
        logRepository.save(logMessage);
        log.info("logRepository call end");
    }
}
