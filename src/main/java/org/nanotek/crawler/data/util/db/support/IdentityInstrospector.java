package org.nanotek.crawler.data.util.db.support;

import java.util.List;
import java.util.Optional;

import org.nanotek.crawler.data.config.meta.MetaIdentidy;
import org.nanotek.crawler.data.stereotype.Introspection;
import org.nanotek.crawler.data.stereotype.Introspector;
import org.nanotek.crawler.data.util.MutatorSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import schemacrawler.schema.TableConstraintColumn;


@AllArgsConstructor
public class IdentityInstrospector<T extends MetaIdentidy> implements Introspector<T> {

	@Getter
	protected Optional<T> identity;


	public IdentityInstrospector() {
		identity  = MutatorSupport.empty();
	}
	

	public Optional<Introspection<T>> introspect(){
			return identity.isPresent()? instrospect(identity.get()) : Optional.empty();
	}
	
	@Override
	public Optional<Introspection<T>> instrospect(T t) {
		processColumns (t.getColumns());
		return Introspector.super.instrospect(t);
	}


	private void processColumns(List<TableConstraintColumn> columns) {
		
	}
	
}
