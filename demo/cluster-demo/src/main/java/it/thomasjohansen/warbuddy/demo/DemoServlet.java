package it.thomasjohansen.warbuddy.demo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Collections.list;

/**
 * @author thomas@thomasjohansen.it
 */
@WebServlet(name = "DemoServlet", value = "/")
public class DemoServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        for (String parameter : list(request.getParameterNames()))
            request.getSession().setAttribute(parameter, request.getParameter(parameter));
    }

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        for (String sessionAttribute : list(request.getSession().getAttributeNames()))
            response.getWriter().println(sessionAttribute + "=" + request.getSession().getAttribute(sessionAttribute));
    }

}
