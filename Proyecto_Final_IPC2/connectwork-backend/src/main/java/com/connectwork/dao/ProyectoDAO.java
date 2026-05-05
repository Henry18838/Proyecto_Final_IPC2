package com.connectwork.dao;

import com.connectwork.db.DatabaseConnection;
import com.connectwork.models.Habilidad;
import com.connectwork.models.Proyecto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProyectoDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crear proyecto
    public int crear(Proyecto p, List<Integer> idHabilidades) throws SQLException {
        String sql = "INSERT INTO proyectos (id_cliente, titulo, descripcion, id_categoria, presupuesto_max, fecha_limite) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        int idProyecto = -1;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdCliente());
            ps.setString(2, p.getTitulo());
            ps.setString(3, p.getDescripcion());
            ps.setInt(4, p.getIdCategoria());
            ps.setDouble(5, p.getPresupuestoMax());
            ps.setDate(6, Date.valueOf(p.getFechaLimite()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) idProyecto = rs.getInt(1);
        }
        if (idProyecto > 0) agregarHabilidades(idProyecto, idHabilidades);
        return idProyecto;
    }

    // Agregar habilidades requeridas
    private void agregarHabilidades(int idProyecto, List<Integer> idHabilidades) throws SQLException {
        String sql = "INSERT INTO proyecto_habilidades (id_proyecto, id_habilidad) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            for (int idH : idHabilidades) {
                ps.setInt(1, idProyecto);
                ps.setInt(2, idH);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Buscar por ID
    public Proyecto buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.*, u.nombre as nombre_cliente, c.nombre as nombre_categoria " +
                     "FROM proyectos p JOIN usuarios u ON p.id_cliente = u.id " +
                     "JOIN categorias c ON p.id_categoria = c.id WHERE p.id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Proyecto proy = mapear(rs);
                proy.setHabilidades(obtenerHabilidades(id));
                return proy;
            }
        }
        return null;
    }

    // Listar proyectos ABIERTOS (para freelancers)
    public List<Proyecto> listarAbiertos(Integer idCategoria, Double presupMin, Double presupMax) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, u.nombre as nombre_cliente, c.nombre as nombre_categoria " +
            "FROM proyectos p JOIN usuarios u ON p.id_cliente = u.id " +
            "JOIN categorias c ON p.id_categoria = c.id WHERE p.estado = 'ABIERTO'");
        if (idCategoria != null) sql.append(" AND p.id_categoria = ").append(idCategoria);
        if (presupMin != null)   sql.append(" AND p.presupuesto_max >= ").append(presupMin);
        if (presupMax != null)   sql.append(" AND p.presupuesto_max <= ").append(presupMax);
        sql.append(" ORDER BY p.fecha_creacion DESC");

        List<Proyecto> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Listar proyectos de un cliente
    public List<Proyecto> listarPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT p.*, u.nombre as nombre_cliente, c.nombre as nombre_categoria " +
                     "FROM proyectos p JOIN usuarios u ON p.id_cliente = u.id " +
                     "JOIN categorias c ON p.id_categoria = c.id WHERE p.id_cliente = ? " +
                     "ORDER BY p.fecha_creacion DESC";
        List<Proyecto> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Cambiar estado del proyecto
    public boolean cambiarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE proyectos SET estado=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Editar proyecto
    public boolean editar(Proyecto p) throws SQLException {
        String sql = "UPDATE proyectos SET titulo=?, descripcion=?, id_categoria=?, presupuesto_max=?, fecha_limite=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, p.getTitulo());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getIdCategoria());
            ps.setDouble(4, p.getPresupuestoMax());
            ps.setDate(5, Date.valueOf(p.getFechaLimite()));
            ps.setInt(6, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Obtener habilidades de un proyecto
    public List<Habilidad> obtenerHabilidades(int idProyecto) throws SQLException {
        String sql = "SELECT h.*, c.nombre as nombre_categoria FROM habilidades h " +
                     "JOIN proyecto_habilidades ph ON h.id = ph.id_habilidad " +
                     "JOIN categorias c ON h.id_categoria = c.id WHERE ph.id_proyecto = ?";
        List<Habilidad> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
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

    private Proyecto mapear(ResultSet rs) throws SQLException {
        Proyecto p = new Proyecto();
        p.setId(rs.getInt("id"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setNombreCliente(rs.getString("nombre_cliente"));
        p.setTitulo(rs.getString("titulo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setIdCategoria(rs.getInt("id_categoria"));
        p.setNombreCategoria(rs.getString("nombre_categoria"));
        p.setPresupuestoMax(rs.getDouble("presupuesto_max"));
        p.setFechaLimite(rs.getDate("fecha_limite").toLocalDate());
        p.setEstado(rs.getString("estado"));
        p.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        return p;
    }
}
