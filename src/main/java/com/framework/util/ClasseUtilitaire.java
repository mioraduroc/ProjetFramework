package com.framework.util;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Json;
import com.framework.core.Mapping;
import com.framework.model.MySession;
import com.framework.core.MapUrlMethod;
import com.framework.model.Mapfile;
import com.framework.model.MySession;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ClasseUtilitaire {
    public ClasseUtilitaire() {
    }

    public static Object convertir(String valeur, Class<?> type) {
        System.out.println("Conversion de la valeur : " + valeur + " vers le type : " + type.getName());
        if (valeur == null) {
            if (type == int.class) {
                System.out.println("valeur null pour type : " + type.getName());
                return 0;
            }
            if (type == long.class) {
                System.out.println("valeur null pour type : " + type.getName());
                return 0L;
            }
            if (type == double.class) {
                System.out.println("valeur null pour type : " + type.getName());
                return 0.0d;
            }
            if (type == float.class) {
                System.out.println("valeur null pour type : " + type.getName());
                return 0.0f;
            }
            if (type == boolean.class) {
                System.out.println("valeur null pour type : " + type.getName());
                return false;
            }

            System.out.println("valeur null pour type : " + type.getName());
            return null;
        }
        if (type == String.class) {
            System.out.println("valeur convertie : " + valeur);
            return valeur;
        }
        if (type == int.class || type == Integer.class) {
            System.out.println("valeur convertie : " + valeur);
            return Integer.parseInt(valeur);
        }
        if (type == long.class || type == Long.class) {
            System.out.println("valeur convertie : " + valeur);
            return Long.parseLong(valeur);
        }
        if (type == double.class || type == Double.class) {
            System.out.println("valeur convertie : " + valeur);
            return Double.parseDouble(valeur);
        }
        if (type == float.class || type == Float.class) {
            System.out.println("valeur convertie : " + valeur);
            return Float.parseFloat(valeur);
        }
        if (type == boolean.class || type == Boolean.class) {
            System.out.println("valeur convertie : " + valeur);
            return Boolean.parseBoolean(valeur);
        }
        System.out.println("Type non pris en charge : " + type.getName());
        return null;
    }

    public static Object construireObjet(Class<?> type, HttpServletRequest req) throws Exception {
        Object objet = type.getDeclaredConstructor().newInstance();

        System.out.println("Construction de l'objet de type : " + type.getName());

        for (Field champ : type.getDeclaredFields()) {
            champ.setAccessible(true);
            String nomChamp = champ.getName();
            Class<?> typeChamp = champ.getType();
            System.out.println("Traitement du champ : " + nomChamp + " | Type : " + typeChamp.getName());

            if (typeChamp == Mapfile.class) {
                System.out.println("le typeChamp est : " + typeChamp.getName());
                System.out.println("Traitement du champ Mapfile : " + nomChamp);
                
                champ.set(objet, construireMapfile(nomChamp, req));
                continue;
            }

            if (typeChamp == MySession.class) {
                System.out.println("le typeChamp est : " + typeChamp.getName());
                System.out.println("Traitement du champ MySession : " + nomChamp);

                MySession mySession = (MySession) req.getAttribute("mySession");
                if (mySession == null) {
                    throw new IllegalStateException("MySession n'est pas disponible dans la requête.");
                }
                champ.set(objet, mySession);
                continue;
            }

            String valeur = req.getParameter(nomChamp);
            if (valeur != null || estTypeSimple(typeChamp)) {
                champ.set(objet, convertir(valeur, typeChamp));
            } else {
                champ.set(objet, construireObjet(typeChamp, req));
            }
        }

        return objet;
    }

    private static boolean estTypeSimple(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == boolean.class || type == Boolean.class;
    }

    private static Mapfile construireMapfile(String nomChamp, HttpServletRequest req)
            throws Exception {
        jakarta.servlet.http.Part filePart = req.getPart(nomChamp);
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }

        String fileName = filePart.getSubmittedFileName();
        int dotIndex = fileName.lastIndexOf('.');
        String extension = dotIndex > 0 && dotIndex < fileName.length() - 1
                ? fileName.substring(dotIndex + 1)
                : "";
        return new Mapfile(fileName, extension, filePart.getInputStream().readAllBytes());
    }


    public static String toJson(Object value) {
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

public Map<MapUrlMethod, Mapping> scanPackageEtMethod(String annotationValue, String packageClasse) {
    Map<String, Class<?>> classesParAnnotation = new HashMap<>();
    
    Map<MapUrlMethod, Mapping> map = new HashMap<>();
    Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));
    Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

    for (Class<?> clazz : toutesLesClasses) {
        if (clazz.isAnnotationPresent(Controller.class)) {
            Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
            String valeurAnnotationActuelle = controllerAnnotation.value();

            if (valeurAnnotationActuelle.equals(annotationValue)) {

                if (classesParAnnotation.containsKey(valeurAnnotationActuelle)) {
                    Class<?> classeExistante = classesParAnnotation.get(valeurAnnotationActuelle);
                    
                    throw new RuntimeException(
                            "Conflit d'annotation détecté ! La valeur \"" + valeurAnnotationActuelle + "\" est déjà utilisée.\n"
                                    + "Contrôleur existant : " + classeExistante.getName() + "\n"
                                    + "Nouveau contrôleur en conflit : " + clazz.getName());
                }

                classesParAnnotation.put(valeurAnnotationActuelle, clazz);

                for (Method m : clazz.getDeclaredMethods()) {
                    String url = null;
                    String method = null;

                    if (m.isAnnotationPresent(GetMapping.class)) {
                        GetMapping mapping = m.getAnnotation(GetMapping.class);
                        url = mapping.value();
                        method = mapping.method();
                    } else if (m.isAnnotationPresent(Json.class)) {
                        Json mapping = m.getAnnotation(Json.class);
                        url = mapping.value();
                        method = mapping.method();
                    }

                    if (url != null) {

                        MapUrlMethod mapUrlMethod = new MapUrlMethod(url, method);

                        if (map.containsKey(mapUrlMethod)) {
                            throw new RuntimeException(
                                    "Conflit de mapping HTTP détecté pour [" + method + " " + url + "]\n"
                                            + "Méthode existante : " + map.get(mapUrlMethod).getMethod().getName() + "\n"
                                            + "Nouvelle méthode : " + m.getName() + "\n"
                                            + "Classe : " + clazz.getName());
                        }
                        map.put(mapUrlMethod, new Mapping(clazz, m));
                    }
                }
            }
        }
    }
    return map;
}
    public Map<String, Mapping> scanControllers(String annotationValue, String packageClasse) {
        Map<String, Mapping> map = new HashMap<>();
        Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));
        Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : toutesLesClasses) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);

                if (controllerAnnotation.value().equals(annotationValue)) {
                    for (Method m : clazz.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(GetMapping.class)) {
                            String url = m.getAnnotation(GetMapping.class).value();

                            // map.computeIfAbsent(url, k -> new ArrayList<>())
                            // .add(new Mapping(clazz, m));
                            map.put(url, new Mapping(clazz, m));
                        }
                    }
                }
            }
        }
        return map;
    }

}