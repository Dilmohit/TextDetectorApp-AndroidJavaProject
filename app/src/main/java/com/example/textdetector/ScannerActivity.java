package com.example.textdetector;

import static android.Manifest.permission.CAMERA;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Objects;

public class ScannerActivity extends AppCompatActivity {

    // Initialize variables
    private ImageView captureIV;
    private TextView resultTV;
    private Bitmap imageBitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this will hide status bar in this activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_scanner);

        captureIV = findViewById(R.id.idIVCaptureImage);
        resultTV = findViewById(R.id.idIvDetectedText);
        Button snapBTN = findViewById(R.id.idBTNSnap);
        Button detectBTN = findViewById(R.id.idBTNDetect);

        // set onclicklistener
        detectBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (captureIV.getDrawable()==null){
                    Toast.makeText(ScannerActivity.this, "Failed to fetch Image", Toast.LENGTH_SHORT).show();
                }else {
                    detectText();
                }
            }
        });

        // set onclicklistener
        snapBTN.setOnClickListener(v -> {

            if (checkPermission()){
                captureImage();
            }else {
                requestPermission();
            }

        });

    }

    // check Camera Permission
    private boolean checkPermission(){
        int camerpermission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return camerpermission == PackageManager.PERMISSION_GRANTED;
    }

    // Request Camera Permission
    private void requestPermission(){
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_CODE);
    }

    // Open Camera
    @SuppressLint("QueryPermissionsNeeded")
    private void captureImage(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePicture,REQUEST_IMAGE_CAPTURE);
        }
    }

    // Show toast on the user response on Permission Request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0){
            boolean cameraPermision = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraPermision){
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                captureImage();
            }else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // After detecting data add that data to text view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = Objects.requireNonNull(data).getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIV.setImageBitmap(imageBitmap);
        }
    }

    // Detect text in image
    private void detectText(){
        InputImage image = InputImage.fromBitmap(imageBitmap,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(text -> {
            StringBuilder result1 = new StringBuilder();
            for (Text.TextBlock block: text.getTextBlocks()){
                String blockText = block.getText();
                Point[] blockCornerPoint = block.getCornerPoints();
                Rect blockFrame = block.getBoundingBox();
                for (Text.Line line: block.getLines()){
                    String lineText = line.getText();
                    Point[] lineCornerPoint = line.getCornerPoints();
                    Rect linREct = line.getBoundingBox();
                    for (Text.Element element: line.getElements()){
                        String elementText = element.getText();
                        result1.append(elementText);
                    }

                    resultTV.setText(blockText);
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(ScannerActivity.this, "Fail To Detect Text"+e.getMessage(), Toast.LENGTH_SHORT).show());

    }
}