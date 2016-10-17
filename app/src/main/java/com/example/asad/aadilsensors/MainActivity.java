package com.example.asad.aadilsensors;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView t1;
    TextView t2;
    TextView t3;
    private SensorManager mSensorManager;
    private float[] ref = new float[3];
    DecimalFormat df;
    float[] current = new float[3];
    float[] actual = new float[3];
    private float[] SensorVals;
    List<float[]> valuesList = new ArrayList<>();
    String currentTimestamp;
    int counter = 0;
    String imageName;
    NotificationManager manager;
    Notification myNotication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        t1 = (TextView) findViewById(R.id.a1);
        t2 = (TextView) findViewById(R.id.a2);
        t3 = (TextView) findViewById(R.id.a3);
        df = new DecimalFormat("#.#");
        ref[0] = 0;
        ref[1] = 0;
        ref[2] = 0;

        float[] temp = {0, 0, 0};
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);
        valuesList.add(temp);


        Button calibrate = (Button) findViewById(R.id.calibrate);
        Button reset = (Button) findViewById(R.id.reset);
        Button camera = (Button) findViewById(R.id.camera);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera(v);
            }
        });


        assert calibrate != null;
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ref[0] = actual[0];
                ref[1] = actual[1];
                ref[2] = actual[2];
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

                currentTimestamp = dateFormat.format(new Date());

                counter = 0;

//                imageName = currentTimestamp + String.valueOf(++counter);
//                generateFile(MainActivity.this, imageName, "AAA");


            }
        });

        assert reset != null;
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref[0] = 0;
                ref[1] = 0;
                ref[2] = 0;
            }
        });

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .width(85)  // width in px
                .height(85) // height in px
                .fontSize(24)
                .endConfig()

                .buildRect("Pitch", Color.BLACK);


        Bitmap icon = drawableToBitmap(drawable);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(MainActivity.this);
        builder.setAutoCancel(false);
        builder.setSmallIcon(Icon.createWithBitmap(icon));
        builder.setOngoing(true);
        builder.setContentTitle("Yaw");
        builder.setContentText(String.valueOf(current[0]));
        builder.build();

        myNotication = builder.getNotification();
        manager.notify(11, myNotication);

        Notification.Builder builder2 = new Notification.Builder(MainActivity.this);
        builder.setAutoCancel(false);
        builder.setSmallIcon(Icon.createWithBitmap(icon));
        builder.setOngoing(true);
        builder.setContentTitle("Pitch");
        builder.setContentText(String.valueOf(current[1]));
        builder.build();

        myNotication = builder.getNotification();
        manager.notify(12, myNotication);

        Notification.Builder builder3 = new Notification.Builder(MainActivity.this);
        builder.setAutoCancel(false);
        builder.setSmallIcon(Icon.createWithBitmap(icon));
        builder.setOngoing(true);
        builder.setContentTitle("Roll");
        builder.setContentText(String.valueOf(current[1]));
        builder.build();

        myNotication = builder.getNotification();
        manager.notify(13, myNotication);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public Icon getIcon(String text) {
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .width(85)  // width in px
                .height(85) // height in px
                .fontSize(24)
                .endConfig()

                .buildRect(text, Color.BLACK);
        Bitmap icon = drawableToBitmap(drawable);
        return Icon.createWithBitmap(icon);
    }


    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageName = currentTimestamp + "_" + String.valueOf(++counter);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(imageName)); // set the image file name

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, 1034);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1034) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getPhotoFileUri(imageName);
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
//                // RESIZE BITMAP, see section below
//                // Load the taken image into a preview
//                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
//                ivPreview.setImageBitmap(takenImage);+
                String body = "yaw:" + String.valueOf(current[0]) + " " + "pitch:" + String.valueOf(current[1]) + " " + "roll:" + String.valueOf(current[2]);
                generateFile(MainActivity.this, imageName, body);


            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d("AadilSensors", "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public void generateFile(Context context, String sFileName, String sBody) {
        try {
            File directory = new File(Environment.getExternalStorageDirectory().getPath());

            FileOutputStream fOut = null;

            fOut = new FileOutputStream(new File(directory, sFileName + ".txt"));
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(sBody);
            osw.close();
            osw.flush();
            fOut.flush();
            fOut.close();

//            File gpxfile = new File(directory, sFileName+".txt");
//            FileWriter writer = new FileWriter(gpxfile);
//            writer.append(sBody);
//            writer.flush();
//            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, "Data Saved", Toast.LENGTH_SHORT).show();

    }

    private float[] getValue(float[] value) {

        float[] temp = {value[0], value[1], value[2]};
        valuesList.remove(0);
        valuesList.add(temp);
        float x = 0, y = 0, z = 0;
        for (float[] fl : valuesList) {
            x += fl[0];
            y += fl[1];
            z += fl[2];
        }

        return new float[]{x / 10, y / 10, z / 10};


    }

//    protected float[] lowPass(float[] input, float[] output) {
//        if (output == null) return input;
//        for (int i = 0; i < input.length; i++) {
//            output[i] = output[i] + ALPHA * (input[i] - output[i]);
//        }
//        return output;
//    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        SensorVals = getValue(event.values);

        if (SensorVals[0] > 180)
            SensorVals[0] -= 360;
        SensorVals[0] = -1 * SensorVals[0];


        actual[0] = (SensorVals[0]);
        actual[1] = (SensorVals[1]);
        actual[2] = (SensorVals[2]);

        current[0] = Float.parseFloat(df.format(SensorVals[0] - ref[0]));
        current[1] = Float.parseFloat(df.format(SensorVals[1] - ref[1]));
        current[2] = Float.parseFloat(df.format(SensorVals[2] - ref[2]));

        t1.setText(String.valueOf(current[0]));
        t2.setText(String.valueOf((current[1])));
        t3.setText(String.valueOf((current[2])));

        Notification.Builder builder = new Notification.Builder(MainActivity.this);
        builder.setAutoCancel(false);
        builder.setContentTitle("yaw");
        builder.setSmallIcon(getIcon(String.valueOf(current[0])));
        builder.setContentText(String.valueOf(current[0]));

        builder.setOngoing(true);
        builder.build();

        myNotication = builder.getNotification();
        manager.notify(11, myNotication);

        Notification.Builder builder2 = new Notification.Builder(MainActivity.this);
        builder.setAutoCancel(false);
        builder.setContentTitle("pitch");
        builder.setSmallIcon(getIcon(String.valueOf(current[1])));
        builder.setContentText(String.valueOf(current[1]));

        builder.setOngoing(true);
        builder.build();

        myNotication = builder.getNotification();
        manager.notify(12, myNotication);

        Notification.Builder builder3 = new Notification.Builder(MainActivity.this);
        builder.setAutoCancel(false);
        builder.setContentTitle("Roll");
        builder.setSmallIcon(getIcon(String.valueOf(current[2])));
        builder.setContentText(String.valueOf(current[2]));

        builder.setOngoing(true);
        builder.build();

        myNotication = builder.getNotification();
        manager.notify(13, myNotication);



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);

    }


}
