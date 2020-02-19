package wezom.kiviremoteserver.environment.bridge;

import com.android.inputmethod.pinyin.util.Manufacture;
import com.android.inputmethod.pinyin.util.PropertyHelper;
import com.realtek.tv.PQ;
import com.realtek.tv.Tv;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.environment.EnvironmentFactory;


public class BridgeGeneral {
    public static final int ENVIRONMENT = EnvironmentFactory.ENVIRONMENT_REALTEC;
   public static final Manufacture MANUFACTURE = Manufacture.REALTEK;
    private static Tv rtkTV;

    public boolean isRUMarket() {
        String country = PropertyHelper.getProperty("ro.product.country");
        return country == null || !("UKRAINE".equalsIgnoreCase(country.trim())
                || "UA".equalsIgnoreCase(country.trim()));
    }


    public static Tv getTv() {
        if (rtkTV == null) {
            rtkTV = new Tv();
        }
        return rtkTV;
    }


    public static void checkHDMIStatus() {
        boolean old1 = App.hdmiStatus1;
        boolean old2 = App.hdmiStatus2;
        boolean old3 = App.hdmiStatus3;
        App.hdmiStatus1 = getTv().GetHDMIConnectionState(2);
        App.hdmiStatus2 = getTv().GetHDMIConnectionState(1);
        App.hdmiStatus3 = getTv().GetHDMIConnectionState(0);
        if (old1 != App.hdmiStatus1 || old2 != App.hdmiStatus2 ||
                old3 != App.hdmiStatus3) {
            int i = -1;
            if (old1 != App.hdmiStatus1 && App.hdmiStatus1) {
                i = 1;
            } else if (old2 != App.hdmiStatus2 && App.hdmiStatus2) {
                i = 2;
            } else if (old3 != App.hdmiStatus3 && App.hdmiStatus3) {
                i = 3;
            }
            App.hdmiStatusChanged(i);
        }
    }



    public void inStore(boolean isAdd) {
        new PQ().setLocationMode(isAdd ? 0 : 1);
    }

    public void onPause() {

    }
}
