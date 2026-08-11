package models;

import dtos.ReimbursementDto;

public class Reimbursement {
    private Integer reimbursementId;
    private Double amount;
    private String description;
    private String type;
    private String status;
    private Integer authorId;
    private Integer resolverId;

    public Reimbursement(Integer reimbursementId, Double amount, String description, String type, String status, Integer authorId, Integer resolverId) {
        this.reimbursementId = reimbursementId;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.status = status;
        this.authorId = authorId;
        this.resolverId = resolverId;
    }

    public Reimbursement(ReimbursementDto reimbursementDto, User authorId, User resolverId) {
        this.reimbursementId = reimbursementDto.getReimbursementId();
        this.amount = reimbursementDto.getAmount();
        this.description = reimbursementDto.getDescription();
        this.type = reimbursementDto.getType();
        this.status = reimbursementDto.getStatus();
        this.authorId = authorId.getId();
        this.resolverId = resolverId.getId();
    }



    public Reimbursement() {
    }

    public Integer getReimbursementId() {
        return reimbursementId;
    }

    public void setReimbursementId(Integer reimbursementId) {
        this.reimbursementId = reimbursementId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getResolverId() {
        return resolverId;
    }

    public void setResolverId(Integer resolverId) {
        this.resolverId = resolverId;
    }

    @Override
    public String toString() {
        return "Reimbursement{" +
                "reimbursementId=" + reimbursementId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", authorId=" + authorId +
                ", resolverId=" + resolverId +
                '}';
    }
}
