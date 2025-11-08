# 📋 Documento de Requisitos - Sistema de Gerenciamento de Aulas

## Grupo 3 - Cadastro e Gerenciamento de Aulas

---

## 1. Requisitos Funcionais

### RF001 - Cadastro de Aulas
**Descrição:** O sistema deve permitir o cadastro de novas aulas com todos os dados necessários.

**Critérios de Aceite:**
- ✅ Informar título e descrição da aula
- ✅ Vincular a um curso existente
- ✅ Vincular a um instrutor
- ✅ Definir local (sala/laboratório)
- ✅ Definir data e horário de início
- ✅ Definir data e horário de término
- ✅ Definir quantidade de vagas
- ✅ Adicionar observações (opcional)

**Status:** ✅ Implementado

---

### RF002 - Validação de Conflitos de Horário
**Descrição:** O sistema deve verificar automaticamente conflitos de horário antes de agendar uma aula.

**Critérios de Aceite:**
- ✅ Verificar se o instrutor já possui aula no mesmo horário
- ✅ Verificar se o local já está ocupado no mesmo horário
- ✅ Exibir mensagem de erro clara indicando o conflito
- ✅ Impedir o agendamento em caso de conflito

**Status:** ✅ Implementado (RF EXTRA)

---

### RF003 - Verificação de Disponibilidade de Local
**Descrição:** O sistema deve verificar a disponibilidade do local antes de agendar.

**Critérios de Aceite:**
- ✅ Verificar se o local está marcado como disponível
- ✅ Verificar se a capacidade do local é suficiente para as vagas
- ✅ Verificar conflitos de horário do local

**Status:** ✅ Implementado

---

### RF004 - Reagendamento de Aulas
**Descrição:** O sistema deve permitir o reagendamento de aulas já cadastradas.

**Critérios de Aceite:**
- ✅ Alterar data e horário
- ✅ Alterar local
- ✅ Alterar quantidade de vagas (respeitando inscrições existentes)
- ✅ Notificar alunos inscritos sobre a mudança
- ✅ Validar novos conflitos de horário

**Status:** ✅ Implementado

---

### RF005 - Notificação Automática
**Descrição:** O sistema deve notificar automaticamente os alunos quando uma aula for reagendada.

**Critérios de Aceite:**
- ✅ Notificar todos os alunos inscritos na aula
- ✅ Informar data/hora anterior e nova data/hora
- ✅ Incluir informações do local
- ✅ Registrar a notificação no histórico

**Status:** ✅ Implementado

---

### RF006 - Material Complementar
**Descrição:** O sistema deve permitir adicionar material complementar às aulas.

**Critérios de Aceite:**
- ✅ Upload de arquivos PDF
- ✅ Adicionar links externos
- ✅ Armazenar metadata do arquivo (nome, tipo, tamanho)
- ✅ Permitir download do material
- ✅ Remover material

**Status:** ✅ Implementado

---

### RF007 - Visualização de Aulas
**Descrição:** O sistema deve fornecer uma interface visual para visualização das aulas.

**Critérios de Aceite:**
- ✅ Listar todas as aulas cadastradas
- ✅ Filtrar aulas por status (agendada, cancelada, concluída)
- ✅ Filtrar aulas futuras
- ✅ Filtrar aulas com vagas disponíveis
- ✅ Exibir informações completas de cada aula
- ✅ Interface moderna e intuitiva

**Status:** ✅ Implementado

---

### RF008 - Exportação de Dados
**Descrição:** O sistema deve permitir exportação da lista de aulas em formato CSV.

**Critérios de Aceite:**
- ✅ Exportar aulas de um curso específico
- ✅ Incluir todos os dados relevantes no CSV
- ✅ Formato padronizado com cabeçalhos
- ✅ Codificação UTF-8 para caracteres especiais

**Status:** ✅ Implementado (RF EXTRA)

---

### RF009 - Cancelamento de Aulas
**Descrição:** O sistema deve permitir o cancelamento de aulas.

**Critérios de Aceite:**
- ✅ Alterar status para "CANCELADA"
- ✅ Registrar motivo do cancelamento
- ✅ Cancelar automaticamente todas as inscrições
- ✅ Notificar alunos inscritos

**Status:** ✅ Implementado

---

### RF010 - Controle de Vagas
**Descrição:** O sistema deve controlar a quantidade de vagas disponíveis.

**Critérios de Aceite:**
- ✅ Decrementar vagas ao adicionar inscrição
- ✅ Incrementar vagas ao cancelar inscrição
- ✅ Impedir inscrição quando não há vagas
- ✅ Exibir vagas disponíveis vs totais

**Status:** ✅ Implementado

---

## 2. Requisitos Não Funcionais

### RNF001 - Performance
**Descrição:** O sistema deve responder rapidamente às operações do usuário.

**Critérios:**
- Operações CRUD devem ser concluídas em menos de 500ms
- Listagens devem carregar em menos de 1 segundo
- Interface deve ser responsiva

**Status:** ✅ Atendido

---

### RNF002 - Usabilidade
**Descrição:** A interface deve ser intuitiva e fácil de usar.

**Critérios:**
- Design moderno inspirado em Google Material Design
- Ícones intuitivos para ações
- Mensagens de erro claras
- Confirmações para ações destrutivas

**Status:** ✅ Atendido

---

### RNF003 - Confiabilidade
**Descrição:** O sistema deve ser confiável e prevenir erros.

**Critérios:**
- Validações em todas as operações
- Transações ACID no banco de dados
- Tratamento adequado de exceções
- Logs de erros

**Status:** ✅ Atendido

---

### RNF004 - Manutenibilidade
**Descrição:** O código deve ser limpo e fácil de manter.

**Critérios:**
- Arquitetura em camadas (Controller, Service, Repository)
- Código documentado
- Seguir padrões de projeto
- Baixo acoplamento

**Status:** ✅ Atendido

---

### RNF005 - Portabilidade
**Descrição:** O sistema deve rodar em diferentes ambientes.

**Critérios:**
- Java 21 compatível com Windows, Linux, macOS
- Banco de dados embarcado (H2)
- Configurações externalizadas

**Status:** ✅ Atendido

---

## 3. Regras de Negócio

### RN001 - Conflito de Instrutor
Um instrutor não pode ter duas aulas no mesmo horário (considerando sobreposição de horários).

**Status:** ✅ Implementado

---

### RN002 - Conflito de Local
Um local não pode ser usado por duas aulas simultaneamente.

**Status:** ✅ Implementado

---

### RN003 - Capacidade do Local
O número de vagas da aula não pode exceder a capacidade do local.

**Status:** ✅ Implementado

---

### RN004 - Redução de Vagas
Não é possível reduzir o número de vagas para menos que o número de alunos já inscritos.

**Status:** ✅ Implementado

---

### RN005 - Edição de Aula Cancelada
Não é possível editar ou reagendar uma aula cancelada.

**Status:** ✅ Implementado

---

### RN006 - Aula no Passado
Não é possível criar aulas com data/hora no passado.

**Status:** ✅ Implementado

---

### RN007 - Horário Válido
A data/hora de término deve ser posterior à data/hora de início.

**Status:** ✅ Implementado

---

### RN008 - Local Disponível
Só é possível agendar aulas em locais marcados como disponíveis.

**Status:** ✅ Implementado

---

### RN009 - Curso Ativo
Só é possível criar aulas para cursos ativos.

**Status:** ✅ Implementado

---

## 4. Casos de Uso

### UC001 - Criar Aula
**Ator Principal:** Administrador

**Fluxo Principal:**
1. Administrador acessa o menu "Gerenciar Aulas"
2. Clica em "Nova Aula"
3. Preenche os dados da aula
4. Sistema valida os dados
5. Sistema verifica conflitos
6. Sistema salva a aula
7. Sistema exibe mensagem de sucesso

**Fluxos Alternativos:**
- 4a. Dados inválidos: Sistema exibe mensagem de erro
- 5a. Conflito detectado: Sistema exibe alerta e impede salvamento

**Status:** ✅ Implementado

---

### UC002 - Reagendar Aula
**Ator Principal:** Administrador

**Fluxo Principal:**
1. Administrador seleciona uma aula
2. Clica em "Editar" ou "Reagendar"
3. Altera os dados necessários
4. Sistema valida as mudanças
5. Sistema verifica novos conflitos
6. Sistema atualiza a aula
7. Sistema notifica os alunos inscritos
8. Sistema exibe mensagem de sucesso

**Status:** ✅ Implementado

---

### UC003 - Exportar Aulas para CSV
**Ator Principal:** Administrador

**Fluxo Principal:**
1. Administrador acessa lista de aulas
2. Filtra por curso (opcional)
3. Clica em "Exportar CSV"
4. Sistema gera o arquivo CSV
5. Sistema oferece o download
6. Usuário salva o arquivo

**Status:** ✅ Implementado

---

## 5. Matriz de Rastreabilidade

| Requisito | Classe/Arquivo | Método | Status |
|-----------|---------------|---------|---------|
| RF001 | AulaController | criar() | ✅ |
| RF002 | AulaRepository | existsConflitoInstrutor() | ✅ |
| RF003 | AulaService | validarLocalDisponivel() | ✅ |
| RF004 | AulaController | reagendar() | ✅ |
| RF005 | NotificacaoService | notificarReagendamento() | ✅ |
| RF006 | AulaController | uploadMaterial() | ✅ |
| RF007 | GerenciarAulasController | initialize() | ✅ |
| RF008 | CsvExportService | exportAulasToCsv() | ✅ |
| RF009 | AulaService | cancelarAula() | ✅ |
| RF010 | Aula | adicionarInscricao() | ✅ |

---

## 6. Conclusão

Todos os requisitos especificados foram implementados com sucesso. O sistema está completo, funcional e pronto para uso, incluindo as funcionalidades extras que valem pontos adicionais.

**Pontuação EXTRA obtida:** +0.5
- ✅ Verificação automática de conflito de horário
- ✅ Alertas visuais de sobreposição
- ✅ Exportação CSV

---

**Última atualização:** Janeiro 2025  
**Versão do documento:** 2.0  
**Status do projeto:** ✅ Concluído
