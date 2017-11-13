package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagContentDirectionTSVColumn extends Flag {
	private Enum<EnumContentDirectionTSVColumn> direction;

	public FlagContentDirectionTSVColumn(Enum<EnumContentDirectionTSVColumn> direction){
		this.direction = direction;
	}

	public Enum<EnumContentDirectionTSVColumn> getDirection() {
		return direction;
	}
	
	

}
