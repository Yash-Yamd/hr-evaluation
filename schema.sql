-- 初めて使うPCにデータを入れる表を用意する

-- 部署の表
CREATE TABLE IF NOT EXISTS departments (
    department_id INT NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (department_id)
);

-- 社員の表
CREATE TABLE IF NOT EXISTS employees (
    employee_id INT NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name_kana VARCHAR(255) NOT NULL,
    first_name_kana VARCHAR(255) NOT NULL,
    department_id INT NOT NULL,
    PRIMARY KEY (employee_id),
    FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
);

-- 評価点の表
CREATE TABLE IF NOT EXISTS evaluations (
    employee_id INT NOT NULL,
    practical_skill_score INT NOT NULL,
    problem_solving_score INT NOT NULL,
    logical_thinking_score INT NOT NULL,
    communication_score INT NOT NULL,
    goal_achievement_score INT NOT NULL,
    PRIMARY KEY (employee_id),
    FOREIGN KEY (employee_id)
        REFERENCES employees (employee_id)
);