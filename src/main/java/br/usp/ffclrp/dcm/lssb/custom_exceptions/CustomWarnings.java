package br.usp.ffclrp.dcm.lssb.custom_exceptions;

public abstract class CustomWarnings extends Exception {
    public CustomWarnings(String message){ super("Warning: " + message); }
}
