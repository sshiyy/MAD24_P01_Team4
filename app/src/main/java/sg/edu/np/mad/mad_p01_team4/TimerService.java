package sg.edu.np.mad.mad_p01_team4;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class TimerService extends Service {

    public static final String ACTION_UPDATE_WIDGET = "sg.edu.np.mad.mad_p01_team4.UPDATE_WIDGET";
    private static final int TOTAL_TIME = 10 * 60 * 1000; // 10 minutes in milliseconds
    private int timeRemaining = TOTAL_TIME;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (timeRemaining > 0) {
                timeRemaining -= 1000;
                int minutes = (timeRemaining / 1000) / 60;
                int seconds = (timeRemaining / 1000) % 60;
                String time = String.format("%02d:%02d", minutes, seconds);

                Log.d("TimerService", "Time : " + time);

                Intent intent = new Intent(ACTION_UPDATE_WIDGET);
                intent.putExtra("time", time);
                intent.putExtra("order_ready", false);
                sendBroadcast(intent);

                handler.postDelayed(this, 1000);
            } else {
                Log.d("TimerService", "Timer Completed");

                Intent intent = new Intent(ACTION_UPDATE_WIDGET);
                intent.putExtra("order_ready", true);
                sendBroadcast(intent);
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler.post(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
