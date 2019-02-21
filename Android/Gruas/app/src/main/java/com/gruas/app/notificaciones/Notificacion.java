package com.gruas.app.notificaciones;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.couchbase.lite.Document;
import com.gruas.app.R;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.menu.Menu;
import com.gruas.app.servicio.VistaServicio;

public class Notificacion {
    private int notificationID;
    private static int NUM_SERVICIOS = 0;
    private Activity activity;
    private CharSequence ticker;
    private CharSequence contentTitle;
    private CharSequence contentText;
    private Intent intent;
    public final static String ACTION_NOTIFY = "OPEN_HISTORY";
    private static boolean MULTIPLE_NOTIFICATION = false;


    public Notificacion(int notificationID, CharSequence ticker, CharSequence contentTitle, CharSequence contentText, Activity activity) {
        this.notificationID = notificationID;
        this.ticker = ticker;
        this.contentTitle = contentTitle;
        this.contentText = contentText;
        this.activity = activity;
    }

    public void displayNotification(int num){
        intent = new Intent(activity, Menu.class);
        intent.setAction(ACTION_NOTIFY);
        MULTIPLE_NOTIFICATION = true;
        NUM_SERVICIOS += num;
        display();
    }

    public void displayNotification(Service s){
        if(MULTIPLE_NOTIFICATION) displayNotification(1);
        else {
            NUM_SERVICIOS++;
            intent = new Intent(activity, VistaServicio.class);
            MULTIPLE_NOTIFICATION = true;
            display();
        }
    }

    private void display(){
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        NotificationManager nm = (NotificationManager)activity.getSystemService(activity.NOTIFICATION_SERVICE);
        int num = (MULTIPLE_NOTIFICATION)? NUM_SERVICIOS : 1;


        Notification noti = new NotificationCompat.Builder(activity)
                .setContentIntent(pendingIntent)
                .setTicker(ticker)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                        //.setContentInfo(contentInfo)
                .setSmallIcon(R.drawable.ic_stat_grua)
                        //.addAction(0, "", pendingIntent)
                .setVibrate(new long[]{100, 250, 100, 500})
                .setNumber(num)
                .build();
        noti.sound = Uri.parse("android.resource://"
                + activity.getPackageName() + "/" + R.raw.alarma);

        nm.notify(notificationID, noti);
    }

    public static boolean isMULTIPLE_NOTIFICATION(){
        boolean bol = (NUM_SERVICIOS == 0)? false : true;
        if(bol) MULTIPLE_NOTIFICATION = false;
        NUM_SERVICIOS = 0;
        return bol;
    }

    public static void resetParametersNotification(){
        MULTIPLE_NOTIFICATION = false;
        NUM_SERVICIOS = 0;
    }
}
