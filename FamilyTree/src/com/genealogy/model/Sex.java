package com.genealogy.model;

public enum Sex {
	MALE,FEMALE,OTHER;
	
	  /**
    * Return a Camel case representation of this Enum
    */
	public String toString() {
		return name().charAt(0) + name().substring(1).toLowerCase();
	}
}
