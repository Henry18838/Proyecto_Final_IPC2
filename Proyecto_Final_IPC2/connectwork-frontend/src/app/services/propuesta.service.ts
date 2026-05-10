import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class PropuestaService {
  private apiUrl = 'http://localhost:8080/connectwork-backend/api/propuestas';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  listarPorProyecto(idProyecto: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/proyecto/${idProyecto}`, { headers: this.headers() });
  }

  listarMisPropuestas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/freelancer`, { headers: this.headers() });
  }

  enviar(data: any): Observable<any> {
    return this.http.post(this.apiUrl, data, { headers: this.headers() });
  }

  aceptar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/aceptar`, {}, { headers: this.headers() });
  }

  rechazar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/rechazar`, {}, { headers: this.headers() });
  }

  retirar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/retirar`, {}, { headers: this.headers() });
  }
}
