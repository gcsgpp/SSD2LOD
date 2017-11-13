package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumContentDirectionTSVColumn  {
	
	RIGHT("RIGHT"), DOWN("DOWN");
	
	private final String direction;
	EnumContentDirectionTSVColumn(String directionP) {
		this.direction = directionP;
	}
}
