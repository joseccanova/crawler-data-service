package org.nanotek.crawler.data.util;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

public class PropertyEditorRegistar {

	public PropertyEditorRegistar() {
	}
	
	public static void registerPropertyEditor(PropertyEditor pe , Class<?> forClass) {
		PropertyEditorManager.registerEditor(forClass, pe.getClass());
	}
	
}
