package gagebu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class GagebuDao {
	
	public Connection conn = null;
	public PreparedStatement pstmt = null;
	public ResultSet rs = null;
	
	String sql = "";
	
	GagebuVo vo = null;
	
	public GagebuDao() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/works";
			String user = "green";
			String password = "1234";
			conn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			System.out.println("Driver not found");
		} catch (Exception e) {
			System.out.println("DataBase connect fall");
		}
	}
	
	// 데이터 베이스 Close(Connection 객체 닫기)
	public void dbClose() {
	if(conn != null)
		try {
			conn.close();
		} catch (Exception e) {}
	}
	
	// Statement객체 Close
	public void pstmtClose() {	
		if(pstmt != null)
			try {
				pstmt.close();
			} catch (Exception e) {}
	}
	
	// ResultSet 객체 Close
	public void rsClose() {
		if(rs != null)
			try {
				rs.close();
				if(pstmt != null) pstmt.close();
			} catch (Exception e) {}
	}

	public void gInput(GagebuVo vo) {
		// 기존 잔고 load
		int balance;
		try {
			sql = "select balance from gagebu order by idx desc limit 1";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			rs = pstmt.executeQuery(sql);
			if(rs.next()) balance = rs.getInt("balance");
			else balance = 0;
			if(pstmt != null) pstmt.close();
			
			// 수입 / 지출 판별하여 잔액 계산
			if(vo.getgCode().equals("+")) balance += vo.getPrice();
			else balance -= vo.getPrice();
			
			// 입력된 자료를 가계부 테이블에 등록
			sql = "insert into gagebu values (default,default,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, vo.getgCode());
			pstmt.setInt(2, vo.getPrice());
			pstmt.setString(3, vo.getContent());
			pstmt.setInt(4, balance);
			pstmt.executeUpdate();
			System.out.println("자료가 입력되었습니다");
		} catch (Exception e) {
			System.out.println("SQL오류 : " + e.getMessage());
		} finally {
			rsClose();
		}
	}

	public  ArrayList<GagebuVo> gList() {
		ArrayList<GagebuVo> vos = new ArrayList<GagebuVo>();
		try {
			sql = "select * from gagebu order by idx desc";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				vo = new GagebuVo();
				
				vo.setIdx(rs.getInt("idx"));
				vo.setWdate(rs.getString("wdate"));
				vo.setgCode(rs.getString("gCode"));
				vo.setPrice(rs.getInt("price"));
				vo.setContent(rs.getString("content"));
				vo.setBalance(rs.getInt("balance"));
				
				vos.add(vo);
			}
		} catch (Exception e) {
			System.out.println("SQL오류 : " + e.getMessage());
		} finally {
			rsClose();
		}
		return vos;
	}
	public ArrayList<GagebuVo> gSearch(String wdate) {
		ArrayList<GagebuVo> vos = new ArrayList<GagebuVo>();
		try {
			if(wdate.equals("list")) {
				sql = "select * from gagebu order by idx desc"; //sql의 substr(변수, 시작위치, 꺼낼 갯수)
				pstmt = conn.prepareStatement(sql);
			}
			else {
				sql = "select * from gagebu where replace(substr(wdate,1,10),'-','') = ? order by idx desc"; //sql의 substr(변수, 시작위치, 꺼낼 갯수)
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, wdate);
			}
			rs = pstmt.executeQuery();
			while(rs.next()) {
				vo = new GagebuVo();
				
				vo.setIdx(rs.getInt("idx"));
				vo.setWdate(rs.getString("wdate"));
				vo.setgCode(rs.getString("gCode"));
				vo.setPrice(rs.getInt("price"));
				vo.setContent(rs.getString("content"));
				vo.setBalance(rs.getInt("balance"));
				
				vos.add(vo);
			}
		} catch (Exception e) {
			System.out.println("SQL오류 : " + e.getMessage());
		}finally {
			rsClose();
		}
		return vos;
	}
	// DB에서 실제 레코드 삭제 처리
	public void gDelete(int idx) {
		try {
			String gCode = "";
			int price = 0;
			// 고유번호 idx에 해당하는 gCode와 price를 구해온다.
			sql = "select gCode, price from gagebu where idx = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, idx);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				gCode = rs.getString("gCode");
				price = rs.getInt("price");
			}
			if(pstmt != null) pstmt.close();
			
			//기존의 잔고를 읽어온다
			int balance = 0;
			sql = "select idx, balance from gagebu order by idx desc limit 1";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				balance = rs.getInt("balance");
			}
			if(pstmt != null) pstmt.close();
			
			if(gCode.equals("+")) balance -= price;
			else balance += price;
			
			sql = "delete from gagebu where idx = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, idx);
			pstmt.executeUpdate();
			pstmtClose();
			
			sql = "select idx from gagebu order by idx desc limit 1";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			int imsiidx = 0;
			if(rs.next()) {
				imsiidx = rs.getInt("idx");
			}
			if(pstmt != null) pstmt.close();
			
			sql = "update gagebu set balance = ? where idx = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, balance);
			pstmt.setInt(2, idx);
			pstmt.executeUpdate();
			pstmtClose();
			
			System.out.println("자료가 삭제되었습니다");
		} catch (Exception e) {
			System.out.println("SQL오류 : " + e.getMessage());
		} finally {
			pstmtClose();
		}
	}
}