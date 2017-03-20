package com.fgl.microservices.composite.services;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fgl.microservices.commons.model.Price;
import com.fgl.microservices.commons.model.Product;
import com.fgl.microservices.composite.model.ProductDetail;

@Component
public class CompositeService {
	@Autowired
	private Tracer tracer;
	@Autowired
	private Random random;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ProductDetail productDetail;

	@Bean
	ProductDetail productDetail(){
		return new ProductDetail();
	}
	
	@Async
	public void background() throws InterruptedException {
		int millis = this.random.nextInt(1000);
		Thread.sleep(millis);
		this.tracer.addTag("background-sleep-millis", String.valueOf(millis));
	}
	public ProductDetail getProductDetailById(String id) {
		
		this.tracer.addTag("getProductDetail", "get product detail by id Service");
		String productUrl = "http://35.184.125.140:9876/products/"+id;
	
		String priceUrl = "http://35.184.125.140:9899/price/"+id;

		ResponseEntity<Product> product = restTemplate.getForEntity(productUrl, Product.class);
		ResponseEntity<Price> price = restTemplate.getForEntity(priceUrl, Price.class);
		productDetail.setId(id);
		productDetail.setName(product.getBody().getName());
		productDetail.setDescription(product.getBody().getDescription());
		productDetail.setOfferPrice(price.getBody().getOfferPrice());
		productDetail.setOriginalPrice(price.getBody().getOriginalPrice());
		return productDetail;
	}
}
