package com.abhi.model;

import lombok.Data;

@Data
public class Order {
    private final long orderId;
    private final Status status;
    private final double amount;
}
