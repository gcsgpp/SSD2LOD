package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.FileAccessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class SemistructuredFileReader {
	
	
	List<String[]> allRows = null;
	Map<String, Integer> header = new HashMap<String, Integer>();
	
	public SemistructuredFileReader(){
		
	}
	
	public SemistructuredFileReader(String relativePathTsv){
		getAllFileData(relativePathTsv);
	}
	
	public List<String[]> getAllFileData(String relativePathTsv) {

		TsvParserSettings settings = new TsvParserSettings();
		settings.getFormat().setLineSeparator("\r\n");
		settings.setMaxCharsPerColumn(999999);
		settings.setMaxColumns(99999);

		TsvParser parser = new TsvParser(settings);

		allRows = parser.parseAll(getReader(relativePathTsv)); // parses all rows in one go.
		
		String[] rawHeader = allRows.get(0);
		for(int i = 0; i < rawHeader.length; i++){
			header.put(rawHeader[i], i);
		}

		return allRows;
	}
	
	public String getData(String tsvColumn, Integer lineNumber) throws FileAccessException{
		
		String[] dataRow = allRows.get(lineNumber);
		
		try{
			String str = dataRow[header.get(tsvColumn)];
			if(str.startsWith("\"") && str.endsWith("\""))
				str = str.substring(1, str.length() - 1);
			return str;
		}catch(NullPointerException ex){
			throw new FileAccessException("Not possible to access the file or the content in the file. Column tried to access: " + tsvColumn + ". This column may not exist or the file is not accessible.");
		}
	}
	
	public Integer getLinesCount(){
		return this.allRows.size();
	}
	
	public Integer getColumnsCount(Integer lineNumber){
		return this.allRows.get(lineNumber).length;
	}
	
	/*public List<String[]> getAllDataRows(){
		return this.allRows;
	}*/

	private Reader getReader(String relativePath) {
		try{
			return new InputStreamReader(new FileInputStream(new File(relativePath)), "UTF-8");
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
