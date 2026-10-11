import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API, toParams } from '../http-utils';
import {
  OfferDetail,
  OfferFilters,
  OfferRequest,
  OfferStatus,
  OfferSummary,
  Page,
  PageRequest,
  RecruiterOffer,
} from '../models';

@Injectable({ providedIn: 'root' })
export class OfferApi {
  private readonly http = inject(HttpClient);

  // --- Public

  search(filters: OfferFilters, page: PageRequest): Observable<Page<OfferSummary>> {
    return this.http.get<Page<OfferSummary>>(`${API}/offers`, { params: toParams(filters, page) });
  }

  get(id: number): Observable<OfferDetail> {
    return this.http.get<OfferDetail>(`${API}/offers/${id}`);
  }

  cities(): Observable<string[]> {
    return this.http.get<string[]>(`${API}/offers/cities`);
  }

  // --- Recruteur

  mine(page: PageRequest): Observable<Page<RecruiterOffer>> {
    return this.http.get<Page<RecruiterOffer>>(`${API}/recruiter/offers`, { params: toParams({}, page) });
  }

  getMine(id: number): Observable<OfferDetail> {
    return this.http.get<OfferDetail>(`${API}/recruiter/offers/${id}`);
  }

  create(request: OfferRequest): Observable<OfferDetail> {
    return this.http.post<OfferDetail>(`${API}/recruiter/offers`, request);
  }

  update(id: number, request: OfferRequest): Observable<OfferDetail> {
    return this.http.put<OfferDetail>(`${API}/recruiter/offers/${id}`, request);
  }

  changeStatus(id: number, status: OfferStatus): Observable<OfferDetail> {
    return this.http.patch<OfferDetail>(`${API}/recruiter/offers/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/recruiter/offers/${id}`);
  }
}
