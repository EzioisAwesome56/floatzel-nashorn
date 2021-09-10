package com.eziosoft.floatzel.nashorn;

import com.eziosoft.floatzel.Res.Files;

import java.io.InputStream;

public class Utils {

    public static InputStream getResourse(String path, String filename){
        return BasePlugin.class.getResourceAsStream(path + filename);
    }
}
