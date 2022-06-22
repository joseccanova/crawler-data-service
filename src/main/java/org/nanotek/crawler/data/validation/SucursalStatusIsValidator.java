package org.nanotek.crawler.data.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SucursalStatusIsValidator  implements ConstraintValidator<StatusIs, String>{

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return "A".equals(value);
	}

}
