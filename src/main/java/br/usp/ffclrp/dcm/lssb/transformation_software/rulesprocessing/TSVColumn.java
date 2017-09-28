package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class TSVColumn {
	private String title;
	private Enum<ContentDirectionTSVColumn> contentDirection;
	private boolean metadata;
	
	public TSVColumn(String title, Enum<ContentDirectionTSVColumn> contentDirection, boolean metadata){
		this.title = title;
		this.contentDirection = contentDirection;
		this.metadata = metadata;
	}
}
