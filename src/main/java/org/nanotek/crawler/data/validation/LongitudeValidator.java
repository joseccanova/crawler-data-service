package org.nanotek.crawler.data.validation;

import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LongitudeValidator implements ConstraintValidator<Longitude, Optional<Double>>{

	static final Double MIN_LONGITUDE = -180d;
	static final Double MAX_LONGITUDE = 180d;
	
	@Override
	public boolean isValid(Optional<Double> valueo, ConstraintValidatorContext context) {
		return valueo.map(v -> v >= MIN_LONGITUDE && v <=MAX_LONGITUDE).orElse(false);
	}

}
