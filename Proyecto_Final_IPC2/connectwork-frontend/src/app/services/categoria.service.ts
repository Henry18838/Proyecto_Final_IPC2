import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private apiUrl = 'http://localhost:8080/connectwork-backend/api/categorias';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { headers: this.headers() });
  }

  listarTodas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/todas`, { headers: this.headers() });
  }

  listarHabilidades(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/habilidades`, { headers: this.headers() });
  }

  listarHabilidadesPorCategoria(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/habilidades`, { headers: this.headers() });
  }

  crear(nombre: string): Observable<any> {
    return this.http.post(this.apiUrl, { nombre }, { headers: this.headers() });
  }

  crearHabilidad(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/habilidad`, data, { headers: this.headers() });
  }

  editar(id: number, nombre: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, { nombre }, { headers: this.headers() });
  }

  toggleActiva(id: number, activa: boolean): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/toggle`, { activa }, { headers: this.headers() });
  }
}