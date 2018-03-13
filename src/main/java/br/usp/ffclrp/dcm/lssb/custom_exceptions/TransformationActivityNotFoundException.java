package br.usp.ffclrp.dcm.lssb.custom_exceptions;

public class TransformationActivityNotFoundException extends Exception{

    public TransformationActivityNotFoundException(String id){
        super("Transformation ID: " + id + " not found.");
    }
}
