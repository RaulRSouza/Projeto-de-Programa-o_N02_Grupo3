# 🎓 Sistema de Gerenciamento de Aulas - Véridia

## Grupo 3 - Cadastro e Gerenciamento de Aulas

### 🔧 ÚLTIMA ATUALIZAÇÃO - 05/11/2025

✅ **CORREÇÕES APLICADAS**: Problema do banco de dados foi corrigido!
- Arquivo `data.sql` ajustado para herança JPA correta
- 10 alunos + 5 instrutores + 25 aulas populados
- Ver `CORRECOES_APLICADAS.md` para detalhes

### 🚀 COMO RODAR (ATUALIZADO)

```cmd
# Opção 1: Script automatizado
RODAR_CORRIGIDO.cmd

# Opção 2: Manual
mvnw.cmd clean install -DskipTests
mvnw.cmd javafx:run
```

---

### 📋 Descrição do Projeto

Sistema completo e moderno de gerenciamento de aulas desenvolvido para a Prefeitura de Véridia, implementando todas as funcionalidades especificadas nos requisitos da Unidade II com interface profissional inspirada no Google Material Design.

### ✨ Funcionalidades Implementadas

#### ✅ Requisitos Básicos
- ✔️ **Classes de Domínio**: Aula, Curso, Instrutor, Local, Inscricao, Usuario
- ✔️ **Vinculação**: Aulas vinculadas a cursos e instrutores
- ✔️ **Controle de Datas**: Gerenciamento completo de datas e horários
- ✔️ **Reagendamento**: Sistema de reagendamento com notificação automática
- ✔️ **Verificação de Disponibilidade**: Validação de conflitos de horário e local
- ✔️ **Material Complementar**: Upload de PDF e links externos
- ✔️ **Repositório**: AulaRepository com operações CRUD completas
- ✔️ **Conflitos**: Métodos para verificar conflitos de horário
- ✔️ **Interface Visual**: Tela moderna em JavaFX para visualização
- ✔️ **Testes**: Validações implementadas

#### 🌟 Funcionalidades EXTRA (+0.5)
- ✔️ **Verificação Automática**: Conflitos de horário detectados automaticamente
- ✔️ **Alertas Visuais**: Indicadores de sobreposição de aulas
- ✔️ **Exportação CSV**: Lista de aulas por curso em formato CSV

### 🛠️ Tecnologias Utilizadas

- **Java 21**: Linguagem principal
- **Spring Boot 3.5.6**: Framework backend
- **Spring Data JPA**: Persistência de dados
- **JavaFX 21**: Interface gráfica moderna
- **H2 Database**: Banco de dados embarcado
- **Maven**: Gerenciamento de dependências

### 📊 Estrutura do Banco de Dados

#### Tabela: `aulas`
```sql
- id (PRIMARY KEY)
- titulo
- descricao
- data_hora_inicio
- data_hora_fim
- curso_id (FOREIGN KEY)
- instrutor_id (FOREIGN KEY)
- local_id (FOREIGN KEY)
- vagas_disponiveis
- vagas_totais
- status
- observacoes
- material_url
- material_nome_arquivo
- material_tipo
- material_arquivo (BLOB)
```

### 🚀 Como Executar

#### Opção 1: Usando o Script PowerShell (Recomendado)
```powershell
.\iniciar.ps1
```

#### Opção 2: Usando Maven Diretamente
```bash
# Compilar o projeto
./mvnw clean install -DskipTests

# Executar a aplicação
./mvnw javafx:run
```

#### Opção 3: Usando IDE (IntelliJ/Eclipse)
1. Importe o projeto como Maven Project
2. Execute a classe `JavaFXApplication.java`

### 🌐 Endpoints REST API

A API REST está disponível em: `http://localhost:9090/api/aulas`

#### Principais Endpoints:
- `GET /api/aulas` - Listar todas as aulas
- `GET /api/aulas/{id}` - Buscar aula por ID
- `POST /api/aulas` - Criar nova aula
- `PUT /api/aulas/{id}` - Atualizar aula
- `PATCH /api/aulas/{id}/reagendar` - Reagendar aula
- `DELETE /api/aulas/{id}` - Deletar aula
- `GET /api/aulas/curso/{cursoId}` - Listar aulas por curso
- `GET /api/aulas/curso/{cursoId}/exportar-csv` - Exportar aulas em CSV
- `GET /api/aulas/futuras` - Listar aulas futuras
- `GET /api/aulas/disponiveis` - Listar aulas com vagas

### 🗄️ Console H2 Database

Acesse o console do banco de dados em: `http://localhost:9090/h2-console`

**Credenciais:**
- JDBC URL: `jdbc:h2:file:./data/veridiadb`
- Username: `dev`
- Password: `123456`

### 📱 Interface do Usuário

O sistema possui uma interface moderna inspirada no Google Material Design com:

- 📊 **Dashboard**: Visão geral com estatísticas
- 📚 **Gerenciamento de Aulas**: CRUD completo com filtros
- 🎓 **Cursos**: Gestão de cursos
- 👨‍🏫 **Instrutores**: Cadastro de instrutores
- 📍 **Locais**: Gerenciamento de salas e laboratórios
- 👥 **Inscrições**: Controle de inscrições de alunos

### 📦 Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/com/unit/gerenciamentoAulas/
│   │   ├── controllers/        # REST Controllers
│   │   ├── entidades/          # Entidades JPA
│   │   ├── repositories/       # Repositórios Spring Data
│   │   ├── servicos/           # Camada de serviços
│   │   ├── ui/                 # Controllers JavaFX
│   │   ├── dtos/               # Data Transfer Objects
│   │   └── exceptions/         # Exceções customizadas
│   └── resources/
│       ├── fxml/               # Arquivos FXML (Views)
│       ├── css/                # Estilos CSS
│       ├── application.properties
│       └── data.sql            # Dados iniciais
```

### 🧪 Dados de Teste

O sistema já vem com dados de exemplo:
- 5 Cursos cadastrados
- 5 Instrutores
- 5 Locais (salas/laboratórios)
- 8 Aulas de exemplo

### 🎯 Funcionalidades Detalhadas

#### Verificação de Conflitos
- ✅ Conflito de instrutor (mesmo instrutor, mesmo horário)
- ✅ Conflito de local (mesma sala, mesmo horário)
- ✅ Validação de capacidade do local
- ✅ Alertas visuais na interface

#### Reagendamento
- ✅ Alteração de data/hora
- ✅ Mudança de local
- ✅ Notificação automática aos alunos inscritos
- ✅ Validação de novos conflitos

#### Material Complementar
- ✅ Upload de arquivos PDF
- ✅ Armazenamento de links externos
- ✅ Download de materiais

### 👥 Responsabilidades dos Integrantes

**Grupo 3:**
- Desenvolvimento completo do sistema de gerenciamento de aulas
- Implementação da interface JavaFX moderna
- Criação das APIs REST
- Validações e tratamento de conflitos
- Documentação e testes

### 📸 Screenshots

> Interface moderna com design inspirado no Google Material Design
> Dashboard com estatísticas em tempo real
> Tabelas com ações inline (editar, cancelar, deletar)
> Filtros inteligentes e exportação CSV

### 📝 Notas de Desenvolvimento

- Sistema totalmente funcional e pronto para produção
- Código limpo seguindo boas práticas
- Validações robustas em todas as operações
- Interface responsiva e intuitiva
- Banco de dados persistente (arquivo H2)

### 🔄 Próximas Melhorias (Futuras)

- [ ] Sistema de autenticação e autorização
- [ ] Relatórios avançados em PDF
- [ ] Integração com e-mail para notificações
- [ ] App mobile (React Native)
- [ ] Sistema de avaliação de aulas

---

**© 2025 - Sistema de Gerenciamento de Aulas Véridia | Grupo 3**
