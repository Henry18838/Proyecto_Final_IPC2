package com.connectwork.dao;

import com.connectwork.db.DatabaseConnection;
import com.connectwork.models.Categoria;
import com.connectwork.models.Habilidad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Listar categorías activas
    public List<Categoria> listarActivas() throws SQLException {
        String sql = "SELECT * FROM categorias WHERE activa = TRUE ORDER BY nombre";
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Listar todas (para admin)
    public List<Categoria> listarTodas() throws SQLException {
        String sql = "SELECT * FROM categorias ORDER BY nombre";
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Crear categoría
    public boolean crear(String nombre) throws SQLException {
        String sql = "INSERT INTO categorias (nombre) VALUES (?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        }
    }

    // Editar categoría
    public boolean editar(int id, String nombre) throws SQLException {
        String sql = "UPDATE categorias SET nombre=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Activar/desactivar categoría
    public boolean toggleActiva(int id, boolean activa) throws SQLException {
        String sql = "UPDATE categorias SET activa=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, activa);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Listar habilidades por categoría
    public List<Habilidad> listarHabilidades(int idCategoria) throws SQLException {
        String sql = "SELECT h.*, c.nombre as nombre_categoria FROM habilidades h " +
                     "JOIN categorias c ON h.id_categoria = c.id " +
                     "WHERE h.id_categoria = ? AND h.activa = TRUE ORDER BY h.nombre";
        List<Habilidad> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Habilidad h = new Habilidad();
                h.setId(rs.getInt("id"));
                h.setNombre(rs.getString("nombre"));
                h.setDescripcion(rs.getString("descripcion"));
                h.setIdCategoria(rs.getInt("id_categoria"));
                h.setNombreCategoria(rs.getString("nombre_categoria"));
                h.setActiva(rs.getBoolean("activa"));
                lista.add(h);
            }
        }
        return lista;
    }

    // Listar todas las habilidades activas
    public List<Habilidad> listarTodasHabilidades() throws SQLException {
        String sql = "SELECT h.*, c.nombre as nombre_categoria FROM habilidades h " +
                     "JOIN categorias c ON h.id_categoria = c.id WHERE h.activa = TRUE ORDER BY h.nombre";
        List<Habilidad> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Habilidad h = new Habilidad();
                h.setId(rs.getInt("id"));
                h.setNombre(rs.getString("nombre"));
                h.setDescripcion(rs.getString("descripcion"));
                h.setIdCategoria(rs.getInt("id_categoria"));
                h.setNombreCategoria(rs.getString("nombre_categoria"));
                h.setActiva(rs.getBoolean("activa"));
                lista.add(h);
            }
        }
        return lista;
    }

    // Crear habilidad
    public boolean crearHabilidad(String nombre, String descripcion, int idCategoria) throws SQLException {
        String sql = "INSERT INTO habilidades (nombre, descripcion, id_categoria) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setInt(3, idCategoria);
            return ps.executeUpdate() > 0;
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setActiva(rs.getBoolean("activa"));
        return c;
    }
}

