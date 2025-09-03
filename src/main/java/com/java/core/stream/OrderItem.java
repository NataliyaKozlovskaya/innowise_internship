package com.java.core.stream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItem {

    private String productName;
    private int quantity;
    private double price;
    private Category category;

}
