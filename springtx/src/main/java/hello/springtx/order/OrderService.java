package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order call");
        orderRepository.save(order);

        log.info("pay process...");
        if (order.getUsername().equals("예외")){
            log.info("system exception occur");
            throw new RuntimeException("system error");
        }else if (order.getUsername().equals("잔고부족")){
            log.info("not enough exception occur");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고가 부족");
        }else{
            log.info("normal accepted");
            order.setPayStatus("완료");
        }
        log.info("pay process finished");
    }
}
