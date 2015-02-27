package com.mipt.clientcommon.download;

import java.io.File;
import java.io.FilenameFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.os.Environment;

public class DownloadUtils {

	private static final String TEMP_FILE_SURFIX = ".apk.tmp";
	private static final String FORMAL_FILE_SURFIX = ".apk";

	static final int DOWNLOAD_RETRY_TIMES = 2;

	public static String genKeyForUrl(String url) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(url.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(url.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	private static String getDownloadRootPathWithSdcard(Context context) {
		String rootPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "Download"
				+ File.separator + context.getPackageName() + File.separator;
		return rootPath;
	}

	private static String getDownloadRootPathWithoutSdcard(Context context) {
		String rootPath = context.getCacheDir().getAbsolutePath()
				+ File.separator + "apps";
		return rootPath;
	}

	private static String getDownloadRootPath(Context context) {
		String rootPath = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			rootPath = getDownloadRootPathWithSdcard(context);
		} else {
			rootPath = getDownloadRootPathWithoutSdcard(context);
		}
		return rootPath;
	}

	/**
	 * use key as file name
	 * 
	 * @param context
	 * @param key
	 * @return
	 * @author: herry
	 * @date: 2014年8月29日 上午11:29:56
	 */

	public static File createDownloadTempFile(Context context, String key) {
		String rootPath = getDownloadRootPath(context);
		File newFile = new File(rootPath, key + TEMP_FILE_SURFIX);
		if (!newFile.getParentFile().exists()) {
			newFile.getParentFile().mkdirs();
		}
		return newFile;
	}

	public static File createDownloadFormalFile(Context context, String key) {
		String rootPath = getDownloadRootPath(context);
		File newFile = new File(rootPath, key + FORMAL_FILE_SURFIX);
		if (!newFile.getParentFile().exists()) {
			newFile.getParentFile().mkdirs();
		}
		return newFile;
	}

	public static void renameFile(File tempFile, File newFile) {
		tempFile.renameTo(newFile);
	}

	public static File getDownloadFile(Context context, String key) {
		String rootPath = getDownloadRootPath(context);
		File rootDir = new File(rootPath);
		File[] files = rootDir.listFiles(new FileSearchComp(key));
		if (files == null || files.length <= 0) {
			return null;
		}
		return files[0];
	}

	public static long getDownloadRange(Context context, String key) {
		File file = getDownloadFile(context, key);
		if (file == null) {
			return 0;
		}
		return file.length();
	}

	private static final class FileSearchComp implements FilenameFilter {
		private String key;

		public FileSearchComp(String key) {
			this.key = key;
		}

		@Override
		public boolean accept(File dir, String filename) {
			if (filename.startsWith(key)) {
				return true;
			}
			return false;
		}
	}
}
