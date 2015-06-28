package com.genealogy.model;

import java.io.Serializable;

public class FamilyRelation implements Serializable{

   /**
    * 
    */
   private static final long serialVersionUID = -1887660982050116834L;

   private Person left;

   private Person right;

   private Relationship type;

   public FamilyRelation(Person me, Person to, Relationship rtype) {
      this.left = me;
      this.right = to;
      this.type = rtype;
   }

   /**
    * This is the source relation.
    * @return
    */
   public Person getLeft() {
      return left;
   }

   /**
    * This is the target relation.
    * @return
    */
   public Person getRight() {
      return right;
   }

   public Relationship getType() {
      return type;
   }

   public String toString() {
      return this.right.getName() + " is the " + this.type + " of " + this.left.getName();
   }

   @Override
   public int hashCode() {
      if ( left != null && right != null && left.getUID() != null && right.getUID() != null )
         return ((left.getUID().toString())+(right.getUID().toString())+(type.toString())).hashCode();
      else
         return super.hashCode();
   }
   
   @Override
   public boolean equals(Object o) 
   {
      if (o instanceof FamilyRelation) 
      {
         FamilyRelation c = (FamilyRelation) o;

         if ( this.left.equals(c.left) && this.right.equals( c.right) && this.type.equals(c.type)) //whatever here
            return true;
      }
      return false;
   }
}
