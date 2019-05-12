package ba.unsa.etf.labirint3dserial;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public class UsbService extends Service
{

    public static final String ACTION_USB_READY = "USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_PERMISSION = "USB_PERMISSION";

    public static boolean SERVICE_CONNECTED = false;

    private IBinder binder = new UsbBinder();

    private Context context;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private CDCSerial cdcSerialPort;

    private boolean serialPortKonektovan;

    @Override
    public void onCreate()
    {
        this.context = this;
        serialPortKonektovan = false;
        UsbService.SERVICE_CONNECTED = true;

        postaviFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        nadjiSpojenUredjaj();
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(ACTION_USB_PERMISSION))
            {
                boolean odobreno = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                if (odobreno) // Acceptano, probaj otvoriti
                {
                    Intent novi_intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    context.sendBroadcast(novi_intent);

                    connection = usbManager.openDevice(device);

                    new ConnectionThread().start();
                }
                else // Nije acceptano
                {
                    Intent novi_intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    context.sendBroadcast(novi_intent);
                }
            }
            else if (intent.getAction().equals(ACTION_USB_ATTACHED))
            {
                if (!serialPortKonektovan)
                {
                    nadjiSpojenUredjaj(); // Attachan usb, probaj otvoriti kao serial port
                }
            }
            else if (intent.getAction().equals(ACTION_USB_DETACHED))
            {
                // Iskopcan usb
                Intent novi_intent = new Intent(ACTION_USB_DISCONNECTED);
                context.sendBroadcast(novi_intent);

                if (serialPortKonektovan)
                {
                    cdcSerialPort.close();
                }
                serialPortKonektovan = false;
            }
        }
    };

    private void nadjiSpojenUredjaj()
    {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        if (!usbDevices.isEmpty())
        {
            boolean nijeSpojen = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
            {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

                if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003) && deviceVID != 0x5c6 && devicePID != 0x904c)
                {
                    // Postoji spojen uredjaj
                    requestUserPermission();
                    nijeSpojen = false;
                }
                else
                {
                    connection = null;
                    device = null;
                }

                if (!nijeSpojen) break;
            }
            if (!nijeSpojen)
            {
                Intent intent = new Intent(ACTION_NO_USB);
                sendBroadcast(intent);
            }
        }
        else
        {
            Intent intent = new Intent(ACTION_NO_USB);
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        UsbService.SERVICE_CONNECTED = false;
    }

    public void write(byte[] data)
    {
        if (cdcSerialPort != null)
        {
            cdcSerialPort.write(data, 0);
        }
    }


    private void postaviFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    private void requestUserPermission()
    {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    public class UsbBinder extends Binder
    {
        public UsbService getService()
        {
            return UsbService.this;
        }
    }

    // za otvaranje serial porta
    private class ConnectionThread extends Thread
    {
        @Override
        public void run()
        {
            cdcSerialPort = new CDCSerial(device, connection);

            if (cdcSerialPort.open())
            {
                serialPortKonektovan = true;

                Intent intent = new Intent(ACTION_USB_READY);
                context.sendBroadcast(intent);
            }
            else
            {
                Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                context.sendBroadcast(intent);
            }
        }
    }
}

