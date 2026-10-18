package com.example.hrevaluation;

import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import  org.springframework.web.bind.annotation.GetMapping;

@RestController
public class EmployeeController {

    private final EmployeeRepository employeeRepository; //受付で使う、社員データの窓口を持つ場所

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository; //Springから渡された窓口、その場所に入れる
    }

    //MySQLから取り出した社員データを入れ物（record）に移す
    private EmployeeResponse toResponse(Employee employee) { //社員1人分を画面へ渡す形に変える処理
        String departmentName = null;

        if (employee.getDepartment()!= null) { //社員の所属部署が未設定でなかったら
            departmentName = employee.getDepartment().getDepartmentName(); //社員の所属部署から部署名を取り出してdepartmentNameに入れる
        }

        return new EmployeeResponse( //6項目を入れた新しい入れ物を作る
                employee.getEmployeeId(),
                departmentName,
                employee.getLastName(),
                employee.getFirstName(),
                employee.getLastNameKana(),
                employee.getFirstNameKana()
        );
    }
    //全員分を取り出して画面へ返す処理
    @GetMapping("/api/employees") //APIからデータ取得依頼がきたら、以下の処理をするという目印
    public List<EmployeeResponse> getEmployees(){ //List<EmployeeResponse> →EmployeeResponsを複数並べた一覧
        List<Employee> employees = //社員データを全員分、社員番号順に取り出し「employees」という名前で保持しておく
                employeeRepository.findAll(Sort.by("employeeId"));

        List<EmployeeResponse> responses = new ArrayList<>(); //「responses」という名の返すための一覧を作る

        for (Employee employee : employees) { //1人ずつ返す形に変えて追加する
            responses.add(toResponse(employee)); //社員を画面へ渡す6項目の形に変え、返すための一覧（responses）に追加する
        }

        return responses; //出来上がった一覧を依頼した側へ返す
    }
}
