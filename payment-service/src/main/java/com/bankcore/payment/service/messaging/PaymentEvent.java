package com.bankcore.payment.service.messaging;

import java.io.Serializable;

public class PaymentEvent implements Serializable {
    private String requestId;
    private String instructionId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }
}
