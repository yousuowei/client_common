package com.mipt.clientcommon.filecache;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import com.mipt.clientcommon.download.DownloadUtils;

public class FileCacheUtils {
	private static final String TEMP_FILE_SUFFIX = "_temp";

	public static String getRootPath(Context context, String dirName) {
		String rootPath = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			rootPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ File.separator
					+ "Download"
					+ File.separator
					+ context.getPackageName()
					+ File.separator + dirName;
		} else {
			rootPath = context.getCacheDir().getAbsolutePath() + File.separator
					+ dirName;
		}
		return rootPath;
	}

	public static String getTempCacheFilePath(Context context, String dirName,
			String cacheFileName) {
		return getFilePath(context, dirName, cacheFileName, true);
	}

	public static String getCacheFilePath(Context context, String dirName,
			String cacheFileName) {
		return getFilePath(context, dirName, cacheFileName, false);
	}

	private static String getFilePath(Context context, String dirName,
			String cacheFileName, boolean temp) {
		String rootPath = getRootPath(context, dirName);
		// String[] fileInfo = new String[2];
		// splitFileInfo(cacheFileName, fileInfo);

		String key4File = DownloadUtils.genKeyForUrl(cacheFileName);
		if (temp) {
			key4File += TEMP_FILE_SUFFIX;
		}
		File cacheFile = new File(rootPath, key4File);
		File parrentFile = cacheFile.getParentFile();
		if (!parrentFile.exists()) {
			parrentFile.mkdirs();
		}
		return cacheFile.getAbsolutePath();
	}

	@Deprecated
	private static void splitFileInfo(String cacheFileName, String[] outFileInfo) {
		int indexOfDot = cacheFileName.lastIndexOf(".");
		if (indexOfDot > -1) {
			outFileInfo[0] = cacheFileName.substring(0, indexOfDot);
			outFileInfo[1] = cacheFileName.substring(indexOfDot + 1);
		} else {
			outFileInfo[0] = cacheFileName;
			outFileInfo[1] = "";
		}
	}

	public static boolean deleteCacheFile(Context context, String dirName,
			String cacheFileName) {
		String filePath = getCacheFilePath(context, dirName, cacheFileName);
		File file4Delete = new File(filePath);
		return file4Delete.delete();
	}

	public static boolean isFileCached(Context context, String dirName,
			String cacheFileName) {
		String cachePath = getCacheFilePath(context, dirName, cacheFileName);
		File cacheFile = new File(cachePath);
		if (cacheFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
}
.getParentFile();
		if (!parrentFile.exists()) {
			parrentFile.mkdirs();
		}
		return cacheFile.getAbsolutePath();
	}

	@Deprecated
	private static void splitFileInfo(String cacheFileName, String[] outFileInfo) {
		int indexOfDot = cacheFileName.lastIndexOf(".");
		if (indexOfDot > -1) {
			outFileInfo[0] = cacheFileName.substring(0, indexOfDot);
			outFileInfo[1] = cacheFileName.substring(indexOfDot + 1);
		} else {
			outFileInfo[0] = cacheFileName;
			outFileInfo[1] = "";
		}
	}

	pub