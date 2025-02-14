package com.example.ex07_plane;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.viewpager.widget.PagerAdapter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SeekBar seekBar;
    TextView myTextView;
    GLSurfaceView mSurfaceView;
    MainRenderer mRenderer;

    Session mSession;
    Config mConfig;

    boolean mUserRequestedInstall = true;
    boolean isPlaneDetected = false;

    float mCurrentX, mCurrentY;
    boolean mTouched = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.glsurfaceview);
        myTextView = findViewById(R.id.myTextView);
        seekBar = findViewById(R.id.seekBar);
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if(displayManager != null){
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {

                }

                @Override
                public void onDisplayRemoved(int displayId) {

                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized(this){
                        mRenderer.mViewportChanged = true;
                    }
                }
            },null);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mRenderer.mObj.setLightIntensity((float) progress/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mRenderer = new MainRenderer(this, new MainRenderer.RenderCallback() {
            @Override
            public void preRender() {
                if(mRenderer.mViewportChanged){
                   Display display = getWindowManager().getDefaultDisplay();
                   int displayRotation = display.getRotation();
                   mRenderer.updateSession(mSession, displayRotation);
                }

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = null;

                try{
                    frame = mSession.update();
                } catch (CameraNotAvailableException e){
                    e.printStackTrace();
                }

                if(frame.hasDisplayGeometryChanged()){
                    mRenderer.mCamera.transformDisplayGeometry(frame);
                }

                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.mPointCloud.update(pointCloud);
                pointCloud.release();


                // 터치하였다면
                if(mTouched){

                    LightEstimate estimate = frame.getLightEstimate();
                    // LightEstimate :: 빛에 대한 정보를 가지는 클래스
                    // getPixelIntensity() :: 빛의 강도 0.0 ~ 1.0

//                    float lightIntensity = estimate.getPixelIntensity();


                    float [] colorCorrection = new float[4];
                    // 빛의 색깔 가져오기
                    estimate.getColorCorrection(colorCorrection, 0);
                    List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);
                    // 증강현실로 얻어온 좌표가 plane이 어디에 있는지 맞추고 그 위에 올려줘야함.
                    for(HitResult result : results){
                        Pose pose = result.getHitPose(); // 증강공간에서의 좌표
                        float [] modelMatrix = new float[16];
                        pose.toMatrix(modelMatrix, 0);  // 좌표를 가지고 matrix화 함.
                        // 증강공간의 좌표에 객체가 있는지 받아온다. ex) Plane이 있는가?
                        Trackable trackable = result.getTrackable();

                        // scale 조정 단, 다시 되돌릴 때는 다시 계산해야함.
                        // => 원본을 갖고있어야하는 불편함이 생김.
                        Matrix.scaleM(modelMatrix, 0, 0.05f, 0.05f, 0.05f);
                        // 이동 (거리)

                        // 회전 (각도)                   옵셋     각도   축
                        Matrix.rotateM(modelMatrix, 0, 45, 0f, 0f, 1f);
                        // 좌표에 걸린 객체가 Plane 인가?
                        if(trackable instanceof Plane &&
                                // Plane 폴리곤(면) 안에 좌표가 있는가?
                                ((Plane) trackable).isPoseInPolygon(pose)
                        ){
                            // 큐브의 modelMatrix를 터치한 증강현실 modelMatrix로 설정
                            // cube의 model matrix 를 내가 터치한 위치의 matrix로 update 시켜줌.
//                            mRenderer.mCube.setModelMatrix(modelMatrix);

                            // 빛의 세기값을 넘긴다.
//                            mRenderer.mObj.setLightIntensity(lightIntensity);

                            // 빛의 색을 magenta로 강제화 시킴
//                            mRenderer.mObj.setColorCorrection(new float[]{1.0f, 0.0f, 1.0f, 1.0f});
//                            mRenderer.mObj.setColorCorrection(colorCorrection);
                            mRenderer.mObj.setModelMatrix(modelMatrix);
                        }

                    }

                    mTouched = false;


                }


                // Session으로부터 증강현실 속에서의 평면이나 점 객체를 얻을 수 있다.
                //                          Plane,  point
                Collection<Plane> planes = mSession.getAllTrackables(Plane.class);



                // ARCore 상의 Plane들을 얻는다.
                for (Plane plane :planes) {

                    // plane이 정상이라면
//                                                    평면 찾음              다른 평면이 존재하냐?   없으면
                    if(plane.getTrackingState() == TrackingState.TRACKING && plane.getSubsumedBy() == null){
                        isPlaneDetected = true;
                        // 렌더링에서 plane 정보를 갱신하여 출력
                        mRenderer.mPlane.update(plane);
                    }
                }

                if(isPlaneDetected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTextView.setText("평면을 찾았어욤!!!");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTextView.setText("평면이 어디로 간거야~~~");
                        }
                    });
                }

                Camera camera = frame.getCamera();
                float [] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);
                float[] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix, 0);

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);

            }
        });

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        mSurfaceView.setRenderer(mRenderer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCameraPermission();
        try {
        if(mSession == null){
            switch(ArCoreApk.getInstance().requestInstall(this, true)){
                case INSTALLED:
                    mSession = new Session(this);
                    Log.d("메인", "ARCore session 생성");
                    break;
                case INSTALL_REQUESTED:
                    Log.d("메인","ARCore 설치 필요");
                    mUserRequestedInstall = false;
                    break;
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mConfig = new Config(mSession);
        mSession.configure(mConfig);
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mSurfaceView.onResume();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        mSession.pause();

    }

    void hideStatusBarAndTitleBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    void requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    0
                    );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            mTouched = true;
            mCurrentX = event.getX();
            mCurrentY = event.getY();


        }

        return true;
    }

    public void btnClick(View view){
        float[] newColor = new float[4];
        ColorDrawable cd = (ColorDrawable) view.getBackground();
        int color = cd.getColor();
        newColor[0] = Color.red(color)/255f;
        newColor[1] = Color.green(color)/255f;
        newColor[2] = Color.blue(color)/255f;
        newColor[3] = Color.alpha(color)/255f;

        mRenderer.mObj.setColorCorrection(newColor);
    }
}