package com.pipelinepro.application;

import com.pipelinepro.domain.Payment;
import com.pipelinepro.application.port.out.PaymentIntakeTransactionalWorker;
import com.pipelinepro.domain.port.in.ReceivePaymentUseCase;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PaymentIntakeApplicationService implements ReceivePaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentIntakeApplicationService.class);

    private final PaymentIntakeTransactionalWorker paymentIntakeTransactionalWorker;

    public PaymentIntakeApplicationService(
            PaymentIntakeTransactionalWorker paymentIntakeTransactionalWorker) {
        this.paymentIntakeTransactionalWorker = Objects.requireNonNull(
                paymentIntakeTransactionalWorker, "paymentIntakeTransactionalWorker");
    }

    @Override
    public Payment receivePayment(ReceivePaymentCommand command) {
        log.info("+++start receivePayment+++");
        try {
            return paymentIntakeTransactionalWorker.receivePayment(command);
        } finally {
            log.info("+++end receivePayment+++");
        }
    }
}
