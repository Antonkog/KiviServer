package com.wezom.kiviremoteserver.service.communication;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;

import com.google.gson.Gson;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
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

    public static final String EXTRA_CHANGE_INPUT_ID = "com.wezom.kiviremoteserver.service.extra.change.input.id";


    public DelegatePlatformsService() {
        super("DelegatePlatformsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INPUTS_LIST.equals(action)) {
                sendInputsList();
            } else if (ACTION_CHANGE_INPUT.equals(action)) {
                final Integer param1 = intent.getIntExtra(EXTRA_CHANGE_INPUT_ID, 0);
                changeInput(param1);
            }
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
