-- 最初の部署データを用意する

INSERT INTO departments (
    department_id,
    department_name
)
VALUES
    (1, '営業部'),
    (2, '企画部'),
    (3, '開発部')
ON DUPLICATE KEY UPDATE
    department_id = departments.department_id;