package com.example.manishsb.visualfy;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Manish S B on 21-03-2018.
 */


class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {


private Camera  camera;
private SurfaceHolder surfaceHolder;

public ImageSurfaceView(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        }


public void surfaceCreated(SurfaceHolder holder) {
        try {
        this.camera.setPreviewDisplay(holder);
        camera.setDisplayOrientation(90);
        this.camera.startPreview();
        } catch (IOException e) {
        e.printStackTrace();
        }
}


public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }


public void surfaceDestroyed(SurfaceHolder holder) {
        this.camera.stopPreview();
        this.camera.release();
        }


        }

