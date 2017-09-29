package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class ContentDirectionTSVColumn extends Flag {
	private Enum<EnumContentDirectionTSVColumn> direction;
	public ContentDirectionTSVColumn() {
	}
	
	public ContentDirectionTSVColumn(Enum<EnumContentDirectionTSVColumn> direction){
		this.direction = direction;
	}

}
