package com.example.fragmentstyle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.example.fragmentstyle.Constants.BT_CONNECTED;
import static com.example.fragmentstyle.Constants.BT_CONNECTION_LOST;
import static com.example.fragmentstyle.Constants.BT_DISCONNECTING;
import static com.example.fragmentstyle.Constants.BT_ERROR_OCCURRED;
import static com.example.fragmentstyle.Constants.MESSAGE_READ;
import static com.example.fragmentstyle.Constants.MESSAGE_WRITE;
import static com.example.fragmentstyle.BluetoothService.State.CONNECTED;
import static com.example.fragmentstyle.BluetoothService.State.CONNECTING;
import static com.example.fragmentstyle.BluetoothService.State.LISTEN;

public class BluetoothService {

    private static volatile BluetoothService INSTANCE = null;

    //  Bluetooth Settings
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String NAME = MY_UUID.toString();
    private static final String TAG = "BT_SVC:";
    private String MY_TAG = " Shawn_Log: BluetootheService: ";

    //  Member variables
    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    private static AcceptThread mAcceptThread;
    private Handler mHandler;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    //  State of BluetoothService
    private State mState;

    //  Available states of BluetoothService
    public enum State {
        NONE, LISTEN, CONNECTING, CONNECTED
    }

    private BluetoothService() {
        Log.d(MY_TAG, "Initializing mHandler in BluetoothService");
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        });
        mState = State.NONE;
    }

    /**
     * Get instance of BluetoothService to be used
     * @return An instance of BluetoothService
     */
    public static BluetoothService getInstance()  {
        if (INSTANCE == null) {
            synchronized (BluetoothService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluetoothService();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Send messages/updates to Activities
     * @param messageConstant MessageConstant to be captured in Activity handler
     */
    private synchronized void sendServiceMessage(int messageConstant) {
        mHandler.obtainMessage(messageConstant, -1, -1).sendToTarget();
    }

    private synchronized void sendServiceMessage(int messageConstant, Object object) {
        mHandler.obtainMessage(messageConstant, -1, -1, object).sendToTarget();
    }

    /**
     * Used by Activity to handle messages sent to it from this service
     * @param callback Callback to be handled by this service
     */
    public void registerNewHandlerCallback(Handler.Callback callback) {
        mHandler = new Handler(callback);
    }

    /**
     * Start listening for incoming Bluetooth connections
     */
    synchronized void listen() {
        //  Start listening on socket
        if (mAcceptThread == null && mState != LISTEN) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Connect to a specified BluetoothDevice
     * @param device Specific BluetoothDevice chosen by user
     */
    synchronized void connect(BluetoothDevice device) {
        disconnect();
        if (mConnectThread == null) {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        }
    }

    /**
     * Disconnect from any existing connections
     */
    public synchronized void disconnect() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mState = State.NONE;

        //  Listen to incoming Bluetooth connections, if not already doing so
        listen();
    }

    /**
     * Perform relevant actions on connected BluetoothSocket
     * @param mmSocket Connected socket
     */
    private synchronized void manageConnectedSocket(BluetoothSocket mmSocket) {
        if (mmSocket.isConnected()) {
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }
    }

    /**
     * Get name of connected remote device
     * @return Name of connected remote device, else empty string
     */
    String getConnectedDeviceName() {
        if (mState == CONNECTED) {
            return mConnectedThread.mmSocket.getRemoteDevice().getName();
        } else {
            return "";
        }
    }

    /**
     * Get state of BluetoothService
     * @return State of BluetoothService
     */
    public State getState() {
        return mState;
    }

    /**
     * Send message from local device to remote device
     * @param message Message to be sent
     */
    public synchronized void sendMessageToRemoteDevice(String message) {
        //  Create temporary object
        synchronized (this) {
            if (mState != CONNECTED) return;
        }
        mConnectedThread.write(message);
    }

    /**
     * Thread used to initiate connection to a remote device
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
            mmSocket = tmp;
            mState = CONNECTING;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    Log.e(TAG, "Unable to connect, closing client socket", connectException);
                    sendServiceMessage(BT_DISCONNECTING, mmDevice.getName());
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                    sendServiceMessage(BT_ERROR_OCCURRED);
                }
                return;
            }

            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            sendServiceMessage(BT_CONNECTED, mmDevice.getName());
            manageConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            try {
                sendServiceMessage(BT_DISCONNECTING, mmDevice.getName());
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
        }
    }

    /**
     * Thread used to handle a connected socket
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = CONNECTED;
        }

        public void run() {

            mmBuffer = new byte[256];
            int numBytes;
            String message;

            // Keep listening to the InputStream until an exception occurs.
            while (mState == CONNECTED) {
                try {
                    //  Read from the InputStream
                    numBytes = mmInStream.read(mmBuffer);
                    message = new String(mmBuffer).substring(0, numBytes);
                    // Send the obtained bytes to the UI activity.
                    Log.d(MY_TAG, "mHandler message (run): "+message);
                    mHandler.obtainMessage(MESSAGE_READ, numBytes, -1, message).sendToTarget();
                 } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    sendServiceMessage(BT_CONNECTION_LOST);
                    break;
                }
            }
        }

        // Call this from the main activity to sendMessageToRemoteDevice data to the remote device.
        void write(String command) {
            try {
                // Allocate bytes for integer indicating size and message itself
                mmOutStream.write(command.getBytes());
                mmOutStream.flush();
                // Share the sent message with the UI activity.
                Log.d(MY_TAG, "mHandler command sent to remote device: "+command);
                mHandler.obtainMessage(MESSAGE_WRITE, command.getBytes().length, -1, command).sendToTarget();
                //mHandler.obtainMessage(MESSAGE_READ, command.getBytes().length, -1, command).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
        }

        // Call this method from the main activity to shut down the connection.
        void cancel() {
            try {
                sendServiceMessage(BT_DISCONNECTING);
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
            mState = LISTEN;
        }

        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned.
            while (mState != CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case LISTEN:
                            case CONNECTING:
                                // A connection was accepted. Perform work associated with
                                // the connection in a separate thread.
                                sendServiceMessage(BT_CONNECTED, socket.getRemoteDevice().getName());
                                manageConnectedSocket(socket);
                                cancel();
                                break;
                            case NONE:
                            case CONNECTED:
                               cancel();
                               break;
                        }
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
