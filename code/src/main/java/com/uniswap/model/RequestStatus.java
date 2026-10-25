package com.uniswap.model;

/** Lifecycle state of a request sent by one student to another. */
public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    COMPLETED
}
