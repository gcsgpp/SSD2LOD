package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

public class FlagCol extends Flag {
    String filename = null;
    Integer colPosition = null;
    String colName = null;
    public FlagCol(String filename, Integer colPosition, String colName){
        this.filename = filename;
        if(colPosition != null)
            this.colPosition = colPosition - 1;
        this.colName = colName;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getColPosition() {
        return colPosition;
    }

    public String getColName() { return colName; }

    public static FlagCol getColFlag(List<Flag> tsvFlags){
        for(Flag flag : tsvFlags){
            if(flag instanceof FlagCol){
                return (FlagCol) flag;
            }
        }
        return null;
    }

    public static FlagCol extractFlagColFromSentence(String sentence) throws Exception {
        String  flagString = null;
        String  filenameExtracted = null;
        Integer colNumber = null;
        String  colName = null;

        try {
            flagString = Utils.matchRegexOnString(EnumRegexList.SELECTCOLFLAG.get(), sentence).group();


            int numberOfQuotationMarks = 0;
            Matcher matcher = Utils.matchRegexOnString("\".*?\"", flagString);
            while(!matcher.hitEnd()) {
                numberOfQuotationMarks++;
                matcher.find();
            }


            if( numberOfQuotationMarks == 1){
                filenameExtracted = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(flagString, EnumRegexList.SELECTCOLFLAG.get());
                flagString = Utils.removeDataFromFirstQuotationMarkBlockInsideRegex(flagString, EnumRegexList.SELECTCOLFLAG.get());
                colNumber = Integer.parseInt(Utils.matchRegexOnString("\\d+", flagString).group());
            }else if(numberOfQuotationMarks == 2) {
                colName = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(flagString, EnumRegexList.SELECTCOLFLAG.get());
                flagString = Utils.removeDataFromFirstQuotationMarkBlockInsideRegex(flagString, EnumRegexList.SELECTCOLFLAG.get());
                filenameExtracted = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(flagString, EnumRegexList.SELECTCOLFLAG.get());
            }else
                throw new Exception("Col flag is not properly configured. Sentence: " + flagString);
        }catch (Exception e){
            throw e;
        }

        return new FlagCol(filenameExtracted, colNumber, colName);

    }
}
