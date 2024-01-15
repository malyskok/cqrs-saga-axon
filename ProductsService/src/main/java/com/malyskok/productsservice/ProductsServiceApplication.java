package com.malyskok.productsservice;

import com.malyskok.productsservice.command.interceptors.CreateProductCommandInterceptor;
import com.malyskok.productsservice.config.AxonXstreamConfig;
import com.malyskok.productsservice.core.errorhandling.ProductsServiceEventsErrorHandler;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({ AxonXstreamConfig.class })
@EnableDiscoveryClient
@SpringBootApplication
public class ProductsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsServiceApplication.class, args);
	}

	@Autowired
	public void registerCreateProductCommandInterceptor(ApplicationContext applicationContext, CommandBus commandBus){
		commandBus.registerDispatchInterceptor(applicationContext.getBean(CreateProductCommandInterceptor.class));
	}

	@Autowired
	public void configure(EventProcessingConfigurer configurer){
		configurer.registerListenerInvocationErrorHandler("product-group",
				conf -> new ProductsServiceEventsErrorHandler());

//		configurer.registerListenerInvocationErrorHandler("product-group",
//				conf -> new PropagatingErrorHandler.instance());
	}

	@Bean(name = "productSnapshotTriggerDefinition")
	public SnapshotTriggerDefinition productSnapshotTriggerDefinition(Snapshotter snapshotter){
		return new EventCountSnapshotTriggerDefinition(snapshotter, 3);
	}

}
