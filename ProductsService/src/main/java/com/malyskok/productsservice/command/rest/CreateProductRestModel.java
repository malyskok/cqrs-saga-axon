/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.malyskok.productsservice.command.rest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRestModel {

    @NotBlank(message = "Product title must not be blank")
    private String title;

    @NotNull
    @Min(value = 1, message = "Price cannot be lower than 1")
    private BigDecimal price;

    @NotNull
    @Min(value = 1, message = "Quantity cannot be lower than 1")
    @Max(value = 5, message = "Quantity cannot be larger than 5")
    private Integer quantity;
}