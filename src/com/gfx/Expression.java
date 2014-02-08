package com.gfx;

import android.app.Activity;
import android.os.Bundle;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

public  class Expression
{
    public class keyword{String name;int priority,type;
        public keyword(String n,int p,int t){name=n;priority=p;type=t;}}


    public Hashtable<String,keyword> keywords=new Hashtable<String,keyword>();
    public  String text="0123456789.ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_\\!";

    public void add_keyword(String n,int p,int t){keywords.put(n.toLowerCase(),new keyword(n,p,t));}
    public Boolean is_keyword(String k){return(keywords.containsKey(k.toLowerCase()));}
    public keyword get_keyword(String k){return((keyword)keywords.get(k.toLowerCase()));}

    public int get_priority(String n)
    {
        if(n.length() ==0)
            return(0);
        else if ( n.charAt(0) ==';')
            return(0);
        else if ( n.charAt(0) =='~')
            return(0);
        else if(is_keyword(n))
        {
            keyword k=get_keyword(n);
            if(k.type==2) return(30); else return(k.priority);
        }
        else
            return(30);


    }
    public String[] compile(String exp)
    {


        exp=exp+"~";

        String ch = "";String _txt = "";String op  = "";String _typ = "";String _ltyp = "";String err = "";
        int l  = exp.length();
        List<String> OperatorStack= new ArrayList<String>();
        List<String> Compiled= new ArrayList<String>();
        int[] ArguStack = new int[20];
        int cnt  = 0;
        int OpArgu=0;
        Boolean WaitForOperator = false;
        OperatorStack.add("s");
        int ip  = 0;
        Boolean expEnd = false;
        _txt = "";



        do{
            program:{

                ch=exp.substring(ip,ip+1);

                if(ch==" ") break program;
                _txt = "";_typ = "";
                if(ip == l) ch = "~";


                if("0123456789.".indexOf(ch)>-1)
                {
                    _typ="number";
                    while("0123456789.".indexOf(ch)>-1 && ip<l)
                    {
                        _txt=_txt +ch;ip++;
                        if(ip<=l) ch=exp.substring(ip,ip+1);
                    }
                    ip--;
                }
                else if(ch=="'" || ch=="\"")
                {
                    String ccc=ch;
                    _typ="constant";
                    ch=	exp.substring(ip,ip+1);
                    while(ch!=ccc && ip<=l)
                    {
                        _txt=_txt +ch;ip++;
                        if(ip<=l) ch=exp.substring(ip,ip+1);
                    }

                }
                else if("(),+-*/\\^&;~".indexOf(ch)>-1)
                {
                    _typ="operator";
                    _txt=ch;
                }
                else if("=<>".indexOf(ch)>-1)
                {
                    _typ="operator";
                    while("=<>".indexOf(ch)>-1 && ip<l)
                    {
                        _txt=_txt +ch;ip++;
                        if(ip<=l) ch=exp.substring(ip,ip+1);
                    }
                    ip--;

                }
                else if(text.indexOf(ch)>-1)
                {
                    while(text.indexOf(ch)>-1 && ip<l)
                    {
                        _txt=_txt +ch;ip++;
                        if(ip<=l) ch=exp.substring(ip,ip+1);
                    }
                    ip--;
                    if(is_keyword(_txt)) _typ="keyword"; else _typ="symbol";
                }



                cnt=OperatorStack.size()-1;


                if(!WaitForOperator)
                {
                    if(_typ=="number")
                    {
                        if(OperatorStack.get(cnt)=="_")
                        {Compiled.add("NUM");Compiled.add("-"+_txt);OperatorStack.remove(cnt);}
                        else{Compiled.add("NUM");Compiled.add(_txt);}
                        WaitForOperator = true;
                    }

                    else if(_typ=="number")
                    {
                        Compiled.add("CNS");Compiled.add(_txt);WaitForOperator = true;
                    }
                    else if(_typ=="symbol")
                    {
                        Compiled.add("SYM");Compiled.add(_txt);WaitForOperator = true;
                    }

                    else if(_typ=="operator")
                    {
                        if(_txt.charAt(0) =='(')
                        {
                            OperatorStack.add("(");
                            if (_ltyp != "keyword")
                            {OpArgu++;ArguStack[OpArgu]=1;}
                        }
                        else if(_txt.charAt(0) =='-')
                            OperatorStack.add("_");
                        else
                            err = "Syntax Error";
                    }

                    else if(_typ=="keyword")
                    {
                        OperatorStack.add(_txt);
                        int arg=get_keyword(_txt).priority;
                        if(arg>0) {OpArgu++;ArguStack[OpArgu]=arg;} else WaitForOperator=true;
                    }




                }

                else
                {

                    cnt = OperatorStack.size()- 1;
                    op = OperatorStack.get(cnt);
                    if(_txt.charAt(0) !=')') WaitForOperator=false;

                    while(get_priority(_txt)<=get_priority(op) && get_priority(op)!=255 && cnt>=0)
                    {
                        if(op.length()>0 && !op.equalsIgnoreCase("s")) Compiled.add(op);
                        OperatorStack.remove(cnt);
                        cnt=OperatorStack.size()-1;
                        if(cnt>=0) op=OperatorStack.get(cnt);

                    }
                    if(op.equalsIgnoreCase("n"))
                    {

                    }
                    else if(_txt.equalsIgnoreCase(")"))
                    {
                        ArguStack[OpArgu]= ArguStack[OpArgu]-1;
                        OperatorStack.remove(cnt); cnt=OperatorStack.size()-1; OpArgu--;

                    }
                    else if(_txt.equalsIgnoreCase(","))
                        ArguStack[OpArgu]= ArguStack[OpArgu]-1;
                    else
                        OperatorStack.add(_txt);


                }




                _ltyp=_typ;
            }
            ip++;

        }while(ip<l && err=="" && !expEnd);


        String[] ss=new String[Compiled.size()];
        for(int i=0;i<Compiled.size();i++)
            ss[i]= Compiled.get(i);


        return(ss);



    }

    public void call(String op, Stack ST)
    {

    }
    public void on_init(){}

    public Object execute(String[] _code)
    {

        Object _return =null;
        String op = "";
        Stack ST  = new Stack(40);
        int ip = 0;
        int l = _code.length;
        float _num2 = 0;
        float _num1 = 0;



        while(ip<l)
        {
            op=_code[ip];
            if(op.length()>0)
            {
                if(op.equalsIgnoreCase("NUM"))
                {
                    ST.Push(Float.parseFloat((_code[ip+1])));
                    ip++;
                }

                else if(op.equalsIgnoreCase("SYM"))
                {
                    ST.Push(Double.parseDouble(_code[ip+1]));
                    ip++;
                }
                else
                {
                    if("+_-/*>=<=".indexOf(op)>-1)
                    {
                        _num1=Float.parseFloat(ST.Pop().toString());
                        _num2=Float.parseFloat(ST.Pop().toString());
                    }
                    //***********************************************************
                    if(op.equalsIgnoreCase("+"))
                        ST.Push((_num2 + _num1));
                    else if(op.equalsIgnoreCase("-") || op.equalsIgnoreCase("_"))
                        ST.Push((_num2 - _num1));
                    else if(op.equalsIgnoreCase("*"))
                        ST.Push((_num2 * _num1));
                    else if(op.equalsIgnoreCase("/"))
                        ST.Push((_num2 / _num1));
                    else if(op.equalsIgnoreCase("&"))
                        ST.Push(ST.Pop().toString() +ST.Pop().toString());

                    else if(op.equalsIgnoreCase("="))
                        ST.Push(ST.Pop()== ST.Pop());

                    else if(op.equalsIgnoreCase("<>"))
                        ST.Push(ST.Pop()!= ST.Pop());

                    else if(op.equalsIgnoreCase(">="))
                        ST.Push(_num2 >=_num1);
                    else if(op.equalsIgnoreCase("<="))
                        ST.Push(_num2 >=_num1);

                    else if(op.equalsIgnoreCase(">"))
                        ST.Push(_num2 >_num1);

                    else if(op.equalsIgnoreCase("<"))
                        ST.Push(_num2 <_num1);

                    else if(op.equalsIgnoreCase("AND"))
                        ST.Push((Boolean)ST.Pop() && (Boolean)ST.Pop());

                    else if(op.equalsIgnoreCase("OR"))
                        ST.Push((Boolean)ST.Pop() || (Boolean)ST.Pop());

                    else if(op.equalsIgnoreCase("NOT"))
                    {
                        ST.Pop();
                        ST.Push(!(Boolean)ST.Pop());
                    }
                    else if(op.charAt(0)==';'){

                        // ST.Pop();

                    }
                    else
                    {
                        if(op.equalsIgnoreCase("sin"))
                            ST._MEM[ST.sp]=Math.sin((Double)ST._MEM[ST.sp]);
                        else if(op.equalsIgnoreCase("cos"))
                            ST._MEM[ST.sp]=Math.cos((Double)ST._MEM[ST.sp]);

                        else
                            call(op,ST);
                    }
                    //***********************************************************

                }
            }
            ip++;
        }
        _return=ST.Pop();



        return(_return);
    }


    public String get_string(String str){return(str);}

    public  Expression()
    {


        String[] oprs={"+" , "-" , "/" , "*" , "\\" , "^" ,"&" , "<" ,">" , "<>" , ">=" , "<=" , "=" ,"(" ,"MOD" , "NOT" , "AND" , "OR" , "XOR" , ")" , "S" ,"N" , "," ,"_"};
        String[] pr={"13","13","17","17","16","20","12","10","10","10","10","10","10","255","15","8","7","6","6","0","255", "0","0","18"};
        for(int i=0;i<oprs.length;i++)
        {
            add_keyword(oprs[i],Integer.parseInt(pr[i]),1);
        }

        add_keyword("sin",1,2);
        add_keyword("cos",1,2);

        on_init();

    }


    public class Stack
    {
        public Object[] _MEM;
        public int size = 0;
        public int sp;
        public Stack(int memsize){size = memsize;_MEM = new Object[(size)];}
        public void Push(Object o){sp++;_MEM[sp] = o;}
        public Object Pop(){return(_MEM[sp--]);}
        public Object Peek(int i){return _MEM[i];}
    }



}

