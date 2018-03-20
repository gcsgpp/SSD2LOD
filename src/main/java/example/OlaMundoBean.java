package example;

import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.FacesConfig;
import javax.inject.Named;

@FacesConfig
@Named
@RequestScoped
public class OlaMundoBean {

    private String name;
    private String message;

    public void createMessage() {
        message = "Hello, " + name + "!";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

}

/*
@ManagedBean
public class OlaMundoBean {

    public String getHorario(){
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        return "Atualizado em " + sdf.format(new Date());
    }
}*/
