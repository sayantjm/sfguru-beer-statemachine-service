package sayant.springframeworkguru.sfgurubeerstatemachineservice.service;

import org.springframework.statemachine.StateMachine;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.Payment;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentEvent;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentState;

/**
 * Created by sayantjm on 24/1/21
 */
public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declinePayment(Long paymentId);
}
