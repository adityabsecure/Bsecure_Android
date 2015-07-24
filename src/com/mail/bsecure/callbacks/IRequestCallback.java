package com.mail.bsecure.callbacks;

import java.io.InputStream;

public interface IRequestCallback {

	public void onRequestComplete(InputStream inputStream, String mimeType);

	public void onRequestComplete(String data, String mimeType);

	public void onRequestFailed(String errorData);

	public void onRequestCancelled(String extraInfo);

	public void onRequestProgress(Long... values);

}
