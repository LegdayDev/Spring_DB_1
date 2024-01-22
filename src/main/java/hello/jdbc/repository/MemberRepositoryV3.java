package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * <h2>트랜잭션 - 트랜잭션 매니저</h2>
 * <li>DataSourceUtils.getConnection()</li>
 * <li>DataSourceUtils.releaseConnection()</li>
 */
@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "INSERT INTO member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null; // 파라미터를 바인딩 할 수 있는 SQL 을 담을 수 있다.

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 실제 실행시키는 명령어
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "SELECT * FROM member WHERE member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery(); // 데이터변경일 경우엔 executeUpdate() , 데이터를 조회할 때는 executeQuery()

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("Member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "UPDATE member SET money=? WHERE member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate(); // 쿼리를 실행하고 영향받은 row 수를 반환
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "DELETE FROM member WHERE member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate(); // 쿼리를 실행하고 영향받은 row 수를 반환
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }

    }

    private void close(Connection con, Statement statement, ResultSet resultSet) { // Statement 는 일반 SQL 을 담는다.
        JdbcUtils.closeResultSet(resultSet);
        JdbcUtils.closeStatement(statement);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 주의 ! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
