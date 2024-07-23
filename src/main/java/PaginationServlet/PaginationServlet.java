package PaginationServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PaginationServlet extends HttpServlet {

	static String filePath = "C:\\Users\\SKTS_Admin_02\\eclipse-workspace\\Tomcat\\UserChoice1\\Properties_Files\\DBDetails.properties";

	Connection connection = null;
	Statement statement = null;
	PrintWriter printWriter;
	ResultSet resultSet1;
	ResultSet resultSet2;

	@Override
	public void init() throws ServletException {
		try {
			// DataBase Connections
			File file = new File(filePath);
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			String dbDriver = properties.getProperty("dbDriver");
			String url = properties.getProperty("url");
			String userName = properties.getProperty("userName");
			String passWord = properties.getProperty("passWord");

			Class.forName(dbDriver);
			connection = DriverManager.getConnection(url, userName, passWord);
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			// It can be Moved to Any Direction
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override // Override the Method getting the Data
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			process(req, res);
			connectionsClose();

		} catch (ServletException | IOException | SQLException e) {
			printWriter.print("Errorfor Server issues ,   could you Check Once....");
		}

	}

	public void process(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		int pageNumber = 0;
		int recordPerPage = 10;

		String startingPageNo = request.getParameter("pageno");
		if (startingPageNo != null && !startingPageNo.isEmpty()) {
			try {

				pageNumber = Integer.parseInt(startingPageNo);

			} catch (NumberFormatException e) {

				pageNumber = 1;
			}
		}

		try {
			int startIndex = (pageNumber - 1) * recordPerPage;

			resultSet1 = statement
					.executeQuery("SELECT * FROM userchoice LIMIT " + recordPerPage + " OFFSET " + startIndex);
			printWriter = response.getWriter();

			printWriter.println("<!DOCTYPE html>");
			printWriter.println("<html><head><title>Pagination Example</title></head><body>");
			printWriter.println("<center><table border=1 width = 60%>");
			printWriter.println("<tr>");
			printWriter.print("<h2>USER DETAILS</h2>");
			printWriter.println("<th>USER ID</th><th>NAME</th><th>PROFESSION</th><th>LOCATION</th>");
			printWriter.println("</tr>");
			while (resultSet1.next()) {
				printWriter.println("<tr>");
				printWriter.println("<td >" + resultSet1.getInt(1) + "</td>");
				printWriter.println("<td>" + resultSet1.getString(2) + "</td>");
				printWriter.println("<td>" + resultSet1.getString(3) + "</td>");
				printWriter.println("<td>" + resultSet1.getString(4) + "</td>");
				printWriter.println("</tr>");
			}
			printWriter.println("</table>");

			resultSet2 = statement.executeQuery("SELECT COUNT(*) FROM userchoice");
			resultSet2.next();
			int totalNumberOfRecords = resultSet2.getInt(1);
			int numberOfPages = (int) Math.ceil((double) totalNumberOfRecords / recordPerPage);
			printWriter.print("<br>");
			printWriter.println("<div>Page: ");
			for (int k = 1; k <= numberOfPages; k++) {
				printWriter.println("<a href=ps?pageno=" + k + ">" + k + "</a>");
			}

			printWriter.println("</div></center>");
			printWriter.print("<br>");

			printWriter.print("<br>");
			printWriter.print("<center><form action=Download method=POST>");
			printWriter.print("<input type=hidden name=pageno value=" + pageNumber+ ">" );
			printWriter.print("<input type=submit value=Download>");
			printWriter.print("</form></center>");

			printWriter.println("</body></html>");

		} catch (Exception e) {
			printWriter.print("Exception of the Programm...");
		} finally {
			resultSet1.close();
			resultSet2.close();
			printWriter.close();
		}
	}

	public void connectionsClose() { // Statement and DataBase Connections Closing.
		try {
			if (statement != null)
				statement.close();
			if (connection != null)
				connection.close();
		} catch (Exception e) {
			printWriter.print("Error for Closing Statements....");
		}
	}
}