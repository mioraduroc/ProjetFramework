package com.framework.core;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.framework.util.ClasseUtilitaire;
import java.util.Map;
    
import com.framework.model.ModelAndView ;

@WebListener // Cette annotation permet à Tomcat de détecter automatiquement le Listener    9342
public class FrontControllerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ClasseUtilitaire util = new ClasseUtilitaire();

        String packageController = context.getInitParameter("controller-package");
        if (packageController == null || packageController.isEmpty()) {
            packageController = "com.app.controller"; // Valeur par défaut
        }

        String annotationClass = "mg.itu.4231.annotation.Controller";

        try {
            System.out.println("[Framework] Début du scan des contrôleurs...");
            
            Map<MapUrlMethod, Mapping> mapUrlMethod = util.scanPackageEtMethod(annotationClass, packageController);
            
            // On enregistre la map dans le ServletContext pour qu'elle soit accessible partout
            context.setAttribute("urlMappings", mapUrlMethod);
            
            System.out.println("[Framework] Scan terminé avec succès. Mappings enregistrés.");
        } catch (Exception e) {
            System.err.println("[Framework] Erreur critique lors du scan initial :");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Optionnel : Nettoyage à la fermeture de l'application
    }
}