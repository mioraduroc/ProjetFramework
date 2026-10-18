package com.framework.core;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;

import com.framework.annotation.Apirest;
import com.framework.core.MapUrlMethod;
import com.framework.core.Mapping;

import java.util.*;
import com.framework.model.ModelAndView;
import com.framework.util.ClasseUtilitaire;

// ajout spring 
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 10, // 10 MB
        maxRequestSize = 1024 * 1024 * 50 // 50 MB
)
public class FrontControllerServlet extends HttpServlet {

    private Map<MapUrlMethod, Mapping> mapUrlMethod;
    private String prefixe = "";
    private String suffixe = "";

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {

        ServletContext context = getServletContext();
        this.mapUrlMethod = (Map<MapUrlMethod, Mapping>) context.getAttribute("urlMappings");
        this.prefixe = (String) getServletContext().getInitParameter("view-prefixe");
        this.suffixe = (String) getServletContext().getInitParameter("view-suffixe");

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

    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        String method = req.getMethod();

        if (path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
            req.getServletContext().getNamedDispatcher("default").forward(req, res);
            return;
        }

        MapUrlMethod mp = new MapUrlMethod(path, method);
        Mapping resultUrlMethod = mapUrlMethod.get(mp);

        if (resultUrlMethod != null) {

            Object result = null;

            try {
                Object controllerInstance = resultUrlMethod.getControllerClass().getDeclaredConstructor().newInstance();

                Method meth = resultUrlMethod.getMethod();

                int nombreParametresMeth = meth.getParameterCount();

                Object[] tabParametresMeth = new Object[nombreParametresMeth];

                if (nombreParametresMeth != 0) {

                    Parameter[] parametresMeth = meth.getParameters();

                    for (int i = 0; i < nombreParametresMeth; i++) {
                        Parameter param = parametresMeth[i];
                        String paramName = param.getName();
                        Class<?> paramType = param.getType();
                        String paramValueHttp = req.getParameter(paramName);

                        System.out.println("Paramètre : " + paramName + " | Type : " + paramType.getName() + " | Valeur HTTP : " + paramValueHttp);

                        boolean typeSimple = paramType == String.class
                                || paramType == int.class || paramType == Integer.class
                                || paramType == long.class || paramType == Long.class
                                || paramType == double.class || paramType == Double.class
                                || paramType == float.class || paramType == Float.class
                                || paramType == boolean.class || paramType == Boolean.class;
                        
                        System.out.println("Type simple : " + typeSimple);

                        tabParametresMeth[i] = typeSimple
                                ? ClasseUtilitaire.convertir(paramValueHttp, paramType)
                                : ClasseUtilitaire.construireObjet(paramType, req);
                                
                        System.out.println("Valeur convertie : " + tabParametresMeth[i]);
                    }
                }

                result = meth.invoke(controllerInstance, tabParametresMeth);

                if (meth.isAnnotationPresent(Apirest.class)) {
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().println(ClasseUtilitaire.toJson(result));
                    return;
                }

                res.setContentType("text/plain;charset=UTF-8");
                res.getWriter().println("URL demandée : " + path + " [" + method + "]");
                res.getWriter().println("Prefixe : " + prefixe);
                res.getWriter().println("Suffixe : " + suffixe);
                res.getWriter().println("Controller : " + resultUrlMethod.getControllerClass().getName());
                res.getWriter().println("Method : " + resultUrlMethod.getMethod().getName());

                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;
                    String urlSuivant = prefixe + mv.getUrlSuivant() + suffixe;

                    if (mv.getList() != null) {
                        for (Map.Entry<String, String> entry : mv.getList().entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    req.getRequestDispatcher(urlSuivant).forward(req, res);
                    return;
                } else {
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