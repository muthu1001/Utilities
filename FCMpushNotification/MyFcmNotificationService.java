package com.thanthi.dtnext.dtnextapplication.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.widget.RemoteViews;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thanthi.dtnext.dtnextapplication.MainActivity;
import com.thanthi.dtnext.dtnextapplication.DescriptionActivity;
import com.thanthi.dtnext.dtnextapplication.R;
import com.thanthi.dtnext.dtnextapplication.utils.Constant;
import com.thanthi.dtnext.dtnextapplication.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MyFcmNotificationService extends FirebaseMessagingService {

    String articleID;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String,String> extras = remoteMessage.getData();
        String msg = "";
        boolean flag;
        String imageUrl = null;

        if (extras != null){
            for (String key: extras.keySet()){
                if (key.equals("alert")) {
                    msg = extras.get(key);
                    flag = true;
                }
                if (key.equals("ArticleID")) {
                    articleID = extras.get(key);
                }
                if (key.equals("default")) {
                    msg = extras.get(key);
                    flag = false;
                }
                if (key.equals("ImageURL")) {
                    imageUrl = extras.get(key);
                }
            }
            if (articleID.equals("00000000-0000-0000-0000-000000000000000000") || articleID.equals(" ") || articleID == null) {
                imageUrl = "";
                articleID = "";
                flag = false;
            } else {
                flag = true;
            }
            sendNotification(msg, imageUrl, flag);
        }

    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        //String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.d("FCM Token refreshed", "Refreshed token: " + refreshedToken);
    }

    private void sendNotification(String message, String imageUrl, boolean flag) {

        final Intent intent;
        if(flag){
            intent = new Intent(this, DescriptionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("message", message);
            intent.putExtra("articleId",articleID);
            intent.putExtra("isFromPushNotification", true);
            new ImageAsync(intent, message, imageUrl, true).execute();

        }else{
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("FromPush",true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            sendNotification(intent, message, null, false);
        }

    }

    private void sendNotification(Intent intent, String message, Bitmap bitmap, boolean flag){
        int notificationId = (int) System.currentTimeMillis();
        String time = Utils.getCurrentTime();

        Uri sound_uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        int randomPIN = (int)(Math.random()*9000)+1000;
        PendingIntent contentIntent = PendingIntent.getActivity(this, randomPIN, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setTicker(Html.fromHtml(message))
                .setAutoCancel(true)
                .setSound(sound_uri)
                .setPriority(2);


        // intents
        Intent intent2 = new Intent(Intent.ACTION_SEND);
        intent2.setType("text/plain");
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent2.putExtra(Intent.EXTRA_SUBJECT, "DTNEXT App");
        intent2.putExtra(Intent.EXTRA_TEXT, String.valueOf(Html.fromHtml(message)) + "\n\n" +"Shared via DTNEXT News App");

        PendingIntent shareIntent = PendingIntent.getActivity(this, randomPIN, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        // collapsed view
        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.notification_collapsed);
        collapsedView.setTextViewText(R.id.textView, message);

        RemoteViews expandedView;

        if (flag){
            expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded_article);
            expandedView.setImageViewBitmap(R.id.contentImageView, bitmap);

        } else {
            expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);
        }
        expandedView.setTextViewText(R.id.textView, message);
        expandedView.setTextViewText(R.id.timeView, time);
        expandedView.setOnClickPendingIntent(R.id.shareButton, shareIntent);

        builder.setCustomContentView(collapsedView).setCustomBigContentView(expandedView);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.app_icon_transparent);
            builder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.blueDark));
        }
        else builder.setSmallIcon(R.mipmap.app_icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.notification_channel_id), getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) notificationManager.createNotificationChannel(notificationChannel);
        }

        builder.setContentIntent(contentIntent);
        if (notificationManager != null) notificationManager.notify(notificationId, builder.build());
    }

    private class ImageAsync extends AsyncTask<Void, Void, Void>{

        private Bitmap bitmap;
        private Intent intent;
        private String message;
        private String imageUrl;
        private boolean flag;

        ImageAsync(Intent intent, String message, String imageUrl, boolean flag) {
            this.intent = intent;
            this.message = message;
            this.imageUrl = imageUrl;
            this.flag = flag;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(imageUrl+ Constant.IMAGE_APPEND);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                bitmap = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            sendNotification(intent, message, bitmap, flag);
        }
    }

}