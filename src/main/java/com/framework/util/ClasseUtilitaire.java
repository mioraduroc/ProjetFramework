package com.framework.util;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Json;
import com.framework.core.Mapping;
import com.framework.core.MapUrlMethod;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import java.lang.reflect.Method;
import java.util.*;

public class ClasseUtilitaire {
    public ClasseUtilitaire() {
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