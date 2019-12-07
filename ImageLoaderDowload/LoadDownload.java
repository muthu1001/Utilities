package com.thanthi.dtnext.dtnextapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.comscore.Analytics;
import com.thanthi.dtnext.dtnextapplication.utils.Constant;
import com.thanthi.dtnext.dtnextapplication.utils.ImageSaver;
import com.thanthi.dtnext.dtnextapplication.utils.Utils;
import com.thanthi.dtnext.dtnextapplication.widget.TouchZoomImageView;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = ImageActivity.class.getSimpleName();

    Context context;
    TouchZoomImageView imageView;
    String imageUrl = "";

    private static final int STORAGE_REQUEST = 200;
    private static final int REQUEST_PERMISSION_SETTINGS = 201;
    private static final String IMAGE_DIRECTORY_NAME = "DTNEXT";
    private String articleId = "";
    private SharedPreferences permissionStatus;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);

        context = ImageActivity.this;
        Utils.getTracker(context, TAG);
        permissionStatus = getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);

        imageView = findViewById(R.id.imageView);

        Intent intent = getIntent();
        if (intent != null) {
            imageUrl = intent.getStringExtra("imageUrl");
            articleId = intent.getStringExtra("articleId");
        }

        imageView.setMaxZoom(3f);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.placeholder_error);
        requestOptions.placeholder(R.drawable.placeholder_error);
        Glide.with(context).setDefaultRequestOptions(requestOptions).load(imageUrl).into(imageView);

    }

    public void closeButton(View view) {
        finish();
    }

    public void downloadButton(View view) {
        if (checkPermission()) downloadAndStoreImage();
        else requestPermission();
    }

    private boolean checkPermission() {
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionRationaleDialog();
        else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false))
            permissionSettingsDialog();
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST);

        SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                downloadAndStoreImage();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    permissionRationaleDialog();
                } else {
                    Toast.makeText(context, "Unable to get Permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void permissionRationaleDialog() {
        //Show Information about why you need the permission
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Need Storage Permission");
        builder.setMessage("To save image in gallery, app requires storage permission to access storage.");
        builder.setPositiveButton("Grant", (dialog, which) -> {
            dialog.cancel();
            ActivityCompat.requestPermissions(ImageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST);
        });
        builder.setNegativeButton("Deny", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void permissionSettingsDialog() {
        //Previously Permission Request was cancelled with 'Dont Ask Again',
        //Redirect to Settings after showing Information about why you need the permission
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
        builder.setTitle("Need Storage Permission");
        builder.setMessage("To save image in gallery, app requires storage permission to access storage.");
        builder.setPositiveButton("Grant", (dialog, which) -> {
            dialog.cancel();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
            Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTINGS) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
                downloadAndStoreImage();
            }
        }
    }

    private void downloadAndStoreImage() {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        new ImageSaver(IMAGE_DIRECTORY_NAME, getFileName(), context, false).save(bitmap);
    }

    private String getFileName() {
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        //return "IMG_"+timeStamp+".jpg";
        return IMAGE_DIRECTORY_NAME + "_" + articleId + ".jpg";
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.notifyEnterForeground();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Analytics.notifyExitForeground();
    }

}
