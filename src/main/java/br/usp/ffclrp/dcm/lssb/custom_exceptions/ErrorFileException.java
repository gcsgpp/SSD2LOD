package br.usp.ffclrp.dcm.lssb.custom_exceptions;

import java.io.IOException;

public class ErrorFileException extends IOException {

    public ErrorFileException(String msg){
        super(msg);
    }
}
