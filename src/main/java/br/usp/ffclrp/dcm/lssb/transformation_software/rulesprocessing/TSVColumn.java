package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class TSVColumn {

	private String title;
	private List<Flag> flags;
	
	public TSVColumn(){
		
	}
	
	public TSVColumn(String title, List<Flag> flags){
		this.title = title;
		this.flags = flags;
	}
	
	public String getTitle() {
		return title;
	}
	
	public List<Flag> getFlags(){
		return flags;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setFlags(List<Flag> flags) {
		this.flags = flags;
	}
}