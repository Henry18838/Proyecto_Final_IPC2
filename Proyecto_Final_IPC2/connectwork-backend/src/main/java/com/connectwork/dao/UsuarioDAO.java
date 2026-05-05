package com.connectwork.dao;

import com.connectwork.db.DatabaseConnection;
import com.connectwork.models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UsuarioDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Registrar nuevo usuario (cliente o freelancer)
    public int registrar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, username, password_hash, email, telefono, direccion, cui, fecha_nac, rol) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsername());
            ps.setString(3, BCrypt.hashpw(u.getPasswordHash(), BCrypt.gensalt()));
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getTelefono());
            ps.setString(6, u.getDireccion());
            ps.setString(7, u.getCui());
            ps.setDate(8, u.getFechaNac() != null ? Date.valueOf(u.getFechaNac()) : null);
            ps.setString(9, u.getRol());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    // Login: buscar por username y verificar password
    public Usuario login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND activo = TRUE";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, hash)) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // Buscar por ID
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    // Verificar si username ya existe
    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT id FROM usuarios WHERE username = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        }
    }

    // Verificar si email ya existe
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT id FROM usuarios WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        }
    }

    // Activar o desactivar cuenta (solo admin)
    public boolean toggleActivo(int id, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Marcar perfil como completo
    public boolean completarPerfil(int id) throws SQLException {
        String sql = "UPDATE usuarios SET perfil_completo = TRUE WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Actualizar saldo
    public boolean actualizarSaldo(int id, double nuevoSaldo) throws SQLException {
        String sql = "UPDATE usuarios SET saldo = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, nuevoSaldo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        u.setDireccion(rs.getString("direccion"));
        u.setCui(rs.getString("cui"));
        if (rs.getDate("fecha_nac") != null)
            u.setFechaNac(rs.getDate("fecha_nac").toLocalDate());
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        u.setPerfilCompleto(rs.getBoolean("perfil_completo"));
        u.setSaldo(rs.getDouble("saldo"));
        if (rs.getTimestamp("fecha_registro") != null)
            u.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        return u;
    }
}