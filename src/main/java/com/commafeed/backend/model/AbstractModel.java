package com.commafeed.backend.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractModel implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "gen")
	@TableGenerator(name = "gen", allocationSize = 1000)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
