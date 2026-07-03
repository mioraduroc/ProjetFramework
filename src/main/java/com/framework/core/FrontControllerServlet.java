package com.framework.core;

import java.io.*;
import java.lang.reflect.Method;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.framework.core.Mapping;
import com.framework.core.MapUrlMethod;
import com.framework.util.ClasseUtilitaire;
import java.util.*;

public class FrontControllerServlet extends HttpServlet {
    ClasseUtilitaire util = new ClasseUtilitaire();
    Map<String,Mapping> routeMap = new HashMap<>();
    Map<MapUrlMethod, Mapping> mapUrlMethod = new HashMap<>();

    public void init() throws ServletException {
        try {
            this.routeMap = util.scanControllers("mg.itu.4231.annotation.Controller", "com.app.controller");

            this.mapUrlMethod = util.scanPackageEtMethod("mg.itu.4231.annotation.Controller", "com.app.controller") ;

        } catch (Exception e) {
            throw new ServletException("Erreur d'initialisation", e);
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
        String method = req.getMethod() ;

        if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
            RequestDispatcher dispatcher = req.getServletContext().getNamedDispatcher("default");
            dispatcher.forward(req, res);
            return;
        }

        res.setContentType("text/plain");
        res.getWriter().println("URL demandée : " + path);
        res.getWriter().println("Mapping trouvé via la Map ");
        
        Mapping matches = routeMap.get(path); 

        if (matches != null ) {
                res.getWriter().println("Controller : " + matches.getControllerClass().getName());
                res.getWriter().println("Method : " + matches.getMethod().getName());
        } else {
            res.getWriter().println("Aucun mapping trouvé pour cet url");
        }

        res.getWriter().println("URL demandée : " + path + " method : " + method );
        MapUrlMethod mp = new MapUrlMethod(path,method) ;

        if( mapUrlMethod == null){
             res.getWriter().println("mapUrlMethod null");
        }else {

            Mapping resultUrlMethod = mapUrlMethod.get(mp) ;
            if (resultUrlMethod != null ) {
                    try {
                        Object controllerInstance = resultUrlMethod
                            .getControllerClass()
                            .getDeclaredConstructor()
                            .newInstance();

                        Method meth = resultUrlMethod.getMethod();

                        meth.invoke(controllerInstance);

                        res.getWriter().println("Controller : " + resultUrlMethod.getControllerClass().getName());
                        res.getWriter().println("Method : " + resultUrlMethod.getMethod().getName());
                        res.getWriter().println(meth.invoke(controllerInstance));
                        
                    } catch (Exception e) {
                        throw new ServletException("Erreur lors de l'exécution du controller", e);
                    }
            } else {
                res.getWriter().println("Aucun mapping trouvé pour cet url");
            }

        }



    }
}