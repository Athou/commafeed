package com.commafeed.backend;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class MySQL5Dialect extends MySQL5InnoDBDialect {

	public String getTableTypeString() {
		return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
	}
}