package hello.helloSpring.repository;

import hello.helloSpring.domain.Member;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMemberRepository implements MemberRepository {
    private final DataSource dataSource;
    public JdbcMemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        /* 내가 직접 getConnection 을 할 수도 있다. */
        /* 근데 그렇게 하지는 말자. Spring Framework 을 쓸 땐 DataSourceUtils 를 통해야 한다. */
    }

    @Override
    public Member save(Member member) {
        /* save 할 SQL */
        /* 변수보다는 밖에 상수로 빼 내는 게 낫다. */
        String sql = "insert into member(name) values(?)";

        /* DB Connection 은 외부 네트워크와 연결되어 있기 때문에 사용이 끝나면 릴리즈를 끊어야 한다. */
        /* DB Connection 이 계속 쌓이면 대장애가 날 수도 있다. */
        Connection conn = null;
        PreparedStatement pstmt = null;
        /* ResultSet : 결과를 받는 것 */
        ResultSet rs = null;

        try {
            /* 먼저 getConnection 을 통해 연결을 가져온다. */
            conn = getConnection();
            /* PreparedStatement 에서 SQL 을 넣는다. */
            /* RETURN_GENERATED_KEYS : DB 에 인서트하면 인서트를 해야 id 값을 얻을 수 있다. 그 때 사용된다. */
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            /* parameterIndex : values(?) 에 들어갈 물음표와 매칭이 된다. */
            /* 매칭을 시키면 member.Name() 으로 값을 넣는다. */
            pstmt.setString(1, member.getName());

            /* DB에 실제 쿼리가 executeUpdate() 할 때 날아간다. */
            pstmt.executeUpdate();
            /* getGeneratedKeys() : DB 가 id 값(1번, 2번, ...)을 반환해 준다. */
            rs = pstmt.getGeneratedKeys();

            /* ResultSet 이 값을 가지고 있으면, .next() 를 통해 값을 꺼내 온다. */
            if (rs.next()) {
                /* 값이 있으면? */
                /* .getLong() 을 통해 값을 꺼낸다. */
                member.setId(rs.getLong(1));
            } else {
                throw new SQLException("id 조회 실패");
            }
            return member;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    /* 조회 */
    @Override
    public Optional<Member> findById(Long id) {
        /* 쿼리문을 날려서 sql 을 가지고 온다. */
        String sql = "select * from member where id = ?";

        Connection conn = null; PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            /* Connection */
            conn = getConnection();
            /* SQL 보내기 */
            pstmt = conn.prepareStatement(sql);
            /* PreparedStatement 세팅 */
            pstmt.setLong(1, id);

            /* 조회는 executeUpdate() 가 아니라 executeQuery() 다. */
            rs = pstmt.executeQuery();

            if(rs.next()) {
                /* 값이 있으면? */
                /* 멤버 객체를 생성한다. */
                Member member = new Member();
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                /* 그리고 반환한다. */
                return Optional.of(member);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    /* 모두 조회 */
    @Override
    public List<Member> findAll() {
        String sql = "select * from member";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();

            List<Member> members = new ArrayList<>();
            /* 전부 조회하는 것이기 때문에 while문을 이용한다. */
            while(rs.next()) {
                Member member = new Member();
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                /* 멤버를 담는다. */
                members.add(member);
            }
            /* 담긴 멤버를 반환한다. */
            return members;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    @Override
    public Optional<Member> findByName(String name) {
        String sql = "select * from member where name = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);

            rs = pstmt.executeQuery();

            if(rs.next()) {
                Member member = new Member();
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                return Optional.of(member);
            }

            /* 찾고자 하는 name 이 없으면 Optional.empty() */
            return Optional.empty();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    /* Close Resources : 리소스를 끊는 것이다. 자원 낭비를 막기 위함 */
    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                close(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(Connection conn) throws SQLException {
        DataSourceUtils.releaseConnection(conn, dataSource);
    }
}