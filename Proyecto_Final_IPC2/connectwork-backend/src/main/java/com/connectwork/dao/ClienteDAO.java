package com.connectwork.dao;

import com.connectwork.db.DatabaseConnection;
import com.connectwork.models.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crear registro en tabla clientes (después del registro de usuario)
    public boolean crear(int idUsuario) throws SQLException {
        String sql = "INSERT INTO clientes (id) VALUES (?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    // Completar información inicial del cliente
    public boolean completarInfo(int id, String descripcion, String sector, String sitioWeb) throws SQLException {
        String sql = "UPDATE clientes SET descripcion=?, sector=?, sitio_web=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, descripcion);
            ps.setString(2, sector);
            ps.setString(3, sitioWeb);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Obtener cliente por ID (con datos del usuario)
    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT c.*, u.nombre, u.username, u.email, u.telefono, u.activo, u.saldo " +
                     "FROM clientes c JOIN usuarios u ON c.id = u.id WHERE c.id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    // Listar todos los clientes (para el admin)
    public List<Cliente> listarTodos() throws SQLException {
        String sql = "SELECT c.*, u.nombre, u.username, u.email, u.telefono, u.activo, u.saldo " +
                     "FROM clientes c JOIN usuarios u ON c.id = u.id ORDER BY u.nombre";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Recargar saldo
    public boolean recargarSaldo(int idCliente, double monto) throws SQLException {
        Connection conn = getConn();
        // Actualizar saldo en usuarios
        String sqlSaldo = "UPDATE usuarios SET saldo = saldo + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlSaldo)) {
            ps.setDouble(1, monto);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
        // Registrar en tabla recargas
        String sqlRecarga = "INSERT INTO recargas (id_cliente, monto) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlRecarga)) {
            ps.setInt(1, idCliente);
            ps.setDouble(2, monto);
            return ps.executeUpdate() > 0;
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setSector(rs.getString("sector"));
        c.setSitioWeb(rs.getString("sitio_web"));
        c.setNombre(rs.getString("nombre"));
        c.setUsername(rs.getString("username"));
        c.setEmail(rs.getString("email"));
        c.setTelefono(rs.getString("telefono"));
        c.setActivo(rs.getBoolean("activo"));
        c.setSaldo(rs.getDouble("saldo"));
        return c;
    }
}

