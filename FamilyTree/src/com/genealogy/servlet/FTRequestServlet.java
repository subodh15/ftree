package com.genealogy.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.genealogy.manager.FamilyTreeManager;
import com.genealogy.model.Person;

/**
 * Servlet implementation class FTRequestServlet
 */
public class FTRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FTRequestServlet() {
		super();

		//
		// Seed our database with some dummy data.
	//	if ( FamilyTreeManager.getTotalCount() < 1 )
		   FamilyTreeManager.createTestData();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String parameter = request.getParameter("searchstring");

		if ( parameter != null ) {
		   ArrayList<Person> findPerson = FamilyTreeManager.findPerson(parameter);

		   PrintWriter out = response.getWriter();
		   out.println("<html>");
		   out.println("<body>");
		   if ( findPerson.size() == 0 ) {
		      out.println("No results found");
		   }
		   for ( Person p : findPerson) {
		      String pp = FamilyTreeManager.printFamilyTree(p);
		      out.println(pp);
		   }
		   out.println("</body>");
		   out.println("</html>");
		}
		else {
		   ArrayList<Person> findPerson = FamilyTreeManager.findHeadOfFamilies();
         response.setContentType("application/json");
         StringBuffer members = new StringBuffer();
         members.append('[');
         int count = 0;
		   for ( Person p : findPerson) {
		      members.append("{\"firstname\":\""+ p.getName()+"\",\"lastname\":\""+ p.getLastName() +
		            "\",\"id\":\""+ p.getUID().toString() 
		            + "\", \"relations\":"+p.getRelations().size() + ",\"relatives\":\"" + p.getRelations().toString() + "\""
		            +"}"); //[{"firstname":"Abraham","lastname":"Simpson","id":1398096700363}]
		      count ++;
		      if ( count == findPerson.size() )
		         break;
		      else
		         members.append(',');
		   }
         members.append(']');
         
         System.out.println( members );
         response.getWriter().write( members.toString());
		}
		//
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}
