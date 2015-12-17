package fr.aufilelec.stripneopixel.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;

/**
 * Created by Manuel on 16/12/2015.
 */
public class Utils {
    public static HashMap<String, Integer> getHashTableResource(Context c, int hashMapResId) {
        HashMap<String, Integer> map = null;
        XmlResourceParser parser = c.getResources().getXml(hashMapResId);

        Integer id = null;
        String value = null;

        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("utils", "Start document");
                }
                else if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("map")) {
                        //boolean isLinked = parser.getAttributeBooleanValue(null, "linked", false);

                        map = new HashMap<String, Integer>();
                    }
                    else if (parser.getName().equals("entry")) {
                        id = Integer.parseInt(parser.getAttributeValue(null, "id"));

                        if (null == id) {
                            parser.close();
                            return null;
                        }
                    }
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("entry")) {
                        map.put(value, id);
                        id = null;
                        value = null;
                    }
                }
                else if (eventType == XmlPullParser.TEXT) {
                    if (null != id) {
                        value = parser.getText();
                    }
                }
                eventType = parser.next();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return map;
    }
}
