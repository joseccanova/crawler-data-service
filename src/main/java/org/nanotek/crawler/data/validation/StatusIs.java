package org.nanotek.crawler.data.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SucursalStatusIsValidator.class)
@Documented
public @interface StatusIs {
    String message () default "Status deve ser válido " +
            "Encontrado: ${validatedValue}";

    Class<?>[] groups () default {};

    Class<? extends Payload>[] payload () default {};
}
