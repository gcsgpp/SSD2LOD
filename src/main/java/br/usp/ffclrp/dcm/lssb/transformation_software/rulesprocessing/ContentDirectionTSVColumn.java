package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class ContentDirectionTSVColumn extends Flag {
	private Enum<EnumContentDirectionTSVColumn> direction;

	public ContentDirectionTSVColumn(Enum<EnumContentDirectionTSVColumn> direction){
		this.direction = direction;
	}

	public Enum<EnumContentDirectionTSVColumn> getDirection() {
		return direction;
	}
	
	

}
