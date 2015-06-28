package com.genealogy.model;

public enum Relationship {
	SPOUSE,
	MOTHER,
	FATHER,
	SIBLING,
	GRANDPARENT,
	OTHER,
	CHILD,
	NONE;
	
	/**
	 * Return a Camel case representation of this Enum
	 */
	public String toString() {
		return name().charAt(0) + name().substring(1).toLowerCase();
	}
}
