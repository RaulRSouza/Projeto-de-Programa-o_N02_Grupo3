@echo off
cd /d "C:\Users\Raul\Downloads\Projeto-de-Programa-o_N02_Grupo3"

echo ====================================
echo RESETANDO STAGED FILES...
echo ====================================
git reset

echo.
echo ====================================
echo COMMIT 1: SessionManager
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/ui/SessionManager.java
git commit -m "Adicionar SessionManager para gerenciamento de perfis e permissoes"

echo.
echo ====================================
echo COMMIT 2: Configuracoes - Alertas
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/ui/pages/ConfiguracoesController.java
git commit -m "Implementar mudanca de perfil com alertas de confirmacao"

echo.
echo ====================================
echo COMMIT 3: Protecao Aulas Frontend
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/ui/pages/GerenciarAulasController.java
git commit -m "Adicionar validacoes de permissao para gerenciamento de aulas"

echo.
echo ====================================
echo COMMIT 4: Protecao Cursos Frontend
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/ui/pages/CursosController.java
git commit -m "Implementar controle de acesso para gerenciamento de cursos"

echo.
echo ====================================
echo COMMIT 5: Protecao Instrutores e Locais
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/ui/pages/InstrutoresController.java
git add src/main/java/br/com/unit/gerenciamentoAulas/ui/pages/LocaisController.java
git commit -m "Adicionar validacoes de permissao para instrutores e locais"

echo.
echo ====================================
echo COMMIT 6: Protecao Inscricoes Frontend
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/ui/pages/InscricoesController.java
git commit -m "Implementar controle de acesso para inscricoes de alunos"

echo.
echo ====================================
echo COMMIT 7: Backend AulaController
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/controllers/AulaController.java
git commit -m "Adicionar validacoes de permissao no backend para aulas"

echo.
echo ====================================
echo COMMIT 8: Backend Curso e Inscricao
echo ====================================
git add src/main/java/br/com/unit/gerenciamentoAulas/controllers/CursoController.java
git add src/main/java/br/com/unit/gerenciamentoAulas/controllers/InscricaoController.java
git commit -m "Implementar validacoes de permissao no backend para cursos e inscricoes"

echo.
echo ====================================
echo COMMIT 9: Documentacao
echo ====================================
git add ALERTA_MUDANCA_PERFIL.md
git add CONTROLE_PERMISSOES_INTERFACE.md
git add TESTE_PERMISSOES.md
git add AUDITORIA_PERMISSOES_IMPLEMENTADAS.md
git commit -m "Adicionar documentacao completa do sistema de permissoes"

echo.
echo ====================================
echo COMMIT 10: Arquivos de dados
echo ====================================
git add data/
git add commit.bat
git add fazer_commits.bat
git add commits_separados.bat
git commit -m "Atualizar arquivos de configuracao e dados do sistema"

echo.
echo ====================================
echo VERIFICANDO STATUS...
echo ====================================
git status

echo.
echo ====================================
echo ENVIANDO PARA O GITHUB (FORCE)...
echo ====================================
git push origin main --force

echo.
echo ====================================
echo HISTORICO DOS ULTIMOS 10 COMMITS:
echo ====================================
git log --oneline -10

echo.
echo ====================================
echo CONCLUIDO!
echo Verifique em: https://github.com/RaulRSouza/Projeto-de-Programa-o_N02_Grupo3/commits/main/
echo ====================================
pause
