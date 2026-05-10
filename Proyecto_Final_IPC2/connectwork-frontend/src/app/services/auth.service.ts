import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/connectwork-backend/api';

  constructor(private http: HttpClient) {}

  registro(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/registro`, data);
  }

  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, { username, password });
  }

  guardarSesion(data: any): void {
    localStorage.setItem('token', data.token);
    localStorage.setItem('usuario', JSON.stringify(data));
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUsuario(): any {
    const u = localStorage.getItem('usuario');
    return u ? JSON.parse(u) : null;
  }

  getRol(): string {
    return this.getUsuario()?.rol || '';
  }

  getId(): number {
    return this.getUsuario()?.id || 0;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  perfilCompleto(): boolean {
    return this.getUsuario()?.perfilCompleto || false;
  }

  actualizarPerfilCompleto(): void {
    const u = this.getUsuario();
    if (u) {
      u.perfilCompleto = true;
      localStorage.setItem('usuario', JSON.stringify(u));
    }
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
  }
}
