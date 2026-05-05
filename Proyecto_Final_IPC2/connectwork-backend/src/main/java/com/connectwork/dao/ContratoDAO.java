package com.connectwork.dao;

import com.connectwork.db.DatabaseConnection;
import com.connectwork.models.Contrato;
import com.connectwork.models.Entrega;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContratoDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crear contrato al aceptar propuesta
    public int crear(int idPropuesta, int idProyecto, int idCliente, int idFreelancer, double monto) throws SQLException {
        // Obtener comisión vigente
        int idComision = obtenerComisionVigente();
        String sql = "INSERT INTO contratos (id_propuesta, id_proyecto, id_cliente, id_freelancer, monto, id_comision) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPropuesta);
            ps.setInt(2, idProyecto);
            ps.setInt(3, idCliente);
            ps.setInt(4, idFreelancer);
            ps.setDouble(5, monto);
            ps.setInt(6, idComision);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    // Obtener ID de la comisión vigente
    private int obtenerComisionVigente() throws SQLException {
        String sql = "SELECT id FROM comisiones WHERE fecha_fin IS NULL ORDER BY fecha_inicio DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return 1;
    }

    // Buscar contrato por ID
    public Contrato buscarPorId(int id) throws SQLException {
        String sql = "SELECT con.*, p.titulo as titulo_proyecto, " +
                     "uc.nombre as nombre_cliente, uf.nombre as nombre_freelancer, com.porcentaje " +
                     "FROM contratos con JOIN proyectos p ON con.id_proyecto = p.id " +
                     "JOIN usuarios uc ON con.id_cliente = uc.id " +
                     "JOIN usuarios uf ON con.id_freelancer = uf.id " +
                     "JOIN comisiones com ON con.id_comision = com.id WHERE con.id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    // Listar contratos de un freelancer (EN_PROGRESO)
    public List<Contrato> listarActivosPorFreelancer(int idFreelancer) throws SQLException {
        String sql = "SELECT con.*, p.titulo as titulo_proyecto, " +
                     "uc.nombre as nombre_cliente, uf.nombre as nombre_freelancer, com.porcentaje " +
                     "FROM contratos con JOIN proyectos p ON con.id_proyecto = p.id " +
                     "JOIN usuarios uc ON con.id_cliente = uc.id " +
                     "JOIN usuarios uf ON con.id_freelancer = uf.id " +
                     "JOIN comisiones com ON con.id_comision = com.id " +
                     "WHERE con.id_freelancer = ? AND p.estado IN ('EN_PROGRESO','ENTREGA_PENDIENTE')";
        List<Contrato> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idFreelancer);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // Subir entrega
    public boolean subirEntrega(int idContrato, String descripcion, String archivos) throws SQLException {
        String sql = "INSERT INTO entregas (id_contrato, descripcion, archivos) VALUES (?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idContrato);
            ps.setString(2, descripcion);
            ps.setString(3, archivos);
            return ps.executeUpdate() > 0;
        }
    }

    // Obtener entregas de un contrato
    public List<Entrega> obtenerEntregas(int idContrato) throws SQLException {
        String sql = "SELECT * FROM entregas WHERE id_contrato = ? ORDER BY fecha_entrega DESC";
        List<Entrega> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idContrato);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Entrega e = new Entrega();
                e.setId(rs.getInt("id"));
                e.setIdContrato(rs.getInt("id_contrato"));
                e.setDescripcion(rs.getString("descripcion"));
                e.setArchivos(rs.getString("archivos"));
                e.setEstado(rs.getString("estado"));
                e.setMotivoRechazo(rs.getString("motivo_rechazo"));
                e.setFechaEntrega(rs.getTimestamp("fecha_entrega").toLocalDateTime());
                lista.add(e);
            }
        }
        return lista;
    }

    // Aprobar entrega más reciente
    public boolean aprobarEntrega(int idContrato) throws SQLException {
        String sql = "UPDATE entregas SET estado='APROBADA' WHERE id_contrato=? AND estado='PENDIENTE' ORDER BY fecha_entrega DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idContrato);
            return ps.executeUpdate() > 0;
        }
    }

    // Rechazar entrega más reciente
    public boolean rechazarEntrega(int idContrato, String motivo) throws SQLException {
        String sql = "UPDATE entregas SET estado='RECHAZADA', motivo_rechazo=? WHERE id_contrato=? AND estado='PENDIENTE' ORDER BY fecha_entrega DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, motivo);
            ps.setInt(2, idContrato);
            return ps.executeUpdate() > 0;
        }
    }

    // Completar contrato y liberar pago
    public boolean completar(int idContrato, int idCliente, int idFreelancer, double monto, double porcentaje) throws SQLException {
        Connection conn = getConn();
        double comision = monto * (porcentaje / 100);
        double pagoFreelancer = monto - comision;

        // Cerrar contrato
        String sqlContrato = "UPDATE contratos SET fecha_fin=NOW() WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlContrato)) {
            ps.setInt(1, idContrato);
            ps.executeUpdate();
        }
        // Abonar al freelancer
        String sqlFreelancer = "UPDATE usuarios SET saldo = saldo + ? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlFreelancer)) {
            ps.setDouble(1, pagoFreelancer);
            ps.setInt(2, idFreelancer);
            ps.executeUpdate();
        }
        // Registrar comisión en saldo_plataforma
        String sqlPlataforma = "INSERT INTO saldo_plataforma (id_contrato, monto) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlPlataforma)) {
            ps.setInt(1, idContrato);
            ps.setDouble(2, comision);
            return ps.executeUpdate() > 0;
        }
    }

    // Cancelar contrato y devolver saldo al cliente
    public boolean cancelar(int idContrato, int idCliente, double monto, String motivo) throws SQLException {
        Connection conn = getConn();
        // Devolver saldo al cliente
        String sqlSaldo = "UPDATE usuarios SET saldo = saldo + ? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlSaldo)) {
            ps.setDouble(1, monto);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
        // Actualizar contrato
        String sqlContrato = "UPDATE contratos SET fecha_fin=NOW(), motivo_cancelacion=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlContrato)) {
            ps.setString(1, motivo);
            ps.setInt(2, idContrato);
            return ps.executeUpdate() > 0;
        }
    }

    // Calificar freelancer
    public boolean calificar(int idContrato, int idCliente, int idFreelancer, int estrellas, String comentario) throws SQLException {
        String sql = "INSERT INTO calificaciones (id_contrato, id_cliente, id_freelancer, estrellas, comentario) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idContrato);
            ps.setInt(2, idCliente);
            ps.setInt(3, idFreelancer);
            ps.setInt(4, estrellas);
            ps.setString(5, comentario);
            return ps.executeUpdate() > 0;
        }
    }

    private Contrato mapear(ResultSet rs) throws SQLException {
        Contrato c = new Contrato();
        c.setId(rs.getInt("id"));
        c.setIdPropuesta(rs.getInt("id_propuesta"));
        c.setIdProyecto(rs.getInt("id_proyecto"));
        c.setTituloProyecto(rs.getString("titulo_proyecto"));
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombreCliente(rs.getString("nombre_cliente"));
        c.setIdFreelancer(rs.getInt("id_freelancer"));
        c.setNombreFreelancer(rs.getString("nombre_freelancer"));
        c.setMonto(rs.getDouble("monto"));
        c.setPorcentajeComision(rs.getDouble("porcentaje"));
        if (rs.getTimestamp("fecha_inicio") != null)
            c.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        if (rs.getTimestamp("fecha_fin") != null)
            c.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());
        c.setMotivoCancelacion(rs.getString("motivo_cancelacion"));
        return c;
    }
}
