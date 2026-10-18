package com.framework.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Mapfile {
    private final String name;
    private final String extension ;
    private final byte[] content ;

    public Mapfile(String name, String extension, byte[] content) {
        this.name = name;
        this.extension = extension;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public byte[] getContent() {
        return content;
    }

    @Override 
    public String toString() {
        return "Mapfile{" +
                "name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                ", content length=" + (content != null ? content.length : 0) +
                '}';
    }

    public File saveToFile(String directoryPath) throws IOException {
        if ( content == null || content.length == 0 ){
            throw new IOException("Le contenu du fichier est vide.");
            // throw new IllegalArgumentException("Le contenu du fichier est vide.");
        }
        File dir = new File(directoryPath);

        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.mkdirs()) {
                throw new IOException("Impossible de créer le répertoire : " + directoryPath);
            }
        }

        String fullFIleName = this.name ;
        if ( this.extension != null && !this.extension.isEmpty() ) {
            fullFIleName += "." + this.extension ;
        }
        File destinationFile = new File(dir, fullFIleName);

        try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
            fos.write(this.content);
        }
        return destinationFile;
    }
}
