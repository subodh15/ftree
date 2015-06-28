package com.genealogy.persistence;

public class DatasourceManager {

   static PersistanceManager persistenceManager = StaticDBImpl.getInstance();
   
   public static PersistanceManager getPersistenceManager() {
      return persistenceManager;
   }
}
