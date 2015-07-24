package com.mail.bsecure.parsers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.Item;

public class JSONParser {

	private IItemHandler handler;

	private int REQ_TYPE;

	private Item item = new Item("");

	public JSONParser(IItemHandler aHandler, int requestId) throws Exception {
		handler = aHandler;
		REQ_TYPE = requestId;
	}

	public void parseXmlData(InputStream inputStream) throws Exception {
		try {

			byte[] bytebuf = new byte[0x1000];

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (;;) {
				int len = inputStream.read(bytebuf);
				if (len < 0)
					break;
				baos.write(bytebuf, 0, len);
			}

			bytebuf = baos.toByteArray();

			String jsonStr = new String(bytebuf, "UTF-8");	
			
			JSONObject root = new JSONObject(jsonStr);

			Iterator<String> rootItr = root.keys();

			while (rootItr.hasNext()) {

				String inrKey = rootItr.next();

				Object object = root.get(inrKey);

				if (object instanceof String || object instanceof Integer) {

					item.setAttribute(inrKey, object.toString());

				} else if (object instanceof JSONArray) {

					item.setAttributeValue(inrKey,
							parseArray((JSONArray) object));

				} else if (object instanceof JSONObject) {

					item.setAttributeValue(inrKey,
							parseObject((JSONObject) object));

				}
			}

			handler.onFinish(item, REQ_TYPE);

		} catch (Exception e) {
			handler.onError("Parser Exception", REQ_TYPE);
			
			throw e;
		}
	}

	private Vector<Item> parseArray(JSONArray array) throws JSONException {

		Vector<Item> items = new Vector<Item>();

		for (int i = 0; i < array.length(); i++) {

			Item item = new Item("values");

			JSONObject obj = array.getJSONObject(i);

			Iterator<String> iterator = obj.keys();

			while (iterator.hasNext()) {

				String tempKey = iterator.next();

				Object tempValue = obj.get(tempKey);
				
				if (tempValue instanceof JSONArray) {

					item.setAttributeValue(tempKey,
							parseArray((JSONArray) tempValue));

				} 
				else {
					item.setAttribute(tempKey, tempValue + "");
				}

				
			}
			items.add(item);
		}

		return items;
	}

	private Item parseObject(JSONObject object) throws JSONException {

		Item item = new Item("");

		Iterator<String> iterator = object.keys();

		while (iterator.hasNext()) {

			String tempKey = iterator.next();

			Object tempValue = object.opt(tempKey);

			if (tempValue instanceof JSONObject) {

				item.setAttributeValue(tempKey,
						parseObject((JSONObject) tempValue));

			} else if (tempValue instanceof JSONArray) {

				item.setAttributeValue(tempKey,
						parseArray((JSONArray) tempValue));

			} else {
				
				item.setAttribute(tempKey, tempValue + "");
				
			}
		}
		return item;
	}

}