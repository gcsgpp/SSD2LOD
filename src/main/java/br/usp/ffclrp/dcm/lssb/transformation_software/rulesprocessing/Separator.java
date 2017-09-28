package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class Separator extends Flag {
	private String term;
	private List<Integer> columns;
	
	public Separator(String term, List<Integer> columns){
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
