package model;

import com.google.gson.annotations.SerializedName;

public class Translate{

	@SerializedName("data")
	private Data data;

	public Data getData(){
		return data;
	}
}