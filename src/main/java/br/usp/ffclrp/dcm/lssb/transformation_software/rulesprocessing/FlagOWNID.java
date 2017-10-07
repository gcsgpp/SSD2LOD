package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagOWNID extends Flag {
	private boolean ownId;
	public FlagOWNID(Boolean ownId){
		this.ownId = ownId;
	}
	
	public boolean isOwnId(){
		return ownId;
	}
}
