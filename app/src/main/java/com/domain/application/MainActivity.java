package com.domain.application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String INTENT_EDIT = "intent_edit";

    /**

     */
    private static final String SERVER_PATH = "https://php-lnmiit.rhcloud.com/UploadToServer.php";

    /**
     * Name of the XML files in phone memory
     */
    private static final String XML_FILE_1 = "send_1.xml";
    private static final String XML_FILE_2 = "send_2.xml";

    private TextView mSend1;
    private TextView mSend2;
    private TextView mMessage;

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    private Location mLastLocation;

    private ProgressDialog dialog = null;
    private Integer serverResponseCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSend1 = (TextView) findViewById(R.id.send_1);
        mSend2 = (TextView) findViewById(R.id.send_2);
        mMessage = (TextView) findViewById(R.id.message);

        buildGoogleApiClient();

        mSend1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create the XML file
                // previous file, if exists, would be overwritten
                Util.createXMLFile(getApplicationContext(), XML_FILE_1, false, null);

                // display progress dialog
                dialog = ProgressDialog.show(MainActivity.this, "Please wait", "Sending file...", true);
                final String filePath = getFilesDir().getAbsolutePath() + "/";
                // start sending file in a thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadFile(filePath + XML_FILE_1);
                    }
                }).start();

            }
        });

        mSend2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation == null) {
                    mMessage.setText("No location detected. Make sure location is enabled on the device.");
                } else {
                    // create the XML file
                    // previous file, if exists, would be overwritten
                    Util.createXMLFile(getApplicationContext(), XML_FILE_2, true, mLastLocation);

                    // display progress dialog
                    dialog = ProgressDialog.show(MainActivity.this, "Please wait", "Sending file...", true);
                    final String filePath = getFilesDir().getAbsolutePath() + "/";
                    // start sending file in a thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadFile(filePath + XML_FILE_2);
                        }
                    }).start();
                }
            }
        });

        mMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessage.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_details) {
            // launch UserInfo activity
            Intent intent = new Intent(MainActivity.this, UserInfo.class);
            intent.putExtra(INTENT_EDIT, true);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Integer uploadFile(String fileURI) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileURI);

        try {
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(
                    sourceFile);
            URL url = new URL(SERVER_PATH);

            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // don't use a cached copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileURI);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileURI + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necessary after file data
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("MainActivity", "HTTP Response is: "
                    + serverResponseMessage + ": " + serverResponseCode);

            if (serverResponseCode == 200) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        mMessage.setText("File sent");
                    }
                });
            }

            // close the streams
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            dialog.dismiss();
            ex.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    mMessage.setText("Please check the server URL is live");
                }
            });

            Log.e("MainActivity", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            dialog.dismiss();
            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    mMessage.setText("Some error occurred");
                }
            });
            Log.e("MainActivity",
                    "Exception : " + e.getMessage(), e);
        }
        dialog.dismiss();
        return serverResponseCode;
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i("MainActivity", "Connected to GoogleApiClient");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("MainActivity", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("MainActivity", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
}
