package com.genealogy.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import com.genealogy.model.FamilyRelation;
import com.genealogy.model.Person;
import com.genealogy.model.Relationship;
import com.genealogy.model.Sex;
import com.genealogy.persistence.DatasourceManager;

public final class FamilyTreeManager {

	private static Logger log = Logger.getLogger("com.genealogy.model");

	public static Person createPerson(String firstname, String lastname, Sex sex) {
		return createPerson(firstname, lastname, sex, false);
	}

	/*
	 * Helper method to create a Person object.
	 */
	public static Person createPerson(String firstname, String lastname, Sex sex, boolean store) {

		Person person = new Person(firstname, lastname,sex);

		DatasourceManager.getPersistenceManager().add( person );

		if ( store ) {
			DatasourceManager.getPersistenceManager().store();
		}
		return person;
	}

	/**
	 * Add the person to our db.
	 * @param person
	 */
	public static void addPerson(Person person) {
		DatasourceManager.getPersistenceManager().add( person );
	}

	/**
	 * Remove the person from our db,
	 * @param remove
	 */
	public static void removePerson(Person remove) {
		// Update Relationships.
	   HashSet<FamilyRelation> relations = remove.getRelations();

		Iterator<FamilyRelation> iterator = relations.iterator();

		while ( iterator.hasNext() ) {
			FamilyRelation familyRelation = iterator.next();
			Person targetPerson = familyRelation.getRight();
			HashSet<FamilyRelation> targetRelationsToClean = targetPerson.getRelations();
			FamilyRelation[] relationArray = targetRelationsToClean.toArray(new FamilyRelation[targetRelationsToClean.size()]);
			for ( FamilyRelation targetRelation : relationArray) {
				if ( targetRelation.getRight() == remove ) {
					System.out.println(targetRelation.getLeft() + " : Need to remove " +  targetRelation + ":Right=" + targetRelation.getRight());
					Person originPerson = targetRelation.getLeft();
					{
						originPerson.removeRelation(targetRelation);
					}   
				}   
			}
		}
		DatasourceManager.getPersistenceManager().remove(remove);
	}

	/**
	 * Clear the database - NOT recommended.
	 */
	public static void clearDB() {
		DatasourceManager.getPersistenceManager().destroyData();
	}

	/**
	 * Get the total number of people in the database
	 * @return
	 */
	public static long getTotalCount() {
		return DatasourceManager.getPersistenceManager().getTotalCount();
	}

	/**
	 * Search for a person using a textual match.
	 * @param searchString
	 * @return
	 */
	public static ArrayList<Person> findPerson(String searchString) {
		ArrayList<Person> searchResults = DatasourceManager.getPersistenceManager().find(searchString);
		return searchResults;
	}

	/**
	 * Find the flat list of root nodes - i.e. people who dont have 
	 * @param searchString
	 * @return
	 */
	public static ArrayList<Person> findHeadOfFamilies() {
		ArrayList<Person> rootNodes = DatasourceManager.getPersistenceManager().getPrimaries();
		return rootNodes;
	}

   private static ArrayList<Person> processed = new ArrayList<>();

   public static int countRelatives( Person me) {
      int count = 1;
      processed.add( me);
      for (FamilyRelation relation : me.getRelations()) {
         
         if ( relation.getType() == Relationship.GRANDPARENT ) // TODO Dont need for now - configurable option. 
            continue;
         //
         // TODO : Dont need for now - configurable option.e.g. Parents of spouses.
         if ( relation.getType() == Relationship.FATHER || relation.getType() == Relationship.MOTHER )
            continue;
         
         Person right = relation.getRight();
         
         if ( ! processed.contains(right)) {
            count = count +   countRelatives(right);
         }
      }
      return count;
   }
   
	/**
	 * Recursively go through all the relations for this person and print a textual tree/graph
	 * @param me
	 * @param sourceRelation
	 * @param level
	 * @param html
	 * @return
	 */
	private static String traverseTree( Person me, FamilyRelation sourceRelation2, int level, String html) {
		level++;
		processed.add( me);
		String buildHtml ="<br>";

		StringBuffer padding = new StringBuffer();
		if ( level > 1 ) {
			padding.append("&nbsp;&nbsp;|");
		}
		for ( int i=0; i < level ; i++) {
			padding.append("&nbsp;&nbsp;");
		}

		padding.append("|___");

		String pad = padding.toString();
		for (FamilyRelation relation : me.getRelations()) {	
			Person right = relation.getRight();

			if ( relation.getType() == Relationship.SIBLING ) { // TODO Dont need for now - configurable option.
				if ( level > 1) // If we are the root - print all my siblings.
					continue;
			}

			if ( relation.getType() == Relationship.GRANDPARENT ) // TODO Dont need for now - configurable option. 
				continue;
			//
			// TODO : Since we will always climb up to the patriarch - we can skip the threads. This could be controlled with an option.
			if ( relation.getType() == Relationship.FATHER || relation.getType() == Relationship.MOTHER )
				continue;

			buildHtml = buildHtml +  pad + " " + relation.getType() + "===>"+ right.toDetails();

			// System.out.println( pad + " " + relation.getType() + "===>"+ right.toDetails());

			if ( relation.getType() == Relationship.SPOUSE && me == relation.getLeft()) { // Prevent recursive spouse adds.
				String spouseTree = getSpouseTree(right,pad,level);
				if ( ! "".equals( spouseTree) )
					buildHtml = buildHtml + spouseTree;
				else
					buildHtml = buildHtml + "<br>";
			}
			else 
			{
				buildHtml  = buildHtml + traverseTree(right, relation, level, html);
			}
		}
		return buildHtml;
	}
	
	private static String getSpouseTree(Person me,String pad, int level) {
		level++;
		StringBuffer padding = new StringBuffer();
		if ( level > 1 ) {
			padding.append("&nbsp;&nbsp;|");
		}
		for ( int i=0; i < level ; i++) {
			padding.append("&nbsp;&nbsp;");
		}

		padding.append("|___");
		String buildHtml ="<br>";
		boolean found = false;
		for (FamilyRelation relation : me.getRelations()) {	
			if ( relation.getType() == Relationship.SIBLING ) {
				
				Person right = relation.getRight();
				processed.add(right);
				buildHtml = buildHtml +  padding  + relation.getType() + "===>"+ right.toDetails() +"<br>";
				found=true;
			}
		}
		if ( found )
			return buildHtml;
		else 
			return "";
	}

	
	/**
	 * Print out the entire family tree. By default assume that "me" is the head of the family.
	 * @param me- Person who's family tree needs to be printed.
	 */
	public static String printFamilyTree(Person me) {
		return printFamilyTree( me,  false) ;
	}

	/**
	 * Print out the entire family tree. 
	 * @param me - Person who's family tree needs to be printed.
	 * @param deepTraverse - Specify if we want the entire tree ( going back to his ancestry ) - or just go below the current person.
	 */
	public static String printFamilyTree(Person me, boolean deepTraverse) {

		log.finest("Traversing the family tree for " + me + " deepFlag = " + deepTraverse );

		processed.clear();

		if ( deepTraverse ) {
			//
			// Since we want the complete tree. We will get the ancestor and then start with him/her as the head of the family.
			Person ancestor = me.getAncestor();
			if ( ancestor != null ) 
			{
				me = ancestor;
			}
		}

	   String html = "Family Tree<br>" + me.toDetails() +  FamilyTreeManager.traverseTree( me, null,  0, "");	
		System.out.println("\n====" + me.toDetails() + html.replaceAll("<br>", "\n").replaceAll("&nbsp;", " "));
		processed.clear();
		return html;
	}

/**
 * Create dummy data which then returns a count of the number of people in the db.
 * @return
 */
   public static long createTestData() {
      DatasourceManager.getPersistenceManager().destroyData();
      System.out.println("Creating test data");
      Person grandpasimpson = FamilyTreeManager.createPerson("Abraham", "Simpson",Sex.MALE);
      Person homer = FamilyTreeManager.createPerson("Homer", "Simpson",Sex.MALE);
      Person marge = FamilyTreeManager.createPerson("Marge", "Simpson", Sex.FEMALE);
      Person bart = FamilyTreeManager.createPerson("Bart", "Simpson", Sex.MALE);
      Person maggie = FamilyTreeManager.createPerson("Maggie", "Simpson", Sex.FEMALE);
      Person lisa = FamilyTreeManager.createPerson("Lisa", "Simpson", Sex.FEMALE);
      Person stewiegriffin = FamilyTreeManager.createPerson("Stewie", "Griffin", Sex.MALE);
      Person meggriffin = FamilyTreeManager.createPerson("Meg", "Griffin", Sex.FEMALE);
      Person daughterinlaw = FamilyTreeManager.createPerson("Lois", "Pewterschmidt", Sex.FEMALE);
      Person milhouse = FamilyTreeManager.createPerson("Milhouse", "Houten", Sex.MALE);
      Person wiggum = FamilyTreeManager.createPerson("Chief", "Wiggum", Sex.MALE);
      Person ralphwiggum = FamilyTreeManager.createPerson("Ralph", "Wiggum", Sex.MALE);
      Person nelsonmuntz = FamilyTreeManager.createPerson("Nelson", "Muntz", Sex.MALE);
      RelationshipManager.addRelationship(bart,daughterinlaw, Relationship.SPOUSE);

      RelationshipManager.addRelationship(homer,marge, Relationship.SPOUSE);
      RelationshipManager.addRelationship(homer, bart, Relationship.CHILD);
      RelationshipManager.addRelationship(homer, maggie, Relationship.CHILD);
      RelationshipManager.addRelationship(homer, lisa, Relationship.CHILD);
      RelationshipManager.addRelationship(wiggum,ralphwiggum, Relationship.CHILD);

      RelationshipManager.addRelationship(bart,stewiegriffin, Relationship.CHILD);
      RelationshipManager.addRelationship(bart,meggriffin, Relationship.CHILD);
      RelationshipManager.addRelationship(bart,FamilyTreeManager.createPerson("Chris", "Griffin", Sex.MALE), Relationship.CHILD);
      RelationshipManager.addRelationship(maggie,milhouse, Relationship.SPOUSE);
      RelationshipManager.addRelationship(lisa, nelsonmuntz, Relationship.SPOUSE);

      RelationshipManager.addRelationship(meggriffin,ralphwiggum, Relationship.SPOUSE  );

      RelationshipManager.addRelationship(meggriffin,FamilyTreeManager.createPerson("Eric", "Cartman", Sex.MALE), Relationship.CHILD  );
      RelationshipManager.addRelationship(homer,FamilyTreeManager.createPerson("Herb", "Simpson", Sex.MALE), Relationship.SIBLING   );
      Person selma = FamilyTreeManager.createPerson("Selma", "Bouvier", Sex.FEMALE);
      RelationshipManager.addRelationship(selma, FamilyTreeManager.createPerson("Selma Jr.", "Bouvier", Sex.FEMALE), Relationship.CHILD);
      RelationshipManager.addRelationship(marge,selma, Relationship.SIBLING   );
      RelationshipManager.addRelationship(marge,FamilyTreeManager.createPerson("Patty", "Bouvier", Sex.FEMALE), Relationship.SIBLING   );

      RelationshipManager.addRelationship(grandpasimpson,homer, Relationship.CHILD);

      Person mike = FamilyTreeManager.createPerson("Mike", "Myers",Sex.MALE);
      Person michelle = FamilyTreeManager.createPerson("Michelle", "Myers",Sex.FEMALE);
      Person junior = FamilyTreeManager.createPerson("Junior", "Myers",Sex.MALE);
      RelationshipManager.addRelationship(mike,michelle, Relationship.SPOUSE);
      RelationshipManager.addRelationship(mike,junior, Relationship.CHILD);

      //
      // House Stark
      Person eddardstark = FamilyTreeManager.createPerson("Eddard", "Stark", Sex.MALE);
      RelationshipManager.addRelationship(eddardstark,FamilyTreeManager.createPerson("Catelyn", "Stark", Sex.FEMALE), Relationship.SPOUSE   );
      Person robstark = FamilyTreeManager.createPerson("Rob", "Stark", Sex.MALE);
      RelationshipManager.addRelationship(eddardstark,robstark, Relationship.CHILD   );
      RelationshipManager.addRelationship(eddardstark,FamilyTreeManager.createPerson("Sansa", "Stark", Sex.FEMALE), Relationship.CHILD   );
      RelationshipManager.addRelationship(eddardstark,FamilyTreeManager.createPerson("Arya", "Stark", Sex.FEMALE), Relationship.CHILD   );
      RelationshipManager.addRelationship(eddardstark,FamilyTreeManager.createPerson("Bran", "Stark", Sex.MALE), Relationship.CHILD   );
      RelationshipManager.addRelationship(eddardstark,FamilyTreeManager.createPerson("Rickon", "Stark", Sex.MALE), Relationship.CHILD   );
      RelationshipManager.addRelationship(robstark,FamilyTreeManager.createPerson("Talisa", "Stark", Sex.FEMALE), Relationship.SPOUSE);
        
      DatasourceManager.getPersistenceManager().store();

      return DatasourceManager.getPersistenceManager().getTotalCount();
   }
	/**
	 * Command line tester
	 * @param args
	 */
	public static void main( String args[]) {    

		//if ( DatasourceManager.getPersistenceManager().getTotalCount() == 0) 
		{
			createTestData(); // This creates a person.db file in the system.
		}

		ArrayList<Person> findPerson;
		if ( args.length > 0 ) {
			findPerson = findPerson(args[0]);
		}
		else {
			findPerson = findPerson("Se.*");
		}
		
		for ( Person person : findPerson) {
			FamilyTreeManager.printFamilyTree(person);
			System.out.println( "Total relatives for " + person + " = " + FamilyTreeManager.countRelatives(person));
		}

		//      ArrayList<Person> headOfFamilies = findHeadOfFamilies();
		//      for ( Person person : headOfFamilies) {
		//         Person.printFamilyTree(person);
		//      }

	}

}
