package org.nanotek.crawler.data.util.bean;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MapToBean <T>{

	private ClassMapStrategy mapStrategy;

	public MapToBean() {
	}

	public Optional<?> processMap(final Map<String,?> result) {
		ClassMapStrategy mapper =   Optional.ofNullable(ClassMapStrategy.class.cast(mapStrategy)).orElseThrow();
		final Optional<?> beano = mapper.newInstance();
		result.keySet().forEach(key -> {
			try { 
				final PropertyDescriptor prop = mapper.findDescriptor(key);
				if (null != prop) {
						final Object value = convertValue(result.get(key), prop);
						beano
						.ifPresent(bean -> { 
							try {
								prop.getWriteMethod().invoke(bean, prop.getPropertyType().cast(value));
							}catch (Exception ex) {
								throw new RuntimeException(ex);
							}
						});
				}
			}catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		return beano;
	}
	
	public  Optional<?>  processMap(ClassMapStrategy mapper, final Map<String,?> result) {
		final Optional<?> beano = mapper.newInstance();
		result.keySet().forEach(key -> {
			try { 
				final PropertyDescriptor prop = mapper.findDescriptor(key);
				if (null != prop) {
						final Object value = convertValue(result.get(key), prop);
						beano
						.ifPresent(bean -> { 
							try {
								prop.getWriteMethod().invoke(bean, prop.getPropertyType().cast(value));
							}catch (Exception ex) {
								throw new RuntimeException(ex);
							}
						});
				}
			}catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		return beano;
	}

	static  Optional<?> newInstance(Class<?> clazz)  { 
		try {
			return Optional.of(clazz.getDeclaredConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private String checkForTrim(final String s, PropertyDescriptor prop) {
		return trimmableProperty(prop) ? s.trim() : s;
	}

	private boolean trimmableProperty(PropertyDescriptor prop) {
		return !prop.getPropertyType().getName().contains("String");
	}

	Function<Object,String> myStringFromList = x -> Optional.ofNullable(x)
																.filter(x1->ArrayList.class.equals(x1.getClass()))
																.map(x1 -> ArrayList.class.cast(x1))
																.filter (x1->x1.size()>0)
																.map(x1->x1.get(0))
																.map(x1->String.class.cast(x1))
																.orElse("");
	
	public Object convertValue(Object value, final PropertyDescriptor prop) throws InstantiationException, IllegalAccessException {
		final PropertyEditor editor = getPropertyEditor(prop);
		if (null != editor && null != value) {
			if (String[].class.equals(value.getClass()) && String[].class.equals(prop.getPropertyType())) {
				editor.setValue(value);
			}else  if (String.class.equals(value.getClass()) && String.class.equals(prop.getPropertyType())) { 
				editor.setValue(value);
			}else  if (ArrayList.class.equals(value.getClass())) { 
				editor.setAsText(myStringFromList.apply(value));
			}else {
				setAsValueIs(value , prop.getPropertyType() , editor);
			}
		}
		return Optional.ofNullable(editor).map(e -> e.getValue()).orElse(null);
	}

	private void setAsValueIs(Object value, Class<?> propertyType, PropertyEditor editor2) {
		Optional
			.ofNullable(value)
//			.filter(v -> propertyType.equals(v.getClass()))
			.ifPresent(v -> {
				if(editor2 !=null)
					editor2.setValue(v);
			});		
	}

	public PropertyEditor getPropertyEditorValue(Class<?> cls) {
			return PropertyEditorManager.findEditor(cls);
	}

	protected PropertyEditor getPropertyEditor(PropertyDescriptor desc) throws InstantiationException, IllegalAccessException {
//		Class<?> cls = desc.getPropertyEditorClass();
//		if (null != cls) return (PropertyEditor) cls.newInstance();
		return getPropertyEditorValue(desc.getPropertyType());
	}

	public void setMapStrategy(ClassMapStrategy classMapStrategy) {
		Optional.ofNullable(classMapStrategy.getType()).orElseThrow();
		this.mapStrategy = classMapStrategy;
	}
	
}
