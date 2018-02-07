package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagSearchBlock extends Flag {
    private Integer id;

    public FlagSearchBlock(Integer id) {
        this.id = id;
    }

    public Integer getId(){
        return this.id;
    }
}
