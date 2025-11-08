# 📖 Guia do Usuário - Sistema Véridia

## Bem-vindo ao Sistema de Gerenciamento de Aulas

Este guia irá ajudá-lo a utilizar todas as funcionalidades do sistema.

---

## 🚀 Iniciando o Sistema

### Windows
```powershell
.\iniciar.ps1
```

### Linux/Mac
```bash
./mvnw javafx:run
```

O sistema abrirá automaticamente a interface gráfica.

---

## 🏠 Dashboard Principal

Ao iniciar, você verá o **Dashboard** com:

### 📊 Cards de Estatísticas
- **Total de Aulas**: Quantidade total de aulas cadastradas
- **Próximas Aulas**: Aulas agendadas para o futuro
- **Vagas Disponíveis**: Total de vagas abertas em todas as aulas

### 📋 Tabela de Aulas Recentes
Exibe as 10 aulas mais recentes ou próximas com:
- ID da aula
- Curso
- Instrutor
- Local
- Data/Hora
- Vagas disponíveis
- Status

---

## 📚 Gerenciar Aulas

Clique em **"Gerenciar Aulas"** no menu lateral.

### 🔍 Filtros Disponíveis

#### Todas
Lista todas as aulas cadastradas no sistema.

#### Futuras
Exibe apenas aulas com data futura (não realizadas ainda).

#### Disponíveis
Mostra aulas futuras que ainda possuem vagas disponíveis.

#### Canceladas
Lista aulas que foram canceladas.

### ➕ Criar Nova Aula

1. Clique no botão **"➕ Nova Aula"**
2. Preencha os campos:
   - **Título**: Nome da aula (ex: "Introdução ao Java")
   - **Descrição**: Detalhes sobre o conteúdo
   - **Curso**: Selecione o curso vinculado
   - **Instrutor**: Escolha o instrutor
   - **Local**: Selecione sala/laboratório
   - **Data/Hora Início**: Quando a aula começa
   - **Data/Hora Fim**: Quando termina
   - **Vagas**: Quantidade de alunos
   - **Observações**: Informações extras (opcional)
3. Clique em **"Salvar"**

#### ⚠️ Validações Automáticas
O sistema verificará automaticamente:
- ✅ Se o instrutor está disponível
- ✅ Se o local está livre
- ✅ Se as vagas cabem no local
- ✅ Se as datas são válidas
- ✅ Se o curso está ativo

---

### ✏️ Editar Aula

1. Localize a aula na tabela
2. Clique no botão **"✏️"** (Editar)
3. Modifique os campos desejados
4. Clique em **"Salvar"**

**Nota:** Aulas canceladas não podem ser editadas.

---

### 🔄 Reagendar Aula

1. Clique no botão **"✏️"** da aula
2. Altere a data/hora ou local
3. O sistema validará novos conflitos
4. Ao salvar, **todos os alunos inscritos serão notificados**

---

### 🚫 Cancelar Aula

1. Clique no botão **"🚫"** (Cancelar)
2. Confirme a ação
3. Digite o motivo do cancelamento
4. A aula será marcada como CANCELADA
5. Todas as inscrições serão canceladas automaticamente
6. Alunos receberão notificação

---

### 🗑️ Deletar Aula

1. Clique no botão **"🗑️"** (Deletar)
2. Confirme a ação
3. **Atenção:** Esta ação é irreversível!

---

## 📊 Exportar Aulas para CSV

### Exportar Todas as Aulas
1. No menu "Gerenciar Aulas"
2. Clique em **"📊 Exportar CSV"**
3. Escolha o local para salvar
4. Arquivo será gerado com todas as aulas

### Exportar Aulas de um Curso Específico
1. Use a API REST: `GET /api/aulas/curso/{cursoId}/exportar-csv`
2. Ou filtre as aulas por curso antes de exportar

### 📄 Formato do CSV
```csv
ID,Título,Descrição,Curso,Instrutor,Local,Data Início,Data Fim,Vagas Disponíveis,Vagas Totais,Status
1,Introdução ao Java,Conceitos básicos...,Java Avançado,Prof. Carlos,Sala 101,15/12/2025 09:00,15/12/2025 12:00,25,30,AGENDADA
```

---

## 📎 Material Complementar

### Adicionar Material

#### Opção 1: Upload de PDF
1. Edite a aula
2. Vá até a seção "Material Complementar"
3. Clique em **"Upload PDF"**
4. Selecione o arquivo (máx. 10MB)
5. Arquivo será armazenado no banco

#### Opção 2: Link Externo
1. Edite a aula
2. Cole o link na caixa "URL do Material"
3. Pode ser link do Google Drive, Dropbox, etc.

### Download de Material
1. Visualize a aula
2. Clique em **"📥 Download Material"**
3. Arquivo será baixado

### Remover Material
1. Edite a aula
2. Clique em **"Remover Material"**

---

## 🎓 Gestão de Cursos

### Ver Cursos
1. Clique em **"Cursos"** no menu lateral
2. Lista todos os cursos cadastrados

### Informações do Curso
- Nome
- Descrição
- Carga Horária
- Categoria
- Status (Ativo/Inativo)
- Quantidade de aulas

---

## 👨‍🏫 Gestão de Instrutores

### Ver Instrutores
1. Clique em **"Instrutores"** no menu
2. Lista todos os instrutores

### Informações do Instrutor
- Nome
- E-mail
- Especialidade
- Registro profissional
- Aulas ministradas

---

## 📍 Gestão de Locais

### Ver Locais
1. Clique em **"Locais"** no menu
2. Lista salas e laboratórios

### Informações do Local
- Nome (ex: "Sala 101")
- Endereço/Localização
- Capacidade máxima
- Tipo (Sala, Laboratório, Auditório)
- Disponibilidade

---

## 👥 Gestão de Inscrições

### Ver Inscrições
1. Clique em **"Inscrições"** no menu
2. Lista todas as inscrições

### Status de Inscrição
- **CONFIRMADA**: Aluno está inscrito
- **CANCELADA**: Inscrição foi cancelada
- **EM ESPERA**: Lista de espera (quando não há vagas)

---

## 🔔 Sistema de Notificações

### Quando são Enviadas Notificações?

#### Reagendamento de Aula
- Todos os alunos inscritos recebem notificação
- Informações: data/hora antiga vs nova
- Motivo da mudança (se informado)

#### Cancelamento de Aula
- Notificação enviada a todos os inscritos
- Motivo do cancelamento
- Status da inscrição alterado para CANCELADA

---

## 🛡️ Alertas e Validações

### 🚨 Conflito de Horário Detectado
**Situação:** Tentar agendar aula quando instrutor ou local já está ocupado.

**Ação do Sistema:**
- Exibe alerta vermelho
- Informa detalhes do conflito
- Impede o agendamento
- Sugere horários alternativos

### ⚠️ Capacidade Excedida
**Situação:** Número de vagas maior que capacidade do local.

**Ação do Sistema:**
- Exibe alerta
- Mostra capacidade máxima do local
- Sugere reduzir vagas ou mudar local

### ℹ️ Curso Inativo
**Situação:** Tentar criar aula para curso inativo.

**Ação do Sistema:**
- Exibe aviso
- Impede criação da aula
- Sugere ativar o curso primeiro

---

## 🗄️ Console do Banco de Dados

### Acessar Console H2
1. Abra navegador
2. Acesse: `http://localhost:9090/h2-console`
3. Use as credenciais:
   - **JDBC URL:** `jdbc:h2:file:./data/veridiadb`
   - **Username:** `dev`
   - **Password:** `123456`

### Consultas Úteis

#### Ver todas as aulas
```sql
SELECT * FROM aulas;
```

#### Ver aulas futuras
```sql
SELECT * FROM aulas 
WHERE data_hora_inicio > CURRENT_TIMESTAMP 
ORDER BY data_hora_inicio;
```

#### Verificar conflitos de instrutor
```sql
SELECT a1.*, a2.* 
FROM aulas a1, aulas a2 
WHERE a1.instrutor_id = a2.instrutor_id 
AND a1.id != a2.id 
AND a1.data_hora_inicio < a2.data_hora_fim 
AND a1.data_hora_fim > a2.data_hora_inicio;
```

---

## 🔧 Solução de Problemas

### Sistema não inicia
1. Verifique se Java 21 está instalado: `java -version`
2. Tente: `./mvnw clean install`
3. Execute novamente

### Erro ao criar aula
- Verifique se todos os campos obrigatórios estão preenchidos
- Confirme que não há conflitos de horário
- Verifique se o curso está ativo

### Material não faz upload
- Verifique se é arquivo PDF
- Tamanho máximo: 10MB
- Tente usar link externo como alternativa

### Dados não aparecem
1. Clique em **"🔄 Atualizar"**
2. Verifique conexão com banco
3. Reinicie o sistema se necessário

---

## 📞 Suporte

### Recursos Disponíveis
- 📖 **README.md**: Visão geral do projeto
- 📋 **REQUISITOS.md**: Documentação técnica
- 🗂️ **H2 Console**: Visualização direta do banco

### Logs do Sistema
Os logs são exibidos no console onde o sistema foi iniciado.

---

## ✅ Checklist de Uso Diário

- [ ] Verificar dashboard ao iniciar
- [ ] Revisar aulas do dia
- [ ] Conferir conflitos de horário
- [ ] Atualizar materiais complementares
- [ ] Responder notificações pendentes
- [ ] Exportar relatórios semanais

---

## 🎯 Dicas e Boas Práticas

1. **Planeje com Antecedência**: Crie aulas com pelo menos 1 semana de antecedência
2. **Verifique Disponibilidade**: Sempre confirme disponibilidade antes de agendar
3. **Use Observações**: Adicione informações importantes nas observações
4. **Material Complementar**: Envie materiais antes da aula
5. **Notificações**: Sempre adicione motivo ao reagendar/cancelar
6. **Backups**: Exporte CSV regularmente para backup
7. **Console H2**: Use para relatórios personalizados

---

## 📱 Atalhos de Teclado

*(Funcionalidade futura)*

- `Ctrl + N`: Nova aula
- `Ctrl + R`: Atualizar lista
- `Ctrl + F`: Buscar aula
- `Ctrl + E`: Exportar CSV
- `F5`: Recarregar dados

---

**Última atualização:** Janeiro 2025  
**Versão do sistema:** 1.0.0  
**Suporte:** Grupo 3 - Véridia
