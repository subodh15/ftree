package com.genealogy.persistence;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.genealogy.model.FamilyRelation;
import com.genealogy.model.Person;
import com.genealogy.model.Relationship;

public class StaticDBImpl implements PersistanceManager{

   private  Vector<Person> persondb = new Vector<>();

   private static StaticDBImpl instance = null;

   private final static String DBFILE="person.db";

   private static Logger log = Logger.getLogger("com.genealogy.db");

   protected StaticDBImpl() {
      File inputfile = new File(DBFILE);
      try {
         Object object = read(inputfile);
         persondb = (Vector<Person>)object;
         log.info("Read " +persondb.size() + " members from offline db file at "  + inputfile.getAbsolutePath());
      } catch (Exception e) {
        // e.printStackTrace();
         log.fine("Exception reading database file : " + inputfile.getAbsolutePath() + ":" + e.toString());
      }
   }

   public static StaticDBImpl getInstance() {
      if(instance == null) {
         instance = new StaticDBImpl();
      }
      return instance;
   }

   public void add(Person person) {
      log.finest("Creating new person " + person);
      persondb.add( person);
   }

   /**
    * Store our data to an external serialized file.
    */
   public void store()
   {
      File outputfile = new File(DBFILE);

      try (FileOutputStream out = new FileOutputStream(outputfile);ObjectOutputStream s = new ObjectOutputStream(out);)
      {
         log.info("Persisting data to offline file : " + outputfile.getAbsolutePath() );
         s.writeObject(persondb);
         s.flush();
      }
      catch (Exception ex ) { ex.printStackTrace();}
   }

   public static Object read(File file) throws Exception
   {
      FileInputStream in = new FileInputStream(file);
      try ( ObjectInputStream s = new ObjectInputStream(in);) {
         return s.readObject();
      }
   }

   /**
    * 
    */
   public ArrayList<Person> find(String searchString) {
      ArrayList<Person> searchResults = new ArrayList<>();
      for ( Person p : persondb) {
         try {
            if ( p.getName().matches(searchString) || p.getLastName().matches(searchString) || p.getLastName().matches(searchString) ) {
               searchResults.add(p);
            }
         }
         catch (Exception ex) {
            log.severe("Fatal error : could not match on search pattern [" + searchString + "] : " + ex.toString());
            break;
         }
      }	
      return searchResults;
   }

   @Override
   public void destroyData() { // API naming is important - clear seems too benign - an API named destroy would less likely be abused.
      persondb.clear();
   }

   @Override
   public void remove(Person remove) {
      persondb.remove(remove);
   }

   @Override
   public long getTotalCount() {
      return persondb.size();
   }

   /**
    * Get a list of all the primary head of families.
    */
   @Override
   public ArrayList<Person> getPrimaries() {
      ArrayList<Person> headofFamilies = new ArrayList<>();
      for ( Person p : persondb) {
         HashSet<FamilyRelation> relations = p.getRelations();
         boolean foundParent = false;
         for ( FamilyRelation flyRelation : relations) {
            if ( flyRelation.getType() == Relationship.FATHER || flyRelation.getType() == Relationship.MOTHER ) {
               foundParent = true;
               break;
            }
         }
         if ( ! foundParent ) {
            headofFamilies.add(p);
         }
      }
      return headofFamilies;
   }

}
