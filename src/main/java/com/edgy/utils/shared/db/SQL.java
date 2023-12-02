package com.edgy.utils.shared.db;

import com.edgy.utils.EdgyUtils;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

public class SQL {

  /**
   * Connect to a MySQL-Style database.
   *
   * @param databaseType The database type.
   * @param host         The host of the database.
   * @param port         The port of the database.
   * @param database     The name of the database.
   * @param username     The username of the database.
   * @param password     The password of the database.
   * @return An SQL object. Or null if an error occurred.
   */
  public static SQL connect(
      DatabaseType databaseType,
      String host,
      int port,
      String database,
      String username,
      String password
  ) {
    try {
      if (databaseType == DatabaseType.SQLITE) {
        return sqlLite(EdgyUtils.dataFolder(), database + ".db");
      }
      return new SQL(databaseType, host, port, database, username, password);
    } catch (SQLException err) {
      err.printStackTrace();
      return null;
    }
  }

  /**
   * Connect to a MySQL database.
   *
   * @param host     The host of the database.
   * @param port     The port of the database.
   * @param database The name of the database.
   * @param username The username of the database.
   * @param password The password of the database.
   * @return An SQL object. Or null if an error occurred.
   */
  public static SQL mySQL(
      String host,
      int port,
      String database,
      String username,
      String password
  ) {
    try {
      return new SQL(DatabaseType.MYSQL, host, port, database, username, password);
    } catch (SQLException err) {
      err.printStackTrace();
      return null;
    }
  }

  /**
   * Connect to a SQLite database.
   *
   * @param dataFolder The data folder of the plugin.
   * @param fileName   The name of the database file.
   * @return An SQL object. Or null if an error occurred.
   */
  public static SQL sqlLite(
      File dataFolder,
      String fileName
  ) {
    try {
      return new SQL(DatabaseType.SQLITE, dataFolder, fileName);
    } catch (SQLException err) {
      err.printStackTrace();
      return null;
    }
  }

  /**
   * Connect to a MariaDB database.
   *
   * @param host     The host of the database.
   * @param port     The port of the database.
   * @param database The name of the database.
   * @param username The username of the database.
   * @param password The password of the database.
   * @return An SQL object. Or null if an error occurred.
   */
  public static SQL mariaDB(
      String host,
      int port,
      String database,
      String username,
      String password
  ) {
    try {
      return new SQL(DatabaseType.MARIADB, host, port, database, username, password);
    } catch (SQLException err) {
      err.printStackTrace();
      return null;
    }
  }

  private final DatabaseType databaseType;
  private final String connectionUrl;
  private final String username;
  private final String password;
  private Connection connection;

  private SQL(
      DatabaseType databaseType,
      String host,
      int port,
      String database,
      String username,
      String password
  ) throws SQLException {
    this.databaseType = databaseType;

    if (this.databaseType == DatabaseType.SQLITE) {
      throw new IllegalArgumentException("SQLLITE cannot be connected to remotely!");
    }

    this.connectionUrl =
        "jdbc:" + databaseType.name().toLowerCase() + "://" + host + ":" + port + "/" + database;
    this.username = username;
    this.password = password;
    connect();
  }

  private SQL(
      DatabaseType databaseType,
      File dataFolder,
      String fileName
  ) throws SQLException {
    this.databaseType = databaseType;
    if (this.databaseType != DatabaseType.SQLITE) {
      throw new IllegalArgumentException("Only SQLLLITE can be connected to using local files!");
    }

    this.connectionUrl =
        "jdbc:" + databaseType.name().toLowerCase() + ":" + dataFolder.getAbsolutePath() + "/"
            + fileName;
    this.username = null;
    this.password = null;
    connect();
  }

  private void connect() throws SQLException {
    switch (databaseType) {
      case MARIADB:
      case MYSQL:
        this.connection = DriverManager.getConnection(connectionUrl, username, password);
        break;
      case SQLITE:
        this.connection = DriverManager.getConnection(connectionUrl);
        break;
    }
  }

  @Nullable
  public PreparedStatement prepareStatement(String statement) {
    try {
      if (connection.isClosed()) {
        connect();
      }

      return connection.prepareStatement(statement);
    } catch (Exception err) {
      err.printStackTrace();
      return null;
    }
  }

  public ResultSet executeQuery(
      PreparedStatement stmt,
      Object... objects
  ) {
    try {
      if (connection.isClosed()) {
        connect();
      }

      for (int i = 1; i < objects.length; i++) {
        try {
          stmt.setObject(i, objects[i - 1]);
        } catch (SQLException err) {
          err.printStackTrace();
        }
      }

      return stmt.executeQuery();
    } catch (Exception err) {
      return null;
    }
  }

  public boolean executeUpdate(PreparedStatement stmt, Object... objects) {
    try {
      if (connection.isClosed()) {
        connect();
      }

      for (int i = 1; i < objects.length; i++) {
        try {
          stmt.setObject(i, objects[i - 1]);
        } catch (SQLException err) {
          err.printStackTrace();
        }
      }

      return stmt.executeUpdate() == 1;
    } catch (SQLException err) {
      err.printStackTrace();
    }

    return false;
  }

  public void close() {
    try {
      if (!connection.isClosed()) {
        connection.close();
      }
    } catch (Exception err) {
      return;
    }
  }

  public enum DatabaseType {
    MYSQL,
    SQLITE,
    MARIADB
  }

}
