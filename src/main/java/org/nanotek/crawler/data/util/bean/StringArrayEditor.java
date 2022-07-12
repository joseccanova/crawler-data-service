package org.nanotek.crawler.data.util.bean;

public class StringArrayEditor extends ArrayEditor {

	public StringArrayEditor() {
	}

	
	@Override
	public void setValue(Object value) {
		String[] ary = String[].class.cast(value);
		super.setValue(ary);
	}
}
