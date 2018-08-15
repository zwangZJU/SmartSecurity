package com.wzlab.smartsecurity.utils;


import com.google.gson.Gson;
import com.wzlab.smartsecurity.po.Device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by wzlab on 2018/7/12.
 */

public class DataParser {

    public static String getData(String s, String defaultValue){
        if(s == null || s.equals("") || s.equals("null")){
            return defaultValue;
        }else {
            return s;
        }
    }

}
