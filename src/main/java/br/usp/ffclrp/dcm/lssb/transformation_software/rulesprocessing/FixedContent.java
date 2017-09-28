package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FixedContent extends Flag{
	private String content;
	
	public FixedContent(String content){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}

}
