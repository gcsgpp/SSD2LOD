package br.usp.ffclrp.dcm.lssb.api_rest;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.DirectoryCreationFailedException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.ErrorFileException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.StatusFileException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.TransformationActivityNotFoundException;
import br.usp.ffclrp.dcm.lssb.transformation_manager.EnumActivityState;
import br.usp.ffclrp.dcm.lssb.transformation_manager.TransformationManagerImpl;
import br.usp.ffclrp.dcm.lssb.transformation_software.RuleInterpretor;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("")
public class TransformationManager {

    TransformationManagerImpl fileSystemManager = new TransformationManagerImpl();

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test(){
        return "Test ok";
    }

    @GET
    @Path("/new-transformation")
    public Response createNewTransformation(){
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("Transformation ID: " + new TransformationManagerImpl().newTransformation());
        }catch (DirectoryCreationFailedException e){
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        return Response.ok(buffer.toString()).build();
    }

    @POST
    @Path("/setdataset/{transformationId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setDataset(@FormDataParam("dataset") FormDataBodyPart dataset,
                               @FormDataParam("dataset") ContentDisposition datasetDisposition,
                               @PathParam("transformationId") String transformationId){
        try {
            if(!isRightExtension(datasetDisposition.getFileName(), "tsv"))
                return Response.status(Response.Status.BAD_REQUEST).entity("ERROR: Dataset must have file extension \".tsv\".").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        StringBuffer buffer = new StringBuffer();

        try {
            BodyPartEntity entity = (BodyPartEntity) dataset.getEntity();

            fileSystemManager.addDataSetToTransformation(transformationId, entity.getInputStream(), dataset.getContentDisposition().getFileName());
            buffer.append("Dataset uploaded: '" + dataset.getContentDisposition().getFileName() + "'");
        } catch (TransformationActivityNotFoundException | StatusFileException e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        return Response.ok(buffer.toString()).build();
    }

    @POST
    @Path("/setontology/{transformationId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setOntology(@FormDataParam("ontology") FormDataBodyPart ontology,
                               @FormDataParam("ontology") ContentDisposition ontologyDisposition,
                               @PathParam("transformationId") String transformationId){
        try {
            if(!isRightExtension(ontologyDisposition.getFileName(), "owl"))
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ERROR: Ontology must be an OWL file.").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        StringBuffer buffer = new StringBuffer();

        try {
            BodyPartEntity entity = (BodyPartEntity) ontology.getEntity();

            fileSystemManager.addOntologyToTransformation(transformationId, entity.getInputStream(), ontology.getContentDisposition().getFileName());
            buffer.append("Ontology uploaded: '" + ontology.getContentDisposition().getFileName() + "'");
        } catch (TransformationActivityNotFoundException | StatusFileException e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        return Response.ok(buffer.toString()).build();
    }

    @POST
    @Path("/setrules/{transformationId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setRules(@FormDataParam("rules") FormDataBodyPart rules,
                                @FormDataParam("rules") ContentDisposition rulesDisposition,
                                @PathParam("transformationId") String transformationId){

        try {
            if(!isRightExtension(rulesDisposition.getFileName(), "txt"))
                return Response.status(Response.Status.BAD_REQUEST).entity("ERROR: Rules files must be a txt file.").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        StringBuffer buffer = new StringBuffer();

        try {
            BodyPartEntity entity = (BodyPartEntity) rules.getEntity();

            fileSystemManager.addRulesToTransformation(transformationId, entity.getInputStream());
            buffer.append("Rules uploaded: '" + rulesDisposition.getFileName() + "'");
        } catch (TransformationActivityNotFoundException | StatusFileException e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        return Response.ok(buffer.toString()).build();
    }

    private Boolean isRightExtension(String filename, String expectedFileExtension) throws Exception {
        String[] broken = filename.split("\\.");
        try {
            if(broken[broken.length - 1].equals(expectedFileExtension))
                return true;
        }catch(ArrayIndexOutOfBoundsException e){
            throw new Exception("File must have a extension \"." + expectedFileExtension + "\"");
        }

        return false;
    }

    @GET
    @Path("/getStatus/{transformationId}")
    public Response getStatus(@PathParam("transformationId") String transformationId){
        StringBuffer buffer = new StringBuffer();

        try{
            buffer.append(fileSystemManager.getStatus(transformationId));
        }catch (Exception e){
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        return Response.ok(buffer.toString()).build();
    }

    @POST
    @Path("/new-transformation")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response createNewTransformation(@FormDataParam("datasets") List<FormDataBodyPart> datasetsList,
                                            @FormDataParam("ontologies") List<FormDataBodyPart> ontologiesList,
                                            @FormDataParam("rules") FormDataBodyPart rules){
        if(datasetsList == null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ERROR: No dataset file submitted in the webservice.").build();

        if(ontologiesList == null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ERROR: No ontology file submitted in the webservice.").build();

        if(rules == null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ERROR: No rule file submitted in the webservice.").build();


        StringBuffer buffer = new StringBuffer();

        try {
            String transformationId = fileSystemManager.newTransformation();
            buffer.append("Transformation ID: " + transformationId + "<br />");

            for (FormDataBodyPart part : datasetsList) {
                Response resp = setDataset(part, part.getContentDisposition(), transformationId);
                buffer.append(resp.getEntity().toString() + "<br />");
            }

            for (FormDataBodyPart part : ontologiesList) {
                Response resp = setOntology(part, part.getContentDisposition(), transformationId);
                buffer.append(resp.getEntity().toString() + "<br />");
            }

            Response resp = setRules(rules, rules.getContentDisposition(), transformationId);
            buffer.append(resp.getEntity().toString() + "<br />");

        } catch (DirectoryCreationFailedException e) {
            e.printStackTrace();
            buffer.append(e.getMessage());
        }

        return Response.ok(buffer.toString()).build();
    }

    @GET
    @Path("start-transformation/{transformationId}")
    public Response startTransformation(@PathParam("transformationId") String transformationId){

        try {
            if (fileSystemManager.isReady(transformationId))
                fileSystemManager.updateStatus(transformationId, EnumActivityState.RUNNING);
        }catch (StatusFileException e){
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }


        List<String> ontologiesList;
        List<String> datasetsList;
        String       rulesFilePath;

        StringBuffer buffer = new StringBuffer();
        try {
            ontologiesList =   fileSystemManager.getAllOntologies(transformationId);
            datasetsList   =   fileSystemManager.getAllDatasets(transformationId);
            rulesFilePath  =   fileSystemManager.getRulesFile(transformationId);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }



        RuleInterpretor ruleInterpretor = new RuleInterpretor();
        ruleInterpretor.setTransformationParameters(transformationId,
                                            ontologiesList,
                                            rulesFilePath,
                                            ontologiesList.get(0),
                                            datasetsList);

        Thread t = new Thread(ruleInterpretor);
        t.start();

        return Response.ok().entity("Transformation proccess started.").build();
    }

    @GET
    @Path("delete-transformation/{transformationId}")
    public Response delete(@PathParam("transformationId") String transformationId){
        try {
            fileSystemManager.deleteTransformation(transformationId);
            fileSystemManager.updateStatus(transformationId, EnumActivityState.REMOVED);
        } catch (TransformationActivityNotFoundException | StatusFileException e) {
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        return Response.ok("Transformation " + transformationId + " successfully deleted.").build();
    }


    @GET
    @Path("/getError/{transformationId}")
    public Response getError(@PathParam("transformationId") String transformationId){
        String error = "";
        try {
            error = fileSystemManager.getErrorFileContent(transformationId);
        } catch (ErrorFileException e) {
            e.printStackTrace();
            return Response.serverError().entity("ERROR: " + e.getMessage()).build();
        }

        return Response.ok(error).build();
    }

}

