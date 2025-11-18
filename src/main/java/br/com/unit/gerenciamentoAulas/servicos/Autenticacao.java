package br.com.unit.gerenciamentoAulas.servicos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.com.unit.gerenciamentoAulas.entidades.Administrador;
import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Usuario;

public class Autenticacao {
    private Map<String, Object> usuariosPorEmail;
    private Object usuarioLogado;

    public Autenticacao() {
        this.usuariosPorEmail = new HashMap<>();
    }

    public boolean registrarAluno(Aluno aluno) {
        if (aluno == null || aluno.getEmail() == null || aluno.getSenha() == null) {
            System.out.println("Dados do aluno inválidos");
            return false;
        }

        if (usuariosPorEmail.containsKey(aluno.getEmail())) {
            System.out.println("Email já cadastrado no sistema");
            return false;
        }

        usuariosPorEmail.put(aluno.getEmail(), aluno);
        System.out.println("Aluno registrado com sucesso: " + aluno.getNome());
        return true;
    }

    public boolean registrarInstrutor(Instrutor instrutor) {
        if (instrutor == null || instrutor.getEmail() == null || instrutor.getSenha() == null) {
            System.out.println("Dados do instrutor inválidos");
            return false;
        }

        if (usuariosPorEmail.containsKey(instrutor.getEmail())) {
            System.out.println("Email já cadastrado no sistema");
            return false;
        }

        usuariosPorEmail.put(instrutor.getEmail(), instrutor);
        System.out.println("Instrutor registrado com sucesso: " + instrutor.getNome());
        return true;
    }

    public boolean registrarAdministrador(Administrador administrador) {
        if (administrador == null || administrador.getEmail() == null || administrador.getSenha() == null) {
            System.out.println("Dados do administrador inválidos");
            return false;
        }

        if (usuariosPorEmail.containsKey(administrador.getEmail())) {
            System.out.println("Email já cadastrado no sistema");
            return false;
        }

        usuariosPorEmail.put(administrador.getEmail(), administrador);
        System.out.println("Administrador registrado com sucesso: " + administrador.getNome());
        return true;
    }

    public boolean login(String email, String senha) {
        if (email == null || senha == null) {
            System.out.println("Email ou senha não podem ser nulos");
            return false;
        }

        Object usuario = usuariosPorEmail.get(email);
        
        if (usuario == null) {
            System.out.println("Usuário não encontrado");
            return false;
        }

        String senhaArmazenada = null;
        String nomeUsuario = null;

        if (usuario instanceof Aluno) {
            Aluno aluno = (Aluno) usuario;
            senhaArmazenada = aluno.getSenha();
            nomeUsuario = aluno.getNome();
        } else if (usuario instanceof Instrutor) {
            Instrutor instrutor = (Instrutor) usuario;
            senhaArmazenada = instrutor.getSenha();
            nomeUsuario = instrutor.getNome();
        } else if (usuario instanceof Administrador) {
            Administrador admin = (Administrador) usuario;
            senhaArmazenada = admin.getSenha();
            nomeUsuario = admin.getNome();
        }

        if (senhaArmazenada == null || !senhaArmazenada.equals(senha)) {
            System.out.println("Senha incorreta");
            return false;
        }

        usuarioLogado = usuario;
        System.out.println("Login realizado com sucesso: " + nomeUsuario);
        return true;
    }

    public void logout() {
        if (usuarioLogado != null) {
            String nome = getNomeUsuarioLogado();
            System.out.println("Logout realizado: " + nome);
            usuarioLogado = null;
        }
    }

    public Optional<Usuario> getUsuarioLogado() {
        if (usuarioLogado instanceof Usuario) {
            return Optional.of((Usuario) usuarioLogado);
        }
        return Optional.empty();
    }

    public Optional<Aluno> getAlunoLogado() {
        if (usuarioLogado instanceof Aluno) {
            return Optional.of((Aluno) usuarioLogado);
        }
        return Optional.empty();
    }

    public Optional<Instrutor> getInstrutorLogado() {
        if (usuarioLogado instanceof Instrutor) {
            return Optional.of((Instrutor) usuarioLogado);
        }
        return Optional.empty();
    }

    public Optional<Administrador> getAdministradorLogado() {
        if (usuarioLogado instanceof Administrador) {
            return Optional.of((Administrador) usuarioLogado);
        }
        return Optional.empty();
    }

    public boolean isUsuarioLogado() {
        return usuarioLogado != null;
    }

    public boolean isAdministrador() {
        return usuarioLogado instanceof Administrador;
    }

    public boolean isInstrutor() {
        return usuarioLogado instanceof Instrutor;
    }

    public boolean isAluno() {
        return usuarioLogado instanceof Aluno;
    }

    public boolean alterarSenha(String senhaAtual, String novaSenha) {
        if (!isUsuarioLogado()) {
            System.out.println("Nenhum usuário logado");
            return false;
        }

        if (novaSenha == null || novaSenha.length() < 6) {
            System.out.println("Nova senha deve ter pelo menos 6 caracteres");
            return false;
        }

        if (usuarioLogado instanceof Aluno) {
            Aluno aluno = (Aluno) usuarioLogado;
            if (!aluno.getSenha().equals(senhaAtual)) {
                System.out.println("Senha atual incorreta");
                return false;
            }
            aluno.setSenha(novaSenha);
        } else if (usuarioLogado instanceof Instrutor) {
            Instrutor instrutor = (Instrutor) usuarioLogado;
            if (!instrutor.getSenha().equals(senhaAtual)) {
                System.out.println("Senha atual incorreta");
                return false;
            }
            instrutor.setSenha(novaSenha);
        } else if (usuarioLogado instanceof Administrador) {
            Administrador admin = (Administrador) usuarioLogado;
            if (!admin.getSenha().equals(senhaAtual)) {
                System.out.println("Senha atual incorreta");
                return false;
            }
            admin.setSenha(novaSenha);
        } else {
            System.out.println("Tipo de usuário não reconhecido");
            return false;
        }

        System.out.println("Senha alterada com sucesso");
        return true;
    }

    public Optional<Object> buscarUsuarioPorEmail(String email) {
        return Optional.ofNullable(usuariosPorEmail.get(email));
    }

    public Optional<Aluno> buscarAlunoPorEmail(String email) {
        Object usuario = usuariosPorEmail.get(email);
        if (usuario instanceof Aluno) {
            return Optional.of((Aluno) usuario);
        }
        return Optional.empty();
    }

    public Optional<Instrutor> buscarInstrutorPorEmail(String email) {
        Object usuario = usuariosPorEmail.get(email);
        if (usuario instanceof Instrutor) {
            return Optional.of((Instrutor) usuario);
        }
        return Optional.empty();
    }

    public Optional<Administrador> buscarAdministradorPorEmail(String email) {
        Object usuario = usuariosPorEmail.get(email);
        if (usuario instanceof Administrador) {
            return Optional.of((Administrador) usuario);
        }
        return Optional.empty();
    }

    public Map<String, Object> listarUsuarios() {
        if (!isAdministrador()) {
            System.out.println("Apenas administradores podem listar usuários");
            return new HashMap<>();
        }
        return new HashMap<>(usuariosPorEmail);
    }

    public boolean removerUsuario(String email) {
        if (!isAdministrador()) {
            System.out.println("Apenas administradores podem remover usuários");
            return false;
        }

        if (usuariosPorEmail.remove(email) != null) {
            System.out.println("Usuário removido com sucesso");
            return true;
        }

        System.out.println("Usuário não encontrado");
        return false;
    }

    private String getNomeUsuarioLogado() {
        if (usuarioLogado instanceof Aluno) {
            return ((Aluno) usuarioLogado).getNome();
        } else if (usuarioLogado instanceof Instrutor) {
            return ((Instrutor) usuarioLogado).getNome();
        } else if (usuarioLogado instanceof Administrador) {
            return ((Administrador) usuarioLogado).getNome();
        }
        return "Desconhecido";
    }
}