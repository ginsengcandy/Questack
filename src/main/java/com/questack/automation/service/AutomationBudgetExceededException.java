package com.questack.automation.service;

public class AutomationBudgetExceededException extends RuntimeException {

    public AutomationBudgetExceededException(String message) {
        super(message);
    }
}
