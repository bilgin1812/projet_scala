/*
  Copyright (c) 2012, 2014 Oracle and/or its affiliates. All rights reserved.

  The MySQL Connector/J is licensed under the terms of the GPLv2
  <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most MySQL Connectors.
  There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
  this software, see the FLOSS License Exception
  <http://www.mysql.com/about/legal/licensing/foss-exception.html>.

  This program is free software; you can redistribute it and/or modify it under the terms
  of the GNU General Public License as published by the Free Software Foundation; version 2
  of the License.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this
  program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
  Floor, Boston, MA 02110-1301  USA

 */

package com.mysql.jdbc.authentication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.mysql.jdbc.AuthenticationPlugin;
import com.mysql.jdbc.Buffer;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ExportControlled;
import com.mysql.jdbc.Messages;
import com.mysql.jdbc.MySQLConnection;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Security;
import com.mysql.jdbc.StringUtils;

/**
 * MySQL Clear Password Authentication Plugin
 * 
 */
public class Sha256PasswordPlugin implements AuthenticationPlugin {

	private Connection connection;
	private String password = null;
	private String seed = null;
	private boolean publicKeyRequested = false;
	private String publicKeyString = null;

	public void init(Connection conn, Properties props) throws SQLException {
		this.connection = conn;
		
		String pkURL = connection.getServerRSAPublicKeyFile();
		if (pkURL != null) {
			this.publicKeyString = readRSAKey(this.connection, pkURL);
		}
	}

	public void destroy() {
		this.password = null;
		this.seed = null;
		this.publicKeyRequested = false;
	}

	public String getProtocolPluginName() {
		return "sha256_password";
	}

	public boolean requiresConfidentiality() {
		return false;
	}

	public boolean isReusable() {
		return true;
	}

	public void setAuthenticationParameters(String user, String password) {
		this.password = password;
	}

	public boolean nextAuthenticationStep(Buffer fromServer, List<Buffer> toServer) throws SQLException {
		toServer.clear();

		if (this.password == null || this.password.length() == 0 || fromServer == null) {
			// no password or changeUser()
			Buffer bresp = new Buffer(new byte[]{0});
			toServer.add(bresp);

		} else if (((MySQLConnection) connection).getIO().isSSLEstablished()) {
			// allow plain text over SSL
			Buffer bresp = new Buffer(StringUtils.getBytes(this.password));
			bresp.setPosition(bresp.getBufLength());
			int oldBufLength = bresp.getBufLength();
			bresp.writeByte((byte) 0);
			bresp.setBufLength(oldBufLength + 1);
			bresp.setPosition(0);
			toServer.add(bresp);

		} else if (this.connection.getServerRSAPublicKeyFile() != null) {
			// encrypt with given key, don't use "Public Key Retrieval"
			this.seed = fromServer.readString();
			Buffer bresp = new Buffer(encryptPassword(this.password, this.seed, this.connection, this.publicKeyString));
			toServer.add(bresp);

		} else {
			if (!this.connection.getAllowPublicKeyRetrieval()) {
				throw SQLError.createSQLException(
						Messages.getString("Sha256PasswordPlugin.2"),
						SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE,
						connection.getExceptionInterceptor());
			}
			
			// We must request the public key from the server to encrypt the password
			if (this.publicKeyRequested) {
				// read key response
				Buffer bresp = new Buffer(encryptPassword(this.password, this.seed, this.connection, fromServer.readString()));
				toServer.add(bresp);
				this.publicKeyRequested = false;
			} else {
				// build and send Public Key Retrieval packet
				this.seed = fromServer.readString();
				Buffer bresp = new Buffer(new byte[] { 1 });
				toServer.add(bresp);
				this.publicKeyRequested = true;
			}
		}
		return true;
	}

	private static byte[] encryptPassword(String password, String seed, Connection connection, String key) throws SQLException {
		byte[] input = StringUtils.getBytesNullTerminated(password != null ? password : "");
		byte[] mysqlScrambleBuff = new byte[input.length];
		Security.xorString(input, mysqlScrambleBuff, seed.getBytes(), input.length);
		return ExportControlled.encryptWithRSAPublicKey(
				mysqlScrambleBuff,
				ExportControlled.decodeRSAPublicKey(key, ((MySQLConnection) connection).getExceptionInterceptor()),
				((MySQLConnection) connection).getExceptionInterceptor());
	}
	
	private static String readRSAKey(Connection connection, String pkPath) throws SQLException {
		String res = null;
		byte[] fileBuf = new byte[2048];

		BufferedInputStream fileIn = null;

		try {
			File f = new File(pkPath);
			String canonicalPath = f.getCanonicalPath();
			fileIn = new BufferedInputStream(new FileInputStream(canonicalPath));

			int bytesRead = 0;

			StringBuffer sb = new StringBuffer();
			while ((bytesRead = fileIn.read(fileBuf)) != -1) {
				sb.append(StringUtils.toAsciiString(fileBuf, 0, bytesRead));
			}
			res = sb.toString();
			
		} catch (IOException ioEx) {

			if (connection.getParanoid()) {
				throw SQLError.createSQLException(
						Messages.getString("Sha256PasswordPlugin.0", new Object[]{""}),
						SQLError.SQL_STATE_ILLEGAL_ARGUMENT,
						connection.getExceptionInterceptor());
			}
			throw SQLError.createSQLException(
					Messages.getString("Sha256PasswordPlugin.0", new Object[]{"'"+pkPath+"'"}),
					SQLError.SQL_STATE_ILLEGAL_ARGUMENT,
					ioEx,
					connection.getExceptionInterceptor());

		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (Exception ex) {
					SQLException sqlEx = SQLError.createSQLException(
							Messages.getString("Sha256PasswordPlugin.1"),
							SQLError.SQL_STATE_GENERAL_ERROR, ex,
							connection.getExceptionInterceptor());

					throw sqlEx;
				}
			}
		}
		
		return res;
	}

}
