package com.connectwork.dao;

import com.connectwork.db.DatabaseConnection;
import com.connectwork.models.Propuesta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropuestaDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Enviar propuesta
    public boolean enviar(Propuesta p) throws SQLException {
        String sql = "INSERT INTO propuestas (id_proyecto, id_freelancer, monto_ofertado, plazo_dias, carta) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, p.getIdProyecto());
            ps.setInt(2, p.getIdFreelancer());
            ps.setDouble(3, p.getMontoOfertado());
            ps.setInt(4, p.getPlazoDias());
            ps.setString(5, p.getCarta());
            return ps.executeUpdate() > 0;
        }
    }

    // Listar propuestas de un proyecto
    public List<Propuesta> listarPorProyecto(int idProyecto) throws SQLException {
        String sql = "SELECT pr.*, p.titulo as titulo_proyecto, u.nombre as nombre_freelancer, " +
                     "COALESCE(AVG(cal.estrellas),0) as calificacion_freelancer " +
                     "FROM propuestas pr JOIN proyectos p ON pr.id_proyecto = p.id " +
                     "JOIN usuarios u ON pr.id_freelancer = u.id " +
                     "LEFT JOIN calificaciones cal ON cal.id_freelancer = pr.id_freelancer " +
                     "WHERE pr.id_proyecto = ? GROUP BY pr.id ORDER BY pr.fecha_envio DESC";
        List<Propuesta> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Listar propuestas de un freelancer
    public List<Propuesta> listarPorFreelancer(int idFreelancer) throws SQLException {
        String sql = "SELECT pr.*, p.titulo as titulo_proyecto, u.nombre as nombre_freelancer, 0 as calificacion_freelancer " +
                     "FROM propuestas pr JOIN proyectos p ON pr.id_proyecto = p.id " +
                     "JOIN usuarios u ON pr.id_freelancer = u.id " +
                     "WHERE pr.id_freelancer = ? ORDER BY pr.fecha_envio DESC";
        List<Propuesta> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idFreelancer);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Buscar por ID
    public Propuesta buscarPorId(int id) throws SQLException {
        String sql = "SELECT pr.*, p.titulo as titulo_proyecto, u.nombre as nombre_freelancer, 0 as calificacion_freelancer " +
                     "FROM propuestas pr JOIN proyectos p ON pr.id_proyecto = p.id " +
                     "JOIN usuarios u ON pr.id_freelancer = u.id WHERE pr.id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    // Cambiar estado
    public boolean cambiarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE propuestas SET estado=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Verificar si freelancer ya envió propuesta al proyecto
    public boolean yaEnvio(int idProyecto, int idFreelancer) throws SQLException {
        String sql = "SELECT id FROM propuestas WHERE id_proyecto=? AND id_freelancer=? AND estado != 'RETIRADA'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idFreelancer);
            return ps.executeQuery().next();
        }
    }

    private Propuesta mapear(ResultSet rs) throws SQLException {
        Propuesta p = new Propuesta();
        p.setId(rs.getInt("id"));
        p.setIdProyecto(rs.getInt("id_proyecto"));
        p.setTituloProyecto(rs.getString("titulo_proyecto"));
        p.setIdFreelancer(rs.getInt("id_freelancer"));
        p.setNombreFreelancer(rs.getString("nombre_freelancer"));
        p.setCalificacionFreelancer(rs.getDouble("calificacion_freelancer"));
        p.setMontoOfertado(rs.getDouble("monto_ofertado"));
        p.setPlazoDias(rs.getInt("plazo_dias"));
        p.setCarta(rs.getString("carta"));
        p.setEstado(rs.getString("estado"));
        p.setFechaEnvio(rs.getTimestamp("fecha_envio").toLocalDateTime());
        return p;
    }
}
