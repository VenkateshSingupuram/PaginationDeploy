package PaginationServlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PaginationServlet2 extends HttpServlet {
	 private Connection con;
	    private Statement stmt;
	    private static final long serialVersionUID = 1L;

	    public PaginationServlet2() {
	        super();
	    }

	    @Override
	    public void init() throws ServletException {
	        try {
	            Class.forName("org.postgresql.Driver");
	            con = DriverManager.getConnection(
	                    "jdbc:postgresql://localhost:5432/ServletProgramDataBase", "postgres", "postgres");
	            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    @Override
	    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	        process(req, res);
	    }

	    @Override
	    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        process(request, response);
	    }

	    @Override
	    public void destroy() {
	        try {
	            if (stmt != null) stmt.close();
	            if (con != null) con.close();
	            System.out.println("Connection closed");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    public void process(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	    	
	    	
	        int pageNumber = 1; // Default to 1
	        int recordPerPage = 5;

	        String sPageNo = req.getParameter("pageno");
	        if (sPageNo != null && !sPageNo.isEmpty()) {
	            try {
	                pageNumber = Integer.parseInt(sPageNo);
	            } catch (NumberFormatException e) {
	                // Handle gracefully
	                pageNumber = 1;
	            }
	        }

	        try {
	            int startIndex = (pageNumber - 1) * recordPerPage;

	            ResultSet rs = stmt.executeQuery("SELECT * FROM userchoice LIMIT " + recordPerPage + " OFFSET " + startIndex);

	            // Generate Excel
	            Workbook workbook = new XSSFWorkbook();
	            Sheet sheet = workbook.createSheet("User Details");
	            int rowNum = 0;
	            Row headerRow = sheet.createRow(rowNum++);
	            headerRow.createCell(0).setCellValue("UserID");
	            headerRow.createCell(1).setCellValue("Username");
	            headerRow.createCell(2).setCellValue("Profession");
	            headerRow.createCell(3).setCellValue("Location");

	            while (rs.next()) {
	                Row row = sheet.createRow(rowNum++);
	                row.createCell(0).setCellValue(rs.getInt(1));
	                row.createCell(1).setCellValue(rs.getString(2));
	                row.createCell(2).setCellValue(rs.getString(3));
	                row.createCell(3).setCellValue(rs.getString(4));
	            }
	            
	            PrintWriter pw = res.getWriter();
	            pw.println("<html>");
	            pw.println("<head><title>User Details</title></head>");
	            pw.println("<body>");
	            pw.println("<h2>User Details</h2>");
	            pw.println("<form action=\"PaginationServlet\" method=\"get\">");
	            pw.println("<input type=\"hidden\" name=\"pageno\" value=\"" + pageNumber + "\">");
	            pw.println("<input type=\"submit\" value=\"Download Excel\">");
	            pw.println("</form>");
	            pw.println("</body>");
	            pw.println("</html>");

	            // Set content type and attachment header
	            res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	            res.setHeader("Content-Disposition", "attachment; filename=C:\\Users\\SKTS_Admin_02\\Desktop\\01-05-2024\\Desktop Files\\Spring\\pagination.xlsx");

	            // Write Excel to response
	            OutputStream outputStream = res.getOutputStream();
	            workbook.write(outputStream);
	            workbook.close();
	            outputStream.close();
	            

	            rs.close();
	           
	            pw.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

}
