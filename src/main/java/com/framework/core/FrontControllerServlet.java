package com.framework.core;

import java.io.*;
import java.lang.reflect.Method;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.framework.annotation.Apirest;

import java.util.*;
import com.framework.model.ModelAndView;

// ajout spring 
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class FrontControllerServlet extends HttpServlet {
    
    private Map<MapUrlMethod, Mapping> mapUrlMethod;
    private String prefixe = "" ;
    private String suffixe = "" ;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {

        ServletContext context = getServletContext();
        this.mapUrlMethod = (Map<MapUrlMethod, Mapping>) context.getAttribute("urlMappings");
        this.prefixe = (String) getServletContext().getInitParameter("view-prefixe") ;
        this.suffixe = (String) getServletContext().getInitParameter("view-suffixe") ;

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
                
                // Object result = meth.invoke(controllerInstance);

                Object result = null;

                WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());

                Class<?>[] parameterTypes = meth.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> type = parameterTypes[i];

                    if (org.springframework.context.ApplicationContext.class.isAssignableFrom(type)) {
                        parameters[i] = springContext;
                    } 
                    else if (springContext != null) {
                        try {
                            parameters[i] = springContext.getBean(type);
                        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                            parameters[i] = null; 
                        }
                    } 
                    else {
                        parameters[i] = null;
                    }
                }

                result = meth.invoke(controllerInstance, parameters);

                // Json jsonAnnotation = meth.getAnnotation(Json.class);
                // if (jsonAnnotation != null ) {
                //     res.setContentType("application/json;charset=UTF-8");
                //     res.getWriter().println(toJson(result));
                //     return;
                // }

                if (meth.isAnnotationPresent(Apirest.class)) {
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().println(toJson(result));
                    return;
                }

                res.setContentType("text/plain;charset=UTF-8");
                res.getWriter().println("URL demandée : " + path + " [" + method + "]");
                res.getWriter().println("Prefixe : " + prefixe);
                res.getWriter().println("Suffixe : " + suffixe);
                res.getWriter().println("Controller : " + resultUrlMethod.getControllerClass().getName());
                res.getWriter().println("Method : " + resultUrlMethod.getMethod().getName());

                if( result instanceof ModelAndView){
                    ModelAndView mv = ( ModelAndView) result ;
                    String urlSuivant = prefixe + mv.getUrlSuivant() + suffixe ;

                    if(mv.getList() != null){ 
                        for( Map.Entry<String, String> entry : mv.getList().entrySet()){
                            req.setAttribute(entry.getKey(),entry.getValue()) ;
                        }
                    }

                    req.getRequestDispatcher(urlSuivant).forward(req,res) ;
                    return ;
                }
                else{
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

    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append(toJson(String.valueOf(entry.getKey())))
                    .append(":")
                    .append(toJson(entry.getValue()));
                first = false;
            }
            return json.append("}").toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    json.append(",");
                }
                json.append(toJson(item));
                first = false;
            }
            return json.append("]").toString();
        }
        if (value.getClass().isArray()) {
            StringBuilder json = new StringBuilder("[");
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append(toJson(java.lang.reflect.Array.get(value, i)));
            }
            return json.append("]").toString();
        }
        if (value.getClass().getPackageName().startsWith("com.app.")) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Method getter : value.getClass().getMethods()) {
                if (getter.getName().startsWith("get")
                        && getter.getName().length() > 3
                        && getter.getParameterCount() == 0
                        && !getter.getName().equals("getClass")) {
                    try {
                        String property = Character.toLowerCase(getter.getName().charAt(3))
                                + getter.getName().substring(4);
                        if (!first) {
                            json.append(",");
                        }
                        json.append(toJson(property))
                            .append(":")
                            .append(toJson(getter.invoke(value)));
                        first = false;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalArgumentException("Impossible de sérialiser "
                                + value.getClass().getName(), e);
                    }
                }
            }
            return json.append("}").toString();
        }

        String text = value.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + text + "\"";
    }
}