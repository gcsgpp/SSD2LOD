package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagSearchBlock extends Flag {
    private String id;

    public FlagSearchBlock(String id) {
        this.id = id;
    }

    public String getId(){
        return this.id;
    }
}
