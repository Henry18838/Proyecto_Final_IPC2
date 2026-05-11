import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ClienteService } from '../../../services/cliente.service';
import { ProyectoService } from '../../../services/proyecto.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-dashboard-cliente',
  imports: [RouterLink],
  templateUrl: './dashboard-cliente.html',
  styleUrl: './dashboard-cliente.css'
})
export class DashboardCliente implements OnInit {
  usuario: any;
  cliente: any;
  proyectos: any[] = [];

  constructor(
    private auth: AuthService,
    private router: Router,
    private clienteService: ClienteService,
    private proyectoService: ProyectoService
  ) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuario();
    this.clienteService.obtener(this.usuario.id).subscribe(data => this.cliente = data);
    this.proyectoService.listarPorCliente().subscribe(data => this.proyectos = data);
  }

  logout() {
    Swal.fire({
      title: '¿Cerrar sesión?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, salir',
      cancelButtonText: 'Cancelar'
    }).then(result => {
      if (result.isConfirmed) {
        this.auth.logout();
        this.router.navigate(['/login']);
      }
    });
  }

  proyectosActivos() {
    return this.proyectos.filter(p => !['COMPLETADO','CANCELADO'].includes(p.estado)).length;
  }
}