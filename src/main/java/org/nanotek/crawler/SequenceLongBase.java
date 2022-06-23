package org.nanotek.crawler;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.nanotek.crawler.legacy.stereotype.Identificavel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@MappedSuperclass
@JsonInclude(Include.NON_EMPTY)
public abstract class SequenceLongBase<ID extends Serializable>
extends Base<SequenceLongBase<ID> , ID> 
implements Identificavel<ID>{

	private static final long serialVersionUID = 1932266128563675834L;
	
}
