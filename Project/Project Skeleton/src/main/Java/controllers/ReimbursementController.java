package controllers;

import dtos.ReimbursementDto;
import io.javalin.http.Context;
import models.Reimbursement;
import services.ReimbursementService;


public class ReimbursementController {
    ReimbursementService reimbursementService;

    public ReimbursementController(ReimbursementService reimbursementService) {
        this.reimbursementService = reimbursementService;
    }

    public void postNewReimbursement(Context ctx) {
        throw new RuntimeException("This method is not yet implemented.");
    }

    public void getReimbursementById(Context ctx) {
        throw new RuntimeException("This method is not yet implemented.");
    }

    public void getReimbursementWithFilters(Context ctx) {
        throw new RuntimeException("This method is not yet implemented.");
    }

    public void updateReimbursement(Context ctx) {
        //The API endpoint pattern in Javalin:
        //unwrap the request - get the parameters and body from the request
        //prep the work to be done - turn any params from req into params for service layer
        ReimbursementDto reimbursementDto = ctx.bodyAsClass(ReimbursementDto.class);
        reimbursementDto.setReimbursementId(Integer.parseInt(ctx.pathParam("id")));
        //call the service layer - invoke a SL method to start the workflow
        this.reimbursementService.updateReimbursement(reimbursementDto);
        //return with the work done - we come back from the SL with results
        //prep the response - fill the response box and javalin will send it
        ctx.status(200);
    }

    public void deleteReimbursement(Context ctx) {
        throw new RuntimeException("This method is not yet implemented.");
    }
}
