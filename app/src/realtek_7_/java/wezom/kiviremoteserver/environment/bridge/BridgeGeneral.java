package wezom.kiviremoteserver.environment.bridge;

import com.android.inputmethod.pinyin.util.Manufacture;
import com.android.inputmethod.pinyin.util.PropertyHelper;
import com.realtek.tv.PQ;
import com.wezom.kiviremoteserver.environment.EnvironmentFactory;


public class BridgeGeneral {
    public static final int ENVIRONMENT = EnvironmentFactory.ENVIRONMENT_REALTEC;
   public static final Manufacture MANUFACTURE = Manufacture.REALTEK;

    public boolean isRUMarket() {
        String country = PropertyHelper.getProperty("ro.product.country");
        return country == null || !("UKRAINE".equalsIgnoreCase(country.trim())
                || "UA".equalsIgnoreCase(country.trim()));
    }

    public static void checkHDMIStatus() {

    }

    public void inStore(boolean isAdd) {
        new PQ().setLocationMode(isAdd ? 0 : 1);
    }

    public void onPause() {

    }
}
