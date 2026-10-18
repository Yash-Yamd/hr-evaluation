package com.example.hrevaluation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    public Integer getDepartmentId() { //部署の番号を取り出す
        return departmentId;
    }

    public String getDepartmentName() { //部署の名前を取り出す
        return departmentName;
    }
}
