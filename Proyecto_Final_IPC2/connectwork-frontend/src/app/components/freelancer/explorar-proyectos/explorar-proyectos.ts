import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SlicePipe } from '@angular/common'; 
import { ProyectoService } from '../../../services/proyecto.service';
import { PropuestaService } from '../../../services/propuesta.service';
import { CategoriaService } from '../../../services/categoria.service';
import { AuthService } from '../../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-explorar-proyectos',
  imports: [FormsModule, RouterLink, SlicePipe],
  templateUrl: './explorar-proyectos.html',
  styleUrl: './explorar-proyectos.css'
})
export class ExplorarProyectos implements OnInit {
  proyectos: any[] = [];
  categorias: any[] = [];
  proyectoSeleccionado: any = null;
  mostrandoPropuesta = false;

  filtros = { categoria: '', min: '', max: '' };
  propuestaForm = { montoOfertado: 0, plazoDias: 1, carta: '' };

  constructor(
    private proyectoService: ProyectoService,
    private propuestaService: PropuestaService,
    private categoriaService: CategoriaService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.cargarProyectos();
    this.categoriaService.listar().subscribe(data => this.categorias = data);
  }

  cargarProyectos() {
    const cat = this.filtros.categoria ? +this.filtros.categoria : undefined;
    const min = this.filtros.min ? +this.filtros.min : undefined;
    const max = this.filtros.max ? +this.filtros.max : undefined;
    this.proyectoService.listarAbiertos(cat, min, max).subscribe(data => this.proyectos = data);
  }

  verDetalle(proyecto: any) {
    this.proyectoSeleccionado = proyecto;
    this.mostrandoPropuesta = false;
    this.propuestaForm = { montoOfertado: 0, plazoDias: 1, carta: '' };
  }

  cerrarDetalle() {
    this.proyectoSeleccionado = null;
  }

  enviarPropuesta() {
    if (!this.propuestaForm.carta || !this.propuestaForm.montoOfertado) {
      Swal.fire('Error', 'Completa todos los campos', 'error'); return;
    }
    if (this.propuestaForm.montoOfertado > this.proyectoSeleccionado.presupuestoMax) {
      Swal.fire('Error', 'El monto no puede superar el presupuesto máximo', 'error'); return;
    }
    const data = { ...this.propuestaForm, idProyecto: this.proyectoSeleccionado.id };
    this.propuestaService.enviar(data).subscribe({
      next: () => {
        Swal.fire('¡Éxito!', 'Propuesta enviada correctamente', 'success');
        this.cerrarDetalle();
        this.cargarProyectos();
      },
      error: (err) => Swal.fire('Error', err.error?.error || 'No se pudo enviar', 'error')
    });
  }
}