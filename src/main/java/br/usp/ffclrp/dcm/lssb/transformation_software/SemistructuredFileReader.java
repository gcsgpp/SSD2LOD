package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.ColumnNotFoundWarning;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Condition;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagCol;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;
import com.univocity.parsers.common.AbstractParser;
import com.univocity.parsers.common.CommonParserSettings;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SemistructuredFileReader {
	static final int maxCharPerColumn = 999999;
	static final int maxColumns = 999999;

	//List<String[]> allRows = null;
	//Map<String, Integer> header = new HashMap<String, Integer>();
	Map<String, FileToBeProcessed> filesToBeProcessed = new LinkedHashMap<>();
	
	public SemistructuredFileReader(){
		
	}

	public void addFilesToBeProcessed(String relativePathDataFile) throws IOException {
		File file = new File(relativePathDataFile);
		List<String[]> dataRows = getAllFileData(file);

		if(dataRows != null)
			filesToBeProcessed.put(file.getName(), new FileToBeProcessed(file, dataRows));
	}

	public List<String[]> getAllFileData(File file) throws IOException {
		List<String[]> rows = null;
		if(file.getName().equals("format.txt")) return rows;


		String formatFile = Utils.readFile(file.getParent() + "/format.txt");

//		String name = file.getName();
//		String extension = name.substring(name.lastIndexOf("."));

		AbstractParser parser = null;
		if(formatFile.toLowerCase().equals("csv")) {
			CsvParserSettings settings = new CsvParserSettings();

			settings.getFormat().setLineSeparator("\r\n");
			settings.setMaxCharsPerColumn(maxCharPerColumn);
			settings.setMaxColumns(maxColumns);

			parser = new CsvParser(settings);
		}else{
			TsvParserSettings settings = new TsvParserSettings();

			settings.getFormat().setLineSeparator("\r\n");
			settings.setMaxCharsPerColumn(maxCharPerColumn);
			settings.setMaxColumns(maxColumns);

			parser = new TsvParser(settings);
		}


		try(Reader fileReader = getReader(file)) {
			rows = parser.parseAll(fileReader);
		}catch (IOException e) {
			throw e;
		}

		return rows; // parses all rows in one go.
	}

	public String getData(TSVColumn tsvColumn, Integer lineNumber) throws ColumnNotFoundWarning {
		String[] dataRow;

		Boolean columnFoundInAnyFile = false;
		Integer position = null;
		FlagCol flagCol = FlagCol.getColFlag(tsvColumn.getFlags());

		for(FileToBeProcessed file : filesToBeProcessed.values()) {
			try {
				if(flagCol == null) {
					position = file.getHeader().get(tsvColumn.getTitle());

					if(position != null)
						columnFoundInAnyFile = true;
				}else{
					if(file.getFilename().toUpperCase().equals(flagCol.getFilename().toUpperCase())) {
						position = flagCol.getColPosition();
						if(position == null)
							position = file.getHeader().get(flagCol.getColName());

						if(position >= file.getHeader().size())
							continue;
						else
							columnFoundInAnyFile = true;
					}else{
						continue;
					}
				}

				if(position == null)
					continue;

				//The column belongs to this file but there is no row anymore
				if((lineNumber >= file.getAllRows().size()))
					continue;

				dataRow = file.getAllRows().get(lineNumber);

				String str = dataRow[position];
				if (str.startsWith("\"") && str.endsWith("\""))
					str = str.substring(1, str.length() - 1);
				return str;
			} catch (NullPointerException ex) {
				continue;
			} catch (IndexOutOfBoundsException ex2) {
				continue;
			}
		}

		if(!columnFoundInAnyFile) {
			if (flagCol == null) {
				throw new ColumnNotFoundWarning("Column required not found. Column tried to access: " + tsvColumn.getTitle() + ". This column may not exist.");
			} else {
				throw new ColumnNotFoundWarning("Column required not found. Column tried to access: " + flagCol.getColPosition() + " from file: " + flagCol.getFilename() + ". This column/file may not exist.");
			}
		}else{
			return null;
		}
	}
	
	public String getData(Condition condition, Integer lineNumber) throws ColumnNotFoundWarning {
		return getData(condition.getColumn(), lineNumber);
	}
	
	public Integer getLinesCount(){
		int maxLines = Integer.MIN_VALUE;
		for(FileToBeProcessed file : filesToBeProcessed.values()) {
			if(file.getAllRows().size() > maxLines)
				maxLines = file.getAllRows().size();
		}

		return maxLines;
	}
	
	public Integer getColumnsCount(Integer lineNumber){

		int maxColumns = Integer.MIN_VALUE;
		for(FileToBeProcessed file : filesToBeProcessed.values()) {
			if(file.getAllRows().get(lineNumber).length > maxColumns)
				maxColumns = file.getAllRows().get(lineNumber).length;
		}

		return maxColumns;
	}

	private Reader getReader(File file) {
		try{
			return new InputStreamReader(new FileInputStream(file), "UTF-8");
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public int getFilesAdded() {
		return filesToBeProcessed.size();
	}
}
