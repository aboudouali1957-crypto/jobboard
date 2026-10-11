import { HttpClient, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../http-utils';
import { CandidateProfile, UpdateProfileRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class CandidateApi {
  private readonly http = inject(HttpClient);

  profile(): Observable<CandidateProfile> {
    return this.http.get<CandidateProfile>(`${API}/candidate/profile`);
  }

  updateProfile(request: UpdateProfileRequest): Observable<CandidateProfile> {
    return this.http.put<CandidateProfile>(`${API}/candidate/profile`, request);
  }

  uploadCv(file: File): Observable<CandidateProfile> {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.http.post<CandidateProfile>(`${API}/candidate/cv`, body);
  }

  downloadCv(): Observable<HttpResponse<Blob>> {
    return this.http.get(`${API}/candidate/cv`, { observe: 'response', responseType: 'blob' });
  }
}
