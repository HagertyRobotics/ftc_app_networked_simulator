package com.ftdi.j2xx;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class D2xxManager
{
    private static D2xxManager mInstance = null;
    private static final String TAG = "D2xx::";
    protected static final String ACTION_USB_PERMISSION = "com.ftdi.j2xx";
    private ArrayList<FT_Device> mFtdiDevices;

    private D2xxManager(Context parentContext)
            throws D2xxManager.D2xxException
    {
        Log.v("D2xx::", "Start constructor");

        if (parentContext == null) {
            throw new D2xxException("D2xx init failed: Can not find parentContext!");
        }
        //updateContext(parentContext);

        Log.v("D2xx::", "End constructor");
    }


    public int createDeviceInfoList(Context parentContext)
    {
        ArrayList devices = new ArrayList();
        FT_Device ftDev = null;
        int rc = 0;

        if (parentContext == null) return rc;

        // Check if this is the first time, don't change anything after the 1st call, we already made a list
        // Real code would look again for new usb devices
        if (this.mFtdiDevices==null) {
            ftDev = new FT_Device("A501E27V", "Hagerty USB1");
            devices.add(ftDev);

//            ftDev = new FT_Device("A1", "Hagerty USB2");
//            devices.add(ftDev);

            this.mFtdiDevices = devices;
        }



        rc = this.mFtdiDevices.size();

        return rc;
    }

    public synchronized FT_Device openBySerialNumber(Context parentContext, String serialNumber)
    {
        FtDeviceInfoListNode devInfo = null;
        FT_Device ftDev = null;

        if (parentContext == null) return ftDev;

        //updateContext(parentContext);

        for (int i = 0; i < this.mFtdiDevices.size(); i++) {
            FT_Device tmpDev = (FT_Device)this.mFtdiDevices.get(i);
            if (tmpDev == null)
                continue;
            devInfo = tmpDev.mDeviceInfoNode;

            if (devInfo == null) {
                Log.d("D2xx::", "***devInfo cannot be null***");
            }
            else if (devInfo.serialNumber.equals(serialNumber)) {
                ftDev = tmpDev;
                break;
            }

        }

        return ftDev;
    }


    public static class D2xxException extends IOException
    {
        private static final long serialVersionUID = 1L;

        public D2xxException()
        {
        }

        public D2xxException(String ftStatusMsg)
        {
            super();
        }
    }

    public synchronized FtDeviceInfoListNode getDeviceInfoListDetail(int index)
    {
        if ((index > this.mFtdiDevices.size()) || (index < 0))
            return null;
        return ((FT_Device)this.mFtdiDevices.get(index)).mDeviceInfoNode;
    }


    public static synchronized D2xxManager getInstance(Context parentContext)
            throws D2xxManager.D2xxException
    {
        if (mInstance == null) mInstance = new D2xxManager(parentContext);

        return mInstance;
    }

    public static class FtDeviceInfoListNode
    {
        public int flags;
        public short bcdDevice;
        public int type;
        public byte iSerialNumber;
        public int id;
        public int location;
        public String serialNumber;
        public String description;
        public int handle;
        public int breakOnParam;
        public short modemStatus;
        public short lineStatus;
    }
}
