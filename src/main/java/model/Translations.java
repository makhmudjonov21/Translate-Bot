package model;

import com.google.gson.annotations.SerializedName;

public class Translations{

	@SerializedName("translatedText")
	private String translatedText;

	public String getTranslatedText(){
		return translatedText;
	}
}