package org.nanotek.crawler.data.config.meta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;

public class MetaIdentidy implements PrimaryKey {

	protected PrimaryKey key;

	public MetaIdentidy() {
		super();
	}

	public MetaIdentidy(PrimaryKey key) {
		super();
		this.key = key;
	}
	
	
	
	public String getDefinition() {
		return key.getDefinition();
	}

	public String getRemarks() {
		return key.getRemarks();
	}

	public Table getParent() {
		return key.getParent();
	}

	public TableConstraintType getType() {
		return key.getType();
	}

	public <T> T getAttribute(String name) {
		return key.getAttribute(name);
	}

	public Schema getSchema() {
		return key.getSchema();
	}

	public boolean hasRemarks() {
		return key.hasRemarks();
	}

	public boolean hasDefinition() {
		return key.hasDefinition();
	}

	public String getFullName() {
		return key.getFullName();
	}

	public List<TableConstraintColumn> getColumns() {
		return key.getColumns();
	}

	public void setRemarks(String remarks) {
		key.setRemarks(remarks);
	}

	public String getShortName() {
		return key.getShortName();
	}

	public <T> T getAttribute(String name, T defaultValue) throws ClassCastException {
		return key.getAttribute(name, defaultValue);
	}

	public String getName() {
		return Optional.ofNullable(key).map(k->k.getName()).orElse("");
	}

	public List<TableConstraintColumn> getConstrainedColumns() {
		return key.getConstrainedColumns();
	}

	public NamedObjectKey key() {
		return key.key();
	}

	public boolean isParentPartial() {
		return key.isParentPartial();
	}

	public boolean isDeferrable() {
		return key.isDeferrable();
	}

	public Map<String, Object> getAttributes() {
		return key.getAttributes();
	}

	public boolean isInitiallyDeferred() {
		return key.isInitiallyDeferred();
	}

	public boolean hasAttribute(String name) {
		return key.hasAttribute(name);
	}

	public <T> Optional<T> lookupAttribute(String name) {
		return key.lookupAttribute(name);
	}

	public void removeAttribute(String name) {
		key.removeAttribute(name);
	}

	public <T> void setAttribute(String name, T value) {
		key.setAttribute(name, value);
	}

	public int compareTo(NamedObject o) {
		return key.compareTo(o);
	}

}
