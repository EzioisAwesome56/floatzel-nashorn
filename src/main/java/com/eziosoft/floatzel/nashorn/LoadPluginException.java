package com.eziosoft.floatzel.nashorn;

public class LoadPluginException extends Exception {
    public LoadPluginException(String message){
        super("A fault has been detected while loading the plugin!\n" +
                "Error Message: "+ message);
    }
}
