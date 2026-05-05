package com.connectwork.servlets;

import com.connectwork.dao.ClienteDAO;
import com.connectwork.dao.FreelancerDAO;
import com.connectwork.dao.UsuarioDAO;
import com.connectwork.models.Usuario;
import com.connectwork.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final FreelancerDAO freelancerDAO = new FreelancerDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo(); // /registro o /login

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

            if ("/registro".equals(path)) {
                handleRegistro(body, resp, out);
            } else if ("/login".equals(path)) {
                handleLogin(body, resp, out);
            } else {
                resp.setStatus(404);
                out.print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleRegistro(JsonObject body, HttpServletResponse resp, PrintWriter out) throws Exception {
        String rol = body.get("rol").getAsString(); // CLIENTE o FREELANCER

        // Validar username y email únicos
        String username = body.get("username").getAsString();
        String email    = body.get("email").getAsString();

        if (usuarioDAO.existeUsername(username)) {
            resp.setStatus(400);
            out.print("{\"error\":\"El nombre de usuario ya existe\"}");
            return;
        }
        if (usuarioDAO.existeEmail(email)) {
            resp.setStatus(400);
            out.print("{\"error\":\"El correo ya está registrado\"}");
            return;
        }

        Usuario u = new Usuario();
        u.setNombre(body.get("nombre").getAsString());
        u.setUsername(username);
        u.setPasswordHash(body.get("password").getAsString());
        u.setEmail(email);
        u.setTelefono(body.has("telefono") ? body.get("telefono").getAsString() : null);
        u.setDireccion(body.has("direccion") ? body.get("direccion").getAsString() : null);
        u.setCui(body.has("cui") ? body.get("cui").getAsString() : null);
        if (body.has("fechaNac") && !body.get("fechaNac").isJsonNull())
            u.setFechaNac(LocalDate.parse(body.get("fechaNac").getAsString()));
        u.setRol(rol);

        int id = usuarioDAO.registrar(u);
        if (id < 0) {
            resp.setStatus(500);
            out.print("{\"error\":\"Error al registrar usuario\"}");
            return;
        }

        // Crear registro específico según rol
        if ("CLIENTE".equals(rol))    clienteDAO.crear(id);
        if ("FREELANCER".equals(rol)) freelancerDAO.crear(id);

        resp.setStatus(201);
        out.print("{\"mensaje\":\"Usuario registrado correctamente\",\"id\":" + id + "}");
    }

    private void handleLogin(JsonObject body, HttpServletResponse resp, PrintWriter out) throws Exception {
        String username = body.get("username").getAsString();
        String password = body.get("password").getAsString();

        Usuario u = usuarioDAO.login(username, password);
        if (u == null) {
            resp.setStatus(401);
            out.print("{\"error\":\"Credenciales incorrectas o cuenta desactivada\"}");
            return;
        }

        String token = JwtUtil.generarToken(u.getId(), u.getUsername(), u.getRol());

        JsonObject respJson = new JsonObject();
        respJson.addProperty("token", token);
        respJson.addProperty("id", u.getId());
        respJson.addProperty("nombre", u.getNombre());
        respJson.addProperty("username", u.getUsername());
        respJson.addProperty("rol", u.getRol());
        respJson.addProperty("perfilCompleto", u.isPerfilCompleto());
        respJson.addProperty("saldo", u.getSaldo());

        out.print(gson.toJson(respJson));
    }
}