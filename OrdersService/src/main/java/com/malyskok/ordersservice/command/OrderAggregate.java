/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.ordersservice.command;

import com.malyskok.ordersservice.command.commands.ApproveOrderCommand;
import com.malyskok.ordersservice.command.commands.CreateOrderCommand;
import com.malyskok.ordersservice.command.commands.RejectOrderCommand;
import com.malyskok.ordersservice.core.event.OrderApprovedEvent;
import com.malyskok.ordersservice.core.event.OrderCreatedEvent;
import com.malyskok.ordersservice.core.event.OrderRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Slf4j
@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;

    public OrderAggregate() {
    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command){
        log.info(String.format("""
                CommandHandler - handle CreateOrderCommand
                orderId: %s
                productId: %s
                """, command.getOrderId(), command.getProductId()));
        OrderCreatedEvent event = new OrderCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event){
        log.info(String.format("""
                EventSourcingHandler - handle OrderCreatedEvent
                orderId: %s
                productId: %s
                """, event.getOrderId(), event.getProductId()));
        this.orderId = event.getOrderId();
        this.productId = event.getProductId();
        this.userId = event.getUserId();
        this.quantity = event.getQuantity();
        this.addressId = event.getAddressId();
        this.orderStatus = event.getOrderStatus();
    }

    @CommandHandler
    public void handle(ApproveOrderCommand command){
        log.info(String.format("""
                CommandHandler - handle ApproveOrderCommand
                orderId: %s
                """, command.getOrderId()));
        OrderApprovedEvent approvedEvent = new OrderApprovedEvent(command.getOrderId());
        AggregateLifecycle.apply(approvedEvent);
    }

    @EventSourcingHandler
    public void on(OrderApprovedEvent event){
        log.info(String.format("""
                EventSourcingHandler - handle OrderApprovedEvent
                orderId: %s
                """, event.getOrderId()));
        this.orderStatus = event.getOrderStatus();
    }

    @CommandHandler
    public void handle(RejectOrderCommand command){
        log.info(String.format("""
                CommandHandler - handle RejectOrderCommand
                orderId: %s
                """, command.getOrderId()));
        OrderRejectedEvent event = new OrderRejectedEvent(
                command.getOrderId(), command.getReason());
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(OrderRejectedEvent event){
        log.info(String.format("""
                EventSourcingHandler - handle OrderRejectedEvent
                orderId: %s
                """, event.getOrderId()));
        this.orderStatus = event.getOrderStatus();
    }
}