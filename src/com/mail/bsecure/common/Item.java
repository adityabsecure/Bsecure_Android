package com.mail.bsecure.common;

import java.util.Hashtable;

public class Item extends Hashtable {

	/**
	 * Item - Constructor
	 * 
	 * @param String
	 */
	public Item(String name) {
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
		return this.get(key) != null ? this.get(key) : null;
	}

	/**
	 * getAttribValue -
	 * 
	 * @param String
	 * @return String
	 */
	public String getAttribute(String key) {
		return this.get(key) != null ? this.get(key).toString() : "";
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

}