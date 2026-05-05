package com.connectwork.servlets;

import com.connectwork.dao.ClienteDAO;
import com.connectwork.dao.UsuarioDAO;
import com.connectwork.models.Cliente;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/clientes/*")
public class ClienteServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // GET /api/clientes          -> listar todos (admin)
    // GET /api/clientes/{id}     -> obtener por id
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401);
                out.print("{\"error\":\"Token inválido\"}");
                return;
            }

            String path = req.getPathInfo();
            if (path == null || "/".equals(path)) {
                List<Cliente> lista = clienteDAO.listarTodos();
                out.print(gson.toJson(lista));
            } else {
                int id = Integer.parseInt(path.substring(1));
                Cliente c = clienteDAO.buscarPorId(id);
                if (c == null) { resp.setStatus(404); out.print("{\"error\":\"No encontrado\"}"); return; }
                out.print(gson.toJson(c));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/clientes/{id}/perfil  -> completar info inicial
    // PUT /api/clientes/{id}/recargar -> recargar saldo
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }

            String path = req.getPathInfo(); // /{id}/perfil o /{id}/recargar
            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);
            String accion = parts.length > 2 ? parts[2] : "";
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            if ("perfil".equals(accion)) {
                String descripcion = body.has("descripcion") ? body.get("descripcion").getAsString() : "";
                String sector      = body.has("sector")      ? body.get("sector").getAsString()      : "";
                String sitioWeb    = body.has("sitioWeb")    ? body.get("sitioWeb").getAsString()    : "";
                clienteDAO.completarInfo(id, descripcion, sector, sitioWeb);
                usuarioDAO.completarPerfil(id);
                out.print("{\"mensaje\":\"Perfil completado\"}");

            } else if ("recargar".equals(accion)) {
                double monto = body.get("monto").getAsDouble();
                if (monto <= 0) { resp.setStatus(400); out.print("{\"error\":\"Monto inválido\"}"); return; }
                clienteDAO.recargarSaldo(id, monto);
                out.print("{\"mensaje\":\"Saldo recargado correctamente\"}");

            } else {
                resp.setStatus(400); out.print("{\"error\":\"Acción desconocida\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}