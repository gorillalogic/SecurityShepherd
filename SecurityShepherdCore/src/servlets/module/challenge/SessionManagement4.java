package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import utils.Hash;
import utils.ShepherdLogManager;

/**
 * Session Management Challenge Four
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * @author Mark Denihan
 *
 */
public class SessionManagement4 extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SessionManagement4.class);
	private static String levelName = "Session Management Challenge Four";
	private static String levelHash = "ec43ae137b8bf7abb9c85a87cf95c23f7fadcf08a092e05620c9968bd60fcba6";
	private static String levelResult = "238a43b12dde07f39d14599a780ae90f87a23e";
	/**
	 * Users must discover the session id for this sub application is very weak. The default session ID for a guest will be 00000001 base64'd. The admin's session will be 00000021
	 * @param upgraeUserToAdmin Red herring 
	 * @param returnPassword Red herring 
	 * @param adminDetected Red herring 
	 * @param checksum Cookie encoded base 64 that manages who is signed in to the sub schema
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		try
		{
			//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
			//Attempting to recover user name of session that made request
			try
			{
				if (request.getSession() != null)
				{
					HttpSession ses = request.getSession();
					String userName = (String) ses.getAttribute("decyrptedUserName");
					log.debug(userName + " accessed " + levelName + " Servlet");
				}
			}
			catch (Exception e)
			{
				log.debug(levelName + " Servlet Accessed");
				log.error("Could not retrieve user name from session");
			}
			Cookie userCookies[] = request.getCookies();
			int i = 0;
			Cookie theCookie = null;
			for(i = 0; i < userCookies.length; i++)
			{
				if(userCookies[i].getName().compareTo("SubSessionID") == 0)
				{
					theCookie = userCookies[i];
					break; //End Loop, because we found the token
				}
			}
			String htmlOutput = null;
			if(theCookie != null)
			{
				log.debug("Cookie value: " + theCookie.getValue());
				if(theCookie.getValue().equals("TURBd01EQXdNREF3TURBd01EQXdNUT09")) //Guest Session
				{
					log.debug("Guest Session Detected");
				}
				else if (theCookie.getValue().equals("TURBd01EQXdNREF3TURBd01EQXlNUT09")) //Admin Session
				{
					log.debug("Admin Session Detected: Challenge Complete");
					// Get key and add it to the output
					String userKey = Hash.generateUserSolution(levelResult, request.getCookies());
					htmlOutput = "<h2 class='title'>Admin Only Club</h2>" +
							"<p>" +
							"Welcome administrator. Your result key is as follows " +
							"<a>" + userKey + "</a>" +
							"</p>";
				}
				else //Unknown or Dead session
				{
					log.debug("Dead Session Detected");
				}
			}
			if(htmlOutput == null)
			{
				log.debug("Challenge Not Complete");
				boolean hackDetected = false;
				hackDetected = !(request.getParameter("useSecurity") != null && request.getParameter("userId") != null);
				if(!hackDetected)
				{
					log.debug("useSecurity: " + request.getParameter("useSecurity"));
					log.debug("userId: " + request.getParameter("userId"));
					hackDetected = !(request.getParameter("useSecurity").toString().equalsIgnoreCase("true"));
				}
				else
				{
					log.debug("Parameters Missing");
				}
				
				if(!hackDetected)
				{
					htmlOutput = "<h2 class='title'>Your not an Admin!!!</h2>" +
							"<p>" +
							"Stay away from the admin only section. The wolves have been released." +
							"</p>";
				}
				else
				{
					htmlOutput = "<h2 class='title'>HACK DETECTED</h2>" +
							"<p>" +
							"A possible attack has been detected. Functionality Stopped before any damage was done" +
							"</p>";
				}
			}
			log.debug("Outputting HTML");
			out.write(htmlOutput);
		}
		catch(Exception e)
		{
			out.write("An Error Occurred! You must be getting funky!");
			log.fatal(levelName + " - " + e.toString());
		}
	}
}
