package services;

import daos.ReimbursementDao;
import dtos.ReimbursementDto;
import models.Reimbursement;
import models.User;

import java.sql.SQLException;

public class ReimbursementService {
    ReimbursementDao reimbursementDao;
    UserService userService;

    public ReimbursementService(ReimbursementDao reimbursementDao, UserService userService) {
        this.reimbursementDao = reimbursementDao;
        this.userService = userService;
    }

    public void updateReimbursement(ReimbursementDto reimbursementDto) {
        try {
            Reimbursement oldInfo = this.reimbursementDao.getReimbursementById(reimbursementDto.getReimbursementId());

            //Java doesn't have a technique like merging dicts, so we make due with the ternary operator...
            //we could build something for this into the model, a helper function that handles merging objects...
            Reimbursement reimbursementMerged = new Reimbursement();
            reimbursementMerged.setReimbursementId(reimbursementDto.getReimbursementId());
            reimbursementMerged.setAmount(reimbursementDto.getAmount() != null ? reimbursementDto.getAmount() : oldInfo.getAmount());
            reimbursementMerged.setDescription(reimbursementDto.getDescription() != null ? reimbursementDto.getDescription() : oldInfo.getDescription());
            reimbursementMerged.setType(reimbursementDto.getType() != null ? reimbursementDto.getType() : oldInfo.getType());
            reimbursementMerged.setStatus(reimbursementDto.getStatus() != null ? reimbursementDto.getStatus() : oldInfo.getStatus());
            reimbursementMerged.setAuthorId(reimbursementDto.getAuthor() != null ? this.userService.findUserByUsername(reimbursementDto.getAuthor()).getId() : oldInfo.getAuthorId());
            reimbursementMerged.setResolverId(reimbursementDto.getResolver() != null ? this.userService.findUserByUsername(reimbursementDto.getResolver()).getId() : oldInfo.getResolverId());

            this.reimbursementDao.updateReimbursement(reimbursementMerged);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
