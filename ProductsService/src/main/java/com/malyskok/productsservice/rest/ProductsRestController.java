package com.malyskok.productsservice.rest;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malyskok.productsservice.command.CreateProductCommand;

@RestController
@RequestMapping("/products")
public class ProductsRestController {
    private final Environment env;

    private final CommandGateway commandGateway;

    @Autowired
    public ProductsRestController(Environment env, CommandGateway commandGateway) {
        this.env = env;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createProduct(@RequestBody CreateProductRestModel model) {

        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .price(model.price())
                .quantity(model.quantity())
                .title(model.title())
                .productId(UUID.randomUUID().toString())
                .build();

        String returnValue;
        try {
            returnValue = commandGateway.sendAndWait(createProductCommand);
        } catch (Exception ex) {
            returnValue = ex.getLocalizedMessage();
        }
        return returnValue;
    }

    @GetMapping
    public String getProduct() {
        return "HTTP Get handled by server " + env.getProperty("local.server.port");
    }
}