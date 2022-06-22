package org.nanotek.crawler.data.config.meta;

public interface IMetaEdge {

	boolean equals(java.lang.Object o);

	Class<?> getLeft();

	Class<?> getRight();

	int hashCode();

	void setLeft(Class<?> left);

	void setRight(Class<?> right);

	void setType(RelationType type);

	java.lang.String toString();

	Class<?> getLeft(Object object);

	Class<?> getRight(Object object);

	RelationType getType();

}