package org.nanotek.crawler.data.validation;

import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LatitudeValidator implements ConstraintValidator<Latitude, Optional<Double>>{

	static final Double MIN_LATITUDE = -90d;

	static final Double MAX_LATITUDE = 90d;
	
	@Override
	public boolean isValid(Optional<Double> valueo, ConstraintValidatorContext context) {
		return valueo.map(v -> v >= MIN_LATITUDE && v <= MAX_LATITUDE).orElse(false);
	}

}
