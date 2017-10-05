package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.ArrayList;
import java.util.List;

public class TripleObjectAsColumns implements TripleObject {
	
	private List<TSVColumn> columns = null;
	
	public TripleObjectAsColumns(){
		
	}
	
	public TripleObjectAsColumns(TSVColumn obj){
		this.columns = new ArrayList<TSVColumn>();
		this.columns.add(obj);
	}
	
	public TripleObjectAsColumns(List<TSVColumn> obj){
		this.columns = obj;
	}
	
	
	@Override
	public List<TSVColumn> getObject() {
		return this.columns;
	}

}
