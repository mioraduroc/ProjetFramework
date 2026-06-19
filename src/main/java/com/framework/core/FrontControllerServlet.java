package com.framework.core;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.List ;
import java.util.ArrayList ;

import com.framework.util.ClasseUtilitaire ;


public class FrontControllerServlet extends HttpServlet {
    ClasseUtilitaire util = new ClasseUtilitaire() ;
    List<String> listNomController = new ArrayList<>() ;

    public void init() throws ServletException {

        try {
            super.init();
            System.out.println("Démarrage du scan...");
            String annotationValue = "mg.itu.4231.annotation.Controller" ;
            String packageClasse = "main.java.com.app.controller" ;

            listNomController = util.findController(annotationValue,packageClasse) ;

        } catch (Exception e) {
            e.printStackTrace(); 
            throw new ServletException("Échec de l'initialisation : " + e.getMessage(), e);
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
            
        processRequest(req, res);
    }

    public void doPost( HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        
        processRequest(req, res);
    
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException , IOException {

            
        String url = req.getRequestURI();
        String context = req.getContextPath();
        String path = url.substring(context.length());

        if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
            res.setContentType("text/html");
            RequestDispatcher dispatcher = req.getServletContext().getNamedDispatcher("default");
            dispatcher.forward(req, res);
            return;
        }else{
                res.setContentType("text/plain");
                res.getWriter().println(url) ;
            }

            for (String nom : listNomController) {
                res.getWriter().println(nom) ;
            }
    }
}