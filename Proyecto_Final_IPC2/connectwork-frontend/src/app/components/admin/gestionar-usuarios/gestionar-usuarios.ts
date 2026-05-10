import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-gestionar-usuarios',
  imports: [RouterLink],
  templateUrl: './gestionar-usuarios.html',
  styleUrl: './gestionar-usuarios.css'
})
export class GestionarUsuarios implements OnInit {
  clientes: any[] = [];
  freelancers: any[] = [];
  vista = 'clientes';
  private apiUrl = 'http://localhost:8080/connectwork-backend/api';

  constructor(private auth: AuthService, private http: HttpClient) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  ngOnInit() {
    this.cargarClientes();
    this.cargarFreelancers();
  }

  cargarClientes() {
    this.http.get<any[]>(`${this.apiUrl}/clientes`, { headers: this.headers() })
      .subscribe(data => this.clientes = data);
  }

  cargarFreelancers() {
    this.http.get<any[]>(`${this.apiUrl}/freelancers`, { headers: this.headers() })
      .subscribe(data => this.freelancers = data);
  }

  toggleUsuario(id: number, activo: boolean, tipo: string) {
    const accion = activo ? 'desactivar' : 'activar';
    Swal.fire({
      title: `¿${accion.charAt(0).toUpperCase() + accion.slice(1)} usuario?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí',
      cancelButtonText: 'No'
    }).then(result => {
      if (result.isConfirmed) {
        this.http.put(`${this.apiUrl}/usuarios/${id}/toggle`, { activo: !activo }, { headers: this.headers() })
          .subscribe({
            next: () => {
              Swal.fire('¡Listo!', `Usuario ${accion}do`, 'success');
              if (tipo === 'cliente') this.cargarClientes();
              else this.cargarFreelancers();
            },
            error: () => Swal.fire('Error', 'No se pudo actualizar', 'error')
          });
      }
    });
  }
}