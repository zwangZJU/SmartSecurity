package com.wzlab.smartsecurity.activity.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



/**
 * Created by wzlab on 2018/6/2.
 */

public class Config {
    //liu 31.41
   // 云118.126.95.215
    //192.168.31.7
    //
   // public static final String IP = "118.126.95.215";
    public static final String IP = "60.190.23.22";
    public static final String PORT = "9090";
    public static final String DOMAIN_NAME = "http://"+IP+":"+PORT+"/zhaf/";
    public static final String SERVER_URL = DOMAIN_NAME + "api/do.jhtml?router=appApiService.";
    public static final String DEVICE_ICON_URL = DOMAIN_NAME + "myupload/device/";
    public static final String ADVERT_IMG_URL = DOMAIN_NAME + "myupload/advert/img";
    public static final String ADVERT_HTML_URL = DOMAIN_NAME + "myupload/advert/html";
    public static final String START_IMG_URL = DOMAIN_NAME + "myupload/start/start.jpg";
    public static final String TYPE_ROLE = "1";//普通用户

    public static final String ACTION_REGISTER = "userRegister";
    public static final String ACTION_LOGIN="userLogin";
    public static final String ACTION_FORGET_PASSWORD="forgetPwd";
    public static final String ACTION_GET_SMS_CODE="getSmsCode";
    public static final String ACTION_LOGOUT = "userLogout";
    public static final String ACTION_GET_DEVICE_DETAILS = "getDeviceDetails";
    public static final String ACTION_GET_DEVICE_LIST = "getDeviceList";
    public static final String ACTION_DEPLOY_DEFENSE = "deployDefense";
    public static final String ACTION_GRT_REPAIR_PROGRESS = "getRepairProgress";
    public static final String ACTION_GET_ALARM_LOG_LIST = "getAlarmLogList";
    public static final String ACTION_DEVICE_FAULT_REPORT = "deviceFaultReport";
    public static final String ACTION_GET_REPAIR_LOG_LIST = "getRepairLogList";
    public static final String ACTION_GET_USER_BASIC_INFO = "getUserBasicInfo";
    public static final String ACTION_UPLOAD_USER_AVATAR = "uploadUserAvatar";
    public static final String ACTION_BINDING_CAMERA = "bindingCamera";
    public static final String ACTION_SUPPLEMENT_CAMERA_INFO = "supplementCameraInfo";
    public static final String ACTION_DELETE_CAMERA = "deleteCamera";
    public static final String ACTION_GET_CAMERA_LIST = "getCameraList";
    public static final String ACTION_SOS = "sos";
    public static final String ACTION_UPLOAD_REAL_TIME_LOCATION = "uploadRealTimeLocation";
    public static final String ACTION_CANCEL_ALARM = "cancelAlarm";
    public static final String ACTION_SUBMIT_SUGGESTION = "submitSuggestion";
    public static final String ACTION_DELETE_DEVICE = "deleteDevice";
    public static final String ACTION_UPDATE_USER_BASIC_INFO = "updateUserBasicInfo";
    public static final String ACTION_GET_ADVERT_LIST = "getAdvertList";
    public static final String ACTION_CHECK_AND_UPDATE = "checkAndUpdate";
    public static final String TYPE_SMS_CODE_LOGIN = "2";
    public static final String TYPE_SMS_CODE_FORGET_PASSWORD = "1";
    public static final String TYPE_SMS_CODE_REGISTER = "0";

    public static final String KEY_ACTION = "action";
    public static final String KEY_TYPE = "type";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PHONE_MD5 = "phone_md5";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_NEWPASSWORD = "newPwd";
    public static final String KEY_SMS_CODE = "smsCode";
    public static final String KEY_SMS_SESSION_ID = "smsSessionId";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ROLE = "role";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_COMMAND = "cmd";
    public static final String KEY_LOCATION_LABEL = "loc_label";


    public static final int KEY_LOADING_ERROR = -1;
    public static final int KEY_LOADING_EMPTY = 0;
    public static final int KEY_LOADING_SUCCESS = 1;
    public static final int KEY_LOADING_LOADING = 2;
    public static final int KEY_FINISH_REFRESH = 3;


    public static final String RESULT_STATUS_SUCCESS = "1";
    public static final String RESULT_STATUS_FAIL = "0";
    public static final String RESULT_STATUS_INVALID_TOKEN = "2";
    public static final String RESULT_MESSAGE = "msg";
    public static final String LOGIN_BY_PASSWORD = "0";
    public static final String LOGIN_BY_SMS_CODE = "1";
    public static final int SCAN_QR_CODE_TO_ADD_DEVICE = 1001;
    public static final int SCAN_QR_CODE_TO_ADD_CAMERA = 1002;
    public static final String CHARSET = "utf-8";
    public static final String APP_ID = "com.wzlab.smartsecurity";



   public static void clearCache(Context context){
       SharedPreferences.Editor editor = context.getSharedPreferences(APP_ID, Context.MODE_PRIVATE).edit();

       editor.clear();
       editor.apply();
   }

    public static String getCachedToken(Context context){
        return context.getSharedPreferences(APP_ID,Context.MODE_PRIVATE).getString(KEY_TOKEN,null);
    }

    public static void cacheToken(Context context, String token){
        SharedPreferences.Editor editor = context.getSharedPreferences(APP_ID, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_TOKEN,token);
        editor.apply();
    }

    public static String getCachedPhone(Context context){
        return context.getSharedPreferences(APP_ID,Context.MODE_PRIVATE).getString(KEY_PHONE,null);
    }

    public static void cachePhone(Context context, String phone){
        SharedPreferences.Editor editor = context.getSharedPreferences(APP_ID, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_PHONE,phone);
        editor.apply();
    }

    public static String getCachedPassword(Context context){
        return context.getSharedPreferences(APP_ID,Context.MODE_PRIVATE).getString(KEY_PASSWORD,null);
    }

    public static void cachePassword(Context context, String password){
        SharedPreferences.Editor editor = context.getSharedPreferences(APP_ID, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_PASSWORD,password);
        editor.apply();
    }

    //main
    public static final String KEY_DATA = "data";
    public static final String KEY_ROW = "rows";
}
