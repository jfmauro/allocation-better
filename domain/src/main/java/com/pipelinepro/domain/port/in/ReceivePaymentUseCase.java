package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.Payment;
import com.pipelinepro.domain.port.in.command.ReceivePaymentCommand;

public interface ReceivePaymentUseCase {
    Payment receivePayment(ReceivePaymentCommand command);
}
