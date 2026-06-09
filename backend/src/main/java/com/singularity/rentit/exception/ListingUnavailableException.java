package com.singularity.rentit.exception;

public class ListingUnavailableException extends RuntimeException {
    public ListingUnavailableException() {
        super("listing.unavailable");
    }
}
