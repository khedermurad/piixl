import { HttpClient } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { RegisterResponse } from '../../models/auth/register-response';
import { RegisterRequest } from '../../models/auth/register-request';
import { LoginRequest } from '../../models/auth/login-request';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../../../environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = environment.backendUrl;
  private http = inject(HttpClient);

  currentUser = signal<string | null>(null);
  isLoading = signal<boolean>(true);
  private platformId = inject(PLATFORM_ID);

  checkInitialAuth(): Observable<boolean> {
    if (!isPlatformBrowser(this.platformId)) {
      this.isLoading.set(false);
      return of(false);
    }

    return this.http.get<string>(this.apiUrl + '/me').pipe(
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
    return this.http.post<RegisterResponse>(this.apiUrl + '/register', registerRequest);
  }

  login(loginRequest: LoginRequest): Observable<string> {
    return this.http.post(this.apiUrl + '/login', loginRequest, {
      responseType: 'text',
    });
  }
}
