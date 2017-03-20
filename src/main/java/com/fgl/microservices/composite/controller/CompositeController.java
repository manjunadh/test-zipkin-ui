package com.fgl.microservices.composite.controller;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fgl.microservices.commons.model.Product;
import com.fgl.microservices.composite.model.ProductDetail;
import com.fgl.microservices.composite.services.CompositeService;


@RestController
public class CompositeController implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	private static final Log log = LogFactory.getLog(CompositeController.class);

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private Tracer tracer;
	@Autowired
	private SpanAccessor accessor;

	@Autowired
	private Random random;
	@Autowired
	private CompositeService compositeService;
	private int port;

	@RequestMapping("/")
	public String hi() throws InterruptedException {
		Thread.sleep(this.random.nextInt(1000));
		log.info("Home page");
		String s = this.restTemplate.getForObject("http://localhost:" + this.port + "/hi2", String.class);
		return "hi/" + s;
	}

	@RequestMapping("/call")
	public Callable<String> call() {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				int millis = CompositeController.this.random.nextInt(1000);
				Thread.sleep(millis);
				CompositeController.this.tracer.addTag("callable-sleep-millis", String.valueOf(millis));
				Span currentSpan = CompositeController.this.accessor.getCurrentSpan();
				return "async hi: " + currentSpan;
			}
		};
	}

	@RequestMapping("/products")
	public List<Product> getAllProducts() {
		CompositeController.this.tracer.addTag("Get-products", "all products controller");
		return this.restTemplate.getForObject("http://35.184.125.140:9876/products", List.class);
	}

	@RequestMapping("/products/{id}")
	public ResponseEntity<Product> getProductById(@PathVariable String id) {
		log.info("getting all product  file for id" + id);
		return this.restTemplate.getForEntity("http://35.184.125.140:9876/products/" + id, Product.class);
	}

	@RequestMapping("/prices/{id}")
	public ResponseEntity<Product> getPriceById(@PathVariable String id) {
		log.info("getting price files for id" + id);
		return this.restTemplate.getForEntity("http://35.184.125.140:9899/price/" + id, Product.class);
	}

	@RequestMapping("/hi2")
	public String hi2() throws InterruptedException {
		log.info("hi2");
		int millis = this.random.nextInt(1000);
		Thread.sleep(millis);
		this.tracer.addTag("random-sleep-millis", String.valueOf(millis));
		return "hi2";
	}

	@RequestMapping("/traced")
	public String traced() throws InterruptedException {
		Span span = this.tracer.createSpan("http:customTraceEndpoint", new AlwaysSampler());
		int millis = this.random.nextInt(1000);
		log.info(String.format("Sleeping for [%d] millis", millis));
		Thread.sleep(millis);
		this.tracer.addTag("random-sleep-millis", String.valueOf(millis));

		String s = this.restTemplate.getForObject("http://localhost:" + this.port + "/call", String.class);
		this.tracer.close(span);
		return "traced/" + s;
	}

	@RequestMapping("/start")
	public String start() throws InterruptedException {
		int millis = this.random.nextInt(1000);
		log.info(String.format("Sleeping for [%d] millis", millis));
		Thread.sleep(millis);
		this.tracer.addTag("random-sleep-millis", String.valueOf(millis));

		String s = this.restTemplate.getForObject("http://localhost:" + this.port + "/call", String.class);
		return "start/" + s;
	}

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		this.port = event.getEmbeddedServletContainer().getPort();
	}
	@RequestMapping("/product/{id}")
	public ProductDetail getProductDetailById(@PathVariable String id) {
		this.tracer.addTag("get product detail by id", "get product detail by id controller");
		log.info("fetching product details for id"+id);
		return compositeService.getProductDetailById(id);
	}
}
