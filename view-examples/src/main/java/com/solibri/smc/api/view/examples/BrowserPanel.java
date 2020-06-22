package com.solibri.smc.api.view.examples;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;

public final class BrowserPanel extends JFXPanel {

	private static final long serialVersionUID = 1L;


	public static BrowserPanel getBrowserPanel(String url) {
		final BrowserPanel panel = new BrowserPanel();
		Platform.runLater(() -> {
			Browser browser = new Browser(panel, url);
			Scene scene = new Scene(browser, 750, 500, Color.DARKGREY);
			panel.setScene(scene);
		});
		return panel;
	}

	private BrowserPanel() {
	}

	private static class Browser extends Region {
		private final WebView browser = new WebView();

		private final BrowserPanel browserPanel;

		Browser(BrowserPanel browserPanel, String urlToLoad) {
			this.browserPanel = browserPanel;

			// disable context menu
			browser.setContextMenuEnabled(false);

			// add the web view to the scene
			getChildren().add(browser);
			browser.getEngine().setUserAgent("Solibri Desktop Client");
			browser.getEngine().load(urlToLoad);
		}
	}
}
