package com.genealogy.persistence;

import java.util.ArrayList;

import com.genealogy.model.Person;

public interface PersistanceManager {
	
	public void add(Person person);

	public ArrayList<Person> find(String searchString);

   public void destroyData();

   public void remove(Person remove);

   public long getTotalCount();

   public ArrayList<Person> getPrimaries();

   public void store();
}
