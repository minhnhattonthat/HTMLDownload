package com.nhatton.htmldownload.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nhatton on 10/16/17.
 */

// Noninstantiable helper class
public class HtmlHelper {

    // Suppress default constructor for noninstantiability
    private HtmlHelper(){
        throw new AssertionError();
    }
    public static String autoCorrectUrl(String url) {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        return url;
    }

    public static ArrayList<String> filterResponse(String response) {

        ArrayList<String> result = new ArrayList<>();
        String regex = "(<img.*src|content)=\"https?://.*\\.(jpg|png)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);

        int i = 0;
        while (matcher.find(i)) {
            String item = response.substring(matcher.start(), matcher.end());
            item = item.replaceFirst("(<img.*src|content)=\"", "");
            if (item.indexOf("http") != item.lastIndexOf("http")) {
                item = item.substring(item.lastIndexOf("http"));
            }
            result.add(item);
            i = matcher.end();
        }

        Log.e("Number of images", String.valueOf(result.size()));
        return result;
    }
}
