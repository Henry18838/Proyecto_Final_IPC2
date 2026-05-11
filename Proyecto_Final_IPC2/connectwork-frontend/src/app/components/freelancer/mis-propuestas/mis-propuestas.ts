import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SlicePipe } from '@angular/common';
import { PropuestaService } from '../../../services/propuesta.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-mis-propuestas',
  imports: [RouterLink, SlicePipe],
  templateUrl: './mis-propuestas.html',
  styleUrl: './mis-propuestas.css'
})
export class MisPropuestas implements OnInit {
  propuestas: any[] = [];

  constructor(private propuestaService: PropuestaService) {}

  ngOnInit() {
    this.propuestaService.listarMisPropuestas().subscribe(data => this.propuestas = data);
  }

  retirar(id: number) {
    Swal.fire({ title: '¿Retirar propuesta?', icon: 'question', showCancelButton: true, confirmButtonText: 'Sí' })
      .then(r => {
        if (r.isConfirmed) {
          this.propuestaService.retirar(id).subscribe({
            next: () => { Swal.fire('¡Listo!', 'Propuesta retirada', 'success'); this.ngOnInit(); },
            error: () => Swal.fire('Error', 'No se pudo retirar', 'error')
          });
        }
      });
  }

  badgeClass(estado: string) {
    if (estado === 'ACEPTADA') return 'badge-success';
    if (estado === 'RECHAZADA' || estado === 'RETIRADA') return 'badge-danger';
    return 'badge-warning';
  }
}