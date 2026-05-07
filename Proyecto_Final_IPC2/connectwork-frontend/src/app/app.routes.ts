import { Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { RegistroComponent } from './components/auth/registro/registro.component';
import { CompletarPerfilComponent } from './components/auth/completar-perfil/completar-perfil.component';
import { DashboardClienteComponent } from './components/cliente/dashboard-cliente/dashboard-cliente.component';
import { MisProyectosComponent } from './components/cliente/mis-proyectos/mis-proyectos.component';
import { CrearProyectoComponent } from './components/cliente/crear-proyecto/crear-proyecto.component';
import { VerPropuestasComponent } from './components/cliente/ver-propuestas/ver-propuestas.component';
import { DashboardFreelancerComponent } from './components/freelancer/dashboard-freelancer/dashboard-freelancer.component';
import { ExplorarProyectosComponent } from './components/freelancer/explorar-proyectos/explorar-proyectos.component';
import { MisPropuestasComponent } from './components/freelancer/mis-propuestas/mis-propuestas.component';
import { MisContratosComponent } from './components/freelancer/mis-contratos/mis-contratos.component';
import { DashboardAdminComponent } from './components/admin/dashboard-admin/dashboard-admin.component';
import { GestionarCategoriasComponent } from './components/admin/gestionar-categorias/gestionar-categorias.component';
import { GestionarUsuariosComponent } from './components/admin/gestionar-usuarios/gestionar-usuarios.component';
import { authGuard } from './guards/auth.guard';
import { clienteGuard } from './guards/cliente.guard';
import { freelancerGuard } from './guards/freelancer.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'completar-perfil', component: CompletarPerfilComponent, canActivate: [authGuard] },

  // Cliente
  { path: 'cliente', component: DashboardClienteComponent, canActivate: [authGuard, clienteGuard] },
  { path: 'cliente/proyectos', component: MisProyectosComponent, canActivate: [authGuard, clienteGuard] },
  { path: 'cliente/proyectos/crear', component: CrearProyectoComponent, canActivate: [authGuard, clienteGuard] },
  { path: 'cliente/proyectos/:id/propuestas', component: VerPropuestasComponent, canActivate: [authGuard, clienteGuard] },

  // Freelancer
  { path: 'freelancer', component: DashboardFreelancerComponent, canActivate: [authGuard, freelancerGuard] },
  { path: 'freelancer/explorar', component: ExplorarProyectosComponent, canActivate: [authGuard, freelancerGuard] },
  { path: 'freelancer/propuestas', component: MisPropuestasComponent, canActivate: [authGuard, freelancerGuard] },
  { path: 'freelancer/contratos', component: MisContratosComponent, canActivate: [authGuard, freelancerGuard] },

  // Admin
  { path: 'admin', component: DashboardAdminComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/categorias', component: GestionarCategoriasComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/usuarios', component: GestionarUsuariosComponent, canActivate: [authGuard, adminGuard] },

  { path: '**', redirectTo: '/login' }
];