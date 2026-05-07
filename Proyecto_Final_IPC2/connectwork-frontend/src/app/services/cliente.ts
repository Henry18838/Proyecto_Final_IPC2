import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private apiUrl = 'http://localhost:8080/connectwork-backend/api/clientes';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  obtener(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.headers() });
  }

  listarTodos(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { headers: this.headers() });
  }

  completarPerfil(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/perfil`, data, { headers: this.headers() });
  }

  recargarSaldo(id: number, monto: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/recargar`, { monto }, { headers: this.headers() });
  }
}
