import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ContratoService {
  private apiUrl = 'http://localhost:8080/connectwork-backend/api/contratos';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  obtener(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.headers() });
  }

  listarActivosFreelancer(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/freelancer`, { headers: this.headers() });
  }

  obtenerEntregas(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/entregas`, { headers: this.headers() });
  }

  subirEntrega(id: number, data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/entrega`, data, { headers: this.headers() });
  }

  aprobar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/aprobar`, {}, { headers: this.headers() });
  }

  rechazar(id: number, motivo: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/rechazar`, { motivo }, { headers: this.headers() });
  }

  cancelar(id: number, motivo: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/cancelar`, { motivo }, { headers: this.headers() });
  }

  calificar(id: number, estrellas: number, comentario: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/calificar`, { estrellas, comentario }, { headers: this.headers() });
  }
}
