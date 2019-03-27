package com.wezom.kiviremoteserver.service.communication;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.wezom.kiviremoteserver.service.AspectLayoutService;
import com.wezom.kiviremoteserver.service.AspectLayoutService;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DelegatePlatformsService extends IntentService {

    public static final String ACTION_INPUTS_LIST = "com.wezom.kiviremoteserver.service.action.getInputList";
    public static final String ACTION_CHANGE_INPUT = "com.wezom.kiviremoteserver.service.action.changeInput";
    public static final String ACTION_REMOTE_KEY_PRESSED = "com.wezom.kiviremoteserver.service.action.remote_pressed";


    public static final String EXTRA_KEY_ID = "com.wezom.kiviremoteserver.service.action.extra_key_id";
    public static final int KEY_FAST_LAUNCH_MENU = 420;
    public static final int KEY_TV = 170;
    public static final int KEY_MEDIA = 303;

    public static final String EXTRA_CHANGE_INPUT_ID = "com.wezom.kiviremoteserver.service.extra.change.input.id";

    private final String TV_APP_OLD = "com.android.tv";
    private final String TV_APP = "com.kivitvplayer";
    private final String MEDIA_APP = "com.rtk.mediabrowser";

    public DelegatePlatformsService() {
        super("DelegatePlatformsService");
    }

//            Intent kiviKeyService = new Intent();
//    kiviKeyService.setComponent(new ComponentName("com.wezom.kiviremoteserver",
//                  "com.wezom.kiviremoteserver.service.communication.DelegatePlatformsService"));
//    kiviKeyService.setAction("com.wezom.kiviremoteserver.service.action.remote_pressed");
//    kiviKeyService.putExtra("com.wezom.kiviremoteserver.service.action.extra_key_id",keyCode);// KEY_FAST_LAUNCH_MENU = 406;KEY_TV = 170;KEY_MEDIA = 303;
//    startService(kiviKeyService);


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.e("ACTION_INPUTS_LIST", "ACTION_INPUTS_LIST");
            final String action = intent.getAction();
            if (ACTION_INPUTS_LIST.equals(action)) {
                sendInputsList();
            } else if (ACTION_CHANGE_INPUT.equals(action)) {
                final Integer param1 = intent.getIntExtra(EXTRA_CHANGE_INPUT_ID, 0);
                changeInput(param1);
            } else if (ACTION_REMOTE_KEY_PRESSED.equals(action)) {
                int keyCode = intent.getIntExtra(EXTRA_KEY_ID, -1);
                if (keyCode >= 0) {
                    handleBtn(keyCode);
                }
            }
        }
    }

    private void handleBtn(int keyCode) {
        switch (keyCode) {
            case KEY_FAST_LAUNCH_MENU:
                startService(new Intent(getApplicationContext(), AspectLayoutService.class));
                break;
            case KEY_TV:
                Intent tvIntent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    tvIntent = getPackageManager().getLeanbackLaunchIntentForPackage(TV_APP);
                }
                if (tvIntent == null) {
                    tvIntent = getPackageManager().getLaunchIntentForPackage(TV_APP);
                }

                if (tvIntent == null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    tvIntent = getPackageManager().getLeanbackLaunchIntentForPackage(TV_APP_OLD);
                }
                if (tvIntent == null) {
                    tvIntent = getPackageManager().getLaunchIntentForPackage(TV_APP_OLD);
                }
                if (tvIntent != null) {
                    tvIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(tvIntent);
                }
                break;
            case KEY_MEDIA:
                Intent mediaIntent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    mediaIntent = getPackageManager().getLeanbackLaunchIntentForPackage(MEDIA_APP);
                }
                if (mediaIntent == null) {
                    mediaIntent = getPackageManager().getLaunchIntentForPackage(MEDIA_APP);
                }

                if (mediaIntent != null) {
                    mediaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mediaIntent);
                }
                break;
        }
    }


    private void sendInputsList() {
        Intent i = new Intent();

        List<InputItemJson> result = new ArrayList<>();
        for (InputSourceHelper.INPUT_PORT port : new InputSourceHelper().getPortsList(getBaseContext())) {
            result.add(new InputItemJson(port, getBaseContext()));
        }
        i.setComponent(new ComponentName("com.kivi.launcher_v2", "com.kivi.launcher_v2.services.CallbackIntentService"));
        i.putExtra("package", "com.kivi.launcher_v2");
        i.putExtra("service", "com.kivi.launcher_v2.sandbox.TestService");
        i.putExtra("requestCode", 112233);
        i.putExtra("responseAction", "sendToken");
        i.putExtra("result", new Gson().toJson(new InputDataJson(result)));
        i.setAction("requestToken");
        getApplicationContext().startService(i);
    }


    private void changeInput(int inputID) {
        new InputSourceHelper().changeInput(inputID, getBaseContext());
    }
}
