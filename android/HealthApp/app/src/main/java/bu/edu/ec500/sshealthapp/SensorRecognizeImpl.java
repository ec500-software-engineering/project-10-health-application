package bu.edu.ec500.sshealthapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class SensorRecognizeImpl {
    private boolean isInitialized = false;

    private SensorService.MyBinder myBinder;
    private Context ctx;

    private AppInitCallback initCallback;
    private AppResultCallback resultCallback;

    SensorRecognizeImpl(AppInitCallback callback, AppResultCallback resultCallback) {
        this.initCallback = callback;
        this.resultCallback = resultCallback;
    }

    public void init(Context context) {
        ctx = context;
        try {
            Intent bindIntent = new Intent(ctx, SensorService.class);
            ctx.bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        } catch (Throwable t) {
            isInitialized = false;
            AppResult result = new AppResult(-1, t.getLocalizedMessage());
            initCallback.onFailed(result);
        }
    }

    public void start() {
        try {
            Intent bindIntent = new Intent(ctx, SensorService.class);
            ctx.bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        } catch (Throwable t) {
            isInitialized = false;
            AppResult result = new AppResult(-1, t.getLocalizedMessage());
            initCallback.onFailed(result);
        }
        myBinder.startRecognizing();
    }

    public void stop() {
        myBinder.stopRecognizing();
        ctx.unbindService(connection);
    }

    public boolean isInitialized() { return isInitialized; }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (SensorService.MyBinder) service;
            MyClassifier myClassifier = new MyClassifier(ctx);
            //XGBoostClassifier myClassifier = new XGBoostClassifier(ctx);
            HumanActivityRecognizer humanActivityRecognizer = new HumanActivityRecognizer(myClassifier, resultCallback);
            myBinder.setRecognizer(humanActivityRecognizer);
            initCallback.onSucceeded();
            isInitialized = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isInitialized = false;
        }
    };
}
