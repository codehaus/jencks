/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.jencks.tranql;

import java.sql.SQLException;

import org.enhydra.jdbc.standard.StandardXADataSource;
import org.tranql.connector.AllExceptionsAreFatalSorter;
import org.tranql.connector.jdbc.AbstractXADataSourceMCF;

/**
 * @author Thierry Templier
 */
public class XAPoolDataSourceMCF extends AbstractXADataSourceMCF {

	public XAPoolDataSourceMCF() {
		super(new StandardXADataSource(),new AllExceptionsAreFatalSorter());
	}

	/**
	 * @see org.tranql.connector.UserPasswordManagedConnectionFactory#getUserName()
	 */
	public String getUserName() {
		return ((StandardXADataSource)xaDataSource).getUser();
	}

	/**
	 * @see org.tranql.connector.UserPasswordManagedConnectionFactory#getPassword()
	 */
	public String getPassword() {
		return ((StandardXADataSource)xaDataSource).getPassword();
	}

	/*
	 * 
	 */
	public void setDriverName(String driverName) {
		try {
			((StandardXADataSource)xaDataSource).setDriverName(driverName);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
    }

	public void setUrl(String url) {
		((StandardXADataSource)xaDataSource).setUrl(url);
	}

	public void setUser(String user) {
		((StandardXADataSource)xaDataSource).setUser(user);
	}

	public void setPassword(String password) {
		((StandardXADataSource)xaDataSource).setPassword(password);
	}

}
