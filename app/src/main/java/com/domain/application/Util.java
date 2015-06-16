package com.domain.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.telephony.TelephonyManager;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pratik on 9/6/15.
 */
public class Util {
    /**
     * Regex for name supporting Unicode characters
     */
    public static final String REGEX_NAME = "^[\\p{L} .'-]+$";

    /**
     * Regex for 8-digit mobile number
     */
    public static final String REGEX_PHONE = "^[0-9]{8}$";

    /**
     * Regex for standard email-id
     */
    public static final String REGEX_EMAIL = "\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}\\b";

    /*
     * keys for storing login data in shared preferences
     */
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_ADDRESS = "address";

    /*
     * Shared Preference file name
     */
    public static final String PREFERENCE_NAME = "Settings";

    /**
     * Toast text on the screen
     *
     * @param text    The text to be displayed on the screen
     * @param context Context of the application where text is to be displayed
     * @param length  Duration for which the text is to displayed -
     *                Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     */
    public static void toastText(String text, Context context, Integer length) {
        Toast.makeText(context, text, length).show();
        return;
    }

    private static SharedPreferences getSharedPrefs(Context context) {
        return (context).getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
    }

    /**
     * Saves the login data to SharedPreferences
     *
     * @param context Activity context from which function is called
     * @param name    Name of the user
     * @param email   Email of the user
     * @param mobile  Phone of the user
     * @param address Address of the user
     */
    public static void save(Context context, String name, String email,
                            String mobile, String address) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_MOBILE, mobile);
        editor.putString(KEY_ADDRESS, address);
        editor.commit();
    }

    /**
     * Retrieve name of the user
     *
     * @param context Activity context from which function is called
     * @return name of the user
     */
    public static String getName(Context context) {
        return getSharedPrefs(context).getString(KEY_NAME, null);
    }

    /**
     * Retrieve email-id of the user
     *
     * @param context Activity context from which function is called
     * @return email-id of the user
     */
    public static String getEmail(Context context) {
        return getSharedPrefs(context).getString(KEY_EMAIL, null);
    }

    /**
     * Retrieve mobile number of the user
     *
     * @param context Activity context from which function is called
     * @return mobile number of the user
     */
    public static String getMobile(Context context) {
        return getSharedPrefs(context).getString(KEY_MOBILE, null);
    }

    /**
     * Retrieve address of the user
     *
     * @param context Activity context from which function is called
     * @return address of the user
     */
    public static String getAddress(Context context) {
        return getSharedPrefs(context).getString(KEY_ADDRESS, null);
    }

    /**
     * Returns the phone number saved in the SIM
     *
     * @param context Activity context from which function is called
     * @return phone number as string if available, null otherwise
     */
    public static String getPhoneNumberOfMobile(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telephonyManager.getLine1Number();
        return phoneNumber;
    }

    /**
     * Creates a XML file in Internal Storage of the phone
     *
     * @param context      Activity context from which function is called
     * @param fileName     name of the file to be created
     * @param sendLocation true if location (latitude and longitude) needs to be added in the file, false otherwise
     * @param location     location object to send latitude and longitude. Only useful if sendLocation is true
     */
    public static void createXMLFile(Context context, String fileName, Boolean sendLocation, Location location) {

        FileOutputStream outputStream = null;
        XmlSerializer serializer = Xml.newSerializer();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            serializer.setOutput(outputStream, "UTF-8");
            serializer.startDocument("UTF-8", true);

            serializer.startTag("", "message");
            serializer.attribute("", "button", sendLocation ? String.valueOf(2) : String.valueOf(1));

            serializer.startTag("", "name");
            serializer.text(getName(context));
            serializer.endTag("", "name");
            serializer.startTag("", "date_time");
            serializer.text(dateFormat.format(date));
            serializer.endTag("", "date_time");
            serializer.startTag("", "phone_number");
            serializer.text(getMobile(context));
            serializer.endTag("", "phone_number");

            if (sendLocation) {
                serializer.startTag("", "latitude");
                serializer.text(String.valueOf(location.getLatitude()));
                serializer.endTag("", "latitude");
                serializer.startTag("", "longitude");
                serializer.text(String.valueOf(location.getLongitude()));
                serializer.endTag("", "longitude");
            }

            serializer.endTag("", "message");

            serializer.endDocument();
            serializer.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
}
