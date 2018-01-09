package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileToBeProcessed {
    private File file;
    private List<String[]> data;
    private Map<String, Integer> header = new HashMap<String, Integer>();

    FileToBeProcessed(){

    }

    public FileToBeProcessed(File file, List<String[]> dataRows) {
        this.file = file;
        this.data = dataRows;
        setHeaders();
    }

    private void setHeaders(){
        String[] rawHeader = data.get(0);
        for(int i = 0; i < rawHeader.length; i++){
            header.put(rawHeader[i], i);
        }
    }

    public Map<String, Integer> getHeader(){
        return this.header;
    }

    public List<String[]> getAllRows(){
        return this.data;
    }
}
