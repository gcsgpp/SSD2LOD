package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class NotMetadata extends Flag {
	private boolean notmetadata;
	public NotMetadata(Boolean notmetadata){
		this.notmetadata = notmetadata;
	}
	
	public boolean isMetadata(){
		return notmetadata;
	}
}
