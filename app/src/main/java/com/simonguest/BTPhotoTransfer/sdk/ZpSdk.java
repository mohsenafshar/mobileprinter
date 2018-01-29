package com.simonguest.BTPhotoTransfer.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;

import com.simonguest.BTPhotoTransfer.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

public class ZpSdk {
    public static final String LEFT_ALIGN = "LEFT_ALIGN";
    public static final String RIGHT_ALIGN = "RIGHT_ALIGN";
    public static final String CENTER_ALIGN = "CENTER_ALIGN";
    public static String ErrorMessage = "No_Error_Message";
    public static boolean TextPosWinStyle = false;
    private static OutputStream myOutStream = null;
    private static InputStream myInStream = null;
    private static BluetoothSocket mySocket = null;
    private static BluetoothAdapter myBluetoothAdapter;
    private static BluetoothDevice myDevice;
    public static int textHeight;
    public static int textWidth;

    public static Bitmap getBitmap() {
        return myBitmap;
    }

    private static Bitmap myBitmap = null;
    private static Canvas myCanvas = null;
    private static Paint myPaint = null;
    private static int myBitmapHeight = 0;
    private static int myBitmapWidth = 0;
    private static int PrinterDotWidth = 576;
    private static int PrinterDotPerMM = 8;

    /*static
    {
        System.loadLibrary("Barcode");
    }

    private static native String BarcodeMakeCODE128(String paramString, int paramInt1, int paramInt2);

    private static native String BarcodeMakeCODE39(String paramString, int paramInt1, int paramInt2);

    private static native String BarcodeMakeCODE93(String paramString, int paramInt1, int paramInt2);

    private static native String BarcodeMakeEAN8(String paramString, int paramInt1, int paramInt2);

    private static native String BarcodeMakeEAN13(String paramString, int paramInt1, int paramInt2);

    private static native String BarcodeMakeCODABAR(String paramString, int paramInt1, int paramInt2);

    private static native String BarcodeMakeUPC(String paramString, int paramInt1, int paramInt2);

    private static native int getBarcodeWidth();

    private static native int getBarcodeHeight();

    private static native byte[] BarcodeMakePDF417(int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4);

    private static native byte[] BarcodeMakeDataMatrix(byte[] paramArrayOfByte, int paramInt);

    private static native byte[] BarcodeMakeQRCode(int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3);
*/
    public static enum BARCODE_TYPE {
        BARCODE_CODE128, BARCODE_CODE39, BARCODE_CODE93, BARCODE_CODABAR, BARCODE_EAN8, BARCODE_EAN13, BARCODE_UPC;
    }

    public static enum BARCODE2D_TYPE {
        BARCODE2D_DATAMATRIX, BARCODE2D_QRCODE, BARCODE2D_PDF417;
    }

    public static boolean zp_open(BluetoothAdapter myBluetoothAdapter, BluetoothDevice btDevice) {
        if (!SPPOpen(myBluetoothAdapter, btDevice)) {
            return false;
        }
        return true;
    }

    public static void zp_close() {
        SPPClose();
    }

    public static boolean zp_page_create(double pagewidth, double pageheight) {
        if (pagewidth > 600.0D) {
            pagewidth = 600.0D;
        }
        if (pageheight > 600.0D) {
            pageheight = 600.0D;
        }
        myBitmapWidth = (int) (pagewidth * PrinterDotPerMM);
        myBitmapHeight = (int) (pageheight * PrinterDotPerMM);
        myBitmap = Bitmap.createBitmap(myBitmapWidth, myBitmapHeight, Bitmap.Config.RGB_565);
        /*if(myCanvas == null) {
            myCanvas = new Canvas(myBitmap);
        }*/
        myCanvas = new Canvas(myBitmap);
        myPaint = new Paint();
        myPaint.setColor(-16777216);
        myPaint.setTextSize(36.0F);

        myPaint.setStrokeWidth(1.0F);

        myCanvas.drawColor(-1);
        return true;
    }

    public static void zp_page_free() {
        myBitmap.recycle();
        myBitmap = null;
        myCanvas = null;
        myPaint = null;
        myBitmapHeight = 0;
        myBitmapWidth = 0;
    }

    public static void zp_page_clear() {
        if (myCanvas != null) {
            myCanvas.drawColor(-1);
        }
    }

    public static boolean zp_page_print(boolean IsPortrait) {
        if (!IsPortrait) {
            if (myBitmapWidth > PrinterDotWidth) {
                myBitmapWidth = PrinterDotWidth;
            }
            int len = (myBitmapWidth + 7) / 8;
            byte[] data = new byte[(len + 4) * myBitmapHeight];
            int ndata = 0;
            int i = 0;
            int j = 0;
            int[] RowData = new int[myBitmapWidth * myBitmapHeight];
            myBitmap.getPixels(RowData, 0, myBitmapWidth, 0, 0, myBitmapWidth, myBitmapHeight);
            for (i = 0; i < myBitmapHeight; i++) {
                data[(ndata + 0)] = 31;
                data[(ndata + 1)] = 16;
                data[(ndata + 2)] = ((byte) (len % 256));
                data[(ndata + 3)] = ((byte) (len / 256));
                for (j = 0; j < len; j++) {
                    data[(ndata + 4 + j)] = 0;
                }
                for (j = 0; j < myBitmapWidth; j++) {
                    int color = RowData[(i * myBitmapWidth + j)];
                    int b = color >> 0 & 0xF;
                    int g = color >> 4 & 0xF;
                    int r = color >> 8 & 0xF;
                    int grey = (r + g + b) / 3;
                    if (grey < 12) {
                        int tmp221_220 = (ndata + 4 + j / 8);
                        byte[] tmp221_211 = data;
                        tmp221_211[tmp221_220] = ((byte) (tmp221_211[tmp221_220] | (byte) (128 >> j % 8)));
                    }
                }
                int size;
                for (size = len - 1; size >= 0; size--) {
                    if (data[(ndata + 4 + size)] != 0) {
                        break;
                    }
                }
                size++;
                size = len;
                data[(ndata + 2)] = ((byte) (size % 256));
                data[(ndata + 3)] = ((byte) (size / 256));
                ndata += 4 + size;
            }
            SPPWrite(data, ndata);
        } else {
            int yOffset = 0;
            int yHeight = myBitmapHeight;
            if (myBitmapHeight > PrinterDotWidth) {
                yOffset = myBitmapHeight - PrinterDotWidth;
                yHeight = PrinterDotWidth;
            }
            int len = (myBitmapHeight + 7) / 8;
            byte[] data = new byte[(len + 4) * myBitmapWidth];
            int ndata = 0;
            int i = 0;
            int j = 0;
            int[] RowData = new int[myBitmapWidth * myBitmapHeight];
            myBitmap.getPixels(RowData, 0, myBitmapWidth, 0, yOffset, myBitmapWidth, yHeight);
            for (i = 0; i < myBitmapWidth; i++) {
                data[(ndata + 0)] = 31;
                data[(ndata + 1)] = 16;
                data[(ndata + 2)] = ((byte) (len % 256));
                data[(ndata + 3)] = ((byte) (len / 256));
                int bit = 0;
                for (j = 0; j < len; j++) {
                    data[(ndata + 4 + j)] = 0;
                }
                for (j = yHeight - 1; j >= 0; j--) {
                    int color = RowData[(i + j * myBitmapWidth)];
                    int b = color >> 0 & 0xF;
                    int g = color >> 4 & 0xF;
                    int r = color >> 8 & 0xF;
                    int grey = (r + g + b) / 3;
                    if (grey < 12) {
                        int tmp580_579 = (ndata + 4 + bit / 8);
                        byte[] tmp580_568 = data;
                        tmp580_568[tmp580_579] = ((byte) (tmp580_568[tmp580_579] | (byte) (128 >> bit % 8)));
                    }
                    bit++;
                }
                int size;
                for (size = len - 1; size >= 0; size--) {
                    if (data[(ndata + 4 + size)] != 0) {
                        break;
                    }
                }
                size++;
                data[(ndata + 2)] = ((byte) (size % 256));
                data[(ndata + 3)] = ((byte) (size / 256));
                ndata += size + 4;
            }
            SPPWrite(data, ndata);
        }
        return true;
    }

    public static void zp_draw_text(double x, double y, String text) {
        if (myCanvas == null) {
            ErrorMessage = "Please Create Page!";
            return;
        }

        Rect tempBounds = new Rect();
        myPaint.getTextBounds(text, 0, text.length(), tempBounds);
        int tempHeight = tempBounds.height();
        int tempWidth = tempBounds.width();

        //myCanvas.drawText(text, (float) (PrinterDotPerMM * x), (float) (y * PrinterDotPerMM), myPaint);
        myCanvas.drawText(text, (float) (PrinterDotPerMM * x), (float) tempHeight, myPaint);
    }

    public static Canvas getCanvas(){
        return myCanvas;
    }

    public static void setCanvas(Canvas c){
        myCanvas = c;
    }

    static void myDrawText(Canvas canvas, String text, float x, float y, Paint paint, float angle) {
        if (angle != 0.0F) {
            canvas.rotate(angle, x, y);
        }
        canvas.drawText(text, x, y, paint);
        if (angle != 0.0F) {
            canvas.rotate(-angle, x, y);
        }
    }

    public static void setTextAlign(String textAlign){
        if(ZpSdk.RIGHT_ALIGN.equals(textAlign)) {
            myPaint.setTextAlign(Paint.Align.RIGHT);
        } else if(ZpSdk.LEFT_ALIGN.equals(textAlign)) {
            myPaint.setTextAlign(Paint.Align.LEFT);
        }
    }

    public static int getTextHeight(double fontheight){
        myPaint.setTextSize((int) (fontheight * PrinterDotPerMM));
        myPaint.setTextAlign(Paint.Align.RIGHT);

        Rect tempBounds = new Rect();
        String text = "0123456789012345678901234567890123456789";
        myPaint.getTextBounds(text, 0, text.length(), tempBounds);
        textHeight = tempBounds.height();
        textWidth = tempBounds.width();

        return tempBounds.height();
    }

    public static void zp_draw_text_ex(double x, double y, String text, String fontname, double fontheight, int angle, boolean bold, boolean italic, boolean underline, String alignment) {

        //x += fontheight;
        //y += fontheight;

        //x *= 8;
        //y *= 8;

        /*Rect bounds = new Rect();
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.height();
        int width = bounds.width();*/

        if (myCanvas == null) {
            ErrorMessage = "Please Create Page!";
            return;
        }
        myPaint.setTextSize((int) (fontheight * PrinterDotPerMM));
        //myPaint.setTextAlign(Paint.Align.LEFT);

        Rect tempBounds = new Rect();
        myPaint.getTextBounds(text, 0, text.length(), tempBounds);
        textHeight = tempBounds.height();
        textWidth = tempBounds.width();

        //Typeface font;
        Typeface font;
        if ((bold) && (italic)) {
            font = Typeface.create(fontname, Typeface.BOLD_ITALIC);
        } else {
            //Typeface font;
            if ((bold) && (!italic)) {
                font = Typeface.create(fontname, Typeface.BOLD);
            } else {
                //Typeface font;
                if ((!bold) && (italic)) {
                    font = Typeface.create(fontname, Typeface.ITALIC);
                } else {
                    font = Typeface.create(fontname, Typeface.NORMAL);
                }
            }
        }
        myPaint.setTypeface(font);
        myPaint.setUnderlineText(underline);
        /*if(isRight) {
            myDrawText(myCanvas, text, (float) ((float) myBitmapWidth - x), (float) (textHeight / 2 + y), myPaint, angle);
        } else if (TextPosWinStyle) {
            myDrawText(myCanvas, text, (float) (PrinterDotPerMM * x + 570.0D), (float) (y * PrinterDotPerMM) + (int) (fontheight * PrinterDotPerMM), myPaint, angle);
        } else {
            //myDrawText(myCanvas, text, (float) textWidth, (float) (textHeight / 2 + y), myPaint, angle);
            if(x == 0){
                myDrawText(myCanvas, text, (float) textWidth, (float) (textHeight / 2 + y), myPaint, angle);
            } else {
                myDrawText(myCanvas, text, (float) (textWidth + (x - textWidth / 2)), (float) (textHeight / 2 + y), myPaint, angle);
            }
            //myDrawText(myCanvas, text, (float)(PrinterDotPerMM * x + 570.0D), (float)(y * PrinterDotPerMM), myPaint, angle);
            //myDrawText(myCanvas, text, (float) (PrinterDotPerMM * x + 570.0D), (float) (y * PrinterDotPerMM) + (int) (fontheight * PrinterDotPerMM), myPaint, angle);
        }*/

        switch (alignment) {
            case RIGHT_ALIGN:
                myDrawText(myCanvas, text, (float) ((float) myBitmapWidth - x), (float) (textHeight / 2 + y), myPaint, angle);
                break;
            case LEFT_ALIGN:
                myDrawText(myCanvas, text, (float) ((float) textWidth + x), (float) (textHeight / 2 + y), myPaint, angle);
                break;
            case CENTER_ALIGN:
                myDrawText(myCanvas, text, (float) (myBitmapWidth/2 + textWidth / 2 + x), (float) (textHeight / 2 + y), myPaint, angle);
                break;
            default:
                myDrawText(myCanvas, text, (float) textWidth, (float) (textHeight / 2 + y), myPaint, angle);
                break;
        }
    }

    static int mRealLine = 0;

    private static Vector GetTextMultiLines(String Text, float BoxWidth, float BoxHeight) {
        Vector mString = new Vector();

        int w = 0;
        int istart = 0;
        int count = Text.length();
        for (int i = 0; i < count; i++) {
            char ch = Text.charAt(i);
            float[] widths = new float[1];
            String str = String.valueOf(ch);
            myPaint.getTextWidths(str, widths);
            if (ch == '\n') {
                mRealLine += 1;
                mString.addElement(Text.substring(istart, i));
                istart = i + 1;
                w = 0;
            } else {
                w += (int) Math.ceil(widths[0]);
                if (w > BoxWidth) {
                    mRealLine += 1;
                    mString.addElement(Text.substring(istart, i));
                    istart = i;
                    i--;
                    w = 0;
                } else if (i == count - 1) {
                    mRealLine += 1;
                    mString.addElement(Text.substring(istart, count));
                }
            }
        }
        return mString;
    }

    public static void zp_draw_text_box(double x, double y, double width, double height, String text, String fontname, double fontheight, int angle, boolean bold, boolean italic, boolean underline) {
        if (myCanvas == null) {
            ErrorMessage = "Please Create Page!";
            return;
        }
        myPaint.setTextSize((int) (fontheight * PrinterDotPerMM));
        Typeface font;
        //Typeface font;
        if ((bold) && (italic)) {
            font = Typeface.create(fontname, Typeface.BOLD_ITALIC);
        } else {
            //Typeface font;
            if ((bold) && (!italic)) {
                font = Typeface.create(fontname, Typeface.BOLD);
            } else {
                //Typeface font;
                if ((!bold) && (italic)) {
                    font = Typeface.create(fontname, Typeface.ITALIC);
                } else {
                    font = Typeface.create(fontname, Typeface.NORMAL);
                }
            }
        }
        myPaint.setTypeface(font);
        myPaint.setUnderlineText(underline);

        Paint.FontMetrics fm = myPaint.getFontMetrics();
        int mFontHeight = (int) (Math.ceil(fm.descent - fm.top) + 2.0D);
        mRealLine = 0;
        Vector vStr = GetTextMultiLines(text, (float) (PrinterDotPerMM * width), (float) (PrinterDotPerMM * height));
        if (TextPosWinStyle) {
            int i = 0;
            for (int j = 0; i < mRealLine; j++) {
                myCanvas.drawText((String) vStr.elementAt(i), (float) (PrinterDotPerMM * x), (float) (y * PrinterDotPerMM + mFontHeight * j + (int) (fontheight * PrinterDotPerMM)), myPaint);
                i++;
            }
        } else {
            int i = 0;
            for (int j = 0; i < mRealLine; j++) {
                myCanvas.drawText((String) vStr.elementAt(i), (float) (PrinterDotPerMM * x), (float) (y * PrinterDotPerMM + mFontHeight * j), myPaint);
                i++;
            }
        }
    }

    public static void zp_draw_line(double x0, double y0, double x1, double y1, int LineWidth) {
        if (myCanvas == null) {
            ErrorMessage = "Please Create Page!";
            return;
        }
        Paint tempPaint = new Paint();
        tempPaint.setColor(-16777216);
        tempPaint.setStrokeWidth(LineWidth);
        myCanvas.drawLine((float) x0 * PrinterDotPerMM, (float) y0 * PrinterDotPerMM, (float) x1 * PrinterDotPerMM, (float) y1 * PrinterDotPerMM, tempPaint);
    }

    public static void zp_draw_rect(double left, double top, double right, double bottom, int LineWidth) {
        if (myCanvas == null) {
            ErrorMessage = "Please Create Page!";
            return;
        }
        Paint tempPaint = new Paint();
        tempPaint.setStrokeWidth(LineWidth);
        myCanvas.drawLine((float) left * PrinterDotPerMM, (float) top * PrinterDotPerMM, (float) right * PrinterDotPerMM, (float) top * PrinterDotPerMM, tempPaint);
        myCanvas.drawLine((float) left * PrinterDotPerMM, (float) top * PrinterDotPerMM, (float) left * PrinterDotPerMM, (float) bottom * PrinterDotPerMM, tempPaint);
        myCanvas.drawLine((float) right * PrinterDotPerMM, (float) top * PrinterDotPerMM, (float) right * PrinterDotPerMM, (float) bottom * PrinterDotPerMM, tempPaint);
        myCanvas.drawLine((float) left * PrinterDotPerMM, (float) bottom * PrinterDotPerMM, (float) right * PrinterDotPerMM, (float) bottom * PrinterDotPerMM, tempPaint);
    }

    public static void zp_draw_bitmap(Bitmap bitmap, double left, double top) {
        if (myCanvas == null) {
            ErrorMessage = "Please Create Page!";
            return;
        }
        myCanvas.drawBitmap(bitmap, (float) left, (float) top, myPaint);
    }

    /*public static boolean zp_draw_barcode(double x, double y, String pData, BARCODE_TYPE type, double Height, int LineWidth, int Rotate)
    {
        int bit_len = 0;
        if (myCanvas == null)
        {
            ErrorMessage = "Please Create Page!";
            return false;
        }
        String str;
        //String str;
        if (type == BARCODE_TYPE.BARCODE_CODE128)
        {
            str = BarcodeMakeCODE128(pData, pData.length(), 2048);
        }
        else
        {
            String str;
            if (type == BARCODE_TYPE.BARCODE_CODE39)
            {
                str = BarcodeMakeCODE39(pData, pData.length(), 2048);
            }
            else
            {
                String str;
                if (type == BARCODE_TYPE.BARCODE_CODE93)
                {
                    str = BarcodeMakeCODE93(pData, pData.length(), 2048);
                }
                else
                {
                    String str;
                    if (type == BARCODE_TYPE.BARCODE_CODABAR)
                    {
                        str = BarcodeMakeCODABAR(pData, pData.length(), 2048);
                    }
                    else
                    {
                        String str;
                        if (type == BARCODE_TYPE.BARCODE_EAN8)
                        {
                            str = BarcodeMakeEAN8(pData, pData.length(), 2048);
                        }
                        else
                        {
                            String str;
                            if (type == BARCODE_TYPE.BARCODE_EAN13)
                            {
                                str = BarcodeMakeEAN13(pData, pData.length(), 2048);
                            }
                            else
                            {
                                String str;
                                if (type == BARCODE_TYPE.BARCODE_UPC) {
                                    str = BarcodeMakeUPC(pData, pData.length(), 2048);
                                } else {
                                    str = null;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (str.length() < 10) {
            return false;
        }
        bit_len = str.getBytes().length;
        byte[] dotbuf = new byte[bit_len];
        dotbuf = str.getBytes();

        Paint tempPaint = new Paint();
        tempPaint.setColor(-16777216);
        tempPaint.setStrokeWidth(LineWidth);

        int DotX = (int)(x * PrinterDotPerMM);
        int DotY = (int)(y * PrinterDotPerMM);

        int offset = 0;int blackwidth = 0;int whitewidth = 0;
        int gapvalue = 1;
        if (Rotate == 0)
        {
            int DotHeight = (int)(Height * PrinterDotPerMM);
            for (int i = 0; i < bit_len; i++)
            {
                if (dotbuf[i] == 49)
                {
                    if ((blackwidth >= gapvalue) && (whitewidth == 1)) {
                        offset++;
                    }
                    if (whitewidth != 0) {
                        blackwidth = 0;
                    }
                    myCanvas.drawLine(DotX + offset, DotY, DotX + offset, DotY + DotHeight, tempPaint);
                    whitewidth = 0;
                    blackwidth++;
                }
                else
                {
                    whitewidth++;
                }
                offset += LineWidth;
            }
        }
        else if (Rotate == 90)
        {
            int DotHeight = (int)(Height * PrinterDotPerMM);
            for (int i = 0; i < bit_len; i++)
            {
                if (dotbuf[i] == 49)
                {
                    if ((blackwidth >= gapvalue) && (whitewidth == 1)) {
                        offset++;
                    }
                    if (whitewidth != 0) {
                        blackwidth = 0;
                    }
                    myCanvas.drawLine(DotX, DotY - offset, DotX + DotHeight, DotY - offset, tempPaint);

                    whitewidth = 0;
                    blackwidth++;
                }
                else
                {
                    whitewidth++;
                }
                offset += LineWidth;
            }
        }
        else if (Rotate == 180)
        {
            int DotHeight = (int)(Height * PrinterDotPerMM);
            for (int i = 0; i < bit_len; i++)
            {
                if (dotbuf[i] == 49)
                {
                    if ((blackwidth >= gapvalue) && (whitewidth == 1)) {
                        offset++;
                    }
                    if (whitewidth != 0) {
                        blackwidth = 0;
                    }
                    myCanvas.drawLine(DotX - offset, DotY, DotX - offset, DotY - DotHeight, tempPaint);
                    whitewidth = 0;
                    blackwidth++;
                }
                else
                {
                    whitewidth++;
                }
                offset += LineWidth;
            }
        }
        else if (Rotate == 270)
        {
            int DotHeight = (int)(Height * PrinterDotPerMM);
            for (int i = 0; i < bit_len; i++)
            {
                if (dotbuf[i] == 49)
                {
                    if ((blackwidth >= gapvalue) && (whitewidth == 1)) {
                        offset++;
                    }
                    if (whitewidth != 0) {
                        blackwidth = 0;
                    }
                    myCanvas.drawLine(DotX, DotY + offset, DotX - DotHeight, DotY + offset, tempPaint);
                    whitewidth = 0;
                    blackwidth++;
                }
                else
                {
                    whitewidth++;
                }
                offset += LineWidth;
            }
        }
        return true;
    }

    public static boolean zp_draw_barcode2d(double x, double y, String pData, BARCODE2D_TYPE type, int colsize, int bank, int size, int Rotate)
    {
        double DotX = x * PrinterDotPerMM;
        double DotY = y * PrinterDotPerMM;
        byte[] dotbuf = new byte['?'];
        if (myCanvas == null)
        {
            ErrorMessage = "Please Create Page!";
            return false;
        }
        int YSize;
        if (type == BARCODE2D_TYPE.BARCODE2D_DATAMATRIX)
        {
            byte[] tempbuf = new byte['?'];
            try
            {
                tempbuf = pData.getBytes("GBK");
                dotbuf = BarcodeMakeDataMatrix(tempbuf, pData.getBytes("GBK").length);
            }
            catch (UnsupportedEncodingException localUnsupportedEncodingException) {}
            int XSize = size;
            YSize = size;
        }
        else
        {
            int YSize;
            if (type == BARCODE2D_TYPE.BARCODE2D_PDF417)
            {
                byte[] tempbuf = new byte['?'];
                try
                {
                    tempbuf = pData.getBytes("GBK");
                    dotbuf = BarcodeMakePDF417(colsize, 2, size, tempbuf, pData.getBytes("GBK").length);
                }
                catch (UnsupportedEncodingException localUnsupportedEncodingException1) {}
                int XSize = size;
                YSize = size * 3;
            }
            else
            {
                int YSize;
                if (type == BARCODE2D_TYPE.BARCODE2D_QRCODE)
                {
                    byte[] tempbuf = new byte['?'];
                    try
                    {
                        tempbuf = pData.getBytes("GBK");
                        dotbuf = BarcodeMakeQRCode(colsize, bank, tempbuf, pData.getBytes("GBK").length);
                    }
                    catch (UnsupportedEncodingException localUnsupportedEncodingException2) {}
                    int XSize = size;
                    YSize = size;
                }
                else
                {
                    return false;
                }
            }
        }
        int YSize;
        int XSize;
        int BarcodeWidth = getBarcodeWidth();
        int BarcodeHeight = getBarcodeHeight();
        int BytePerLine = (BarcodeWidth - 1) / 8 + 1;
        Paint tempPaint = new Paint();
        tempPaint.setColor(-16777216);
        tempPaint.setStyle(Paint.Style.FILL);
        if (Rotate == 0) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX + xx * XSize));
                    myRect.top = ((int)(DotY + yy * YSize));
                    myRect.right = (myRect.left + XSize);
                    myRect.bottom = (myRect.top + YSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        } else if (Rotate == 90) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX + yy * YSize));
                    myRect.top = ((int)(DotY - xx * XSize));
                    myRect.right = (myRect.left + YSize);
                    myRect.bottom = (myRect.top - XSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        } else if (Rotate == 180) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX - xx * XSize));
                    myRect.top = ((int)(DotY - yy * YSize));
                    myRect.right = (myRect.left - XSize);
                    myRect.bottom = (myRect.top - YSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        } else if (Rotate == 270) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX - yy * YSize));
                    myRect.top = ((int)(DotY + xx * XSize));
                    myRect.right = (myRect.left - YSize);
                    myRect.bottom = (myRect.top + XSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        }
        return true;
    }

    public static boolean zp_draw_barcode2d_byte(double x, double y, byte[] pData, int datalen, BARCODE2D_TYPE type, int colsize, int bank, int size, int Rotate)
    {
        double DotX = x * PrinterDotPerMM;
        double DotY = y * PrinterDotPerMM;
        byte[] dotbuf = new byte['?'];
        if (myCanvas == null)
        {
            ErrorMessage = "Please Create Page!";
            return false;
        }
        int YSize;
        if (type == BARCODE2D_TYPE.BARCODE2D_DATAMATRIX)
        {
            byte[] tempbuf = new byte['?'];
            for (int i = 0; i < datalen; i++) {
                tempbuf[i] = pData[i];
            }
            dotbuf = BarcodeMakeDataMatrix(tempbuf, datalen);
            int XSize = size;
            YSize = size;
        }
        else
        {
            int YSize;
            if (type == BARCODE2D_TYPE.BARCODE2D_PDF417)
            {
                byte[] tempbuf = new byte['?'];
                for (int i = 0; i < datalen; i++) {
                    tempbuf[i] = pData[i];
                }
                dotbuf = BarcodeMakePDF417(colsize, 2, size, tempbuf, datalen);
                int XSize = size;
                YSize = size * 3;
            }
            else
            {
                int YSize;
                if (type == BARCODE2D_TYPE.BARCODE2D_QRCODE)
                {
                    byte[] tempbuf = new byte['?'];
                    for (int i = 0; i < datalen; i++) {
                        tempbuf[i] = pData[i];
                    }
                    dotbuf = BarcodeMakeQRCode(colsize, bank, tempbuf, datalen);
                    int XSize = size;
                    YSize = size;
                }
                else
                {
                    return false;
                }
            }
        }
        int YSize;
        int XSize;
        int BarcodeWidth = getBarcodeWidth();
        int BarcodeHeight = getBarcodeHeight();
        int BytePerLine = (BarcodeWidth - 1) / 8 + 1;
        Paint tempPaint = new Paint();
        tempPaint.setColor(-16777216);
        tempPaint.setStyle(Paint.Style.FILL);
        if (Rotate == 0) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX + xx * XSize));
                    myRect.top = ((int)(DotY + yy * YSize));
                    myRect.right = (myRect.left + XSize);
                    myRect.bottom = (myRect.top + YSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        } else if (Rotate == 90) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX + yy * YSize));
                    myRect.top = ((int)(DotY - xx * XSize));
                    myRect.right = (myRect.left + YSize);
                    myRect.bottom = (myRect.top - XSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        } else if (Rotate == 180) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX - xx * XSize));
                    myRect.top = ((int)(DotY - yy * YSize));
                    myRect.right = (myRect.left - XSize);
                    myRect.bottom = (myRect.top - YSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        } else if (Rotate == 270) {
            for (int yy = 0; yy < BarcodeHeight; yy++) {
                for (int xx = 0; xx < BarcodeWidth; xx++)
                {
                    Rect myRect = new Rect();
                    myRect.left = ((int)(DotX - yy * YSize));
                    myRect.top = ((int)(DotY + xx * XSize));
                    myRect.right = (myRect.left - YSize);
                    myRect.bottom = (myRect.top + XSize);
                    byte TempData = dotbuf[(yy * BytePerLine + xx / 8)];
                    if ((TempData >> xx % 8 & 0x1) == 1) {
                        myCanvas.drawRect(myRect, tempPaint);
                    }
                }
            }
        }
        return true;
    }*/

    public static boolean zp_goto_mark_left(int MaxFeedMM) {
        byte[] data = new byte[7];
        MaxFeedMM *= PrinterDotPerMM;
        int hMaxFeedMM = MaxFeedMM / 256;
        int lMaxFeedMM = MaxFeedMM % 256;
        data[0] = 31;
        data[1] = 32;
        data[2] = 3;
        data[3] = 0;
        data[4] = 3;
        data[5] = ((byte) lMaxFeedMM);
        data[6] = ((byte) hMaxFeedMM);
        if (!SPPWrite(data, 7)) {
            ErrorMessage = "Port Send Data Error!";
            return false;
        }
        return true;
    }

    public static boolean zp_goto_mark_right(int MaxFeedMM) {
        byte[] data = new byte[7];
        MaxFeedMM *= PrinterDotPerMM;
        int hMaxFeedMM = MaxFeedMM / 256;
        int lMaxFeedMM = MaxFeedMM % 256;
        data[0] = 31;
        data[1] = 32;
        data[2] = 3;
        data[3] = 0;
        data[4] = 2;
        data[5] = ((byte) lMaxFeedMM);
        data[6] = ((byte) hMaxFeedMM);
        if (!SPPWrite(data, 7)) {
            ErrorMessage = "Port Send Data Error!";
            return false;
        }
        return true;
    }

    public static boolean zp_goto_mark_label(int MaxFeedMM) {
        byte[] data = new byte[7];
        MaxFeedMM *= PrinterDotPerMM;
        int hMaxFeedMM = MaxFeedMM / 256;
        int lMaxFeedMM = MaxFeedMM % 256;
        data[0] = 31;
        data[1] = 32;
        data[2] = 3;
        data[3] = 0;
        data[4] = 4;
        data[5] = ((byte) lMaxFeedMM);
        data[6] = ((byte) hMaxFeedMM);
        if (!SPPWrite(data, 7)) {
            ErrorMessage = "Port Send Data Error!";
            return false;
        }
        return true;
    }

    private static boolean no_paper(int timeout) {
        int v = zp_reg_get_u32("NoPaper", timeout);
        if (v < 0) {
            ErrorMessage = "???????";
            return true;
        }
        if (v == 1) {
            ErrorMessage = "?????";
            return true;
        }
        return false;
    }

    private static boolean cover_opened(int timeout) {
        int v = zp_reg_get_u32("CoverOpened", timeout);
        if (v < 0) {
            ErrorMessage = "???????";
            return true;
        }
        if (v == 1) {
            ErrorMessage = "????????";
            return true;
        }
        return false;
    }

    private static boolean over_heat(int timeout) {
        int v = zp_reg_get_u32("OverHeat", timeout);
        if (v < 0) {
            ErrorMessage = "???????";
            return true;
        }
        if (v == 1) {
            ErrorMessage = "?????";
            return true;
        }
        return false;
    }

    public static boolean zp_printer_check_error() {
        int timeout = 3000;
        return false;
    }

    public static int zp_error_status(int timeout) {
        byte[] data = new byte[4];
        data[0] = 31;
        data[1] = 69;
        data[2] = 0;
        data[3] = 0;
        SPPWrite(data, 4);
        byte[] readata = new byte[1];
        return 0;
    }

    public static int zp_realtime_status(int timeout) {
        byte[] data = new byte[10];
        data[0] = 31;
        data[1] = 0;
        data[2] = 6;
        data[3] = 0;
        data[4] = 7;
        data[5] = 20;
        data[6] = 24;
        data[7] = 35;
        data[8] = 37;
        data[9] = 50;
        SPPWrite(data, 10);
        byte[] readata = new byte[1];
        if (!SPPReadTimeout(readata, 1, timeout)) {
            ErrorMessage = "?????????";
            return -1;
        }
        int status = readata[0];
        if ((status & 0x1) != 0) {
            ErrorMessage = "???????";
        }
        if ((status & 0x2) != 0) {
            ErrorMessage = "?????";
        }
        if ((status & 0x4) != 0) {
            ErrorMessage = "?????";
        }
        return status;
    }

    public static void zp_printer_status_detect() {
        byte[] data = new byte[4];
        data[0] = 31;
        data[1] = 87;
        data[2] = 0;
        data[3] = 0;
        SPPWrite(data, 4);
    }

    public static int zp_printer_status_get(int timeout) {
        byte[] readata = new byte[1];
        if (!SPPReadTimeout(readata, 1, timeout)) {
            ErrorMessage = "?????????";
            return -1;
        }
        int status = readata[0];
        if ((status & 0x1) != 0) {
            ErrorMessage = "???????";
        }
        if ((status & 0x2) != 0) {
            ErrorMessage = "?????";
        }
        if ((status & 0x4) != 0) {
            ErrorMessage = "?????";
        }
        return status;
    }

    private static int zp_reg_get_u32(String keyname, int timeout) {
        SPPFlush();
        byte[] keybyte = keyname.getBytes();
        int keynamelen = keybyte.length;
        byte len = (byte) (keynamelen + 2 + 1);
        byte[] data = new byte[keynamelen + 6 + 1];
        data[0] = 31;
        data[1] = 82;
        data[2] = len;
        data[3] = 0;
        data[4] = 32;
        data[5] = 0;
        for (byte i = 0; i < keynamelen; i = (byte) (i + 1)) {
            data[(6 + i)] = keybyte[i];
        }
        data[(6 + keynamelen)] = 0;
        if (!SPPWrite(data, keynamelen + 6 + 1)) {
            return -1;
        }
        byte[] readata = new byte[7];
        if (!SPPReadTimeout(readata, 7, timeout)) {
            ErrorMessage = "?????????";
            return -1;
        }
        if ((readata[0] != 32) || (readata[1] != 4) || (readata[2] != 0)) {
            ErrorMessage = "?????????";
            return -1;
        }
        int r = readata[3] + readata[4] * 256 + readata[5] * 256 * 256 + readata[6] * 256 * 256 * 256;
        return r;
    }

    public static boolean zp_check_printer() {
        byte[] data = new byte[8];
        byte[] readdata = new byte[8];
        data[0] = 29;
        data[1] = -103;
        if (!SPPWrite(data, 2)) {
            ErrorMessage = "Port Send Data Error!";
            return false;
        }
        if (SPPReadTimeout(readdata, 4, 2000)) {
            if ((readdata[0] == 29) && (readdata[1] == -103)) {
                return true;
            }
        }
        return false;
    }

    public static boolean OpenPrinter(String BDAddr) {
        if (myBluetoothAdapter.isDiscovering()) {
            myBluetoothAdapter.cancelDiscovery();
        }

        if (BDAddr == "") {
            ErrorMessage = "????????";
            return false;
        }
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            ErrorMessage = "??????";
            return false;
        }
        myDevice = myBluetoothAdapter.getRemoteDevice(BDAddr);
        if (myDevice == null) {
            ErrorMessage = "????????";
            return false;
        }
        if (!SPPOpen(myBluetoothAdapter, myDevice)) {
            return false;
        }
        return true;
    }

    public static void doDiscovery() {
        //Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        //setProgressBarIndeterminateVisibility(true);
        //setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (myBluetoothAdapter.isDiscovering()) {
            myBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        myBluetoothAdapter.startDiscovery();
    }

    /*private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };*/

    private static boolean SPPOpen(BluetoothAdapter BluetoothAdapter, BluetoothDevice btDevice) {
        boolean error = false;
        myBluetoothAdapter = BluetoothAdapter;
        myDevice = btDevice;
        if (!myBluetoothAdapter.isEnabled()) {
            ErrorMessage = "?????????";
            return false;
        }
        try {
            Method m = myDevice.getClass().getMethod("createRfcommSocket", new Class[]{Integer.TYPE});
            mySocket = (BluetoothSocket) m.invoke(myDevice, new Object[]{Integer.valueOf(1)});
        } catch (SecurityException e) {
            mySocket = null;
            ErrorMessage = "??????";
            return false;
        } catch (NoSuchMethodException e) {
            mySocket = null;
            ErrorMessage = "??????";
            return false;
        } catch (IllegalArgumentException e) {
            mySocket = null;
            ErrorMessage = "??????";
            return false;
        } catch (IllegalAccessException e) {
            mySocket = null;
            ErrorMessage = "??????";
            return false;
        } catch (InvocationTargetException e) {
            mySocket = null;
            ErrorMessage = "??????";
            return false;
        }
        try {
            mySocket.connect();
        } catch (IOException e2) {
            ErrorMessage = e2.getLocalizedMessage();
            mySocket = null;
            return false;
        }
        try {
            myOutStream = mySocket.getOutputStream();
        } catch (IOException e3) {
            myOutStream = null;
            error = true;
        }
        try {
            myInStream = mySocket.getInputStream();
        } catch (IOException e3) {
            myInStream = null;
            error = true;
        }
        if (error) {
            SPPClose();
            return false;
        }
        return true;
    }

    private static void SPPFlush() {
        int i = 0;
        int DataLen = 0;
        try {
            DataLen = myInStream.available();
        } catch (IOException localIOException) {
        }
        for (i = 0; i < DataLen; i++) {
            try {
                myInStream.read();
            } catch (IOException localIOException1) {
            }
        }
    }

    private static boolean SPPReadTimeout(byte[] Data, int DataLen, int Timeout) {
        for (int i = 0; i < Timeout / 5; i++) {
            try {
                if (myInStream.available() >= DataLen) {
                    try {
                        myInStream.read(Data, 0, DataLen);
                        return true;
                    } catch (IOException e) {
                        ErrorMessage = "????????";
                        return false;
                    }
                }
                try {
                    Thread.sleep(5L);
                } catch (InterruptedException e) {
                    ErrorMessage = "????????";
                    return false;
                }
            } catch (IOException e) {
                ErrorMessage = "????????";
                return false;
            }
        }
        ErrorMessage = "???????";
        return false;
    }

    private static boolean SPPWrite(byte[] Data, int DataLen) {
        try {
            myOutStream.write(Data, 0, DataLen);
        } catch (IOException e) {
            ErrorMessage = "????????";
            return false;
        }
        return true;
    }

    private static boolean SPPClose() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException localInterruptedException1) {
        }
        if (myOutStream != null) {
            try {
                myOutStream.flush();
            } catch (IOException localIOException1) {
            }
            try {
                myOutStream.close();
            } catch (IOException localIOException2) {
            }
            myOutStream = null;
        }
        if (myInStream != null) {
            try {
                myInStream.close();
            } catch (IOException localIOException3) {
            }
            myInStream = null;
        }
        if (mySocket != null) {
            try {
                mySocket.close();
            } catch (IOException localIOException4) {
            }
            mySocket = null;
        }
        try {
            Thread.sleep(200L);
        } catch (InterruptedException localInterruptedException2) {
        }
        return true;
    }
}
