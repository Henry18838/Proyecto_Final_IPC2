package com.connectwork.servlets;

import com.connectwork.dao.CategoriaDAO;
import com.connectwork.models.Categoria;
import com.connectwork.models.Habilidad;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/categorias/*")
public class CategoriaServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // GET /api/categorias             -> listar activas
    // GET /api/categorias/todas       -> listar todas (admin)
    // GET /api/categorias/habilidades -> todas las habilidades
    // GET /api/categorias/{id}/habilidades -> habilidades de una categoría
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String path = req.getPathInfo();
            if (path == null || "/".equals(path)) {
                out.print(gson.toJson(categoriaDAO.listarActivas()));
            } else if ("/todas".equals(path)) {
                out.print(gson.toJson(categoriaDAO.listarTodas()));
            } else if ("/habilidades".equals(path)) {
                out.print(gson.toJson(categoriaDAO.listarTodasHabilidades()));
            } else if (path.endsWith("/habilidades")) {
                int id = Integer.parseInt(path.split("/")[1]);
                out.print(gson.toJson(categoriaDAO.listarHabilidades(id)));
            } else {
                resp.setStatus(404); out.print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // POST /api/categorias            -> crear categoría
    // POST /api/categorias/habilidad  -> crear habilidad
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String token = JwtUtil.extraerToken(req.getHeader("Authorization"));

        try {
            if (token == null || !JwtUtil.esValido(token)) {
                resp.setStatus(401); out.print("{\"error\":\"Token inválido\"}"); return;
            }
            String path  = req.getPathInfo();
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            if (path == null || "/".equals(path)) {
                String nombre = body.get("nombre").getAsString();
                categoriaDAO.crear(nombre);
                resp.setStatus(201);
                out.print("{\"mensaje\":\"Categoría creada\"}");
            } else if ("/habilidad".equals(path)) {
                String nombre      = body.get("nombre").getAsString();
                String descripcion = body.has("descripcion") ? body.get("descripcion").getAsString() : "";
                int idCategoria    = body.get("idCategoria").getAsInt();
                categoriaDAO.crearHabilidad(nombre, descripcion, idCategoria);
                resp.setStatus(201);
                out.print("{\"mensaje\":\"Habilidad creada\"}");
            } else {
                resp.setStatus(404); out.print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // PUT /api/categorias/{id}          -> editar
    // PUT /api/categorias/{id}/toggle   -> activar/desactivar
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

            if (parts.length > 2 && "toggle".equals(parts[2])) {
                boolean activa = body.get("activa").getAsBoolean();
                categoriaDAO.toggleActiva(id, activa);
                out.print("{\"mensaje\":\"Estado actualizado\"}");
            } else {
                String nombre = body.get("nombre").getAsString();
                categoriaDAO.editar(id, nombre);
                out.print("{\"mensaje\":\"Categoría actualizada\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500); out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}