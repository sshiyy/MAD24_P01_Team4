package sg.edu.np.mad.mad_p01_team4;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.RemoteViews;

public class OrderWidget extends AppWidgetProvider {

    private static TimerUpdateReceiver timerUpdateReceiver;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context, TimerService.class));
    }

    @Override
    public void onEnabled(Context context) {
        // Register the broadcast receiver
        if (timerUpdateReceiver == null) {
            timerUpdateReceiver = new TimerUpdateReceiver();
            IntentFilter filter = new IntentFilter(TimerService.ACTION_UPDATE_WIDGET);
            context.getApplicationContext().registerReceiver(timerUpdateReceiver, filter);
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Unregister the broadcast receiver
        if (timerUpdateReceiver != null) {
            context.getApplicationContext().unregisterReceiver(timerUpdateReceiver);
            timerUpdateReceiver = null;
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId, String time, boolean orderReady) {

        CharSequence widgetText = orderReady ? "Your order is ready" : "Your order will be ready in " + time;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.order_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static class TimerUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String time = intent.getStringExtra("time");
            boolean orderReady = intent.getBooleanExtra("order_ready", false);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, OrderWidget.class));
            for (int appWidgetId : appWidgetIds) {
                OrderWidget.updateAppWidget(context, appWidgetManager, appWidgetId, time, orderReady);
            }
        }
    }
}
