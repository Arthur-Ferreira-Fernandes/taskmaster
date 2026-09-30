DROP TABLE IF EXISTS tasks;

CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    categoria VARCHAR(50) NOT NULL,
    data_limite DATE NOT NULL,
    data_criacao TIMESTAMP NOT NULL
);
