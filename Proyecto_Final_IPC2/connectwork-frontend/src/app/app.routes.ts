import { Routes } from '@angular/router';
import { Login } from './components/auth/login/login';
import { Registro } from './components/auth/registro/registro';
import { CompletarPerfil } from './components/auth/completar-perfil/completar-perfil';
import { DashboardCliente } from './components/cliente/dashboard-cliente/dashboard-cliente';
import { MisProyectos } from './components/cliente/mis-proyectos/mis-proyectos';
import { CrearProyecto } from './components/cliente/crear-proyecto/crear-proyecto';
import { VerPropuestas } from './components/cliente/ver-propuestas/ver-propuestas';
import { DashboardFreelancer } from './components/freelancer/dashboard-freelancer/dashboard-freelancer';
import { ExplorarProyectos } from './components/freelancer/explorar-proyectos/explorar-proyectos';
import { MisPropuestas } from './components/freelancer/mis-propuestas/mis-propuestas';
import { MisContratos } from './components/freelancer/mis-contratos/mis-contratos';
import { DashboardAdmin } from './components/admin/dashboard-admin/dashboard-admin';
import { GestionarCategorias } from './components/admin/gestionar-categorias/gestionar-categorias';
import { GestionarUsuarios } from './components/admin/gestionar-usuarios/gestionar-usuarios';
import { authGuard } from './guards/auth.guard';
import { clienteGuard } from './guards/cliente.guard';
import { freelancerGuard } from './guards/freelancer.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'completar-perfil', component: CompletarPerfil, canActivate: [authGuard] },

  { path: 'cliente', component: DashboardCliente, canActivate: [authGuard, clienteGuard] },
  { path: 'cliente/proyectos', component: MisProyectos, canActivate: [authGuard, clienteGuard] },
  { path: 'cliente/proyectos/crear', component: CrearProyecto, canActivate: [authGuard, clienteGuard] },
  { path: 'cliente/proyectos/:id/propuestas', component: VerPropuestas, canActivate: [authGuard, clienteGuard] },

  { path: 'freelancer', component: DashboardFreelancer, canActivate: [authGuard, freelancerGuard] },
  { path: 'freelancer/explorar', component: ExplorarProyectos, canActivate: [authGuard, freelancerGuard] },
  { path: 'freelancer/propuestas', component: MisPropuestas, canActivate: [authGuard, freelancerGuard] },
  { path: 'freelancer/contratos', component: MisContratos, canActivate: [authGuard, freelancerGuard] },

  { path: 'admin', component: DashboardAdmin, canActivate: [authGuard, adminGuard] },
  { path: 'admin/categorias', component: GestionarCategorias, canActivate: [authGuard, adminGuard] },
  { path: 'admin/usuarios', component: GestionarUsuarios, canActivate: [authGuard, adminGuard] },

  { path: '**', redirectTo: '/login' }
];