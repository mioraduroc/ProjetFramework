package com.framework.util;
import com.framework.annotation.Controller;

import java.util.List ;
import java.util.ArrayList;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import java.util.Set;

public class ClasseUtilitaire {
    public ClasseUtilitaire(){} ;

    public List<String> findController(String annotationValue,String packageClasse){
        List<String> liste = new ArrayList<>() ;

        Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));

        Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : toutesLesClasses) {
            if( clazz.isAnnotationPresent(Controller.class)){
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                String valeur = controllerAnnotation.value() ;

                if (valeur.equals(annotationValue)){
                    liste.add(clazz.getName()) ;
                }
            } 
        }
        return liste ;
    }
}
