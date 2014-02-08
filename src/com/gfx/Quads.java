package com.gfx;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Quads {
    public static FloatBuffer quad_texture_coord;
    public static Animator animator;
    public static Boolean prepared=false;

    public static void prepare()
    {
        if(prepared) return;
        float[] texCoords = { // Texture coords for the above face (NEW)
                0.0f, 1.0f,  // A. left-bottom (NEW)
                1.0f, 1.0f,  // B. right-bottom (NEW)
                0.0f, 0.0f,  // C. left-top (NEW)
                1.0f, 0.0f   // D. right-top (NEW)
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(texCoords.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        quad_texture_coord = vbb.asFloatBuffer(); // Convert from byte to float
        quad_texture_coord.put(texCoords);
        quad_texture_coord.position(0);
        texCoords=null;
        animator=new Animator();
        prepared=true;


    }







}
 class Quad {
    private FloatBuffer vertexBuffer;
    public ArrayList<Quad> childs=new ArrayList<Quad>();
    public float [] matrix={
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
    };



    public float rx,ry,rz,sx,sy,sz,cr,cg,cb,ca;
    public Boolean hasChilds=false;
    public Boolean hasAnimation=false;
    public float animation_time,animation_speed;
    public int animation_frame,texture;
    public Animator.AnimationItem animation;


    public void init(float w,float h)
    {
        ByteBuffer vbb = ByteBuffer.allocateDirect(12 * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        setSize(w,h);
        rx=0;ry=0;rz=0;
        cr=1;cg=1;cb=1;ca=1;
        texture=0;


    }
    public Quad() {init(1,1);}
    public Quad(float w,float h) {init(w,h);}

    public Quad setColor(float r,float g,float b,float a){cr=r;cg=g;cb=b;ca=a;return (this); }
    public Quad addChild(Quad c){hasChilds = true;childs.add(c);return (c);}

    public Quad setAnimation(String name,float speed)
    {
        hasAnimation=false;
        animation = null;
        if(Quads.animator.animations.containsKey(name))
        {
           hasAnimation=true;
           animation_speed=speed;
           animation = Quads.animator.animations.get(name);
        }
        return (this);
    }


   public Quad setPosition(float x,float y,float z){matrix[12]=x;matrix[13]=y;matrix[14]=z;return(this);}
   public void setSize(float w,float h)
    {
        float[] r = new float[12];
        r[0]=-w; r[1]=-h;r[2]=0.0f;
        r[3]=w; r[4]=-h;r[5]=0.0f;
        r[6]=-w; r[7]=h;r[8]=0.0f;
        r[9]=w; r[10]=h;r[11]=0.0f;
        vertexBuffer.put(r);
        vertexBuffer.position(0);
        r=null;
    }


    public void animate(float speed) {

        int total_frames =animation.total_frames;

        if(animation_frame>total_frames-2){
            animation_frame=0;
            animation_time=0;
        }


        int frame1,frame2;
        frame1=animation_frame*14;frame2=frame1+14;


        float xt= animation_time-animation.animation[frame1];
        float xd=animation.animation[frame2]-animation.animation[frame1];


        if((animation.attribs & 1)==1){
            matrix[12]=(animation.animation[frame2+1]-animation.animation[frame1+1])*xt/xd+animation.animation[frame1+1];
            matrix[13]=(animation.animation[frame2+2]-animation.animation[frame1+2])*xt/xd+animation.animation[frame1+2];
            matrix[14]=(animation.animation[frame2+3]-animation.animation[frame1+3])*xt/xd+animation.animation[frame1+3];

        }

        if((animation.attribs & 2)==2){
            rx=(animation.animation[frame2+4]-animation.animation[frame1+4])*xt/xd+animation.animation[frame1+4];
            ry=(animation.animation[frame2+5]-animation.animation[frame1+5])*xt/xd+animation.animation[frame1+5];
            rz=(animation.animation[frame2+6]-animation.animation[frame1+6])*xt/xd+animation.animation[frame1+6];

            float COSX=(float) Math.cos(rx);
            float SINX=(float)Math.sin(rx);

            float COSY=(float)Math.cos(ry);
            float SINY=(float)Math.sin(ry);

            float COSZ=(float)Math.cos(rz);
            float SINZ=(float)Math.sin(rz);

            matrix[0] = COSY * COSZ + SINX * SINY * SINZ;
            matrix[1] =-COSX * SINZ;
            matrix[2] =SINX * COSY * SINZ - SINY * COSZ;
            matrix[4] =COSY * SINZ - SINX * SINY * COSZ;
            matrix[5] =COSX * COSZ;
            matrix[6] =-SINY * SINZ - SINX * COSY * COSZ;
            matrix[8] =COSX * SINY;
            matrix[9] =SINX;
            matrix[10] =COSX * COSY;
        }

        if((animation.attribs & 4)==4){
            sx=(animation.animation[frame2+7]-animation.animation[frame1+7])*xt/xd+animation.animation[frame1+7];
            sy=(animation.animation[frame2+8]-animation.animation[frame1+8])*xt/xd+animation.animation[frame1+8];
            sz=(animation.animation[frame2+9]-animation.animation[frame1+9])*xt/xd+animation.animation[frame1+9];

            matrix[0]*=sx;
            matrix[1]*=sy;
            matrix[2]*=sz;

            matrix[4]*=sx;
            matrix[5]*=sy;
            matrix[6]*=sz;

            matrix[8]*=sx;
            matrix[9]*=sy;
            matrix[10]*=sz;


        }

        if((animation.attribs & 8)==8){
            cr=(animation.animation[frame2+10]-animation.animation[frame1+10])*xt/xd+animation.animation[frame1+10];
            cg=(animation.animation[frame2+11]-animation.animation[frame1+11])*xt/xd+animation.animation[frame1+11];
            cb=(animation.animation[frame2+12]-animation.animation[frame1+12])*xt/xd+animation.animation[frame1+12];
            ca=(animation.animation[frame2+13]-animation.animation[frame1+13])*xt/xd+animation.animation[frame1+13];
        }
        animation_time=animation_time+speed;

        if(animation_time>=animation.animation [frame2]) animation_frame++;
    }



    public void draw(GL10 gl)
    {


        if(hasAnimation ) animate(animation_speed);

        gl.glPushMatrix();
        gl.glMultMatrixf(matrix,0);
        gl.glColor4f(cr,cg,cg,ca);

        gl.glBindTexture( GL10.GL_TEXTURE_2D, texture );


        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);


        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);




        if(hasChilds)
        {
            for(int i=0;i<childs.size();i++)
                childs.get(i).draw(gl);
        }

        gl.glPopMatrix();
    }

}

class GBitmap{
    public Bitmap bmp;
    public Canvas canv;
    public static Paint defualtPaint=new Paint();
    public GBitmap(int w,int h)
    {
        bmp=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canv = new Canvas(bmp);
    }
    public void Dispose(){
        canv=null;
        bmp.recycle();
        bmp=null;

    }

}