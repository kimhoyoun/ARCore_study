package com.example.ex02_homework;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer  implements GLSurfaceView.Renderer {
    MyPicture myBox;
    MyPicture myBox2;
    MyPicture myBox3;
    MyPicture myBox4;
    MyPicture myBox5;

    // 밑에 두개를 가지고 처음껄 만든다
    // 한번에 처리하기 어려우니까 각각 처리를하고 만들어 줄것.
    float [] mMVPMatrix = new float[16];
    float [] mProjectionMatrix = new float[16];
    float [] mViewMatrix = new float[16];

    // 얼굴
    float[] squareCoords = {
            -0.45f,0f,0f,
            -0.45f,-0.4f,0f,
            0.45f,0f,0f,
            0.45f,-0.4f,0f,
            -0.25f,-0.4f,0f,
            -0.25f,-0.6f,0f,
            0.25f,-0.4f,0f,
            0.25f,-0.6f,0f     // 7
    };
    float[] color = {0.792f, 0.796f, 0.807f, 1.0f};
    short[] drawOrder = {0,1,2, 2,1,3, 1,4,5, 4,5,6, 6,5,7, 6,7,3};

    // 모자틀
    float[] squareCoords2 = {
            -0.5f,0f,0f,
            -0.5f,0.1f,0f,
            0.5f,0.1f,0f,
            0.5f,0f,0f


    };

    // 색깔


    float[] color2 = {0.168f, 0.168f, 0.168f, 1.0f};
    short[] drawOrder2 = {0,1,3, 3,2,1,
            };

    // 모자
    float[] squareCoords3 = {
            -0.5f,0.1f,0f,
            -0.3f,0.1f,0f,
            -0.3f,0.4f,0f,
            0.5f,0.1f,0f,
            0.3f,0.1f,0f,
            0.3f,0.4f,0f,
            0f,0.5f,0f,



    };

    // 색깔

    float[] color3 = {0.0f, 0.0f, 0.0f, 1.0f};
    short[] drawOrder3 = {0,1,2, 3,4,5, 1,2,4, 4,5,2, 2,5,6};

    // 뿔, 입
    float[] squareCoords4 = {
            -0.5f, 0.7f, 0f,
            -0.5f, 0.1f, 0f,
            -0.3f, 0.4f, 0f,
            0.5f, 0.7f, 0f,
            0.5f, 0.1f, 0f,
            0.3f, 0.4f, 0f,
            0f,-0.5f,0f,
            -0.1f,-0.55f,0f,
            0.1f,-0.55f,0f

    };

    // 색깔

    float[] color4 = {1.0f, 0.0f, 0.0f, 1.0f};
    short[] drawOrder4 = {0,1,2, 3,4,5, 6,7,8};

    // 눈, 코
    float[] squareCoords5 = {
           -0.4f, 0f, 0f,
            -0.1f, 0f, 0f,
            -0.25f, -0.1f, 0f,
            0.4f, 0f, 0f,
            0.1f, 0f, 0f,
            0.25f, -0.1f, 0f,
            0f,-0.1f,0f,
            -0.1f,-0.4f,0f,
            0.1f,-0.4f,0f,
            0f,-0.34f,-0.05f,
            -0.25f, -0.05f, -0.05f,
            0.25f, -0.05f, -0.05f,
    };

    // 색깔

    float[] color5 = {0.0f, 0.0f, 1.0f, 1.0f};
    short[] drawOrder5 = {0,1,2, 3,4,5, 6,7,8,
                        6,7,9, 6,8,9, 7,8,9,
                        0,1,10, 1,2,10, 0,2,10,
                        3,4,11, 4,5,11, 3,5,11};


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);

        myBox = new MyPicture(squareCoords, color, drawOrder);
        myBox2 = new MyPicture(squareCoords2, color2, drawOrder2);
        myBox3 = new MyPicture(squareCoords3, color3, drawOrder3);
        myBox4 = new MyPicture(squareCoords4, color4, drawOrder4);
        myBox5 = new MyPicture(squareCoords5, color5, drawOrder5);

    }

    // 화면갱신 되면서 화면에서 배치
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        // 비율을 잡는다
        float ratio = (float) width/height;

        // 사각형의 중심을 어떻게해서 그리겠다.
        // 어떻게 넣어주겟느냐
        Matrix.frustumM(mProjectionMatrix, 0,ratio,-ratio,-1,1,3,7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        // 그리기위해서 시점의 값을 잡아주고 카메라의 위치를 정한다.
        // 카메라 위치, 카메라 시선, 카메라 정방향 필요
        Matrix.setLookAtM(mViewMatrix,0, // 배열의 시작 위치
                // x, y, z
                0,5,-3, // 카메라 위치 : -3이므로 위에서 뒤로 빠져있는 것
                0,0,0, // 카메라 시선
                0,1,0 // 카메라 윗방향 : y축 정방향이 윗방향
        );

        // 합치기
        // 두개(mProjectionMatrix, mViewMatrix)를 가져와 각각 0번째값을 곱해서 mMVPMatrix의 0번째부터 저장한다.
        Matrix.multiplyMM(mMVPMatrix,0,mProjectionMatrix,0,mViewMatrix,0);
        // 그리기(정사각형 그리기)
        myBox.draw(mMVPMatrix);
        myBox3.draw(mMVPMatrix);
        myBox4.draw(mMVPMatrix);
        myBox5.draw(mMVPMatrix);
        myBox2.draw(mMVPMatrix);
    }

    // GPU를 이용하여 그리기를 연산한다.
    static int loadShader(int type, String shaderCode){

        int res = GLES20.glCreateShader(type);

        GLES20.glShaderSource(res, shaderCode);
        GLES20.glCompileShader(res);

        return res;
    }

}
