package com.solibri.smc.api.view.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solibri.smc.api.ifc.IfcEntityType;
import com.solibri.smc.api.ifc.IfcSchema;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.ui.View;

public final class IfcView implements View {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getUniqueId() {
		return "IFC-data-viewer";
	}

	private BrowserPanel browserPanel = BrowserPanel.getBrowserPanel("https://standards.buildingsmart.org/");
	private JPanel root;
	private JComboBox<IfcSchema> comboBox;

	@Override
	public void initializePanel(JPanel panel) {
		this.root = panel;
		panel.setLayout(new BorderLayout(10, 10));
		comboBox = new JComboBox<>(IfcSchema.values());
		panel.add(comboBox, BorderLayout.NORTH);
		browserPanel.setPreferredSize(new Dimension(0, 0));
		browserPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0xD8D8D8)));
		panel.add(browserPanel, BorderLayout.CENTER);
	}

	@Override
	public void onComponentChosen(Component component) {
		Optional<IfcEntityType> optionalType = component.getIfcEntityType();
		if (optionalType.isEmpty()) {
			LOG.info("No Ifc type found from component {}.", component.getName());
			return;
		}
		IfcEntityType type = optionalType.get();
		IfcSchema schema = comboBox.getSelectedItem() == null ? IfcSchema.IFC2X3 : (IfcSchema) comboBox.getSelectedItem();

		// Convert type name to lowercase if schema is IFC2X3 or IFC4
		String typeName = (schema == IfcSchema.IFC2X3 || schema == IfcSchema.IFC4)
			? type.name().toLowerCase(Locale.getDefault())
			: type.name();

		String ifcUrl = getBaseUrl(schema) + typeName + ".htm";

		root.remove(browserPanel);
		browserPanel = BrowserPanel.getBrowserPanel(ifcUrl);
		root.add(browserPanel);

		SwingUtilities.invokeLater(() -> {
			browserPanel.revalidate();
			browserPanel.repaint();
			root.revalidate();
			root.repaint();
		});
	}

	private static String getBaseUrl(IfcSchema schema) {
		String baseUrl = "https://standards.buildingsmart.org/";
		switch (schema) {
		case IFC2X3:
			return baseUrl + "IFC/RELEASE/IFC2x3/TC1/HTML/ifcsharedbldgelements/lexical/";
		case IFC4:
			return baseUrl + "MVD/RELEASE/IFC4/ADD2_TC1/RV1_2/HTML/schema/ifcsharedbldgelements/lexical/";
		case IFC4X3:
			return "https://ifc43-docs.standards.buildingsmart.org/IFC/RELEASE/IFC4x3/HTML/lexical/";
		default:
			throw new IllegalArgumentException();
		}
	}

}
