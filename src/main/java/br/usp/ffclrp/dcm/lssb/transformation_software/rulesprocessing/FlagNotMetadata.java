package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagNotMetadata extends Flag {
	private boolean notmetadata;
	public FlagNotMetadata(Boolean notmetadata){
		this.notmetadata = notmetadata;
	}
	
	public boolean isMetadata(){
		return notmetadata;
	}
}
