package com.example.abacserver;

import static java.lang.Thread.sleep;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    System.out.println("permission granted");
                }
                System.out.println("permission");
            });

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermission(Manifest.permission.READ_SMS);
        getPermission(Manifest.permission.INTERNET);
        getPermission(Manifest.permission.ACCESS_NETWORK_STATE);
        getPermission(Manifest.permission.SEND_SMS);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view) {
        EditText number = (EditText) findViewById(R.id.number);
        TextView message =  (TextView) findViewById(R.id.message);
        sendsms(number.getText().toString(), message.getText().toString());
    }

    public void readsmsAsServer(View view) {
        long timestamp  = System.currentTimeMillis() - 36000000;
        EditText number = (EditText) findViewById(R.id.number);
        readAndSendSms(timestamp, number.getText().toString());
    }

    public void readMessage(View view) {
        EditText number = (EditText) findViewById(R.id.number);
        long timestamp  = System.currentTimeMillis() - (1000*60*60*5);
       // TODO long timestamp  = System.currentTimeMillis() - (1000*60*60);
        response res = readsms(timestamp, number.getText().toString());
        TextView message =  (TextView) findViewById(R.id.message);
        message.setText(res.getMessage());
    }
    private void sendsms(String number, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number,
                null, message,
                null, null);
        Toast.makeText(getApplicationContext(), "SMS sent.",
                Toast.LENGTH_LONG).show();
    }

    private void getPermission(String permission) {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), permission) !=
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            requestPermissionLauncher.launch(
                    permission);
        }
    }

    private void sendData(Entity entity, long timestamp, String number) {

        RequestQueue queue = Volley.newRequestQueue(this);
        if(null == entity) {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readAndSendSms(timestamp, number);
        }

        try {
            String jsonInputString = new Gson().toJson(entity);
            JSONObject mJSONObject = new JSONObject(jsonInputString);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    "https://smspublisher.azurewebsites.net/OfflineMessage",
                    mJSONObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    readAndSendSms(timestamp, number);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    readAndSendSms(timestamp, number);
                }
            });

            queue.add(jsonObjectRequest);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private response readsms(long timestamp, String number) {
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), projection,
                "address='"+number+"' and date > " + timestamp,
                 null, "date desc");
        int index_Address = cursor.getColumnIndex("address");
        int index_Body = cursor.getColumnIndex("body");
        int index_Date = cursor.getColumnIndex("date");
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            int count = 0 ;
            String strAddress = cursor.getString(index_Address);
            String strbody = cursor.getString(index_Body);
            long longDate = cursor.getLong(index_Date);
            String[] split = strbody.split("~");
            String message = "";
            String sender = "";
            if(split.length == 2) {
                message = split[1].replaceAll("\\n", " ");;
                sender = split[0] + "@70sf4z.onmicrosoft.com";
            }
            else if (split.length == 1) {
                message = split[0].replaceAll("\\n", " ");
                sender = "DON HAS SENT A MESSAGE";
            }
            Entity entity = new Entity(message, strAddress, sender, String.valueOf(longDate));
            return new response(longDate, message, entity);

        }
        return new response(timestamp, "No new message", null);
    }

    private void readAndSendSms(long timestamp, String number) {
        response res = readsms(timestamp, number);
        sendData(res.getEntity(), res.getTimestamp(), number);
    }
}