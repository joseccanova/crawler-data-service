package org.nanotek.crawler.data.config.meta;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyDeferrability;
import schemacrawler.schema.ForeignKeyUpdateRule;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;

public class MetaRelationClass implements ForeignKey{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5472699723306201451L;
	
	
	public ForeignKey foreignKey;

	public MetaRelationClass(ForeignKey f) {
		this.foreignKey = f;
	}

	public Iterator<ColumnReference> iterator() {
		return foreignKey.iterator();
	}

	public void forEach(Consumer<? super ColumnReference> action) {
		foreignKey.forEach(action);
	}

	public String getDefinition() {
		return foreignKey.getDefinition();
	}

	public String getRemarks() {
		return foreignKey.getRemarks();
	}

	public Table getParent() {
		return foreignKey.getParent();
	}

	public TableConstraintType getType() {
		return foreignKey.getType();
	}

	public <T> T getAttribute(String name) {
		return foreignKey.getAttribute(name);
	}

	public Schema getSchema() {
		return foreignKey.getSchema();
	}

	public boolean hasRemarks() {
		return foreignKey.hasRemarks();
	}

	public boolean hasDefinition() {
		return foreignKey.hasDefinition();
	}

	public String getFullName() {
		return foreignKey.getFullName();
	}

	public List<TableConstraintColumn> getColumns() {
		return foreignKey.getColumns();
	}

	public void setRemarks(String remarks) {
		foreignKey.setRemarks(remarks);
	}

	public String getShortName() {
		return foreignKey.getShortName();
	}

	public List<ColumnReference> getColumnReferences() {
		return foreignKey.getColumnReferences();
	}

	public <T> T getAttribute(String name, T defaultValue) throws ClassCastException {
		return foreignKey.getAttribute(name, defaultValue);
	}

	public String getName() {
		return foreignKey.getName();
	}

	public List<TableConstraintColumn> getConstrainedColumns() {
		return foreignKey.getConstrainedColumns();
	}

	public NamedObjectKey key() {
		return foreignKey.key();
	}

	public boolean isParentPartial() {
		return foreignKey.isParentPartial();
	}

	public Table getForeignKeyTable() {
		return foreignKey.getForeignKeyTable();
	}

	public boolean isDeferrable() {
		return foreignKey.isDeferrable();
	}

	public Table getPrimaryKeyTable() {
		return foreignKey.getPrimaryKeyTable();
	}

	public Map<String, Object> getAttributes() {
		return foreignKey.getAttributes();
	}

	public boolean isInitiallyDeferred() {
		return foreignKey.isInitiallyDeferred();
	}

	public Table getReferencedTable() {
		return foreignKey.getReferencedTable();
	}

	public boolean hasAttribute(String name) {
		return foreignKey.hasAttribute(name);
	}

	public Table getReferencingTable() {
		return foreignKey.getReferencingTable();
	}

	public Spliterator<ColumnReference> spliterator() {
		return foreignKey.spliterator();
	}

	public <T> Optional<T> lookupAttribute(String name) {
		return foreignKey.lookupAttribute(name);
	}

	public void removeAttribute(String name) {
		foreignKey.removeAttribute(name);
	}

	public <T> void setAttribute(String name, T value) {
		foreignKey.setAttribute(name, value);
	}

	public int compareTo(NamedObject o) {
		return foreignKey.compareTo(o);
	}

	public ForeignKeyDeferrability getDeferrability() {
		return foreignKey.getDeferrability();
	}

	public ForeignKeyUpdateRule getDeleteRule() {
		return foreignKey.getDeleteRule();
	}

	public String getSpecificName() {
		return foreignKey.getSpecificName();
	}

	public ForeignKeyUpdateRule getUpdateRule() {
		return foreignKey.getUpdateRule();
	}

}
