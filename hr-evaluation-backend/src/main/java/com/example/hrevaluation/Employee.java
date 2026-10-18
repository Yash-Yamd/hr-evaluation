package com.example.hrevaluation;

import  jakarta.persistence.Column;
import  jakarta.persistence.Entity;
import  jakarta.persistence.Id;
import  jakarta.persistence.Table;
import  jakarta.persistence.JoinColumn;
import  jakarta.persistence.ManyToOne;

@Entity
@Table(name = "employees") //MySQLの「社員の表」に対応する目印
public class Employee {

    @Id //社員を区別する番号の目印
    @Column(name ="employee_id") //MySQLでの項目名
    private Integer employeeId; //Javaで社員番号を持つための名前

    @Column(name = "last_name", nullable = false) //姓
    private String lastName;

    @Column(name = "first_name", nullable = false) //名
    private String firstName;

    @Column(name = "last_name_kana", nullable = false) //セイ
    private String lastNameKana;

    @Column(name = "first_name_kana", nullable = false) //メイ
    private String firstNameKana;

    @ManyToOne(optional = false) //複数の社員が同じ1つの部署に所属できる
    @JoinColumn(name = "department_id", nullable = false) //MySQLでは部署番号を使って部署とつなぐ
    private Department department; //Departmentを使って、この社員の所属部署を持つ

    //社員データか値（社員番号、姓名、フリガナ、所属部署）を取り出す
    public Integer getEmployeeId() { //社員番号
        return employeeId;
    }

    public String getLastName() { //姓
        return lastName;
    }

    public String getFirstName() { //名
        return firstName;
    }

    public String getLastNameKana() { //セイ
        return lastNameKana;
    }

    public String getFirstNameKana() { //メイ
        return firstNameKana;
    }

    public Department getDepartment() { //部署（部署データを渡すためString型ではない）
        return department;
    }
}
