package com.framework.util;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.core.Mapping;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import java.lang.reflect.Method;
import java.util.*;

public class ClasseUtilitaire {
    public ClasseUtilitaire() {}

    public Map<String, List<Mapping>> scanControllers(String annotationValue, String packageClasse) {
        Map<String, List<Mapping>> map = new HashMap<>();
        Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));
        Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : toutesLesClasses) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                
                if (controllerAnnotation.value().equals(annotationValue)) {
                    for (Method m : clazz.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(GetMapping.class)) {
                            String url = m.getAnnotation(GetMapping.class).value();
                            
                            map.computeIfAbsent(url, k -> new ArrayList<>())
                               .add(new Mapping(clazz, m));
                        }
                    }
                }
            }
        }
        return map;
    }
}