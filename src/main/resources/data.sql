-- ===============================================
-- DADOS INICIAIS - Sistema de Gerenciamento de Aulas Veridia
-- Data: 2025-11-05
-- CORRIGIDO para funcionar com auto-increment
-- ===============================================

-- Reset das tabelas para evitar erros de chave unica em reinicializacoes
SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE notificacoes;
TRUNCATE TABLE inscricoes;
TRUNCATE TABLE aulas;
TRUNCATE TABLE alunos;
TRUNCATE TABLE instrutores;
TRUNCATE TABLE administradores;
TRUNCATE TABLE usuarios;
TRUNCATE TABLE locais;
TRUNCATE TABLE cursos;
ALTER TABLE notificacoes ALTER COLUMN id RESTART WITH 1;
ALTER TABLE inscricoes ALTER COLUMN id RESTART WITH 1;
ALTER TABLE aulas ALTER COLUMN id RESTART WITH 1;
ALTER TABLE usuarios ALTER COLUMN id RESTART WITH 1;
ALTER TABLE locais ALTER COLUMN id RESTART WITH 1;
ALTER TABLE cursos ALTER COLUMN id RESTART WITH 1;
SET REFERENTIAL_INTEGRITY TRUE;

-- 1. CURSOS (IDs serão gerados automaticamente: 1, 2, 3, 4, 5)
INSERT INTO cursos (nome, descricao, carga_horaria, categoria, ativo) VALUES
('Programacao Java Avancada', 'Curso completo de Java com foco em desenvolvimento empresarial', 120, 'Programacao', true),
('Desenvolvimento Web com Spring Boot', 'Aprenda a criar aplicacoes web modernas com Spring Framework', 80, 'Programacao', true),
('Banco de Dados SQL', 'Fundamentos e tecnicas avancadas de banco de dados relacionais', 60, 'Banco de Dados', true),
('Design de Interfaces UI/UX', 'Principios de design e experiencia do usuario', 40, 'Design', true),
('Gestao de Projetos Ageis', 'Metodologias ageis e Scrum para gestao de projetos', 50, 'Gestao', true);

-- 2. LOCAIS (IDs serão gerados automaticamente: 1, 2, 3, 4, 5)
INSERT INTO locais (nome, endereco, capacidade, tipo, disponivel) VALUES
('Sala 101 - Laboratorio de Informatica', 'Bloco A - 1 Andar', 30, 'Laboratorio', true),
('Sala 205 - Auditorio Principal', 'Bloco B - 2 Andar', 100, 'Auditorio', true),
('Sala 303 - Sala de Aula', 'Bloco C - 3 Andar', 40, 'Sala de Aula', true),
('Sala 104 - Laboratorio de Design', 'Bloco A - 1 Andar', 25, 'Laboratorio', true),
('Sala 208 - Sala de Reunioes', 'Bloco B - 2 Andar', 20, 'Sala de Reuniao', true);

-- 3. INSTRUTORES (IDs serão gerados automaticamente: 1, 2, 3, 4, 5)
-- 3.1 Usuários base para instrutores
INSERT INTO usuarios (nome, email, senha, cpf, telefone) VALUES
('Prof. Carlos Silva', 'carlos.silva@veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '12345678901', '(11) 98765-4321'),
('Profa. Ana Santos', 'ana.santos@veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '23456789012', '(11) 98765-4322'),
('Prof. Ricardo Oliveira', 'ricardo.oliveira@veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '34567890123', '(11) 98765-4323'),
('Profa. Marina Costa', 'marina.costa@veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '45678901234', '(11) 98765-4324'),
('Prof. Joao Ferreira', 'joao.ferreira@veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '56789012345', '(11) 98765-4325');

-- 3.2 Dados específicos de instrutores (usando IDENTITY_VAL_LOCAL() ou LAST_INSERT_ID())
-- Para H2, usamos SELECT id FROM usuarios WHERE email = '...'
INSERT INTO instrutores (id, especialidade, registro)
SELECT id, 'Java e Spring Framework', 'INST-2024-001' FROM usuarios WHERE email = 'carlos.silva@veridia.edu'
UNION ALL
SELECT id, 'Desenvolvimento Web Full-Stack', 'INST-2024-002' FROM usuarios WHERE email = 'ana.santos@veridia.edu'
UNION ALL
SELECT id, 'Banco de Dados e SQL', 'INST-2024-003' FROM usuarios WHERE email = 'ricardo.oliveira@veridia.edu'
UNION ALL
SELECT id, 'UI/UX Design', 'INST-2024-004' FROM usuarios WHERE email = 'marina.costa@veridia.edu'
UNION ALL
SELECT id, 'Gestao de Projetos', 'INST-2024-005' FROM usuarios WHERE email = 'joao.ferreira@veridia.edu';

-- 4. ALUNOS (IDs serão gerados automaticamente: 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
-- 4.1 Usuários base para alunos
INSERT INTO usuarios (nome, email, senha, cpf, telefone) VALUES
('Pedro Henrique Santos', 'pedro.santos@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '11122233344', '(11) 91234-5678'),
('Maria Eduarda Lima', 'maria.lima@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '22233344455', '(11) 92345-6789'),
('Lucas Gabriel Souza', 'lucas.souza@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '33344455566', '(11) 93456-7890'),
('Ana Clara Costa', 'ana.costa@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '44455566677', '(11) 94567-8901'),
('Rafael Oliveira Silva', 'rafael.oliveira@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '55566677788', '(11) 95678-9012'),
('Beatriz Alves', 'beatriz.alves@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '66677788899', '(11) 96789-0123'),
('Guilherme Rodrigues', 'guilherme.rodrigues@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '77788899900', '(11) 97890-1234'),
('Larissa Fernandes', 'larissa.fernandes@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '88899900011', '(11) 98901-2345'),
('Matheus Silva', 'matheus.silva@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '99900011122', '(11) 99012-3456'),
('Julia Martins', 'julia.martins@aluno.veridia.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye', '00011122233', '(11) 90123-4567');

-- 4.2 Dados específicos de alunos
INSERT INTO alunos (id, matricula, curso)
SELECT id, 'ALU-2024-001', 'Programacao Java Avancada' FROM usuarios WHERE email = 'pedro.santos@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-002', 'Desenvolvimento Web com Spring Boot' FROM usuarios WHERE email = 'maria.lima@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-003', 'Banco de Dados SQL' FROM usuarios WHERE email = 'lucas.souza@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-004', 'Design de Interfaces UI/UX' FROM usuarios WHERE email = 'ana.costa@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-005', 'Gestao de Projetos Ageis' FROM usuarios WHERE email = 'rafael.oliveira@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-006', 'Programacao Java Avancada' FROM usuarios WHERE email = 'beatriz.alves@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-007', 'Desenvolvimento Web com Spring Boot' FROM usuarios WHERE email = 'guilherme.rodrigues@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-008', 'Banco de Dados SQL' FROM usuarios WHERE email = 'larissa.fernandes@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-009', 'Design de Interfaces UI/UX' FROM usuarios WHERE email = 'matheus.silva@aluno.veridia.edu'
UNION ALL
SELECT id, 'ALU-2024-010', 'Gestao de Projetos Ageis' FROM usuarios WHERE email = 'julia.martins@aluno.veridia.edu';

-- Inserir Aulas
INSERT INTO aulas (titulo, descricao, curso_id, instrutor_id, local_id, data_hora_inicio, data_hora_fim, vagas_disponiveis, vagas_totais, status, observacoes) VALUES
-- Aulas Concluidas (Passado)
('Introducao a Programacao', 'Conceitos basicos de logica de programacao', 1, 1, 1, '2024-10-01 09:00:00', '2024-10-01 12:00:00', 0, 30, 'CONCLUIDA', 'Aula inaugural do curso'),
('Fundamentos de POO', 'Programacao Orientada a Objetos em Java', 1, 1, 1, '2024-10-08 09:00:00', '2024-10-08 12:00:00', 0, 30, 'CONCLUIDA', NULL),
('Introducao ao SQL', 'Comandos basicos de SQL', 3, 3, 1, '2024-10-15 14:00:00', '2024-10-15 17:00:00', 0, 30, 'CONCLUIDA', NULL),
('HTML e CSS Basico', 'Estrutura e estilizacao de paginas web', 2, 2, 3, '2024-10-20 10:00:00', '2024-10-20 13:00:00', 0, 40, 'CONCLUIDA', NULL),
('Design Thinking', 'Metodologia de design centrada no usuario', 4, 4, 4, '2024-10-25 15:00:00', '2024-10-25 18:00:00', 0, 25, 'CONCLUIDA', 'Otimo feedback dos alunos'),

-- Aulas de Hoje
('Java Collections Framework', 'Trabalhando com listas, sets e maps em Java', 1, 1, 1, '2025-11-05 09:00:00', '2025-11-05 12:00:00', 15, 30, 'EM_ANDAMENTO', 'Aula acontecendo agora'),
('Spring MVC', 'Criando aplicacoes web com Spring MVC', 2, 2, 1, '2025-11-05 14:00:00', '2025-11-05 17:00:00', 20, 30, 'AGENDADA', 'Trazer projeto anterior'),

-- Aulas Proximas (Esta Semana)
('JPA e Hibernate', 'Mapeamento objeto-relacional com JPA', 1, 1, 1, '2025-11-06 09:00:00', '2025-11-06 12:00:00', 25, 30, 'AGENDADA', NULL),
('JavaScript Moderno ES6+', 'Features modernas do JavaScript', 2, 2, 3, '2025-11-06 14:00:00', '2025-11-06 17:00:00', 32, 40, 'AGENDADA', NULL),
('Normalizacao de Banco de Dados', 'Tecnicas de normalizacao 1NF a 5NF', 3, 3, 1, '2025-11-07 09:00:00', '2025-11-07 12:00:00', 18, 30, 'AGENDADA', NULL),
('UX Research', 'Pesquisa e analise de usuarios', 4, 4, 4, '2025-11-07 10:00:00', '2025-11-07 13:00:00', 20, 25, 'AGENDADA', 'Material fornecido'),
('Scrum Ceremonies', 'Rituais e cerimonias do Scrum', 5, 5, 5, '2025-11-08 15:00:00', '2025-11-08 18:00:00', 15, 20, 'AGENDADA', NULL),

-- Aulas Semana que Vem
('Testes Unitarios com JUnit', 'Escrevendo testes automatizados', 1, 1, 1, '2025-11-11 09:00:00', '2025-11-11 12:00:00', 30, 30, 'AGENDADA', 'Importante para certificacao'),
('React Fundamentals', 'Introducao ao React.js', 2, 2, 3, '2025-11-12 14:00:00', '2025-11-12 17:00:00', 40, 40, 'AGENDADA', NULL),
('Stored Procedures', 'Criando procedimentos armazenados', 3, 3, 1, '2025-11-13 09:00:00', '2025-11-13 12:00:00', 28, 30, 'AGENDADA', NULL),
('Prototipagem com Figma', 'Criando prototipos interativos', 4, 4, 4, '2025-11-14 10:00:00', '2025-11-14 13:00:00', 23, 25, 'AGENDADA', 'Conta Figma necessaria'),
('Kanban na Pratica', 'Implementando Kanban em projetos', 5, 5, 5, '2025-11-15 15:00:00', '2025-11-15 18:00:00', 18, 20, 'AGENDADA', NULL),

-- Aulas Dezembro (Futuras)
('Microservices com Spring Boot', 'Arquitetura de microsservicos', 2, 2, 2, '2025-12-02 09:00:00', '2025-12-02 12:00:00', 30, 30, 'AGENDADA', 'Aula especial no auditorio'),
('NoSQL - MongoDB', 'Banco de dados nao relacionais', 3, 3, 1, '2025-12-05 14:00:00', '2025-12-05 17:00:00', 30, 30, 'AGENDADA', NULL),
('Design System', 'Criando bibliotecas de componentes', 4, 4, 4, '2025-12-10 10:00:00', '2025-12-10 13:00:00', 25, 25, 'AGENDADA', NULL),
('DevOps e CI/CD', 'Integracao e entrega continuas', 1, 1, 1, '2025-12-15 09:00:00', '2025-12-15 12:00:00', 30, 30, 'AGENDADA', NULL),
('Kubernetes Basico', 'Orquestracao de containers', 2, 2, 2, '2025-12-18 14:00:00', '2025-12-18 17:00:00', 30, 30, 'AGENDADA', NULL),

-- Aulas Canceladas
('Workshop Python', 'Introducao a linguagem Python', 1, 1, 1, '2025-11-20 09:00:00', '2025-11-20 12:00:00', 30, 30, 'CANCELADA', 'Cancelada por falta de quorum'),
('Machine Learning Basico', 'Conceitos de aprendizado de maquina', 1, 1, 2, '2025-11-25 10:00:00', '2025-11-25 13:00:00', 100, 100, 'CANCELADA', 'Reagendada para 2026'),

-- Aulas com Vagas Esgotadas
('Workshop Java Spring', 'Workshop intensivo de Spring', 2, 2, 2, '2025-11-30 09:00:00', '2025-11-30 18:00:00', 0, 100, 'AGENDADA', 'LOTADO - Lista de espera disponivel');
