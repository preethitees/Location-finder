package uk.ac.tees.aad.W9457747;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageUpload extends AppCompatActivity {

    private static int CAMERA_UNIC_CODE = 145;

    ImageView imageView;
    Button find;
    Bitmap bitmap;
    private FirebaseFunctions mFunctions;
    FirebaseAuth firebaseAuth;


    ArrayList<Places> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        firebaseAuth = FirebaseAuth.getInstance();
        imageView = findViewById(R.id.imageView2);
        find = findViewById(R.id.upload2);
        find.setVisibility(View.INVISIBLE);
        places = new ArrayList<Places>();

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findLabel();
            }
        });


        Button btn = findViewById(R.id.upload);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenCamera();
            }
        });

    }

    private void findLabel() {

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);


        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        String text ="";
                        for (ImageLabel label : labels) {
                            text = text +","+ label.getText();
                        }
                       Intent intent = new Intent(getApplicationContext(),ListOFAvalilablity.class);
                        intent.putExtra("values",text);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


    private void findImage() {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        mFunctions = FirebaseFunctions.getInstance();


        JsonObject request = new JsonObject();

        JsonObject image = new JsonObject();
        image.add("content", new JsonPrimitive(base64encoded));
        request.add("image", image);

        JsonObject feature = new JsonObject();
        feature.add("maxResults", new JsonPrimitive(5));
        feature.add("type", new JsonPrimitive("LANDMARK_DETECTION"));
        JsonArray features = new JsonArray();
        features.add(feature);
        request.add("features", features);

        annotateImage(request.toString())
                .addOnCompleteListener(new OnCompleteListener<JsonElement>() {
                    @Override
                    public void onComplete(@NonNull Task<JsonElement> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"failed",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();
                            for (JsonElement label : task.getResult().getAsJsonArray().get(0).getAsJsonObject().get("landmarkAnnotations").getAsJsonArray()) {
                                JsonObject labelObj = label.getAsJsonObject();
                                String landmarkName = labelObj.get("description").getAsString();
                                String entityId = labelObj.get("mid").getAsString();
                                float score = labelObj.get("score").getAsFloat();
                                JsonObject bounds = labelObj.get("boundingPoly").getAsJsonObject();
                                double latitude=0;
                                double longitude= 0;
                                for (JsonElement loc : labelObj.get("locations").getAsJsonArray()) {
                                    JsonObject latLng = loc.getAsJsonObject().get("latLng").getAsJsonObject();
                                    latitude = latLng.get("latitude").getAsDouble();
                                    longitude = latLng.get("longitude").getAsDouble();
                                }
                                Places p = new Places();
                                p.setName(landmarkName);
                                p.setLat(longitude);
                                places.add(p);
                                find.setText(places.toString());


                            }


                        }
                    }
                });

    }

    private Task<JsonElement> annotateImage(String requestJson) {
        return mFunctions
                .getHttpsCallable("annotateImage")
                .call(requestJson)
                .continueWith(new Continuation<HttpsCallableResult, JsonElement>() {
                    @Override
                    public JsonElement then(@NonNull Task<HttpsCallableResult> task) {
                        Toast.makeText(getApplicationContext(),task.getResult().getData().toString(),Toast.LENGTH_SHORT).show();
                        return JsonParser.parseString(new Gson().toJson(task.getResult().getData()));
                    }
                });
    }















    private void   OpenCamera()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_UNIC_CODE);
        }else {
            TakePicture();
        }

    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_UNIC_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                TakePicture();
            }else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void TakePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent,200);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == Activity.RESULT_OK){

            bitmap  =(Bitmap) data.getExtras().get("data");

            setimage();
            find.setVisibility(View.VISIBLE);
        }
    }

    private void setimage() {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}
