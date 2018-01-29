package com.simonguest.BTPhotoTransfer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.simonguest.btxfr.ClientThread;
import com.simonguest.btxfr.MessageType;
import com.simonguest.btxfr.ProgressData;
import com.simonguest.btxfr.ServerThread;
import com.simonguest.utill.MyDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
* TODO: 1.Track Connection State, 2.Enable Bluetooth, 3.Add Functionality for Initializing Connections 4.Refactor Code For
* */

public class MainActivity extends Activity implements MyDialogFragment.Connection {
    private static final String TAG = "BTPHOTO/MainActivity";
    public static String TARGET_ADDRESS;
    private Spinner deviceSpinner;
    private ProgressDialog progressDialog;

    private Map<String, BluetoothDevice> deviceStringMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        byte[] b = new byte[]{29, 119, 2, 29, 104, 60, 29, 72, 2, 29, 107, 65, 11};
        String s = "12345678905";

        if (s.getBytes() == b){
            Log.d(TAG, "onCreate: THIS ARE EQUAL");
        } else {
            byte[] tempB = s.getBytes();
            Log.d(TAG, "onCreate: " + tempB.length);
            for (byte b1 : tempB) {
                Log.d(TAG, "onCreate: " + b1);
            }
        }

        Log.d(TAG, "onCreate: " + new String(b));

        deviceStringMap = new HashMap<>();

        MainApplication.clientHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MessageType.READY_FOR_DATA: {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + MainApplication.TEMP_IMAGE_FILE_NAME);
                        //Uri outputFileUri = Uri.fromFile(file);
                        Uri outputFileUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".my.package.name.provider", file);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        startActivityForResult(takePictureIntent, MainApplication.PICTURE_RESULT_CODE);
                        break;
                    }

                    case MessageType.COULD_NOT_CONNECT: {
                        Toast.makeText(MainActivity.this, "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.SENDING_DATA: {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Sending photo...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        break;
                    }

                    case MessageType.DATA_SENT_OK: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        Toast.makeText(MainActivity.this, "Photo was sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(MainActivity.this, "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.UPDATE_STATE_CONNECTING:
                        Toast.makeText(MainActivity.this, "CONNECTING...", Toast.LENGTH_SHORT).show();
                        break;

                    case MessageType.UPDATE_STATE_CONNECTED:
                        Toast.makeText(MainActivity.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };


        MainApplication.serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MessageType.DATA_RECEIVED: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageBitmap(image);
                        break;
                    }

                    case MessageType.DATA_TEXT_RECEIVED: {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                        EditText et = findViewById(R.id.editText);
                        byte[] readBuf = (byte[]) message.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, message.arg1);
                        Log.d(TAG, "handleMessage: " + readMessage);
                        et.setText(readMessage);
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(MainActivity.this, "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DATA_PROGRESS_UPDATE: {
                        // some kind of update
                        MainApplication.progressData = (ProgressData) message.obj;
                        double pctRemaining = 100 - (((double) MainApplication.progressData.remainingSize / MainApplication.progressData.totalSize) * 100);
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setMessage("Receiving photo...");
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setProgress(0);
                            progressDialog.setMax(100);
                            progressDialog.show();
                        }
                        progressDialog.setProgress((int) Math.floor(pctRemaining));
                        break;
                    }

                    case MessageType.INVALID_HEADER: {
                        Toast.makeText(MainActivity.this, "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };

        if (MainApplication.pairedDevices != null) {
            if (MainApplication.serverThread == null) {
                Log.v(TAG, "Starting server thread.  Able to accept photos.");
                MainApplication.serverThread = new ServerThread(MainApplication.adapter, MainApplication.serverHandler);
                MainApplication.serverThread.start();
            }
        }

        if (MainApplication.pairedDevices != null) {
            ArrayList<DeviceData> deviceDataList = new ArrayList<DeviceData>();
            for (BluetoothDevice device : MainApplication.pairedDevices) {
                deviceDataList.add(new DeviceData(device.getName(), device.getAddress()));
            }

            ArrayAdapter<DeviceData> deviceArrayAdapter = new ArrayAdapter<DeviceData>(this, android.R.layout.simple_spinner_item, deviceDataList);
            deviceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
            deviceSpinner.setAdapter(deviceArrayAdapter);

            Button clientButton = (Button) findViewById(R.id.clientButton);
            clientButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeviceData deviceData = (DeviceData) deviceSpinner.getSelectedItem();
                    for (BluetoothDevice device : MainApplication.adapter.getBondedDevices()) {
                        if (device.getAddress().contains(deviceData.getValue())) {
                            Log.v(TAG, "Starting client thread");
                            if (MainApplication.clientThread != null) {
                                MainApplication.clientThread.cancel();
                            }
                            MainApplication.clientThread = new ClientThread(device, MainApplication.clientHandler, true);
                            MainApplication.clientThread.start();


                        }
                    }
                    //MainApplication.clientHandler.sendEmptyMessageAtTime(MessageType.READY_FOR_DATA, 1000);
                }
            });

            final EditText et = findViewById(R.id.editText);
            et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeviceData deviceData = (DeviceData) deviceSpinner.getSelectedItem();
                    String text = et.getText().toString();
                    if (text.length() > 0) {
                        // Get the message bytes and tell the BluetoothChatService to write
                        byte[] send = text.getBytes();
                        Message message = new Message();
                        message.obj = send;
                        if (MainApplication.clientThread == null) {
                            return;
                        }
                        if (MainApplication.clientThread.incomingHandler == null) {
                            Toast.makeText(MainActivity.this, "Make Sure You Are Connected!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        MainApplication.clientThread.incomingHandler.sendMessage(message);
                    }
                    /*for (BluetoothDevice device : MainApplication.adapter.getBondedDevices()) {
                        if (device.getAddress().contains(deviceData.getValue())) {
                            Log.v(TAG, "Starting client thread");
                            if (MainApplication.clientThread != null) {
                                MainApplication.clientThread.cancel();
                            }
                            MainApplication.clientThread = new ClientThread(device, MainApplication.clientHandler, false);
                            MainApplication.clientThread.start();
                        }
                    }*/
                }
            });
        } else {
            Toast.makeText(this, "Bluetooth is not enabled or supported on this device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainApplication.PICTURE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Photo acquired from camera intent");
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), MainApplication.TEMP_IMAGE_FILE_NAME);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                    ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, MainApplication.IMAGE_QUALITY, compressedImageStream);
                    byte[] compressedImage = compressedImageStream.toByteArray();
                    Log.v(TAG, "Compressed image size: " + compressedImage.length);

                    // Display the image locally
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(image);

                    // Invoke client thread to send
                    Message message = new Message();
                    message.obj = compressedImage;
                    if (MainApplication.clientThread.incomingHandler == null) {
                        Toast.makeText(this, "Make Sure You Are Connected!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MainApplication.clientThread.incomingHandler.sendMessage(message);

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
    }

    public void goToBarcode(View view) {
        Intent intent = new Intent(this, BarcodeExample.class);
        startActivity(intent);
    }

    public void gotoTestClass(View view) {
        Intent intent = new Intent(this, TestSdk.class);
        intent.putExtra("ADDRESS", TARGET_ADDRESS);
        startActivity(intent);
    }

    class DeviceData {

        String spinnerText;
        String value;

        public DeviceData(String spinnerText, String value) {
            this.spinnerText = spinnerText;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return spinnerText;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_devices:
                ArrayList<String> devicesAddress = new ArrayList<>();
                ArrayList<String> devicesName = new ArrayList<>();
                for (BluetoothDevice device : MainApplication.adapter.getBondedDevices()) {
                    String data = device.getAddress() + "\n" + device.getName();
                    devicesAddress.add(data);
                    //devicesName.add(device.getName());
                    deviceStringMap.put(device.getAddress(), device);
                }

                showDialog(devicesAddress);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialog(ArrayList<String> devicesAddress) {

        FragmentManager manager = getFragmentManager();

        MyDialogFragment dialog = new MyDialogFragment();
        Bundle b = new Bundle();
        b.putStringArrayList("devices", devicesAddress);
        //b.putStringArrayList("devices_name", devicesName);
        dialog.setArguments(b);
        dialog.show(manager, "DIALOG");
    }

    public void initiateConnection(String deviceAddress) {
        //1.FIRST SOLUTION:
        Log.d(TAG, "initiateConnection: ");
        for (BluetoothDevice device : MainApplication.adapter.getBondedDevices()) {
            Log.d(TAG, "initiateConnection: " + device.getAddress() + " " + deviceAddress);
            if (device.getAddress().contains(deviceAddress)) {
                Log.v(TAG, "Starting client thread");
                if (MainApplication.clientThread != null) {
                    MainApplication.clientThread.cancel();
                }
                MainApplication.clientThread = new ClientThread(device, MainApplication.clientHandler, false);
                MainApplication.clientThread.start();
                return;
            }
        }


        //2.ANOTHER APPROACH IS :
        BluetoothDevice targetDevice = MainApplication.adapter.getRemoteDevice(deviceAddress.trim());
        Log.v(TAG, "Starting client thread");
        if (MainApplication.clientThread != null) {
            MainApplication.clientThread.cancel();
        }
        MainApplication.clientThread = new ClientThread(targetDevice, MainApplication.clientHandler, false);
        MainApplication.clientThread.start();
    }

    @Override
    public void startConnection(String deviceName) {
        Log.d(TAG, "startConnection: ");
        initiateConnection(deviceName);
    }

    /*
    * THIS BIG SECTION INCLUDE BARCODE PRINTING
    *
    *
    *
    *
    * */

    public void print1DBarcode(View paramView) throws IOException {
        String barcodeNumber = "54100005727123215";
        //String barcodeNumber = "54100005727";

        byte[] arrayOfByte9 = new byte[11];
        arrayOfByte9 = barcodeNumber.getBytes();
        /*arrayOfByte9[0] = 50;
        arrayOfByte9[1] = 57;
        arrayOfByte9[2] = 50;
        arrayOfByte9[3] = 51;
        arrayOfByte9[4] = 52;
        arrayOfByte9[5] = 53;
        arrayOfByte9[6] = 54;
        arrayOfByte9[7] = 55;
        arrayOfByte9[8] = 56;
        arrayOfByte9[9] = 57;
        arrayOfByte9[10] = 48;*/
        //tmp8_6;
        /*byte[] arrayOfByte3 = new byte[12];
        arrayOfByte3[0] = 48;
        arrayOfByte3[1] = 54;
        arrayOfByte3[2] = 53;
        arrayOfByte3[3] = 49;
        arrayOfByte3[4] = 48;
        arrayOfByte3[5] = 48;
        arrayOfByte3[6] = 48;
        arrayOfByte3[7] = 48;
        arrayOfByte3[8] = 52;
        arrayOfByte3[9] = 51;
        arrayOfByte3[10] = 50;
        arrayOfByte3[11] = 55;*/
        //tmp88_86;
        //paramView = WoosimCmd.printData();
        byte[] arrayOfByte1 = WoosimBarcode.createBarcode(65, 2, 60, true, arrayOfByte9);
        /*byte[] arrayOfByte2 = WoosimBarcode.createBarcode(66, 2, 60, true, arrayOfByte3);
        arrayOfByte3 = WoosimBarcode.createBarcode(67, 2, 60, true, arrayOfByte3);
        byte[] arrayOfByte4 = WoosimBarcode.createBarcode(68, 2, 60, true, new byte[]{48, 49, 50, 51, 52, 53, 54, 55});
        byte[] arrayOfByte5 = WoosimBarcode.createBarcode(69, 2, 60, true, arrayOfByte9);
        byte[] arrayOfByte6 = WoosimBarcode.createBarcode(70, 2, 60, true, arrayOfByte9);
        byte[] arrayOfByte7 = WoosimBarcode.createBarcode(71, 2, 60, true, arrayOfByte9);
        byte[] arrayOfByte8 = WoosimBarcode.createBarcode(72, 2, 60, true, arrayOfByte9);*/
        arrayOfByte9 = WoosimBarcode.createBarcode(73, 2, 60, true, arrayOfByte9);
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(1024);
        /*localByteArrayOutputStream.write("UPC-A Barcode\r\n\n\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte1);
        localByteArrayOutputStream.write("\n\n\n".getBytes());*/
        //localByteArrayOutputStream.write(paramView);
        /*localByteArrayOutputStream.write("UPC-E Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte2);
        localByteArrayOutputStream.write(paramView);
        localByteArrayOutputStream.write("EAN13 Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte3);
        localByteArrayOutputStream.write(paramView);
        localByteArrayOutputStream.write("EAN8 Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte4);
        localByteArrayOutputStream.write(paramView);
        localByteArrayOutputStream.write("CODE39 Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte5);
        localByteArrayOutputStream.write(paramView);
        localByteArrayOutputStream.write("ITF Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte6);
        localByteArrayOutputStream.write(paramView);
        localByteArrayOutputStream.write("CODEBAR Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte7);
        localByteArrayOutputStream.write(paramView);
        localByteArrayOutputStream.write("CODE93 Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte8);
        localByteArrayOutputStream.write(paramView);*/
        localByteArrayOutputStream.write("CODE128 Barcode\r\n".getBytes());
        localByteArrayOutputStream.write(arrayOfByte9);
        localByteArrayOutputStream.write("\n\n\n".getBytes());

        //localByteArrayOutputStream.write(paramView);
        //sendData(WoosimCmd.initPrinter());
        sendData(localByteArrayOutputStream.toByteArray());
    }

    private void sendData(byte[] paramArrayOfByte) {
        /*if (!ClientThread.isConnected) {
            Toast.makeText(this, "Not Connecter", Toast.LENGTH_LONG).show();
        }*/
        while (paramArrayOfByte.length <= 0) {
            return;
        }

        byte[] send = paramArrayOfByte;
        Message message = new Message();
        message.obj = send;
        if (MainApplication.clientThread == null) {
            return;
        }
        if (MainApplication.clientThread.incomingHandler == null) {
            Toast.makeText(MainActivity.this, "Make Sure You Are Connected!", Toast.LENGTH_SHORT).show();
            return;
        }
        MainApplication.clientThread.incomingHandler.sendMessage(message);
    }

}


class WoosimCmd {
    public static byte[] printData()
    {
        return new byte[] { 10 };
    }

    public static byte[] initPrinter()
    {
        return new byte[] { 27, 64 };
    }
}

class WoosimBarcode {
    private static final String TAG = "WoosimBarcode";

    public static byte[] createBarcode(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean, byte[] paramArrayOfByte)
    {
        int i = 1;
        /*if (!validateBarcodeParameter(paramInt2, paramInt3, paramInt1, paramArrayOfByte)) {
            return null;
        }*/

        byte[] arrayOfByte1 = new byte[13];
        arrayOfByte1[0] = 29;
        arrayOfByte1[1] = 119;
        arrayOfByte1[2] = ((byte)paramInt2);
        arrayOfByte1[3] = 29;
        arrayOfByte1[4] = 104;
        arrayOfByte1[5] = ((byte)paramInt3);
        arrayOfByte1[6] = 29;
        arrayOfByte1[7] = 72;
        if (paramBoolean) {}
        int x = 1;
        for (paramInt2 = i;; paramInt2 = 0)
        {
            arrayOfByte1[8] = ((byte)paramInt2);
            arrayOfByte1[9] = 29;
            arrayOfByte1[10] = 107;
            arrayOfByte1[11] = ((byte)paramInt1);
            arrayOfByte1[12] = ((byte)paramArrayOfByte.length);
            byte[] arrayOfByte2 = new byte[arrayOfByte1.length + paramArrayOfByte.length];
            System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
            System.arraycopy(paramArrayOfByte, 0, arrayOfByte2, arrayOfByte1.length, paramArrayOfByte.length);
            return arrayOfByte2;
        }
    }


    /*private static boolean validateBarcodeParameter(int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte)
    {
        if (((paramInt1 < 1) || (paramInt1 <= 8)) || ((paramInt2 < 0) || (paramInt2 > 255))) {}
        do
        {
            return false;
            switch (paramInt3)
            {
                default:
                    return false;
            }
        } while ((paramArrayOfByte.length < 11) || (paramArrayOfByte.length > 12));
        paramInt1 = 0;
        label94:
        if (paramInt1 >= paramArrayOfByte.length) {}
        label100:
        label606:
        for (;;)
        {
            return true;
            if ((paramArrayOfByte[paramInt1] < 48) || (paramArrayOfByte[paramInt1] > 57)) {
                break;
            }
            paramInt1 += 1;
            break label94;
            if ((paramArrayOfByte.length < 11) || (paramArrayOfByte.length > 13)) {
                break;
            }
            paramInt1 = 0;
            for (;;)
            {
                if (paramInt1 >= paramArrayOfByte.length) {
                    break label100;
                }
                if ((paramArrayOfByte[paramInt1] < 48) || (paramArrayOfByte[paramInt1] > 57)) {
                    break;
                }
                paramInt1 += 1;
            }
            if ((paramArrayOfByte.length < 7) || (paramArrayOfByte.length > 8)) {
                break;
            }
            paramInt1 = 0;
            for (;;)
            {
                if (paramInt1 >= paramArrayOfByte.length) {
                    break label100;
                }
                if ((paramArrayOfByte[paramInt1] < 48) || (paramArrayOfByte[paramInt1] > 57)) {
                    break;
                }
                paramInt1 += 1;
            }
            if ((paramArrayOfByte.length < 1) || (paramArrayOfByte.length > 255)) {
                break;
            }
            paramInt1 = 0;
            for (;;)
            {
                if (paramInt1 >= paramArrayOfByte.length) {
                    break label100;
                }
                if ((paramArrayOfByte[paramInt1] != 32) && (paramArrayOfByte[paramInt1] != 36) && (paramArrayOfByte[paramInt1] != 37) && (paramArrayOfByte[paramInt1] != 43) && (paramArrayOfByte[paramInt1] != 45) && (paramArrayOfByte[paramInt1] != 46) && (paramArrayOfByte[paramInt1] != 47) && ((48 > paramArrayOfByte[paramInt1]) || (paramArrayOfByte[paramInt1] > 57)) && ((65 > paramArrayOfByte[paramInt1]) || (paramArrayOfByte[paramInt1] > 90))) {
                    break;
                }
                paramInt1 += 1;
            }
            if ((paramArrayOfByte.length < 1) || (paramArrayOfByte.length > 255)) {
                break;
            }
            paramInt1 = 0;
            for (;;)
            {
                if (paramInt1 >= paramArrayOfByte.length) {
                    break label100;
                }
                if ((paramArrayOfByte[paramInt1] < 48) || (paramArrayOfByte[paramInt1] > 57)) {
                    break;
                }
                paramInt1 += 1;
            }
            if ((paramArrayOfByte.length < 1) || (paramArrayOfByte.length > 255)) {
                break;
            }
            paramInt1 = 0;
            for (;;)
            {
                if (paramInt1 >= paramArrayOfByte.length) {
                    break label100;
                }
                if ((paramArrayOfByte[paramInt1] != 36) && (paramArrayOfByte[paramInt1] != 43) && (paramArrayOfByte[paramInt1] != 45) && (paramArrayOfByte[paramInt1] != 46) && (paramArrayOfByte[paramInt1] != 47) && (paramArrayOfByte[paramInt1] != 58) && ((48 > paramArrayOfByte[paramInt1]) || (paramArrayOfByte[paramInt1] > 57)) && ((65 > paramArrayOfByte[paramInt1]) || (paramArrayOfByte[paramInt1] > 68))) {
                    break;
                }
                paramInt1 += 1;
            }
            if ((paramArrayOfByte.length < 1) || (paramArrayOfByte.length > 255)) {
                break;
            }
            paramInt1 = 0;
            for (;;)
            {
                if (paramInt1 >= paramArrayOfByte.length) {
                    break label100;
                }
                if ((paramArrayOfByte[paramInt1] < 0) || (paramArrayOfByte[paramInt1] > Byte.MAX_VALUE)) {
                    break;
                }
                paramInt1 += 1;
            }
            if ((paramArrayOfByte.length < 2) || (paramArrayOfByte.length > 255)) {
                break;
            }
            paramInt1 = 0;
            for (;;)
            {
                if (paramInt1 >= paramArrayOfByte.length) {
                    break label606;
                }
                if ((paramArrayOfByte[paramInt1] != 193) && (paramArrayOfByte[paramInt1] != 194) && (paramArrayOfByte[paramInt1] != 195) && (paramArrayOfByte[paramInt1] != 196) && ((paramArrayOfByte[paramInt1] < 0) || (paramArrayOfByte[paramInt1] > Byte.MAX_VALUE))) {
                    break;
                }
                paramInt1 += 1;
            }
        }
    }*/
}