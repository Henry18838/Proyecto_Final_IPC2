import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { FreelancerService } from '../../../services/freelancer.service';
import { ContratoService } from '../../../services/contrato.service';
import { PropuestaService } from '../../../services/propuesta.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-dashboard-freelancer',
  imports: [RouterLink],
  templateUrl: './dashboard-freelancer.html',
  styleUrl: './dashboard-freelancer.css'
})
export class DashboardFreelancer implements OnInit {
  usuario: any;
  freelancer: any;
  contratosActivos: any[] = [];
  propuestas: any[] = [];

  constructor(
    private auth: AuthService,
    private router: Router,
    private freelancerService: FreelancerService,
    private contratoService: ContratoService,
    private propuestaService: PropuestaService
  ) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuario();
    this.freelancerService.obtener(this.usuario.id).subscribe(data => this.freelancer = data);
    this.contratoService.listarActivosFreelancer().subscribe(data => this.contratosActivos = data);
    this.propuestaService.listarMisPropuestas().subscribe(data => this.propuestas = data);
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
}