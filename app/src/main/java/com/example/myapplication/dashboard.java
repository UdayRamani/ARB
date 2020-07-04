package com.example.myapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class dashboard extends AppCompatActivity {
    private static final String TAG = "CapturePicture";
    static final int REQUEST_PICTURE_CAPTURE = 1;
    private String pictureFilePath = null;
    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashbord);
        queue = Volley.newRequestQueue(this);
    }
    public void openCaptureCardIntent(View v){
        sendTakePictureIntent();
    }
    private void sendTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Photo file can't be created, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.myapplication", pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }
    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "ZOFTINO_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                decodeaAndUpload();
            }
        }
    }
    private void decodeaAndUpload() {
        final File f = new File(pictureFilePath);
        Bitmap picBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        HashMap<String, String> imageData = new HashMap<>();
        byte[] byteArray = getStreamByteFromImage(f);
        final String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        imageData.put("base64", encoded);
        imageData.put("username", "user123");
        makeNetworkCallForTrainingTheImage(imageData, pictureFilePath);
        //makeNetworkCallTOGCP(encoded);
        //imageRecog(picBitmap);
    }
    public void imageRecog(Bitmap pic){
        Log.d("Image Target", "TEXT Appears Here");
        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(pic);
        FirebaseVision.getInstance().getOnDeviceTextRecognizer().processImage(firebaseVisionImage)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            ProcessTextResult(firebaseVisionText);
                            //Toast.makeText(dashboard.this, firebaseVisionText.getTextBlocks().size(), Toast.LENGTH_SHORT).show();
                            //Toast.makeText(dashboard.this, firebaseVisionText.getTextBlocks().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(dashboard.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void ProcessTextResult(FirebaseVisionText text){
        if(text.getTextBlocks().size() == 0){
            Log.v("TEXT", "No Text Found");
        }
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        for(FirebaseVisionText.TextBlock block : blocks){
            List<FirebaseVisionText.Line> lines = block.getLines();
            for(FirebaseVisionText.Line line : lines){
                Log.v("Line", line.getText());
            }
            Log.v("TEXT",block.getText());
        }
    }
    public static byte[] getStreamByteFromImage(final File imageFile) {
        Bitmap photoBitmap = BitmapFactory.decodeFile(imageFile.getPath());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int imageRotation = getImageRotation(imageFile);
        if (imageRotation != 0)
            photoBitmap = getBitmapRotatedByDegree(photoBitmap, imageRotation);
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
    private static int getImageRotation(final File imageFile) {
        ExifInterface exif = null;
        int exifRotation = 0;
        try {
            exif = new ExifInterface(imageFile.getPath());
            exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif == null)
            return 0;
        else
            return exifToDegrees(exifRotation);
    }
    private static int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;
        return 0;
    }
    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    public void makeNetworkCallForTrainingTheImage(final HashMap<String, String> imageData, final String imageLocationpath) {
        String url = "http://" + DataManager.getHost() + "/extractContacts";
        StringRequest makeRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.v("FB IMAGE UPLOAD ", res);
                System.out.println("----------------FB IMAGE UPLOAD " + res);
                Intent intnt = new Intent(dashboard.this, information.class);
                intnt.putExtra("responseJson", res);
                intnt.putExtra("imageLocation", imageLocationpath);
                startActivity(intnt);
                //Toast.makeText(Dashboard.this, "GOT RESPONSE", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Once the request is performed, failed code over here is executed
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.i("LENGTH", "----------" + imageData.size());
                return imageData;
            }
        };
        makeRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(makeRequest);
    }
    public void makeNetworkCallTOGCP(String base64Data){
        String url = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyC7RV2_bMGbvNNUuy4DBWWxfsr3KQDq-iA";
        String jsonFormed = "{\n" +
                "  \"requests\":[\n" +
                "    {\n" +
                "      \"image\":{\n" +
                "        \"content\": \""+base64Data+"\"\n" +
                "      },\n" +
                "      \"features\": [\n" +
                "        {\n" +
                "          \"type\":\"TEXT_DETECTION\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        System.out.println(jsonFormed);
        sendPost(url, jsonFormed);
    }
    public void sendPost(final String locationURL, final String base64Data) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(locationURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(base64Data);
                    os.flush();
                    os.close();
                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    public void familys(View view)
    {
        Intent intent = new Intent(getApplicationContext(), information_Ar.class);
        startActivity(intent);
    }
    public void saved(View view)
    {
        Intent intent = new Intent(getApplicationContext(), ListDataActivity.class);
        startActivity(intent);
    }
}