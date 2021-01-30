package sayant.springframeworkguru.sfgurubeerstatemachineservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.Payment;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentEvent;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentState;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.repository.PaymentRepository;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.service.PaymentService;

import javax.transaction.Transactional;

/**
 * Created by sayantjm on 24/1/21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTHORIZE);

        return stateMachine;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE);

        return stateMachine;
    }

    @Transactional
    @Override
    @Deprecated
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.AUTH_DECLINED);

        return stateMachine;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {
        Message msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(msg);
    }

    // Recovers the state machine from database
    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
        Payment payment = paymentRepository.getOne(paymentId);

        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });

        stateMachine.start();

        return stateMachine;
    }
}
