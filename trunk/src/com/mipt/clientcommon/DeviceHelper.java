package com.mipt.clientcommon;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

public class DeviceHelper {
	private static final String TAG = "DeviceHelper";
	private static final String VENDOR_MIPT = "MiPT";
	private static final String VENDOR_SKYWORTH = "SKYWORTH";
	private static final String VENDOR_SKYWORTH_DIGITAL_RT = "SkyworthDigitalRT";
	private static final String VENDOR_SKYWORTH_DIGITAL = "SkyworthDigital";
	private static final String VENDOR_HAO = "HAO";

	private static final String A4_INFO_FILE_DEVICE_ID = "/private/.id.txt";
	private static String HOST_SKYWORTH = "ro.stb.user.url";
	private static final String VENDOR_SKYWORTH_PROP = "ro.product.manufacturer";
	private static final int DEVICE_MIPT_IKAN_A6 = 2;
	private static final int DEVICE_MIPT_IKAN_A4 = 3;
	private static final int DEVICE_MIPT_BETV_U6 = 4;
	private static final int DEVICE_SKYWORTH_NEXT_PANDORA = 5;
	private static final int DEVICE_MIPT_IKAN_TEST = 6;
	private static final int DEVICE_MIPT_BETV_TEST = 7;
	private static final int DEVICE_SKYWORTH_HSM1 = 8;
	private static final int DEVICE_MIPT_IKAN_A8 = 9;
	private static final int DEVICE_BST_SNT_B10 = 10;
	private static final int DEVICE_SKYWORTH_HAO = 11;
	private static final int DEVICE_MIPT_IKAN_A5 = 12;
	private static final int DEVICE_SKYWORTH_NORMAL = 13;
	private static final int DEVICE_MIPT_IKAN_R6 = 14;
	private static final int DEVICE_MIPT_IKAN_A7 = 15;
	private static final int DEVICE_SKYWORTH_MB1110 = 16;
	private static final int DEVICE_SKYWORTH_I71S = 17;
	private static final int DEVICE_SKYWORTH_OTHER = 18;
	private static String sDeviceId = null;
	private static String mVendor = null;
	private static String mModel = null;
	private static String mMac = null;
	private static String mName = null;

	public static String getDeviceVendor() {
		if (mVendor == null) {
			String vendor = android.os.Build.MANUFACTURER;
			if (vendor != null) {
				if (VENDOR_MIPT.equals(vendor)
						|| VENDOR_SKYWORTH_DIGITAL_RT.equalsIgnoreCase(vendor)
						|| VENDOR_SKYWORTH_DIGITAL.equalsIgnoreCase(vendor)
						|| VENDOR_HAO.equalsIgnoreCase(vendor)
						|| VENDOR_SKYWORTH.equalsIgnoreCase(vendor)) {
					mVendor = vendor; // MiPT device
				}
			} else { // Maybe Skyworth device
				vendor = getSysProperties(VENDOR_SKYWORTH_PROP);
				if (VENDOR_SKYWORTH.equalsIgnoreCase(vendor)) {
					mVendor = vendor;
				} else {
					mVendor = "";// TODO 设置为空会有哪些影响？
				}
			}
		}
		return mVendor;
	}

	public static String getDeviceModel() {
		if (TextUtils.isEmpty(mModel)) {
			String model = "";
			String vendor = getDeviceVendor();
			if (VENDOR_MIPT.equals(vendor)) {
				model = android.os.Build.MODEL;
			} else if (VENDOR_SKYWORTH.equals(vendor)) {
				model = getSysProperties("ro.product.model");
				if (TextUtils.isEmpty(model)) {
					model = getSkParam("skyworth.params.sys.product_type");
				}
			}
			mModel = model;
		}
		return mModel;
	}

	public static String getDeviceMac() {
		if (TextUtils.isEmpty(mMac)) {
			String vendor = getDeviceVendor();
			String mac = "";
			if (VENDOR_MIPT.equals(vendor)) {
				mac = getFileInfo("/sys/class/net/eth0/address");
			} else if (VENDOR_SKYWORTH.equals(vendor)) {
				String model = getDeviceModel();
				if ("MB1110".equals(model)) {
					mac = getFileInfo("/sys/class/net/eth0/address");
				} else {
					mac = getSkParam("skyworth.params.sys.mac");
				}

			}
			mMac = mac;
		}

		return mMac;
	}

	public static String getDeviceName(Context context) {
		String vendor = getDeviceVendor();
		String name = "";
		if (VENDOR_MIPT.equals(vendor)) {
			name = getMiptDeviceName(context);
		} else if (VENDOR_SKYWORTH.equals(vendor)) {
			String model = getDeviceModel();
			if ("MB1110".equals(model)) {
				name = getSysProperties("ro.build.id");
			} else {
				name = getSkParam("skyworth.params.sys.product_name");
			}
		}
		mName = name;

		return mName;
	}

	private static String getMiptDeviceName(Context context) {
		String name = "";
		try {
			Uri uri = Uri.parse("content://mipt.ott_setting/conf");
			ContentResolver cr = context.getContentResolver();
			Cursor c = cr
					.query(uri,
							null,
							"confgroup = \"ott_device_info\" and name = \"ott_device_dlna_name\" ",
							null, null);
			if (c != null) {
				c.moveToFirst();
				name = c.getString(c.getColumnIndexOrThrow("value"));
				c.close();
			} else {
				Log.e(TAG, "cursor is null");
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}

		return name;
	}

	public static String getDeviceId(Context context) {
		if (TextUtils.isEmpty(sDeviceId)) {
			int deviceType = deviceType(context);
			switch (deviceType) {
			case DEVICE_MIPT_IKAN_A6:
			case DEVICE_MIPT_BETV_U6:
			case DEVICE_SKYWORTH_NEXT_PANDORA:
			case DEVICE_SKYWORTH_NORMAL:
			case DEVICE_SKYWORTH_HSM1:
			case DEVICE_SKYWORTH_HAO:
			case DEVICE_MIPT_IKAN_TEST:
			case DEVICE_MIPT_BETV_TEST:
				// skyworth device id
				sDeviceId = getSysProperties("persist.sys.hwconfig.stb_id");
				break;

			case DEVICE_MIPT_IKAN_A4:
			case DEVICE_BST_SNT_B10:
				sDeviceId = getFileInfo(A4_INFO_FILE_DEVICE_ID);
				break;

			case DEVICE_MIPT_IKAN_A7:
			case DEVICE_MIPT_IKAN_A8:
				sDeviceId = getFileInfo("/sys/class/mipt_hwconfig/deviceid");
				break;
			case DEVICE_MIPT_IKAN_A5:
			case DEVICE_MIPT_IKAN_R6:
				sDeviceId = getFileInfo("/hwcfg/.id.txt");
				break;
			case DEVICE_SKYWORTH_MB1110:
				// id = getSysProperties("ro.serialno");
				String mac = getDeviceMac(); // TEMP
				sDeviceId = "MB1100_" + mac.replaceAll(":", "");
				break;
			case DEVICE_SKYWORTH_I71S:
				Log.d(TAG, "#DEVICE_SKYWORTH_I71S");
				sDeviceId = getFileInfo("/sys/class/sky_hwconfig/deviceid");
				break;
			// case DEVICE_SKYWORTH_OTHER:
			// sDeviceId = getSkParam("skyworth.params.sys.sn");
			// break;
			default:
				// according to discussion, our device will use the file as
				// default device id file.
				sDeviceId = getFileInfo("/sys/class/sky_hwconfig/deviceid");
				if (sDeviceId == null || sDeviceId.trim().length() <= 0) {
					retriveFinalDevId(context);
				}
				break;
			}
		}

		return sDeviceId;
	}

	private static final Object devIdLock = new Object();

	private static void retriveFinalDevId(Context context) {
		if (sDeviceId == null || sDeviceId.trim().length() <= 0) {
			synchronized (devIdLock) {
				if (sDeviceId == null || sDeviceId.trim().length() <= 0) {
					String savedDeviceId = (String) Prefs.getInstance(context)
							.get(Prefs.TYPE_STRING,
									InternalUtils.ITEM_DEVICE_ID, null);
					if (savedDeviceId == null) {
						String deviceId = getDeviceMac(context);
						if (deviceId == null || "".equals(deviceId.trim())) {
							deviceId = Settings.Secure.getString(
									context.getContentResolver(),
									Secure.ANDROID_ID);
							if (deviceId == null || "".equals(deviceId.trim())) {
								deviceId = "ID_" + System.nanoTime();
							}
						}
						Prefs.getInstance(context).save(Prefs.TYPE_STRING,
								InternalUtils.ITEM_DEVICE_ID, deviceId);
						sDeviceId = deviceId;
					} else {
						sDeviceId = savedDeviceId;
					}
				}
			}
		}
	}

	public static String getDeviceMac(Context context) {
		String ret = null;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			ret = getMacEth();
			return ret;
		}
		String netTypeName = ni.getTypeName();
		if (netTypeName.equals("WIFI")) {
			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			ret = wm.getConnectionInfo().getMacAddress();
		} else {
			ret = getMacEth();

		}
		return ret;
	}

	private static String getMacEth() {
		String macEth = null;
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> net = intf.getInetAddresses(); net
						.hasMoreElements();) {
					InetAddress iaddr = net.nextElement();
					if (iaddr instanceof Inet4Address) {
						if (!iaddr.isLoopbackAddress()) {
							byte[] data = intf.getHardwareAddress();
							StringBuilder sb = new StringBuilder();
							if (data != null && data.length > 1) {
								sb.append(parseByte(data[0])).append(":")
										.append(parseByte(data[1])).append(":")
										.append(parseByte(data[2])).append(":")
										.append(parseByte(data[3])).append(":")
										.append(parseByte(data[4])).append(":")
										.append(parseByte(data[5]));
							}
							macEth = sb.toString();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return macEth;
	}

	private static String parseByte(byte b) {
		int intValue = 0;
		if (b >= 0) {
			intValue = b;
		} else {
			intValue = 256 + b;
		}
		return Integer.toHexString(intValue);
	}

	private static final int deviceType(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		int deviceType = sp.getInt("device_type", 0);

		if (deviceType < 2) { // need load device type from system
			// new way to identify a device
			String platformName = getSysProperties("mipt.ott.platform.name");
			if (!TextUtils.isEmpty(platformName)) {
				if ("A3".equals(platformName) || "A6".equals(platformName)) {
					deviceType = DEVICE_MIPT_IKAN_A6;
				} else if ("A4".equals(platformName)) {
					deviceType = DEVICE_MIPT_IKAN_A4;
				} else if ("A8".equals(platformName)) {
					deviceType = DEVICE_MIPT_IKAN_A8;
				} else if ("A7".equals(platformName)) {
					deviceType = DEVICE_MIPT_IKAN_A7;
				} else if ("A5".equals(platformName)) {
					deviceType = DEVICE_MIPT_IKAN_A5;
				} else if ("R6".equals(platformName)) {
					deviceType = DEVICE_MIPT_IKAN_R6;
				}
			}

			if (deviceType < 2) {
				String url = getSysProperties(HOST_SKYWORTH);
				if (!TextUtils.isEmpty(url)) {
					deviceType = DEVICE_SKYWORTH_NORMAL;
				}
			}

			// old way to identify a device
			if (deviceType < 2) {
				if ("MiPT".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
					deviceType = checkMiptDevice();

				} else if ("SkyworthDigitalRT"
						.equalsIgnoreCase(android.os.Build.MANUFACTURER)
						|| "SkyworthDigital"
								.equalsIgnoreCase(android.os.Build.MANUFACTURER)
						|| "HAO".equalsIgnoreCase(android.os.Build.MANUFACTURER)
						|| "SKYWORTH"
								.equalsIgnoreCase(android.os.Build.MANUFACTURER)
						|| VENDOR_SKYWORTH
								.equalsIgnoreCase(getSysProperties("ro.product.manufacturer"))) {
					deviceType = checkSkyworthDevice();

				} else if ("BestTech"
						.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
					deviceType = checkBstDevice();
				}
			}

			// save the useful device information.
			if (deviceType >= 2) {
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("device_type", deviceType);
				editor.commit();
				Log.d(TAG, "Save device type: " + deviceType);
			}
		}

		// Log.d(TAG, "::TD > " + deviceType);
		return deviceType;
	}

	private static String getSysProperties(String name) {
		String id = null;
		try {
			Class c = Class.forName("android.os.SystemProperties");
			Method m = c.getMethod("get", new Class[] { String.class });
			id = (String) m.invoke(c, new Object[] { name });
		} catch (ClassNotFoundException cnfe) {
			Log.e(TAG, "Error", cnfe);
		} catch (NoSuchMethodException nsme) {
			Log.e(TAG, "Error", nsme);
		} catch (SecurityException se) {
			Log.e(TAG, "Error", se);
		} catch (IllegalAccessException iae) {
			Log.e(TAG, "Error", iae);
		} catch (IllegalArgumentException iarge) {
			Log.e(TAG, "Error", iarge);
		} catch (InvocationTargetException ite) {
			Log.e(TAG, "Error", ite);
		} catch (ClassCastException cce) {
			Log.e(TAG, "Error", cce);
		} catch (Throwable th) {
			Log.e(TAG, "Error: ", th);
		}

		return id;
	}

	private static String getSkParam(String name) {
		String value = null;
		try {
			Class c = Class.forName("com.skyworth.sys.param.SkParam");
			Method m = c.getMethod("getParam", new Class[] { String.class });
			value = (String) m.invoke(c, new Object[] { name });
		} catch (ClassNotFoundException cnfe) {
			Log.e(TAG, "Error", cnfe);
		} catch (NoSuchMethodException nsme) {
			Log.e(TAG, "Error", nsme);
		} catch (SecurityException se) {
			Log.e(TAG, "Error", se);
		} catch (IllegalAccessException iae) {
			Log.e(TAG, "Error", iae);
		} catch (IllegalArgumentException iarge) {
			Log.e(TAG, "Error", iarge);
		} catch (InvocationTargetException ite) {
			Log.e(TAG, "Error", ite);
		} catch (ClassCastException cce) {
			Log.e(TAG, "Error", cce);
		} catch (Throwable th) {
			Log.e(TAG, "Error: ", th);
		}

		return value;
	}

	private static String getFileInfo(String filename) {
		if (filename == null || filename.length() < 1) {
			return null;
		}
		String info = null;
		FileReader fr = null;
		try {
			fr = new FileReader(filename);
			StringBuilder sb = new StringBuilder();
			int ch = 0;
			while ((ch = fr.read()) != -1) {
				if ('\n' == ch || '\r' == ch) {
					continue;
				}
				sb.append((char) ch);
			}
			info = sb.toString();
		} catch (Exception e) {
			Log.e(TAG, "error", e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception e) {
				}
			}
		}

		Log.d(TAG, "info:" + info);
		return info;
	}

	private static int checkMiptDevice() {
		int deviceType = 0;

		if ("i.Kan".equalsIgnoreCase(android.os.Build.BRAND)) {
			if ("A6".equalsIgnoreCase(android.os.Build.MODEL)
					|| "A3".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_MIPT_IKAN_A6; // A6, A3
			} else if ("A4".equalsIgnoreCase(android.os.Build.MODEL)
					|| "A400".equalsIgnoreCase(android.os.Build.MODEL)
					|| "A401".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_MIPT_IKAN_A4; // A4
			} else if ("A8".equalsIgnoreCase(android.os.Build.MODEL)
					|| "A800".equalsIgnoreCase(android.os.Build.MODEL)
					|| "A800D".equalsIgnoreCase(android.os.Build.MODEL)) { // A8
				deviceType = DEVICE_MIPT_IKAN_A8;
			} else if ("test".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_MIPT_IKAN_TEST;
			}
		} else if ("BeTV".equalsIgnoreCase(android.os.Build.BRAND)) {
			if ("BeTV-U6".equalsIgnoreCase(android.os.Build.MODEL)
					|| "BeTV-U8".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_MIPT_BETV_U6; // BeTV-U6
			} else if ("test".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_MIPT_BETV_TEST;
			}
		}

		return deviceType;
	}

	private static final int checkSkyworthDevice() {
		int deviceType = 0;
		if ("MB1110".equalsIgnoreCase(android.os.Build.MODEL)) {
			deviceType = DEVICE_SKYWORTH_MB1110;
		} else if ("NEXT".equalsIgnoreCase(android.os.Build.BRAND)) {
			if ("pandorativibu".equalsIgnoreCase(android.os.Build.MODEL)
					|| "pandora".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_SKYWORTH_NEXT_PANDORA;
			}
		} else if ("Skyworth".equalsIgnoreCase(android.os.Build.BRAND)) {
			if (android.os.Build.MODEL != null
					&& (android.os.Build.MODEL.startsWith("HSM") || android.os.Build.MODEL
							.startsWith("hsm"))) {
				deviceType = DEVICE_SKYWORTH_HSM1;
			}
		} else if ("HAO".equalsIgnoreCase(android.os.Build.BRAND)) {
			if ("HA2800".equalsIgnoreCase(android.os.Build.MODEL)
					|| "HAO2".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_SKYWORTH_HAO;
			}
		} else if ("Skyworth Android".equalsIgnoreCase(android.os.Build.BRAND)) {
			if ("Android on skyworth SDK"
					.equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_MIPT_IKAN_A6;
			}
		} else {
			String mode = getSysProperties("ro.product.model");
			Log.d(TAG, "ro.product.model:" + mode);
			if ("I71S".equalsIgnoreCase(mode)) {
				deviceType = DEVICE_SKYWORTH_I71S;
			} else {
				deviceType = DEVICE_SKYWORTH_OTHER;
			}
		}
		return deviceType;
	}

	private static final int checkBstDevice() {
		int deviceType = 0;

		if ("SNT".equalsIgnoreCase(android.os.Build.BRAND)) {
			if ("SNT-T01".equalsIgnoreCase(android.os.Build.MODEL)
					|| "Shinco F90".equalsIgnoreCase(android.os.Build.MODEL)
					|| "SNT-B11".equalsIgnoreCase(android.os.Build.MODEL)
					|| "AD201".equalsIgnoreCase(android.os.Build.MODEL)
					|| "AD202".equalsIgnoreCase(android.os.Build.MODEL)
					|| "SNT-B09B".equalsIgnoreCase(android.os.Build.MODEL)
					|| "SNT-B09".equalsIgnoreCase(android.os.Build.MODEL)) {
				deviceType = DEVICE_BST_SNT_B10;
			}
		}

		return deviceType;
	}
}
