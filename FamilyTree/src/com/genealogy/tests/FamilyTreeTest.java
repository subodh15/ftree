package com.genealogy.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.genealogy.manager.FamilyTreeManager;
import com.genealogy.manager.RelationshipManager;
import com.genealogy.model.Person;
import com.genealogy.model.Relationship;
import com.genealogy.model.Sex;

public class FamilyTreeTest {

   @Test
   public void testRelationAddSpouse() {
      Person mathew = new Person("Mathew", "Haddin", Sex.MALE);
      Person mary = new Person("Mary", "Blige", Sex.FEMALE);
      RelationshipManager.addRelationship(mathew,mary, Relationship.SPOUSE);
      
      ArrayList<Person> relative = mary.getRelative(Relationship.SPOUSE);
      
      assertEquals("There can be only one spouse",relative.size(), 1);
      
      assertEquals("Wife is a spouse of the husband", relative.get(0), mathew);
   }
   
   @Test
   public void testRelationAddChild() {
      Person abraham = new Person("Abraham", "Simpson", Sex.MALE);
      Person homer = new Person("Homer", "Simpson", Sex.MALE);
      Person marge = new Person("Marge", "Simpson", Sex.FEMALE);
      
      RelationshipManager.addRelationship(abraham,homer, Relationship.CHILD);

      RelationshipManager.addRelationship(marge,homer, Relationship.SPOUSE );
      
      ArrayList<Person> relative = homer.getRelative(Relationship.FATHER);
      
      assertEquals("There is only one child added", 1,relative.size());
      
      assertEquals("Parent should be father of the child", abraham, relative.get(0));

      Person lisa = new Person("Lisa", "Simpson", Sex.FEMALE);
      Person bart = new Person("Bart", "Simpson", Sex.MALE);

      RelationshipManager.addRelationship(homer,lisa, Relationship.CHILD);
      RelationshipManager.addRelationship(homer,bart, Relationship.CHILD);
      
      ArrayList<Person> sibling = lisa.getRelative(Relationship.SIBLING);
      
      assertEquals("There needs to be one sibling", 1,sibling.size());
      
      ArrayList<Person> mother = lisa.getRelative(Relationship.MOTHER);
      
      assertEquals("There is only one mother", 1,mother.size());
      
      assertEquals("Marge is Lisa's mother", marge, mother.get(0));
      
      ArrayList<Person> granpa = bart.getRelative(Relationship.GRANDPARENT);
      
      assertEquals("Bart has one granpa", 1,granpa.size());
      
   }
   
   @Test
   public void testPersonCreate() {
      
      // Clear our data and start fresh.
      FamilyTreeManager.clearDB();

      Person hubby = FamilyTreeManager.createPerson("Samuel", "kinison",Sex.MALE);
      Person wife = FamilyTreeManager.createPerson("Samantha", "kinison", Sex.FEMALE);
      Person child = FamilyTreeManager.createPerson("John", "Doe", Sex.MALE);
      RelationshipManager.addRelationship(hubby,wife, Relationship.SPOUSE);
      RelationshipManager.addRelationship(hubby,child, Relationship.CHILD);
      
      assertEquals("We added only 3 people to the DB" , 3 ,FamilyTreeManager.getTotalCount());
      
   }
   
   @Test
   public void testPersistence() {
      FamilyTreeManager.clearDB();
      assertEquals( "Database should be cleared", FamilyTreeManager.getTotalCount(), 0);
      long total = FamilyTreeManager.createTestData();
      System.out.println( "Created " + FamilyTreeManager.getTotalCount());
      assertEquals( "Created count should match actual count", total, FamilyTreeManager.getTotalCount());
   }
   
   @Test
   public void testRelationRemove() {
      FamilyTreeManager.clearDB();
      
      assertEquals("DB should not contain any members ", 0, FamilyTreeManager.getTotalCount());
      
      Person mathew = FamilyTreeManager.createPerson("Mathew", "Haddin", Sex.MALE);
      Person mary = FamilyTreeManager.createPerson("Mary", "Blige", Sex.FEMALE);
      Person mike = FamilyTreeManager.createPerson("Mike", "Haddin", Sex.MALE);
      Person janet = FamilyTreeManager.createPerson("Janet", "Haddin", Sex.FEMALE);
      RelationshipManager.addRelationship(mathew,mary, Relationship.SPOUSE);
      RelationshipManager.addRelationship(mathew,mike, Relationship.CHILD);
      RelationshipManager.addRelationship(mathew,janet, Relationship.CHILD);
      
      assertEquals("Created four people", 4, FamilyTreeManager.getTotalCount());
      
      FamilyTreeManager.removePerson(mike);
      
      assertEquals("Removed one person out of 4", 3, FamilyTreeManager.getTotalCount());
      
      // mike should be removed from Mathew and Mary as children and from janet as a sibling.
      
      ArrayList<Person> relative = mathew.getRelative( Relationship.CHILD );
      
      assertEquals("Should not have any siblings", 1, relative.size());
      
      relative = mary.getRelative( Relationship.CHILD );
      
      assertEquals("Should not have any siblings", 1, relative.size());
      
      FamilyTreeManager.removePerson(mary);
      
      assertEquals("Removed 2 out of 4", 2, FamilyTreeManager.getTotalCount());
      
      assertEquals("Spouse has been removed independently ", 0, mathew.getRelative(Relationship.SPOUSE).size());
      
      assertEquals("Mother has been removed independently ", 0, janet.getRelative(Relationship.MOTHER).size());
      
      Person ann = FamilyTreeManager.createPerson("Ann", "Taylor", Sex.FEMALE);
      
      RelationshipManager.addRelationship(mathew,ann, Relationship.SPOUSE); // New Wife automatically gets one child
      
      assertEquals("Janet gets a new mother automatically. ", 1, janet.getRelative(Relationship.MOTHER).size());
      assertEquals("New Wife automatically gets one child. ", 1, ann.getRelative(Relationship.CHILD).size());
      
   }
   
   /**
    * Head of the family is someone who does not have a parent node - so basically - his family starts from him going down.
    */
   @Test
   public void testHeadOfFamilies() {
      FamilyTreeManager.clearDB();
      
      //
      // Create known data
      Person grandpasimpson = FamilyTreeManager.createPerson("Abraham", "Simpson",Sex.MALE);
      Person homer = FamilyTreeManager.createPerson("Homer", "Simpson",Sex.MALE);
      Person marge = FamilyTreeManager.createPerson("Marge", "Simpson", Sex.FEMALE);
      Person bart = FamilyTreeManager.createPerson("Bart", "Simpson", Sex.MALE);
      Person maggie = FamilyTreeManager.createPerson("Maggie", "Simpson", Sex.FEMALE);
      Person lisa = FamilyTreeManager.createPerson("Lisa", "Simpson", Sex.FEMALE);
      Person grandson = FamilyTreeManager.createPerson("Jason", "Doe", Sex.MALE);
      Person granddaughter = FamilyTreeManager.createPerson("Jill", "Doe", Sex.FEMALE);
      Person daughterinlaw = FamilyTreeManager.createPerson("Brenda", "Doe", Sex.FEMALE);
      Person milhouse = FamilyTreeManager.createPerson("Milhouse", "Houten", Sex.MALE);
      Person wiggum = FamilyTreeManager.createPerson("Chief", "Wiggum", Sex.MALE);
      Person ralphwiggum = FamilyTreeManager.createPerson("Ralph", "Wiggum", Sex.MALE);
      RelationshipManager.addRelationship(bart,daughterinlaw, Relationship.SPOUSE);
      
      RelationshipManager.addRelationship(homer,marge, Relationship.SPOUSE);
      RelationshipManager.addRelationship(homer,bart, Relationship.CHILD);
      RelationshipManager.addRelationship(homer,maggie, Relationship.CHILD);
      RelationshipManager.addRelationship(homer,lisa, Relationship.CHILD);
      RelationshipManager.addRelationship(wiggum,ralphwiggum, Relationship.CHILD);
      
      RelationshipManager.addRelationship(bart,grandson, Relationship.CHILD);
      RelationshipManager.addRelationship(bart,granddaughter, Relationship.CHILD);
      RelationshipManager.addRelationship(maggie,milhouse, Relationship.SPOUSE);
      RelationshipManager.addRelationship(lisa,ralphwiggum, Relationship.SPOUSE);
      Person pa = FamilyTreeManager.createPerson("Jack","Ripper", Sex.MALE);
      RelationshipManager.addRelationship(granddaughter,pa, Relationship.SPOUSE );

      RelationshipManager.addRelationship(grandpasimpson,homer, Relationship.CHILD);
      
      Person mike = FamilyTreeManager.createPerson("Mike", "Myers",Sex.MALE);
      Person michelle = FamilyTreeManager.createPerson("Michelle", "Myers",Sex.FEMALE);
      Person junior = FamilyTreeManager.createPerson("Junior", "Myers",Sex.MALE);
      RelationshipManager.addRelationship(mike,michelle, Relationship.SPOUSE);
      RelationshipManager.addRelationship(mike,junior, Relationship.CHILD);
      //
      // End of known data - do not change as the result could then change.
      
      ArrayList<Person> headOfFamilies = FamilyTreeManager.findHeadOfFamilies();
      
      assertEquals("We created 8 people with no parents - hence head of families", 8 , headOfFamilies.size());
      
   }
}
