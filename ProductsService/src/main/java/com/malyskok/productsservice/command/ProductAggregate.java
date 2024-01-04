package com.malyskok.productsservice.command;

import java.math.BigDecimal;

import com.malyskok.productsservice.core.event.ProductCreateEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import static org.apache.commons.lang.StringUtils.isBlank;

@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand) {
        // Validate Create Product Command

        if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price cannot be less or equal than 0");
        }

        if (isBlank(createProductCommand.getTitle())) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        ProductCreateEvent productCreateEvent = new ProductCreateEvent();
        BeanUtils.copyProperties(createProductCommand, productCreateEvent);
        AggregateLifecycle.apply(productCreateEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreateEvent productCreateEvent){
        this.productId = productCreateEvent.getProductId();
        this.price = productCreateEvent.getPrice();
        this.quantity = productCreateEvent.getQuantity();
        this.title = productCreateEvent.getTitle();
    }
}