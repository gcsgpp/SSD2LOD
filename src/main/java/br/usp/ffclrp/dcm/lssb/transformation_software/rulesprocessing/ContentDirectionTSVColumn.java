package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum ContentDirectionTSVColumn {
	
	RIGHT("RIGHT"), DOWN("DOWN");
	
	private final String direction;
	ContentDirectionTSVColumn(String directionP) {
		this.direction = directionP;
	}
	
	
	public String getDirection() {
		return direction;
	}
	
	

}
