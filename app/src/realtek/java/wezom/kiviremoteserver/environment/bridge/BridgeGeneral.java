package wezom.kiviremoteserver.environment.bridge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.realtek.tv.PQ;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.environment.EnvironmentFactory;

import java.lang.reflect.Method;


public class BridgeGeneral {
    public static final int ENVIRONMENT = EnvironmentFactory.ENVIRONMENT_REALTEC;

    public static boolean isOptimization = false;

    public static String getBrowsPkg() {
        return "com.android.browser";//"com.android.browser"
    }


    public boolean isRUMarket() {
        String country = getProperty("ro.product.country");
        return country != null && !"UKRAINE".equalsIgnoreCase(country.trim());

    }

    public static String getProperty(String value) {
        String result = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            result = (String) get.invoke(c, value);
        } catch (Exception e) {
            Log.e("brige", "getProperty " + value + " : " + e.getMessage());
            e.printStackTrace();
        }
        Log.v("brige", "getProperty " + value + " = " + result);

        return result;
    }

    public void voiceBtn(Activity activity) {
        //lastStartVoiceAssistent = System.currentTimeMillis();
        if (!App.isRUMarket()) {
            Intent intent2 = new Intent();
            intent2.putExtra("search_type", 1);
            intent2.setComponent(new ComponentName("com.google.android.katniss", "com.google.android.katniss.search.SearchActivity"));
            activity.startActivity(intent2);
        }
    }

    public void inStore(boolean isAdd) {
        new PQ().setLocationMode(isAdd ? 0 : 1);
    }

    public void onPause() {

    }
}
