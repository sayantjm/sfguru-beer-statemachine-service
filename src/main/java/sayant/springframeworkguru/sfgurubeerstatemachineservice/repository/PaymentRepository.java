package sayant.springframeworkguru.sfgurubeerstatemachineservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sayant.springframeworkguru.sfgurubeerstatemachineservice.domain.Payment;

/**
 * Created by sayantjm on 24/1/21
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
