package wezom.kiviremoteserver.environment.bridge;

import android.util.Log;

import com.android.inputmethod.pinyin.util.Manufacture;
import com.realtek.tv.PQ;
import com.wezom.kiviremoteserver.environment.EnvironmentFactory;

import java.lang.reflect.Method;


public class BridgeGeneral {
    public static final int ENVIRONMENT = EnvironmentFactory.ENVIRONMENT_REALTEC;
   public static final Manufacture MANUFACTURE = Manufacture.REALTEK;

    public boolean isRUMarket() {
        String country = getProperty("ro.product.country");
        return country == null || !("UKRAINE".equalsIgnoreCase(country.trim())
                || "UA".equalsIgnoreCase(country.trim()));
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


    public void inStore(boolean isAdd) {
        new PQ().setLocationMode(isAdd ? 0 : 1);
    }

    public void onPause() {

    }
}
