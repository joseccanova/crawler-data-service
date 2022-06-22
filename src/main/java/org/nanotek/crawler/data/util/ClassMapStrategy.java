package org.nanotek.crawler.data.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClassMapStrategy {

	
    protected Map<String, PropertyDescriptor> descriptorMap = null;
    
    protected Class<?> type;


	public ClassMapStrategy() {
	}
	

	public ClassMapStrategy(Class<?> type) {
		this.type = type;
	}

	
	 Optional<?> newInstance()  { 
		try {
			return Optional.of(type.getDeclaredConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
    public PropertyDescriptor findDescriptor(String columnName) {
    	try { 
    			return  null != columnName && columnName.trim().length() > 0 ? populateDescriptorMap(columnName) : null;
    	}catch (Exception ex) { 
    		throw new RuntimeException (ex);
    	}
    }

    public PropertyDescriptor populateDescriptorMap(String name) {
    	try { 
    			if (null == descriptorMap) descriptorMap = loadDescriptorMap(getType()); //lazy load descriptors
    			return descriptorMap.get(name.toUpperCase().trim());
    	}catch (Exception ex) { 
    		throw new RuntimeException (ex);
    	}
        
    }

    public boolean matches(String name, PropertyDescriptor desc) {
        return desc.getName().equals(name.trim());
    }

    public Map<String, PropertyDescriptor> loadDescriptorMap(Class<?> cls) throws IntrospectionException {
        Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();

        PropertyDescriptor[] descriptors;
        descriptors = loadDescriptors(getType());
        for (PropertyDescriptor descriptor : descriptors) {
            map.put(descriptor.getName().toUpperCase().trim(), descriptor);
        }

        return map;
    }

    public PropertyDescriptor[] loadDescriptors(Class<?> cls) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        return beanInfo.getPropertyDescriptors();
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }


	public Map<String, PropertyDescriptor> getDescriptorMap() {
		return descriptorMap;
	}
}
