package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;

import java.util.List;

public class FlagCol extends Flag {
    String filename = null;
    Integer colPosition = null;
    public FlagCol(String filename, Integer colPosition){
        this.filename = filename;
        this.colPosition = colPosition - 1;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getColPosition() {
        return colPosition;
    }

    public static FlagCol getColFlag(List<Flag> tsvFlags){
        for(Flag flag : tsvFlags){
            if(flag instanceof FlagCol){
                return (FlagCol) flag;
            }
        }
        return null;
    }

    public static FlagCol extractFlagColFromSentence(String sentence){
        String  flagString = null;
        String  filenameExtracted = null;
        Integer colExtracted = null;

        try {
            flagString = Utils.matchRegexOnString(EnumRegexList.SELECTCOLFLAG.get(), sentence).group();

            filenameExtracted = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(flagString, EnumRegexList.SELECTCOLFLAG.get());
            flagString = Utils.removeDataFromFirstQuotationMarkBlockInsideRegex(flagString, EnumRegexList.SELECTCOLFLAG.get());
            colExtracted = Integer.parseInt(Utils.matchRegexOnString("\\d+", flagString).group());
        }catch (Exception e){
            throw e;
        }

        return new FlagCol(filenameExtracted, colExtracted);

    }
}
