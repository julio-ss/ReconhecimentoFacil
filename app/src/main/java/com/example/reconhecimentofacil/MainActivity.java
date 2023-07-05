package com.example.reconhecimentofacil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Collections;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends CameraActivity {
    private static String TAG = "OpenCV_Log";
    private GifImageView gifImageView;
    private View fontTitle;
    private TextView title;

    private CameraBridgeViewBase cameraBridgeViewBase;

    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();

        cameraBridgeViewBase = findViewById(R.id.cameraView);
        gifImageView = findViewById(R.id.gifFace);

        cameraBridgeViewBase.setVisibility(View.GONE);

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                return inputFrame.rgba();
            }
        });



    }

    public void initReconhecimento(View v){

        gifImageView.setVisibility(View.GONE);
        cameraBridgeViewBase.setVisibility(View.VISIBLE);

        //iniciando OpenCV
        if (OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCV INICIALIZADO");
            cameraBridgeViewBase.enableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    void getPermission(){
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA
            }, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }




}