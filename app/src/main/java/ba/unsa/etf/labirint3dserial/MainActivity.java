package ba.unsa.etf.labirint3dserial;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

public class MainActivity extends AppCompatActivity
{
    private UsbService usbService;
    private final ServiceConnection usbConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1)
        {
            usbService = ((UsbService.UsbBinder) arg1).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stetho.initializeWithDefaults(this);

        setajOnClickZaButtone();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        postaviFilter();
        startService(UsbService.class, usbConnection);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection)
    {
        if (!UsbService.SERVICE_CONNECTED)
        {
            Intent startService = new Intent(this, service);

            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void postaviFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                String toast_text = "";
                switch (intent.getAction())
                {
                    case UsbService.ACTION_USB_PERMISSION_GRANTED:
                        toast_text = "USB Ready";
                        break;
                    case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED:
                        toast_text = "USB Permission not granted";
                        break;
                    case UsbService.ACTION_NO_USB:
                        toast_text = "No USB connected";
                        break;
                    case UsbService.ACTION_USB_DISCONNECTED:
                        toast_text = "USB disconnected";
                        break;
                    case UsbService.ACTION_USB_NOT_SUPPORTED:
                        toast_text = "USB device not supported";
                        break;
                }

                Toast.makeText(context, toast_text, Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private void setajOnClickZaButtone()
    {
        ImageButton buttonDolje = (ImageButton) findViewById(R.id.buttDolje);
        buttonDolje.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String idiDolje = "s";
                if(usbService != null)
                {
                    usbService.write(idiDolje.getBytes());
                }
            }
        });

        ImageButton sendButton = (ImageButton) findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idiNaprijed = "w";
                if (usbService != null)
                {
                    usbService.write(idiNaprijed.getBytes());
                }
            }
        });

        ImageButton buttLijevo = (ImageButton) findViewById(R.id.buttLijevo);
        buttLijevo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String idiLijevo = "a";
                if (usbService != null)
                {
                    usbService.write(idiLijevo.getBytes());
                }
            }
        });

        ImageButton buttDesno = (ImageButton) findViewById(R.id.buttDesno);
        buttDesno.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String idiDesno = "d";
                if (usbService != null)
                {
                    usbService.write(idiDesno.getBytes());
                }
            }
        });


        ImageButton buttConfirm = (ImageButton) findViewById(R.id.buttConfirm);
        buttConfirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s = "k";
                if (usbService != null)
                {
                    usbService.write(s.getBytes());
                }
            }
        });

        ImageButton buttRotacijaLijevo = (ImageButton) findViewById(R.id.buttRotacijaLijevo);
        buttRotacijaLijevo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s = "i";
                if (usbService != null)
                {
                    usbService.write(s.getBytes());
                }
            }
        });

        ImageButton buttRotacijaDesno = (ImageButton) findViewById(R.id.buttRotacijaDesno);
        buttRotacijaDesno.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s = "o";
                if (usbService != null)
                {
                    usbService.write(s.getBytes());
                }
            }
        });

        ImageButton buttBack = (ImageButton) findViewById(R.id.buttBack);
        buttBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s = "b";
                if (usbService != null)
                {
                    usbService.write(s.getBytes());
                }
            }
        });
    }

}
