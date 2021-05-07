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
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;


public class ImageUpload extends AppCompatActivity {

    private static int CAMERA_UNIC_CODE = 145;

    ImageView imageView;
    Button find;
    Bitmap bitmap;
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

        Button btnn = findViewById(R.id.buttonLogout);
        btnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
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
