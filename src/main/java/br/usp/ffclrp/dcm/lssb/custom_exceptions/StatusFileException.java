package br.usp.ffclrp.dcm.lssb.custom_exceptions;

import java.io.IOException;

public class StatusFileException extends IOException {

    public StatusFileException(String msg){
        super(msg);
    }
}
