package com.solibri.smc.api.view.examples;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import com.solibri.smc.api.SMC;

public class Googler {

	/*
	 * Google the given string and returns the first link found.
	 */
	public static String google(String object) throws IOException, GeneralSecurityException {
		String searchQuery = "site:standards.buildingsmart.org " + object;
		String cx = "010785531079632372336:h8d5gppfmbn";

		// The API must be passed as a JVM parameter. In real code it would probably be fetched some other way.
		Customsearch cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
			.setApplicationName("MyApplication")
			.setGoogleClientRequestInitializer(
				new CustomsearchRequestInitializer(SMC.getSettings().getSetting(ApiKeySetting.class).getValue()))
			.build();

		// Set search parameter
		Customsearch.Cse.List list = cs.cse().list(searchQuery).setCx(cx);

		// Execute search
		Search result = list.execute();
		if (result.getItems() != null) {
			for (Result ri : result.getItems()) {
				return (ri.getLink());
			}
		}

		return "https://google.com";

	}
}
