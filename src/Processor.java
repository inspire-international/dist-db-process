package com.inspire.gabatto.client;

import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class Processor {

    public Processor() {}

    public static final String CRLF = "\r\n";
    private static final String SUCCESS = "Success" + CRLF;
    private static final String FAIL = "Fail" + "\r\n";;
    private static final String driverName = "com.mysql.jdbc.Driver";
    private static final String sqlListenerPortOnDocker = "5080"; // SQL Engine listener port
    private static String host = "";
    private static String port = "";
    private static String DB = "";
    private static String table = "";
    private static String filename = "";
    private static String hostOfmysql = "";
    private static String user = "";
    private static String password = "";
    private static String jdbcURL = "";

    /**
     * Developed at #654
     * @param args  1)-host  2)-port  3)-user 4)-password  5)-DB  6)-table  7)-filename
     */
    public static void main(java.lang.String[] args) {
        String returnValue = SUCCESS;
        String[] command = null;
        int start = 0, end=0;
        int noOfRetry = 0;
        final int maxNoOfRetry = 2; // # of attempt with different locations

        for (int i=0; i<args.length; ++i) {
            if ("-host".equals(args[i])) {
                host = args[++i];
            } else if ("-port".equals(args[i])) {
                port = args[++i];
            } else if ("-user".equals(args[i])) {
                user = args[++i];
            } else if ("-password".equals(args[i])) {
                password = args[++i];
            } else if ("-DB".equals(args[i])) {
                DB = args[++i];
            } else if ("-table".equals(args[i])) {
                table = args[++i];
            } else if ("-filename".equals(args[i])) {
                filename = args[++i];
            } else {
                System.err.println("Unknown arugment specified!");
            }
        }
        
        command = new String[]{"/bin/bash", "-c", "broklist " +  host + " " + port};
        while(true) {
            try {Thread.sleep(2000);}
            catch (InterruptedException e) {e.printStackTrace();}

            System.out.println("I am waiting ...");

            returnValue = execCommandLineWithRespnse(command); // Get registered DB from Broker
            start = returnValue.indexOf(DB, start);
            if (start == -1) continue;
            end = returnValue.indexOf(" ", DB.length() + 1 + start);
            if (end == -1) continue;

            hostOfmysql = returnValue.substring(start +  DB.length() + 1, end).trim(); // Substring hostname where MySQL is running.
            System.out.println(hostOfmysql);
            jdbcURL = "jdbc:mysql://" + hostOfmysql + ":" + sqlListenerPortOnDocker + "/" + DB; // Create JDBC URL
            System.out.println("Queue detected ...");
            returnValue = execSQLquery(); // Execute SQL query
            if (returnValue.contains(FAIL)) {
                System.out.println("SQL failed with <" + returnValue + ">");
                removeServerEntryOutOfBroker(host, port, DB, hostOfmysql, sqlListenerPortOnDocker); // Remove the MySQL entry out of Broker due to SQL error
                noOfRetry++;
                if (noOfRetry == maxNoOfRetry)
                    break;
            } else {
               System.out.println("Processed ... Please find " + filename);
               break;
            }
        }
    }

    private static class DBManager {

        public static Connection getConnection() throws ClassNotFoundException, SQLException {
            Connection con = null;
            Class.forName(driverName);
            con = DriverManager.getConnection(jdbcURL,user,password);
            System.out.println("Connecting to <" + jdbcURL + ">");
            return con;
        }
    }

    private static String execSQLquery() {
        String returnValue = SUCCESS;
        final String query = "SELECT * FROM " + table;
        final String resultFile = filename;
        FileWriter fw = null;
        Connection con = null;

        Vector<String> datas = new Vector<String>();
        try {
            con = DBManager.getConnection();
            Statement stmt = (Statement) con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            File file = new File(resultFile);
            fw = new FileWriter(file);
            ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
            int cnt = rsmd.getColumnCount();
            for (int idx = 1; idx <= cnt; idx++) {
                String colName = rsmd.getColumnName(idx);
                datas.add(colName);
            }
            writeData(fw, datas);
            while (rs.next()) {
                for (int idx = 1; idx <= cnt; idx++) {
                    String data = rs.getString(idx);
                    datas.add(data);
                }
                writeData(fw, datas);
            }
            fw.flush();
        } catch (SQLException sqle) {
            returnValue = FAIL + sqle.getMessage();
            System.err.println("SQL error occured:" + sqle.getMessage());
        } catch (IOException ioe) {
            returnValue = FAIL + ioe.getMessage();
            System.err.println("IOError occured:" + ioe.getMessage());
        } catch (ClassNotFoundException cnfe) {
            returnValue = FAIL + cnfe.getMessage();
            System.err.println("Class not found occured:" + cnfe.getMessage());
        } finally {
            try {
                if (fw != null) fw.close();
                if (con !=null ) con.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    protected static void writeData(FileWriter fw, Vector<String> datas) throws IOException {
        String buf = "";

        if (datas != null && !datas.isEmpty()) {
            for (int idx = 0; idx < datas.size(); idx++) {
                if (idx > 0) {
                    buf += ","; 
                }
                buf += datas.get(idx);
            }
            buf += "\n";
            fw.write(buf);
            fw.flush();
            datas.clear();
        }
    }

    public static void removeServerEntryOutOfBroker (final String brokerHost, final String brokerPort, final String serverName,
            final String serverHost, final String serverPort) {
        String[] commands = null;

        commands = new String[]{"/bin/bash", "-c", "broklist " +  brokerHost + " " + brokerPort + " " +
                serverName + "+"  + serverHost + "+" +  serverPort};
        execCommandLine(commands);
        return;
    }

    public static String execCommandLine(String[] command) {
        String returnValue = SUCCESS;
        ProcessBuilder pb = null;
        BufferedReader out = null;

        try {
            pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            pb.start();
        } catch (IOException ioe) {
            returnValue = FAIL + ioe;
            System.err.println(ioe.getMessage());
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException ioe) {
            }
        }
        return returnValue;
    }

    public static String execCommandLineWithRespnse(String[] command) {
        String returnValue = "";
        Process p = null;
        ProcessBuilder pb = null;
        BufferedReader out = null;
        String line = "";

        try {
            pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            p = pb.start();
            out = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = out.readLine();
            while(line != null) {
                returnValue += line + CRLF;
                line = out.readLine();
            }
            out.close();
        } catch (IOException ioex) {
            System.err.println(ioex.toString());
            returnValue = FAIL + ioex.toString();
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException ioe) {};
        }
        return returnValue;
    }

}
