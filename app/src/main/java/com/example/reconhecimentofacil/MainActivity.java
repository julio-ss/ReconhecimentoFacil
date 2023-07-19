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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends CameraActivity {
    private static String TAG = "OpenCV_Log";
    private GifImageView gifImageView;
    private View fontTitle;
    private TextView title;

    private CameraBridgeViewBase cameraBridgeViewBase;
    private CascadeClassifier cascadeClassifier;
    private Mat gray, rgb, transpose_gray, transpose_rgb;
    boolean is_init;

    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
        is_init = false;

        cameraBridgeViewBase = findViewById(R.id.cameraView);
        gifImageView = findViewById(R.id.gifFace);

        cameraBridgeViewBase.setVisibility(View.GONE);

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                rgb = new Mat();
                gray = new Mat();

            }

            @Override
            public void onCameraViewStopped() {
                rgb.release();
                gray.release();

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                rgb = inputFrame.rgba();
                gray = inputFrame.gray();
                
                transpose_gray = gray.t();
                transpose_rgb = rgb.t();

                Core.flip(transpose_rgb, transpose_rgb, 0);

                MatOfRect rects = new MatOfRect();

                cascadeClassifier.detectMultiScale(transpose_gray, rects, 1.08, 2);

                for (Rect rect : rects.toList()){
                    Log.i(TAG, "onCameraFrame: chegou aqui");
                    Mat submat = transpose_rgb.submat(rect);
                    //Imgproc.blur(submat, submat, new Size(20, 20));
                    Imgproc.rectangle(transpose_rgb, rect, new Scalar(0, 255, 0), 3);

                    submat.release();
                }

                return transpose_rgb.t();
            }
        });



    }

    public void initReconhecimento(View v){

        gifImageView.setVisibility(View.INVISIBLE);
        cameraBridgeViewBase.setVisibility(View.VISIBLE);

        //iniciando OpenCV
        if (OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCV INICIALIZADO");
            cameraBridgeViewBase.enableView();

            try {
                InputStream inputStream = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File file = new File(getDir("cascade", MODE_PRIVATE), "lbpcascade_frontalface.xml");
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] data = new byte[4096];
                int read_bytes;

                while((read_bytes = inputStream.read(data)) != -1){
                    fileOutputStream.write(data, 0, read_bytes);
                }

                cascadeClassifier = new CascadeClassifier(file.getAbsolutePath());
                if (cascadeClassifier.empty()){
                    cascadeClassifier = null;
                }

                inputStream.close();
                fileOutputStream.close();
                file.delete();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


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