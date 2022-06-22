package org.nanotek.crawler.data.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.ANNOTATION_TYPE , ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LongitudeValidator.class)
@Documented
public @interface Longitude {
	 String message () default "Longitude deve ser válido " +
	            "Encontrado: ${validatedValue}";

	    Class<?>[] groups () default {};

	    Class<? extends Payload>[] payload () default {};
}
