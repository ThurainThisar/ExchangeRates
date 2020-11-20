package com.thurain_thisar.exchange_rates;

import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.net.ConnectivityManager;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Spinner spinner;
    EditText fromCurrency, toCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        spinner = findViewById(R.id.spinner);
        fromCurrency = findViewById(R.id.fromCurrency);
        toCurrency = findViewById(R.id.toCurrency);

        //Set up the texts
        TextView alertText = findViewById(R.id.alertText);
        alertText.setText(R.string.alert);
        TextView aboutText = findViewById(R.id.aboutText);
        aboutText.setText(R.string.about);

        //Set up the spinner as soon as the activity created
        spinnerSetup();

        if (isNetworkConnected()) {
            toCurrency.setHint(R.string.no_data);
        } else { toCurrency.setHint(R.string.no_connection);
        }
    }

    //Check if the device is connected to the Network or not
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    //Request exchange rates from openexchangerates.org api
    private void getApiResult(String str) {

        //Insert your app id from openexchangerates.org in your_app_id
        String url = "https://openexchangerates.org/api/latest.json?app_id=your_app_id";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {

                        //Get JsonObject
                        JSONObject jsonObject = response.getJSONObject("rates");

                        //Write your base currency. Here, MMk
                        float fMyanmar = Float.parseFloat(jsonObject.get("MMK").toString());
                        float fOthers = Float.parseFloat(jsonObject.get(str).toString());
                        String s = fromCurrency.getText().toString();

                        //Check if the EditText is empty or not
                        if (s.matches("")) {
                            toCurrency.setText("");
                        }
                        else {

                            //Create equation to make MMK to be base currency
                            String fCurrency0 = String.valueOf(fMyanmar / fOthers);
                            String fCurrency = String.valueOf((fMyanmar / fOthers) * Float.parseFloat(s));

                            //When user change value
                            onTextChanged(Float.parseFloat(fCurrency0));
                            toCurrency.setText(fCurrency);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        //Request using Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    //Set up the spinner using currencyArray located in strings.xml
    private void spinnerSetup() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencyArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                //Get the selected item from spinner
                String values = String.valueOf(adapterView.getSelectedItem());

                //Split text into 2 groups as we need just currency code
                //Eg. split USD United State Dollar into 'USD' and 'United Stated Dollar'
                String[] arrayValue = values.split(" ", 2);

                //And we just take 'USD' which is arrayValue[0]
                String value = arrayValue[0];

                //And send this code to getApiResult()
                getApiResult(value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    //Change values according to the uset inputs
    private void onTextChanged(float convRate) {
        fromCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (fromCurrency.getText().toString().matches("")) {
                        toCurrency.setText("");
                    }
                    else {
                        float convAmount = Float.parseFloat(fromCurrency.getText().toString());
                        toCurrency.setText(String.valueOf(convAmount * convRate));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
