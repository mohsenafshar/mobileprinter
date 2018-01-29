package com.simonguest.btxfr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ClientThread extends Thread {
    private final String TAG = "btxfr/ClientThread";
    private final BluetoothSocket socket;
    private final Handler handler;
    public Handler incomingHandler;
    private boolean isImage;
    public boolean isConnected;

    public ClientThread(BluetoothDevice device, Handler handler, boolean isImage) {
        BluetoothSocket tempSocket = null;
        this.handler = handler;
        this.isImage = isImage;

        isConnected = false;

        try {
            tempSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUID_STRING));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        this.socket = tempSocket;
    }

    public void run() {
        try {
            Log.v(TAG, "Opening client socket");
            handler.sendEmptyMessage(MessageType.UPDATE_STATE_CONNECTING);
            socket.connect();
            isConnected = true;
            handler.sendEmptyMessage(MessageType.UPDATE_STATE_CONNECTED);
            Log.v(TAG, "Connection established");

        } catch (IOException ioe) {
            isConnected = false;
            handler.sendEmptyMessage(MessageType.COULD_NOT_CONNECT);
            Log.d(TAG, "run: Connection Failed");
            Log.e(TAG, ioe.toString());
            try {
                socket.close();
            } catch (IOException ce) {
                Log.e(TAG, "Socket close exception: " + ce.toString());
            }
        }

        Looper.prepare();

        incomingHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if(!isConnected) {
                    Log.d(TAG, "handleMessage: not connected");
                    handler.sendEmptyMessage(MessageType.COULD_NOT_CONNECT);
                    return;
                }

                if (message.obj != null) {
                    Log.v(TAG, "Handle received data to send");
                    byte[] payload = (byte[]) message.obj;

                    try {
                        handler.sendEmptyMessage(MessageType.SENDING_DATA);
                        OutputStream outputStream = socket.getOutputStream();

                        /*outputStream.write(Constants.HEADER_MSB);
                        if(isImage){
                            outputStream.write(Constants.HEADER_LSB);
                        } else {
                            outputStream.write(Constants.HEADER_MMB);
                        }

                        // write size
                        outputStream.write(Utils.intToByteArray(payload.length));

                        // write digest
                        byte[] digest = Utils.getDigest(payload);
                        outputStream.write(digest);*/

                        // now write the data
                        outputStream.write(payload);
                        outputStream.flush();


                        ClientThread.this.handler.sendEmptyMessage(MessageType.DATA_SENT_OK);

                        /*Log.v(TAG, "Data sent.  Waiting for return digest as confirmation");
                        InputStream inputStream = socket.getInputStream();
                        byte[] incomingDigest = new byte[16];
                        int incomingIndex = 0;

                        try {
                            while (true) {
                                byte[] header = new byte[1];
                                inputStream.read(header, 0, 1);
                                incomingDigest[incomingIndex++] = header[0];
                                if (incomingIndex == 16) {
                                    if (Utils.digestMatch(payload, incomingDigest)) {
                                        Log.v(TAG, "Digest matched OK.  Data was received OK.");
                                        ClientThread.this.handler.sendEmptyMessage(MessageType.DATA_SENT_OK);
                                    } else {
                                        Log.e(TAG, "Digest did not match.  Might want to resend.");
                                        ClientThread.this.handler.sendEmptyMessage(MessageType.DIGEST_DID_NOT_MATCH);
                                    }

                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }*/

                        Log.v(TAG, "Closing the client socket.");
                        //socket.close();

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
            }
        };

        if(isImage && isConnected) {
            handler.sendEmptyMessage(MessageType.READY_FOR_DATA);
        } else {
            //handler.sendEmptyMessage(MessageType.READY_FOR_TEXT_DATA);
        }
        Looper.loop();
    }

    public void cancel() {
        try {
            if (socket.isConnected()) {
                socket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}