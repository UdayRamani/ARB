package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;

public class information extends AppCompatActivity {

    //saved data
    ImageView imageView;
    EditText emailview, phonenumberview,address,descr;
    Button btnAdd,btnViewData,callbtn;
    DatabaseHelper msavedatabase_helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        imageView = findViewById(R.id.imageView2);
        phonenumberview = findViewById(R.id.textView3);
        emailview = findViewById(R.id.textView4);
        address = findViewById(R.id.textView5);
        descr = findViewById(R.id.editdesc);
        msavedatabase_helper = new DatabaseHelper(this);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        callbtn=(Button)findViewById(R.id.callbtn);
        callbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+phonenumberview.getText().toString()));
                if (ActivityCompat.checkSelfPermission(information.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }
        });
        btnViewData = (Button) findViewById(R.id.btnView);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item2 = emailview.getText().toString();
                String item3 = phonenumberview.getText().toString();
                String item4 = address.getText().toString();
                String item5 = descr.getText().toString();
                if (item2.isEmpty()||item3.isEmpty()||item4.isEmpty()||item5.isEmpty()) {
                    toastMessage("You must put something in the text field!");
                }
                else
                {
                    addData(item2, item3, item4, item5);
                }

            }
        });
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(information.this, ListDataActivity.class);
                startActivity(intent);
            }
        });


        String jsonResp = getIntent().getStringExtra("responseJson");
        String ImageFilePth = getIntent().getStringExtra("imageLocation");
        final File f = new File(ImageFilePth);
        Bitmap picBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        picBitmap = getBitmapRotatedByDegree(picBitmap, 180);
        imageView.setImageBitmap(picBitmap);
        try {
            JSONObject obj = new JSONObject(jsonResp);
            JSONArray emails = obj.getJSONArray("email");
            JSONArray phoneNubers = obj.getJSONArray("phonenumbers");

            if (emails.length() > 0) {
                emailview.setText(emails.getString(0));
            }

            if (phoneNubers.length() > 0) {
                phonenumberview.setText(phoneNubers.getString(0));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void addData(String item2,String item3,String item4,String item5) {
        boolean insertData = msavedatabase_helper.addData(item2, item3, item4, item5);
        if (insertData) {
            toastMessage("Data Successfully Inserted!");
        } else {
            toastMessage("Something went wrong");
        }
    }
    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private void saveContact(String name, String number, String email){
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation> ();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        if (name != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            name).build());
        }

        //------------------------------------------------------ Mobile Number
        if (number != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        //------------------------------------------------------ Email
        if (email != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }
        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}