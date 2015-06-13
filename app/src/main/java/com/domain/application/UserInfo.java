package com.domain.application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by pratik on 9/6/15.
 */
public class UserInfo extends ActionBarActivity {
    private EditText mName;
    private EditText mEmail;
    private EditText mMobile;
    private EditText mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Boolean edit = intent.getBooleanExtra(MainActivity.INTENT_EDIT, false);
        if (!edit) {
            String name = Util.getName(getApplicationContext());
            if (name != null) {
                Intent intentToMainActivity = new Intent(UserInfo.this, MainActivity.class);
                startActivity(intentToMainActivity);
                finish();
            }
        }
        setContentView(R.layout.activity_user_info);
        mName = (EditText) findViewById(R.id.name);
        mEmail = (EditText) findViewById(R.id.email);
        mMobile = (EditText) findViewById(R.id.mobile);
        mAddress = (EditText) findViewById(R.id.address);
        if (edit) {
            mName.setText(Util.getName(getApplicationContext()));
            mEmail.setText(Util.getEmail(getApplicationContext()));
            mMobile.setText(Util.getMobile(getApplicationContext()));
            mAddress.setText(Util.getAddress(getApplicationContext()));
        } else {
            String number = Util.getPhoneNumberOfMobile(getApplicationContext());
            if (number != null) {
                Log.i("UserInfo", number);
                mMobile.setText(number);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_user_details) {
            return tryLogin();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean tryLogin() {
        String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String mobile = mMobile.getText().toString().trim();
        String address = mAddress.getText().toString().trim();

        if (name.matches(Util.REGEX_NAME) && email.matches(Util.REGEX_EMAIL)
                && mobile.matches(Util.REGEX_PHONE)
                && !address.equals("")) {
            // save the details in SharedPreferences
            Util.save(getApplicationContext(), name, email, mobile, address);
            // move to home activity
            Intent intent = new Intent(UserInfo.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            if (!name.matches(Util.REGEX_NAME)) {
                Util.toastText("Please enter a valid name",
                        getApplicationContext(), Toast.LENGTH_LONG);
            } else if (!email.matches(Util.REGEX_EMAIL)) {
                Util.toastText("Please enter a valid email address",
                        getApplicationContext(), Toast.LENGTH_LONG);
            } else if (!mobile.matches(Util.REGEX_PHONE)) {
                Util.toastText("Please enter your 8-digit mobile number",
                        getApplicationContext(), Toast.LENGTH_LONG);
            } else if (address.equals("")) {
                Util.toastText(
                        "Please enter your address",
                        getApplicationContext(), Toast.LENGTH_LONG);
            }
            return false;
        }
    }
}
