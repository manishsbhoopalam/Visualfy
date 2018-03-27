package com.example.manishsb.visualfy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.R.attr.bitmap;
import static android.R.attr.tag;

public class cam extends AppCompatActivity  {

    Bitmap bitmap;

    private ImageSurfaceView mImageSurfaceView;
    private Camera camera;

    private FrameLayout cameraPreviewLayout;
    private ImageView capturedImageHolder;
    private Synthesizer m_syn;
    public VisionServiceClient visionServiceClient;
    ByteArrayInputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        Intent intent =getIntent();
        Log.i("info","reach");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        visionServiceClient = new VisionServiceRestClient("b66543d9652c460bba0b84316809b98a");

        cameraPreviewLayout = (FrameLayout)findViewById(R.id.camera_preview);
        capturedImageHolder = (ImageView)findViewById(R.id.captured_image);

        camera = checkDeviceCamera();
        mImageSurfaceView = new ImageSurfaceView(cam.this, camera);
        cameraPreviewLayout.addView(mImageSurfaceView);
        final Handler handler = new Handler();
        final int delay = 5000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                camera.takePicture(null, null, pictureCallback);
                handler.postDelayed(this, delay);
            }
        }, delay);


    }
    private Camera checkDeviceCamera(){
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
             bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if(bitmap==null){
                Toast.makeText(cam.this, "Captured image is empty", Toast.LENGTH_LONG).show();
                return;
            }
            processimage();
           // capturedImageHolder.setImageBitmap(scaleDownBitmapImage(bitmap, 300, 200 ));
        }
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }
    public void close_camera()
    {
        Intent intent =new Intent(getApplicationContext(),Vision.class);
        startActivity(intent);
    }

    //////////////image/////////
    public void recognize_image()
    {


        AsyncTask<InputStream, String, String> visiontask = new AsyncTask<InputStream, String, String>() {

            //ProgressDialog dialog = new ProgressDialog(cam.this);

            @Override
            protected String doInBackground(InputStream... params) {
               // publishProgress("Recognizing....");
              //  Log.i("info", "recognizzing");
                String[] features = {"Description"};
                String[] details = {};
                try {
                    AnalysisResult result = visionServiceClient.analyzeImage(params[0], features, details);
                    Log.i("info", "try");
                    String strResult = new Gson().toJson(result);
                    return strResult;
                } catch (VisionServiceException e) {
                    Log.i("info", String.valueOf(e));
                    return String.valueOf(e);
                } catch (IOException e) {
                    Log.i("info", String.valueOf(e));
                    return "Exception1";
                }

            }

            @Override
            protected void onPostExecute(String s) {
               // dialog.dismiss();
                Log.i("post", "post");
                AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                StringBuilder stringBuilder = new StringBuilder();
                for (Caption caption : result.description.captions) {

                        stringBuilder.append(caption.text);

                }

                speak(String.valueOf(stringBuilder));
            }

            @Override
            protected void onPreExecute() {
                Log.i("pre", "pre");
               // dialog.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                Log.i("onpro", "onpro");
               // dialog.setMessage(values[0]);
            }
        };
        visiontask.execute(inputStream);
    }

    ///////////////////
    public void processimage() {






        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        recognize_image();

    }

    ////////////////

    public void speak(final String tts_text){

        if (getString(R.string.api_key).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        } else {

            if (m_syn == null) {
                // Create Text To Speech Synthesizer.
                m_syn = new Synthesizer(getString(R.string.api_key));
            }

            //Toast.makeText(this, "If the wave is not played, please see the log for more information.", Toast.LENGTH_LONG).show();

            m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);

            Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)", Voice.Gender.Female, true);
            //Voice v = new Voice("zh-CN", "Microsoft Server Speech Text to Speech Voice (zh-CN, HuihuiRUS)", Voice.Gender.Female, true);
            m_syn.SetVoice(v, null);

            // Use a string for speech.
            m_syn.SpeakToAudio(tts_text);

            // Use SSML for speech.
            String text = "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xml:lang=\"en-US\"><voice xml:lang=\"en-US\" name=\"Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)\">.</voice></speak>";
            m_syn.SpeakSSMLToAudio(text);

            /*findViewById(R.id.stop_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    m_syn.stopSound();
                }
            });*/

            /*findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    m_syn.SpeakToAudio(tts_text);
                }
            });*/
        }
    }
    //////////////////////





}
