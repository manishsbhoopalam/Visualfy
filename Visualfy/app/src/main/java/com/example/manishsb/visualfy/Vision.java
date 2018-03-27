package com.example.manishsb.visualfy;

import android.app.Activity;
import android.app.AlertDialog;

import com.google.gson.Gson;
import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.contract.Tag;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.speech.tts.Voice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.speech.tts.Synthesizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.R.attr.bitmap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Vision extends Activity implements ISpeechRecognitionServerEvents {

    //////////
    private ImageSurfaceView mImageSurfaceView;
    private Camera camera;

    private FrameLayout cameraPreviewLayout;
    private ImageView capturedImageHolder;

    ///////////////
    int m_waitSeconds = 0;
    DataRecognitionClient dataClient = null;
    MicrophoneRecognitionClient micClient = null;
    SpeechRec.FinalResponseStatus isReceivedResponse = SpeechRec.FinalResponseStatus.NotReceived;
    //////////

    public VisionServiceClient visionServiceClient;
    EditText alog;
    Bitmap bitmap;
    ImageView imageView;
    Button btn;
    ByteArrayInputStream inputStream;
    TextView textview;

/////////////////////////Added text to speech
private Synthesizer m_syn;
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

    //////////////image/////////
    public void recognize_image()
    {


        AsyncTask<InputStream, String, String> visiontask = new AsyncTask<InputStream, String, String>() {

            ProgressDialog dialog = new ProgressDialog(Vision.this);

            @Override
            protected String doInBackground(InputStream... params) {
                publishProgress("Recognizing....");
                Log.i("info", "recognizzing");
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
                dialog.dismiss();
                Log.i("post", "post");
                AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                StringBuilder stringBuilder = new StringBuilder();
                for (String tag : result.description.tags) {
                    if(!tag.contains("ing"))
                    stringBuilder.append(tag+ " ");

                }
                textview.setText(stringBuilder);
                speak(String.valueOf(stringBuilder));
            }

            @Override
            protected void onPreExecute() {
                Log.i("pre", "pre");
                dialog.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                Log.i("onpro", "onpro");
                dialog.setMessage(values[0]);
            }
        };
        visiontask.execute(inputStream);
    }

    ///////////////////
    public void processimage() {





        imageView.setImageBitmap(bitmap);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        recognize_image();

    }

    ////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision);
        Intent intent = getIntent();


        speak(intent.getStringExtra("Text"));
       // alog = (EditText)findViewById(R.id.ed1);
        visionServiceClient = new VisionServiceRestClient("b66543d9652c460bba0b84316809b98a");
    //    imageView = (ImageView) findViewById(R.id.imageView);
        btn = (Button) findViewById(R.id.button);



        StartButton_Click();
    }


    /////////////camera //////////





    /////////////////////

   /*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show();
        bitmap =(Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
        processimage();

    } */

//////////////////// speech functions/////////////

    public enum FinalResponseStatus { NotReceived, OK, Timeout }

    /**
     * Gets the primary subscription key
     */
    public String getPrimaryKey() {
        return this.getString(R.string.primaryKey);
    }

    /**
     * Gets the LUIS application identifier.
     * @return The LUIS application identifier.
     */
    private String getLuisAppId() {
        return this.getString(R.string.luisAppID);
    }

    /**
     * Gets the LUIS subscription identifier.
     * @return The LUIS subscription identifier.
     */
    private String getLuisSubscriptionID() {
        return this.getString(R.string.luisSubscriptionID);
    }

    /**
     * Gets a value indicating whether or not to use the microphone.
     * @return true if [use microphone]; otherwise, false.
     */
    private Boolean getUseMicrophone() {
        return TRUE;
    }

    /**
     * Gets a value indicating whether LUIS results are desired.
     * @return true if LUIS results are to be returned otherwise, false.
     */
    private Boolean getWantIntent() {
        return FALSE;
    }

    /**
     * Gets the current speech recognition mode.
     * @return The speech recognition mode.
     */
    private SpeechRecognitionMode getMode() {

        return SpeechRecognitionMode.LongDictation;

    }

    /**
     * Gets the default locale.
     * @return The default locale.
     */
    private String getDefaultLocale() {
        return "en-us";
    }




    /**
     * Gets the Cognitive Service Authentication Uri.
     * @return The Cognitive Service Authentication Uri.  Empty if the global default is to be used.
     */
    private String getAuthenticationUri() {
        return this.getString(R.string.authenticationUri);
    }
//////////////////// functions ////////////


    private void StartButton_Click() {


        this.m_waitSeconds = this.getMode() == SpeechRecognitionMode.ShortPhrase ? 20 : 200;

      //  Toast.makeText(this, "start button", Toast.LENGTH_SHORT).show();
        this.LogRecognitionStart();

        if (this.getUseMicrophone()) {
            if (this.micClient == null) {
                if (this.getWantIntent()) {


                    this.micClient =
                            SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                                    this,
                                    this.getDefaultLocale(),
                                    this,
                                    this.getPrimaryKey(),
                                    this.getLuisAppId(),
                                    this.getLuisSubscriptionID());
                }
                else
                {
                    this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                            this,
                            this.getMode(),
                            this.getDefaultLocale(),
                            this,
                            this.getPrimaryKey());
                }

                this.micClient.setAuthenticationUri(this.getAuthenticationUri());
            }

            this.micClient.startMicAndRecognition();
        }

    }

    /**
     * Logs the recognition start.
     */
    private void LogRecognitionStart() {
        String recoSource;
        if (this.getUseMicrophone()) {
            recoSource = "microphone";
        } else if (this.getMode() == SpeechRecognitionMode.ShortPhrase) {
            recoSource = "short wav file";
        } else {
            recoSource = "long wav file";
        }

        this.WriteLine("\n--- Start speech recognition using " + recoSource + " with " + this.getMode() + " mode in " + this.getDefaultLocale() + " language ----\n\n");
    }


    public void onFinalResponseReceived(final RecognitionResult response) {
        boolean isFinalDicationMessage = this.getMode() == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (null != this.micClient && this.getUseMicrophone() && ((this.getMode() == SpeechRecognitionMode.ShortPhrase) || isFinalDicationMessage)) {
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDicationMessage) {

            this.isReceivedResponse = SpeechRec.FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {

            this.WriteLine("********* Final n-BEST Results *********");
            for (int i = 0; i < response.Results.length; i++) {
                /*this.WriteLine("[" + i + "]" + " Confidence=" + response.Results[i].Confidence +
                        " Text=\"" + response.Results[i].DisplayText + "\"");*/


                this.WriteLine();




                if(response.Results[i].DisplayText.equals("Open camera."))
                {
                    response.Results[i].DisplayText="";
                    Intent intent =new Intent( getApplicationContext(),cam.class);
                    startActivity(intent);
                }
                if(response.Results[i].DisplayText.equals("Close."))
                {

                    Toast.makeText(this, "Closing Camera!!!", Toast.LENGTH_SHORT).show();
                   /* Intent intent =new Intent( getApplicationContext(),Vision.class);
                    intent.putExtra("text","");
                    startActivity(intent);*/

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
            }

        }
    }

    /**
     * Called when a final response is received and its intent is parsed
     */
    public void onIntentReceived(final String payload) {
        this.WriteLine("--- Intent received by onIntentReceived() ---");
        this.WriteLine(payload);
        this.WriteLine();
    }

    public void onPartialResponseReceived(final String response) {


    }

    public void onError(final int errorCode, final String response) {

        this.WriteLine("--- Error received by onError() .. give audio record  permission ---");
        this.WriteLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        this.WriteLine("Error text: " + response);
        this.WriteLine();
    }

    /**
     * Called when the microphone status has changed.
     * @param recording The current recording state
     */
    public void onAudioEvent(boolean recording) {
        this.WriteLine("--- Microphone status change received by onAudioEvent() ---");
        this.WriteLine("********* Microphone status: " + recording + " *********");
        if (recording) {
            this.WriteLine("Please start speaking.");
         //   Toast.makeText(this, " Please start speaking. ", Toast.LENGTH_SHORT).show();
        }

        WriteLine();
        if (!recording) {
            this.micClient.endMicAndRecognition();

        }
    }

    /**
     * Writes the line.
     */
    private void WriteLine() {
        this.WriteLine("");
    }

    /**
     * Writes the line.
     * @param text The line to write.
     */
    private void WriteLine(String text) {
        // Toast.makeText(this,text, Toast.LENGTH_SHORT).show();


    }




    /*
     * Speech recognition with data (for example from a file or audio source).
     * The data is broken up into buffers and each buffer is sent to the Speech Recognition Service.
     * No modification is done to the buffers, so the user can apply their
     * own VAD (Voice Activation Detection) or Silence Detection
     *
     * @param dataClient
     * @param recoMode
     * @param filename
     */
    private class RecognitionTask extends AsyncTask<Void, Void, Void> {
        DataRecognitionClient dataClient;
        SpeechRecognitionMode recoMode;
        String filename;

        RecognitionTask(DataRecognitionClient dataClient, SpeechRecognitionMode recoMode, String filename) {
            this.dataClient = dataClient;
            this.recoMode = recoMode;
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Note for wave files, we can just send data from the file right to the server.
                // In the case you are not an audio file in wave format, and instead you have just
                // raw data (for example audio coming over bluetooth), then before sending up any
                // audio data, you must first send up an SpeechAudioFormat descriptor to describe
                // the layout and format of your raw audio data via DataRecognitionClient's sendAudioFormat() method.
                // String filename = recoMode == SpeechRecognitionMode.ShortPhrase ? "whatstheweatherlike.wav" : "batman.wav";
                InputStream fileStream = getAssets().open(filename);
                int bytesRead = 0;
                byte[] buffer = new byte[1024];

                do {
                    // Get  Audio data to send into byte buffer.
                    bytesRead = fileStream.read(buffer);

                    if (bytesRead > -1) {
                        // Send of audio data to service.
                        dataClient.sendAudio(buffer, bytesRead);
                    }
                } while (bytesRead > 0);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            finally {
                dataClient.endAudio();
            }

            return null;
        }
    }












    //////////////////////////////////
}
