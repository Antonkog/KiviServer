package wezom.kiviremoteserver.environment.bridge;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.wezom.kiviremoteserver.environment.EnvironmentFactory;

public class BridgeGeneral {
    public static final int ENVIRONMENT = EnvironmentFactory.ENVIRONMENT_MTC;
    public static boolean isOptimization = true;
    private AppWidgetManager mAppWidgetManager;
    private AppWidgetHost mAppWidgetHost;
    private Handler handler = new Handler();
    private Runnable runnable;

    public static String getBrowsPkg() {
        return "com.android.browser";
    }

    public void mainOnResume(Context context) {
        Settings.System.putInt(context.getContentResolver(), "previous_input_source", TvCommonManager.INPUT_SOURCE_STORAGE);
        try {
            TvManager.getInstance().setEnvironment("kivi_video", "off");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (Settings.System.getInt(context.getContentResolver(), "shop_mode", 0) != 0) {
                runnable = () -> {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.wezom.kivilauncher", "com.mobile.wezom.kivilauncher.services.Alarm"));
                    intent.putExtra("isHaveSignal", false);
                    context.startService(intent);
                };
                handler.postDelayed(runnable, 1000 * 90);

            }

        } catch (Exception e) {
        }
    }

    public boolean isRUMarket() {
        String Country = null;
        int Index = 51;
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/system/model/model.db",
                null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("select * from  build_info where device_model=?", new String[]{android.os.Build.MODEL});

        if (cursor.moveToFirst()) {

            int id = cursor.getColumnIndex("country");
            Country = cursor.getString(id);
            cursor.close();
            Log.i("country", "" + Country);
            if (Country.equalsIgnoreCase("RU")) {
                Index = 19;
                return true;
            }
        }

        return false;

    }

    public void inStore(boolean isAdd) {

    }

    public void voiceBtn(Activity activity) {

    }

    public static void checkHDMIStatus() { }

    public void onPause() {
        try {
            if (runnable != null)
                handler.removeCallbacks(runnable);
        } catch (Exception e) {
        }
    }
}
