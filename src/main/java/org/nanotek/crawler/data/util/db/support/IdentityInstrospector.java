package org.nanotek.crawler.data.util.db.support;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.nanotek.crawler.data.config.meta.MetaDataAttribute;
import org.nanotek.crawler.data.stereotype.Introspection;
import org.nanotek.crawler.data.stereotype.Introspector;
import org.nanotek.crawler.data.util.MutatorSupport;

import lombok.Getter;
import schemacrawler.schema.TableConstraintColumn;

@SuppressWarnings("unchecked")
public class IdentityInstrospector<T extends List<MetaDataAttribute>, I extends IdentityInstrospection<T,I>> implements Introspector<T,I> {

	@Getter
	protected Optional<T> identity;

	protected List<TableConstraintColumn> columns;

	public IdentityInstrospector() {
		identity  = MutatorSupport.empty();
	}
	
	public IdentityInstrospector(List<TableConstraintColumn> columns2) {
		this.columns=columns2;
	}

	public T instrospected(){
		return identity.orElseThrow();
	}
	
	
	@Override
 	public <S extends Introspector<T, S>> Optional<Introspection<T, S>> instrospect() {
		T instrospected = processColumns(columns);
		Introspection<T, S> introspection = new Introspection<T,S>(){
			@Override
			public T instrospected() {
				return instrospected;
			}
		};
		return Optional.ofNullable(introspection);
 	}	
	
	private T processColumns(List<TableConstraintColumn> columns) {
		return (T) columns
		.stream()
		.map(c ->{
			return extracted(c);
		}).collect(Collectors.toList());
	}

	private MetaDataAttribute extracted(TableConstraintColumn c) {
		MetaDataAttribute mda = new MetaDataAttribute();
		mda.setColumnName(c.getName());
		mda.setId(true);
		mda.setRequired(true);
		processMdaClazz(mda , c);
		return mda;
	}

	private void processMdaClazz(MetaDataAttribute mda, TableConstraintColumn c) {
		 Class<?> clazz = c.getColumnDataType().getTypeMappedClass();
		 if (isValid(clazz)) {
			 mda.setClazz(clazz);
		 }
	}

}
