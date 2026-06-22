import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RegisterResponse } from '../../models/register-response';
import { RegisterRequest } from '../../models/register-request';
import { toSignal } from '@angular/core/rxjs-interop';

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
}
