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

public final class Model {

  private static final Comparator<Uuid> UUID_COMPARE = new Comparator<Uuid>() {

    @Override
    public int compare(Uuid a, Uuid b) {

      if (a == b) { return 0; }

      if (a == null && b != null) { return -1; }

      if (a != null && b == null) { return 1; }

      final int order = Integer.compare(a.id(), b.id());
      return order == 0 ? compare(a.root(), b.root()) : order;
    }
  };

  private static final Comparator<Time> TIME_COMPARE = new Comparator<Time>() {
    @Override
    public int compare(Time a, Time b) {
      return a.compareTo(b);
    }
  };

  private static final Comparator<String> STRING_COMPARE = String.CASE_INSENSITIVE_ORDER;

  private final Store<Uuid, User> userById = new Store<>(UUID_COMPARE);
  private final Store<Time, User> userByTime = new Store<>(TIME_COMPARE);
  private final Store<String, User> userByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, Conversation> conversationById = new Store<>(UUID_COMPARE);
  private final Store<Time, Conversation> conversationByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Conversation> conversationByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, Message> messageById = new Store<>(UUID_COMPARE);
  private final Store<Time, Message> messageByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Message> messageByText = new Store<>(STRING_COMPARE);

  private final Uuid.Generator userGenerations = new LinearUuidGenerator(null, 1, Integer.MAX_VALUE);
  private Uuid currentUserGeneration = userGenerations.make();

  private final String database = "codeutest.db";
  private final String userTable = "USER";
  private final String sqliteClass = "org.sqlite.JDBC";

  public Model () {
    Connection conn = null; // Placeholder for connection to database
    Statement stmt = null; // Placeholder for sql command
    try {

      // Loads the JDBC class dynamically to handle the SQL connection
      // DriverManager cannot create the connection if this class isn't loaded at runtime
      Class.forName(sqliteClass);
      
      // Connect to database. Creates database if it doesn't exist.
      conn = DriverManager.getConnection("jdbc:sqlite:" + database);
      DatabaseMetaData dbm = conn.getMetaData();
      ResultSet rs = dbm.getTables(null, null, userTable, null);
       
      if (rs.next()) { // Table exists
       
        String sql = "SELECT ID, TIME, NAME FROM " + userTable;
        stmt = conn.createStatement();
        ResultSet users = stmt.executeQuery(sql);
        // loop through the result set
        while (users.next()) {
          
          String username = users.getString("NAME");
          String uuidString = users.getString("ID");
          uuidString = uuidString.substring(6, uuidString.length() - 1);
          
          try {
            Uuid useruuid = Uuid.parse(uuidString);
            User u = new User(useruuid, username, Time.fromMs(users.getLong("TIME"))); 
            restoreUser(u);
          } catch (IOException io) {
              System.out.println(io.getMessage());
          }
        }

        stmt.close();
        conn.close();  
      } else { // Table does not exist
          stmt = conn.createStatement();
          // Command to create USER table
          String sql = "CREATE TABLE   " + userTable +
          "(ID TEXT PRIMARY KEY     NOT NULL," +
          " TIME           BIGINT     NOT NULL, " + 
          " NAME           TEXT    NOT NULL" + 
          ")"; 
          stmt.executeUpdate(sql);
          stmt.close();
          conn.close();
        }
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
}

  public void executeSQL(Connection conn, String statement) {

  }

  public void add(User user) {
    currentUserGeneration = userGenerations.make();

    Connection conn = null; 
    Statement stmt = null;
    try {
      Class.forName(sqliteClass); 
      conn = DriverManager.getConnection("jdbc:sqlite:" + database); 
      stmt = conn.createStatement();
      String sql = "INSERT INTO " + userTable + " (ID,TIME,NAME) " +  
                   "VALUES ('"+ user.id.toString() + "', " + user.creation.inMs() + ", '" + user.name + "');"; 
      stmt.executeUpdate(sql);
      stmt.close();
      conn.close();
    } catch ( Exception e ) {
        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        System.exit(0);
    }
    
    userById.insert(user.id, user);
    userByTime.insert(user.creation, user);
    userByText.insert(user.name, user);
  }

  public void restoreUser(User user) {
    currentUserGeneration = userGenerations.make();
    userById.insert(user.id, user);
    userByTime.insert(user.creation, user);
    userByText.insert(user.name, user);
  }

  public StoreAccessor<Uuid, User> userById() {
    return userById;
  }

  public StoreAccessor<Time, User> userByTime() {
    return userByTime;
  }

  public StoreAccessor<String, User> userByText() {
    return userByText;
  }

  public Uuid userGeneration() {
    return currentUserGeneration;
  }

  public void add(Conversation conversation) {
    conversationById.insert(conversation.id, conversation);
    conversationByTime.insert(conversation.creation, conversation);
    conversationByText.insert(conversation.title, conversation);
  }

  public StoreAccessor<Uuid, Conversation> conversationById() {
    return conversationById;
  }

  public StoreAccessor<Time, Conversation> conversationByTime() {
    return conversationByTime;
  }

  public StoreAccessor<String, Conversation> conversationByText() {
    return conversationByText;
  }

  public void add(Message message) {
    messageById.insert(message.id, message);
    messageByTime.insert(message.creation, message);
    messageByText.insert(message.content, message);
  }

  public StoreAccessor<Uuid, Message> messageById() {
    return messageById;
  }

  public StoreAccessor<Time, Message> messageByTime() {
    return messageByTime;
  }

  public StoreAccessor<String, Message> messageByText() {
    return messageByText;
  }
}
