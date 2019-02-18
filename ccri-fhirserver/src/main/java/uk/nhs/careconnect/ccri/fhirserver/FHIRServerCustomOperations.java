package uk.nhs.careconnect.ccri.fhirserver;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("processForm")
public class FHIRServerCustomOperations extends HttpServlet{
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws IOException {
	        System.out.println("my custom operation");
}
}
