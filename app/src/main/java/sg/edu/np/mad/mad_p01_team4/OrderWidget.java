package sg.edu.np.mad.mad_p01_team4;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import android.widget.RemoteViews;

public class OrderWidget extends AppWidgetProvider {

    private static TimerUpdateReceiver timerUpdateReceiver;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context, TimerService.class));
        // Update all widgets
        for (int appWidgetId : appWidgetIds) {
            Log.d("OrderWidget", "Updating widget ID: " + appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId, "00:00", false);
        }
    }

    @Override
    public void onEnabled(Context context) {
        if (timerUpdateReceiver == null) {
            timerUpdateReceiver = new TimerUpdateReceiver();
            IntentFilter filter = new IntentFilter(TimerService.ACTION_UPDATE_WIDGET);
            context.getApplicationContext().registerReceiver(timerUpdateReceiver, filter);
        }
    }

    @Override
    public void onDisabled(Context context) {
        if (timerUpdateReceiver != null) {
            context.getApplicationContext().unregisterReceiver(timerUpdateReceiver);
            timerUpdateReceiver = null;
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId, String time, boolean orderReady) {

        CharSequence widgetText = orderReady ? context.getString(R.string.order_ready_text) : String.format(context.getString(R.string.widget_text), time);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.order_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static class TimerUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String time = intent.getStringExtra("time");
            boolean orderReady = intent.getBooleanExtra("order_ready", false);
            Log.d("TimerUpdateReceiver", "Received update: time=" + time + ", orderReady=" + orderReady);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, OrderWidget.class));

            for (int appWidgetId : appWidgetIds) {
                if (orderReady) {
                    Log.d("OrderWidget", "Updating widget ID: " + appWidgetId + " with order ready");
                    OrderWidget.updateAppWidget(context, appWidgetManager, appWidgetId, "", true);
                    showOrderReadyNotification(context);
                } else {
                    Log.d("OrderWidget", "Updating widget ID: " + appWidgetId + " with time " + time);
                    OrderWidget.updateAppWidget(context, appWidgetManager, appWidgetId, time, false);
                }
            }
        }

        private void showOrderReadyNotification(Context context) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "order_ready_channel";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Order Ready Notifications", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.notification) // Replace with your notification icon
                    .setContentTitle("Order Ready")
                    .setContentText("Your order is ready for pickup!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            notificationManager.notify(1, builder.build());
        }
    }
}
