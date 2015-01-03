package com.mipt.clientcommon;

public interface HttpCallback {
    /**
     * 
     * @param id
     *            the reuqest id
     * @param result
     */
    public void onRequestSuccess(int id, BaseResult result);

    public void onRequestFail(int id, String reason);

    public void onRequestCancel(int id);

    public static class SimpleCallback implements HttpCallback {

	@Override
	public void onRequestSuccess(int id, BaseResult result) {
	}

	@Override
	public void onRequestFail(int id, String reason) {
	}

	@Override
	public void onRequestCancel(int id) {
	}

    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                     