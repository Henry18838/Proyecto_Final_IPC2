export interface Usuario {
  id: number;
  nombre: string;
  username: string;
  email: string;
  rol: string;
  activo: boolean;
  perfilCompleto: boolean;
  saldo: number;
  token?: string;
}

export interface Cliente {
  id: number;
  nombre: string;
  username: string;
  email: string;
  telefono: string;
  descripcion: string;
  sector: string;
  sitioWeb: string;
  activo: boolean;
  saldo: number;
}

export interface Freelancer {
  id: number;
  nombre: string;
  username: string;
  email: string;
  telefono: string;
  bio: string;
  experiencia: string;
  tarifaHora: number;
  habilidades: Habilidad[];
  activo: boolean;
  saldo: number;
  calificacionPromedio: number;
}

export interface Categoria {
  id: number;
  nombre: string;
  activa: boolean;
}

export interface Habilidad {
  id: number;
  nombre: string;
  descripcion: string;
  idCategoria: number;
  nombreCategoria: string;
  activa: boolean;
}

export interface Proyecto {
  id: number;
  idCliente: number;
  nombreCliente: string;
  titulo: string;
  descripcion: string;
  idCategoria: number;
  nombreCategoria: string;
  presupuestoMax: number;
  fechaLimite: string;
  estado: string;
  fechaCreacion: string;
  habilidades: Habilidad[];
}

export interface Propuesta {
  id: number;
  idProyecto: number;
  tituloProyecto: string;
  idFreelancer: number;
  nombreFreelancer: string;
  calificacionFreelancer: number;
  montoOfertado: number;
  plazoDias: number;
  carta: string;
  estado: string;
  fechaEnvio: string;
}

export interface Contrato {
  id: number;
  idPropuesta: number;
  idProyecto: number;
  tituloProyecto: string;
  idCliente: number;
  nombreCliente: string;
  idFreelancer: number;
  nombreFreelancer: string;
  monto: number;
  porcentajeComision: number;
  fechaInicio: string;
  fechaFin: string;
  motivoCancelacion: string;
}

export interface Entrega {
  id: number;
  idContrato: number;
  descripcion: string;
  archivos: string;
  estado: string;
  motivoRechazo: string;
  fechaEntrega: string;
}

export interface Calificacion {
  id: number;
  idContrato: number;
  idCliente: number;
  idFreelancer: number;
  estrellas: number;
  comentario: string;
  fecha: string;
}
