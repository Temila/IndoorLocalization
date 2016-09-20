package com.tudelft.sps.indoorlocalizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by EinNarr on 2016/5/29.
 */

public class DrawAMap extends View implements Runnable {
    private Paint paint= null;
    private RectF[] cell;
    private float n;
    private ViewGroup.LayoutParams layoutParams;
    private int currentCell;
    private boolean showDetailData;
    private String detailData;
    private int textSize=40;
    private Bitmap bMap,bMark,bArrow;
    private Bitmap scaleBMap, scaleBMark, scaleBArrow;
    private int angle;
    private boolean threeDMode;

    public DrawAMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint= new Paint();
        cell = new RectF[19];

        detailData = " ";
        angle = 0;

        bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        bMark = BitmapFactory.decodeResource(getResources(), R.drawable.mark);
        bArrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);

        new  Thread(this).start();
    }
    public void  updateCell() {

        float sf = bMap.getWidth() / 1168F;

        cell[18] = new RectF(236, 196, 286, 242);
        cell[17] = new RectF(233, 30, 290, 116);
        cell[16] = new RectF(233, 116, 290, 151);
        cell[15] = new RectF(290, 116, 345, 151);
        cell[14] = new RectF(345, 116, 400, 151);
        cell[13] = new RectF(400, 30, 455, 116);
        cell[12] = new RectF(400, 116, 455, 151);
        cell[11] = new RectF(455, 116, 510, 151);
        cell[10] = new RectF(510, 116, 565, 151);
        cell[9] = new RectF(565, 116, 621, 151);
        cell[8] = new RectF(621, 151, 678, 242);
        cell[7] = new RectF(621, 116, 678, 151);
        cell[6] = new RectF(678, 116, 734, 151);
        cell[5] = new RectF(734, 116, 792, 151);
        cell[4] = new RectF(792, 30, 848, 116);
        cell[3] = new RectF(792, 116, 848, 151);
        cell[2] = new RectF(848, 116, 911, 151);
        cell[1] = new RectF(848, 151, 911, 242);
        cell[0] = new RectF(0, 0, 0, 0);

        for (RectF r : cell) {
            r.top *= n * sf;
            r.bottom *= n * sf;
            r.left *= n * sf;
            r.right *= n * sf;
        }
    }

    public void  onDraw(Canvas canvas) {
        if(n==0)
            n = Math.min((float)this.getWidth()/bMap.getWidth(),(float)this.getHeight()/bMap.getHeight());

        layoutParams = getLayoutParams();
        layoutParams.width = (int)(n*bMap.getWidth());
        layoutParams.height = (int)(n*bMap.getHeight());
        setLayoutParams(layoutParams);

        super.onDraw(canvas);
        updateCell();

        paint.setAntiAlias(true);
        paint.setTextSize((int)(textSize));
        //设置图形为空心
        paint.setStyle(Paint.Style.STROKE);

        Matrix matrix = new Matrix();

        matrix.postScale(n, n);

        if(scaleBMap==null)
            scaleBMap = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);

        matrix.postScale(n*0.08F, n*0.08F);

        if(scaleBMark==null)
            scaleBMark = Bitmap.createBitmap(bMark, 0, 0, bMark.getWidth(), bMark.getHeight(), matrix, true);

        matrix.postScale(n*2F, n*2F);
        matrix.postRotate(180);

        if(scaleBArrow==null)
            scaleBArrow = Bitmap.createBitmap(bArrow, 0, 0, bArrow.getWidth(), bArrow.getHeight(), matrix, true);

        canvas.drawBitmap(scaleBMap, 0, 0, null);

        paint.setTextSize(textSize/1.2F);
        paint.setColor(Color.BLACK);

        for(int i = 1; i<=18; i++) {
            String str = "C"+i;
            Rect rect = new Rect();
            paint.getTextBounds(str, 0, str.length(), rect);
            float w = rect.width();
            float h = rect.height();
            if(threeDMode) {
                canvas.rotate(-angle-90, cell[i].centerX(), cell[i].centerY());
            }
            canvas.drawText(str, cell[i].centerX()-w/2, cell[i].centerY()+h/2, paint);
            if(threeDMode) {
                canvas.rotate(90+angle, cell[i].centerX(), cell[i].centerY());
            }
        }

        if(!threeDMode && currentCell>0) {
            canvas.drawBitmap(scaleBMark, cell[currentCell].centerX()-scaleBMark.getWidth()/2F,
                    cell[currentCell].centerY()-scaleBMark.getHeight(), null);
        }

        if(showDetailData) {
            multiLineText(detailData,canvas);
        }
        if(threeDMode) {
//            if(currentCell==0)
//                setPos(2);
            canvas.rotate(-angle, cell[currentCell].centerX(), cell[currentCell].centerY());
            canvas.drawBitmap(scaleBArrow, cell[currentCell].centerX()-scaleBArrow.getWidth()/2F,
                    cell[currentCell].centerY()-scaleBArrow.getHeight()/2F, null);
            canvas.rotate(angle, cell[currentCell].centerX(), cell[currentCell].centerY());

            setPivotX(cell[currentCell].centerX());
            setPivotY(cell[currentCell].centerY());

            setTranslationX(this.getWidth() - cell[currentCell].centerX() - this.getWidth()/4);
            setTranslationY(this.getHeight() / 2F - cell[currentCell].centerY());

            setScaleX(4F);
            setScaleY(4F);

            setRotation(angle);
        }
        else {
            setTranslationX(0);
            setTranslationY(0);

            setScaleX(1F);
            setScaleY(1F);

            setRotation(0);
        }
    }

    public void setPos(int cellNum) {
        currentCell = cellNum;
        postInvalidate();
    }

    public void setAngle(int angle) {
        this.angle = angle;
        postInvalidate();
    }

    public void setText(String bayesianResult) {
        detailData = bayesianResult;
    }

    public void toggleTestMode() {
        showDetailData = ! showDetailData;
    }

    public void toggleDisplayMode() {
        threeDMode = !threeDMode;
        postInvalidate();
    }

    public void run() {
        // TODOAuto-generated method stub
        while(!Thread.currentThread().isInterrupted()) {
            try{
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                // TODO: handle exception
                Thread.currentThread().interrupt();
            }
            postInvalidate();
        }
    }

    private void multiLineText(String str, Canvas canvas) {
        paint.setTextSize((int)(textSize));
        paint.setColor(Color.RED);
        String[] text;
        int offset = (int)(textSize);
        text = str.split("\r\n");
        for(String s : text) {
            canvas.drawText(s, 0, offset, paint);
            offset += (int)(textSize);
        }
    }
}
