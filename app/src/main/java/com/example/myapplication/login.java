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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {
    Button btn;
    EditText email,password;
    public static String URL_REGIST = "http://192.168.43.61/registrationdata_ar/login.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

            btn = (Button) findViewById(R.id.button);
            email = (EditText) findViewById(R.id.username);
            password = (EditText) findViewById(R.id.password_login);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //  login();
                         Intent intent=new Intent(getApplicationContext(),dashboard.class);
                   startActivity(intent);
                }
            });
        }
        public void  login()
        {


            final String memail=this.email.getText().toString().trim();
            final String mpassword=this.password.getText().toString().trim();
            StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_REGIST,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("JSON",response);
                                JSONObject jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                JSONArray jsonArray=jsonObject.getJSONArray("login");
                                if (success.equals("1")) {
                                    for (int i=0 ;i<jsonArray.length();i++)
                                    {
                                        JSONObject object=jsonArray.getJSONObject(i);
                                        String name=object.getString("name").trim();
                                        String email=object.getString("email").trim();
                                        Toast.makeText(login.this, "login Success", Toast.LENGTH_SHORT).show();
                                    }
                                    Intent intent = new Intent(getApplicationContext(), dashboard.class);
                                    startActivity(intent);

                                }
                                else
                                {
                                    Toast.makeText(login.this, "User Not exist", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(login.this, "login fail" + e.toString(), Toast.LENGTH_SHORT).show();
                                // reg.setVisibility(View.GONE);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(login.this, "login failed" + error.toString(), Toast.LENGTH_SHORT).show();
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params=new HashMap<>();
                    params.put("username",memail);
                    params.put("password",mpassword);
                    return params;
                }
            };
            RequestQueue requestQueue= Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);

        }

        public void family(View view)
        {
            Intent intent = new Intent(getApplicationContext(), registration.class);
            startActivity(intent);
        }
    public void family1(View view)
    {
        Intent intent = new Intent(getApplicationContext(), forget_pass.class);
        startActivity(intent);
    }

    }

