package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class TripleObjectBuilder {

	
	public static TripleObject createObjectToRule(){
		return new TripleObjectAsRule();
	}
	
	public static TripleObject createObjectAsColumns(){
		return new TripleObjectAsColumns();
	}
	
	public static TripleObject createObjectToRule(ObjectAsRule param){
		return new TripleObjectAsRule(param);
	}
	
	public static TripleObject createObjectAsColumns(TSVColumn param){
		return new TripleObjectAsColumns(param);
	}
	
	public static TripleObject createObjectAsColumns(List<TSVColumn> param){
		return new TripleObjectAsColumns(param);
	}
	
}
