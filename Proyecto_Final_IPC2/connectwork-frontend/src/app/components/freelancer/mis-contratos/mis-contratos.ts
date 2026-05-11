import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SlicePipe } from '@angular/common';
import { ContratoService } from '../../../services/contrato.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-mis-contratos',
  imports: [FormsModule, RouterLink, SlicePipe],
  templateUrl: './mis-contratos.html',
  styleUrl: './mis-contratos.css'
})
export class MisContratos implements OnInit {
  contratos: any[] = [];
  contratoSeleccionado: any = null;
  entregas: any[] = [];
  entregaForm = { descripcion: '', archivos: '' };

  constructor(private contratoService: ContratoService) {}

  ngOnInit() {
    this.contratoService.listarActivosFreelancer().subscribe(data => this.contratos = data);
  }

  verContrato(contrato: any) {
    this.contratoSeleccionado = contrato;
    this.contratoService.obtenerEntregas(contrato.id).subscribe(data => this.entregas = data);
  }

  subirEntrega() {
    if (!this.entregaForm.descripcion) {
      Swal.fire('Error', 'Agrega una descripción', 'error'); return;
    }
    this.contratoService.subirEntrega(this.contratoSeleccionado.id, this.entregaForm).subscribe({
      next: () => {
        Swal.fire('¡Éxito!', 'Entrega subida correctamente', 'success');
        this.entregaForm = { descripcion: '', archivos: '' };
        this.verContrato(this.contratoSeleccionado);
      },
      error: () => Swal.fire('Error', 'No se pudo subir la entrega', 'error')
    });
  }

  badgeClass(estado: string) {
    if (estado === 'APROBADA') return 'badge-success';
    if (estado === 'RECHAZADA') return 'badge-danger';
    return 'badge-warning';
  }
}