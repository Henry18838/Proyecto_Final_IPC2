package com.connectwork.dao;

import com.connectwork.db.DatabaseConnection;
import com.connectwork.models.Freelancer;
import com.connectwork.models.Habilidad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FreelancerDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crear registro en tabla freelancers
    public boolean crear(int idUsuario) throws SQLException {
        String sql = "INSERT INTO freelancers (id) VALUES (?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    // Completar información inicial
    public boolean completarInfo(int id, String bio, String experiencia, double tarifaHora, List<Integer> idHabilidades) throws SQLException {
        Connection conn = getConn();
        String sql = "UPDATE freelancers SET bio=?, experiencia=?, tarifa_hora=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bio);
            ps.setString(2, experiencia);
            ps.setDouble(3, tarifaHora);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
        // Insertar habilidades
        String delSql = "DELETE FROM freelancer_habilidades WHERE id_freelancer = ?";
        try (PreparedStatement ps = conn.prepareStatement(delSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        String insSql = "INSERT INTO freelancer_habilidades (id_freelancer, id_habilidad) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insSql)) {
            for (int idH : idHabilidades) {
                ps.setInt(1, id);
                ps.setInt(2, idH);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return true;
    }

    // Buscar por ID con habilidades
    public Freelancer buscarPorId(int id) throws SQLException {
        String sql = "SELECT f.*, u.nombre, u.username, u.email, u.telefono, u.activo, u.saldo, " +
                     "COALESCE(AVG(cal.estrellas), 0) as calificacion_promedio " +
                     "FROM freelancers f JOIN usuarios u ON f.id = u.id " +
                     "LEFT JOIN calificaciones cal ON cal.id_freelancer = f.id " +
                     "WHERE f.id = ? GROUP BY f.id";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Freelancer fr = mapear(rs);
                fr.setHabilidades(obtenerHabilidades(id));
                return fr;
            }
        }
        return null;
    }

    // Listar todos (para admin)
    public List<Freelancer> listarTodos() throws SQLException {
        String sql = "SELECT f.*, u.nombre, u.username, u.email, u.telefono, u.activo, u.saldo, " +
                     "COALESCE(AVG(cal.estrellas), 0) as calificacion_promedio " +
                     "FROM freelancers f JOIN usuarios u ON f.id = u.id " +
                     "LEFT JOIN calificaciones cal ON cal.id_freelancer = f.id " +
                     "GROUP BY f.id ORDER BY u.nombre";
        List<Freelancer> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Obtener habilidades de un freelancer
    public List<Habilidad> obtenerHabilidades(int idFreelancer) throws SQLException {
        String sql = "SELECT h.*, c.nombre as nombre_categoria FROM habilidades h " +
                     "JOIN freelancer_habilidades fh ON h.id = fh.id_habilidad " +
                     "JOIN categorias c ON h.id_categoria = c.id " +
                     "WHERE fh.id_freelancer = ?";
        List<Habilidad> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idFreelancer);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Habilidad h = new Habilidad();
                h.setId(rs.getInt("id"));
                h.setNombre(rs.getString("nombre"));
                h.setIdCategoria(rs.getInt("id_categoria"));
                h.setNombreCategoria(rs.getString("nombre_categoria"));
                lista.add(h);
            }
        }
        return lista;
    }

    private Freelancer mapear(ResultSet rs) throws SQLException {
        Freelancer f = new Freelancer();
        f.setId(rs.getInt("id"));
        f.setBio(rs.getString("bio"));
        f.setExperiencia(rs.getString("experiencia"));
        f.setTarifaHora(rs.getDouble("tarifa_hora"));
        f.setNombre(rs.getString("nombre"));
        f.setUsername(rs.getString("username"));
        f.setEmail(rs.getString("email"));
        f.setTelefono(rs.getString("telefono"));
        f.setActivo(rs.getBoolean("activo"));
        f.setSaldo(rs.getDouble("saldo"));
        f.setCalificacionPromedio(rs.getDouble("calificacion_promedio"));
        return f;
    }
}

