package br.usp.ffclrp.dcm.lssb.transformation_software;

public class MatrixLineNumberTracking {

    private int lineNumber;
    private String ruleStartedNumbering;

    public MatrixLineNumberTracking(){
        this.setEmpty();
    }

    public MatrixLineNumberTracking(int lineNumber, String ruleStartedNumbering){
        this.lineNumber = lineNumber;
        this.ruleStartedNumbering = ruleStartedNumbering;
    }

    public void setEmpty(){
        this.lineNumber = Integer.MIN_VALUE;
        this.ruleStartedNumbering = null;
    }

    public Boolean isEmpty(){
        return this.lineNumber == Integer.MIN_VALUE && this.ruleStartedNumbering == null;
    }

    public int getLineNumber(){
        return this.lineNumber;
    }
}
