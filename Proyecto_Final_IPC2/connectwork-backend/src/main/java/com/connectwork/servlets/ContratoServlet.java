package com.connectwork.servlets;

import com.connectwork.dao.ContratoDAO;
import com.connectwork.dao.ProyectoDAO;
import com.connectwork.models.Contrato;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/contratos/*")
public class ContratoServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();

    // GET /api/contratos/{id}          -> detalle del contrato
    // GET /api/contratos/{id}/entregas -> entregas del contrato
    // GET /api/contratos/freelancer    -> contratos activos del freelancer
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            String path = req.getPathInfo();

            if ("/freelancer".equals(path)) {
                int idFreelancer = JwtUtil.getUserId(token);
                List<Contrato> lista = contratoDAO.listarActivosPorFreelancer(idFreelancer);
                out.print(gson.toJson(lista));

            } else if (path.endsWith("/entregas")) {
                int idContrato = Integer.parseInt(path.split("/")[1]);
                out.print(gson.toJson(contratoDAO.obtenerEntregas(idContrato)));

            } else {
                int idContrato = Integer.parseInt(path.substring(1));
                Contrato c = contratoDAO.buscarPorId(idContrato);
                if (c == null) { resp.setStatus(404); out.print("{\"error\":\"No encontrado\"}"); return; }
                out.print(gson.toJson(c));
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // POST /api/contratos/{id}/entrega -> freelancer sube entrega
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            String[] parts = req.getPathInfo().split("/");
            int idContrato = Integer.parseInt(parts[1]);
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            String descripcion = body.get("descripcion").getAsString();
            String archivos    = body.has("archivos") ? body.get("archivos").getAsString() : "";

            contratoDAO.subirEntrega(idContrato, descripcion, archivos);

            // Cambiar estado del proyecto a ENTREGA_PENDIENTE
            Contrato c = contratoDAO.buscarPorId(idContrato);
            proyectoDAO.cambiarEstado(c.getIdProyecto(), "ENTREGA_PENDIENTE");

            resp.setStatus(201);
            out.print("{\"mensaje\":\"Entrega subida correctamente\"}");

        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/contratos/{id}/aprobar   -> cliente aprueba entrega
    // PUT /api/contratos/{id}/rechazar  -> cliente rechaza entrega
    // PUT /api/contratos/{id}/cancelar  -> cliente cancela contrato
    // PUT /api/contratos/{id}/calificar -> cliente califica freelancer
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            String[] parts  = req.getPathInfo().split("/");
            int idContrato  = Integer.parseInt(parts[1]);
            String accion   = parts[2];
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            Contrato c      = contratoDAO.buscarPorId(idContrato);

            if (c == null) { resp.setStatus(404); out.print("{\"error\":\"Contrato no encontrado\"}"); return; }

            switch (accion) {
                case "aprobar" -> {
                    contratoDAO.aprobarEntrega(idContrato);
                    contratoDAO.completar(idContrato, c.getIdCliente(), c.getIdFreelancer(),
                                         c.getMonto(), c.getPorcentajeComision());
                    proyectoDAO.cambiarEstado(c.getIdProyecto(), "COMPLETADO");
                    out.print("{\"mensaje\":\"Entrega aprobada, pago liberado\"}");
                }
                case "rechazar" -> {
                    String motivo = body.get("motivo").getAsString();
                    contratoDAO.rechazarEntrega(idContrato, motivo);
                    proyectoDAO.cambiarEstado(c.getIdProyecto(), "EN_PROGRESO");
                    out.print("{\"mensaje\":\"Entrega rechazada\"}");
                }
                case "cancelar" -> {
                    String motivo = body.get("motivo").getAsString();
                    contratoDAO.cancelar(idContrato, c.getIdCliente(), c.getMonto(), motivo);
                    proyectoDAO.cambiarEstado(c.getIdProyecto(), "CANCELADO");
                    out.print("{\"mensaje\":\"Contrato cancelado, saldo devuelto\"}");
                }
                case "calificar" -> {
                    int estrellas     = body.get("estrellas").getAsInt();
                    String comentario = body.has("comentario") ? body.get("comentario").getAsString() : "";
                    int idCliente     = JwtUtil.getUserId(token);
                    contratoDAO.calificar(idContrato, idCliente, c.getIdFreelancer(), estrellas, comentario);
                    out.print("{\"mensaje\":\"Calificación guardada\"}");
                }
                default -> { resp.setStatus(400); out.print("{\"error\":\"Acción desconocida\"}"); }
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}