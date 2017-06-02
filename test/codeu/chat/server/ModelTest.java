// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.server;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import java.util.Comparator;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.IOException;

import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.LinearUuidGenerator;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;
import codeu.chat.util.store.StoreAccessor;



public final class ModelTest {

  private static final String DATABASE = "codeutest.db";
  private static final String USER_TABLE = "USER";
  private static final String SQLITE_CLASS = "org.sqlite.JDBC";
  private static final int UUID_SUBSTRING_START = 6;

  private Model model;

  @Before
  public void doBefore() {
    model = new Model();
  }

  @Test
  public void testModelConstructor() {
    model = new Model();
    Connection conn = null; // Placeholder for connection to DATABASE
    Statement stmt = null; // Placeholder for sql command
    try {

      // Loads the JDBC class dynamically to handle the SQL connection
      // DriverManager cannot create the connection if this class isn't loaded at runtime
      Class.forName(SQLITE_CLASS);
      
      // Connect to DATABASE. Creates DATABASE if it doesn't exist.
      conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
      DatabaseMetaData dbm = conn.getMetaData();
      ResultSet rs = dbm.getTables(null, null, USER_TABLE, null);
      
      assertTrue(
        "Check that the table exists/has been created.",
        rs.isBeforeFirst());
 
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }

  }

  @Test
  public void testAddUser() {

  }
}
