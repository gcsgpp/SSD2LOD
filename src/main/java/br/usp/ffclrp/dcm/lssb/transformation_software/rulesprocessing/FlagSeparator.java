package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class FlagSeparator extends Flag {
	private String term;
	private List<Integer> columns;
	
	public FlagSeparator(String term, List<Integer> columns){
		this.term = term;
		this.columns = columns;
	}
	
	public String getTerm(){
		return term;
	}
	
	public List<Integer> getColumns(){
		return columns;
	}
}
