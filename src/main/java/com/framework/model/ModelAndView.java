package com.framework.model;

import java.util.HashMap;

public class ModelAndView {
    private String urlSuivant ;
    private HashMap <String,String> list ;

    public String getUrlSuivant(){
        return urlSuivant ;
    }

    public void setUrlSuivant(String url){
        this.urlSuivant = url ;
    }

    public HashMap<String, String> getList(){
        return this.list ;
    }

    public void setList( HashMap<String,String> list){
        this.list = list ;
    }
}
