package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagCustomID extends Flag {
	private String customid;
	
	public FlagCustomID(String customid){
		this.customid = customid;
	}
	
	public String getContent(){
		return customid;
	}
}
