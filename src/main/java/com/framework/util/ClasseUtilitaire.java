package com.framework.util;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.core.Mapping;
import com.framework.core.MapUrlMethod;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import java.lang.reflect.Method;
import java.util.*;

public class ClasseUtilitaire {
    public ClasseUtilitaire() {}


    public Map<MapUrlMethod, Mapping> scanPackageEtMethod(String annotationValue, String packageClasse ) {
        Map<MapUrlMethod, Mapping> map = new HashMap<>();
        Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));
        Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : toutesLesClasses) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                
                if (controllerAnnotation.value().equals(annotationValue)) {

                    for (Method m : clazz.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(GetMapping.class)) {
                            String url = m.getAnnotation(GetMapping.class).value();
                            String method = m.getAnnotation(GetMapping.class).method() ;

                            MapUrlMethod mapUrlMethod = new MapUrlMethod(url,method);

                            if (map.containsKey(mapUrlMethod)) {
                                throw new RuntimeException(
                                    "Conflit de mapping détecté pour [" + method + " " + url + "]\n"
                                    + "Méthode existante : "
                                    + map.get(mapUrlMethod).getMethod().getName() + "\n"
                                    + "Nouvelle méthode : " + m.getName() + "\n"
                                    + "Classe : " + clazz.getName()
                                );
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
                            //    .add(new Mapping(clazz, m));
                            map.put(url, new Mapping(clazz, m));
                        }
                    }
                }
            }
        }
        return map;
    }




}