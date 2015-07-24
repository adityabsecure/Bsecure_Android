package com.mail.bsecure.imageloader;

import java.util.Hashtable;

public class ImageItem extends Hashtable {

	/**
	 * Item - Constructor
	 * 
	 * @param String
	 */
	public ImageItem(String name) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * setAttribute -
	 * 
	 * @param String
	 * @param String
	 */
	public void setAttribute(String name, String value) {
		this.put(name, value);
	}

	/**
	 * setAttributeValue -
	 * 
	 * @param String
	 * @param Object
	 */
	public void setAttributeValue(String name, Object value) {
		this.put(name, value);
	}

	/**
	 * getAttribValue -
	 * 
	 * @param String
	 * @return String
	 */
	public Object getAttribValue(String key) {
		return this.get(key) != null ? this.get(key) : "";
	}

	/**
	 * getAttribValue -
	 * 
	 * @param String
	 * @return String
	 */
	public String getAttribute(String key) {
		if (this.get(key) != null) {
			return this.get(key).toString();
		} else {
			return "";
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.imimobile.odp.common.Component#getId()
	 */
	public int getId() {
		return 0;
	}

	/**
	 * getAllAttributes -
	 * 
	 * @return Hashtable
	 */
	public Hashtable getAllAttributes() {
		return this;
	}

	/**
	 * setAllAttributes -
	 * 
	 * @param Hashtable
	 */
	/*
	 * public void setAllAttributes(Hashtable atts) { this = atts; }
	 */

	/**
	 * containKey -
	 * 
	 * @param String
	 * @return boolean
	 */
	public boolean containKey(String key) {
		return this.containsKey(key) ? true : false;
	}

	/**
	 * clear -
	 */
	public void clear() {
		this.clear();
	}

	public int size() {
		return this.size();
	}

}
