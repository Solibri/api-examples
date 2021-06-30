package com.solibri.smc.api.view.examples.spaceinformation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.ifc.IfcEntityType;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.components.Space;
import com.solibri.smc.api.model.components.SpaceBoundary;
import com.solibri.smc.api.ui.View;

/**
 * SpaceInformationView shows information about space boundaries of spaces in the selection basket.
 *
 * It uses JTable to represent information in a table format and allows exporting the data in the table into Excel
 * files.
 */
public final class SpaceInformationView implements View {

	private final static Logger LOG = LoggerFactory.getLogger(SpaceInformationView.class);

	@Override
	public String getUniqueId() {
		return "Space information";
	}

	@Override
	public String getName() {
		return "Space information";
	}

	private Vector<Vector<String>> data = new Vector<>();
	private Vector<String> column = new Vector<>(List.of("Name of Space", "Component Type", "Type", "Total Area"));
	private JTable table = createTable();
	private JButton button;

	@Override
	public void initializePanel(JPanel panel) {
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel label = new JLabel("Space information");
		panel.add(label, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipady = 20;
		gbc.gridx = 0;
		gbc.gridy = 1;

		panel.add(createPane(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		JButton newButton = new JButton("Export to Excel!");
		panel.add(newButton, gbc);

		getSelectedSpaces();

		this.button = newButton;

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					LOG.info("Starting Excel export.");
					exportData();
					LOG.info("Excel export done.");
				} catch (IOException e1) {
					LOG.error("Excel export failed.", e1);
				}
			}

		});

		table.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = table.rowAtPoint(evt.getPoint());
				int col = table.columnAtPoint(evt.getPoint());
				if (row >= 0 && col >= 0) {
					if (evt.getClickCount() > 1) {
						zoomIntoComponents(row, col);
					}
				}
			}
		});

		updateData();
	}

	/*
	 * We need to put the table inside a JScrollPane.
	 * Otherwise the headers are not visible.
	 *
	 * This also adds a scrollbar when needed.
	 */
	private JScrollPane createPane() {
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		return scrollPane;
	}

	private JTable createTable() {
		JTable table = new JTable(new MyTableModel());
		table.setBorder(null);
		table.setOpaque(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.setGridColor(Color.lightGray);
		table.setRowMargin(0);
		table.setRowHeight(22);
		table.setShowHorizontalLines(false);
		table.getTableHeader().setVisible(true);

		return table;
	}

	public class MyTableModel extends AbstractTableModel {

		MyTableModel() {

		}

		@Override
		/*
		 * It is important to disable cell editing. The data is not editable and clicking should trigger the 3D zoom
		 * instead.
		 */
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}
		@Override
		public String getColumnName(int col) {
		    return column.get(col);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data.get(rowIndex).get(columnIndex);
		}

	}

	private void exportData() throws FileNotFoundException, IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook();) {
			XSSFSheet sheet = workbook.createSheet("Space Data");

			int rowCount = 0;

			for (Vector<String> dataRow : data) {
				XSSFRow row = sheet.createRow(++rowCount);

				int columnCount = 0;

				for (String field : dataRow) {
					XSSFCell cell = row.createCell(++columnCount);
					cell.setCellValue(field);
				}

			}

			try (FileOutputStream outputStream = new FileOutputStream(
				SMC.getSettings().getSetting(ExportToExcelFileLocationSetting.class).getValue())) {
				workbook.write(outputStream);
			}
		}
	}

	@Override
	public void onBasketSelectionChanged(Set<Component> oldComponents, Set<Component> newComponents) {
		updateData();
		table.setBounds(30, 40, 200, 300);
		table.repaint();
	}

	Map<Integer, List<Component>> rowToComponentsMap = new HashMap<>();

	private void zoomIntoComponents(int row, int col) {
		List<Component> components = rowToComponentsMap.get(row);
		LOG.debug("Zooming to:");
		for (Component c : components) {
			LOG.debug(c.getName());
		}
		SMC.get3D().getCamera().lookAt(rowToComponentsMap.get(row));
	}

	private void updateData() {
		data.clear();
		for (Space space : getSelectedSpaces()) {
			addRows(space, IfcEntityType.IfcDoor, "Door");
			addRows(space, IfcEntityType.IfcWindow, "Window");
			addRows(space, IfcEntityType.IfcWall, "Wall");
			addRows(space, IfcEntityType.IfcWallStandardCase, "Wall");
			addRows(space, IfcEntityType.IfcSlab, "Slab");
		}
	}

	private void addRows(Space space, IfcEntityType ifcType, String componentClassName) {
		Map<String, List<SpaceBoundary>> typeBoundaryMap = new HashMap<>();
		for (SpaceBoundary sb : space.getSpaceBoundaries()) {
			if (sb.getRelatedBuildingElement().isPresent()
					&& sb.getRelatedBuildingElement().get().getIfcEntityType().isPresent()
					&& sb.getRelatedBuildingElement().get().getIfcEntityType().get() == ifcType) {
				Component component = sb.getRelatedBuildingElement().get();
				String type = component.getConstructionType().orElse("");
				if (typeBoundaryMap.containsKey(type)) {
					typeBoundaryMap.get(type).add(sb);
				} else {
					typeBoundaryMap.put(type, new ArrayList<>());
					typeBoundaryMap.get(type).add(sb);
				}
			}
		}

		for (Entry<String, List<SpaceBoundary>> entry : typeBoundaryMap.entrySet()) {
			List<Component> components = new ArrayList<>();
			List<SpaceBoundary> sbs = entry.getValue();
			for (SpaceBoundary sb : sbs) {
				if (sb.getRelatedBuildingElement().isPresent()) {
					components.add(sb.getRelatedBuildingElement().get());
				}
			}

			double areaOfWindows = sbs.stream().map(SpaceBoundary::getArea).reduce(0.0, Double::sum);
			DecimalFormat formatter = new DecimalFormat("#,###.00");
			String formattedArea = formatter.format(areaOfWindows) + " m2";
			data.add(new Vector<>(
					List.of(space.getName(), componentClassName, entry.getKey(), String.valueOf(formattedArea))));
			rowToComponentsMap.put(data.size() - 1, components);
		}
	}

	private Set<Space> getSelectedSpaces() {
		return SMC.getSelectionBasket().get().stream().filter(component -> component instanceof Space)
				.map(Space.class::cast).collect(Collectors.toSet());
	}

}
