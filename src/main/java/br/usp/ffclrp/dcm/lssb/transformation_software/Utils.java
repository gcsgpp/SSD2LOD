package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	static public Matcher matchRegexOnString(String regex, String content){
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(content);
		m.find();
		return m;
	}
	
	static public String[] splitByIndex(String content, int index){

		String[] splitContent = new String[2];
		splitContent[0] = content.substring(0, index);
		splitContent[1] = content.substring(index, content.length()); // index not included

		return splitContent;
	}
	
	static public String extractDataFromFirstQuotationMarkBlockInsideRegex(String content, String regex){
		String data = null;

		Matcher matcher = Utils.matchRegexOnString(regex, content);

		try{
			data = matcher.group(); 
			int firstQuotationMark = data.indexOf("\"");
			data = data.substring(firstQuotationMark +1, data.indexOf("\"", firstQuotationMark +1));
		}catch(IllegalStateException e){
			//used just to identify when the matcher did not find anything.
		}

		return data;
	}

	static public String removeRegexFromContent(String regex, String content){
		Matcher matcher = Utils.matchRegexOnString(regex, content);

		return matcher.replaceAll("").trim(); 
	}

	static public String 			removeDataFromFirstQuotationMarkBlockInsideRegex(String content, String regex){
		String data = null;

		Matcher matcher = Utils.matchRegexOnString(regex, content);

		try{
			data = matcher.group();
			int firstQuotationMark = data.indexOf("\"");
			String quotationBlock = data.substring(firstQuotationMark, data.indexOf("\"", firstQuotationMark +1) + 1);
			content = content.replace(quotationBlock, "");
		}catch(IllegalStateException e){
			//used just to identify when the matcher did not find anything.
		}

		return content;
	}
	
}
