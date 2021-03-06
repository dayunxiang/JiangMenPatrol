package com.ecity.android.httpforandroid.httpext;

import org.json.JSONException;
import org.json.JSONObject;

public class Return_Message_Builder {
	public static Return_Message build(String jsonString)

	{
		try {
			JSONObject obj;
			obj = new JSONObject(jsonString);
			boolean isSuccess = obj.getBoolean("isSuccess");
			String msg = obj.getString("msg");

			return new Return_Message(isSuccess, msg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
}
