package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.ColumnNotFoundWarning;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.FileAccessException;
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class SemistructuredFileReader {
	
	
	//List<String[]> allRows = null;
	//Map<String, Integer> header = new HashMap<String, Integer>();
	Map<String, FileToBeProcessed> filesToBeProcessed = new LinkedHashMap<>();
	
	public SemistructuredFileReader(){
		
	}

	public void addFilesToBeProcessed(String relativePathDataFile) {
		File file = new File(relativePathDataFile);
		List<String[]> dataRows = getAllFileData(file);

		filesToBeProcessed.put(file.getName(), new FileToBeProcessed(file, dataRows));
	}

	public SemistructuredFileReader(String relativePathTsv){

	}
	
	public List<String[]> getAllFileData(File file) {

		Reader fileReader = getReader(file);
		TsvParserSettings settings = new TsvParserSettings();
		settings.getFormat().setLineSeparator("\r\n");
		settings.setMaxCharsPerColumn(999999);
		settings.setMaxColumns(99999);

		TsvParser parser = new TsvParser(settings);

		return parser.parseAll(fileReader); // parses all rows in one go.
	}
	
	public String getData(String tsvColumn, Integer lineNumber) throws ColumnNotFoundWarning {
		String[] dataRow;
		for(FileToBeProcessed file : filesToBeProcessed.values()) {
			try {
				dataRow = file.getAllRows().get(lineNumber);
				String str = dataRow[file.getHeader().get(tsvColumn)];
				if (str.startsWith("\"") && str.endsWith("\""))
					str = str.substring(1, str.length() - 1);
				return str;
			} catch (NullPointerException ex) {
				continue;
			} catch (IndexOutOfBoundsException ex2){
				continue;
			}
		}
		throw new ColumnNotFoundWarning("Not found the column required. Column tried to access: " + tsvColumn + ". This column may not exist.");
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
