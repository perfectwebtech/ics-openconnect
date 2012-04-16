package de.blinkt.openvpn;

import java.util.LinkedList;

import android.os.ParcelFileDescriptor;
import android.util.Log;

public class OpenVPN {
	private static OpenVpnService mOpenVpnService;
	private static String localip;
	private static final int MAXLOGENTRIES = 500;
	public static native int startOpenVPNThread();
	public static native int startOpenVPNThreadArgs(String argv[]);
    private static final String TAG = "OpenVpn";

	
	public static LinkedList<String> logbuffer = new LinkedList<String>();
	private static int counter=0;


	 static {
		 System.loadLibrary("crypto");
		 System.loadLibrary("ssl");
		 System.loadLibrary("lzo");
		 System.loadLibrary("openvpn");
	    }

	 static void addRoute(String dest,String mask, String gw) {
	        Log.i("openvpn" ,"Got Routing information " + dest + " " + mask + "  " + gw  );		 
	 }
	 
	 synchronized static void logMessage(int level,String prefix, String message)
	 {
		 logbuffer.addFirst(prefix + " " + message);
		 if(logbuffer.size()>MAXLOGENTRIES)
			 logbuffer.removeLast();
		 
		 // The garbage collector does not collect the String from native
		 // but kills me for logging 100 messages with too many references :(
		 // Force GC how and then to kill loose ends
		 if(counter++ % 50==0)
			 System.gc();
	 }
	 
	 
	 static void addInterfaceInfo(int mtu, String local, String remote)
	 {
		 Log.i("openvpn","Got interface info M"  + mtu + " L: " + local + "R: " + remote);
		 localip=local;
	 }

	public static void setCallback(OpenVpnService openVpnService) {
		mOpenVpnService = openVpnService;
	}

	public static boolean protectSocket (int sockfd)
	{
		boolean p = mOpenVpnService.protect(sockfd);
		if(p)
			Log.d("openvpn","Protected socket "+ sockfd);
		else
			Log.e("openvpn","Error protecting socket "+ sockfd);
		return p;
	}
	
	public static int openTunDevice() {
		Log.d(TAG,"Opening tun device");
		ParcelFileDescriptor pfd = mOpenVpnService.openTun(localip);
		return pfd.detachFd();
	}
	//! Dummy method being called to force loading of JNI Libraries
	public static void foo() {	}
	
	synchronized public static String[] getlogbuffer() {
		return (String[]) logbuffer.toArray(new String[logbuffer.size()]);

	}
}
