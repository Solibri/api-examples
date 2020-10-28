package com.solibri.smc.api.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;

/**
 * This example custom Information checks if the GUID of the given {@link Component} is found from an Excel sheet
 * defined in settings.
 */
public class FoundFromExcel implements Information<Boolean> {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getUniqueId() {
		return "Found from the ID Excel";
	}

	@Override
	public Optional<Boolean> getInformation(Component component) {
		String id = component.getGUID();

		File file = new File(SMC.getSettings().getSetting(ExcelFileLocationSetting.class).getValue());
		if (!file.exists()) {
			return Optional.of(Boolean.FALSE);
		}

		try (FileInputStream fis = new FileInputStream(file)) {

			// Finds the workbook instance for XLSX file
			XSSFWorkbook workBook = new XSSFWorkbook(fis);

			// Return first sheet from the XLSX workbook
			XSSFSheet sheet = workBook.getSheetAt(0);

			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = sheet.iterator();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				Cell cell = row.getCell(0);

				switch (cell.getCellType()) {
				case STRING:
					if (id.equals(cell.getStringCellValue())) {
						return Optional.of(Boolean.TRUE);
					}
					break;
				case NUMERIC:
					LOG.warn("The cell value was numeric: {}", cell.getNumericCellValue());
					continue;
				case BOOLEAN:
					LOG.warn("The cell value was boolean: {}", cell.getNumericCellValue());
					continue;
				default:

				}
			}
		} catch (FileNotFoundException e) {
			LOG.error("Unable to open file {}", file, e);
		} catch (IOException e) {
			LOG.error("Unable to open file {}", file, e);
		}

		return Optional.of(Boolean.FALSE);
	}

	@Override
	public PropertyType getType() {
		return PropertyType.BOOLEAN;
	}

}
