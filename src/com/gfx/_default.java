package com.gfx;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.htc.view.DisplaySetting;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class _default extends quad1 {}

//========================================================================================================
class _3d1 extends   opengl_program
{

    Square s1;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config){
        super.onSurfaceCreated(gl,config);
        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        s1=new Square();
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL10.GL_DEPTH_TEST);

    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl,width, height);
        gl.glEnable(GL10.GL_BLEND);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT); // | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );
        if(s1.rz<360.0f) s1.rz=s1.rz+1.0f; else s1.rz=0.0f;
        gl.glPushMatrix();
        gl.glTranslatef(0.0f,0.0f,-10.0f);
       // glView.draw_quad(gl,1,0.4f);
        s1.draw(gl);



        gl.glPopMatrix();

        /*
        glView.begin_ortho(gl);

        glText.begin( 1.0f, 1.0f, 1.0f, 1.0f );         // Begin Text Rendering (Set Color WHITE)
       // glText.draw( "Touch "+touch_x+","+touch_y, 0, 0 );          // Draw Test String
        glText.end();                                   // End Text Rendering
        glView.end_ortho(gl);

        */



    }



    public class Square {
        private FloatBuffer vertexBuffer;  // Buffer for vertex-array

        private float[] vertices = {  // Vertices for the square
                -1.0f, -1.0f,  0.0f,  // 0. left-bottom
                1.0f, -1.0f,  0.0f,  // 1. right-bottom
                -1.0f,  1.0f,  0.0f,  // 2. left-top
                1.0f,  1.0f,  0.0f   // 3. right-top
        };

        // Constructor - Setup the vertex buffer
        public Square() {
            // Setup vertex array buffer. Vertices in float. A float has 4 bytes
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder()); // Use native byte order
            vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
            vertexBuffer.put(vertices);         // Copy data into buffer
            vertexBuffer.position(0);           // Rewind
            rx=0;ry=0;rz=0;
            px=0;py=0;pz=-10.0f;


        }

        // Render the shape
        public void draw(GL10 gl) {
            // Enable vertex-array and define its buffer
            gl.glPushMatrix();



            gl.glTranslatef(px,py,pz);
            gl.glRotatef(rx,1.0f,0.0f,0.0f);
            gl.glRotatef(ry,0.0f,1.0f,0.0f);
            gl.glRotatef(rz,0.0f,0.0f,1.0f);


            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer (3,  GL10.GL_FLOAT, 0, vertexBuffer);
            // Draw the primitives from the vertex-array directly
            gl.glColor4f(0,1,1,0.4f);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glPopMatrix();

        }

        public   float rx,ry,rz;
        public   float px,py,pz;


    }

}

  //===============================================================================================

class quad1 extends  opengl_program
{
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config){
        super.onSurfaceCreated(gl, config);
        gl.glClearColor(0.2f, 0.2f, 0.2f, 0.5f);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        desk = new Quad();
        Quads.animator.add_animation("a1","t(0);rz(0);ca(0.1);sx(0.5);sy(0.5);t(50);rz(180);ca(0.9);sx(1);sy(1);t(100);rz(360);ca(0.1);sx(0.5);sy(0.5)");

        Quads.animator.add_animation("b1","t(0);x(-2);t(50);x(2);t(100);x(-2)");

        desk.setPosition(0,0,-6).setAnimation("b1",0.5f) ;
        desk.addChild(new Quad(0.4f,0.2f)).setAnimation("a1",1.0f);

        desk.texture=newTexture(gl);

        GBitmap gb=new GBitmap(100,100);

        //GBitmap.defualtPaint.setStrokeWidth(10);

       // gb.canv.drawRGB(100,200,100);

        GBitmap.defualtPaint.setAntiAlias(true);

        GBitmap.defualtPaint.setColor(Color.GRAY);

        gb.canv.drawCircle(50,50,30,GBitmap.defualtPaint );
        GBitmap.defualtPaint.setColor(Color.YELLOW);

        gb.canv.drawText("Asif",1,10,GBitmap.defualtPaint);

        loadTexture(gl,desk.texture,gb.bmp);
        gb.Dispose();
        gb=null;


    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl,width, height);
        gl.glEnable(GL10.GL_BLEND);
    }
    //===========================================================

    private Quad desk;
    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT); // | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, Quads.quad_texture_coord);
        gl.glPushMatrix();
        gl.glTranslatef(0,0,-5.0f);
        desk.draw(gl);
        gl.glPopMatrix();

        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // Disable texture-coords-array (NEW)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

    }

}