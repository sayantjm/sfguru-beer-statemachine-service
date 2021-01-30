package sayant.springframeworkguru.sfgurubeerstatemachineservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.Payment;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentEvent;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentState;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.repository.PaymentRepository;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.service.PaymentService;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by sayantjm on 30/1/21
 */
@SpringBootTest
@Slf4j
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        this.payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Test
    @Transactional
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuth(savedPayment.getId());

        Payment preAthedPayment = paymentRepository.getOne(savedPayment.getId());
        log.info(preAthedPayment.toString());

        assertTrue((preAthedPayment.getState().equals(PaymentState.PRE_AUTH)) || (preAthedPayment.getState().equals(PaymentState.PRE_AUTH_ERROR)));
    }

    @RepeatedTest(10)
    @Transactional
    void auth() {
        StateMachine<PaymentState, PaymentEvent> authSM = null;
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuthSM = paymentService.preAuth(savedPayment.getId());

        if (preAuthSM.getState().getId() == PaymentState.PRE_AUTH) {
            log.info("Payment is PRE Authorized.");
            authSM = paymentService.authorizePayment(savedPayment.getId());
            log.info("Result of Auth:{}", authSM.getState().getId());
            assertTrue((authSM.getState().getId().equals(PaymentState.AUTH)) || (authSM.getState().getId().equals(PaymentState.AUTH_ERROR)));
        } else {
            log.info("Payment failed pre-auth...");
        }
        assertTrue((preAuthSM.getState().getId().equals(PaymentState.PRE_AUTH)) || (preAuthSM.getState().getId().equals(PaymentState.PRE_AUTH_ERROR)));
    }
}