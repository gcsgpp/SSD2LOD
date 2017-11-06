package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagFixedContent extends Flag{
	private String content;
	
	public FlagFixedContent(String content){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}

}
