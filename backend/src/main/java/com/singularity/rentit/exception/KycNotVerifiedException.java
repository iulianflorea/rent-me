package com.singularity.rentit.exception;

public class KycNotVerifiedException extends RuntimeException {
    public KycNotVerifiedException() {
        super("kyc.required");
    }
}
