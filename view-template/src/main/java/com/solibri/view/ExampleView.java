package com.solibri.view;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.solibri.smc.api.ui.View;

public final class ExampleView implements View {

	@Override
	public String getUniqueId() {
		return "Example";
	}

	@Override
	public String getName() {
		return "Example in some language";
	}


	@Override
	public void initializePanel(JPanel panel) {
		JLabel label = new JLabel("Example label");
		panel.add(label);
	}

}
