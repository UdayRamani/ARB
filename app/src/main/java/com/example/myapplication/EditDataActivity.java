package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
public class EditDataActivity extends AppCompatActivity {
    private static final String TAG = "EditDataActivity";
    private Button btnSave,btnDelete,btnShare,callbtn;
    private EditText phoneedit,emailedit,addressedit,descredit;
    private TextView text1;
    DatabaseHelper msavedatabase_helper;
    private String selectedName;
    private int selectedID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.edit_data_layout);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnShare = (Button) findViewById(R.id.btnShare);
        callbtn=(Button)findViewById(R.id.btncall);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        msavedatabase_helper = new DatabaseHelper(this);
        phoneedit = findViewById(R.id.phoneitem);
        emailedit = findViewById(R.id.emailitem);
        addressedit = findViewById(R.id.addressitem);
        descredit = findViewById(R.id.descritem);
        text1=findViewById(R.id.textView6);
        //get the intent extra from the ListDataActivity
        Intent receivedIntent = getIntent();
        //now get the itemID we passed as an extra
        selectedID = receivedIntent.getIntExtra("id",0); //NOTE: -1 is just the default value
        //now get the name we passed as an extra
        selectedName = receivedIntent.getStringExtra("descr");
        //set the text to show the current selected name
        Cursor data = msavedatabase_helper.getItemID(selectedName);
        data.moveToNext();
        emailedit.setText(data.getString(1));
        phoneedit.setText(data.getString(2));
        addressedit.setText(data.getString(3));
        descredit.setText(data.getString(4));
        text1.setText(data.getString(4));
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneitem = phoneedit.getText().toString();
                String emailitem = emailedit.getText().toString();
                String addressitem = addressedit.getText().toString();
                String descritem = descredit.getText().toString();
                String text1 = descredit.getText().toString();

                if(phoneitem.isEmpty()||emailitem.isEmpty()||addressitem.isEmpty()||descritem.isEmpty()||text1.isEmpty()){
                    toastMessage("You must enter all fields");
                }else{
                    msavedatabase_helper.updateName(selectedName,emailitem,phoneitem,addressitem,descritem);
                    toastMessage("UPDATED!");
                   // Intent i =  new Intent(getApplicationContext(),EditDataActivity.class);
                  //  startActivity(i);
                    //finish();
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msavedatabase_helper.deleteName(selectedName);
                toastMessage("RECORD DELETED!");
              //  Intent i =  new Intent(getApplicationContext(),EditDataActivity.class);
                //startActivity(i);
            }
        });
        callbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+phoneedit.getText().toString()));
                if (ActivityCompat.checkSelfPermission(EditDataActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneitem = phoneedit.getText().toString();
                String emailitem = emailedit.getText().toString();
                String addressitem = addressedit.getText().toString();
                String descritem = descredit.getText().toString();
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Information Of The:"+descritem+"\n"+"Address:"+addressitem+"\n"+"Email:"+emailitem+"\n"+"Phone:"+phoneitem);
                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(EditDataActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}