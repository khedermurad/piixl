import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RegisterResponse } from '../../models/auth/register-response';
import { RegisterRequest } from '../../models/auth/register-request';
import { LoginRequest } from '../../models/auth/login-request';
import { text } from 'stream/consumers';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';
  private http = inject(HttpClient);

  register(registerRequest: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(this.baseUrl + '/register', registerRequest, {
      withCredentials: true,
    });
  }

  login(loginRequest: LoginRequest): Observable<string> {
    return this.http.post(this.baseUrl + '/login', loginRequest, {
      withCredentials: true,
      responseType: 'text',
    });
  }
}
