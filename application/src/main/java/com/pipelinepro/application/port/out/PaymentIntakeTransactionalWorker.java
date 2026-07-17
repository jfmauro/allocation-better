package com.pipelinepro.application.port.out;

import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;

public interface PaymentIntakeTransactionalWorker {
    Payment receivePayment(ReceivePaymentCommand command);
}
