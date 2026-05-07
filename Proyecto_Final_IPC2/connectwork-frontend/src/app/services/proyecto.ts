import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ProyectoService {
  private apiUrl = 'http://localhost:8080/connectwork-backend/api/proyectos';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  listarAbiertos(categoria?: number, min?: number, max?: number): Observable<any[]> {
    let params: any = {};
    if (categoria) params['categoria'] = categoria;
    if (min) params['min'] = min;
    if (max) params['max'] = max;
    return this.http.get<any[]>(this.apiUrl, { headers: this.headers(), params });
  }

  listarPorCliente(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/cliente`, { headers: this.headers() });
  }

  obtener(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.headers() });
  }

  crear(data: any): Observable<any> {
    return this.http.post(this.apiUrl, data, { headers: this.headers() });
  }

  editar(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data, { headers: this.headers() });
  }

  cancelar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { headers: this.headers() });
  }
}
