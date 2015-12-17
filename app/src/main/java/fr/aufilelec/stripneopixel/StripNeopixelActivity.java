package fr.aufilelec.stripneopixel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;

import fr.aufilelec.stripneopixel.utils.Const;

import static com.android.volley.Request.Method.GET;

public class StripNeopixelActivity extends AppCompatActivity {
    private String TAG = StripNeopixelActivity.class.getSimpleName();

    private Spinner schemes, animations, patterns, widths;
    private Button btnPause;
    private ProgressDialog pDialog;
    private SeekBar speedBar;
    private String tag_json_obj = "pause_jobj_req";

    private static HashMap<String, Integer> mapPatterns;
    static
    {
        mapPatterns = new HashMap<String, Integer>();
        mapPatterns.put("Bars", 0);
        mapPatterns.put("Gradient", 1);
    }

    private static HashMap<String, Integer> mapAnimations;
    static
    {
        mapAnimations = new HashMap<String, Integer>();
        mapAnimations.put("Rainbow", 2);
        mapAnimations.put("Rainbow Cycle", 3);
        mapAnimations.put("Color Wipe", 4);
        mapAnimations.put("Theater Chase", 5);
        mapAnimations.put("Theater Chase Rainbow", 6);
        mapAnimations.put("Random", 7);
    }

    private static final HashMap<String, Integer> mapSchemes;
    static
    {
        mapSchemes = new HashMap<String, Integer>();
        mapSchemes.put("Plain", 0);
        mapSchemes.put("RGB", 1);
        mapSchemes.put("Christmas", 2);
        mapSchemes.put("Hanukkah", 3);
        mapSchemes.put("Kwanzaa", 4);
        mapSchemes.put("Rainbow", 5);
        mapSchemes.put("Fire", 6);
    }

    private static HashMap<String, Integer> mapWidths;
    static
    {
        mapWidths = new HashMap<String, Integer>();
        mapWidths.put("Small", 0);
        mapWidths.put("Medium", 1);
        mapWidths.put("Large", 2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnPause = (Button) findViewById(R.id.btnPause);
        speedBar = (SeekBar) findViewById(R.id.speed);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String addrIpPref = sharedPref.getString(SettingsActivity.ADDRESS_IP, "");
        AppController.setIp(addrIpPref);

        addListenerOnSchemesItemSelection();
        addListenerOnAnimationsItemSelection();
        addListenerOnPatternsItemSelection();
        addListenerOnWidthsItemSelection();
        initSpeedSeekBar();
        btnPause.setOnClickListener(new PauseClickListerner());
    }

    private void addListenerOnWidthsItemSelection() {
        widths = (Spinner) findViewById(R.id.widths);
        widths.setOnItemSelectedListener(new CustomOnWidthSelectedListener());
    }

    protected void initSpeedSeekBar() {
        speedBar = (SeekBar) findViewById(R.id.speed);
        speedBar.setProgress(80);
        speedBar.setOnSeekBarChangeListener(new SpeedChangeListener());
    }

    public void addListenerOnSchemesItemSelection() {
        schemes = (Spinner) findViewById(R.id.schemes);
        schemes.setOnItemSelectedListener(new CustomOnSchemeSelectedListener());
    }

    public void addListenerOnAnimationsItemSelection() {
        animations = (Spinner) findViewById(R.id.animations);
        animations.setOnItemSelectedListener(new CustomOnAnimationSelectedListener());
    }

    public void addListenerOnPatternsItemSelection() {
        patterns = (Spinner) findViewById(R.id.patterns);
        patterns.setOnItemSelectedListener(new CustomOnPatternSelectedListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Lance settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, 1);  //enables return to here
        }

        return super.onOptionsItemSelected(item);
    }

    public void showProgressDialog() {
        if (!pDialog.isShowing()) {
            pDialog.setProgress(0);
            pDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (pDialog.isShowing()) {
            pDialog.setProgress(pDialog.getMax());
            pDialog.dismiss();
        }
    }

    private class CustomOnAnimationSelectedListener implements AdapterView.OnItemSelectedListener {
        private final String TAG = CustomOnSchemeSelectedListener.class
                .getSimpleName();
        private Integer counter = 0;

        // These tags will be used to cancel the requests
        private String tag_json_obj = "anim_jobj_req";

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.v(TAG, "onItemSelected");
            if (counter <= 0) {
                counter++;
                return;
            }
            String val = parent.getItemAtPosition(position).toString();
            final Toast toast = Toast.makeText(view.getContext(),
                    "Animation selected : " + val,
                    Toast.LENGTH_SHORT);
            Log.v("StripNeopixelActivity", "Scheme selected: " + val);

            Integer key = mapAnimations.get(val);
            if (key != null) {
                showProgressDialog();
                String url = Const.URL_ANIMATION.replaceFirst("%IP%", AppController.ip)
                        + key.toString();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(GET,
                        url, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                schemes.setSelection(0);
                                schemes.setSelection(0);
                                patterns.setSelection(0);
                                widths.setSelection(0);
                                hideProgressDialog();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(jsonObjReq,
                        tag_json_obj);

                // Cancelling request
                // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    private class CustomOnPatternSelectedListener implements AdapterView.OnItemSelectedListener {
        private final String TAG = CustomOnSchemeSelectedListener.class
                .getSimpleName();
        private Integer counter = 0;
        private String tag_json_obj = "patt_json_obj";

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (counter <= 0) {
                counter++;
                return;
            }
            String val = parent.getItemAtPosition(position).toString();
            final Toast toast = Toast.makeText(parent.getContext(),
                    "Pattern selected : " + val,
                    Toast.LENGTH_SHORT);
            Log.v("StripNeopixelActivity", "Pattern selected: " + val);

            Integer key = mapPatterns.get(val);
            if (key != null) {
                String url = Const.URL_PATTERN.replaceFirst("%IP%", AppController.ip) + key.toString();

                showProgressDialog();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(GET,
                        url, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                animations.setSelection(0);
                                hideProgressDialog();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(jsonObjReq,
                        tag_json_obj);

                // Cancelling request
                // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    private class CustomOnWidthSelectedListener implements AdapterView.OnItemSelectedListener {
        private final String TAG = CustomOnSchemeSelectedListener.class
                .getSimpleName();
        private Integer counter = 0;
        private String tag_json_obj = "width_json_obj";

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String val = parent.getItemAtPosition(position).toString();
            final Toast toast = Toast.makeText(parent.getContext(),
                    "Width selected : " + val,
                    Toast.LENGTH_SHORT);
            Log.v("StripNeopixelActivity", "Width selected: " + val);

            Integer key = mapWidths.get(val);
            if (key != null) {
                String url = Const.URL_WIDTH.replaceFirst("%IP%", AppController.ip) + key.toString();

                showProgressDialog();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(GET,
                        url, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                animations.setSelection(0);
                                hideProgressDialog();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(jsonObjReq,
                        tag_json_obj);

                // Cancelling request
                // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class CustomOnSchemeSelectedListener implements AdapterView.OnItemSelectedListener {
        private final String TAG = CustomOnSchemeSelectedListener.class
                .getSimpleName();
        private Integer counter = 0;

        // These tags will be used to cancel the requests
        private String tag_json_obj = "scheme_jobj_req";

        /**
         * Making json object request
         *
         */
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (counter <= 0) {
                counter++;
                return;
            }
            String val = parent.getItemAtPosition(position).toString();
            final Toast toast = Toast.makeText(view.getContext(),
                    "OnItemSelectedListener : " + val,
                    Toast.LENGTH_SHORT);
            Log.d("StripNeopixelActivity", "Scheme selected: " + val);

            Integer key = mapSchemes.get(val);
            if (key != null) {
                String url = Const.URL_SCHEME.replaceFirst("%IP%", AppController.ip) + key.toString();

                showProgressDialog();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(GET,
                        url, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                animations.setSelection(0);
                                patterns.setSelection(2);
                                hideProgressDialog();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(jsonObjReq,
                        tag_json_obj);

                // Cancelling request
                // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }
    }

    private class PauseClickListerner implements View.OnClickListener {
        private final String TAG = CustomOnSchemeSelectedListener.class
                .getSimpleName();
        private Integer counter = 0;

        // These tags will be used to cancel the requests
        private String tag_json_obj = "off_jobj_req";
        @Override
        public void onClick(View v) {
            showProgressDialog();
            final Toast toast = Toast.makeText(v.getContext(),
                    "Off selected",
                    Toast.LENGTH_SHORT);
            Log.d("StripNeopixelActivity", "OFF selected");

            String url = Const.URL_PAUSE.replaceFirst("%IP%", AppController.ip);


            JsonObjectRequest jsonObjReq = new JsonObjectRequest(GET,
                    url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                            schemes.setSelection(0);
                            patterns.setSelection(0);
                            animations.setSelection(0);
                            widths.setSelection(0);
                            hideProgressDialog();
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    hideProgressDialog();
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq,
                    tag_json_obj);

            // Cancelling request
            // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
        }
    }

    private class SpeedChangeListener implements SeekBar.OnSeekBarChangeListener {
        private final String TAG = CustomOnSchemeSelectedListener.class
                .getSimpleName();
        Integer progress = 0;
        boolean isUser = false;

        // These tags will be used to cancel the requests
        private String tag_json_obj = "speed_jobj_req";

        @Override
        public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
            progress = progresValue;
            isUser = fromUser;
            //Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            final Toast toast = Toast.makeText(
                    getApplicationContext(),
                    "Stopped tracking seekbar",
                    Toast.LENGTH_SHORT);
            if (isUser) {
                showProgressDialog();
                Log.d("StripNeopixelActivity", "Speed selected: " + progress.toString());

                Integer val = 100 - progress;
                String url = Const.URL_SPEED.replaceFirst("%IP%", AppController.ip)
                        + val.toString();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(GET,
                        url, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                hideProgressDialog();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(jsonObjReq,
                        tag_json_obj);

                // Cancelling request
                // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
            } else {
                toast.show();
            }
        }
    }
}
