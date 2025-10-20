package br.com.unit.gerenciamentoAulas.servicos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.com.unit.gerenciamentoAulas.entidades.Administrador;
import br.com.unit.gerenciamentoAulas.entidades.Aluno;
import br.com.unit.gerenciamentoAulas.entidades.Instrutor;
import br.com.unit.gerenciamentoAulas.entidades.Usuario;

public class Autenticacao {
    private Map<String, Usuario> usuarios;
    private Usuario usuarioLogado;

    public Autenticacao() {
        this.usuarios = new HashMap<>();
    }

    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getEmail() == null || usuario.getSenha() == null) {
            System.out.println("Dados do usuário inválidos");
            return false;
        }

        if (usuarios.containsKey(usuario.getEmail())) {
            System.out.println("Email já cadastrado no sistema");
            return false;
        }

        usuarios.put(usuario.getEmail(), usuario);
        System.out.println("Usuário registrado com sucesso: " + usuario.getNome());
        return true;
    }

    public boolean login(String email, String senha) {
        if (email == null || senha == null) {
            System.out.println("Email ou senha não podem ser nulos");
            return false;
        }

        Usuario usuario = usuarios.get(email);
        
        if (usuario == null) {
            System.out.println("Usuário não encontrado");
            return false;
        }

        if (!usuario.getSenha().equals(senha)) {
            System.out.println("Senha incorreta");
            return false;
        }

        usuarioLogado = usuario;
        System.out.println("Login realizado com sucesso: " + usuario.getNome());
        return true;
    }

    public void logout() {
        if (usuarioLogado != null) {
            System.out.println("Logout realizado: " + usuarioLogado.getNome());
            usuarioLogado = null;
        }
    }

    public Optional<Usuario> getUsuarioLogado() {
        return Optional.ofNullable(usuarioLogado);
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

        if (!usuarioLogado.getSenha().equals(senhaAtual)) {
            System.out.println("Senha atual incorreta");
            return false;
        }

        if (novaSenha == null || novaSenha.length() < 6) {
            System.out.println("Nova senha deve ter pelo menos 6 caracteres");
            return false;
        }

        usuarioLogado.setSenha(novaSenha);
        System.out.println("Senha alterada com sucesso");
        return true;
    }

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        return Optional.ofNullable(usuarios.get(email));
    }

    public Map<String, Usuario> listarUsuarios() {
        if (!isAdministrador()) {
            System.out.println("Apenas administradores podem listar usuários");
            return new HashMap<>();
        }
        return new HashMap<>(usuarios);
    }

    public boolean removerUsuario(String email) {
        if (!isAdministrador()) {
            System.out.println("Apenas administradores podem remover usuários");
            return false;
        }

        if (usuarios.remove(email) != null) {
            System.out.println("Usuário removido com sucesso");
            return true;
        }

        System.out.println("Usuário não encontrado");
        return false;
    }
}