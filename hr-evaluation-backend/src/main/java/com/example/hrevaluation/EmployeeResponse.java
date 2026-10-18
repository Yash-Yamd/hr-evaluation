package com.example.hrevaluation;

//画面へ渡す「社員1人分の入れ物」
public record EmployeeResponse( //recode=いくつかの値をひとまとめにして渡すための書き方
        Integer employeeId,
        String departmentName,
        String lastName,
        String firstName,
        String lastNameKana,
        String firstNameKana
) {
}
