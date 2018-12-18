package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagSearchBlock extends Flag {
    private String id;
    private String variable;

    public FlagSearchBlock(String id, String variable) {
        this.id = id;
        this.variable = variable;
    }

    public String getId(){
        return this.id;
    }

    public String getVariable(){ return this.variable; }
}
