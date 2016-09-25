package uk.co.barbuzz.clockscroller;

import android.content.res.Resources;

public class Utils {

    public static int dp2px(int dp){
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

}
