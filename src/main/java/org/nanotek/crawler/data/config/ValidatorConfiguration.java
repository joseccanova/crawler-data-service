package org.nanotek.crawler.data.config;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@SpringBootConfiguration
public class ValidatorConfiguration {

	
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}
	
	@Bean(name="validator")
	@Primary
	public LocalValidatorFactoryBean getLocalValidatorFactoryBean() { 
		LocalValidatorFactoryBean validatorFactoryBean =  new LocalValidatorFactoryBean();
		validatorFactoryBean.setValidationMessageSource(messageSource());
		return validatorFactoryBean;
	}
	
	@Bean
	@Primary
	public MethodValidationPostProcessor methodValidationPostProcessor(@Autowired Validator validator) {
		MethodValidationPostProcessor processor =  new MethodValidationPostProcessor();
		processor.setValidator(validator);
		processor.setProxyTargetClass(false);
		processor.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return processor;
	}
	
}
