import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CategoriaService } from '../../../services/categoria.service';
import { ClienteService } from '../../../services/cliente.service';
import { FreelancerService } from '../../../services/freelancer.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-completar-perfil',
  imports: [FormsModule],
  templateUrl: './completar-perfil.html',
  styleUrl: './completar-perfil.css'
})
export class CompletarPerfil implements OnInit {
  usuario: any;
  habilidades: any[] = [];
  habilidadesSeleccionadas: number[] = [];

  // Cliente
  clienteForm = { descripcion: '', sector: '', sitioWeb: '' };

  // Freelancer
  freelancerForm = { bio: '', experiencia: 'JUNIOR', tarifaHora: 0 };

  constructor(
    private auth: AuthService,
    private router: Router,
    private categoriaService: CategoriaService,
    private clienteService: ClienteService,
    private freelancerService: FreelancerService
  ) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuario();
    if (this.usuario?.rol === 'FREELANCER') {
      this.categoriaService.listarHabilidades().subscribe(data => this.habilidades = data);
    }
  }

  toggleHabilidad(id: number) {
    const idx = this.habilidadesSeleccionadas.indexOf(id);
    if (idx > -1) this.habilidadesSeleccionadas.splice(idx, 1);
    else this.habilidadesSeleccionadas.push(id);
  }

  estaSeleccionada(id: number): boolean {
    return this.habilidadesSeleccionadas.includes(id);
  }

  guardar() {
    if (this.usuario?.rol === 'CLIENTE') {
      if (!this.clienteForm.descripcion || !this.clienteForm.sector) {
        Swal.fire('Error', 'Completa descripción y sector', 'error'); return;
      }
      this.clienteService.completarPerfil(this.usuario.id, this.clienteForm).subscribe({
        next: () => {
          this.auth.actualizarPerfilCompleto();
          Swal.fire('¡Listo!', 'Perfil completado', 'success');
          this.router.navigate(['/cliente']);
        },
        error: () => Swal.fire('Error', 'No se pudo guardar', 'error')
      });
    } else if (this.usuario?.rol === 'FREELANCER') {
      if (!this.freelancerForm.bio || this.habilidadesSeleccionadas.length === 0) {
        Swal.fire('Error', 'Completa tu bio y selecciona al menos una habilidad', 'error'); return;
      }
      const data = { ...this.freelancerForm, habilidades: this.habilidadesSeleccionadas };
      this.freelancerService.completarPerfil(this.usuario.id, data).subscribe({
        next: () => {
          this.auth.actualizarPerfilCompleto();
          Swal.fire('¡Listo!', 'Perfil completado', 'success');
          this.router.navigate(['/freelancer']);
        },
        error: () => Swal.fire('Error', 'No se pudo guardar', 'error')
      });
    }
  }
}