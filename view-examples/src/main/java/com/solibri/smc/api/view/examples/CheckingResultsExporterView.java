package com.solibri.smc.api.view.examples;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.ui.View;

public class CheckingResultsExporterView implements View {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getUniqueId() {
		return "Checking Exporter";
	}

	private JPanel rootPanel;
	private JCheckBox box;
	private JButton button;
	private MaximumExportedResultsSetting maximumExportedResultsSetting = SMC.getSettings().getSetting(MaximumExportedResultsSetting.class);
	private OverwriteOldExportsSetting overwriteOldExports = SMC.getSettings().getSetting(OverwriteOldExportsSetting.class);

	@Override
	public void initializePanel(JPanel panel) {
		this.rootPanel = panel;
		panel.setLayout(new GridLayout(1, 2));

		JButton newButton = new JButton("Export!");
		panel.add(newButton);
		this.button = newButton;

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportResults();
			}
		});

		JCheckBox newBox = new JCheckBox(new CheckboxAction("Export automatically", button));
		// Default to manual exports.
		newBox.setSelected(false);
		panel.add(newBox);
		this.box = newBox;

	}

	@Override
	public boolean onApplicationExit() {
		int chosenOption = JOptionPane.showConfirmDialog(rootPanel,
			"Do you really wish to close the software?", "Closing confirmation", JOptionPane.YES_NO_OPTION);
		return JOptionPane.YES_OPTION == chosenOption;
	}

	@Override
	public void onCheckingStarted() {
		LOG.info("onCheckingStarted");
		// Do not allow exports during checking.
		this.button.setEnabled(false);
	}

	@Override
	public void onCheckingEnded() {
		LOG.info("onCheckingEnded");
		// Allow manual exports if checking has ended and automatic exports have not been chosen.
		// But if automatic exports have been chosen, just execute the export.
		if (!this.box.isSelected()) {
			this.button.setEnabled(true);
		} else {
			exportResults();
		}
	}

	@Override
	public void onModelClosed() {
		// Do not allow manual exports anymore now that the model has been closed.
		this.button.setEnabled(false);
	}

	private static final int TIMEOUT = 30;

	/*
	 * Writes the checking results to an SQLite database file.
	 */
	private void exportResults() {
		Collection<Result> results = SMC.getChecking().getResults();
		LOG.info("Results size: {}", results.size());

		Connection connection = null;
		try {
			String currentUsersHomeDir = System.getProperty("user.home");
			connection = DriverManager.getConnection("jdbc:sqlite:" + currentUsersHomeDir + "/sample.db");
			try (Statement statement = connection.createStatement()) {
				statement.setQueryTimeout(TIMEOUT);

				if (overwriteOldExports.getValue()) {
					statement.executeUpdate("drop table if exists results");
					statement.executeUpdate("create table results (key string, name string, description string)");
				} else {
					statement.executeUpdate(
						"create table if not exists results (key string, name string, description string)");
				}
			}
			int exportedCount = 0;
			for (Result result : results) {
				if (exportedCount >= maximumExportedResultsSetting.getValue()) {
					break;
				}
				LOG.info("Inserting result: {}", result.getName());
				String insertString = "insert into results values(?, ?, ?)";
				try (PreparedStatement insert = connection.prepareStatement(insertString)) {
					insert.setString(1, result.getUniqueKey());
					insert.setString(2, result.getName());
					insert.setString(3, result.getDescription());
					insert.execute();
				}
				exportedCount++;
			}

			LOG.info("Checking results exported to " + currentUsersHomeDir + "/sample.db.");

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				// connection close failed.
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * This action makes sure no manual exports are allowed if automatic exports are chosen.
	 */
	static class CheckboxAction extends AbstractAction {
		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -8162219987101380626L;

		private final JButton button;

		CheckboxAction(String text, JButton button) {
			super(text);
			this.button = button;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox sourceBox = (JCheckBox) e.getSource();
			if (sourceBox.isSelected()) {
				this.button.setEnabled(false);
			} else {
				this.button.setEnabled(true);
			}
		}
	}
}
