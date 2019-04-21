/*
 * Alex Dwivedi
 * 4/20/2019
 * CMSC 495
 */

package app.db;

import org.apache.derby.tools.ij;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Utilities {

   public static void printResults(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        System.out.println("Results");
        int columnsNumber = rsmd.getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = rs.getString(i);
                System.out.print(columnValue + " " + rsmd.getColumnName(i));
            }
            System.out.println();
        }
   }

    public static boolean runScript(File script, OutputStream os) {
        try (FileInputStream fs = new FileInputStream(script)) {
            return ij.runScript(Database.getConnection(), fs, "UTF-8", os, "UTF-8") == 0;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
