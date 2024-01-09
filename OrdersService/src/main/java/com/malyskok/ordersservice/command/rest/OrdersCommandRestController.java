/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.ordersservice.command.rest;

import com.malyskok.ordersservice.command.CreateOrderCommand;
import com.malyskok.ordersservice.command.OrderStatus;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandRestController {

    private static final String USER_ID = "27b95829-4f3f-4ddf-8983-151ba010e35b";

    private final CommandGateway commandGateway;

    @Autowired
    public OrdersCommandRestController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createOrder(@Valid @RequestBody CreateOrderRestModel model){
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .productId(model.getProductId())
                .quantity(model.getQuantity())
                .addressId(model.getAddressId())
                .orderStatus(OrderStatus.CREATED)
                .userId(USER_ID)
                .orderId(UUID.randomUUID().toString())
                .build();

        return commandGateway.sendAndWait(createOrderCommand);
    }
}