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
import com.malyskok.ordersservice.core.event.OrderCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventsHandler {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreateEvent event){
        log.info(String.format("""
                EventHandler - Handle ProductReservedEvent
                orderId: %s
                productId: %s
                """, event.getOrderId(), event.getProductId()));

        OrderEntity entity = new OrderEntity();
        BeanUtils.copyProperties(event, entity);
        ordersRepository.save(entity);
    }
}