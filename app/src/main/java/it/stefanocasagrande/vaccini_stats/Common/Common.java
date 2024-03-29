package it.stefanocasagrande.vaccini_stats.Common;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Common {

    public static DB Database;
    public static boolean Data_Already_Loaded=false;

    public static int Back_Action=-1;
    public static final int Back_To_CloseApp=0;
    public static final int Back_To_Nowhere=1;
    public static final int Back_To_Summary=2;
    public static final int Back_To_Delivery_Group=3;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String AddDotToInteger(Integer value)
    {
        StringBuilder dotfiedNum= new StringBuilder();
        String inputNum = String.valueOf(Math.abs(value));

        int numLength = inputNum.length();
        for (int i=0; i<numLength; i++) {
            if ((numLength-i)%3 == 0 && i != 0) {
                dotfiedNum.append(".");
            }
            dotfiedNum.append(inputNum.charAt(i));
        }

        if (value<0)
            return String.format("-%s",dotfiedNum.toString());
        else
            return dotfiedNum.toString();
    }

    public static int get_int_from_Date(String value)
    {
        String Year=value.substring(0,4);
        String Month=value.substring(5,7);
        String Day=value.substring(8,10);

        String total=Year+Month+Day;

        return Integer.parseInt(total);
    }

    public static Date get_Date_from_DDMMYYYY(String value)
    {
        try {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return format.parse(value);
        }
        catch (ParseException ex)
        {
            return new Date();
        }

    }

    public static int get_int_from_DDMMYYYY(String value)
    {
        String Year=value.substring(6,10);
        String Month=value.substring(3,5);
        String Day=value.substring(0,2);

        String total=Year+Month+Day;

        return Integer.parseInt(total);
    }

    public static String get_dd_MM_yyyy(String value)
    {
        String Year=value.substring(0,4);
        String Month=value.substring(5,7);
        String Day=value.substring(8,10);

        return String.format("%s/%s/%s", Day, Month, Year);
    }
}
