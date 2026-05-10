import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class Registro {
  form = {
    nombre: '',
    username: '',
    password: '',
    email: '',
    telefono: '',
    direccion: '',
    cui: '',
    fechaNac: '',
    rol: 'CLIENTE'
  };
  cargando = false;

  constructor(private auth: AuthService, private router: Router) {}

  registrar() {
    if (!this.form.nombre || !this.form.username || !this.form.password || !this.form.email) {
      Swal.fire('Error', 'Completa los campos obligatorios', 'error');
      return;
    }
    this.cargando = true;
    this.auth.registro(this.form).subscribe({
      next: () => {
        Swal.fire('¡Registro exitoso!', 'Ahora puedes iniciar sesión', 'success');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.cargando = false;
        Swal.fire('Error', err.error?.error || 'No se pudo registrar', 'error');
      }
    });
  }
}