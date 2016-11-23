package com.capstone.petros.impressionistpainter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private float _lastX = -1;
    private float _lastY = -1;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Circle;
    private int _randRed = 0;
    private int _randGreen = 0;
    private int _randBlue = 0;

    ArrayList<BrushPoint> _brushList = new ArrayList<BrushPoint>();

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(255);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;

    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        // Assign a new, blank bitmap
        _offScreenBitmap = Bitmap.createBitmap(_offScreenCanvas.getWidth(), _offScreenCanvas.getHeight(), Bitmap.Config.ARGB_8888);
        _offScreenCanvas = new Canvas(_offScreenBitmap);
        _brushList.clear();
        _randRed = 0;
        _randGreen = 0;
        _randBlue = 0;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);

    }

    // Responsible for actually drawing on the bitmap
    private void drawBrush(float x, float y, float lastX, float lastY, BrushType brushType, Paint paint){

        // For the color remix
        int color = paint.getColor();
        int red = (color & 0xFF0000) >> 4*4;
        int green = (color & 0xFF00) >> 2*4;
        int blue = color & 0xFF;
        red = (red+_randRed) % 256;
        green = (green+_randGreen) % 256;
        blue = (blue+_randBlue) % 256;
        paint.setColor(Color.argb(255,red,green,blue));

        Rect rect = getBitmapPositionInsideImageView(_imageView);
        switch(brushType){
            case Circle:
                _offScreenCanvas.drawCircle(x, y, 25, paint);
                break;
            case X:
                paint.setStrokeWidth(10);
                _offScreenCanvas.drawLine(x-20, y-20, x+20, y+20, paint);
                _offScreenCanvas.drawLine(x-20, y+20, x+20, y-20, paint);
                break;
            case Edge:
                paint.setAlpha(150);
                if(lastX != -1 && lastY != -1){
                    paint.setStrokeWidth(20);
                    float deltaX = x - lastX;
                    float deltaY = y - lastY;

                    if(Math.abs(deltaX) > Math.abs(deltaY)){
                        // Left or right
                        if(deltaX > 0){

                            _offScreenCanvas.drawLine(x, y, rect.left+rect.width(), y, paint);
                        }
                        else{
                            _offScreenCanvas.drawLine(x, y, rect.left, y, paint);
                        }
                    }
                    else if(Math.abs(deltaX) < Math.abs(deltaY)){
                        // Up or down
                        if(deltaY > 0){
                            _offScreenCanvas.drawLine(x, y, x, rect.top+rect.height(), paint);
                        }
                        else{
                            _offScreenCanvas.drawLine(x, y, x, rect.top, paint);
                        }
                    }
                }
                break;
        }
    }

    // Responsible for getting the pixel color value at the touch point, and drawing with the
    // sent in brush type.
    private void getPixelPoint(float x, float y, float lastX, float lastY, BrushType brushType){
        if(_imageView.getDrawable() == null){
            return; //No bitmap
        }
        Rect rect = getBitmapPositionInsideImageView(_imageView);
        float bitmapX = x - rect.left;
        float bitmapY = y - rect.top;

        //Get the scale that the bitmap was scaled by.
        float [] metrics = new float[9];
        _imageView.getImageMatrix().getValues(metrics);
        final float scaleX = metrics[Matrix.MSCALE_X];
        final float scaleY = metrics[Matrix.MSCALE_Y];

        Bitmap bitmap = ((BitmapDrawable)_imageView.getDrawable()).getBitmap();

        int bitmapXScaled = (int)(bitmapX * (1.0/scaleX));
        int bitmapYScaled = (int)(bitmapY * (1.0/scaleY));

        if(bitmapXScaled >= 0 && bitmapYScaled >= 0 && bitmapXScaled < bitmap.getWidth() &&
                bitmapYScaled < bitmap.getHeight()){

            int color = bitmap.getPixel(bitmapXScaled, bitmapYScaled);
            _paint.setColor(color);
            _brushList.add(new BrushPoint(x,y,lastX,lastY,brushType, color));

            drawBrush(x,y,lastX,lastY,brushType,_paint);

            _lastX = x;
            _lastY = y;

            _paint = new Paint();
            _paint.setColor(Color.RED);
            _paint.setAlpha(255);
            _paint.setAntiAlias(true);
            _paint.setStyle(Paint.Style.FILL);
            _paint.setStrokeWidth(4);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        float x = motionEvent.getX(), y = motionEvent.getY();

        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int historySize = motionEvent.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float touchX = motionEvent.getHistoricalX(i);
                    float touchY = motionEvent.getHistoricalY(i);

                    getPixelPoint(touchX, touchY, _lastX, _lastY, _brushType);
                }
                getPixelPoint(x, y, _lastX, _lastY, _brushType);
                break;
            case MotionEvent.ACTION_UP:
                _lastX = -1;
                _lastY = -1;
                break;
        }

        invalidate();


        return true;
    }




    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }

    // My custom feature
    public void remixColors(){
        // Clear the bitmap
        _offScreenBitmap = Bitmap.createBitmap(_offScreenCanvas.getWidth(), _offScreenCanvas.getHeight(), Bitmap.Config.ARGB_8888);
        _offScreenCanvas = new Canvas(_offScreenBitmap);
        // Re-randomize the colors
        _randRed = (int)(Math.random()*256);
        _randBlue = (int)(Math.random()*256);
        _randGreen = (int)(Math.random()*256);
        // Re-draw each point
        for(BrushPoint bp : _brushList){
            Paint p = new Paint();
            p.setColor(bp.getColor());
            p.setAlpha(255);
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(4);
            drawBrush(bp.getX(), bp.getY(), bp.getLastX(), bp.getLastY(), bp.getBrushType(), p);
        }
        invalidate();
    }

    // The save feature
    // Gotten from this stack overflow:
    // http://stackoverflow.com/questions/8560501/android-save-image-into-gallery
    public void saveImage(ContentResolver cr){
        MediaStore.Images.Media.insertImage(cr, _offScreenBitmap, "IMG" , "Image drawn in Impressionist Painter");
        Toast.makeText(getContext(), "Saved in gallery", Toast.LENGTH_SHORT).show();
    }
}

