package com.solibri.smc.api.view.examples;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solibri.smc.api.ifc.IfcEntityType;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.ui.View;

public final class IfcView implements View {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getUniqueId() {
		return "IFC-data-viewer";
	}

	private BrowserPanel browserPanel = BrowserPanel.getBrowserPanel("https://technical.buildingsmart.org/");
	private JPanel root;

	@Override
	public void initializePanel(JPanel panel) {
		this.root = panel;
		JLabel label = new JLabel("Example label");
		panel.add(label);
		browserPanel.setPreferredSize(new Dimension(0, 0));
		browserPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0xD8D8D8)));
		panel.add(browserPanel);
	}

	Map<IfcEntityType, String> map = new EnumMap<IfcEntityType, String>(IfcEntityType.class);

	@Override
	public void onComponentChosen(Component component) {
		Optional<IfcEntityType> optionalType = component.getIfcEntityType();
		if (!optionalType.isPresent()) {
			LOG.info("No Ifc type found from component {}.", component.getName());
			return;
		}
		IfcEntityType type = optionalType.get();
		if (!map.containsKey(type)) {
			try {
				map.put(type, Googler.google(type.toString()));
			} catch (IOException | GeneralSecurityException e) {
				LOG.error("Google search with term {} failed.", type.toString(), e);
			}
		}
		root.remove(browserPanel);
		browserPanel = BrowserPanel.getBrowserPanel(map.get(type));
		root.add(browserPanel);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				browserPanel.revalidate();
				browserPanel.repaint();
				root.revalidate();
				root.repaint();
			}
		});
	}

}
