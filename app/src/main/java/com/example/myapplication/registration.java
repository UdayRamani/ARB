package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class registration extends AppCompatActivity {

    EditText name_e, mobile_number_e, email_e, password_e, c_password_e;
    Button reg;
    public static String URL_REGIST = "http://192.168.43.61/registrationdata_ar/register.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().hide();
        name_e = (EditText) findViewById(R.id.name);
        mobile_number_e = (EditText) findViewById(R.id.mobile_number);
        email_e = (EditText) findViewById(R.id.email);
        password_e = (EditText) findViewById(R.id.password);
        reg = (Button) findViewById(R.id.reg);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Regist();
            }
        });

    }
    private void Regist() {
        //reg.setVisibility(View.GONE);
        final String name = this.name_e.getText().toString().trim();
        final String email = this.email_e.getText().toString().trim();
        final String password = this.password_e.getText().toString().trim();
        final String mobile_number = this.mobile_number_e.getText().toString().trim();

        if (name.equals("")) {
            this.name_e.setError("Name required");
        } else if (mobile_number.equals("")) {
            this.mobile_number_e.setError("mobile number required");
        } else if (email.equals("")) {
            this.email_e.setError("E-mail required ");
        } else if (password.equals("")) {
            this.password_e.setError("Password required");
        } else {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGIST,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("JSON", response);
                                JSONObject jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                String  responseDescription=jsonObject.getString("responseDescription");
                                // JSONArray jsonArray=jsonObject.getJSONArray("login");
                                //   String otp = jsonObject.getString("otp");

                                if (success.equals("1")) {

                                    Toast.makeText(registration.this, responseDescription, Toast.LENGTH_SHORT).show();

                            Intent io = new Intent(getApplicationContext(),login.class);
                                    startActivity(io);
                                    }
                                else
                                {
                                    Toast.makeText(registration.this, responseDescription, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(registration.this, "already created account.." + e.toString(), Toast.LENGTH_SHORT).show();
                                // reg.setVisibility(View.GONE);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(registration.this, "Register failed" + error.toString(), Toast.LENGTH_SHORT).show();
                    // reg.setVisibility(View.GONE);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    params.put("mobile_number", mobile_number);
                    params.put("email", email);
                    params.put("password", password);


                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
    }
}



