package com.solibri.smc.api.view.examples;

import java.util.Collection;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.solibri.smc.api.checking.Ruleset;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.Model;
import com.solibri.smc.api.ui.View;

/**
 * EventsView is a simple view that just lists all events notifications the view receives in a text area.
 *
 */
public class EventsView implements View {

	private final JTextArea textArea = new JTextArea();

	@Override
	public String getUniqueId() {
		return "ModelEventsView";
	}

	@Override
	public String getName() {
		return "ModelEventsView";
	}


	@Override
	public void initializePanel(JPanel panel) {
		panel.add(new JScrollPane(textArea));
	}

	private synchronized void addText(String text) {
		String newText = textArea.getText() + text + "\n";
		textArea.setText(newText);
	}

	@Override
	public void onModelClosed() {
		addText("onModelClosed");
	}

	@Override
	public void onModelLoaded(Model model) {
		addText("onModelLoaded: " + model.getUUID());
	}

	@Override
	public void onModelModified(Model model) {
		addText("onModelModified: " + model.getUUID());
	}

	@Override
	public boolean onApplicationExit() {
		addText("onApplicationExit");
		return true;
	}

	@Override
	public void onSubModelUpdated(Model updatedSubModel) {
		addText("onSubModelUpdated: " + updatedSubModel.getUUID());
	}

	@Override
	public void onSubModelAdded(Model addedSubModel) {
		addText("onSubModelAdded: " + addedSubModel.getUUID());
	}

	@Override
	public void onBasketSelectionChanged(Set<Component> oldSelection, Set<Component> newSelection) {
		addText("onBasketSelectionChanged: " + oldSelection + " -> " + newSelection);
	}

	@Override
	public void onCheckingStarted() {
		addText("onCheckingStarted");
	}

	@Override
	public void onCheckingEnded() {
		addText("onCheckingEnded");
	}

	@Override
	public void onComponentChosen(Component component) {
		addText("onComponentChosen: " + component.getGUID());
	}

	@Override
	public void onComponentsZoomedTo(Collection<Component> components) {
		addText("onComponentsZoomedTo: " + components);
	}

	@Override
	public void onComponentsHidden(Collection<Component> components) {
		addText("onComponentsHidden: " + components);
	}

	@Override
	public void onSettingsChanged() {
		addText("onSettingsChanged");
	}

	@Override
	public void onItoStarted() {
		addText("onItoStarted");
	}

	@Override
	public void onItoFinished() {
		addText("onItoFinished");
	}

	@Override
	public void onRulesetAdded(Ruleset ruleset) {
		addText("onRulesetAdded: " + ruleset.getName());
	}

	@Override
	public void onRulesetRemoved(Ruleset ruleset) {
		addText("onRulesetRemoved: " + ruleset.getName());
	}
}
