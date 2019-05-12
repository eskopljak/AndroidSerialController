package ba.unsa.etf.labirint3dserial;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

public class CDCSerial
{
    private final UsbDeviceConnection connection;

    private UsbInterface usbInterface;
    private UsbEndpoint inEndpoint;
    private UsbEndpoint outEndpoint;

    public CDCSerial(UsbDevice device, UsbDeviceConnection connection)
    {
        this.connection = connection;

        usbInterface = device.getInterface(nadjiPrviCDCInterfejs(device));
    }

    private static int nadjiPrviCDCInterfejs(UsbDevice device)
    {
        int interfaceCount = device.getInterfaceCount();

        for (int i = 0; i < interfaceCount; i++)
        {
            if (device.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA)
            {
                return i;
            }
        }
        return -1;
    }

    public boolean open()
    {
        if(!connection.claimInterface(usbInterface, true))
        {
            return false;
        }

        int numberEndpoints = usbInterface.getEndpointCount();

        for(int i = 0; i <= numberEndpoints - 1; i++)
        {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);

            if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.getDirection() == UsbConstants.USB_DIR_IN)
            {
                inEndpoint = endpoint;
            }
            else if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
            {
                outEndpoint = endpoint;
            }
        }

        if(outEndpoint == null || inEndpoint == null)
        {
            return false;
        }

        return true;
    }

    public void close()
    {
        connection.releaseInterface(usbInterface);
        connection.close();
    }


    public int write(byte[] buffer, int timeout)
    {
        if(buffer == null)
        {
            return 0;
        }

        return connection.bulkTransfer(outEndpoint, buffer, buffer.length, timeout);
    }
}

