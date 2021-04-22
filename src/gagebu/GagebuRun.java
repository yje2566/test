package gagebu;

import java.util.Scanner;

public class GagebuRun {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		GagebuDao dao = new GagebuDao();
		GagebuService service = new GagebuService();
		
		boolean run = true;
		System.out.println("=====================가계부====================");
		while(run) {
			System.out.println("-------------------------------------------------");
			System.out.println("1. 입력 2. 날짜조회 3. 전체조회 4. 수정 5. 삭제 0.종료");
			System.out.println("-------------------------------------------------");
			System.out.print(">> ");
			int no = sc.nextInt();
			
			switch(no) {
				case 1:  //자료 입력
					service.gInput();
					break;
				case 2:  //날짜 조회
					service.gList(2);
					break;
				case 3:  //전체 조회
					service.gList(3);
					break;
				/*1111case 4:  //자료 수정
					service.gUpdate();
					break;*/
				case 5:  //자료 삭제
					service.gDelete();
					break;
				default:  //종료
					run = false;
					break;
			}
		}
		System.out.println("=============================================");
		System.out.println("\t\tWork End...");
		System.out.println("=============================================");
		dao.dbClose();
		sc.close();
	}
}
