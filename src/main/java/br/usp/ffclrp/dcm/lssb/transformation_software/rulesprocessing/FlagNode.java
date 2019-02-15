package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;
import org.semanticweb.owlapi.model.OWLClass;

public class FlagNode extends Flag {

    private OWLClass nodeType;

    public FlagNode(OWLClass nodeType) {
        this.nodeType = nodeType;
    }

    public OWLClass getFlagNode(){
        return this.nodeType;
    }

}
