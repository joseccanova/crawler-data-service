package org.nanotek.crawler.data.util.bean;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;

public class ArrayEditor extends PropertyEditorSupport{

	public ArrayEditor() {
	}

	@Override
	public String getAsText() {
		Object value = getValue();
        if (value == null)
            return "null";
        else
        	return  Arrays.toString(Object[].class.cast(value)).toString();
	}
	
}
