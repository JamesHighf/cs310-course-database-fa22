package edu.jsu.mcis.cs310;

import java.sql.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Database {
    
    private final Connection connection;
    
    private final int TERMID_SP22 = 1;
    
    /* CONSTRUCTOR */

    public Database(String username, String password, String address) {
        
        this.connection = openConnection(username, password, address);
        
    }
    
    /* PUBLIC METHODS */

    public String getSectionsAsJSON(int termid, String subjectid, String num) {     //this method searches for sections matching the subject ID, course number, and term ID
        
        String result = null;
        
        String query = "SELECT * FROM section WHERE termid=? AND subjectid=? AND num=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, termid);
            stmt.setString(2, subjectid);
            stmt.setString(3, num);
            
            if(stmt.execute()){
                ResultSet resultSet = stmt.getResultSet();
                result = getResultSetAsJSON(resultSet);
            }
            }
           
        catch (Exception e) { e.printStackTrace(); }
        
        
        return result;
        
    }
    
    public int register(int studentid, int termid, int crn) {      //this method registers for a section, inserting student ID, term ID, and CRN
        
        int result = 0;
        
        try
        {
        String query = "INSERT INTO registration (studentid,termid,crn) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, studentid);
        pstmt.setInt(2, termid);
        pstmt.setInt(3, crn);
        result = pstmt.executeUpdate();
        
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
//
        return result;
        
    }

    public int drop(int studentid, int termid, int crn) {       //this method drops a course record
        
        int result = 0;
        
        String query = "DELETE FROM registration WHERE studentid = (?) AND termid = (?) AND crn = (?)";
        try
        {
        PreparedStatement pstmt = this.connection.prepareStatement(query);
        pstmt.setInt(1, studentid);
        pstmt.setInt(2, termid);
        pstmt.setInt(3, crn);
        result = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
        
    }
    
    public int withdraw(int studentid, int termid) {   //this method withdraws from all courses
        
        int result = 0;
        
        
         try
        {
        String query = "DELETE FROM registration WHERE studentid = (?)";
        PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, studentid);
        result = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
        
    }
    
    public String getScheduleAsJSON(int studentid, int termid) {    //this method lists all sections for a given student and term ID
        
        String result = null;
        
       
        String query = "SELECT r.studentid, r.termid, s.scheduletypeid, s.instructor, s.num, s.start, s.days, s.section, s.end, s.where, s.crn, s.subjectid FROM registration AS r, section AS s, term AS t WHERE r.studentid=? AND r.termid=? AND r.crn=s.crn AND s.termid=t.id";
        PreparedStatement stmt;
        try {
        stmt = connection.prepareStatement(query);
        stmt.setInt(1, termid);
        stmt.setInt(2, termid);
        
        if(stmt.execute()){
            ResultSet resultSet = stmt.getResultSet();
        result = getResultSetAsJSON(resultSet);
        }
            }
        
           
        catch (Exception e) { e.printStackTrace(); }
        return result;
        
    }
    
    public int getStudentId(String username) {
        
        int id = 0;
        
        try {
        
            String query = "SELECT * FROM student WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, username);
            
            boolean hasresults = pstmt.execute();
            
            if ( hasresults ) {
                
                ResultSet resultset = pstmt.getResultSet();
                
                if (resultset.next())
                    
                    id = resultset.getInt("id");
                
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return id;
        
    }
    
    public boolean isConnected() {

        boolean result = false;
        
        try {
            
            if ( !(connection == null) )
                
                result = !(connection.isClosed());
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return result;
        
    }
    
    /* PRIVATE METHODS */

    private Connection openConnection(String u, String p, String a) {
        
        Connection c = null;
        
        if (a.equals("") || u.equals("") || p.equals(""))
            
            System.err.println("*** ERROR: MUST SPECIFY ADDRESS/USERNAME/PASSWORD BEFORE OPENING DATABASE CONNECTION ***");
        
        else {
        
            try {

                String url = "jdbc:mysql://" + a + "/jsu_sp22_v1?autoReconnect=true&useSSL=false&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=America/Chicago";
                // System.err.println("Connecting to " + url + " ...");

                c = DriverManager.getConnection(url, u, p);

            }
            catch (Exception e) { e.printStackTrace(); }
        
        }
        
        return c;
        
    }
    
    private String getResultSetAsJSON(ResultSet resultset) {
        
        String result;
        
        /* Create JSON Containers */
        
        JSONArray json = new JSONArray();
        JSONArray keys = new JSONArray();
        
        try {
            
            /* Get Metadata */
        
            ResultSetMetaData metadata = resultset.getMetaData();
            int columnCount = metadata.getColumnCount();
            
            while (resultset.next()) {
                JSONObject obj = new JSONObject();
                for (int i = 0; i < columnCount; i++) {
                    obj.put(metadata.getColumnLabel(i+1).toLowerCase(), resultset.getObject(i+1).toString());
                }
                json.add(obj);
            }
        
        }
        catch (Exception e) { e.printStackTrace(); }
        
        /* Encode JSON Data and Return */
        
        result = JSONValue.toJSONString(json);
        return result;
        
    }
    
}