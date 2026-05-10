import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username = '';
  password = '';
  cargando = false;

  constructor(private auth: AuthService, private router: Router) {}

  login() {
    if (!this.username || !this.password) {
      Swal.fire('Error', 'Ingresa usuario y contraseña', 'error');
      return;
    }
    this.cargando = true;
    this.auth.login(this.username, this.password).subscribe({
      next: (res) => {
        this.auth.guardarSesion(res);
        if (!res.perfilCompleto) {
          this.router.navigate(['/completar-perfil']);
        } else if (res.rol === 'CLIENTE') {
          this.router.navigate(['/cliente']);
        } else if (res.rol === 'FREELANCER') {
          this.router.navigate(['/freelancer']);
        } else if (res.rol === 'ADMIN') {
          this.router.navigate(['/admin']);
        }
      },
      error: (err) => {
        this.cargando = false;
        Swal.fire('Error', err.error?.error || 'Credenciales incorrectas', 'error');
      }
    });
  }
}