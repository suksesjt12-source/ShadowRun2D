package com.example.shadowrun2d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends View {
    private final Paint p = new Paint(3);
    private final List<RectF> platforms = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private float px=90, py=250, vx=0, vy=0, groundY=0;
    private float world=0, camera=0;
    private int health=3, score=0;
    private boolean left=false, right=false, jump=false, gameOver=false, won=false;
    private long last=0;
    private final float playerW=34, playerH=48;

    public GameView(Context c) {
        super(c);
        setFocusable(true);
        platforms.add(new RectF(0,0,520,0));
        platforms.add(new RectF(600,0,900,0));
        platforms.add(new RectF(1010,0,1330,0));
        platforms.add(new RectF(1450,0,1800,0));
        platforms.add(new RectF(1920,0,2300,0));
        platforms.add(new RectF(2420,0,2900,0));
        for (int i=0;i<14;i++) coins.add(new Coin(280+i*165, 240-(i%3)*55));
        enemies.add(new Enemy(700, 0, 650, 850));
        enemies.add(new Enemy(1580, 0, 1500, 1780));
        enemies.add(new Enemy(2550, 0, 2470, 2800));
    }

    private void setupWorld(float h) {
        if (groundY==0) {
            groundY=h-72;
            for (RectF r:platforms) {
                float y = (r.left==0 || r.left==1010 || r.left==1920 || r.left==2420) ? groundY : groundY-90;
                r.top=y; r.bottom=y+22;
            }
            for (Enemy e:enemies) e.y=groundY-34;
        }
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(), h=getHeight();
        setupWorld(h);
        long now=System.nanoTime();
        float dt=last==0?0.016f:Math.min(0.033f,(now-last)/1_000_000_000f);
        last=now;
        update(dt,w,h);
        c.drawColor(0xFF10182B);
        // distant skyline
        p.setColor(0xFF182844);
        for(int i=0;i<12;i++) {
            float bx=i*150-(camera*0.25f)%150;
            c.drawRect(bx,h-210,bx+65,h-72,p);
            c.drawRect(bx+75,h-165,bx+125,h-72,p);
        }
        c.save(); c.translate(-camera,0);
        p.setColor(0xFF344B66);
        for(RectF r:platforms) c.drawRoundRect(r,7,7,p);
        p.setColor(0xFF58C4A5);
        for(RectF r:platforms) c.drawRect(r.left,r.top,r.right,r.top+5,p);
        for(Coin coin:coins) if(!coin.taken) {
            p.setColor(0xFFFFD166); c.drawCircle(coin.x,coin.y,10,p);
            p.setColor(0xFFFFF0B3); c.drawCircle(coin.x-3,coin.y-3,3,p);
        }
        for(Enemy e:enemies) {
            p.setColor(0xFFE76F51); c.drawRoundRect(e.x,e.y,e.x+32,e.y+32,8,8,p);
            p.setColor(0xFFFFFFFF); c.drawCircle(e.x+9,e.y+11,3,p); c.drawCircle(e.x+23,e.y+11,3,p);
        }
        // player
        p.setColor(0xFF67B7FF); c.drawRoundRect(px,py,px+playerW,py+playerH,9,9,p);
        p.setColor(0xFFFFFFFF); c.drawCircle(px+23,py+13,4,p);
        c.restore();
        drawHud(c,w,h);
        drawControls(c,w,h);
        if(gameOver || won) drawEnd(c,w,h);
        postInvalidateOnAnimation();
    }

    private void update(float dt,float w,float h) {
        if(gameOver||won) return;
        float speed=235;
        vx=(right?speed:0)-(left?speed:0);
        px += vx*dt;
        if(px<0) px=0;
        vy += 1050*dt;
        if(jump && onGround()) { vy=-500; }
        jump=false;
        float oldBottom=py+playerH;
        py += vy*dt;
        boolean landed=false;
        for(RectF r:platforms) {
            if(px+playerW>r.left && px<r.right && oldBottom<=r.top+8 && py+playerH>=r.top && vy>=0) {
                py=r.top-playerH; vy=0; landed=true;
            }
        }
        if(py>h+100) hurt();
        for(Coin coin:coins) if(!coin.taken && Math.abs((px+playerW/2)-coin.x)<27 && Math.abs((py+playerH/2)-coin.y)<32) {
            coin.taken=true; score+=10;
        }
        for(Enemy e:enemies) {
            e.x += e.dir*85*dt;
            if(e.x<e.min || e.x>e.max) e.dir*=-1;
            RectF a=new RectF(px,py,px+playerW,py+playerH), b=new RectF(e.x,e.y,e.x+32,e.y+32);
            if(RectF.intersects(a,b)) {
                if(vy>80 && oldBottom<e.y+12) { e.dead=true; vy=-310; score+=50; }
                else hurt();
            }
        }
        Iterator<Enemy> it=enemies.iterator(); while(it.hasNext()) if(it.next().dead) it.remove();
        camera=Math.max(0,Math.min(2200,px-w*0.36f));
        if(px>2820) won=true;
    }

    private boolean onGround() {
        for(RectF r:platforms) if(px+playerW>r.left && px<r.right && Math.abs(py+playerH-r.top)<3) return true;
        return false;
    }
    private void hurt() {
        health--;
        px=Math.max(20,px-100); py=groundY-playerH-2; vy=0;
        if(health<=0) gameOver=true;
    }
    private void drawHud(Canvas c,float w,float h) {
        p.setColor(0xFFFFFFFF); p.setTextSize(22); c.drawText("SHADOW RUN",22,32,p);
        p.setTextSize(18); c.drawText("HP: "+health+"     COINS: "+score,22,58,p);
        p.setTextSize(14); c.drawText("Reach the glowing exit on the right",w-265,28,p);
    }
    private void drawControls(Canvas c,float w,float h) {
        p.setColor(0x775FFFFFF); p.setStyle(Paint.Style.FILL);
        button(c,65,h-70,"◀"); button(c,165,h-70,"▶"); button(c,w-85,h-70,"JUMP");
    }
    private void button(Canvas c,float x,float y,String s) {
        p.setColor(0x665FFFFFF); c.drawCircle(x,y,37,p);
        p.setColor(0xFFFFFFFF); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(s.equals("JUMP")?13:28);
        c.drawText(s,x,y+7,p); p.setTextAlign(Paint.Align.LEFT);
    }
    private void drawEnd(Canvas c,float w,float h) {
        p.setColor(0xCC08101F); c.drawRect(0,0,w,h,p);
        p.setColor(0xFFFFFFFF); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(34);
        c.drawText(won?"LEVEL COMPLETE!":"GAME OVER",w/2,h/2-12,p);
        p.setTextSize(18); c.drawText("Tap anywhere to restart",w/2,h/2+28,p);
        p.setTextAlign(Paint.Align.LEFT);
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        float x=e.getX(), y=e.getY(), w=getWidth(), h=getHeight();
        if(gameOver||won) { if(e.getAction()==MotionEvent.ACTION_DOWN) restart(); return true; }
        int a=e.getActionMasked();
        if(a==MotionEvent.ACTION_DOWN||a==MotionEvent.ACTION_POINTER_DOWN||a==MotionEvent.ACTION_MOVE) {
            left=right=jump=false;
            for(int i=0;i<e.getPointerCount();i++) {
                float tx=e.getX(i), ty=e.getY(i);
                if(ty>h-125) {
                    if(tx<115) left=true;
                    else if(tx<225) right=true;
                    else if(tx>w-170) jump=true;
                }
            }
        } else if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_CANCEL) {
            left=right=jump=false;
        }
        return true;
    }
    private void restart() {
        px=90; py=groundY-playerH; vx=vy=0; camera=0; health=3; score=0; gameOver=won=false;
        for(Coin coin:coins) coin.taken=false;
        enemies.clear(); enemies.add(new Enemy(700,groundY-34,650,850)); enemies.add(new Enemy(1580,groundY-34,1500,1780)); enemies.add(new Enemy(2550,groundY-34,2470,2800));
    }
    private static class Coin { float x,y; boolean taken=false; Coin(float a,float b){x=a;y=b;} }
    private static class Enemy { float x,y,min,max,dir=1; boolean dead=false; Enemy(float a,float b,float c,float d){x=a;y=b;min=c;max=d;} }
}
