/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.ordersservice.query;

import com.malyskok.ordersservice.core.data.OrderEntity;
import com.malyskok.ordersservice.core.data.OrdersRepository;
import com.malyskok.ordersservice.core.model.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary handle(FindOrderQuery query){
        OrderEntity order = ordersRepository.findByOrderId(query.getOrderId());
        return new OrderSummary(order.getOrderId(), order.getOrderStatus(), "");
    }
}