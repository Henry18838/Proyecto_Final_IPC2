import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CategoriaService } from '../../../services/categoria.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-gestionar-categorias',
  imports: [FormsModule, RouterLink],
  templateUrl: './gestionar-categorias.html',
  styleUrl: './gestionar-categorias.css'
})
export class GestionarCategorias implements OnInit {
  categorias: any[] = [];
  habilidades: any[] = [];
  nuevaCategoria = '';
  nuevaHabilidad = { nombre: '', descripcion: '', idCategoria: 0 };
  editando: any = null;

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit() {
    this.cargarCategorias();
    this.cargarHabilidades();
  }

  cargarCategorias() {
    this.categoriaService.listarTodas().subscribe(data => this.categorias = data);
  }

  cargarHabilidades() {
    this.categoriaService.listarHabilidades().subscribe(data => this.habilidades = data);
  }

  crearCategoria() {
    if (!this.nuevaCategoria.trim()) return;
    this.categoriaService.crear(this.nuevaCategoria).subscribe({
      next: () => {
        Swal.fire('¡Éxito!', 'Categoría creada', 'success');
        this.nuevaCategoria = '';
        this.cargarCategorias();
      },
      error: () => Swal.fire('Error', 'No se pudo crear la categoría', 'error')
    });
  }

  crearHabilidad() {
    if (!this.nuevaHabilidad.nombre.trim() || !this.nuevaHabilidad.idCategoria) {
      Swal.fire('Error', 'Completa todos los campos', 'error');
      return;
    }
    this.categoriaService.crearHabilidad(this.nuevaHabilidad).subscribe({
      next: () => {
        Swal.fire('¡Éxito!', 'Habilidad creada', 'success');
        this.nuevaHabilidad = { nombre: '', descripcion: '', idCategoria: 0 };
        this.cargarHabilidades();
      },
      error: () => Swal.fire('Error', 'No se pudo crear la habilidad', 'error')
    });
  }

  toggleCategoria(cat: any) {
    this.categoriaService.toggleActiva(cat.id, !cat.activa).subscribe({
      next: () => this.cargarCategorias(),
      error: () => Swal.fire('Error', 'No se pudo actualizar', 'error')
    });
  }

  editarCategoria(cat: any) {
    this.editando = { ...cat };
  }

  guardarEdicion() {
    this.categoriaService.editar(this.editando.id, this.editando.nombre).subscribe({
      next: () => {
        Swal.fire('¡Éxito!', 'Categoría actualizada', 'success');
        this.editando = null;
        this.cargarCategorias();
      },
      error: () => Swal.fire('Error', 'No se pudo actualizar', 'error')
    });
  }
}