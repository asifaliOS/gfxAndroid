package com.gfx;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.view.Window;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class opengl_program extends Activity {


    protected GLText glText;

    protected  _3dSurface glView;   // Use GLSurfaceView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        glView = new _3dSurface(this);
        glView.program = this;
        this.setContentView(glView);
        Quads.prepare();
    }


    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {


        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        glText = new GLText( gl,glView.context.getAssets() );
        glText.load( "Roboto-Regular.ttf", 18, 2, 2 );




    }
    public void onDrawFrame(GL10 gl) { gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);}


    protected float[] mat_prj=new float[16];
    public void onSurfaceChanged(GL10 gl, int width, int height) {


        gl.glViewport(0, 0, width, height);
        float aspect = (float)width / (float)height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45, aspect, 0.1f, 100.f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

   @Override
    protected void onPause() {super.onPause();glView.onPause(); }
    @Override
    protected void onResume() {super.onResume();glView.onResume(); }

    public int newTexture(GL10 gl)
    {
        int [] Texture=new int[1];
        gl.glGenTextures(1,Texture,0);

        return (Texture[0]);

    }

    public void loadTexture(GL10 gl,int texture,Bitmap bmp)
    {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,  GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);

    }

    public boolean onTouchEvent(MotionEvent event) {return(false);}

    //================================================================================
    public class _3dSurface extends GLSurfaceView implements GLSurfaceView.Renderer {

        public int width,height;
        public  opengl_program program;
        private Context context;
        private SurfaceHolder holder;
        private void init() {
            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
            this.setRenderer(this);
            this.requestFocus();
            this.setFocusableInTouchMode(true);
         }

        public _3dSurface(Context context) {super(context);this.context = context;init();}
        public _3dSurface(Context context, AttributeSet attrs) {super(context, attrs);this.context = context;init();}

        public void onDrawFrame(GL10 gl) {program.onDrawFrame(gl); }
        public void onSurfaceChanged(GL10 gl, int w, int h) {program.onSurfaceChanged(gl,w,h);}
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {super.surfaceChanged(holder, format, w, h);width=w;height=h;}
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {program.onSurfaceCreated(gl,config);}

        //========================================================================================
        @Override
        public boolean onTouchEvent(MotionEvent event) {


            switch (event.getAction())
            {


            }




            return (program.onTouchEvent(event));

        }
       //===================================================================
       public void end_ortho(GL10 gl)
       {
           gl.glMatrixMode( GL10.GL_PROJECTION );
           gl.glPopMatrix();
           gl.glMatrixMode(GL10.GL_MODELVIEW);
           gl.glPopMatrix();

       }
        public void begin_ortho(GL10 gl)
       {

           gl.glMatrixMode( GL10.GL_PROJECTION );
           gl.glPushMatrix();
           gl.glLoadIdentity();
           gl.glOrthof( 0, width,0, height,1.0f, -1.0f);
           gl.glMatrixMode(GL10.GL_MODELVIEW);
           gl.glPushMatrix();
           gl.glLoadIdentity();
       }
        public void set_perspective(GL10 gl)
        {

            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glPopMatrix();

        }
    }




}