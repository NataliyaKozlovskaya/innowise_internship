package com.java.core.stream;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
