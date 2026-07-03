package com.framework.core;

import java.io.*;
import java.lang.reflect.Method;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;

public class FrontControllerServlet extends HttpServlet {
    
    private Map<MapUrlMethod, Mapping> mapUrlMethod;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        // On récupère la map partagée qui a été créée par le Listener
        ServletContext context = getServletContext();
        this.mapUrlMethod = (Map<MapUrlMethod, Mapping>) context.getAttribute("urlMappings");

        if (this.mapUrlMethod == null) {
            throw new ServletException("Le mapping des URLs n'a pas été initialisé par le Listener.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        String method = req.getMethod();

        if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
            req.getServletContext().getNamedDispatcher("default").forward(req, res);
            return;
        }

        res.setContentType("text/plain;charset=UTF-8");
        
        MapUrlMethod mp = new MapUrlMethod(path, method);
        Mapping resultUrlMethod = mapUrlMethod.get(mp);

        if (resultUrlMethod != null) {
            try {
                Object controllerInstance = resultUrlMethod.getControllerClass().getDeclaredConstructor().newInstance();

                Method meth = resultUrlMethod.getMethod();
                
                Object result = meth.invoke(controllerInstance);

                res.getWriter().println("Controller : " + resultUrlMethod.getControllerClass().getName());
                res.getWriter().println("Method : " + resultUrlMethod.getMethod().getName());
                
                if (result != null) {
                    res.getWriter().println("Retour : " + result.toString());
                }

            } catch (Exception e) {
                throw new ServletException("Erreur lors de l'exécution du contrôleur", e);
            }
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().println("Aucun mapping trouvé pour l'URL : " + path + " [" + method + "]");
        }
    }
}