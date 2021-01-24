package sayant.springframeworkguru.sfgurubeerstatemachineservice.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentEvent;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.PaymentState;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by sayantjm on 24/1/21
 */
@SpringBootTest
@Slf4j
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.start();

        log.info("Current state:{}", stateMachine.getState().toString());
        stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);

        log.info("Current state:{}", stateMachine.getState().toString());

        // After PRE_AUTH_APPROVED event the state should be PRE_AUTH
        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        log.info("Current state:{}", stateMachine.getState().toString());
        assertTrue(stateMachine.getState().getId().equals(PaymentState.PRE_AUTH));

        // In status PRE_AUTH, after PRE_AUTH_DECLINED event, the state should be PRE_AUTH
        // as there is no transition configured
        stateMachine.sendEvent(PaymentEvent.PRE_AUTH_DECLINED);
        log.info("Current state:{}", stateMachine.getState().toString());
        assertTrue(stateMachine.getState().getId().equals(PaymentState.PRE_AUTH));

    }


}