package com.connectwork.servlets;

import com.connectwork.dao.FreelancerDAO;
import com.connectwork.dao.UsuarioDAO;
import com.connectwork.models.Freelancer;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/freelancers/*")
public class FreelancerServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final FreelancerDAO freelancerDAO = new FreelancerDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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
            if (path == null || "/".equals(path)) {
                out.print(gson.toJson(freelancerDAO.listarTodos()));
            } else {
                int id = Integer.parseInt(path.substring(1));
                Freelancer f = freelancerDAO.buscarPorId(id);
                if (f == null) { resp.setStatus(404); out.print("{\"error\":\"No encontrado\"}"); return; }
                out.print(gson.toJson(f));
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/freelancers/{id}/perfil
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
            int id = Integer.parseInt(parts[1]);
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            String bio        = body.get("bio").getAsString();
            String experiencia = body.get("experiencia").getAsString();
            double tarifaHora  = body.get("tarifaHora").getAsDouble();
            JsonArray hArr     = body.getAsJsonArray("habilidades");
            List<Integer> ids  = new ArrayList<>();
            hArr.forEach(h -> ids.add(h.getAsInt()));

            freelancerDAO.completarInfo(id, bio, experiencia, tarifaHora, ids);
            usuarioDAO.completarPerfil(id);
            out.print("{\"mensaje\":\"Perfil actualizado\"}");

        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
