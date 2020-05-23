package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    Button btnDetect;
    AlertDialog waitingDiaglog;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (CameraView) findViewById(R.id.camera_view);
        btnDetect = (Button) findViewById(R.id.btn_detect);

        waitingDiaglog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Please waiting...")
                .setCancelable(false).build();
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDiaglog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                runDetector(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });
    }

    private void runDetector(Bitmap bitmap) {

        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        new Helper(new Helper.Consumer() {
            @Override
            public void accept(boolean internet) {
                if (internet) {
                    FirebaseVisionCloudDetectorOptions options =
                            new FirebaseVisionCloudDetectorOptions.Builder()
                                    .setMaxResults(1)
                                    .build();
                    FirebaseVisionImageLabeler detector =
                            FirebaseVision.getInstance().getOnDeviceImageLabeler();
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                                    processDataResultCloud(firebaseVisionImageLabels);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("EDMTERROR", e.getMessage());
                                }
                            });


                }
                else
                {
                    FirebaseVisionOnDeviceImageLabelerOptions options =
                            new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                                    .setConfidenceThreshold(0)
                                    .build();
                    FirebaseVisionImageLabeler detector =
                            FirebaseVision.getInstance().getOnDeviceImageLabeler();

                    detector.processImage(image)
                       .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                           @Override
                           public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                               processDataResult(firebaseVisionImageLabels);
                           }
                       })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("EDMTERROR", e.getMessage());
                                }
                            });

                }
            }
        });
    }

    private void processDataResultCloud(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
    for(FirebaseVisionImageLabel label: firebaseVisionImageLabels){

        Toast.makeText(this, "Cloud result: "+label.getText(), Toast.LENGTH_SHORT).show();
    }
    if(waitingDiaglog.isShowing())
    waitingDiaglog.dismiss();
}


    private void processDataResult(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
        for(FirebaseVisionImageLabel label: firebaseVisionImageLabels){

            Toast.makeText(this, "Device result: "+label.getText(), Toast.LENGTH_SHORT).show();
        }
        if(waitingDiaglog.isShowing())
            waitingDiaglog.dismiss();
    }

}

