package dtos;

public class ReimbursementDto {
    private Integer reimbursementId;
    private Double amount;
    private String description;
    private String type;
    private String status;
    private String author;//by username
    private String resolver;//by username

    //This DTO exists so the client can send over reimbursement JSON without the rest of the user objects
    //for author and resolver. In order to translate from the JSON into the Model we will need to
    //use the usernames given to get from the database those user objects.


    public ReimbursementDto(Integer reimbursementId, Double amount, String description, String type, String status, String author, String resolver) {
        this.reimbursementId = reimbursementId;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.status = status;
        this.author = author;
        this.resolver = resolver;
    }

    public ReimbursementDto() {
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getResolver() {
        return resolver;
    }

    public void setResolver(String resolver) {
        this.resolver = resolver;
    }
}
