package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false); // 트랜잭션 시작
            bizLogic(con, fromId, toId, money);
            con.commit(); // 로직이 예외없이 수행했기 때문에 commit 실행
        } catch (Exception e) {
            // 예외발생시 해당구문으로 이동
            con.rollback(); // 실패 했기 때문에 롤백 수행
            throw new IllegalStateException(e);
        } finally {
            if (con != null) {
                try {
                    release(con);
                } catch (Exception e) {
                    log.error("error", e);
                }
            }
        }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        // 비즈니스 로직
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private static void release(Connection con) throws SQLException {
        // Connection 을 설정할 때 autocommit 을 자동으로 했기 때문에 커넥션풀에 반환할 때도 자동인 채로 반환된다.
        // 다른 누군가 autocommit 이 자동인줄 알고 connection 을 획득할 수 있기 때문에 설정을 원래대로 하고 반환한다.
        con.setAutoCommit(true);
        con.close();
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
