package PaginationServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



public class Download extends HttpServlet {

    private static final String FILE_PATH = "C:\\Users\\SKTS_Admin_02\\eclipse-workspace\\Tomcat\\UserChoice1\\Properties_Files\\DBDetails.properties";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            doDownload(request, response);
        } catch (Exception e) {
        
            response.getWriter().print("An error occurred: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            doDownload(request, response);
        } catch (Exception e) {
            response.getWriter().print("An error occurred: " + e.getMessage());
        }
    }

    private void doDownload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ClassNotFoundException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Workbook workbook = null;
        PrintWriter printWriter =null;

        try (FileInputStream fileInput = new FileInputStream(new File(FILE_PATH))) {
            Properties properties = new Properties();
            properties.load(fileInput);

            String dbDriver = properties.getProperty("dbDriver");
            String url = properties.getProperty("url");
            String userName = properties.getProperty("userName");
            String passWord = properties.getProperty("passWord");

            Class.forName(dbDriver);
            connection = DriverManager.getConnection(url, userName, passWord);
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                                             // flexible navigation of the Result'sSet
            int pageNumber = Integer.parseInt(request.getParameter("pageno"));
            int startIndex = (pageNumber - 1) * 10;

            resultSet = statement.executeQuery("SELECT * FROM userchoice LIMIT 10 OFFSET " + startIndex);
                    // LIMIT 10 OFFSET " + startIndex  
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("User Details");

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Row headerRow = sheet.createRow(0);
            for (int i = 1; i <= columnCount; i++) {
                headerRow.createCell(i - 1).setCellValue(metaData.getColumnName(i));
            }

            int rowCount = 1;
            while (resultSet.next()) {
                Row row = sheet.createRow(rowCount++);
                for (int i = 1; i <= columnCount; i++) {
                    row.createCell(i - 1).setCellValue(resultSet.getString(i));
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=pagination.xlsx");

            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            printWriter.print("Exception of the program in doDownload Method,Can you Cross Check Once");
            throw new ServletException("An error occurred while creating the Excel file: " + e.getMessage(), e);  // It will Display the Exception for the Database
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { printWriter.print("Exception of the ResultSet it will throwing the DataBase Exception"); }
            if (statement != null) try { statement.close(); } catch (SQLException e) { printWriter.print("Error for the Statement Because Not Closing Properly"); }
            if (connection != null) try { connection.close(); } catch (SQLException e) { printWriter.print("Connection not Closing Properlly"); }
            if (workbook != null) try { workbook.close(); } catch (IOException e) { printWriter.print("Workbook Error"); }
        }
    }
}