package br.usp.ffclrp.dcm.lssb.api_rest;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.DirectoryCreationFailedException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.TransformationActivityNotFoundException;
import br.usp.ffclrp.dcm.lssb.transformation_manager.TransformationFileSystemManager;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("")
public class TransformationManager {
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
            buffer.append(new TransformationFileSystemManager().newTransformation());
        }catch (DirectoryCreationFailedException e){
            e.printStackTrace();
            buffer.append(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(buffer.toString()).build();
    }

    @POST
    @Path("/setdataset/{transformationId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setDataset(@FormDataParam("dataset") FormDataBodyPart dataset,
                               @PathParam("transformationId") String transformationId){
        StringBuffer buffer = new StringBuffer();

        TransformationFileSystemManager fileSystemManager = new TransformationFileSystemManager();

        try {
            BodyPartEntity entity = (BodyPartEntity) dataset.getEntity();
            fileSystemManager.addDataSetToTransformation(transformationId, entity.getInputStream(), dataset.getContentDisposition().getFileName());
            buffer.append("Dataset uploaded: '" + dataset.getContentDisposition().getFileName() + "'");
        } catch (TransformationActivityNotFoundException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(buffer.toString()).build();
    }

    @POST
    @Path("/setontology/{transformationId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response setOntology(@FormDataParam("ontology") FormDataBodyPart ontology,
                               @PathParam("transformationId") String transformationId){
        StringBuffer buffer = new StringBuffer();

        TransformationFileSystemManager fileSystemManager = new TransformationFileSystemManager();

        try {
            BodyPartEntity entity = (BodyPartEntity) ontology.getEntity();
            fileSystemManager.addOntologyToTransformation(transformationId, entity.getInputStream(), ontology.getContentDisposition().getFileName());
            buffer.append("Ontology uploaded: '" + ontology.getContentDisposition().getFileName() + "'");
        } catch (TransformationActivityNotFoundException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(buffer.toString()).build();
    }


    @POST
    @Path("/new-transformation")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response createNewTransformation(@FormDataParam("datasets") List<FormDataBodyPart> datasetsList,
                                            @FormDataParam("ontologies") List<FormDataBodyPart> ontologiesList){
        StringBuffer buffer = new StringBuffer();

        TransformationFileSystemManager fileSystemManager = new TransformationFileSystemManager();
        try {
            String transformationId = fileSystemManager.newTransformation();
            buffer.append("Transformation ID: " + transformationId + "<br />");

            for (FormDataBodyPart part : datasetsList) {
                Response resp = setDataset(part, transformationId);
                buffer.append(resp.getEntity().toString() + "<br />");
            }

            for (FormDataBodyPart part : ontologiesList) {
                Response resp = setOntology(part, transformationId);
                buffer.append(resp.getEntity().toString() + "<br />");
            }

        } catch (DirectoryCreationFailedException e) {
            e.printStackTrace();
            buffer.append(e.getMessage());
        }

        return Response.ok(buffer.toString()).build();
    }
}

