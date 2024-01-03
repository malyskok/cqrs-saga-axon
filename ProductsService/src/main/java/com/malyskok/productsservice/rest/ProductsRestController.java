package com.malyskok.productsservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductsRestController
{
  @Autowired
  Environment env;

  @PostMapping
  public String createProduct(@RequestBody CreateProductRestModel model){
    return "HTTP Post handled for Product: " + model.title();
  }

  @GetMapping
  public String getProduct(){
    return "HTTP Get handled by server " + env.getProperty("local.server.port");
  }
}