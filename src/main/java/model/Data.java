package model;

import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("translations")
	private Translations translations;

	public Translations getTranslations(){
		return translations;
	}
}