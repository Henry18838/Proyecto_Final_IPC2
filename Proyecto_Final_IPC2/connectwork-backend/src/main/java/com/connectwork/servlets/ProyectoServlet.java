package com.connectwork.servlets;

import com.connectwork.dao.ProyectoDAO;
import com.connectwork.models.Proyecto;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/proyectos/*")
public class ProyectoServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final ProyectoDAO proyectoDAO = new ProyectoDAO();

    // GET /api/proyectos          -> listar abiertos (freelancer)
    // GET /api/proyectos/cliente  -> listar por cliente
    // GET /api/proyectos/{id}     -> detalle
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
                // Filtros opcionales
                String catParam    = req.getParameter("categoria");
                String minParam    = req.getParameter("min");
                String maxParam    = req.getParameter("max");
                Integer idCategoria = catParam != null ? Integer.parseInt(catParam) : null;
                Double  presupMin   = minParam  != null ? Double.parseDouble(minParam) : null;
                Double  presupMax   = maxParam  != null ? Double.parseDouble(maxParam) : null;
                out.print(gson.toJson(proyectoDAO.listarAbiertos(idCategoria, presupMin, presupMax)));

            } else if ("/cliente".equals(path)) {
                int idCliente = JwtUtil.getUserId(token);
                out.print(gson.toJson(proyectoDAO.listarPorCliente(idCliente)));

            } else {
                int id = Integer.parseInt(path.substring(1));
                Proyecto p = proyectoDAO.buscarPorId(id);
                if (p == null) { resp.setStatus(404); out.print("{\"error\":\"No encontrado\"}"); return; }
                out.print(gson.toJson(p));
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // POST /api/proyectos -> crear proyecto
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }

            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            Proyecto p = new Proyecto();
            p.setIdCliente(JwtUtil.getUserId(token));
            p.setTitulo(body.get("titulo").getAsString());
            p.setDescripcion(body.get("descripcion").getAsString());
            p.setIdCategoria(body.get("idCategoria").getAsInt());
            p.setPresupuestoMax(body.get("presupuestoMax").getAsDouble());
            p.setFechaLimite(LocalDate.parse(body.get("fechaLimite").getAsString()));

            JsonArray hArr = body.getAsJsonArray("habilidades");
            List<Integer> ids = new ArrayList<>();
            hArr.forEach(h -> ids.add(h.getAsInt()));

            int id = proyectoDAO.crear(p, ids);
            resp.setStatus(201);
            out.print("{\"mensaje\":\"Proyecto creado\",\"id\":" + id + "}");

        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/proyectos/{id} -> editar proyecto
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            int id = Integer.parseInt(req.getPathInfo().substring(1));
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            Proyecto p = new Proyecto();
            p.setId(id);
            p.setTitulo(body.get("titulo").getAsString());
            p.setDescripcion(body.get("descripcion").getAsString());
            p.setIdCategoria(body.get("idCategoria").getAsInt());
            p.setPresupuestoMax(body.get("presupuestoMax").getAsDouble());
            p.setFechaLimite(LocalDate.parse(body.get("fechaLimite").getAsString()));
            proyectoDAO.editar(p);
            out.print("{\"mensaje\":\"Proyecto actualizado\"}");

        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // DELETE /api/proyectos/{id} -> cancelar proyecto
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            int id = Integer.parseInt(req.getPathInfo().substring(1));
            proyectoDAO.cambiarEstado(id, "CANCELADO");
            out.print("{\"mensaje\":\"Proyecto cancelado\"}");

        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}