package com.framework.core;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.framework.util.ClasseUtilitaire;
import java.util.*;

public class FrontControllerServlet extends HttpServlet {
    ClasseUtilitaire util = new ClasseUtilitaire();
    Map<String, List<Mapping>> routeMap = new HashMap<>();

    public void init() throws ServletException {
        try {
            this.routeMap = util.scanControllers("mg.itu.4231.annotation.Controller", "com.app.controller");
        } catch (Exception e) {
            throw new ServletException("Erreur d'initialisation du routage", e);
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
            RequestDispatcher dispatcher = req.getServletContext().getNamedDispatcher("default");
            dispatcher.forward(req, res);
            return;
        }

        res.setContentType("text/plain");
        res.getWriter().println("URL demandée : " + path);
        res.getWriter().println("Mapping trouvé via la Map ");
        
        List<Mapping> matches = routeMap.get(path); 

        if (matches != null && !matches.isEmpty()) {
            for (Mapping match : matches) {
                res.getWriter().println("Controller : " + match.getControllerClass().getName());
                res.getWriter().println("Method : " + match.getMethod().getName());
            }
        } else {
            res.getWriter().println("Aucun mapping trouvé pour cet url");
        }
    }
}