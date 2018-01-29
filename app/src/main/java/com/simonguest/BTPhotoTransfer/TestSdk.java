package com.simonguest.BTPhotoTransfer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.simonguest.BTPhotoTransfer.sdk.ZpSdk;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class TestSdk extends Activity {

    public static final String SANS_SERIF = "sans-serif";
    private static String address;
    public static final String TAG = "TestSdk";

    public static double lastHeight = 0;

    public static final int PAGE_WIDTH = 70;
    public static final int PAGE_HEIGHT = 150;
    public static final int RECT_WIDTH = PAGE_WIDTH;
    public static final int RECT_HEIGHT = PAGE_HEIGHT - (PAGE_HEIGHT/10);

    public static final int LINE_WIDTH_DIVIDER = 1;
    public static final int BORDER_WIDTH = 8;

    public static final double BORDER_OFFSET = 1;
    public static final double BORDER_LENGTH = RECT_WIDTH / 10;

    public static final double CONTENT_SMALL_FONT_HEIGHT = 2;
    public static final double CONTENT_FONT_HEIGHT = 2.5;
    public static final double TITLE_FONT_HEIGHT = 3;
    public static final double MID_FONT_HEIGHT = 4.5;
    public static final double BOLD_FONT_HEIGHT = 10;

    public static final int EN_NUMBER_HEIGHT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CanvasView canvasView = new CanvasView(this);
        //setContentView(canvasView);
        setContentView(R.layout.activity_test_sdk);

        address = getIntent().getStringExtra("ADDRESS");

    }

    public void startShow(View view) {
        ZpSdk.OpenPrinter(address);

        ZpSdk.zp_page_create(PAGE_WIDTH, PAGE_HEIGHT);
        //ZpSdk.zp_draw_rect(0, 0, RECT_WIDTH, RECT_HEIGHT, LINE_WIDTH_DIVIDER);
        //ZpSdk.zp_draw_line(0, 0, PAGE_WIDTH, RECT_HEIGHT, LINE_WIDTH_DIVIDER);
        //ZpSdk.zp_draw_line(0, RECT_HEIGHT / 2, PAGE_WIDTH, RECT_HEIGHT / 2, LINE_WIDTH_DIVIDER);
        //ZpSdk.zp_draw_text(0, rectHeight/2, "Everybody AAAA AAAA AAAA");

        ImageView imageView = findViewById(R.id.iv2);
        if (ZpSdk.getBitmap() != null) {
            imageView.setImageBitmap(ZpSdk.getBitmap());
        }

        /*
        * Draw Logo
        * */
        ;
        drawLogo();

        ZpSdk.zp_draw_text_ex(5, ZpSdk.getTextHeight(TITLE_FONT_HEIGHT ) * 2.5, "داخلی سریع", "sans-serif", TITLE_FONT_HEIGHT, 0, false, false, false, ZpSdk.LEFT_ALIGN);
        ZpSdk.zp_draw_text_ex(5, ZpSdk.getTextHeight(TITLE_FONT_HEIGHT) * 4.5 + ZpSdk.getTextHeight(TITLE_FONT_HEIGHT), "cod پیشکرایه", "sans-serif", TITLE_FONT_HEIGHT, 0, false, false, false, ZpSdk.LEFT_ALIGN);
        ZpSdk.zp_draw_text_ex(5, ZpSdk.getTextHeight(TITLE_FONT_HEIGHT) * 4, "021-64085", SANS_SERIF, TITLE_FONT_HEIGHT, 0, false, false, false, ZpSdk.RIGHT_ALIGN);

        ZpSdk.zp_draw_line(0, lastHeight / 8, RECT_WIDTH, lastHeight / 8, LINE_WIDTH_DIVIDER);

        int tempOffset = 10;
        lastHeight += tempOffset;
        ZpSdk.zp_draw_line(BORDER_OFFSET, lastHeight / 8 , BORDER_LENGTH + BORDER_OFFSET, lastHeight / 8, BORDER_WIDTH);
        ZpSdk.zp_draw_line(BORDER_OFFSET, lastHeight / 8, 1, lastHeight / 8 + BORDER_LENGTH, BORDER_WIDTH);

        ZpSdk.zp_draw_line(RECT_WIDTH - BORDER_OFFSET - BORDER_LENGTH, lastHeight/8, RECT_WIDTH - BORDER_OFFSET, lastHeight/8, BORDER_WIDTH);
        ZpSdk.zp_draw_line(RECT_WIDTH - BORDER_OFFSET, lastHeight/8, RECT_WIDTH - BORDER_OFFSET, lastHeight/8 + BORDER_LENGTH, BORDER_WIDTH);

        ZpSdk.zp_draw_text_ex(0, lastHeight + (ZpSdk.getTextHeight(BOLD_FONT_HEIGHT)/2),"زاهدان", SANS_SERIF, BOLD_FONT_HEIGHT, 0, true, false, false, ZpSdk.CENTER_ALIGN);

        ZpSdk.zp_draw_text_ex(BORDER_OFFSET + 15, lastHeight + 40, "به:", SANS_SERIF, TITLE_FONT_HEIGHT, 0 ,true, false ,false, ZpSdk.RIGHT_ALIGN);

        lastHeight += ZpSdk.getTextHeight(BOLD_FONT_HEIGHT);
        lastHeight += tempOffset;

        String address = "شوش خ صابونیان خ شهرداری ک دهم پ 30 واحد 1 بازرگانی زینو شرکت صنایع ماشین های اداری";

        int maxLength = 50;
        int innerPadding = 20;
        if(address.length() > 60) {
            String[] partedAddress = address.split("(?<=\\G.{60})");
            ZpSdk.zp_draw_text_ex(BORDER_OFFSET + innerPadding, lastHeight + (ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 2), partedAddress[0], SANS_SERIF, CONTENT_FONT_HEIGHT, 0, false, false, false, ZpSdk.RIGHT_ALIGN);
            ZpSdk.zp_draw_text_ex(BORDER_OFFSET + innerPadding, lastHeight + (ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 4), partedAddress[1], SANS_SERIF, CONTENT_FONT_HEIGHT, 0, false, false, false, ZpSdk.RIGHT_ALIGN);
        } else {
            ZpSdk.zp_draw_text_ex(BORDER_OFFSET + innerPadding, lastHeight, address, SANS_SERIF, CONTENT_FONT_HEIGHT, 0, false, false, false, ZpSdk.RIGHT_ALIGN);
        }

        String phoneNumber = "02144772277";
        ZpSdk.zp_draw_text_ex(BORDER_OFFSET + innerPadding, lastHeight + (ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 4) + EN_NUMBER_HEIGHT, phoneNumber, SANS_SERIF, CONTENT_FONT_HEIGHT, 0, true, false, false, ZpSdk.LEFT_ALIGN);


        lastHeight += (ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 6);
        //lastHeight += 10;

        ZpSdk.zp_draw_line(BORDER_OFFSET, lastHeight/8 -BORDER_LENGTH, BORDER_OFFSET, lastHeight/8, BORDER_WIDTH);
        ZpSdk.zp_draw_line(BORDER_OFFSET, lastHeight/8, BORDER_LENGTH + BORDER_OFFSET, lastHeight/8, BORDER_WIDTH);

        ZpSdk.zp_draw_line(RECT_WIDTH - BORDER_OFFSET, lastHeight/8 -BORDER_LENGTH, RECT_WIDTH - BORDER_OFFSET, lastHeight/8, BORDER_WIDTH);
        ZpSdk.zp_draw_line(RECT_WIDTH - BORDER_OFFSET - BORDER_LENGTH, lastHeight/8, RECT_WIDTH - BORDER_OFFSET, lastHeight/8, BORDER_WIDTH);
        //drawBarcode();

        lastHeight += tempOffset;

        ZpSdk.zp_draw_line(0, lastHeight/8, PAGE_WIDTH, lastHeight/8, LINE_WIDTH_DIVIDER);

        lastHeight += tempOffset;

        final int ROW_2_OFFSET = 30;
        String origin = "هشتگرد";
        ZpSdk.zp_draw_text_ex(ROW_2_OFFSET, lastHeight + ZpSdk.getTextHeight(CONTENT_SMALL_FONT_HEIGHT), String.format("از: %s", origin), SANS_SERIF, CONTENT_SMALL_FONT_HEIGHT, 0, true, false, false, ZpSdk.LEFT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_2_OFFSET - 6, lastHeight + ZpSdk.getTextHeight(CONTENT_SMALL_FONT_HEIGHT) * 3, "شرکت صنایع اداری", SANS_SERIF, CONTENT_SMALL_FONT_HEIGHT, 0, false, false, false, ZpSdk.LEFT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_2_OFFSET, lastHeight + ZpSdk.getTextHeight(CONTENT_SMALL_FONT_HEIGHT) * 5, "خیابان امام خمینی خیابان امینی کوچه عباسی پلاک 9 , واحد 10", SANS_SERIF, CONTENT_SMALL_FONT_HEIGHT, 0, false, false, false, ZpSdk.LEFT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_2_OFFSET, lastHeight + ZpSdk.getTextHeight(CONTENT_SMALL_FONT_HEIGHT) * 7 + EN_NUMBER_HEIGHT, "02233445566", SANS_SERIF, CONTENT_SMALL_FONT_HEIGHT, 0, false, false, false, ZpSdk.LEFT_ALIGN);

        ZpSdk.zp_draw_text_ex(ROW_2_OFFSET + 30, lastHeight + ZpSdk.getTextHeight(CONTENT_SMALL_FONT_HEIGHT) * 2.5, "تاریخ", SANS_SERIF, CONTENT_FONT_HEIGHT, 0, false, false, false, ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_2_OFFSET, lastHeight + ZpSdk.getTextHeight(CONTENT_SMALL_FONT_HEIGHT) * 5, "1396/06/26", SANS_SERIF, CONTENT_FONT_HEIGHT, 0, true, false, false, ZpSdk.RIGHT_ALIGN);

        lastHeight += ZpSdk.getTextHeight(CONTENT_SMALL_FONT_HEIGHT) * 8 + EN_NUMBER_HEIGHT;
        lastHeight += tempOffset;

        ZpSdk.zp_draw_line(0, lastHeight/8, RECT_WIDTH, lastHeight/8, LINE_WIDTH_DIVIDER);

        lastHeight+= tempOffset * 2;

        String barcode = "54100002675162463";
        drawBarcode(barcode);

        ZpSdk.zp_draw_text_ex(0, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT), barcode, SANS_SERIF, CONTENT_FONT_HEIGHT, 0, false, false, false, ZpSdk.CENTER_ALIGN);

        lastHeight += ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 2;
        lastHeight += tempOffset;

        ZpSdk.zp_draw_line(0, lastHeight/8, RECT_WIDTH, lastHeight/8, LINE_WIDTH_DIVIDER);

        lastHeight += tempOffset;

        int ROW_3_OFFSET_1 = 30;
        int ROW_3_OFFSET_2 = 150;
        int ROW_3_OFFSET_3 = RECT_WIDTH * 8 * 4 / 5;
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 1, "وزن", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 3, "ارزش", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 5, "محتویات کالا", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 7, "نوع بسته بندی", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);

        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 1, "19 کیلوگرم", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 3, "1.500.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 5, "لوازم آرایشی بهداشتی شخصی ...", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 7, "نوع بسته بندی", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);

        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_3, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 2, "بسته", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, true, false,false, ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_3_OFFSET_3, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 5, "89/800", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, true, false,false,ZpSdk.RIGHT_ALIGN);


        lastHeight += ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 8;
        lastHeight += tempOffset;

        ZpSdk.zp_draw_line(0, lastHeight/8, RECT_WIDTH, lastHeight/8, LINE_WIDTH_DIVIDER);

        lastHeight += tempOffset;

        final int ROW_4_OFFSET_1 = 30;
        final int ROW_4_OFFSET_2 = 160;
        final int ROW_4_OFFSET_3 = RECT_WIDTH * 8 / 2;
        final int ROW_4_OFFSET_4 = RECT_WIDTH * 8 * 4 / 5  + 10;

        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 1, "کرایه حمل:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 3, "بیمه:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 5, "ارزش افزوده:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 7, "وجه کالا:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_1, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 9, "هزینه بسته بندی:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);

        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 1, "330.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 3, "3.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 5, "43.470", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 7, "1.500.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_2, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 9, "50.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);

        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_3, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 1, "خارج از محدوده مبدا:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_3, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 3, "خارج از محدوده مقصد:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_3, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 5, "هزینه سوخت:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_3, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 7, "هزینه خدمات:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_3, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 9, "جمع کل:", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);

        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_4, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 1, "140.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_4, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 3, "120.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_4, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 5, "10.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_4, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 7, "20.000", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);
        ZpSdk.zp_draw_text_ex(ROW_4_OFFSET_4, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 9, "2.200.100", SANS_SERIF, CONTENT_FONT_HEIGHT , 0, false, false,false,ZpSdk.RIGHT_ALIGN);

        lastHeight += ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT) * 11;
        lastHeight += tempOffset;

        ZpSdk.zp_draw_rect(30/8, lastHeight/8, RECT_WIDTH - 30/8 , (lastHeight + ZpSdk.getTextHeight(MID_FONT_HEIGHT) * 2 + 30)/8, BORDER_WIDTH);

        String totalCash = "2.615.270";
        ZpSdk.zp_draw_text_ex(0, lastHeight + ZpSdk.getTextHeight(MID_FONT_HEIGHT) * 1.5, String.format("مبلغ قابل پرداخت: %s", totalCash), SANS_SERIF, MID_FONT_HEIGHT, 0, true, false, false,ZpSdk.CENTER_ALIGN);

        lastHeight += ZpSdk.getTextHeight(MID_FONT_HEIGHT) * 2 + 30;
        lastHeight += tempOffset;

        ZpSdk.zp_draw_line(0, lastHeight/8, RECT_WIDTH, lastHeight/8, LINE_WIDTH_DIVIDER);

        lastHeight += tempOffset;

        String cr = "دارنده این برگه تمامی قوانین و مقررات مندرج در وبسایت را قبول دارد.";
        ZpSdk.zp_draw_text_ex(0, lastHeight + ZpSdk.getTextHeight(CONTENT_FONT_HEIGHT), cr, SANS_SERIF, CONTENT_FONT_HEIGHT, 0, false, false, false, ZpSdk.CENTER_ALIGN);


        //ZpSdk.setTextAlign(ZpSdk.LEFT_ALIGN)
        /*StringBuilder tempText = new StringBuilder();
        tempText.append(".");
        int length = "021-64085".length();
        for (int i = 0; i < 40 - length; i++){
            tempText.append(" ");
        }
        String leftText = tempText + "021-64085";*/
        //ZpSdk.setTextAlign(ZpSdk.RIGHT_ALIGN);


        //ZpSdk.zp_draw_text_ex(0, RECT_HEIGHT * 7, "0123456789012345678901234567890123456789", "sans-serif", CONTENT_FONT_HEIGHT, 0, false, false, false, ZpSdk.CENTER_ALIGN);


        /*double y = 0;
        int i = 0;
        int offset = 0;
        offset = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher).getHeight() + 5 + barcodeHeight + 10;
        while ( y + ZpSdk.getTextHeight(fontHeight) * 2 < rectHeight * 8){
            y = ZpSdk.getTextHeight(fontHeight) * i + ZpSdk.getTextHeight(fontHeight) + offset;
            i += 2;
            ZpSdk.zp_draw_text_ex(rectWidth/2 * 8, y, "10123456789012345678901234567890123456789", "sans-serif", fontHeight, 0, false, false, false, false);
            Log.d(TAG, "startShow: " + i);
        }*/

    }

    private void drawLogo() {
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        logo = Bitmap.createScaledBitmap(logo, (int) (PAGE_WIDTH * 2.5), PAGE_WIDTH * 2, false);
        ZpSdk.zp_draw_bitmap(logo, PAGE_WIDTH / 2 * 8 - logo.getWidth() / 2, -2);
        lastHeight = logo.getHeight();
    }

    private void drawBarcode(String barcode) {
        Bitmap bitmap = null;
        int barcodeHeight;
        int widthFixer = 4 * 4;
        int barcodeWidth = (RECT_WIDTH / 2) * widthFixer;
        barcodeHeight = barcodeWidth / 5;
        try {
            bitmap = encodeAsBitmap(barcode, BarcodeFormat.CODE_128, barcodeWidth, barcodeHeight);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        ZpSdk.zp_draw_bitmap(bitmap, RECT_WIDTH / 2 * 8 - barcodeWidth / 2, lastHeight );

        lastHeight += barcodeHeight;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void printCanvas(View view) {
        if (ZpSdk.zp_check_printer()) {
            ZpSdk.zp_page_print(false);
        }
    }

    private class CanvasView extends View {

        public CanvasView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // custom drawing code here
            /*Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);

            // make the entire canvas white
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            // draw blue circle with anti aliasing turned on
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            float initX = 10;
            float initY = 10;
            float radius  = 80;
            canvas.drawCircle(initX, initY, radius, paint);

            // draw red rectangle with anti aliasing turned off
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            float rectWidth = 300;
            float rectHeight = 200;
            canvas.drawRect(initX, initY + 300, rectWidth + radius, initY + rectHeight , paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.GREEN);
            paint.setTextSize(40);
            canvas.drawText("CoderzHeaven, Heaven of all working codes", initX, initY + 600, paint);

            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M)
                canvas.restore();*/


            //ZpSdk.setCanvas(canvas);
        }
    }

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}
