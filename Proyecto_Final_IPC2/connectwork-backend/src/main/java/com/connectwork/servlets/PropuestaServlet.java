package com.connectwork.servlets;

import com.connectwork.dao.ContratoDAO;
import com.connectwork.dao.PropuestaDAO;
import com.connectwork.dao.ProyectoDAO;
import com.connectwork.dao.UsuarioDAO;
import com.connectwork.models.Propuesta;
import com.connectwork.models.Proyecto;
import com.connectwork.models.Usuario;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/propuestas/*")
public class PropuestaServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final PropuestaDAO propuestaDAO = new PropuestaDAO();
    private final ProyectoDAO proyectoDAO   = new ProyectoDAO();
    private final ContratoDAO contratoDAO   = new ContratoDAO();
    private final UsuarioDAO usuarioDAO     = new UsuarioDAO();

    // GET /api/propuestas/proyecto/{idProyecto}   -> propuestas de un proyecto
    // GET /api/propuestas/freelancer              -> mis propuestas
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
            if (path.startsWith("/proyecto/")) {
                int idProyecto = Integer.parseInt(path.split("/")[2]);
                out.print(gson.toJson(propuestaDAO.listarPorProyecto(idProyecto)));
            } else if ("/freelancer".equals(path)) {
                int idFreelancer = JwtUtil.getUserId(token);
                out.print(gson.toJson(propuestaDAO.listarPorFreelancer(idFreelancer)));
            } else {
                resp.setStatus(404); out.print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // POST /api/propuestas -> enviar propuesta
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            int idFreelancer = JwtUtil.getUserId(token);
            JsonObject body  = gson.fromJson(req.getReader(), JsonObject.class);
            int idProyecto   = body.get("idProyecto").getAsInt();

            if (propuestaDAO.yaEnvio(idProyecto, idFreelancer)) {
                resp.setStatus(400); out.print("{\"error\":\"Ya enviaste una propuesta a este proyecto\"}"); return;
            }

            Propuesta p = new Propuesta();
            p.setIdProyecto(idProyecto);
            p.setIdFreelancer(idFreelancer);
            p.setMontoOfertado(body.get("montoOfertado").getAsDouble());
            p.setPlazoDias(body.get("plazoDias").getAsInt());
            p.setCarta(body.get("carta").getAsString());
            propuestaDAO.enviar(p);
            resp.setStatus(201);
            out.print("{\"mensaje\":\"Propuesta enviada\"}");

        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/propuestas/{id}/aceptar  -> cliente acepta propuesta
    // PUT /api/propuestas/{id}/rechazar -> cliente rechaza propuesta
    // PUT /api/propuestas/{id}/retirar  -> freelancer retira propuesta
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            String[] parts = req.getPathInfo().split("/");
            int idPropuesta = Integer.parseInt(parts[1]);
            String accion   = parts[2];
            Propuesta prop  = propuestaDAO.buscarPorId(idPropuesta);
            if (prop == null) { resp.setStatus(404); out.print("{\"error\":\"Propuesta no encontrada\"}"); return; }

            if ("aceptar".equals(accion)) {
                int idCliente = JwtUtil.getUserId(token);
                Usuario cliente = usuarioDAO.buscarPorId(idCliente);

                // Verificar saldo suficiente
                if (cliente.getSaldo() < prop.getMontoOfertado()) {
                    resp.setStatus(400); out.print("{\"error\":\"Saldo insuficiente\"}"); return;
                }

                // Bloquear saldo del cliente
                usuarioDAO.actualizarSaldo(idCliente, cliente.getSaldo() - prop.getMontoOfertado());

                // Crear contrato
                Proyecto proyecto = proyectoDAO.buscarPorId(prop.getIdProyecto());
                contratoDAO.crear(idPropuesta, prop.getIdProyecto(), idCliente, prop.getIdFreelancer(), prop.getMontoOfertado());

                // Cambiar estados
                propuestaDAO.cambiarEstado(idPropuesta, "ACEPTADA");
                proyectoDAO.cambiarEstado(prop.getIdProyecto(), "EN_PROGRESO");

                out.print("{\"mensaje\":\"Propuesta aceptada, contrato creado\"}");

            } else if ("rechazar".equals(accion)) {
                propuestaDAO.cambiarEstado(idPropuesta, "RECHAZADA");
                out.print("{\"mensaje\":\"Propuesta rechazada\"}");

            } else if ("retirar".equals(accion)) {
                propuestaDAO.cambiarEstado(idPropuesta, "RETIRADA");
                out.print("{\"mensaje\":\"Propuesta retirada\"}");

            } else {
                resp.setStatus(400); out.print("{\"error\":\"Acción desconocida\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}