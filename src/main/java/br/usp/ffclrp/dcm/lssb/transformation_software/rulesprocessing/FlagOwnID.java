package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagOwnID extends Flag {
	private boolean ownid;
	public FlagOwnID(Boolean ownid){
		this.ownid = ownid;
	}
	
	public boolean isOwnID(){
		return ownid;
	}
}
