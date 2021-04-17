package com.wavefront.autoconfigissue;

import brave.sampler.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class AutoConfigIssueDemo {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoConfigIssueDemo.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AutoConfigIssueDemo.class, args);
		final Sampler sampler = context.getBean(Sampler.class);
		LOGGER.info("Sampler is: {}", sampler);
	}

	@Configuration
	@ConditionalOnBean(Tracer.class)
	@AutoConfigureAfter(BraveAutoConfiguration.class)
	public class CustomAutoConfigUsingTracer {

		@Bean
		public String dependency(Tracer tracer) {
			return "test";
		}

	}

}