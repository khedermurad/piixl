import { HttpClient } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { RegisterResponse } from '../../models/auth/register-response';
import { RegisterRequest } from '../../models/auth/register-request';
import { LoginRequest } from '../../models/auth/login-request';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';
  private http = inject(HttpClient);

  currentUser = signal<string | null>(null);
  isLoading = signal<boolean>(true);
  private platformId = inject(PLATFORM_ID);

  checkInitialAuth(): Observable<boolean> {
    if (!isPlatformBrowser(this.platformId)) {
      this.isLoading.set(false);
      return of(false);
    }

    return this.http.get<string>(this.baseUrl + '/me').pipe(
      map((user) => {
        this.currentUser.set(user);
        this.isLoading.set(false);
        return true;
      }),
      catchError(() => {
        this.currentUser.set(null);
        this.isLoading.set(false);
        return of(false);
      }),
    );
  }

  register(registerRequest: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(this.baseUrl + '/register', registerRequest);
  }

  login(loginRequest: LoginRequest): Observable<string> {
    return this.http.post(this.baseUrl + '/login', loginRequest, {
      responseType: 'text',
    });
  }
}
