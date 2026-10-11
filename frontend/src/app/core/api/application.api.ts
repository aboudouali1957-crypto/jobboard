import { HttpClient, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API, toParams } from '../http-utils';
import {
  ApplicationStatus,
  CandidateApplication,
  Page,
  PageRequest,
  RecruiterApplication,
} from '../models';

@Injectable({ providedIn: 'root' })
export class ApplicationApi {
  private readonly http = inject(HttpClient);

  // --- Candidat

  apply(offerId: number, coverLetter: string | null): Observable<CandidateApplication> {
    return this.http.post<CandidateApplication>(`${API}/candidate/applications`, { offerId, coverLetter });
  }

  mine(status: ApplicationStatus | null, page: PageRequest): Observable<Page<CandidateApplication>> {
    return this.http.get<Page<CandidateApplication>>(`${API}/candidate/applications`, {
      params: toParams({ status }, page),
    });
  }

  appliedOfferIds(): Observable<number[]> {
    return this.http.get<number[]>(`${API}/candidate/applications/offer-ids`);
  }

  withdraw(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/candidate/applications/${id}`);
  }

  // --- Recruteur

  received(
    filters: { offerId: number | null; status: ApplicationStatus | null },
    page: PageRequest,
  ): Observable<Page<RecruiterApplication>> {
    return this.http.get<Page<RecruiterApplication>>(`${API}/recruiter/applications`, {
      params: toParams(filters, page),
    });
  }

  changeStatus(id: number, status: ApplicationStatus, message: string | null): Observable<RecruiterApplication> {
    return this.http.patch<RecruiterApplication>(`${API}/recruiter/applications/${id}/status`, { status, message });
  }

  downloadCv(id: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${API}/recruiter/applications/${id}/cv`, { observe: 'response', responseType: 'blob' });
  }
}
