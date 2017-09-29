package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class TSVColumn {
	private String title;
	private List<Flag> flags;
	
	public TSVColumn(){
		
	}
	
	public TSVColumn(String title, Enum<EnumContentDirectionTSVColumn> contentDirection, boolean metadata, List<Flag> flags){
		this.title = title;
		this.contentDirection = contentDirection;
		this.metadata = metadata;
		this.flags = flags;
	}
}
