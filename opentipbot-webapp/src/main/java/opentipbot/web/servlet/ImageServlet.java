package opentipbot.web.servlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Gilles Cadignan
 */
@WebServlet("/images/*")
public class ImageServlet extends HttpServlet {

    // content=blob, name=varchar(255) UNIQUE.
    private static final String SQL_FIND = "SELECT coin_addressqrcode FROM opentipbot_user WHERE id = ?";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getPathInfo().substring(1); // Returns "foo.gif". You may want to do nullchecks here to avoid NPE's.
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        DataSource dataSource = (DataSource) context.getBean("dataSource");
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SQL_FIND)) {
            statement.setLong(1, Long.parseLong(userId));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    byte[] content = resultSet.getBytes("coin_addressqrcode");
                    response.setContentType(getServletContext().getMimeType(userId));
                    response.setContentLength(content.length);
                    response.getOutputStream().write(content);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Something failed at SQL/DB level.", e);
        }
    }

}