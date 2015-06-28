package com.genealogy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

import com.genealogy.manager.FamilyTreeManager;
import com.genealogy.manager.RelationshipManager;

/**
 * 
 */
public class Person implements Serializable {
   /**
    * 
    */
   private static final long serialVersionUID = -7613524102631352030L;
   private String name;
   private String lastName;
   private Date dob;

   private HashSet<FamilyRelation> relations = new HashSet<>();

   private Sex sex;
   private UUID uuid ;
   
   public transient static Logger log = Logger.getLogger("com.genealogy.model");
   /**
    * 
    * @param name
    * @param lastName
    */
   public Person(String name, String lastName, Sex sex) {
      this.sex = sex;
      this.name = name;
      this.lastName = lastName;
      uuid = UUID.randomUUID();
   }

   /**
    * Get the ancestor for this person.
    * @return
    */
   public Person getAncestor() {
      //
      // TODO : Get the longest tree back - i.e. could be matriarch or patriach. 
      Person father = this.getFather();
      if ( father != null ) { 
         Person myFather = father.getAncestor();
         if ( myFather != null ) {
            return myFather;
         }
      }

      return father;
   }

   /**
    * Get the matriarch of the family tree.
    * @return
    */
   public Person getMatriarch() {
      Person mother = this.getMother();
      if ( mother != null ) {
         Person matriarch = mother.getMatriarch();
         if ( matriarch != null )
            return matriarch;
      }
      return mother;
   }


   public void addParent(Person child) {
      child.relations.add( new FamilyRelation(child ,this,this.sex == Sex.MALE ? Relationship.FATHER : Relationship.MOTHER) );

      // Also the father of childs siblings.
      FamilyRelation [] relationsArray = child.getRelations().toArray(new FamilyRelation[ child.getRelations().size() ]);
      for ( FamilyRelation relation : relationsArray) {
         if ( relation.getType().equals( Relationship.SIBLING)) {
            Person right = relation.getRight();
            right.relations.add( new FamilyRelation(right ,this,this.sex == Sex.MALE ? Relationship.FATHER : Relationship.MOTHER) );
            this.relations.add(new FamilyRelation(this ,right, Relationship.CHILD)  );
         }
      }
      
      Person spouse = this.getSpouse();
      if ( spouse != null ) {
         FamilyRelation parentRel = new FamilyRelation(child, spouse, spouse.sex == Sex.MALE ? Relationship.FATHER : Relationship.MOTHER);
         child.relations.add(parentRel);
      }
   }
   
   public void addGrandParent(Person grandParent) {
      this.relations.add( new FamilyRelation(this ,grandParent, Relationship.GRANDPARENT) );
   }

   /**
    * 
    * @param relationType
    * @return
    */
   public ArrayList<Person> getRelative( Relationship relationType ) {
      ArrayList<Person> relatives = new ArrayList<>();
      for ( FamilyRelation relation : relations) {
         if ( relation.getType() == relationType ) {
            relatives.add(relation.getRight());
         }
      }
      return relatives;
   }

   /**
    * Remove a relation
    * @param targetRelation
    */
   public void removeRelation(FamilyRelation targetRelation) {
      this.relations.remove(targetRelation);
   }
   
   /**
    * Helper to check if we have person as a child.
    * @param right
    * @return
    */
   public boolean hasChild(Person right) {
      for ( FamilyRelation relation : relations) {
         if ( relation.getRight().equals( right ) ) {
            return true;
         }
      }
      return false;
   }

   public void addChild(Person person) {
      if ( ! hasChild( person ) ) {
         FamilyRelation spouseRelation = new FamilyRelation(this,person,Relationship.CHILD);
         this.relations.add(spouseRelation);
      }
   }

   //
   // Helper methods to get relations.
   public Person getFather() {
      ArrayList<Person> relative = getRelative(Relationship.FATHER);
      return relative.size() > 0 ? relative.get(0) : null;
   }

   public Person getMother() {
      ArrayList<Person> relative = getRelative(Relationship.MOTHER);
      return relative.size() > 0 ? relative.get(0) : null;
   }

   public Person getSpouse() {
      ArrayList<Person> relative = getRelative(Relationship.SPOUSE);
      return relative.size() > 0 ? relative.get(0) : null;		
   }

   /**
    * 
    * @return
    */
   public String toDetails() {
      StringBuffer detailsStr = new StringBuffer();	
      detailsStr.append( this.name ) ;

      detailsStr.append(',');

      detailsStr.append(this.lastName);
      detailsStr.append('(');
      detailsStr.append(this.sex);
      detailsStr.append(')');
      //	detailsStr.append('(');

      //	detailsStr.append(this.uuid);

      //	detailsStr.append(')');

      return detailsStr.toString();
   }

   public String toString() {
      return this.name + ", " + this.lastName;
   }
   /**
    * Get the unique ID for this Person
    * @return
    */
   public UUID getUID() {
      return this.uuid;
   }


   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the lastName
    */
   public String getLastName() {
      return lastName;
   }

   /**
    * @param lastName the lastName to set
    */
   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   /**
    * @return the dob
    */
   public Date getDob() {
      return dob;
   }

   /**
    * @param dob the dob to set
    */
   public void setDob(Date dob) {
      this.dob = dob;
   }

   /**
    * @return the relations
    */
   public HashSet<FamilyRelation> getRelations() {
      return relations;
   }

   /**
    * @param relations the relations to set
    */
   public void setRelations(HashSet<FamilyRelation> relations) {
      this.relations = relations;
   }

   /**
    * @return the sex
    */
   public Sex getSex() {
      return sex;
   }

   /**
    * @param sex the sex to set
    */
   public void setSex(Sex sex) {
      this.sex = sex;
   }

   /**
    * @return the uuid
    */
   public UUID getUuid() {
      return uuid;
   }

   /**
    * @param uuid the uuid to set
    */
   public void setUuid(UUID uuid) {
      this.uuid = uuid;
   }
   
   @Override
   public int hashCode() {
      return ( this.getUID().toString() ).hashCode();
   }
   
   @Override
   public boolean equals(Object o) 
   {
       if (o instanceof Person) 
       {
         Person c = (Person) o;
         if ( this.uuid.equals(c.uuid) ) //whatever here
            return true;
       }
       return false;
   }
}
