package br.usp.ffclrp.dcm.lssb.transformation_software;

public class MatrixLineNumberTracking {

    private int lineNumber;
    private int ruleStartedNumbering;

    public MatrixLineNumberTracking(){
        this.setEmpty();
    }

    public MatrixLineNumberTracking(int lineNumber, int ruleStartedNumbering){
        this.lineNumber = lineNumber;
        this.ruleStartedNumbering = ruleStartedNumbering;
    }

    public void setEmpty(){
        this.lineNumber = Integer.MIN_VALUE;
        this.ruleStartedNumbering = Integer.MIN_VALUE;
    }

    public Boolean isEmpty(){
        return this.lineNumber == Integer.MIN_VALUE && this.ruleStartedNumbering == Integer.MIN_VALUE;
    }

    public int getLineNumber(){
        return this.lineNumber;
    }
}
