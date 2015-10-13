/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.bsc.compss.ui.auth;

import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public class AuthenticationService {
	private static final Logger logger = Logger.getLogger("compssMonitor.autentication");
	
    public static UserCredential getUserCredential() {
    	Session session = Sessions.getCurrent();
    	return (UserCredential) session.getAttribute("userCredential");
    }

    public static boolean login(String username) {
    	logger.info("Login");
    	//Create user credential
    	UserCredential cred = new UserCredential(username);

    	//Verify user
    	if (cred.setAuthenticated()) {
    		//Correct user. Add to session
    		Session session = Sessions.getCurrent();
    		session.setAttribute("userCredential", cred);
    		//Successfull login
    		return true;
    	}
    	
    	//Invalid user
    	return false;
    }

    public static void logout() {
    	logger.info("Logout");
    	Session session = Sessions.getCurrent();
    	session.removeAttribute("userCredential");
    }
}