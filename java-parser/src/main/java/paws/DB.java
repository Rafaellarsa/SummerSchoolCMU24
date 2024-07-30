package paws;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;

// 
// Decompiled by Procyon v0.5.30
// 

public class DB extends Export
{
    private Connection connParser;
    private boolean isConnParserValid;
    
    public DB(final String user, final String pass, final String port) {
        this.connectToParser("jdbc:mysql://" + port + "/parser", user, pass);
        this.type = "db";
    }
    
    public void connectToParser(final String url, final String userName, final String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connParser = DriverManager.getConnection(url, userName, password);
            this.isConnParserValid = true;
            System.out.println("Connected to the database parser.");
        }
        catch (Exception e) {
            System.out.println("Could not connect to parser!");
            e.printStackTrace();
        }
    }
    
    public boolean isConnectedToParser() {
        if (this.connParser != null) {
            try {
                if (!this.connParser.isClosed() & this.isConnParserValid) {
                    return true;
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    public void disconnectFromParser() {
        if (this.connParser != null) {
            try {
                this.connParser.close();
                this.connParser = null;
                System.out.println("Connection parser Closed.");
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void insertContentConcept(final String title, final String concept, final int sline, int eline) {
        String sqlCommand = "";
        String table = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        table = "ent_content_concept";
        if (sline > eline) {
            eline = sline;
        }
        try {
            sqlCommand = "select * from " + table + " where content_id = ? and concept = ? and sline = ? and eline =?";
            ps = this.connParser.prepareStatement(sqlCommand);
            ps.setString(1, title);
            ps.setString(2, concept);
            ps.setInt(3, sline + 1);
            ps.setInt(4, eline + 1);
            rs = ps.executeQuery();
            final boolean exists = rs.next();
            this.release(rs);
            this.release(ps);
            if (!exists) {
                sqlCommand = "insert into ent_content_concept (content_id,concept,sline,eline) values (?,?,?,?)";
                ps = this.connParser.prepareStatement(sqlCommand);
                ps.setString(1, title);
                ps.setString(2, concept);
                ps.setInt(3, sline + 1);
                ps.setInt(4, eline + 1);
                ps.executeUpdate();
                this.release(ps);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            this.release(rs);
            this.release(ps);
        }
    }
    
    private void release(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
                ps = null;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void release(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteConcept(final String question, final String[] conceptsToBeRemoved, final boolean isExample) {
        PreparedStatement ps = null;
        String sqlCommand = "";
        String concepts = "";
        final String table = "ent_content_concept";
        for (int i = 0; i < conceptsToBeRemoved.length; ++i) {
            concepts = String.valueOf(concepts) + "'" + conceptsToBeRemoved[i] + "'";
            if (i < conceptsToBeRemoved.length - 1) {
                concepts = String.valueOf(concepts) + ",";
            }
        }
        try {
            sqlCommand = "delete from " + table + " where concept in (" + concepts + ") and content_id = ?";
            ps = this.connParser.prepareStatement(sqlCommand);
            ps.setString(1, question);
            ps.executeUpdate();
            this.release(ps);
        }
        catch (SQLException e) {
            e.printStackTrace();
            this.release(ps);
        }
    }
}
