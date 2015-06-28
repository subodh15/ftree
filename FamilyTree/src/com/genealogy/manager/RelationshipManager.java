package com.genealogy.manager;

import java.util.ArrayList;
import java.util.HashSet;

import com.genealogy.model.FamilyRelation;
import com.genealogy.model.Person;
import com.genealogy.model.Relationship;

public class RelationshipManager {

   /**
    * Add a relation to this person.
    * @param secondary person
    * @param relationType type
    */
   public static void addRelationship(Person from, Person person, Relationship relationType) {

      if ( relationType == Relationship.SIBLING ) {
         addSiblingRelation(from, person);
         return;
      }
      HashSet<FamilyRelation> relations = from.getRelations();

      Person.log.finest("Adding relationship " + relationType + " to " + person);

      // TODO This can be updated to create all other relationships
      // e.g. If a child is being added - it can create a grandparent relation with its grandparents

      FamilyRelation relationShip = new FamilyRelation(from,person,relationType);
      from.getRelations().add(relationShip); // Use a set for restricted relationships.

      if ( relationType  == Relationship.CHILD ) {
         from.addParent( person );
         //
         // Add sibling relationships.
         FamilyRelation[] relationsArray = from.getRelations().toArray(new FamilyRelation[from.getRelations().size()]);
         for ( FamilyRelation relation : relationsArray) {
            if ( relation.getType() == Relationship.CHILD && person != relation.getRight() ) {
               RelationshipManager.addRelationship(relation.getRight(),person, Relationship.SIBLING);
            }

            if ( relation.getType() == Relationship.SPOUSE ) {
               relation.getRight().addChild(person);
            }
         }

         //
         // Add Grandparents
         for ( FamilyRelation relation : relations) {
            if (relation.getType().equals( Relationship.FATHER) || relation.getType().equals( Relationship.FATHER) ) {
               person.addGrandParent(relation.getRight());
            }
         }

      }
      else if ( relationType  == Relationship.SPOUSE ) {
         FamilyRelation spouseRelation = new FamilyRelation(person,from,Relationship.SPOUSE);
         person.getRelations().add(spouseRelation);

         // All children are now children of person as well.

         FamilyRelation relationArray[] = relations.toArray(new FamilyRelation[relations.size()]);
         for ( FamilyRelation relation : relationArray) {
            if ( relation.getType() == Relationship.CHILD ) {
               if ( ! person.hasChild(relation.getRight() ) ) {
                  RelationshipManager.addRelationship(person,relation.getRight(), Relationship.CHILD);
               }
            }
         }
      }

   }

   private static void addSiblingRelation(Person from, Person person) {
      
      // Create a two relationship between the siblings.
      from.getRelations().add( new FamilyRelation( from,person, Relationship.SIBLING ) ); 
      person.getRelations().add( new FamilyRelation( person,from, Relationship.SIBLING ) ); 
      
      FamilyRelation [] relations = from.getRelations().toArray(new FamilyRelation[from.getRelations().size()]);
      
      for (FamilyRelation relation : relations ) {
         Person right = relation.getRight();
         if ( relation.getType() == Relationship.SIBLING ) {
           // from.getRelations().add( new FamilyRelation( from,right, Relationship.SIBLING ) ); 
            if ( ! right.equals(person) ) {
               right.getRelations().add( new FamilyRelation( right,person, Relationship.SIBLING ) );   
               person.getRelations().add( new FamilyRelation( person,right, Relationship.SIBLING ) );   
            }
         }
         else if ( relation.getType() == Relationship.FATHER ) {
            // Add a father relation to me.
            right.getRelations().add( new FamilyRelation( right,person, Relationship.CHILD ) ); 
            person.getRelations().add( new FamilyRelation( person,right, Relationship.FATHER ) ); 
         }
      }
   }

   /**
    * @param person
    * @return
    */
   public static ArrayList<Person> getCousins(Person person) {
      ArrayList<Person> cousins = new ArrayList<>();
      return cousins;
   }

}
