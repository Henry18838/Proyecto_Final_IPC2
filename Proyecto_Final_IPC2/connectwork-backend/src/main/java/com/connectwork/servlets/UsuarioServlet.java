package com.connectwork.servlets;

import com.connectwork.dao.UsuarioDAO;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/usuarios/*")
public class UsuarioServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // PUT /api/usuarios/{id}/toggle -> activar/desactivar usuario
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            if (!"ADMIN".equals(JwtUtil.getRol(token))) {
                resp.setStatus(403); out.print("{\"error\":\"Sin permisos\"}"); return;
            }

            String[] parts = req.getPathInfo().split("/");
            int id = Integer.parseInt(parts[1]);
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            boolean activo = body.get("activo").getAsBoolean();

            usuarioDAO.toggleActivo(id, activo);
            out.print("{\"mensaje\":\"Usuario actualizado\"}");

        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}