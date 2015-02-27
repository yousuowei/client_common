package com.mipt.clientcommon.download;

import java.io.File;

public interface DownloadCallback {
	public void onDownloadPreStart(String downloadUrl);

	public void onDownloadStart(String downloadUrl);

	public void onDownloadProgress(String downloadUrl, int downloaded, int total);

	public void onDownloadSuccess(String downloadUrl, File apkFile);

	public void onDownloadFail(String downloadUrl);

	public void onDownloadPause(String downloadUrl);

	public void onDownloadStop(String downloadUrl);

	public void onDownloadDelete(String downloadUrl);

	public static class SimpleCallback implements DownloadCallback {
		@Override
		public void onDownloadPreStart(String downloadUrl) {
		}

		@Override
		public void onDownloadStart(String downloadUrl) {
		}

		@Override
		public void onDownloadProgress(String downloadUrl, int downloaded,
				int total) {
		}

		@Override
		public void onDownloadSuccess(String downloadUrl, File apkFile) {
		}

		@Override
		public void onDownloadFail(String downloadUrl) {

		}

		@Override
		public void onDownloadPause(String downloadUrl) {
		}

		@Override
		public void onDownloadStop(String downloadUrl) {
		}

		@Override
		public void onDownloadDelete(String downloadUrl) {
		}
	}
}
