package io.extact.msa.spring.rms.application.universal;

import io.extact.msa.spring.platform.fw.exception.RentalReservationServiceException;

public class LoginFailedException extends RentalReservationServiceException {

    public LoginFailedException(String message) {
        super(message);
    }
}
