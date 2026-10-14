package com.framework.core;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Field;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;

import com.framework.annotation.Apirest;
import com.framework.core.MapUrlMethod;
import com.framework.core.Mapping;

import java.util.*;
import com.framework.model.ModelAndView;
import com.framework.model.Mapfile;

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

                Map<String, String[]> mapParamUrl = req.getParameterMap();

                int nombreParametresMeth = meth.getParameterCount();

                Object[] tabParametresMeth = new Object[nombreParametresMeth];

                if (nombreParametresMeth != 0) {

                    Parameter[] parametresMeth = meth.getParameters();

                    for (int i = 0; i < nombreParametresMeth; i++) {
                        Parameter param = parametresMeth[i];
                        String paramName = param.getName();
                        Class<?> paramType = param.getType();

                        if (paramType != int.class && paramType != Integer.class && paramType != long.class
                                && paramType != Long.class &&
                                paramType != double.class && paramType != Double.class && paramType != float.class
                                && paramType != Float.class &&
                                paramType != boolean.class && paramType != Boolean.class && paramType != String.class) {

                            String className = paramType.getName();
                            System.out.println("[DEBUG FRAMEWORK] Paramètre de type objet détecté : " + className);

                            try {
                                Object paramObject = paramType.getDeclaredConstructor().newInstance();

                                Field[] champs = paramType.getDeclaredFields();
                                for (Field champ : champs) {
                                    champ.setAccessible(true);
                                    String nomChamp = champ.getName();
                                    Class<?> typeChamp = champ.getType();
                                    System.out.println("[DEBUG FRAMEWORK] Traitement du champ : " + nomChamp
                                            + " de type : " + typeChamp.getName());

                                    String valeurChampHttp = req.getParameter(nomChamp);
                                    if (valeurChampHttp != null) {
                                        if (typeChamp == int.class || typeChamp == Integer.class) {
                                            champ.set(paramObject, Integer.parseInt(valeurChampHttp));
                                        } else if (typeChamp == long.class || typeChamp == Long.class) {
                                            champ.set(paramObject, Long.parseLong(valeurChampHttp));
                                        } else if (typeChamp == double.class || typeChamp == Double.class) {
                                            champ.set(paramObject, Double.parseDouble(valeurChampHttp));
                                        } else if (typeChamp == float.class || typeChamp == Float.class) {
                                            champ.set(paramObject, Float.parseFloat(valeurChampHttp));
                                        } else if (typeChamp == boolean.class || typeChamp == Boolean.class) {
                                            champ.set(paramObject, Boolean.parseBoolean(valeurChampHttp));
                                        } else if (typeChamp == String.class) {
                                            champ.set(paramObject, valeurChampHttp);
                                        } else if ( typeChamp == Mapfile.class ) {
                                            System.out.println("[DEBUG FRAMEWORK] Traitement du champ de type Mapfile : " + nomChamp);
                                           Part filePart = req.getPart(nomChamp);
                                           if (filePart != null && filePart.getSize() > 0) {
                                               String fileName = filePart.getSubmittedFileName();
                                               String fileExtension = "";
                                               int dotIndex = fileName.lastIndexOf('.');
                                               if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                                                   fileExtension = fileName.substring(dotIndex + 1);
                                               }
                                               byte[] fileContent = filePart.getInputStream().readAllBytes();
                                               Mapfile mapfile = new Mapfile(fileName, fileExtension, fileContent);
                                               champ.set(paramObject, mapfile);
                                           } else {
                                               champ.set(paramObject, null);
                                           }
                                        } else {
                                            System.out.println("[DEBUG FRAMEWORK] Type de champ non pris en charge : "
                                                    + typeChamp.getName());
                                        }
                                        System.out.println("[DEBUG FRAMEWORK] Champ " + nomChamp
                                                + " rempli avec la valeur : " + valeurChampHttp);

                                        tabParametresMeth[i] = paramObject;

                                    } else {
                                        System.out
                                                .println("[DEBUG FRAMEWORK] Aucune valeur HTTP trouvée pour le champ : "
                                                        + nomChamp);
                                    }

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {

                            String paramValueHttp = req.getParameter(paramName);

                            System.out.println("[DEBUG FRAMEWORK] Paramètre recherché : '" + paramName
                                    + "' | Valeur HTTP trouvée : " + paramValueHttp);

                            if (paramValueHttp != null) {

                                if (paramType == int.class || paramType == Integer.class) {

                                    tabParametresMeth[i] = Integer.parseInt(paramValueHttp);
                                } else if (paramType == long.class || paramType == Long.class) {
                                    tabParametresMeth[i] = Long.parseLong(paramValueHttp);
                                } else if (paramType == double.class || paramType == Double.class) {
                                    tabParametresMeth[i] = Double.parseDouble(paramValueHttp);
                                } else if (paramType == float.class || paramType == Float.class) {
                                    tabParametresMeth[i] = Float.parseFloat(paramValueHttp);
                                } else if (paramType == boolean.class || paramType == Boolean.class) {
                                    tabParametresMeth[i] = Boolean.parseBoolean(paramValueHttp);
                                } else if (paramType == String.class) {
                                    tabParametresMeth[i] = paramValueHttp;
                                } else {
                                    tabParametresMeth[i] = null;
                                }

                            } else {
                                if (paramType == int.class || paramType == double.class || paramType == long.class
                                        || paramType == float.class) {
                                    tabParametresMeth[i] = 0;
                                } else if (paramType == boolean.class) {
                                    tabParametresMeth[i] = false;
                                } else {
                                    tabParametresMeth[i] = null;
                                }
                            }

                        }
                    }
                }

                result = meth.invoke(controllerInstance, tabParametresMeth);

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