package org.nanotek.crawler;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.nanotek.crawler.legacy.stereotype.Identificavel;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@MappedSuperclass
@JsonInclude(Include.NON_EMPTY)
public class SequenceLongBase<ID extends Serializable>
extends Base<SequenceLongBase<ID> , ID> 
implements Identificavel<ID>{

	private static final long serialVersionUID = 1932266128563675834L;
	

	@Override
	public ID getId() {
		return null;
	}

	@Override
	public void setId(ID id) {
	}
	
}
