package com.mail.bsecure.callbacks;

public interface SocialCallbacks {

	public void onSocialSuccess(Object results, int requestId);

	public void onSocialFailed(Object results, int requestId);

	public void onSocialCancel(Object results, int requestId);

	public void onSocialDetails(Object results, int requestId);

	public void onSocialLogOut(Object results, int requestId);

}