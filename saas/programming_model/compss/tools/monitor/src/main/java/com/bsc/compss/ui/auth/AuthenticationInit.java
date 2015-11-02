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

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;
import com.bsc.compss.ui.Constants;
import java.util.Map;

public class AuthenticationInit implements Initiator {
   
    @Override
    public void doInit(Page page, Map<String, Object> args) throws Exception {
    	UserCredential cred = AuthenticationService.getUserCredential();
	
    	if (cred == null || !cred.isAuthenticated()) {
    		Executions.sendRedirect(Constants.LOGIN_PAGE);
    		return;
    	}
    }
}