package com.gfx;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Animator extends Expression
{
    List<Float> anim = new ArrayList<Float>();
    int a_index=0;
    int frames=0;
    int attribs=0;
    float anim_time=0.0f;

    public void call(String op, Expression.Stack ST)
    {
        if(op.equalsIgnoreCase("t"))
        {
            float time=((Float)ST._MEM[ST.sp]);
            anim.add(time);
            if(time>anim_time ) anim_time = time;

            anim.add(0.0f); anim.add(0.0f);anim.add(0.0f);
            anim.add(0.0f); anim.add(0.0f); anim.add(0.0f);

            anim.add(1.0f);anim.add(1.0f);anim.add(1.0f);

            anim.add(1.0f); anim.add(1.0f);anim.add(1.0f);anim.add(1.0f);
            a_index=frames*14;
            if(frames>0)
            {
                int findex=(frames-1)*14;
                for(int i=1;i<14;i++)
                    anim.set(a_index+i,anim.get(findex+i));

            }
            frames++;
        }
        else if(op.equalsIgnoreCase("x"))
        {  anim.set(a_index+1, (Float)ST._MEM[ST.sp]);attribs = attribs | 1;  }
        else if(op.equalsIgnoreCase("y"))
        {  anim.set(a_index+2, (Float)ST._MEM[ST.sp]);  attribs = attribs | 1;  }
        else if(op.equalsIgnoreCase("z"))
        {   anim.set(a_index+3, (Float)ST._MEM[ST.sp]);  attribs = attribs | 1;  }
        else if(op.equalsIgnoreCase("rx"))
        { anim.set(a_index+4, ((Float)ST._MEM[ST.sp])*maths.DEGTORAD  );  attribs = attribs | 2;  }
        else if(op.equalsIgnoreCase("ry"))
        {   anim.set(a_index+5, ((Float)ST._MEM[ST.sp])*maths.DEGTORAD);        attribs = attribs | 2;  }
        else if(op.equalsIgnoreCase("rz"))
        {   anim.set(a_index+6, ((Float)ST._MEM[ST.sp])*maths.DEGTORAD);  attribs = attribs | 2;  }
        else if(op.equalsIgnoreCase("sx"))
        {  anim.set(a_index+7, (Float)ST._MEM[ST.sp]);    attribs = attribs | 4;  }
        else if(op.equalsIgnoreCase("sy"))
        {   anim.set(a_index+8, (Float)ST._MEM[ST.sp]);       attribs = attribs | 4;  }
        else if(op.equalsIgnoreCase("sz"))
        {   anim.set(a_index+9, (Float)ST._MEM[ST.sp]);     attribs = attribs | 4;  }
        else if(op.equalsIgnoreCase("cr"))
            { anim.set(a_index+10, (Float)ST._MEM[ST.sp]); attribs = attribs | 8;  }
        else if(op.equalsIgnoreCase("cg"))
            { anim.set(a_index+11, (Float)ST._MEM[ST.sp]);attribs = attribs | 8;  }
        else if(op.equalsIgnoreCase("cb"))
                {   anim.set(a_index+12, (Float)ST._MEM[ST.sp]);attribs = attribs | 8;  }
        else if(op.equalsIgnoreCase("ca"))
        { anim.set(a_index+13, (Float)ST._MEM[ST.sp]); attribs = attribs | 8;  }




    }

    @Override
    public void on_init()
    {
        super.on_init();
        add_keyword("t", 1, 2);

        add_keyword("x", 1, 2);
        add_keyword("y",1,2);
        add_keyword("z", 1, 2);
        add_keyword("rx",1,2);
        add_keyword("ry",1,2);
        add_keyword("rz", 1, 2);
        add_keyword("sx",1,2);
        add_keyword("sy",1,2);
        add_keyword("sz", 1, 2);
        add_keyword("cr",1,2);
        add_keyword("cg",1,2);
        add_keyword("cb",1,2);
        add_keyword("ca", 1, 2);


    }


    public void add_animation(String name, String script)
    {
        anim_time=0;
        attribs=0;
        frames=0;
        anim.clear();
        String[] pg=compile(script);
        execute(pg);
        pg=null;
        AnimationItem itm=new AnimationItem();
        itm.animation = new float[anim.size() ];

        for(int i=0;i<anim.size();i++)
            itm.animation [i]=anim.get(i);

        itm.attribs = attribs;
        itm.total_frames = frames;
        itm.total_time = anim_time;
        animations.put (name, itm);


    }




    public Hashtable<String,AnimationItem >  animations=new Hashtable<String,AnimationItem>();

    public class AnimationItem
    {
        public float []animation;
        public float total_time;
        public int total_frames;
        public int attribs;
    }
}


