package com.framework.model;

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

}
