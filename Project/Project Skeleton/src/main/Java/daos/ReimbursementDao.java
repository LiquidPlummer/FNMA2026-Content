package daos;

import models.Reimbursement;
import utils.ConnectionManager;

import java.sql.*;
import java.util.*;

/*
*     private Integer reimbursementId;
    private Double amount;
    private String description;
    private String type;
    private String status;
    private Integer author;
    private Integer resolver;
* */
public class ReimbursementDao {
    private static final Set<String> ALLOWED_COLUMNS = Set.of("reimbursement_id", "amount", "description", "type", "status", "author_id", "resolver_id");

    public ReimbursementDao() {

    }

    public Reimbursement getReimbursementById(Integer id) throws SQLException {
        String sql = "SELECT * FROM reimbursements WHERE reimbursement_id = ?";
        Reimbursement result = null;
        try (
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                result = new Reimbursement(
                        rs.getInt("reimbursement_id"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getInt("author_id"),
                        rs.getInt("resolver_id")
                );
            }
            rs.close();
        } catch (SQLException e) {
            throw e;
        }

        return result;
    }

    public void updateReimbursement(Reimbursement reimbursement) throws SQLException {
        String sql = "UPDATE reimbursements SET amount = ?, description = ?, type = ?, status = ?, author_id = ?, resolver_id = ? WHERE reimbursement_id = ?";
        try (
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setDouble(1, reimbursement.getAmount());
            pstmt.setString(2, reimbursement.getDescription());
            pstmt.setString(3, reimbursement.getType());
            pstmt.setString(4, reimbursement.getStatus());
            pstmt.setInt(5, reimbursement.getAuthorId());
            pstmt.setInt(6, reimbursement.getResolverId());
            pstmt.setInt(7, reimbursement.getReimbursementId());
            int rows = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    public List<Reimbursement> getReimbursementsWithFiltering(Map<String, String> filters) {
        List<Reimbursement> resultList = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM reimbursements");
        sb.append(filters.isEmpty() ? "" : " WHERE ");
        List<Object> values = new ArrayList<>();
        Iterator<String> iter = filters.keySet().iterator();
        while(iter.hasNext()) {
            String k = iter.next();
            if (!ALLOWED_COLUMNS.contains(k)) {
                throw new IllegalArgumentException("Invalid filter column: " + k);
            }
            sb.append(k);
            sb.append(" = ?");
            values.add(filters.get(k));
            if(iter.hasNext()) {
                sb.append(" AND ");
            }
        }

        try (
                Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sb.toString())
        ) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                resultList.add(new Reimbursement(
                        rs.getInt("reimbursement_id"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getInt("author_id"),
                        rs.getInt("resolver_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return resultList;
    }


}
