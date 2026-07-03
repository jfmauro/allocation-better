package com.pipelinepro.domain.port.in;

import com.pipelinepro.domain.port.in.command.MatchPaymentCommand;
import com.pipelinepro.domain.port.in.result.MatchPaymentResult;

public interface MatchPaymentUseCase {
    MatchPaymentResult matchPayment(MatchPaymentCommand command);
}
