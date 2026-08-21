package com.zyrotech.config;

import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {
    public AppConfig(){
        packages("com.zyrotech.controller");
        packages("com.zyrotech.middleware");
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
    }
}
